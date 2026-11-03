"""
트리거가 쌓는 이력 두 곳을 합쳐 읽는다. (MariaDB 10.1 — CTE 없음, 파생 테이블로 묶는다)

  RAW_HIS_TB (TB <> '')  : 시험기간·시험방법·성적서 발급일 변경 전/후  → SRC='HIS'
  RAW_DONE_EDIT_TB       : 프로젝트완료 후 로데이터 저장                → SRC='DONE'

RAW_HIS_TB 의 TB='' 행(기존 저장 시점 기록)은 ERP 이력 화면용이라 여기서는 뺀다.
"""
import time
from datetime import date, timedelta
from typing import List, Optional, Tuple

import pymysql

from .config import settings

# SB 시험번호 — rawMapper.detail 의 rawId 와 같은 식
TEST_ID_SQL = """(
    SELECT CONCAT('SB', j.SBK_YM, '-', j.SBK_TYPE, LPAD(j.SBK_SEQ, 4, 0),
                  '-', t.TEST_TYPE_CODE, LPAD(t.TEST_NO, 4, 0),
                  IF(j.SBK_REVISION > 0, CONCAT('-', j.SBK_REVISION), ''))
      FROM TEST_ITEM_TB ti
           INNER JOIN JOB_TB j ON ti.QUO_YM = j.QUO_YM AND ti.QUO_SEQ = j.QUO_SEQ
     WHERE ti.TEST_ITEM_SEQ = t.TEST_ITEM_SEQ
     LIMIT 1)"""

PRDCT_SQL = """(
    SELECT j.PRDCT_NAME
      FROM TEST_ITEM_TB ti
           INNER JOIN JOB_TB j ON ti.QUO_YM = j.QUO_YM AND ti.QUO_SEQ = j.QUO_SEQ
     WHERE ti.TEST_ITEM_SEQ = t.TEST_ITEM_SEQ
     LIMIT 1)"""

HIS_BRANCH = """
    SELECT 'HIS' AS SRC, h.INS_DT, h.MEM_ID, h.RAW_ID AS RAW_SEQ,
           CASE WHEN h.TB = 'TEST_TB' THEN CAST(SUBSTRING_INDEX(h.COL, ':', -1) AS UNSIGNED)
                ELSE r.TEST_SEQ END AS TEST_SEQ,
           h.TB, h.COL, h.`BEFORE` AS BEFORE_VAL, h.`AFTER` AS AFTER_VAL, NULL AS ACT
      FROM RAW_HIS_TB h
           LEFT JOIN RAW_TB r ON r.RAW_SEQ = h.RAW_ID
     WHERE h.TB <> ''
       AND h.INS_DT >= %s AND h.INS_DT < %s
       {tb_cond}"""

DONE_BRANCH = """
    SELECT 'DONE' AS SRC, d.INS_DT, d.MEM_ID, d.RAW_SEQ, d.TEST_SEQ,
           d.TB, NULL AS COL, NULL AS BEFORE_VAL, NULL AS AFTER_VAL, d.ACT
      FROM RAW_DONE_EDIT_TB d
     WHERE d.INS_DT >= %s AND d.INS_DT < %s"""

EMPTY_BRANCH = """
    SELECT 'DONE' AS SRC, NULL AS INS_DT, NULL AS MEM_ID, NULL AS RAW_SEQ, NULL AS TEST_SEQ,
           NULL AS TB, NULL AS COL, NULL AS BEFORE_VAL, NULL AS AFTER_VAL, NULL AS ACT
      FROM DUAL WHERE 1 = 0"""

# 화면 종류 필터 → RAW_HIS_TB.TB
KIND_TB = {"period": "RAW_TB", "method": "RAW_MET_TB", "report": "TEST_TB"}

_done_table_cache: Tuple[float, bool] = (0.0, False)


def connect():
    return pymysql.connect(
        host=settings.db_host,
        port=settings.db_port,
        user=settings.db_user,
        password=settings.db_password,
        database=settings.db_name,
        charset="utf8mb4",
        cursorclass=pymysql.cursors.DictCursor,
        read_timeout=30,
    )


