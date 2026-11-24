"""
erp 스키마를 읽고, CHAMBER_ETC_USE_TB(현황판 사용)·챔버 관리 테이블에만 쓴다. (MariaDB 10.1 — CTE 없음)
다른 테이블은 계정 권한으로도 쓸 수 없다 (sql/chamber_svc_user.sql).

"진행 중"의 정의는 ERP 와 같아야 한다 (docs/design/chamber-svc.md §2.2):
  측정 행          : RAW_ENV_TB          DEL_YN=0 AND MIG_YN=0 AND TEST_S_DT IS NOT NULL AND TEST_E_DT IS NULL
  로데이터 외 사용 : CHAMBER_ETC_USE_TB  DEL_YN=0 AND END_DT IS NULL
"""
import threading
from contextlib import contextmanager
from datetime import date, timedelta, datetime
from typing import List, Optional

import pymysql

from .config import settings

# 동시 접속 수 제한 — ERP 가 쓸 DB 접속을 차지하지 않게 한다
_slots = threading.BoundedSemaphore(settings.db_max_conn)


class DbBusy(Exception):
    """접속 자리가 5초 안에 나지 않음"""


class Reject(Exception):
    """업무 규칙 위반. status 는 HTTP 상태 코드"""

    def __init__(self, status: int, message: str, detail: Optional[dict] = None):
        super().__init__(message)
        self.status = status
        self.message = message
        self.detail = detail


@contextmanager
def connect():
    if not _slots.acquire(timeout=5):
        raise DbBusy()
    try:
        conn = pymysql.connect(
            host=settings.db_host,
            port=settings.db_port,
            user=settings.db_user,
            password=settings.db_password,
            database=settings.db_name,
            charset="utf8mb4",
            cursorclass=pymysql.cursors.DictCursor,
            autocommit=False,
            connect_timeout=5,
            read_timeout=10,
            write_timeout=10,
        )
        try:
            # 구역 행을 잠근 뒤의 사용 중 확인이 잠금 이후에 커밋된 행까지 보도록 한다
            with conn.cursor() as cur:
                cur.execute("SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED")
            yield conn
        finally:
            conn.close()
    finally:
        _slots.release()


# SB 시험번호 — his-svc TEST_ID_SQL 과 같은 식 (스키마 이름만 붙임)
TEST_ID_SQL = f"""(
    SELECT CONCAT('SB', j.SBK_YM, '-', j.SBK_TYPE, LPAD(j.SBK_SEQ, 4, 0),
                  '-', IF(t.TEST_TYPE_CODE = 'NS', 'SF', t.TEST_TYPE_CODE), LPAD(t.TEST_NO, 4, 0),
                  IF(j.SBK_REVISION > 0, CONCAT('-', j.SBK_REVISION), ''))
      FROM TEST_ITEM_TB ti
           INNER JOIN JOB_TB j ON ti.QUO_YM = j.QUO_YM AND ti.QUO_SEQ = j.QUO_SEQ
     WHERE ti.TEST_ITEM_SEQ = t.TEST_ITEM_SEQ
     LIMIT 1)"""

PRDCT_SQL = f"""(
    SELECT j.PRDCT_NAME
      FROM TEST_ITEM_TB ti
           INNER JOIN JOB_TB j ON ti.QUO_YM = j.QUO_YM AND ti.QUO_SEQ = j.QUO_SEQ
     WHERE ti.TEST_ITEM_SEQ = t.TEST_ITEM_SEQ
     LIMIT 1)"""

# 업체 (신청 업체)
CMPY_SQL = f"""(
    SELECT j.CMPY_NAME
      FROM TEST_ITEM_TB ti
           INNER JOIN JOB_TB j ON ti.QUO_YM = j.QUO_YM AND ti.QUO_SEQ = j.QUO_SEQ
     WHERE ti.TEST_ITEM_SEQ = t.TEST_ITEM_SEQ
     LIMIT 1)"""

# 측정 행의 시험원 이름: 시험자명(MEM_NAME)은 [종료] 때 들어가므로, 진행 중이면 [시작]을 누른 사람(MEM_ID)의 이름
RAW_MEM_NAME = ("COALESCE(NULLIF(e.MEM_NAME, ''),"
                " (SELECT mb.MEM_NAME FROM MEMBER_TB mb WHERE mb.ID = e.MEM_ID), e.MEM_ID)")

RAW_ACTIVE = "e.DEL_YN = 0 AND e.MIG_YN = 0 AND e.TEST_S_DT IS NOT NULL AND e.TEST_E_DT IS NULL"
ETC_ACTIVE = "x.DEL_YN = 0 AND x.END_DT IS NULL"

# 진행 중인 사용 건 (측정 행 + 현황판 사용). MEM_ID = 시험원 (측정 행은 [시작]을 누른 사람)
# TEST_SEQ 는 현황판에서 시험번호를 골라 시작한 건만. 측정 행은 RAW_TB 를 거쳐 찾는다
ACTIVE_UNION = f"""
    SELECT 'RAW' AS SRC, e.ENV_SEQ AS SEQ, e.ZONE_SEQ, e.RAW_SEQ, NULL AS TEST_SEQ, e.MET_KEY,
           NULL AS PURPOSE, NULL AS MEMO, e.MEM_ID, {RAW_MEM_NAME} AS MEM_NAME, e.TEST_S_DT AS S_DT,
           NULL AS WITNESS_NAME, NULL AS REG_MEM_ID, 0 AS WHOLE_YN
      FROM RAW_ENV_TB e
     WHERE {RAW_ACTIVE} AND e.ZONE_SEQ IS NOT NULL
    UNION ALL
    SELECT 'ETC', x.USE_SEQ, x.ZONE_SEQ, NULL, x.TEST_SEQ, NULL,
           x.PURPOSE, x.MEMO, x.MEM_ID, x.MEM_NAME, x.START_DT,
           x.WITNESS_NAME, x.REG_MEM_ID, x.WHOLE_YN
      FROM CHAMBER_ETC_USE_TB x
     WHERE {ETC_ACTIVE}"""

# 사용 건 하나가 점유하는 구역 (ERP rawEnvMapper 와 같은 규칙, 기획 v7_1 SC-09 · X-2 · X-5).
#   1) 자기 구역
#   2) 연계 구역 (CHAMBER_ZONE_TB.LINK_ZONE_SEQ) — 장비 이관. 예: RE (3m)/RE (Above) → RE (Above) 챔버.
#      시험번호가 있는 시험만 (측정 행, 현황판 PURPOSE='TEST'). 시험번호 없이 사용은 이관하지 않는다 (X-5)
#   3) 단독 사용 챔버(CHAMBER_TB.SOLO_YN=1)의 다른 구역 — 예: RE (3m)는 RE (Below)·RE (Above) 중 하나만
#   4) 시험장 전체 점유(CHAMBER_ETC_USE_TB.WHOLE_YN=1)면 그 챔버의 모든 구역 (시험번호 없이 항목 "선택 안 함")
# 두 사용의 점유 구역이 하나라도 겹치면 함께 쓸 수 없다.
TESTED = "(u.SRC = 'RAW' OR u.PURPOSE = 'TEST')"
ACTIVE_OCC = f"""
    SELECT u.*, u.ZONE_SEQ AS OCC_ZONE FROM ({ACTIVE_UNION}
           ) u
    UNION ALL
    SELECT u.*, z.LINK_ZONE_SEQ FROM ({ACTIVE_UNION}
           ) u INNER JOIN CHAMBER_ZONE_TB z ON z.ZONE_SEQ = u.ZONE_SEQ
     WHERE z.LINK_ZONE_SEQ IS NOT NULL AND {TESTED}
    UNION ALL
    SELECT u.*, s.ZONE_SEQ FROM ({ACTIVE_UNION}
           ) u INNER JOIN CHAMBER_ZONE_TB z ON z.ZONE_SEQ = u.ZONE_SEQ
               INNER JOIN CHAMBER_TB sc ON sc.CHAMBER_SEQ = z.CHAMBER_SEQ
               INNER JOIN CHAMBER_ZONE_TB s ON s.CHAMBER_SEQ = z.CHAMBER_SEQ AND s.ZONE_SEQ <> u.ZONE_SEQ
     WHERE sc.SOLO_YN = 1 OR u.WHOLE_YN = 1"""

