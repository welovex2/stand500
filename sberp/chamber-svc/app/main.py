import logging
import mimetypes
from datetime import date, datetime, timedelta
from pathlib import Path
from typing import List, Optional
from urllib.parse import quote

import pymysql
from fastapi import Depends, FastAPI, HTTPException, Query, Request, Response
from fastapi.responses import JSONResponse, RedirectResponse
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel, Field

from . import auth, db, xlsx
from .config import (erp_raw_page,MET_KEYS, PURPOSE_NO_RD, PURPOSE_TEST, PURPOSES, PURPOSES_RETIRED, TEST_STATE_NAMES,
                     is_no_rd_zone, settings)

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
        "wholeYn": bool(r.get("WHOLE_YN")),      # 시험장 전체 점유 (시험번호 없이 · 항목 선택 안 함)
    }
    if r.get("WITNESS_NAME"):
        # 고객 입회 시험: 시험원 미지정, 입회자 표시
        out.update(witnessYn=True, witnessName=r["WITNESS_NAME"], memName="입회 · " + r["WITNESS_NAME"])
    if r.get("CHAMBER_NAME") and session is None:
        out["chamberName"] = r["CHAMBER_NAME"]   # 사용 중 거부 응답: 어느 시험장인지
    if elapsed is not None:
        out["elapsedMin"] = int(elapsed)
        out["stale"] = int(elapsed) >= settings.stale_hours * 60
    if session is not None:
        out["canEnd"] = is_etc and not session.get("mon") and (
            session.get("rev") or r.get("MEM_ID") == session["id"]
            or (not r.get("MEM_ID") and r.get("REG_MEM_ID") == session["id"]))   # 입회 시험은 등록자
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
                          "noRd": is_no_rd_zone(r["ZONE_NAME"]),   # CTI · 4-39: 시험번호 없이 시작
                          "uses": [], "linked": [], "held": False})
            by_zone[r["ZONE_SEQ"]] = zones[-1]
        if r["SRC"]:
            zones[-1]["uses"].append(_use(r, session))
    for c in chambers:
        for z in c["zones"]:
            if any(u["wholeYn"] for u in z["uses"]):
                # 시험장 전체 점유: 다른 항목도 모두 사용 중 (held). 사용 건 자체는 기록된 구역에만 둔다
                for other in c["zones"]:
                    other["held"] = True
            target = by_zone.get(z["linkZoneSeq"])
            if not target:
                continue
            for u in z["uses"]:
                # 장비 이관은 시험번호가 있는 시험만 (측정 행, 현황판 시험). 시험번호 없이 사용은 이관하지 않는다 (X-5)
                if u["src"] != "RAW" and u["purpose"] != PURPOSE_TEST:
                    continue
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
        "timelineMenu": bool(session.get("rev")) and not session.get("mon"),   # 시험원 현황 (기술책임자·관리자)
        "adminMenu": bool(session.get("p01")) and not session.get("mon"),   # 관리 탭 · 기록 수정 (P01·P02, 모니터 화면 제외)
        "staleHours": settings.stale_hours,
        # "시험번호 없이 사용" 사유 (TEST 제외)
        "purposes": [{"value": k, "label": v} for k, v in PURPOSES.items()
                     if k not in (PURPOSE_TEST, PURPOSE_NO_RD) and k not in PURPOSES_RETIRED],
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
    chamber_seqs: str = Query("", alias="chamberSeqs"),
    floor: Optional[int] = None,
    deleted: bool = False,
    _: dict = Depends(auth.require_login),
    overlap: bool = False,
) -> dict:
    """사용 이력 (기획 v7_1 SC-15). 기간은 시작 일시 기준이고 진행 중인 건도 나온다.
    overlap=true 면 기간과 시간이 겹치는 기록 (시험원 현황 타임라인)
    chamberSeqs = 시험장 여러 개 (쉼표). deleted=true 이면 현황판에서 삭제한 건도 함께 (합계에서는 뺀다)"""
    date_to = date_to or date.today()
    date_from = date_from or (date_to - timedelta(days=7))
    if date_from > date_to:
        raise HTTPException(400, "시작일이 종료일보다 늦습니다.")
    if (date_to - date_from).days > MAX_DAYS:
        raise HTTPException(400, "조회 기간은 최대 1년입니다.")
    try:
        seqs = [int(x) for x in chamber_seqs.split(",") if x.strip()]
    except ValueError:
        raise HTTPException(400, "알 수 없는 시험장입니다.")

    rows = db.fetch_history(date_from, date_to, seqs, deleted, floor, overlap)
    limit = settings.history_limit
    out = []
    for r in rows[:limit]:
        row = _use(r)
        page = erp_raw_page(r.get("STNDR_SEQ"), r.get("MET_KEY")) if r["SRC"] == "RAW" and r.get("RAW_SEQ") else None
        row.update(
            chamberSeq=r["CHAMBER_SEQ"], chamberName=r["CHAMBER_NAME"], floor=int(r["FLOOR"]),
            zoneName=r["ZONE_NAME"] or "", memId=r.get("MEM_ID") or "",
            endDt=_dt(r["E_DT"]), useMin=int(r["USE_MIN"] or 0), running=r["E_DT"] is None,
            stale=int(r["USE_MIN"] or 0) >= settings.stale_hours * 60,
            testSeq=r.get("TEST_SEQ"),
            # 장비 이관받은 시험: "⇄ RE (Above) 장비 이관" (기획 v8: 장비를 보낸 시험장 이름만)
            linkName=r.get("LINK_CHAMBER_NAME") or "",
            # 측정 행: 시험번호를 누르면 ERP 로데이터의 그 시험 항목 화면 (자동화 시험규격만 화면이 있다).
            # ERP 시험리스트의 RD 링크와 같은 형식: ?test=&&raw=&&type=RS&&rdEditYn=0&&sbkInsDt=26-10-16&dt=-
            # rdEditYn = ERP 와 같은 TEST_TB.RAW_EDIT_YN (없으면 1, 프로젝트완료면 0)
            erpUrl=(f"{settings.erp_web_base.rstrip('/')}/{page[0]}?test={r['TEST_SEQ']}&&raw={r['RAW_SEQ']}"
                    f"&&type={page[1]}&&rdEditYn={1 if r.get('RAW_EDIT_YN') is None else int(r['RAW_EDIT_YN'])}"
                    f"&&sbkInsDt={r.get('SBK_INS_DT') or ''}"
                    f"&dt={r.get('REPORT_DT') or '-'}") if page and r.get("TEST_SEQ") else "",
            delYn=bool(r["DEL_YN"]),
            udtDt=_dt(r.get("UDT_DT")), udtMemName=r.get("UDT_MEM_NAME") or "",
        )
        if row["delYn"]:
            row.update(delReason=r["DEL_REASON"], delMemName=r["DEL_MEM_NAME"], delDt=_dt(r["DEL_DT"]))
        out.append(row)
    return {
        "from": date_from.isoformat(),
        "to": date_to.isoformat(),
        "rows": out,
        "truncated": len(rows) > limit,   # 한도를 넘어 최근 것만 보여 줌
        "staleHours": settings.stale_hours,
    }


