import logging
import mimetypes
from datetime import date, timedelta
from pathlib import Path
from typing import List, Optional

import pymysql
from fastapi import Depends, FastAPI, HTTPException, Query, Request, Response
from fastapi.responses import JSONResponse
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel, Field

from . import auth, db
from .config import MET_KEYS, PURPOSE_TEST, PURPOSES, TEST_STATE_NAMES, settings

logging.basicConfig(level=logging.INFO)
log = logging.getLogger("chamber-svc")

app = FastAPI(title="sberp chamber-svc", version="0.1.0", docs_url=None, redoc_url=None, openapi_url=None)

STATIC = Path(__file__).parent / "static"
# 글꼴 파일 형식 (python:slim 이미지는 woff2 를 몰라 text/plain 으로 보낸다)
mimetypes.add_type("font/woff2", ".woff2")
MAX_DAYS = 366


# ---------------------------------------------------------------- 오류 응답

@app.exception_handler(db.Reject)
def _reject(_: Request, e: db.Reject) -> JSONResponse:
    body = {"detail": e.message}
    if e.detail and e.detail.get("confirm"):
        body["confirm"] = True          # 화면이 확인을 받은 뒤 force=true 로 다시 보낸다
    elif e.detail:
        body["busy"] = _use(e.detail)
    return JSONResponse(body, status_code=e.status)


@app.exception_handler(db.DbBusy)
def _db_busy(_: Request, __: db.DbBusy) -> JSONResponse:
    return JSONResponse({"detail": "요청이 몰려 처리하지 못했습니다. 잠시 뒤 다시 시도하세요."}, status_code=503)


@app.exception_handler(pymysql.MySQLError)
def _db_error(_: Request, e: pymysql.MySQLError) -> JSONResponse:
    log.error("DB 오류: %s", e)
    return JSONResponse({"detail": "DB에 연결하지 못했습니다."}, status_code=503)


# ---------------------------------------------------------------- 응답 모양

def _dt(v) -> str:
    return v.strftime("%Y-%m-%d %H:%M") if v else ""


def _use(r: dict, session: Optional[dict] = None) -> dict:
    """사용 건 한 줄 (측정 행 RAW / 로데이터 외 사용 ETC)"""
    is_etc = r["SRC"] == "ETC"
    elapsed = r.get("ELAPSED_MIN")
    out = {
        "src": r["SRC"],
        "seq": r.get("SEQ"),
        "testId": r.get("TEST_ID") or "",
        "prdctName": r.get("PRDCT_NAME") or "",
        "cmpyName": r.get("CMPY_NAME") or "",
        "metKey": r.get("MET_KEY") or "",
        "purpose": r.get("PURPOSE") or "",
        "purposeName": PURPOSES.get(r.get("PURPOSE") or "", r.get("PURPOSE") or ""),
        "memo": r.get("MEMO") or "",
        "memName": r.get("MEM_NAME") or "",
        "startDt": _dt(r.get("S_DT")),
    }
    if r.get("CHAMBER_NAME") and session is None:
        out["chamberName"] = r["CHAMBER_NAME"]   # 사용 중 거부 응답: 어느 시험장인지
    if elapsed is not None:
        out["elapsedMin"] = int(elapsed)
        out["stale"] = int(elapsed) >= settings.stale_hours * 60
    if session is not None:
        out["canEnd"] = is_etc and not session.get("mon") and (
            session.get("rev") or r.get("MEM_ID") == session["id"])
    return out


def _chambers(rows: List[dict], session: dict) -> List[dict]:
    """구역별 줄을 챔버 > 구역 > 사용 건으로 묶는다 (MULTI_YN=1 구역은 사용 건이 여러 개).
    연계 구역이 있는 구역을 쓰는 중이면, 연계 대상 구역의 linked 에 그 사용 건을 붙인다 (장비 이관 중 표시).
    soloYn=1 챔버는 구역 중 한 번에 하나만 쓴다 (화면은 카드 하나 + 항목 선택)"""
    chambers: List[dict] = []
    by_zone: dict = {}
    for r in rows:
        if not chambers or chambers[-1]["chamberSeq"] != r["CHAMBER_SEQ"]:
            chambers.append({"chamberSeq": r["CHAMBER_SEQ"], "chamberName": r["CHAMBER_NAME"],
                             "floor": int(r["FLOOR"]), "soloYn": int(r.get("SOLO_YN") or 0), "zones": []})
        zones = chambers[-1]["zones"]
        if not zones or zones[-1]["zoneSeq"] != r["ZONE_SEQ"]:
            zones.append({"zoneSeq": r["ZONE_SEQ"], "zoneName": r["ZONE_NAME"],
                          "multiYn": int(r["MULTI_YN"]), "linkZoneSeq": r.get("LINK_ZONE_SEQ"),
                          "uses": [], "linked": []})
            by_zone[r["ZONE_SEQ"]] = zones[-1]
        if r["SRC"]:
            zones[-1]["uses"].append(_use(r, session))
    for c in chambers:
        for z in c["zones"]:
            target = by_zone.get(z["linkZoneSeq"])
            if not target:
                continue
            for u in z["uses"]:
                # 연계 대상에서는 종료할 수 없다 (원래 시험을 종료하면 함께 풀린다)
                target["linked"].append(dict(u, canEnd=False, linkFrom=c["chamberName"], linkZone=z["zoneName"]))
    return chambers


