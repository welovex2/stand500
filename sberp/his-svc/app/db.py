"""
트리거가 쌓는 이력 두 곳을 합쳐 읽는다. (MariaDB 10.1 — CTE 없음, 파생 테이블로 묶는다)

  RAW_HIS_TB (TB = RAW_TB / RAW_MET_TB) : 시험기간·시험방법 변경 전/후  → SRC='HIS'
  RAW_DONE_EDIT_TB                      : 프로젝트완료 후 로데이터 저장  → SRC='DONE'

RAW_HIS_TB 의 TB='' 행(기존 저장 시점 기록)은 ERP 이력 화면용이라 뺀다.
TB='TEST_TB'(성적서 발급일)는 원래 완료 후에 정하는 값이라 뺀다. 트리거는 계속 남긴다.
변경 전 값이 없는 행(최초 입력)도 뺀다. 트리거도 지금은 남기지 않지만, 그 전에 쌓인 행이 있을 수 있다.
"""
import time
from datetime import date, timedelta
from typing import List, Optional, Tuple

import pymysql

from . import labels
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
    SELECT 'HIS' AS SRC, h.INS_DT, h.MEM_ID, h.RAW_ID AS RAW_SEQ, r.TEST_SEQ,
           h.TB, h.COL, h.`BEFORE` AS BEFORE_VAL, h.`AFTER` AS AFTER_VAL, NULL AS ACT
      FROM RAW_HIS_TB h
           LEFT JOIN RAW_TB r ON r.RAW_SEQ = h.RAW_ID
     WHERE h.TB IN ('RAW_TB', 'RAW_MET_TB')
       AND IFNULL(h.`BEFORE`, '') <> ''
       AND h.INS_DT >= %s AND h.INS_DT < %s
       {tb_cond}"""

DONE_BRANCH = """
    SELECT 'DONE' AS SRC, d.INS_DT, d.MEM_ID, d.RAW_SEQ, d.TEST_SEQ,
           d.TB, NULL AS COL, NULL AS BEFORE_VAL, NULL AS AFTER_VAL, d.ACT
      FROM RAW_DONE_EDIT_TB d
     WHERE d.INS_DT >= %s AND d.INS_DT < %s
       {tb_cond}"""

EMPTY_BRANCH = """
    SELECT 'DONE' AS SRC, NULL AS INS_DT, NULL AS MEM_ID, NULL AS RAW_SEQ, NULL AS TEST_SEQ,
           NULL AS TB, NULL AS COL, NULL AS BEFORE_VAL, NULL AS AFTER_VAL, NULL AS ACT
      FROM DUAL WHERE 1 = 0"""

# 프로젝트완료 후인가: 저장 기록은 트리거가 완료(18) 시험만 남기므로 항상 완료 후,
# 값 변경은 시험이 완료 상태이고 완료 시각(TEST_STATE_DT) 이후에 바뀐 것
AFTER_DONE_SQL = "(u.SRC = 'DONE' OR (t.TEST_STATE_CODE = '18' AND u.INS_DT >= t.TEST_STATE_DT))"

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
    row_type: str = "",
    target: str = "",
    timing: str = "",
    mem: str = "",
    test_no: str = "",
) -> Tuple[str, list, bool]:
    """조회 SQL(정렬 포함, LIMIT 제외)과 파라미터. 세 번째 값은 저장 기록 테이블 존재 여부.

    row_type : '' / 'save'(저장) / 'change'(값 변경)
    target   : '' / 'save:{RAW_DONE_EDIT_TB.TB}' / 'change:{period|method}'
    timing   : '' / 'after'(완료 후) / 'before'(완료 전)
    """
    dt_from = date_from.strftime("%Y-%m-%d")
    dt_to = (date_to + timedelta(days=1)).strftime("%Y-%m-%d")
    has_done = done_table_exists(cur)

    target_kind, _, target_key = target.partition(":")
    want_change = row_type in ("", "change") and target_kind in ("", "change")
    want_save = row_type in ("", "save") and target_kind in ("", "save") and has_done

    branches: List[str] = []
    params: list = []

    if want_change:
        his_tb = labels.CHANGE_TARGET[target_key][0] if target_kind == "change" else None
        branches.append(HIS_BRANCH.format(tb_cond="AND h.TB = %s" if his_tb else ""))
        params += [dt_from, dt_to] + ([his_tb] if his_tb else [])
    if want_save:
        save_tb = target_key if target_kind == "save" else None
        branches.append(DONE_BRANCH.format(tb_cond="AND d.TB = %s" if save_tb else ""))
        params += [dt_from, dt_to] + ([save_tb] if save_tb else [])

    if not branches:
        # 저장만 골랐는데 기록 테이블이 아직 없는 경우 등 → 빈 결과
        branches.append(EMPTY_BRANCH)

    where: List[str] = []
    if mem:
        where.append("(u.MEM_ID = %s OR m.MEM_NAME LIKE %s)")
        params += [mem, "%" + mem + "%"]
    if test_no:
        where.append(TEST_ID_SQL + " LIKE %s")
        params.append("%" + test_no + "%")
    if timing == "after":
        where.append(AFTER_DONE_SQL)
    elif timing == "before":
        where.append("NOT " + AFTER_DONE_SQL)

    sql = f"""
SELECT u.SRC, u.INS_DT, u.MEM_ID, m.MEM_NAME, u.RAW_SEQ, u.TEST_SEQ, u.TB, u.COL,
       u.BEFORE_VAL, u.AFTER_VAL, u.ACT,
       {TEST_ID_SQL} AS TEST_ID,
       {PRDCT_SQL} AS PRDCT_NAME,
       t.TEST_STATE_DT AS DONE_DT,
       {AFTER_DONE_SQL} AS AFTER_DONE
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