# 기록 수정에서 쓰는: [lo, hi) 와 시간이 겹치는 사용 건 (종료된 것 포함). 열은 ACTIVE_UNION + E_DT
RANGE_UNION = f"""
    SELECT 'RAW' AS SRC, e.ENV_SEQ AS SEQ, e.ZONE_SEQ, e.RAW_SEQ, NULL AS TEST_SEQ, e.MET_KEY,
           NULL AS PURPOSE, NULL AS MEMO, e.MEM_ID, {RAW_MEM_NAME} AS MEM_NAME, e.TEST_S_DT AS S_DT,
           NULL AS WITNESS_NAME, NULL AS REG_MEM_ID, 0 AS WHOLE_YN, e.TEST_E_DT AS E_DT
      FROM RAW_ENV_TB e
     WHERE e.DEL_YN = 0 AND e.MIG_YN = 0 AND e.TEST_S_DT IS NOT NULL AND e.ZONE_SEQ IS NOT NULL
       AND e.TEST_S_DT < %(hi)s AND (e.TEST_E_DT IS NULL OR e.TEST_E_DT > %(lo)s)
    UNION ALL
    SELECT 'ETC', x.USE_SEQ, x.ZONE_SEQ, NULL, x.TEST_SEQ, NULL,
           x.PURPOSE, x.MEMO, x.MEM_ID, x.MEM_NAME, x.START_DT,
           x.WITNESS_NAME, x.REG_MEM_ID, x.WHOLE_YN, x.END_DT
      FROM CHAMBER_ETC_USE_TB x
     WHERE x.DEL_YN = 0 AND x.USE_SEQ <> %(self)s
       AND x.START_DT < %(hi)s AND (x.END_DT IS NULL OR x.END_DT > %(lo)s)"""
RANGE_OCC = ACTIVE_OCC.replace(ACTIVE_UNION, RANGE_UNION)

STATUS_SQL = f"""
SELECT c.CHAMBER_SEQ, c.NAME AS CHAMBER_NAME, c.FLOOR, c.SOLO_YN,
       z.ZONE_SEQ, z.NAME AS ZONE_NAME, z.MULTI_YN, z.LINK_ZONE_SEQ,
       u.SRC, u.SEQ, u.MET_KEY, u.PURPOSE, u.MEMO, u.MEM_ID, u.MEM_NAME, u.S_DT, u.WITNESS_NAME, u.REG_MEM_ID,
       u.WHOLE_YN, TIMESTAMPDIFF(MINUTE, u.S_DT, NOW()) AS ELAPSED_MIN,
       {TEST_ID_SQL} AS TEST_ID,
       {PRDCT_SQL} AS PRDCT_NAME,
       {CMPY_SQL} AS CMPY_NAME
  FROM CHAMBER_TB c
       INNER JOIN CHAMBER_ZONE_TB z ON z.CHAMBER_SEQ = c.CHAMBER_SEQ AND z.USE_YN = 1
       LEFT JOIN ({ACTIVE_UNION}
       ) u ON u.ZONE_SEQ = z.ZONE_SEQ
       LEFT JOIN RAW_TB r ON r.RAW_SEQ = u.RAW_SEQ
       LEFT JOIN TEST_TB t ON t.TEST_SEQ = COALESCE(r.TEST_SEQ, u.TEST_SEQ)
 WHERE c.USE_YN = 1
 ORDER BY c.FLOOR, c.SORT_NO, c.CHAMBER_SEQ, z.SORT_NO, z.ZONE_SEQ, u.S_DT"""


def fetch_status() -> List[dict]:
    with connect() as conn, conn.cursor() as cur:
        cur.execute(STATUS_SQL)
        return list(cur.fetchall())


def fetch_chambers() -> List[dict]:
    with connect() as conn, conn.cursor() as cur:
        cur.execute(
            "SELECT CHAMBER_SEQ, NAME, FLOOR FROM CHAMBER_TB WHERE USE_YN = 1 ORDER BY FLOOR, SORT_NO, CHAMBER_SEQ"
        )
        return list(cur.fetchall())


