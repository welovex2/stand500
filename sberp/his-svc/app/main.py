import logging
from datetime import date, timedelta
from pathlib import Path
from typing import Optional

from fastapi import FastAPI, HTTPException, Query
from fastapi.responses import FileResponse

from . import db, labels
from .config import settings

logging.basicConfig(level=logging.INFO)
log = logging.getLogger("his-svc")

app = FastAPI(title="sberp his-svc", version="0.1.0", docs_url=None, redoc_url=None)

STATIC = Path(__file__).parent / "static"
TYPES = ("", "save", "change")
TIMINGS = ("", "after", "before")
TARGETS = {""} | {"save:" + k for k in labels.SAVE_TARGET} | {"change:" + k for k in labels.CHANGE_TARGET}
MAX_DAYS = 366


def _filters(date_from: Optional[date], date_to: Optional[date], row_type: str, target: str,
             timing: str, mem: str, test_no: str) -> dict:
    date_to = date_to or date.today()
    date_from = date_from or (date_to - timedelta(days=30))
    if date_from > date_to:
        raise HTTPException(400, "시작일이 종료일보다 늦습니다.")
    if (date_to - date_from).days > MAX_DAYS:
        raise HTTPException(400, "조회 기간은 최대 1년입니다.")
    if row_type not in TYPES or target not in TARGETS or timing not in TIMINGS:
        raise HTTPException(400, "알 수 없는 조건입니다.")
    return dict(date_from=date_from, date_to=date_to, row_type=row_type, target=target,
                timing=timing, mem=mem.strip(), test_no=test_no.strip())


def _row(r: dict) -> dict:
    return {
        "insDt": r["INS_DT"].strftime("%Y-%m-%d %H:%M:%S") if r["INS_DT"] else "",
        "memId": r["MEM_ID"] or "",
        "memName": r["MEM_NAME"] or "",
        "testId": r["TEST_ID"] or "",
        "testSeq": r["TEST_SEQ"],
        "rawSeq": r["RAW_SEQ"],
        "prdctName": r["PRDCT_NAME"] or "",
        "src": r["SRC"],
        "type": labels.row_type(r["SRC"]),
        "target": labels.target(r["SRC"], r["TB"], r["COL"], r["ACT"]),
        "before": labels.value(r["TB"], r["COL"], r["BEFORE_VAL"]),
        "after": labels.value(r["TB"], r["COL"], r["AFTER_VAL"]),
        "afterDone": bool(r["AFTER_DONE"]),
        "timing": labels.timing(bool(r["AFTER_DONE"])),
        "doneDt": r["DONE_DT"].strftime("%Y-%m-%d %H:%M") if r["DONE_DT"] else "",
    }


@app.get("/")
def index():
    return FileResponse(STATIC / "index.html")


@app.get("/api/options")
def options() -> dict:
    """화면 대상 선택 목록. labels 와 같은 값을 쓰도록 서버에서 내려준다."""
    return {
        "save": [{"value": "save:" + k, "label": v} for k, v in labels.SAVE_TARGET.items()],
        "change": [{"value": "change:" + k, "label": v[1]} for k, v in labels.CHANGE_TARGET.items()],
    }


@app.get("/health")
def health() -> dict:
    err = db.ping()
    return {"ok": err is None, "service": "his-svc", "dbError": err}


@app.get("/api/logs")
def logs(
    date_from: Optional[date] = Query(None, alias="from"),
    date_to: Optional[date] = Query(None, alias="to"),
    row_type: str = Query("", alias="type"),
    target: str = "",
    timing: str = "",
    mem: str = "",
    test_no: str = Query("", alias="testNo"),
    page: int = Query(1, ge=1),
) -> dict:
    f = _filters(date_from, date_to, row_type, target, timing, mem, test_no)
    size = settings.page_size
    # 한 건 더 읽어 다음 페이지가 있는지 본다
    rows, has_done = db.fetch_logs(size + 1, (page - 1) * size, **f)
    return {
        "page": page,
        "hasNext": len(rows) > size,
        "doneTableReady": has_done,
        "from": f["date_from"].isoformat(),
        "to": f["date_to"].isoformat(),
        "rows": [_row(r) for r in rows[:size]],
    }

