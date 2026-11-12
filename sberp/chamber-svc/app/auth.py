"""
로그인과 세션.

- 계정은 ERP MEMBER_TB 를 읽어 확인한다. 해시는 ERP(EgovFileScrty.encryptPassword)와 같다.
- 세션은 서명된 쿠키에 담는다. 서버 메모리에 두지 않으므로 컨테이너를 다시 띄워도 유지된다.
- 실패 횟수와 계정 잠금은 ERP 와 같은 컬럼(MEMBER_TB.FAIL_PASS_CNT, LOCK_YN)을 쓴다.
  현황판에서 틀린 횟수와 ERP 에서 틀린 횟수가 합쳐지고, 10회가 되면 ERP 로그인도 잠긴다.
  잠금 해제는 ERP 관리자가 한다.
- 비밀번호와 쿠키 값은 로그에 남기지 않는다.
"""
import base64
import hashlib
import hmac
import json
import time
from typing import Optional

from fastapi import HTTPException, Request, Response

from . import db
from .config import settings

COOKIE = "chamber_session"
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

    db.login_succeeded(member["ID"])
    is_monitor = bool(settings.monitor_mem_id) and member["ID"] == settings.monitor_mem_id
    max_age = settings.monitor_days * 86400 if is_monitor else settings.session_hours * 3600
    session = {
        "id": member["ID"],
        "name": member["MEM_NAME"] or member["ID"],
        "rev": bool(member["REV_YN"]) and not is_monitor,
        "mon": is_monitor,
        "exp": int(time.time()) + max_age,
    }
    response.set_cookie(
        COOKIE, _pack(session), max_age=max_age, path=settings.cookie_path,
        httponly=True, secure=settings.cookie_secure, samesite="lax",
    )
    return session


def logout(response: Response) -> None:
    response.delete_cookie(COOKIE, path=settings.cookie_path)


def require_login(request: Request) -> dict:
    session = _unpack(request.cookies.get(COOKIE, ""))
    if not session:
        raise HTTPException(401, "로그인이 필요합니다.")
    return session


def require_writer(request: Request) -> dict:
    """모니터 전용 계정은 조회만 할 수 있다."""
    session = require_login(request)
    if session.get("mon"):
        raise HTTPException(403, "조회 전용 계정입니다.")
    return session


def require_rev(request: Request) -> dict:
    """기술책임자(MEMBER_TB.REV_YN=1)만. 챔버 관리에 쓴다."""
    session = require_writer(request)
    if not session.get("rev"):
        raise HTTPException(403, "기술책임자만 할 수 있습니다.")
    return session