@app.get("/api/timeline")
def timeline(
    date_from: Optional[date] = Query(None, alias="from"),
    date_to: Optional[date] = Query(None, alias="to"),
    session: dict = Depends(auth.require_rev_view),
) -> dict:
    """시험원 현황 (시험원별 타임라인): 기간과 겹치는 사용 기록 + EMC 시험원 목록 (기록이 없는 사람도 빈 줄로).
    기술책임자·관리자만"""
    date_to = date_to or date.today()
    date_from = date_from or date_to
    if (date_to - date_from).days > 31:
        raise HTTPException(400, "시험원 현황은 최대 31일까지 볼 수 있습니다.")
    out = history(date_from=date_from, date_to=date_to, chamber_seqs="", floor=None, deleted=False,
                  _=session, overlap=True)
    out["members"] = [{"memId": m["ID"], "memName": m["MEM_NAME"] or m["ID"]} for m in db.fetch_members()]
    out["now"] = _dt(datetime.now())
    return out


class XlsxIn(BaseModel):
    fileName: str = Field("시험장_사용이력", max_length=100)
    head: List[str] = Field(max_length=40)
    rows: List[List[Optional[object]]] = Field(max_length=20000)


@app.post("/api/history/xlsx")
def history_xlsx(body: XlsxIn, _: dict = Depends(auth.require_login)) -> Response:
    """사용 이력 [엑셀 다운로드] (기획 SC-15 ⑥): 화면에서 지금 조건으로 거른 행을 받아 .xlsx 로 돌려준다"""
    cells = [[v if isinstance(v, (int, float)) and not isinstance(v, bool) else ("" if v is None else str(v)[:1000])
              for v in r[:len(body.head)]] for r in body.rows]
    data = xlsx.build([h[:100] for h in body.head], cells, "사용 이력")
    name = "".join(ch for ch in body.fileName if ch not in '\\/:*?"<>|' and ord(ch) >= 0x20) or "시험장_사용이력"
    return Response(data, media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    headers={"Content-Disposition": f"attachment; filename=\"history.xlsx\"; filename*=UTF-8''{quote(name)}.xlsx",
                             "Cache-Control": "no-store"})