def fetch_history(date_from: date, date_to: date, chamber_seqs: List[int],
                  include_deleted: bool = False, floor: Optional[int] = None, overlap: bool = False) -> List[dict]:
    """사용 기록 (기획 v7_1 SC-15). 기간은 시작 일시 기준, 진행 중인 건도 포함. history_limit + 1 건까지 읽는다.
    include_deleted 이면 현황판에서 삭제한 사용도 함께. 측정 행(RAW_ENV_TB)의 삭제는 ERP 가 하므로 보여 주지 않는다.
    나머지 조건(시험 항목 · 시험원 · 상태 · 사용 구분 · 검색)은 화면에서 거른다.
    overlap 이면 시작 일시 기준이 아니라 기간과 시간이 겹치는 기록 (시험원 현황 타임라인: 전날 시작해 이어지는 시험 포함)"""
    dt_from = date_from.strftime("%Y-%m-%d")
    dt_to = (date_to + timedelta(days=1)).strftime("%Y-%m-%d")
    etc_del = "" if include_deleted else "AND x.DEL_YN = 0"
    if overlap:
        raw_range = "e.TEST_S_DT < %s AND (e.TEST_E_DT IS NULL OR e.TEST_E_DT > %s)"
        etc_range = "x.START_DT < %s AND (x.END_DT IS NULL OR x.END_DT > %s)"
        params: list = [dt_to, dt_from, dt_to, dt_from]
    else:
        raw_range = "e.TEST_S_DT >= %s AND e.TEST_S_DT < %s"
        etc_range = "x.START_DT >= %s AND x.START_DT < %s"
        params = [dt_from, dt_to, dt_from, dt_to]

    conds: List[str] = []
    if chamber_seqs:
        conds.append(f"c.CHAMBER_SEQ IN ({', '.join(['%s'] * len(chamber_seqs))})")
        params += chamber_seqs
    if floor is not None:
        conds.append("c.FLOOR = %s")
        params.append(floor)
    where = ("WHERE " + " AND ".join(conds)) if conds else ""
    params.append(settings.history_limit + 1)

    sql = f"""
SELECT u.SRC, u.SEQ, u.RAW_SEQ, c.CHAMBER_SEQ, c.NAME AS CHAMBER_NAME, c.FLOOR, z.NAME AS ZONE_NAME,
       u.MET_KEY, u.PURPOSE, u.MEMO, u.MEM_ID, u.MEM_NAME, u.WITNESS_NAME, u.REG_MEM_ID, u.WHOLE_YN,
       u.S_DT, u.E_DT, TIMESTAMPDIFF(MINUTE, u.S_DT, COALESCE(u.E_DT, NOW())) AS USE_MIN,
       u.DEL_YN, u.DEL_REASON, u.DEL_DT, COALESCE(m.MEM_NAME, u.DEL_MEM_ID) AS DEL_MEM_NAME,
       u.UDT_DT, COALESCE(um.MEM_NAME, u.UDT_MEM_ID) AS UDT_MEM_NAME,
       t.TEST_SEQ, DATE_FORMAT(t.REPORT_DT, '%%y-%%m-%%d') AS REPORT_DT,
       (SELECT ti.TEST_STNDR_SEQ FROM TEST_ITEM_TB ti WHERE ti.TEST_ITEM_SEQ = t.TEST_ITEM_SEQ LIMIT 1) AS STNDR_SEQ,
       lc.NAME AS LINK_CHAMBER_NAME, lz.NAME AS LINK_ZONE_NAME,
       {TEST_ID_SQL} AS TEST_ID,
       {PRDCT_SQL} AS PRDCT_NAME,
       {CMPY_SQL} AS CMPY_NAME
  FROM (
    SELECT 'RAW' AS SRC, e.ENV_SEQ AS SEQ, e.ZONE_SEQ, e.RAW_SEQ, NULL AS TEST_SEQ, e.MET_KEY,
           NULL AS PURPOSE, NULL AS MEMO, e.MEM_ID, {RAW_MEM_NAME} AS MEM_NAME, NULL AS WITNESS_NAME,
           NULL AS REG_MEM_ID, 0 AS WHOLE_YN, e.TEST_S_DT AS S_DT, e.TEST_E_DT AS E_DT,
           0 AS DEL_YN, NULL AS DEL_REASON, NULL AS DEL_MEM_ID, NULL AS DEL_DT, NULL AS UDT_MEM_ID, NULL AS UDT_DT
      FROM RAW_ENV_TB e
     WHERE e.DEL_YN = 0 AND e.MIG_YN = 0 AND e.ZONE_SEQ IS NOT NULL
       AND {raw_range}
    UNION ALL
    SELECT 'ETC', x.USE_SEQ, x.ZONE_SEQ, NULL, x.TEST_SEQ, NULL,
           x.PURPOSE, x.MEMO, x.MEM_ID, x.MEM_NAME, x.WITNESS_NAME,
           x.REG_MEM_ID, x.WHOLE_YN, x.START_DT, x.END_DT,
           x.DEL_YN, x.DEL_REASON, x.DEL_MEM_ID, x.DEL_DT, x.UDT_MEM_ID, x.UDT_DT
      FROM CHAMBER_ETC_USE_TB x
     WHERE {etc_range} {etc_del}
       ) u
       INNER JOIN CHAMBER_ZONE_TB z ON z.ZONE_SEQ = u.ZONE_SEQ
       INNER JOIN CHAMBER_TB c ON c.CHAMBER_SEQ = z.CHAMBER_SEQ
       -- 장비 이관: 시험번호 있는 시험이 연계 구역이 있는 항목을 썼으면 (기획 SC-15 ⑦, X-5)
       LEFT JOIN CHAMBER_ZONE_TB lz ON lz.ZONE_SEQ = z.LINK_ZONE_SEQ AND (u.SRC = 'RAW' OR u.PURPOSE = 'TEST')
       LEFT JOIN CHAMBER_TB lc ON lc.CHAMBER_SEQ = lz.CHAMBER_SEQ
       LEFT JOIN RAW_TB r ON r.RAW_SEQ = u.RAW_SEQ
       LEFT JOIN TEST_TB t ON t.TEST_SEQ = COALESCE(r.TEST_SEQ, u.TEST_SEQ)
       LEFT JOIN MEMBER_TB m ON m.ID = u.DEL_MEM_ID
       LEFT JOIN MEMBER_TB um ON um.ID = u.UDT_MEM_ID
 {where}
 ORDER BY u.S_DT DESC
 LIMIT %s"""
    with connect() as conn, conn.cursor() as cur:
        cur.execute(sql, params)
        return list(cur.fetchall())


def fetch_use_his(use_seq: int) -> List[dict]:
    """기록 수정 이력 (최근 것부터)"""
    with connect() as conn, conn.cursor() as cur:
        cur.execute(
            """
SELECT h.HIS_SEQ, h.BEFORE_TXT, h.AFTER_TXT, h.REASON, h.INS_DT, COALESCE(m.MEM_NAME, h.MEM_ID) AS MEM_NAME
  FROM CHAMBER_ETC_USE_HIS_TB h
       LEFT JOIN MEMBER_TB m ON m.ID = h.MEM_ID
 WHERE h.USE_SEQ = %s
 ORDER BY h.HIS_SEQ DESC""",
            (use_seq,),
        )
        return list(cur.fetchall())


