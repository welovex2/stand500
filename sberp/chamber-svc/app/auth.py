"""
로그인과 세션.

- 계정은 ERP MEMBER_TB 를 읽어 확인한다. 해시는 ERP(EgovFileScrty.encryptPassword)와 같다.
- 세션은 서명된 쿠키에 담는다. 서버 메모리에 두지 않으므로 컨테이너를 다시 띄워도 유지된다.
- 실패 횟수는 ops.LOGIN_FAIL_TB 에 센다. ERP 테이블에는 쓰지 않는다.
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

    left = db.login_blocked_min(mem_id)
    if left:
        raise HTTPException(429, f"로그인 실패가 많아 잠시 막혔습니다. {left}분 뒤에 다시 시도하세요.")

    member = db.fetch_member(mem_id)
    if not member:
        raise fail  # 없는 아이디는 실패 횟수를 세지 않는다 (테이블이 임의 값으로 차지 않게)
    if not hmac.compare_digest(encrypt_password(password, member["ID"]), member["PASSWORD"] or ""):
        db.login_failed(member["ID"])
        raise fail
    if member["LOCK_YN"] or member["QUIT_YN"]:
        raise HTTPException(403, "사용할 수 없는 계정입니다. 관리자에게 문의하세요.")

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
