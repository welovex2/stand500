"""DB 코드값 → 화면 표시 이름."""
import re

KIND = {
    "RAW_TB": "시험기간",
    "RAW_MET_TB": "시험방법",
    "TEST_TB": "성적서 발급일",
}
KIND_DONE = "완료 후 저장"

# RAW_DONE_EDIT_TB.TB → 저장 화면
DONE_TB = {
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
DONE_ACT = {"INSERT": "첫 저장", "UPDATE": "재저장"}

HIS_COL = {
    "TEST_S_DT": "시험시작일",
    "TEST_E_DT": "시험완료일",
    "REPORT_DT": "성적서 발급일",
    "TEST_REPORT_DT": "첨부 로데이터 발급일",
}

# RAW_MET_TB COL 예) 'CHECK_YN:6 ESD', 'TYPE:6 ESD'
_MET_COL = re.compile(r"^(CHECK_YN|TYPE):\d+\s*(.*)$")


def kind(src: str, tb: str) -> str:
    return KIND_DONE if src == "DONE" else KIND.get(tb, tb or "")


def item(src: str, tb: str, col: str, act: str) -> str:
    if src == "DONE":
        return "%s %s" % (DONE_TB.get(tb, tb or ""), DONE_ACT.get(act, act or ""))
    col = col or ""
    m = _MET_COL.match(col)
    if m:
        return "%s %s" % (m.group(2), "체크" if m.group(1) == "CHECK_YN" else "규격")
    # TEST_TB 는 'REPORT_DT:{TEST_SEQ}'
    return HIS_COL.get(col.split(":")[0], col)


def value(tb: str, col: str, v):
    if v is None:
        return ""
    v = str(v)
    if tb == "RAW_MET_TB" and (col or "").startswith("CHECK_YN:"):
        return {"1": "체크", "0": "해제"}.get(v, v)
    return v
