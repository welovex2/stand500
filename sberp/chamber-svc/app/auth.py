"""
로그인과 세션.

- 계정은 ERP MEMBER_TB 를 읽어 확인한다. 해시는 ERP(EgovFileScrty.encryptPassword)와 같다.
- 세션은 서명된 쿠키에 담는다. 서버 메모리에 두지 않으므로 컨테이너를 다시 띄워도 유지된다.
- 사용하는 동안 연장한다 (마지막 사용 후 session_hours 가 지나면 끊김). 남은 시간이 절반 아래일 때 사람이 보낸
  요청이 오면 ERP 에서 잠금·퇴사·권한을 다시 읽고 새 쿠키를 준다. 화면 자동 새로고침(X-Chamber-Poll)은 연장하지
  않는다 — 화면을 켜 두기만 한 PC 가 계속 로그인돼 있지 않게. 모니터 계정은 자동 새로고침으로도 연장한다.
- 실패 횟수와 계정 잠금은 ERP 와 같은 컬럼(MEMBER_TB.FAIL_PASS_CNT, LOCK_YN)을 쓴다.
  현황판에서 틀린 횟수와 ERP 에서 틀린 횟수가 합쳐지고, 10회가 되면 ERP 로그인도 잠긴다.
  잠금 해제는 ERP 관리자가 한다.
- 비밀번호와 쿠키 값은 로그에 남기지 않는다.
- 모니터 전용 주소(…/monitor?key=MONITOR_KEY): 일반 로그인과 다른 쿠키(chamber_kiosk)에 조회 전용 세션을 담는다.
  화면이 X-Chamber-Kiosk 머리글을 보낼 때만 그 쿠키를 쓰므로, 같은 PC 에서 누가 로그인·로그아웃해도 모니터는 그대로다.
  쿠키에는 키의 지문만 넣는다. MONITOR_KEY 를 바꾸면 지문이 달라져 기존 모니터가 끊긴다.
"""
import base64
import hashlib
import hmac
import json
import time
from typing import Optional

from fastapi import HTTPException, Request, Response

from . import db
from .config import ADMIN_AUTH_CODES, REV_AUTH_CODE, settings

COOKIE = "chamber_session"
KIOSK_COOKIE = "chamber_kiosk"
KIOSK_MAX_AGE = 400 * 86400          # 브라우저가 허용하는 최대 쿠키 기간. 쓰는 동안 계속 연장한다
_KEY = settings.session_secret.encode()


def encrypt_password(password: str, user_id: str) -> str:
    """SHA-256(아이디 + 비밀번호) → Base64"""
    md = hashlib.sha256()
    md.update(user_id.encode())
    md.update(password.encode())
    return base64.b64encode(md.digest()).decode()


def _sign(body: str) -> str:
    return base64.urlsafe_b64encode(hmac.new(_KEY, body.encode(), hashlib.sha256).digest()).decode()


def _pack(session: dict) -> str:
    body = base64.urlsafe_b64encode(json.dumps(session, ensure_ascii=False).encode()).decode()
    return body + "." + _sign(body)


def _unpack(value: str) -> Optional[dict]:
    body, _, sig = value.partition(".")
    if not body or not hmac.compare_digest(sig, _sign(body)):
        return None
    try:
        session = json.loads(base64.urlsafe_b64decode(body.encode()))
    except ValueError:
        return None
    if not isinstance(session, dict) or session.get("exp", 0) < time.time():
        return None
    return session