def edit_etc_use(use_seq: int, test_seq: Optional[int], mem_id: Optional[str], mem_name: Optional[str],
                 witness_name: Optional[str], start_dt: datetime, end_dt: Optional[datetime],
                 reason: str, by: str, before_txt: str, after_txt: str) -> None:
    """기록 수정 (기획 SC-16, P01·P02). 시험번호 · 시험원(입회면 입회자) · 시작 · 종료.
    시간을 옮겨 같은 시험장(점유 구역)의 다른 기록과 겹치면 저장하지 않는다 (V-3, 입회 시험 포함).
    시험원을 바꾸거나 시간을 옮겨 같은 시험원의 다른 기록과 시간이 겹치면 저장하지 않는다 (V-1).
    무엇을 바꿨는지는 CHAMBER_ETC_USE_HIS_TB 에 남긴다."""
    with connect() as conn, conn.cursor() as cur:
        cur.execute("SELECT x.USE_SEQ, x.ZONE_SEQ, x.WHOLE_YN, x.PURPOSE, x.END_DT, x.DEL_YN, z.MULTI_YN"
                    "  FROM CHAMBER_ETC_USE_TB x INNER JOIN CHAMBER_ZONE_TB z ON z.ZONE_SEQ = x.ZONE_SEQ"
                    " WHERE x.USE_SEQ = %s FOR UPDATE", (use_seq,))
        row = cur.fetchone()
        if not row or row["DEL_YN"]:
            raise Reject(404, "없는 기록입니다.")
        if row["END_DT"] is None and end_dt is not None:
            raise Reject(400, "진행 중인 기록은 종료 시각을 고칠 수 없습니다. 현황판에서 종료하세요.")
        purpose = row["PURPOSE"]
        if purpose != "NORD":
            if test_seq is not None:
                purpose = "TEST"
            elif purpose == "TEST":
                purpose = "ETC"            # 시험번호를 지우면 "시험번호 없이 사용 · 기타"

        # 시험장: 바꾼 시간에 이 기록이 점유하는 구역(장비 이관 · 단독 사용 · 전체 점유 포함)을 다른 기록이 쓰고 있으면
        # 저장하지 않는다 (V-3, 시작과 같은 규칙). 시험원과 관계없이 본다 — 입회 시험도 여기서 걸린다
        whole = bool(row["WHOLE_YN"])
        occ = occupied_zones(cur, int(row["ZONE_SEQ"]), link=purpose == "TEST", whole=whole)
        if whole:
            share = "1 = 1"
        elif row["MULTI_YN"]:
            share = "u.WHOLE_YN = 1"
        else:
            share = "uz.MULTI_YN = 0 OR u.WHOLE_YN = 1"
        args = {"lo": start_dt, "hi": end_dt or datetime.now(), "self": use_seq}
        args.update({f"z{i}": z for i, z in enumerate(occ)})
        cur.execute(
            f"""
SELECT {_BUSY_COLS}, u.E_DT, u.WITNESS_NAME, u.WHOLE_YN
  FROM ({RANGE_OCC}
       ) u {_BUSY_JOIN}
 WHERE u.OCC_ZONE IN ({", ".join(f"%(z{i})s" for i in range(len(occ)))})
   AND ({share})
 ORDER BY u.S_DT
 LIMIT 1""",
            args,
        )
        hit = cur.fetchone()
        if hit:
            conn.rollback()
            who = hit["MEM_NAME"] or (hit["WITNESS_NAME"] and "입회 " + hit["WITNESS_NAME"]) or "-"
            place = hit["CHAMBER_NAME"] + (" 시험장 점유" if hit["WHOLE_YN"] else " · " + hit["ZONE_NAME"] if hit["ZONE_NAME"] else "")
            raise Reject(409, f"같은 시간에 {place} 다른 기록({hit['S_DT']:%m-%d %H:%M} · {who})이 있어 "
                              f"시험장이 겹칩니다.", hit)
        if mem_id:
            cur.execute(
                f"""
SELECT {_BUSY_COLS}
  FROM (
    SELECT 'RAW' AS SRC, e.ENV_SEQ AS SEQ, e.ZONE_SEQ, e.RAW_SEQ, NULL AS TEST_SEQ, e.MET_KEY, NULL AS PURPOSE,
           {RAW_MEM_NAME} AS MEM_NAME, e.TEST_S_DT AS S_DT, e.TEST_E_DT AS E_DT
      FROM RAW_ENV_TB e
     WHERE e.MEM_ID = %s AND e.DEL_YN = 0 AND e.MIG_YN = 0 AND e.ZONE_SEQ IS NOT NULL AND e.TEST_S_DT IS NOT NULL
    UNION ALL
    SELECT 'ETC', x.USE_SEQ, x.ZONE_SEQ, NULL, x.TEST_SEQ, NULL, x.PURPOSE, x.MEM_NAME, x.START_DT, x.END_DT
      FROM CHAMBER_ETC_USE_TB x
     WHERE x.MEM_ID = %s AND x.DEL_YN = 0 AND x.USE_SEQ <> %s
       ) u {_BUSY_JOIN}
 WHERE u.S_DT < COALESCE(%s, '9999-12-31') AND (u.E_DT IS NULL OR u.E_DT > %s)
 ORDER BY u.S_DT
 LIMIT 1""",
                (mem_id, mem_id, use_seq, end_dt, start_dt),
            )
            hit = cur.fetchone()
            if hit:
                conn.rollback()
                raise Reject(409, f"{mem_name} 님의 다른 기록({hit['CHAMBER_NAME']} {hit['S_DT']:%m-%d %H:%M})과 "
                                  f"시간이 겹칩니다.", hit)
        cur.execute(
            """
UPDATE CHAMBER_ETC_USE_TB
   SET TEST_SEQ = %s, PURPOSE = %s, MEM_ID = %s, MEM_NAME = %s, WITNESS_NAME = %s,
       START_DT = %s, END_DT = COALESCE(%s, END_DT), UDT_MEM_ID = %s, UDT_DT = NOW()
 WHERE USE_SEQ = %s""",
            (test_seq if purpose != "NORD" else None, purpose, mem_id, mem_name, witness_name,
             start_dt, end_dt, by, use_seq),
        )
        cur.execute(
            """
INSERT INTO CHAMBER_ETC_USE_HIS_TB (USE_SEQ, BEFORE_TXT, AFTER_TXT, REASON, MEM_ID, INS_DT)
VALUES (%s, %s, %s, %s, %s, NOW())""",
            (use_seq, before_txt[:1000], after_txt[:1000], reason, by),
        )
        conn.commit()


def fetch_etc_use(use_seq: int) -> Optional[dict]:
    """기록 수정 전 값 (수정 이력에 남길 비교용)"""
    with connect() as conn, conn.cursor() as cur:
        cur.execute(
            f"""
SELECT x.USE_SEQ, x.PURPOSE, x.TEST_SEQ, x.MEM_ID, x.MEM_NAME, x.WITNESS_NAME, x.START_DT, x.END_DT, x.DEL_YN,
       {TEST_ID_SQL} AS TEST_ID
  FROM CHAMBER_ETC_USE_TB x
       LEFT JOIN TEST_TB t ON t.TEST_SEQ = x.TEST_SEQ
 WHERE x.USE_SEQ = %s""",
            (use_seq,),
        )
        return cur.fetchone()


def search_test_by_seq(test_seq: int) -> Optional[dict]:
    """EMC 시험 1건 (상태 관계없이). 기록 수정에서 시험번호 확인"""
    with connect() as conn, conn.cursor() as cur:
        cur.execute(f"SELECT t.TEST_SEQ, {TEST_ID_SQL} AS TEST_ID FROM TEST_TB t"
                    f" WHERE t.TEST_SEQ = %s AND t.TEST_TYPE_CODE = 'EM'", (test_seq,))
        return cur.fetchone()


def search_tests(q: str) -> List[dict]:
    """기록 수정에서 시험번호 연결: EMC 시험을 상태와 관계없이 번호 · 업체명으로 찾는다 (최대 30건)"""
    like = "%" + q.replace("%", "").replace("_", "") + "%"
    with connect() as conn, conn.cursor() as cur:
        cur.execute(
            f"""
SELECT * FROM (
  SELECT t.TEST_SEQ, {TEST_ID_SQL} AS TEST_ID, {PRDCT_SQL} AS PRDCT_NAME, {CMPY_SQL} AS CMPY_NAME, t.TEST_STATE_CODE
    FROM TEST_TB t
   WHERE t.TEST_TYPE_CODE = 'EM'
) s
 WHERE s.TEST_ID LIKE %s OR s.CMPY_NAME LIKE %s
 ORDER BY s.TEST_SEQ DESC
 LIMIT 30""",
            (like, like),
        )
        return list(cur.fetchall())


# ---------------------------------------------------------------- 로데이터 외 사용

_BUSY_COLS = f"""u.SRC, u.MET_KEY, u.PURPOSE, u.MEM_NAME, u.S_DT, {TEST_ID_SQL} AS TEST_ID,
       uc.NAME AS CHAMBER_NAME, uz.NAME AS ZONE_NAME"""