def done_table_exists(cur) -> bool:
    """RAW_DONE_EDIT_TB 는 트리거 반영 후에 생긴다. 없으면 DONE 쪽을 빼고 조회한다 (1분 캐시)"""
    global _done_table_cache
    checked_at, exists = _done_table_cache
    if time.time() - checked_at < 60:
        return exists
    cur.execute(
        "SELECT COUNT(*) AS n FROM information_schema.TABLES"
        " WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'RAW_DONE_EDIT_TB'"
    )
    exists = cur.fetchone()["n"] > 0
    _done_table_cache = (time.time(), exists)
    return exists


def build_query(
    cur,
    date_from: date,
    date_to: date,
    kind: str = "",
    mem: str = "",
    test_no: str = "",
    after_done_only: bool = False,
) -> Tuple[str, list, bool]:
    """조회 SQL(정렬 포함, LIMIT 제외)과 파라미터. 세 번째 값은 완료 후 수정 기록 테이블 존재 여부."""
    dt_from = date_from.strftime("%Y-%m-%d")
    dt_to = (date_to + timedelta(days=1)).strftime("%Y-%m-%d")
    has_done = done_table_exists(cur)

    branches: List[str] = []
    params: list = []

    if kind in ("", "period", "method", "report"):
        tb_cond = "AND h.TB = %s" if kind else ""
        branches.append(HIS_BRANCH.format(tb_cond=tb_cond))
        params += [dt_from, dt_to] + ([KIND_TB[kind]] if kind else [])
    if kind in ("", "done") and has_done:
        branches.append(DONE_BRANCH)
        params += [dt_from, dt_to]

    if not branches:
        # 종류=완료 후 저장인데 테이블이 아직 없음 → 빈 결과
        branches.append(EMPTY_BRANCH)

    where: List[str] = []
    if mem:
        where.append("(u.MEM_ID = %s OR m.MEM_NAME LIKE %s)")
        params += [mem, "%" + mem + "%"]
    if test_no:
        where.append(TEST_ID_SQL + " LIKE %s")
        params.append("%" + test_no + "%")
    if after_done_only:
        where.append("(u.SRC = 'DONE' OR (t.TEST_STATE_CODE = '18' AND u.INS_DT >= t.TEST_STATE_DT))")

    sql = f"""
SELECT u.SRC, u.INS_DT, u.MEM_ID, m.MEM_NAME, u.RAW_SEQ, u.TEST_SEQ, u.TB, u.COL,
       u.BEFORE_VAL, u.AFTER_VAL, u.ACT,
       {TEST_ID_SQL} AS TEST_ID,
       {PRDCT_SQL} AS PRDCT_NAME,
       t.TEST_STATE_DT AS DONE_DT,
       (u.SRC = 'DONE' OR (t.TEST_STATE_CODE = '18' AND u.INS_DT >= t.TEST_STATE_DT)) AS AFTER_DONE
  FROM ({" UNION ALL ".join(branches)}
       ) u
       LEFT JOIN MEMBER_TB m ON m.ID = u.MEM_ID
       LEFT JOIN TEST_TB t ON t.TEST_SEQ = u.TEST_SEQ
 {"WHERE " + " AND ".join(where) if where else ""}
 ORDER BY u.INS_DT DESC, u.SRC ASC, u.TB ASC, u.COL ASC"""
    return sql, params, has_done


def fetch_logs(limit: int, offset: int = 0, **filters) -> Tuple[List[dict], bool]:
    conn = connect()
    try:
        with conn.cursor() as cur:
            sql, params, has_done = build_query(cur, **filters)
            cur.execute(sql + "\n LIMIT %s OFFSET %s", params + [limit, offset])
            return list(cur.fetchall()), has_done
    finally:
        conn.close()


def ping() -> Optional[str]:
    """DB 연결 확인. 문제 없으면 None, 있으면 오류 메시지."""
    try:
        conn = connect()
        try:
            with conn.cursor() as cur:
                cur.execute("SELECT 1")
        finally:
            conn.close()
        return None
    except Exception as e:  # noqa: BLE001 — 상태 확인용
        return str(e)