def login(mem_id: str, password: str, response: Response) -> dict:
    """성공하면 쿠키를 심고 세션 내용을 돌려준다. 실패하면 HTTPException."""
    fail = HTTPException(401, "아이디 또는 비밀번호가 올바르지 않습니다.")
    mem_id = mem_id.strip()
    if not mem_id or len(mem_id) > 20 or not password:
        raise fail

    member = db.fetch_member(mem_id)
    if not member:
        raise fail
    locked = HTTPException(403, "계정이 잠겼습니다. ERP 관리자에게 문의하세요.")
    if member["LOCK_YN"]:
        raise locked
    if not hmac.compare_digest(encrypt_password(password, member["ID"]), member["PASSWORD"] or ""):
        cnt = db.login_failed(member["ID"])
        if cnt >= settings.login_fail_max:
            raise locked
        raise HTTPException(
            401, f"아이디 또는 비밀번호가 올바르지 않습니다. (암호 {settings.login_fail_max}회중 / 현재 {cnt}회 오류)")
    if member["QUIT_YN"]:
        raise HTTPException(403, "사용할 수 없는 계정입니다. 관리자에게 문의하세요.")
    # ERP 는 초기 비밀번호(아이디 + "300")면 변경부터 시킨다. 현황판에는 변경 화면이 없으므로 ERP 로 보낸다
    if hmac.compare_digest(encrypt_password(member["ID"] + "300", member["ID"]), member["PASSWORD"] or ""):
        raise HTTPException(403, "초기 비밀번호입니다. ERP에서 비밀번호를 변경한 뒤 로그인하세요.")

    session = _session_of(member)
    if session is None:
        raise HTTPException(403, "현황판은 EMC 부서만 사용할 수 있습니다.")

    db.login_succeeded(member["ID"])
    _set_cookie(response, session)
    return session


def _max_age(is_monitor: bool) -> int:
    return settings.monitor_days * 86400 if is_monitor else settings.session_hours * 3600


def _session_of(member: dict) -> Optional[dict]:
    """ERP 회원 정보 → 세션 내용. 현황판을 쓸 수 없는 사람이면 None"""
    is_monitor = bool(settings.monitor_mem_id) and member["ID"] == settings.monitor_mem_id
    role = "mon" if is_monitor else role_of(member)
    if role is None:
        return None
    return {
        "id": member["ID"],
        "name": member["MEM_NAME"] or member["ID"],
        "role": role,
        # 기술책임자·관리자: 다른 사람의 사용 종료·삭제, 시험원 선택
        "rev": role in ("rev", "admin"),
        # 관리자(AUTH_CODE P01·P02): 관리 탭(챔버 관리) · 사용 이력 기록 수정. 키 이름은 예전 세션과 맞추려고 p01 그대로
        "p01": (member.get("AUTH_CODE") or "").strip().upper() in ADMIN_AUTH_CODES,
        "mon": is_monitor,
        "exp": int(time.time()) + _max_age(is_monitor),
    }


def _set_cookie(response: Response, session: dict) -> None:
    response.set_cookie(
        COOKIE, _pack(session), max_age=_max_age(bool(session.get("mon"))), path=settings.cookie_path,
        httponly=True, secure=settings.cookie_secure, samesite="lax",
    )


def role_of(member: dict) -> Optional[str]:
    """admin(관리자: AUTH_CODE P01·P02, 부서 상관없음) / rev(기술책임자: EMC 부서 + P04) / user(EMC 부서).
    그 밖의 부서는 None (로그인 거부)"""
    auth_code = (member.get("AUTH_CODE") or "").strip().upper()
    if auth_code in ADMIN_AUTH_CODES:
        return "admin"
    if member.get("DEPT_SEQ") != settings.emc_dept_seq:
        return None
    return "rev" if auth_code == REV_AUTH_CODE else "user"


def logout(response: Response) -> None:
    response.delete_cookie(COOKIE, path=settings.cookie_path)


# ---------------------------------------------------------------- 모니터 전용 주소

def _kiosk_fp() -> str:
    return hashlib.sha256(("kiosk:" + settings.monitor_key).encode()).hexdigest()[:24]


def open_kiosk(key: str, response: Response) -> None:
    """…/monitor?key= 로 들어왔을 때. 키가 맞으면 모니터 쿠키를 심는다"""
    if not settings.monitor_key or not hmac.compare_digest(key.encode(), settings.monitor_key.encode()):
        raise HTTPException(404, "Not Found")
    _set_kiosk(response)