_BUSY_JOIN = """
       INNER JOIN CHAMBER_ZONE_TB uz ON uz.ZONE_SEQ = u.ZONE_SEQ
       INNER JOIN CHAMBER_TB uc ON uc.CHAMBER_SEQ = uz.CHAMBER_SEQ
       LEFT JOIN RAW_TB r ON r.RAW_SEQ = u.RAW_SEQ
       LEFT JOIN TEST_TB t ON t.TEST_SEQ = COALESCE(r.TEST_SEQ, u.TEST_SEQ)"""


def occupied_zones(cur, zone_seq: int, link: bool, whole: bool) -> List[int]:
    """새 사용이 점유할 구역 (자신 포함, 번호 순). link = 시험번호 있는 시험(장비 이관), whole = 시험장 전체"""
    cur.execute(
        """
SELECT z.ZONE_SEQ AS OCC_ZONE FROM CHAMBER_ZONE_TB z WHERE z.ZONE_SEQ = %s
UNION
SELECT z.LINK_ZONE_SEQ FROM CHAMBER_ZONE_TB z WHERE z.ZONE_SEQ = %s AND z.LINK_ZONE_SEQ IS NOT NULL AND %s = 1
UNION
SELECT s.ZONE_SEQ
  FROM CHAMBER_ZONE_TB z
       INNER JOIN CHAMBER_TB sc ON sc.CHAMBER_SEQ = z.CHAMBER_SEQ
       INNER JOIN CHAMBER_ZONE_TB s ON s.CHAMBER_SEQ = z.CHAMBER_SEQ
 WHERE z.ZONE_SEQ = %s AND (sc.SOLO_YN = 1 OR %s = 1)
 ORDER BY 1""",
        (zone_seq, zone_seq, int(link), zone_seq, int(whole)))
    return [int(r["OCC_ZONE"]) for r in cur.fetchall()]


def start_etc_use(zone_seq: int, purpose: str, test_seq: Optional[int], memo: str,
                  mem_id: Optional[str], mem_name: Optional[str], reg_mem_id: str,
                  witness_name: Optional[str] = None, whole: bool = False) -> int:
    """구역 잠금 → 사용 중 확인(연계·단독 사용 포함) → 1인 1시험 확인 → INSERT 를 한 트랜잭션에서 한다.
    mem_id = 시험원, reg_mem_id = [사용 시작]을 누른 사람 (기술책임자·관리자가 시험원을 골라 시작한 경우 다름).
    고객 입회 시험은 시험원을 지정하지 않는다 (mem_id=None, witness_name=입회자). 1인 1시험 확인도 하지 않는다.
    whole = 시험장 전체 점유 (시험번호 없이 사용 + 항목 선택 안 함). 그 챔버의 첫 구역에 기록하고 WHOLE_YN=1"""
    with connect() as conn, conn.cursor() as cur:
        cur.execute(
            "SELECT z.ZONE_SEQ, z.CHAMBER_SEQ, z.MULTI_YN FROM CHAMBER_ZONE_TB z WHERE z.ZONE_SEQ = %s AND z.USE_YN = 1",
            (zone_seq,),
        )
        zone = cur.fetchone()
        if not zone:
            raise Reject(400, "사용할 수 없는 구역입니다.")
        if whole:
            cur.execute("SELECT ZONE_SEQ FROM CHAMBER_ZONE_TB WHERE CHAMBER_SEQ = %s AND USE_YN = 1"
                        " ORDER BY SORT_NO, ZONE_SEQ LIMIT 1", (zone["CHAMBER_SEQ"],))
            zone_seq = int(cur.fetchone()["ZONE_SEQ"])

        # 점유할 구역을 모두, 번호 순으로 잠근다. ERP 측정 [시작]도 같은 순서로 잠근다 (교착 방지)
        # chamber-svc 는 구역만 잠근다 (RAW_TB 는 잠그지 않는다)
        occ = occupied_zones(cur, zone_seq, link=purpose == "TEST", whole=whole)
        marks = ", ".join(["%s"] * len(occ))
        cur.execute(f"SELECT ZONE_SEQ FROM CHAMBER_ZONE_TB WHERE ZONE_SEQ IN ({marks})"
                    f" ORDER BY ZONE_SEQ FOR UPDATE", occ)
        # 점유 구역이 하나라도 겹치는 진행 중 사용. 동시 사용 허용 구역(MULTI_YN=1)의 사용끼리는 겹쳐도 된다.
        # 시험장 전체 점유는 무엇과도 함께 쓸 수 없다
        if whole:
            share = "1 = 1"
        elif zone["MULTI_YN"]:
            share = "u.WHOLE_YN = 1"
        else:
            share = "uz.MULTI_YN = 0 OR u.WHOLE_YN = 1"
        cur.execute(
            f"""
SELECT {_BUSY_COLS}
  FROM ({ACTIVE_OCC}
       ) u {_BUSY_JOIN}
 WHERE u.OCC_ZONE IN ({marks})
   AND ({share})
 ORDER BY u.S_DT
 LIMIT 1""",
            occ,
        )
        busy = cur.fetchone()
        if busy:
            conn.rollback()
            same = busy["CHAMBER_NAME"] == _chamber_name(cur, zone_seq)
            if whole and same:
                raise Reject(409, "다른 항목이 진행 중이라 시험장 전체를 점유할 수 없습니다. 항목을 고르세요.", busy)
            raise Reject(409, "이미 사용 중인 시험장입니다." if same
                         else f"{busy['CHAMBER_NAME']} 시험과 장비·시험장을 함께 쓰고 있어 시작할 수 없습니다.", busy)

        # 1인 1시험: 같은 시험원이 진행 중인 측정 행·현황판 사용이 있으면 안 된다 (입회 시험은 시험원이 없다)
        mine = None
        if mem_id:
            cur.execute(
                f"""
SELECT {_BUSY_COLS}
  FROM ({ACTIVE_UNION}
       ) u {_BUSY_JOIN}
 WHERE u.MEM_ID = %s
 ORDER BY u.S_DT
 LIMIT 1""",
                (mem_id,),
            )
            mine = cur.fetchone()
        if mine:
            conn.rollback()
            raise Reject(409, f"{mem_name} 님은 {mine['CHAMBER_NAME']}에서 진행 중인 시험이 있습니다. "
                              f"한 사람은 한 번에 한 시험만 할 수 있습니다.", mine)

        cur.execute(
            f"""
INSERT INTO CHAMBER_ETC_USE_TB
       (ZONE_SEQ, PURPOSE, TEST_SEQ, MEMO, MEM_ID, MEM_NAME, REG_MEM_ID, WITNESS_NAME, WHOLE_YN, START_DT, INS_DT)
VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, NOW(), NOW())""",
            (zone_seq, purpose, test_seq, memo or None, mem_id, mem_name, reg_mem_id, witness_name, int(whole)),
        )
        use_seq = cur.lastrowid
        conn.commit()
        return use_seq


