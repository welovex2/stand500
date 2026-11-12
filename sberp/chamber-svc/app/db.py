"""
erp 스키마를 읽고, CHAMBER_ETC_USE_TB(로데이터 외 사용)에만 쓴다. (MariaDB 10.1 — CTE 없음)
다른 테이블은 계정 권한으로도 쓸 수 없다 (sql/chamber_svc_user.sql).

"진행 중"의 정의는 ERP 와 같아야 한다 (docs/design/chamber-svc.md §2.2):
  측정 행          : RAW_ENV_TB          DEL_YN=0 AND MIG_YN=0 AND TEST_S_DT IS NOT NULL AND TEST_E_DT IS NULL
  로데이터 외 사용 : CHAMBER_ETC_USE_TB  DEL_YN=0 AND END_DT IS NULL
"""
import threading
from contextlib import contextmanager
from datetime import date, timedelta
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

RAW_ACTIVE = "e.DEL_YN = 0 AND e.MIG_YN = 0 AND e.TEST_S_DT IS NOT NULL AND e.TEST_E_DT IS NULL"
ETC_ACTIVE = "x.DEL_YN = 0 AND x.END_DT IS NULL"

# 진행 중인 사용 건 (측정 행 + 로데이터 외 사용)
ACTIVE_UNION = f"""
    SELECT 'RAW' AS SRC, e.ENV_SEQ AS SEQ, e.ZONE_SEQ, e.RAW_SEQ, e.MET_KEY,
           NULL AS PURPOSE, NULL AS MEMO, NULL AS MEM_ID, e.MEM_NAME, e.TEST_S_DT AS S_DT
      FROM RAW_ENV_TB e
     WHERE {RAW_ACTIVE} AND e.ZONE_SEQ IS NOT NULL
    UNION ALL
    SELECT 'ETC', x.USE_SEQ, x.ZONE_SEQ, NULL, NULL,
           x.PURPOSE, x.MEMO, x.MEM_ID, x.MEM_NAME, x.START_DT
      FROM CHAMBER_ETC_USE_TB x
     WHERE {ETC_ACTIVE}"""

STATUS_SQL = f"""
SELECT c.CHAMBER_SEQ, c.NAME AS CHAMBER_NAME, z.ZONE_SEQ, z.NAME AS ZONE_NAME, z.MULTI_YN,
       u.SRC, u.SEQ, u.MET_KEY, u.PURPOSE, u.MEMO, u.MEM_ID, u.MEM_NAME, u.S_DT,
       TIMESTAMPDIFF(MINUTE, u.S_DT, NOW()) AS ELAPSED_MIN,
       {TEST_ID_SQL} AS TEST_ID,
       {PRDCT_SQL} AS PRDCT_NAME
  FROM CHAMBER_TB c
       INNER JOIN CHAMBER_ZONE_TB z ON z.CHAMBER_SEQ = c.CHAMBER_SEQ AND z.USE_YN = 1
       LEFT JOIN ({ACTIVE_UNION}
       ) u ON u.ZONE_SEQ = z.ZONE_SEQ
       LEFT JOIN RAW_TB r ON r.RAW_SEQ = u.RAW_SEQ
       LEFT JOIN TEST_TB t ON t.TEST_SEQ = r.TEST_SEQ
 WHERE c.USE_YN = 1
 ORDER BY c.SORT_NO, c.CHAMBER_SEQ, z.SORT_NO, z.ZONE_SEQ, u.S_DT"""


def fetch_status() -> List[dict]:
    with connect() as conn, conn.cursor() as cur:
        cur.execute(STATUS_SQL)
        return list(cur.fetchall())


def fetch_chambers() -> List[dict]:
    with connect() as conn, conn.cursor() as cur:
        cur.execute(
            f"SELECT CHAMBER_SEQ, NAME FROM CHAMBER_TB WHERE USE_YN = 1 ORDER BY SORT_NO, CHAMBER_SEQ"
        )
        return list(cur.fetchall())