def _set_kiosk(response: Response) -> dict:
    session = {"id": "@monitor", "name": "모니터", "role": "mon", "rev": False, "p01": False, "mon": True,
               "kiosk": True, "fp": _kiosk_fp(), "exp": int(time.time()) + KIOSK_MAX_AGE}
    response.set_cookie(KIOSK_COOKIE, _pack(session), max_age=KIOSK_MAX_AGE, path=settings.cookie_path,
                        httponly=True, secure=settings.cookie_secure, samesite="lax")
    return session


def kiosk_ok(request: Request) -> bool:
    """이 브라우저가 모니터로 등록돼 있는지 (…/monitor?key= 로 연 적이 있고 키가 그대로인지). 쿠키는 건드리지 않는다"""
    session = _unpack(request.cookies.get(KIOSK_COOKIE, ""))
    return bool(session and settings.monitor_key and session.get("kiosk")
                and hmac.compare_digest(str(session.get("fp", "")), _kiosk_fp()))


def _kiosk_session(request: Request, response: Response) -> dict:
    session = _unpack(request.cookies.get(KIOSK_COOKIE, ""))
    if (not session or not settings.monitor_key or not session.get("kiosk")
            or not hmac.compare_digest(str(session.get("fp", "")), _kiosk_fp())):
        response.delete_cookie(KIOSK_COOKIE, path=settings.cookie_path)
        raise HTTPException(401, "모니터 주소로 다시 열어 주세요.")
    if session["exp"] - time.time() < KIOSK_MAX_AGE / 2:
        session = _set_kiosk(response)      # 켜 두는 동안 만료되지 않게 연장
    return session


def require_login(request: Request, response: Response) -> dict:
    if request.headers.get("X-Chamber-Kiosk") == "1":
        return _kiosk_session(request, response)       # 모니터 화면: 일반 로그인과 무관
    session = _unpack(request.cookies.get(COOKIE, ""))
    if not session:
        raise HTTPException(401, "로그인이 필요합니다.")
    return _renew(request, response, session)


def _renew(request: Request, response: Response, session: dict) -> dict:
    """사용 중이면 연장. 남은 시간이 절반 아래일 때만 (요청마다 DB 를 읽지 않게)"""
    poll = request.headers.get("X-Chamber-Poll") == "1"
    if poll and not session.get("mon"):
        return session                      # 자동 새로고침은 연장하지 않는다
    left = session.get("exp", 0) - time.time()
    if left > _max_age(bool(session.get("mon"))) / 2:
        return session
    member = db.fetch_member(session["id"])
    fresh = _session_of(member) if member and not member["LOCK_YN"] and not member["QUIT_YN"] else None
    if fresh is None:
        # ERP 에서 잠겼거나 퇴사·부서 이동으로 쓸 수 없게 됨 → 로그아웃
        response.delete_cookie(COOKIE, path=settings.cookie_path)
        raise HTTPException(401, "로그인 정보가 바뀌었습니다. 다시 로그인하세요.")
    _set_cookie(response, fresh)
    return fresh


def require_writer(request: Request, response: Response) -> dict:
    """모니터 전용 계정은 조회만 할 수 있다."""
    session = require_login(request, response)
    if session.get("mon"):
        raise HTTPException(403, "조회 전용 계정입니다.")
    return session


def require_p01(request: Request, response: Response) -> dict:
    """관리 탭(챔버 관리) · 기록 수정은 관리자(AUTH_CODE P01·P02) 만"""
    session = require_writer(request, response)
    if not session.get("p01"):
        raise HTTPException(403, "관리자(P01·P02)만 사용할 수 있습니다.")
    return session


def require_rev_view(request: Request, response: Response) -> dict:
    """기술책임자·관리자만 볼 수 있는 화면 (시험원 현황). 모니터 화면 · 일반 시험원은 볼 수 없다"""
    session = require_login(request, response)
    if session.get("mon") or not session.get("rev"):
        raise HTTPException(403, "시험원 현황은 기술책임자·관리자만 볼 수 있습니다.")
    return session


def require_rev(request: Request, response: Response) -> dict:
    """기술책임자·관리자만 (시험원 선택 목록 등)"""
    session = require_writer(request, response)
    if not session.get("rev"):
        raise HTTPException(403, "기술책임자·관리자만 할 수 있습니다.")
    return session