def fetch_zone_name(zone_seq: int) -> Optional[str]:
    """사용 중인 구역 이름. 없거나 사용 안 하는 구역이면 None"""
    with connect() as conn, conn.cursor() as cur:
        cur.execute("SELECT z.NAME FROM CHAMBER_ZONE_TB z INNER JOIN CHAMBER_TB c ON c.CHAMBER_SEQ = z.CHAMBER_SEQ"
                    " WHERE z.ZONE_SEQ = %s AND z.USE_YN = 1 AND c.USE_YN = 1", (zone_seq,))
        row = cur.fetchone()
    return row["NAME"] if row else None


def _chamber_name(cur, zone_seq: int) -> str:
    cur.execute("SELECT c.NAME FROM CHAMBER_ZONE_TB z INNER JOIN CHAMBER_TB c ON c.CHAMBER_SEQ = z.CHAMBER_SEQ"
                " WHERE z.ZONE_SEQ = %s", (zone_seq,))
    row = cur.fetchone()
    return row["NAME"] if row else ""


# ---------------------------------------------------------------- 시험번호 (사용 시작 때 고를 목록)
# 접수·시험중·디버깅(TEST_STATE_CODE 1·2·3, 설정 TEST_STATES) · EMC 시험(TEST_TYPE_CODE='EM') · 삭제 안 된 항목
# · 자동화 로데이터 규격 제외.
# 일반 사용자는 담당자(TEST_MNGR_TB)에 본인이 있는 시험만.

def _test_where(mem_id: Optional[str]) -> tuple:
    from .config import RD_AUTO_STNDR, TEST_STATES
    states = ", ".join(["%s"] * len(TEST_STATES)) or "NULL"
    marks = ", ".join(["%s"] * len(RD_AUTO_STNDR)) or "NULL"
    where = (f"t.TEST_STATE_CODE IN ({states}) AND t.TEST_TYPE_CODE = 'EM' AND (ti.STATE IS NULL OR ti.STATE <> 'D')"
             f" AND ti.TEST_STNDR_SEQ NOT IN ({marks})")
    params: list = list(TEST_STATES) + list(RD_AUTO_STNDR)
    if mem_id:
        where += " AND EXISTS (SELECT 1 FROM TEST_MNGR_TB m WHERE m.TEST_SEQ = t.TEST_SEQ AND m.TEST_MNG_ID = %s)"
        params.append(mem_id)
    return where, params


def fetch_tests(mem_id: Optional[str]) -> List[dict]:
    """고를 수 있는 시험 + 담당자. mem_id 가 있으면 그 사람이 담당자인 시험만"""
    where, params = _test_where(mem_id)
    with connect() as conn, conn.cursor() as cur:
        cur.execute(
            f"""
SELECT t.TEST_SEQ, t.TEST_STATE_CODE, {TEST_ID_SQL} AS TEST_ID, {PRDCT_SQL} AS PRDCT_NAME, {CMPY_SQL} AS CMPY_NAME
  FROM TEST_TB t
       INNER JOIN TEST_ITEM_TB ti ON ti.TEST_ITEM_SEQ = t.TEST_ITEM_SEQ
 WHERE {where}
 ORDER BY t.TEST_SEQ DESC
 LIMIT 1000""",
            params,
        )
        tests = list(cur.fetchall())
        mngrs = _test_mngrs(cur, [r["TEST_SEQ"] for r in tests])
    for r in tests:
        r["MNGRS"] = mngrs.get(r["TEST_SEQ"], [])
    return tests


def fetch_test(test_seq: int, mem_id: Optional[str]) -> Optional[dict]:
    """사용 시작 때 다시 확인: 고를 수 있는 시험인지 + 담당자"""
    where, params = _test_where(mem_id)
    with connect() as conn, conn.cursor() as cur:
        cur.execute(f"""
SELECT t.TEST_SEQ, {TEST_ID_SQL} AS TEST_ID
  FROM TEST_TB t
       INNER JOIN TEST_ITEM_TB ti ON ti.TEST_ITEM_SEQ = t.TEST_ITEM_SEQ
 WHERE t.TEST_SEQ = %s AND {where}""", [test_seq] + params)
        row = cur.fetchone()
        if row:
            row["MNGRS"] = _test_mngrs(cur, [test_seq]).get(test_seq, [])
        return row


def _test_mngrs(cur, test_seqs: List[int]) -> dict:
    if not test_seqs:
        return {}
    marks = ", ".join(["%s"] * len(test_seqs))
    cur.execute(f"""
SELECT m.TEST_SEQ, m.TEST_MNG_ID AS ID, COALESCE(mb.MEM_NAME, m.TEST_MNG_ID) AS NAME
  FROM TEST_MNGR_TB m
       LEFT JOIN MEMBER_TB mb ON mb.ID = m.TEST_MNG_ID
 WHERE m.TEST_SEQ IN ({marks}) AND m.TEST_MNG_ID IS NOT NULL AND m.TEST_MNG_ID <> ''
 ORDER BY m.TEST_SEQ, m.TEST_MNGR_SEQ""", test_seqs)
    out: dict = {}
    for r in cur.fetchall():
        lst = out.setdefault(r["TEST_SEQ"], [])
        if all(x["ID"] != r["ID"] for x in lst):
            lst.append({"ID": r["ID"], "NAME": r["NAME"]})
    return out


def _own_etc_use(cur, use_seq: int, mem_id: str, is_rev: bool) -> dict:
    cur.execute(
        f"SELECT USE_SEQ, MEM_ID, REG_MEM_ID, END_DT FROM CHAMBER_ETC_USE_TB"
        f" WHERE USE_SEQ = %s AND DEL_YN = 0 FOR UPDATE",
        (use_seq,),
    )
    row = cur.fetchone()
    if not row:
        raise Reject(404, "기록을 찾을 수 없습니다.")
    mine = row["MEM_ID"] == mem_id or (not row["MEM_ID"] and row["REG_MEM_ID"] == mem_id)   # 입회 시험은 등록자
    if not mine and not is_rev:
        raise Reject(403, "본인 시험(입회 시험은 등록한 사람)이거나 기술책임자·관리자만 할 수 있습니다.")
    return row


def end_etc_use(use_seq: int, mem_id: str, is_rev: bool) -> None:
    with connect() as conn, conn.cursor() as cur:
        row = _own_etc_use(cur, use_seq, mem_id, is_rev)
        if row["END_DT"] is not None:
            raise Reject(409, "이미 종료된 기록입니다.")
        cur.execute(
            f"UPDATE CHAMBER_ETC_USE_TB SET END_DT = NOW(), UDT_MEM_ID = %s, UDT_DT = NOW()"
            f" WHERE USE_SEQ = %s",
            (mem_id, use_seq),
        )
        conn.commit()


def delete_etc_use(use_seq: int, reason: str, mem_id: str, is_rev: bool) -> None:
    with connect() as conn, conn.cursor() as cur:
        _own_etc_use(cur, use_seq, mem_id, is_rev)
        cur.execute(
            f"UPDATE CHAMBER_ETC_USE_TB"
            f" SET DEL_YN = 1, DEL_REASON = %s, DEL_MEM_ID = %s, DEL_DT = NOW()"
            f" WHERE USE_SEQ = %s",
            (reason, mem_id, use_seq),
        )
        conn.commit()


# ---------------------------------------------------------------- 시험원 (기술책임자·관리자가 시험원을 고를 때)