def fetch_history(date_from: date, date_to: date, chamber_seq: Optional[int], src: str) -> List[dict]:
    """종료된 기록. 기간은 종료일 기준. history_limit + 1 건까지 읽는다."""
    dt_from = date_from.strftime("%Y-%m-%d")
    dt_to = (date_to + timedelta(days=1)).strftime("%Y-%m-%d")
    branches: List[str] = []
    params: list = []
    if src in ("", "RAW"):
        branches.append(f"""
    SELECT 'RAW' AS SRC, e.ENV_SEQ AS SEQ, e.ZONE_SEQ, e.RAW_SEQ, e.MET_KEY,
           NULL AS PURPOSE, NULL AS MEMO, e.MEM_NAME, e.TEST_S_DT AS S_DT, e.TEST_E_DT AS E_DT
      FROM RAW_ENV_TB e
     WHERE e.DEL_YN = 0 AND e.MIG_YN = 0 AND e.ZONE_SEQ IS NOT NULL
       AND e.TEST_E_DT >= %s AND e.TEST_E_DT < %s""")
        params += [dt_from, dt_to]
    if src in ("", "ETC"):
        branches.append(f"""
    SELECT 'ETC' AS SRC, x.USE_SEQ AS SEQ, x.ZONE_SEQ, NULL AS RAW_SEQ, NULL AS MET_KEY,
           x.PURPOSE, x.MEMO, x.MEM_NAME, x.START_DT AS S_DT, x.END_DT AS E_DT
      FROM CHAMBER_ETC_USE_TB x
     WHERE x.DEL_YN = 0 AND x.END_DT >= %s AND x.END_DT < %s""")
        params += [dt_from, dt_to]

    where = ""
    if chamber_seq:
        where = "WHERE c.CHAMBER_SEQ = %s"
        params.append(chamber_seq)
    params.append(settings.history_limit + 1)

    sql = f"""
SELECT u.SRC, u.SEQ, c.NAME AS CHAMBER_NAME, z.NAME AS ZONE_NAME, u.MET_KEY, u.PURPOSE, u.MEMO,
       u.MEM_NAME, u.S_DT, u.E_DT, TIMESTAMPDIFF(MINUTE, u.S_DT, u.E_DT) AS USE_MIN,
       {TEST_ID_SQL} AS TEST_ID,
       {PRDCT_SQL} AS PRDCT_NAME
  FROM ({" UNION ALL ".join(branches)}
       ) u
       INNER JOIN CHAMBER_ZONE_TB z ON z.ZONE_SEQ = u.ZONE_SEQ
       INNER JOIN CHAMBER_TB c ON c.CHAMBER_SEQ = z.CHAMBER_SEQ
       LEFT JOIN RAW_TB r ON r.RAW_SEQ = u.RAW_SEQ
       LEFT JOIN TEST_TB t ON t.TEST_SEQ = r.TEST_SEQ
 {where}
 ORDER BY u.E_DT DESC
 LIMIT %s"""
    with connect() as conn, conn.cursor() as cur:
        cur.execute(sql, params)
        return list(cur.fetchall())


# ---------------------------------------------------------------- 로데이터 외 사용

def start_etc_use(zone_seq: int, purpose: str, test_seq: Optional[int], memo: str,
                  mem_id: str, mem_name: str) -> int:
    """구역 행 잠금 → 사용 중 확인 → INSERT 를 한 트랜잭션에서 한다."""
    with connect() as conn, conn.cursor() as cur:
        cur.execute(
            f"SELECT ZONE_SEQ, MULTI_YN FROM CHAMBER_ZONE_TB WHERE ZONE_SEQ = %s AND USE_YN = 1",
            (zone_seq,),
        )
        zone = cur.fetchone()
        if not zone:
            raise Reject(400, "사용할 수 없는 구역입니다.")

        if not zone["MULTI_YN"]:
            # ERP 의 측정 [시작]도 같은 행을 잠근다. chamber-svc 는 이 행만 잠근다 (RAW_TB 는 잠그지 않는다)
            cur.execute(
                f"SELECT ZONE_SEQ FROM CHAMBER_ZONE_TB WHERE ZONE_SEQ = %s FOR UPDATE", (zone_seq,)
            )
            cur.execute(
                f"""
SELECT u.SRC, u.MET_KEY, u.PURPOSE, u.MEM_NAME, u.S_DT, {TEST_ID_SQL} AS TEST_ID
  FROM ({ACTIVE_UNION}
       ) u
       LEFT JOIN RAW_TB r ON r.RAW_SEQ = u.RAW_SEQ
       LEFT JOIN TEST_TB t ON t.TEST_SEQ = r.TEST_SEQ
 WHERE u.ZONE_SEQ = %s
 ORDER BY u.S_DT
 LIMIT 1""",
                (zone_seq,),
            )
            busy = cur.fetchone()
            if busy:
                conn.rollback()
                raise Reject(409, "이미 사용 중인 구역입니다.", busy)

        cur.execute(
            f"""
INSERT INTO CHAMBER_ETC_USE_TB
       (ZONE_SEQ, PURPOSE, TEST_SEQ, MEMO, MEM_ID, MEM_NAME, START_DT, INS_DT)
VALUES (%s, %s, %s, %s, %s, %s, NOW(), NOW())""",
            (zone_seq, purpose, test_seq, memo or None, mem_id, mem_name),
        )
        use_seq = cur.lastrowid
        conn.commit()
        return use_seq


def _own_etc_use(cur, use_seq: int, mem_id: str, is_rev: bool) -> dict:
    cur.execute(
        f"SELECT USE_SEQ, MEM_ID, END_DT FROM CHAMBER_ETC_USE_TB"
        f" WHERE USE_SEQ = %s AND DEL_YN = 0 FOR UPDATE",
        (use_seq,),
    )
    row = cur.fetchone()
    if not row:
        raise Reject(404, "기록을 찾을 수 없습니다.")
    if row["MEM_ID"] != mem_id and not is_rev:
        raise Reject(403, "기록한 사람 또는 기술책임자만 할 수 있습니다.")
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


