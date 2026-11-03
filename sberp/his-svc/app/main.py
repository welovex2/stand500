import csv
import io
import logging
from datetime import date, timedelta
from pathlib import Path
from typing import Optional

from fastapi import FastAPI, HTTPException, Query
from fastapi.responses import FileResponse, StreamingResponse

from . import db, labels
from .config import settings

logging.basicConfig(level=logging.INFO)
log = logging.getLogger("his-svc")

app = FastAPI(title="sberp his-svc", version="0.1.0", docs_url=None, redoc_url=None)

STATIC = Path(__file__).parent / "static"
KINDS = ("", "period", "method", "report", "done")
MAX_DAYS = 366


def _filters(date_from: Optional[date], date_to: Optional[date], kind: str, mem: str,
             test_no: str, after_done_only: bool) -> dict:
    date_to = date_to or date.today()
    date_from = date_from or (date_to - timedelta(days=30))
    if date_from > date_to:
        raise HTTPException(400, "시작일이 종료일보다 늦습니다.")
    if (date_to - date_from).days > MAX_DAYS:
        raise HTTPException(400, "조회 기간은 최대 1년입니다.")
    if kind not in KINDS:
        raise HTTPException(400, "알 수 없는 종류입니다.")
    return dict(date_from=date_from, date_to=date_to, kind=kind, mem=mem.strip(),
                test_no=test_no.strip(), after_done_only=after_done_only)


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
        "kind": labels.kind(r["SRC"], r["TB"]),
        "item": labels.item(r["SRC"], r["TB"], r["COL"], r["ACT"]),
        "before": labels.value(r["TB"], r["COL"], r["BEFORE_VAL"]),
        "after": labels.value(r["TB"], r["COL"], r["AFTER_VAL"]),
        "afterDone": bool(r["AFTER_DONE"]),
        "doneDt": r["DONE_DT"].strftime("%Y-%m-%d %H:%M") if r["DONE_DT"] else "",
    }


@app.get("/")
def index():
    return FileResponse(STATIC / "index.html")


@app.get("/health")
def health() -> dict:
    err = db.ping()
    return {"ok": err is None, "service": "his-svc", "dbError": err}


@app.get("/api/logs")
def logs(
    date_from: Optional[date] = Query(None, alias="from"),
    date_to: Optional[date] = Query(None, alias="to"),
    kind: str = "",
    mem: str = "",
    test_no: str = Query("", alias="testNo"),
    after_done_only: bool = Query(False, alias="afterDoneOnly"),
    page: int = Query(1, ge=1),
) -> dict:
    f = _filters(date_from, date_to, kind, mem, test_no, after_done_only)
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


@app.get("/api/logs.csv")
def logs_csv(
    date_from: Optional[date] = Query(None, alias="from"),
    date_to: Optional[date] = Query(None, alias="to"),
    kind: str = "",
    mem: str = "",
    test_no: str = Query("", alias="testNo"),
    after_done_only: bool = Query(False, alias="afterDoneOnly"),
):
    f = _filters(date_from, date_to, kind, mem, test_no, after_done_only)
    rows, _ = db.fetch_logs(settings.csv_max_rows, 0, **f)

    buf = io.StringIO()
    buf.write("﻿")  # 엑셀에서 한글이 깨지지 않게 BOM
    w = csv.writer(buf)
    w.writerow(["일시", "변경자", "아이디", "시험번호", "제품명", "종류", "항목", "변경 전", "변경 후",
                "완료 후", "프로젝트완료 시각"])
    for r in map(_row, rows):
        w.writerow([r["insDt"], r["memName"], r["memId"], r["testId"], r["prdctName"], r["kind"],
                    r["item"], r["before"], r["after"], "Y" if r["afterDone"] else "", r["doneDt"]])

    name = "raw-history_%s_%s.csv" % (f["date_from"].strftime("%Y%m%d"), f["date_to"].strftime("%Y%m%d"))
    return StreamingResponse(
        iter([buf.getvalue()]),
        media_type="text/csv; charset=utf-8",
        headers={"Content-Disposition": 'attachment; filename="%s"' % name},
    )