_EMC_MEMBER_WHERE = "DEPT_SEQ = %s AND NOT (QUIT_DT IS NOT NULL AND QUIT_DT > '1900-01-01' AND QUIT_DT <= NOW())"


def fetch_members() -> List[dict]:
    """EMC 부서 재직자 + 지금 진행 중인 시험이 있으면 그 시험장 (1인 1시험: 고를 수 없다)"""
    with connect() as conn, conn.cursor() as cur:
        cur.execute(f"SELECT ID, MEM_NAME FROM MEMBER_TB WHERE {_EMC_MEMBER_WHERE} ORDER BY MEM_NAME",
                    (settings.emc_dept_seq,))
        members = list(cur.fetchall())
        cur.execute(f"""
SELECT u.MEM_ID, MIN(uc.NAME) AS CHAMBER_NAME
  FROM ({ACTIVE_UNION}
       ) u {_BUSY_JOIN}
 WHERE u.MEM_ID IS NOT NULL
 GROUP BY u.MEM_ID""")
        busy = {r["MEM_ID"]: r["CHAMBER_NAME"] for r in cur.fetchall()}
    for m in members:
        m["BUSY"] = busy.get(m["ID"])
    return members


def fetch_emc_member(mem_id: str) -> Optional[dict]:
    """고른 시험원이 EMC 부서 재직자인지"""
    with connect() as conn, conn.cursor() as cur:
        cur.execute(f"SELECT ID, MEM_NAME FROM MEMBER_TB WHERE ID = %s AND {_EMC_MEMBER_WHERE}",
                    (mem_id, settings.emc_dept_seq))
        return cur.fetchone()


# ---------------------------------------------------------------- 챔버 관리
# CHAMBER_TB, CHAMBER_ZONE_TB, CHAMBER_ZONE_MET_TB 는 이 화면만 쓴다. ERP 는 읽기만 한다.
# 챔버·구역은 지우지 않고 USE_YN=0 으로 숨긴다. 매핑(CHAMBER_ZONE_MET_TB)만 행을 넣고 지운다.

def fetch_admin_tree() -> dict:
    """숨긴 것까지 포함한 챔버·구역·매핑 전체와 구역별 진행 중 건수"""
    with connect() as conn, conn.cursor() as cur:
        cur.execute("SELECT CHAMBER_SEQ, NAME, FLOOR, SOLO_YN, SORT_NO, USE_YN FROM CHAMBER_TB ORDER BY FLOOR, SORT_NO, CHAMBER_SEQ")
        chambers = list(cur.fetchall())
        cur.execute(
            "SELECT ZONE_SEQ, CHAMBER_SEQ, NAME, MULTI_YN, LINK_ZONE_SEQ, SORT_NO, USE_YN FROM CHAMBER_ZONE_TB"
            " ORDER BY SORT_NO, ZONE_SEQ"
        )
        zones = list(cur.fetchall())
        cur.execute("SELECT ZONE_SEQ, MET_KEY FROM CHAMBER_ZONE_MET_TB")
        mets = list(cur.fetchall())
        cur.execute(f"SELECT u.ZONE_SEQ, COUNT(*) AS CNT FROM ({ACTIVE_UNION}\n) u GROUP BY u.ZONE_SEQ")
        active = {r["ZONE_SEQ"]: int(r["CNT"]) for r in cur.fetchall()}
        # 다른 구역의 사용 때문에 점유된 구역 (현황판·시작과 같은 규칙: 시험장 전체 점유 · 단독 사용 · 장비 이관)
        cur.execute(f"""
SELECT u.OCC_ZONE, u.WHOLE_YN, uz.CHAMBER_SEQ AS USE_CHAMBER, oz.CHAMBER_SEQ AS OCC_CHAMBER,
       uc.NAME AS CHAMBER_NAME, uz.NAME AS ZONE_NAME
  FROM ({ACTIVE_OCC}
       ) u
       INNER JOIN CHAMBER_ZONE_TB uz ON uz.ZONE_SEQ = u.ZONE_SEQ
       INNER JOIN CHAMBER_TB uc ON uc.CHAMBER_SEQ = uz.CHAMBER_SEQ
       INNER JOIN CHAMBER_ZONE_TB oz ON oz.ZONE_SEQ = u.OCC_ZONE
 WHERE u.OCC_ZONE <> u.ZONE_SEQ""")
        held: dict = {}
        for r in cur.fetchall():
            held.setdefault(r["OCC_ZONE"], set()).add(_held_reason(r))
        return {"chambers": chambers, "zones": zones, "mets": mets, "active": active,
                "held": {k: sorted(v) for k, v in held.items()}}


def _held_reason(r: dict) -> str:
    """점유 사유 (챔버 관리 표시용)"""
    if r["WHOLE_YN"]:
        return "시험장 전체 점유"
    if r["USE_CHAMBER"] == r["OCC_CHAMBER"]:
        return f"단독 사용 · {r['ZONE_NAME'] or '다른 항목'} 사용 중"
    return f"장비 이관 · {r['CHAMBER_NAME']}" + (f" · {r['ZONE_NAME']}" if r["ZONE_NAME"] else "") + " 시험"


def _active_count(cur, zone_seqs: List[int]) -> int:
    """이 구역들을 쓰거나 점유하고 있는 진행 중 사용 건수 (시험장 전체 점유 · 단독 사용 · 장비 이관 포함)"""
    if not zone_seqs:
        return 0
    marks = ", ".join(["%s"] * len(zone_seqs))
    cur.execute(f"SELECT COUNT(DISTINCT u.SRC, u.SEQ) AS CNT FROM ({ACTIVE_OCC}\n) u WHERE u.OCC_ZONE IN ({marks})",
                zone_seqs)
    return int(cur.fetchone()["CNT"])


def save_chamber(chamber_seq: Optional[int], name: str, floor: int, solo_yn: int, sort_no: int, use_yn: int,
                 mem_id: str, force: bool) -> int:
    """solo_yn=1: 구역이 여러 개여도 한 번에 한 구역만 쓴다 (예: RE (3m)의 RE (Below)·RE (Above))"""
    with connect() as conn, conn.cursor() as cur:
        if chamber_seq is None:
            cur.execute(
                "INSERT INTO CHAMBER_TB (NAME, FLOOR, SOLO_YN, SORT_NO, USE_YN, INS_MEM_ID, INS_DT)"
                " VALUES (%s, %s, %s, %s, %s, %s, NOW())",
                (name, floor, solo_yn, sort_no, use_yn, mem_id),
            )
            chamber_seq = cur.lastrowid
        else:
            cur.execute("SELECT USE_YN FROM CHAMBER_TB WHERE CHAMBER_SEQ = %s FOR UPDATE", (chamber_seq,))
            old = cur.fetchone()
            if not old:
                raise Reject(404, "챔버를 찾을 수 없습니다.")
            if old["USE_YN"] and not use_yn and not force:
                cur.execute("SELECT ZONE_SEQ FROM CHAMBER_ZONE_TB WHERE CHAMBER_SEQ = %s", (chamber_seq,))
                cnt = _active_count(cur, [r["ZONE_SEQ"] for r in cur.fetchall()])
                if cnt:
                    raise Reject(409, f"이 챔버를 사용·점유 중인 건이 {cnt}건 있습니다. 그래도 숨길까요?", {"confirm": True})
            cur.execute(
                "UPDATE CHAMBER_TB SET NAME = %s, FLOOR = %s, SOLO_YN = %s, SORT_NO = %s, USE_YN = %s,"
                " UDT_MEM_ID = %s, UDT_DT = NOW() WHERE CHAMBER_SEQ = %s",
                (name, floor, solo_yn, sort_no, use_yn, mem_id, chamber_seq),
            )
        conn.commit()
        return chamber_seq