# ---------------------------------------------------------------- 기록 수정 (기획 SC-16, P01·P02)

class EtcEditIn(BaseModel):
    testSeq: Optional[int] = None      # 없으면 시험번호 없음
    memId: Optional[str] = None        # 시험원. 입회 시험이면 비운다
    witnessName: str = ""              # 입회 시험의 입회자
    startDt: str                       # 'YYYY-MM-DDTHH:MM'
    endDt: str = ""                    # 진행 중이면 비운다
    reason: str = ""


def _parse_dt(v: str, label: str) -> Optional[datetime]:
    if not v:
        return None
    try:
        return datetime.strptime(v.replace("T", " ")[:16], "%Y-%m-%d %H:%M")
    except ValueError:
        raise HTTPException(400, f"{label} 시각 형식이 올바르지 않습니다.")


def _edit_text(test_id, mem, start, end) -> str:
    return f"시험번호 {test_id or '없음'} · 시험원 {mem or '-'} · {_dt(start)} ~ {_dt(end) or '진행 중'}"


@app.get("/api/etc-use/{use_seq}/his")
def etc_his(use_seq: int, _: dict = Depends(auth.require_p01)) -> dict:
    return {"his": [{"before": h["BEFORE_TXT"], "after": h["AFTER_TXT"], "reason": h["REASON"],
                     "memName": h["MEM_NAME"], "insDt": _dt(h["INS_DT"])} for h in db.fetch_use_his(use_seq)]}


@app.get("/api/tests/search")
def tests_search(q: str = "", _: dict = Depends(auth.require_p01)) -> dict:
    """기록 수정에서 시험번호 연결: 상태와 관계없이 EMC 시험 (2글자 이상)"""
    q = q.strip()
    if len(q) < 2:
        return {"tests": []}
    return {"tests": [{"testSeq": r["TEST_SEQ"], "testId": r["TEST_ID"] or "", "prdctName": r["PRDCT_NAME"] or "",
                       "cmpyName": r["CMPY_NAME"] or "",
                       "stateName": TEST_STATE_NAMES.get(int(r["TEST_STATE_CODE"] or 0), "")}
                      for r in db.search_tests(q)]}