# ---------------------------------------------------------------- 로그인

class LoginIn(BaseModel):
    id: str = Field(max_length=50)
    password: str = Field(max_length=200)


def _me(session: dict) -> dict:
    return {
        "memId": session["id"],
        "memName": session["name"],
        "revYn": bool(session.get("rev")),
        # user 일반 사용자 / rev 기술책임자 / admin 관리자 / mon 조회 전용 (예전 세션은 role 이 없다)
        "role": session.get("role") or ("mon" if session.get("mon") else "rev" if session.get("rev") else "user"),
        "monitorYn": bool(session.get("mon")),
        "adminMenu": bool(session.get("p01")),   # 관리 탭 (P01 만)
        "staleHours": settings.stale_hours,
        # "시험번호 없이 사용" 사유 (TEST 제외)
        "purposes": [{"value": k, "label": v} for k, v in PURPOSES.items() if k != PURPOSE_TEST],
    }


@app.post("/api/login")
def login(body: LoginIn, response: Response) -> dict:
    session = auth.login(body.id, body.password, response)
    log.info("로그인: %s", session["id"])
    return _me(session)


@app.post("/api/logout")
def logout(response: Response) -> dict:
    auth.logout(response)
    return {"ok": True}


@app.get("/api/me")
def me(session: dict = Depends(auth.require_login)) -> dict:
    return _me(session)


# ---------------------------------------------------------------- 현황·이력

@app.get("/api/status")
def status(session: dict = Depends(auth.require_login)) -> dict:
    return {"chambers": _chambers(db.fetch_status(), session)}


@app.get("/api/zones")
def zones(session: dict = Depends(auth.require_writer)) -> dict:
    """[사용] 때 고를 구역 목록. MULTI_YN=1 구역은 항상 inUse=false"""
    out = []
    for c in _chambers(db.fetch_status(), session):
        for z in c["zones"]:
            out.append({
                "zoneSeq": z["zoneSeq"], "chamberName": c["chamberName"], "zoneName": z["zoneName"],
                "multiYn": z["multiYn"], "inUse": bool(z["uses"] or z["linked"]) and not z["multiYn"],
            })
    return {"zones": out}


@app.get("/api/chambers")
def chambers(_: dict = Depends(auth.require_login)) -> dict:
    """이력 화면의 챔버 선택 목록"""
    return {"chambers": [{"chamberSeq": r["CHAMBER_SEQ"], "chamberName": r["NAME"], "floor": int(r["FLOOR"])}
                         for r in db.fetch_chambers()]}


@app.get("/api/history")
def history(
    date_from: Optional[date] = Query(None, alias="from"),
    date_to: Optional[date] = Query(None, alias="to"),
    chamber_seq: Optional[int] = Query(None, alias="chamberSeq"),
    floor: Optional[int] = None,
    src: str = "",
    deleted: bool = False,
    _: dict = Depends(auth.require_login),
) -> dict:
    """deleted=true 이면 현황판에서 삭제한 건도 함께 (사용 시간 합계에서는 뺀다)"""
    date_to = date_to or date.today()
    date_from = date_from or (date_to - timedelta(days=30))
    if date_from > date_to:
        raise HTTPException(400, "시작일이 종료일보다 늦습니다.")
    if (date_to - date_from).days > MAX_DAYS:
        raise HTTPException(400, "조회 기간은 최대 1년입니다.")
    if src not in ("", "RAW", "ETC", "TEST", "NOTEST"):
        raise HTTPException(400, "알 수 없는 조건입니다.")

    rows = db.fetch_history(date_from, date_to, chamber_seq, src, deleted, floor)
    limit = settings.history_limit
    shown = rows[:limit]
    out = []
    for r in shown:
        row = _use(r)
        row.update(chamberName=r["CHAMBER_NAME"], floor=int(r["FLOOR"]), zoneName=r["ZONE_NAME"],
                   endDt=_dt(r["E_DT"]), useMin=int(r["USE_MIN"] or 0),
                   delYn=bool(r["DEL_YN"]))
        if row["delYn"]:
            row.update(delReason=r["DEL_REASON"], delMemName=r["DEL_MEM_NAME"], delDt=_dt(r["DEL_DT"]))
        out.append(row)
    return {
        "from": date_from.isoformat(),
        "to": date_to.isoformat(),
        "rows": out,
        "totalMin": sum(r["useMin"] for r in out if not r["delYn"]),
        "truncated": len(rows) > limit,   # 한도를 넘어 일부만 보여 줌 (합계도 보이는 행만)
    }