def save_zone(zone_seq: Optional[int], chamber_seq: int, name: str, multi_yn: int, link_zone_seq: Optional[int],
              sort_no: int, use_yn: int, met_keys: List[str], mem_id: str, force: bool) -> int:
    """구역과 시험방법 매핑을 한 트랜잭션에서 저장한다.
    link_zone_seq: 이 구역을 쓰면 함께 점유되는 구역 (장비 이관. 다른 챔버의 구역만)"""
    with connect() as conn, conn.cursor() as cur:
        cur.execute("SELECT CHAMBER_SEQ FROM CHAMBER_TB WHERE CHAMBER_SEQ = %s", (chamber_seq,))
        if not cur.fetchone():
            raise Reject(400, "챔버를 찾을 수 없습니다.")
        if link_zone_seq is not None:
            cur.execute("SELECT CHAMBER_SEQ, MULTI_YN, LINK_ZONE_SEQ FROM CHAMBER_ZONE_TB WHERE ZONE_SEQ = %s",
                        (link_zone_seq,))
            target = cur.fetchone()
            if not target or target["CHAMBER_SEQ"] == chamber_seq:
                raise Reject(400, "연계 구역은 다른 챔버의 구역이어야 합니다.")
            if target["MULTI_YN"] or multi_yn:
                raise Reject(400, "동시 사용 허용 구역은 연계할 수 없습니다.")
            if target["LINK_ZONE_SEQ"] is not None:
                raise Reject(400, "연계 구역이 또 다른 구역과 연계되어 있으면 안 됩니다 (한 단계만).")
        if zone_seq is None:
            cur.execute(
                "INSERT INTO CHAMBER_ZONE_TB (CHAMBER_SEQ, NAME, MULTI_YN, LINK_ZONE_SEQ, SORT_NO, USE_YN,"
                " INS_MEM_ID, INS_DT) VALUES (%s, %s, %s, %s, %s, %s, %s, NOW())",
                (chamber_seq, name, multi_yn, link_zone_seq, sort_no, use_yn, mem_id),
            )
            zone_seq = cur.lastrowid
            old_keys: set = set()
        else:
            # [사용]·측정 [시작]이 잠그는 것과 같은 행이다. 저장하는 동안 그 구역의 시작이 잠깐 기다린다
            cur.execute("SELECT USE_YN FROM CHAMBER_ZONE_TB WHERE ZONE_SEQ = %s FOR UPDATE", (zone_seq,))
            old = cur.fetchone()
            if not old:
                raise Reject(404, "구역을 찾을 수 없습니다.")
            if old["USE_YN"] and not use_yn and not force:
                cnt = _active_count(cur, [zone_seq])
                if cnt:
                    raise Reject(409, f"이 구역을 사용·점유 중인 건이 {cnt}건 있습니다. 그래도 숨길까요?", {"confirm": True})
            cur.execute(
                "UPDATE CHAMBER_ZONE_TB SET CHAMBER_SEQ = %s, NAME = %s, MULTI_YN = %s, LINK_ZONE_SEQ = %s,"
                " SORT_NO = %s, USE_YN = %s, UDT_MEM_ID = %s, UDT_DT = NOW() WHERE ZONE_SEQ = %s",
                (chamber_seq, name, multi_yn, link_zone_seq, sort_no, use_yn, mem_id, zone_seq),
            )
            cur.execute("SELECT MET_KEY FROM CHAMBER_ZONE_MET_TB WHERE ZONE_SEQ = %s", (zone_seq,))
            old_keys = {r["MET_KEY"] for r in cur.fetchall()}

        new_keys = set(met_keys)
        for key in sorted(old_keys - new_keys):
            cur.execute("DELETE FROM CHAMBER_ZONE_MET_TB WHERE ZONE_SEQ = %s AND MET_KEY = %s", (zone_seq, key))
        for key in sorted(new_keys - old_keys):
            cur.execute("INSERT INTO CHAMBER_ZONE_MET_TB (ZONE_SEQ, MET_KEY) VALUES (%s, %s)", (zone_seq, key))
        conn.commit()
        return zone_seq


# ---------------------------------------------------------------- 로그인

def fetch_member(mem_id: str) -> Optional[dict]:
    with connect() as conn, conn.cursor() as cur:
        cur.execute(
            f"""
SELECT ID, PASSWORD, MEM_NAME, LOCK_YN, DEPT_SEQ, AUTH_CODE, FAIL_PASS_CNT,
       IF(QUIT_DT IS NOT NULL AND QUIT_DT > '1900-01-01' AND QUIT_DT <= NOW(), 1, 0) AS QUIT_YN
  FROM MEMBER_TB
 WHERE ID = %s""",
            (mem_id,),
        )
        return cur.fetchone()


def login_failed(mem_id: str) -> int:
    """ERP 와 같은 MEMBER_TB.FAIL_PASS_CNT 를 올리고, 한도에 닿으면 계정을 잠근다 (LOCK_YN=1).
    잠긴 계정은 ERP 로그인도 막힌다. 해제는 ERP 관리자가 한다. 올린 뒤의 횟수를 돌려준다."""
    with connect() as conn, conn.cursor() as cur:
        cur.execute(
            "UPDATE MEMBER_TB"
            "   SET FAIL_PASS_CNT = IFNULL(FAIL_PASS_CNT, 0) + 1,"
            "       LOCK_YN = IF(FAIL_PASS_CNT >= %s, 1, LOCK_YN)"   # 왼쪽부터 계산되므로 올린 뒤의 값을 본다
            " WHERE ID = %s",
            (settings.login_fail_max, mem_id),
        )
        cur.execute("SELECT FAIL_PASS_CNT FROM MEMBER_TB WHERE ID = %s", (mem_id,))
        row = cur.fetchone()
        conn.commit()
        return int(row["FAIL_PASS_CNT"] or 0) if row else 0


def login_succeeded(mem_id: str) -> None:
    """실패 횟수만 0으로 돌린다. LAST_IP·LAST_LOGIN_DT 는 ERP 로그인 기록이라 건드리지 않는다."""
    with connect() as conn, conn.cursor() as cur:
        cur.execute("UPDATE MEMBER_TB SET FAIL_PASS_CNT = 0 WHERE ID = %s AND FAIL_PASS_CNT <> 0", (mem_id,))
        conn.commit()


def ping() -> Optional[str]:
    """DB 연결 확인. 문제 없으면 None, 있으면 오류 종류."""
    try:
        with connect() as conn, conn.cursor() as cur:
            cur.execute("SELECT 1")
        return None
    except Exception as e:  # noqa: BLE001 — 상태 확인용
        return type(e).__name__