# ---------------------------------------------------------------- 챔버 관리
# CHAMBER_TB, CHAMBER_ZONE_TB, CHAMBER_ZONE_MET_TB 는 이 화면만 쓴다. ERP 는 읽기만 한다.
# 챔버·구역은 지우지 않고 USE_YN=0 으로 숨긴다. 매핑(CHAMBER_ZONE_MET_TB)만 행을 넣고 지운다.

def fetch_admin_tree() -> dict:
    """숨긴 것까지 포함한 챔버·구역·매핑 전체와 구역별 진행 중 건수"""
    with connect() as conn, conn.cursor() as cur:
        cur.execute("SELECT CHAMBER_SEQ, NAME, SORT_NO, USE_YN FROM CHAMBER_TB ORDER BY SORT_NO, CHAMBER_SEQ")
        chambers = list(cur.fetchall())
        cur.execute(
            "SELECT ZONE_SEQ, CHAMBER_SEQ, NAME, MULTI_YN, SORT_NO, USE_YN FROM CHAMBER_ZONE_TB"
            " ORDER BY SORT_NO, ZONE_SEQ"
        )
        zones = list(cur.fetchall())
        cur.execute("SELECT ZONE_SEQ, MET_KEY FROM CHAMBER_ZONE_MET_TB")
        mets = list(cur.fetchall())
        cur.execute(f"SELECT u.ZONE_SEQ, COUNT(*) AS CNT FROM ({ACTIVE_UNION}\n) u GROUP BY u.ZONE_SEQ")
        active = {r["ZONE_SEQ"]: int(r["CNT"]) for r in cur.fetchall()}
        return {"chambers": chambers, "zones": zones, "mets": mets, "active": active}


def _active_count(cur, zone_seqs: List[int]) -> int:
    if not zone_seqs:
        return 0
    marks = ", ".join(["%s"] * len(zone_seqs))
    cur.execute(f"SELECT COUNT(*) AS CNT FROM ({ACTIVE_UNION}\n) u WHERE u.ZONE_SEQ IN ({marks})", zone_seqs)
    return int(cur.fetchone()["CNT"])


def save_chamber(chamber_seq: Optional[int], name: str, sort_no: int, use_yn: int,
                 mem_id: str, force: bool) -> int:
    with connect() as conn, conn.cursor() as cur:
        if chamber_seq is None:
            cur.execute(
                "INSERT INTO CHAMBER_TB (NAME, SORT_NO, USE_YN, INS_MEM_ID, INS_DT) VALUES (%s, %s, %s, %s, NOW())",
                (name, sort_no, use_yn, mem_id),
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
                    raise Reject(409, f"이 챔버에 사용 중인 건이 {cnt}건 있습니다. 그래도 숨길까요?", {"confirm": True})
            cur.execute(
                "UPDATE CHAMBER_TB SET NAME = %s, SORT_NO = %s, USE_YN = %s, UDT_MEM_ID = %s, UDT_DT = NOW()"
                " WHERE CHAMBER_SEQ = %s",
                (name, sort_no, use_yn, mem_id, chamber_seq),
            )
        conn.commit()
        return chamber_seq


def save_zone(zone_seq: Optional[int], chamber_seq: int, name: str, multi_yn: int, sort_no: int,
              use_yn: int, met_keys: List[str], mem_id: str, force: bool) -> int:
    """구역과 시험방법 매핑을 한 트랜잭션에서 저장한다."""
    with connect() as conn, conn.cursor() as cur:
        cur.execute("SELECT CHAMBER_SEQ FROM CHAMBER_TB WHERE CHAMBER_SEQ = %s", (chamber_seq,))
        if not cur.fetchone():
            raise Reject(400, "챔버를 찾을 수 없습니다.")
        if zone_seq is None:
            cur.execute(
                "INSERT INTO CHAMBER_ZONE_TB (CHAMBER_SEQ, NAME, MULTI_YN, SORT_NO, USE_YN, INS_MEM_ID, INS_DT)"
                " VALUES (%s, %s, %s, %s, %s, %s, NOW())",
                (chamber_seq, name, multi_yn, sort_no, use_yn, mem_id),
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
                    raise Reject(409, f"이 구역에 사용 중인 건이 {cnt}건 있습니다. 그래도 숨길까요?", {"confirm": True})
            cur.execute(
                "UPDATE CHAMBER_ZONE_TB SET CHAMBER_SEQ = %s, NAME = %s, MULTI_YN = %s, SORT_NO = %s, USE_YN = %s,"
                " UDT_MEM_ID = %s, UDT_DT = NOW() WHERE ZONE_SEQ = %s",
                (chamber_seq, name, multi_yn, sort_no, use_yn, mem_id, zone_seq),
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
SELECT ID, PASSWORD, MEM_NAME, LOCK_YN, REV_YN, FAIL_PASS_CNT,
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