# ---------------------------------------------------------------- 로데이터 외 사용

class EtcStartIn(BaseModel):
    zoneSeq: int
    purpose: str = ""                  # 시험번호 없이 사용할 때 사유. 시험번호가 있으면 TEST
    testSeq: Optional[int] = None
    memId: Optional[str] = None        # 시험원. 기술책임자·관리자만 고를 수 있다 (그 시험의 담당자 중에서)
    memo: str = ""


class EtcDeleteIn(BaseModel):
    reason: str = ""


@app.get("/api/tests")
def tests(session: dict = Depends(auth.require_writer)) -> dict:
    """사용 시작 때 고를 시험번호. 접수·시험중·디버깅 · EMC · 자동화 로데이터 규격 제외.
    일반 사용자는 본인이 담당자인 시험만, 기술책임자·관리자는 전체"""
    rows = db.fetch_tests(None if session.get("rev") else session["id"])
    return {"tests": [{
        "testSeq": r["TEST_SEQ"], "testId": r["TEST_ID"] or "", "prdctName": r["PRDCT_NAME"] or "",
        "cmpyName": r.get("CMPY_NAME") or "",
        "stateName": TEST_STATE_NAMES.get(int(r["TEST_STATE_CODE"] or 0), "") if r.get("TEST_STATE_CODE") is not None else "",
        "mngrs": [{"memId": m["ID"], "memName": m["NAME"]} for m in r["MNGRS"]],
    } for r in rows]}


@app.post("/api/etc-use/start")
def etc_start(body: EtcStartIn, session: dict = Depends(auth.require_writer)) -> dict:
    memo = body.memo.strip()
    if len(memo) > 200:
        raise HTTPException(400, "메모는 200자까지 입력할 수 있습니다.")
    mem_id, mem_name = session["id"], session["name"]
    if body.testSeq is not None:
        purpose = PURPOSE_TEST
        test = db.fetch_test(body.testSeq, None if session.get("rev") else session["id"])
        if not test:
            raise HTTPException(400, "고를 수 없는 시험번호입니다. (접수·시험중·디버깅인 EMC 시험, 일반 사용자는 본인 담당 시험만)")
        if body.memId and body.memId != session["id"]:
            if not session.get("rev"):
                raise HTTPException(403, "시험원은 본인으로만 시작할 수 있습니다.")
            pick = next((m for m in test["MNGRS"] if m["ID"] == body.memId), None)
            if not pick:
                raise HTTPException(400, "그 시험의 담당자가 아닙니다.")
            mem_id, mem_name = pick["ID"], pick["NAME"]
    else:
        purpose = body.purpose
        if purpose not in PURPOSES or purpose == PURPOSE_TEST:
            raise HTTPException(400, "시험번호를 고르거나, 시험번호 없이 사용할 사유를 선택하세요.")
    use_seq = db.start_etc_use(body.zoneSeq, purpose, body.testSeq, memo, mem_id, mem_name, session["id"])
    log.info("사용 시작: use=%s zone=%s %s test=%s mem=%s by %s",
             use_seq, body.zoneSeq, purpose, body.testSeq, mem_id, session["id"])
    return {"useSeq": use_seq}


@app.post("/api/etc-use/{use_seq}/end")
def etc_end(use_seq: int, session: dict = Depends(auth.require_writer)) -> dict:
    db.end_etc_use(use_seq, session["id"], bool(session.get("rev")))
    log.info("사용 종료: use=%s by %s", use_seq, session["id"])
    return {"ok": True}


@app.post("/api/etc-use/{use_seq}/delete")
def etc_delete(use_seq: int, body: EtcDeleteIn, session: dict = Depends(auth.require_writer)) -> dict:
    reason = body.reason.strip()
    if not reason:
        raise HTTPException(400, "삭제 사유를 입력하세요.")
    if len(reason) > 200:
        raise HTTPException(400, "삭제 사유는 200자까지 입력할 수 있습니다.")
    db.delete_etc_use(use_seq, reason, session["id"], bool(session.get("rev")))
    log.info("사용 삭제: use=%s by %s", use_seq, session["id"])
    return {"ok": True}


# ---------------------------------------------------------------- 챔버 관리 (AUTH_CODE P01 만)

