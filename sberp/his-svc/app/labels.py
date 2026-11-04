"""DB 코드값 → 화면 표시 이름.

열마다 기준이 하나다.
  종류 : 무엇이 남았나 — 저장(RAW_DONE_EDIT_TB) / 값 변경(RAW_HIS_TB)
  대상 : 어디를       — 저장한 화면 / 바뀐 항목
  시점 : 언제         — 프로젝트완료 전 / 후
"""
import re

TYPE_SAVE = "저장"
TYPE_CHANGE = "값 변경"
TIMING_AFTER = "완료 후"
TIMING_BEFORE = "완료 전"

# 저장 대상: RAW_DONE_EDIT_TB.TB → 저장한 화면
SAVE_TARGET = {
    "RAW_TB": "기본정보",
    "METHOD_CE_TB": "CE",
    "METHOD_RE_TB": "RE",
    "METHOD_ESD_TB": "ESD",
    "METHOD_RS_TB": "RS",
    "METHOD_EFT_TB": "EFT/BURST",
    "METHOD_SURGE_TB": "SURGE",
    "METHOD_CS_TB": "CS",
    "METHOD_MF_TB": "M-Field",
    "METHOD_VDIP_TB": "V-Dip",
    "METHOD_CK_TB": "Click",
    "METHOD_DP_TB": "DP",
    "METHOD_TEL_TB": "TEL",
    "METHOD_IMG_TB": "사진",
    "FILE_RAW_TB": "첨부 로데이터",
}
SAVE_ACT = {"INSERT": "첫 저장", "UPDATE": "재저장"}

# 값 변경 대상: 필터 값 → RAW_HIS_TB.TB
CHANGE_TARGET = {
    "period": ("RAW_TB", "시험기간 (시작일·완료일)"),
    "method": ("RAW_MET_TB", "시험방법 (체크·규격)"),
}

HIS_COL = {
    "TEST_S_DT": "시험시작일",
    "TEST_E_DT": "시험완료일",
}

# RAW_MET_TB COL 예) 'CHECK_YN:6 ESD', 'TYPE:6 ESD'
_MET_COL = re.compile(r"^(CHECK_YN|TYPE):\d+\s*(.*)$")


def row_type(src: str) -> str:
    return TYPE_SAVE if src == "DONE" else TYPE_CHANGE


def target(src: str, tb: str, col: str, act: str) -> str:
    if src == "DONE":
        return "%s (%s)" % (SAVE_TARGET.get(tb, tb or ""), SAVE_ACT.get(act, act or ""))
    col = col or ""
    m = _MET_COL.match(col)
    if m:
        return "%s %s" % (m.group(2), "체크" if m.group(1) == "CHECK_YN" else "규격")
    return HIS_COL.get(col, col)


def timing(after_done: bool) -> str:
    return TIMING_AFTER if after_done else TIMING_BEFORE


def value(tb: str, col: str, v):
    if v is None:
        return ""
    v = str(v)
    if tb == "RAW_MET_TB" and (col or "").startswith("CHECK_YN:"):
        return {"1": "체크", "0": "해제"}.get(v, v)
    return v