@app.post("/api/etc-use/{use_seq}/edit")
def etc_edit(use_seq: int, body: EtcEditIn, session: dict = Depends(auth.require_p01)) -> dict:
    reason = body.reason.strip()
    if not reason:
        raise HTTPException(400, "수정 사유를 입력하세요.")
    if len(reason) > 200:
        raise HTTPException(400, "수정 사유는 200자까지 입력할 수 있습니다.")
    cur = db.fetch_etc_use(use_seq)
    if not cur or cur["DEL_YN"]:
        raise HTTPException(404, "없는 기록입니다.")
    start = _parse_dt(body.startDt, "시작")
    if start is None:
        raise HTTPException(400, "시작 시각을 입력하세요.")
    end = _parse_dt(body.endDt, "종료") if cur["END_DT"] is not None else None
    if cur["END_DT"] is not None and end is None:
        raise HTTPException(400, "종료 시각을 입력하세요.")
    if end is not None and end <= start:
        raise HTTPException(400, "종료 시각이 시작 시각보다 빠릅니다.")
    if start > datetime.now() or (end and end > datetime.now()):
        raise HTTPException(400, "앞으로의 시각은 입력할 수 없습니다.")

    witness = cur.get("WITNESS_NAME")
    if witness:                       # 입회 시험: 시험원 대신 입회자
        witness = body.witnessName.strip()
        if not witness:
            raise HTTPException(400, "입회자를 입력하세요.")
        mem_id = mem_name = None
    else:
        if not body.memId:
            raise HTTPException(400, "시험원을 선택하세요.")
        if body.memId == cur.get("MEM_ID"):
            mem_id, mem_name = cur["MEM_ID"], cur["MEM_NAME"]
        else:
            pick = db.fetch_emc_member(body.memId)
            if not pick:
                raise HTTPException(400, "EMC 부서 시험원이 아닙니다.")
            mem_id, mem_name = pick["ID"], pick["MEM_NAME"] or pick["ID"]
    test_id = ""
    test_seq = None if cur["PURPOSE"] == PURPOSE_NO_RD else body.testSeq
    if cur.get("WHOLE_YN") and test_seq is not None:
        # 시작과 같은 규칙: 시험장 전체 점유는 시험번호 없이 사용할 때만 (연결했다 빼면 사유가 "기타"로 바뀌는 것도 막는다)
        raise HTTPException(400, "시험장 전체 점유 기록에는 시험번호를 연결할 수 없습니다.")
    if test_seq is not None and test_seq != cur.get("TEST_SEQ"):
        hit = db.search_test_by_seq(test_seq)
        if not hit:
            raise HTTPException(400, "EMC 시험이 아닙니다.")
        test_id = hit["TEST_ID"]
    elif test_seq is not None:
        test_id = cur.get("TEST_ID") or ""
    before = _edit_text(cur.get("TEST_ID"), cur.get("MEM_NAME") or (cur.get("WITNESS_NAME") and "입회 " + cur["WITNESS_NAME"]),
                        cur["START_DT"], cur["END_DT"])
    after = _edit_text(test_id, mem_name or (witness and "입회 " + witness), start, end or cur["END_DT"])
    db.edit_etc_use(use_seq, test_seq, mem_id, mem_name, witness or None, start, end, reason, session["id"],
                    before, after)
    log.info("기록 수정: use=%s by %s · %s → %s · %s", use_seq, session["id"], before, after, reason)
    return {"ok": True}


# ---------------------------------------------------------------- 로데이터 외 사용

class EtcStartIn(BaseModel):
    zoneSeq: int
    purpose: str = ""                  # 시험번호 없이 사용할 때 사유. 시험번호가 있으면 TEST
    testSeq: Optional[int] = None
    memId: Optional[str] = None        # 시험원. 기술책임자·관리자만 고를 수 있다 (EMC 부서 재직자 중에서)
    witness: bool = False              # 고객 입회 시험: 시험원 미지정
    witnessName: str = ""              # 입회자 이름 (소속). 입회면 필수
    whole: bool = False                # 시험장 전체 점유: 시험번호 없이 사용 + 항목 선택 안 함 (zoneSeq 는 그 챔버의 아무 구역)
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


@app.get("/api/members")
def members(_: dict = Depends(auth.require_rev)) -> dict:
    """기술책임자·관리자가 시험원을 고를 목록: EMC 부서 재직자. busy = 진행 중인 시험장 (고를 수 없다)"""
    return {"members": [{"memId": m["ID"], "memName": m["MEM_NAME"] or m["ID"], "busy": m["BUSY"] or ""}
                        for m in db.fetch_members()]}