class ChamberIn(BaseModel):
    name: str = Field(max_length=50)
    floor: int = Field(1, ge=1, le=99)   # 층 (현황판 층 탭·평면도 구분)
    soloYn: bool = False                 # 구역 단독 사용 (구역이 여러 개여도 한 번에 하나만)
    sortNo: int = Field(0, ge=0, le=32767)
    useYn: bool = True
    force: bool = False


class ZoneIn(BaseModel):
    chamberSeq: int
    name: str = Field("", max_length=50)   # 구역이 하나뿐인 챔버는 빈 값
    multiYn: bool = False
    linkZoneSeq: Optional[int] = None      # 이 구역을 쓰면 함께 점유되는 구역 (장비 이관)
    sortNo: int = Field(0, ge=0, le=32767)
    useYn: bool = True
    metKeys: List[str] = []
    force: bool = False


@app.get("/api/admin/chambers")
def admin_chambers(_: dict = Depends(auth.require_p01)) -> dict:
    t = db.fetch_admin_tree()
    mets: dict = {}
    for m in t["mets"]:
        mets.setdefault(m["ZONE_SEQ"], []).append(m["MET_KEY"])
    zones: dict = {}
    for z in t["zones"]:
        zones.setdefault(z["CHAMBER_SEQ"], []).append({
            "zoneSeq": z["ZONE_SEQ"], "chamberSeq": z["CHAMBER_SEQ"], "name": z["NAME"],
            "multiYn": bool(z["MULTI_YN"]), "linkZoneSeq": z["LINK_ZONE_SEQ"],
            "sortNo": int(z["SORT_NO"]), "useYn": bool(z["USE_YN"]),
            "metKeys": [k for k in MET_KEYS if k in mets.get(z["ZONE_SEQ"], [])],
            "activeCnt": t["active"].get(z["ZONE_SEQ"], 0),
        })
    return {
        "metKeys": MET_KEYS,
        "chambers": [{
            "chamberSeq": c["CHAMBER_SEQ"], "name": c["NAME"], "floor": int(c["FLOOR"]),
            "soloYn": bool(c["SOLO_YN"]), "sortNo": int(c["SORT_NO"]),
            "useYn": bool(c["USE_YN"]), "zones": zones.get(c["CHAMBER_SEQ"], []),
        } for c in t["chambers"]],
    }


def _save_chamber(seq: Optional[int], body: ChamberIn, session: dict) -> dict:
    name = body.name.strip()
    if not name:
        raise HTTPException(400, "챔버 이름을 입력하세요.")
    seq = db.save_chamber(seq, name, body.floor, int(body.soloYn), body.sortNo, int(body.useYn), session["id"],
                          body.force)
    log.info("챔버 저장: chamber=%s floor=%s use=%s by %s", seq, body.floor, int(body.useYn), session["id"])
    return {"chamberSeq": seq}


@app.post("/api/admin/chamber")
def admin_chamber_add(body: ChamberIn, session: dict = Depends(auth.require_p01)) -> dict:
    return _save_chamber(None, body, session)


@app.post("/api/admin/chamber/{chamber_seq}")
def admin_chamber_edit(chamber_seq: int, body: ChamberIn, session: dict = Depends(auth.require_p01)) -> dict:
    return _save_chamber(chamber_seq, body, session)


def _save_zone(seq: Optional[int], body: ZoneIn, session: dict) -> dict:
    keys = list(dict.fromkeys(body.metKeys))
    if any(k not in MET_KEYS for k in keys):
        raise HTTPException(400, "알 수 없는 시험방법입니다.")
    seq = db.save_zone(seq, body.chamberSeq, body.name.strip(), int(body.multiYn), body.linkZoneSeq, body.sortNo,
                       int(body.useYn), keys, session["id"], body.force)
    log.info("구역 저장: zone=%s use=%s met=%s by %s", seq, int(body.useYn), ",".join(keys), session["id"])
    return {"zoneSeq": seq}


@app.post("/api/admin/zone")
def admin_zone_add(body: ZoneIn, session: dict = Depends(auth.require_p01)) -> dict:
    return _save_zone(None, body, session)


@app.post("/api/admin/zone/{zone_seq}")
def admin_zone_edit(zone_seq: int, body: ZoneIn, session: dict = Depends(auth.require_p01)) -> dict:
    return _save_zone(zone_seq, body, session)


@app.get("/health")
def health() -> dict:
    err = db.ping()
    return {"ok": err is None, "service": "chamber-svc", "dbError": err}


# 화면 (index.html, history.html, login.html, app.js, app.css). API 경로 뒤에 둔다
app.mount("/", StaticFiles(directory=STATIC, html=True), name="static")