@app.post("/api/etc-use/start")
def etc_start(body: EtcStartIn, session: dict = Depends(auth.require_writer)) -> dict:
    memo = body.memo.strip()
    if len(memo) > 200:
        raise HTTPException(400, "메모는 200자까지 입력할 수 있습니다.")
    mem_id, mem_name, witness_name = session["id"], session["name"], None
    if body.witness:
        # 고객 입회: 시험원을 지정하지 않는다 (1인 1시험 제한 없음). 누가 시작했는지는 REG_MEM_ID 에 남는다
        witness_name = body.witnessName.strip()
        if not witness_name:
            raise HTTPException(400, "입회자를 입력하세요.")
        if len(witness_name) > 100:
            raise HTTPException(400, "입회자는 100자까지 입력할 수 있습니다.")
        mem_id, mem_name = None, None
    elif body.memId and body.memId != session["id"]:
        if not session.get("rev"):
            raise HTTPException(403, "시험원은 본인으로만 시작할 수 있습니다.")
        pick = db.fetch_emc_member(body.memId)
        if not pick:
            raise HTTPException(400, "EMC 부서 시험원이 아닙니다.")
        mem_id, mem_name = pick["ID"], pick["MEM_NAME"] or pick["ID"]
    zone_name = db.fetch_zone_name(body.zoneSeq)
    if zone_name is None:
        raise HTTPException(400, "없는 시험 항목입니다.")
    if is_no_rd_zone(zone_name):
        # 로데이터 연동 없는 시험 (기획 SC-08): 시험번호 없이 시작·종료 시간만 남긴다
        if body.testSeq is not None:
            raise HTTPException(400, f"{zone_name} 시험은 시험번호 없이 시작합니다.")
        purpose = PURPOSE_NO_RD
    elif body.testSeq is not None:
        purpose = PURPOSE_TEST
        if not db.fetch_test(body.testSeq, None if session.get("rev") else session["id"]):
            raise HTTPException(400, "고를 수 없는 시험번호입니다. (접수·시험중·디버깅인 EMC 시험, 일반 사용자는 본인 담당 시험만)")
    else:
        purpose = body.purpose
        if purpose not in PURPOSES or purpose in (PURPOSE_TEST, PURPOSE_NO_RD) or purpose in PURPOSES_RETIRED:
            raise HTTPException(400, "시험번호를 고르거나, 시험번호 없이 사용할 사유를 선택하세요.")
    if body.whole and purpose in (PURPOSE_TEST, PURPOSE_NO_RD):
        raise HTTPException(400, "시험장 전체 점유는 시험번호 없이 사용할 때만 고를 수 있습니다.")
    use_seq = db.start_etc_use(body.zoneSeq, purpose, body.testSeq, memo, mem_id, mem_name, session["id"],
                               witness_name, whole=body.whole)
    log.info("사용 시작: use=%s zone=%s %s test=%s mem=%s by %s",
             use_seq, body.zoneSeq, purpose, body.testSeq, mem_id, session["id"])
    return {"useSeq": use_seq}


@app.post("/api/etc-use/{use_seq}/end")
def etc_end(use_seq: int, session: dict = Depends(auth.require_writer)) -> dict:
    db.end_etc_use(use_seq, session["id"], bool(session.get("rev")))
    log.info("사용 종료: use=%s by %s", use_seq, session["id"])
    return {"ok": True}


@app.post("/api/etc-use/{use_seq}/delete")
def etc_delete(use_seq: int, body: EtcDeleteIn, session: dict = Depends(auth.require_p01)) -> dict:
    """기록 삭제: 일단 관리자(P01·P02)만 (2026-11-25. 기획 v8 에는 없는 기능. 기록한 사람 · 기술책임자는 못 한다)"""
    reason = body.reason.strip()
    if not reason:
        raise HTTPException(400, "삭제 사유를 입력하세요.")
    if len(reason) > 200:
        raise HTTPException(400, "삭제 사유는 200자까지 입력할 수 있습니다.")
    db.delete_etc_use(use_seq, reason, session["id"], True)
    log.info("사용 삭제: use=%s by %s", use_seq, session["id"])
    return {"ok": True}


# ---------------------------------------------------------------- 챔버 관리 (AUTH_CODE P01·P02)

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
            # 다른 구역의 사용 때문에 점유 중 (시험장 전체 점유 · 단독 사용 · 장비 이관). 사유 목록
            "heldBy": t.get("held", {}).get(z["ZONE_SEQ"], []),
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


@app.get("/api/kiosk")
def kiosk(request: Request) -> dict:
    """로그인 화면의 [모니터링 화면으로 보기] 버튼: 모니터로 등록된 PC(모니터 쿠키가 있는 브라우저)에만 보인다 (2026-11-25)"""
    return {"kiosk": auth.kiosk_ok(request)}


# 화면 (index.html, history.html, login.html, app.js, app.css). API 경로 뒤에 둔다
@app.get("/monitor")
def monitor(key: str = "") -> Response:
    """모니터 전용 주소: …/monitor?key=MONITOR_KEY → 현황판 모니터 화면.
    키가 맞으면 모니터 쿠키를 심고 화면으로 보낸다. 주소창에 키가 남지 않게 다시 보낸다.
    (시험원 현황은 기술책임자·관리자만 볼 수 있어 모니터 화면에서는 열지 않는다)"""
    resp = RedirectResponse("./?kiosk=1", status_code=303)
    auth.open_kiosk(key, resp)
    log.info("모니터 주소로 열림")
    return resp


app.mount("/", StaticFiles(directory=STATIC, html=True), name="static")
