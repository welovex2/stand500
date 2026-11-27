from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    # 전용 계정(chamber_svc)으로 접속한다. 쓰는 곳은 CHAMBER_ETC_USE_TB, 챔버 관리 테이블 3개,
    # MEMBER_TB 의 로그인 실패 컬럼(FAIL_PASS_CNT, LOCK_YN) 뿐이다.
    # 권한은 sql/chamber_svc_user.sql
    db_host: str = "127.0.0.1"
    db_port: int = 3306
    db_user: str = "chamber_svc"
    db_password: str = ""
    db_name: str = "erp"
    # 계정의 MAX_USER_CONNECTIONS(5) 보다 작게 둔다
    db_max_conn: int = 4

    # 세션 쿠키 서명 키. 32자 이상. 바꾸면 모든 로그인이 풀린다
    session_secret: str = ""
    session_hours: int = 1
    monitor_mem_id: str = ""        # 모니터 전용 계정 (조회만, 7일 유지)
    monitor_days: int = 7
    # 모니터 전용 주소: …/monitor?key=<이 값> 으로 열면 로그인 없이 조회 전용 모니터 화면만 연다.
    # 일반 로그인 쿠키와 따로 저장하므로 그 PC 에서 누가 로그인·로그아웃해도 모니터 화면은 그대로다.
    # 비워 두면 이 기능은 꺼진다. 값을 바꾸면 이미 열려 있는 모니터도 끊긴다 (다시 새 주소로 열어야 함)
    monitor_key: str = ""
    cookie_path: str = "/chamber/"  # 로컬에서 직접 띄울 때는 "/"
    cookie_secure: bool = True      # 로컬(http)에서는 false

    # 로그인 실패 한도. ERP(LoginController)와 같은 값이어야 한다 — 같은 MEMBER_TB.FAIL_PASS_CNT 를 센다
    login_fail_max: int = 10

    stale_hours: int = 10           # 이 시간을 넘긴 사용 건은 현황판에서 강조 (10시간)
    history_limit: int = 500

    # 로그인할 수 있는 부서 (MEMBER_TB.DEPT_SEQ). EMC 부서 = 5.
    # 이 부서 + AUTH_CODE P04 → 기술책임자, 이 부서 → 일반 사용자, AUTH_CODE P01·P02 → 관리자 (부서 상관없음)
    emc_dept_seq: int = 5
    # 시험번호 목록에서 뺄 시험규격 (TEST_ITEM_TB.TEST_STNDR_SEQ). ERP 로데이터 화면이 있는(자동화된) 규격이라
    # 측정 기록을 ERP 로데이터에서 남긴다. 프론트 test.js 의 로데이터 화면 분기와 같은 목록. 쉼표로 구분
    rd_auto_stndr: str = "10,571,7,686,774,775,560,14"
    # 로데이터 연동 없는 시험 항목. 구역 이름(CHAMBER_ZONE_TB.NAME)이 이 중 하나면
    # 시험번호 없이 현황판에서 시작·종료 시간만 기록한다. 쉼표로 구분
    no_rd_zones: str = "CTI,4-39"
    # 사용 이력에서 시험번호를 누르면 여는 ERP 로데이터 화면 (ERP 프론트 주소. 같은 도메인이면 "/")
    erp_web_base: str = "/"
    # 시험번호 목록에 나오는 시험 상태 (TEST_TB.TEST_STATE_CODE, 공통코드 TS). 1 접수 / 2 시험중 / 3 디버깅
    test_states: str = "1,2,3"

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")


settings = Settings()

# 빈 값이면 기동 단계에서 바로 실패시킨다
if not settings.db_password.strip():
    raise RuntimeError("환경변수 DB_PASSWORD 가 설정되지 않았거나 비어 있습니다.")
if len(settings.session_secret.strip()) < 32:
    raise RuntimeError("환경변수 SESSION_SECRET 은 32자 이상이어야 합니다.")

RD_AUTO_STNDR = sorted({int(x) for x in settings.rd_auto_stndr.split(",") if x.strip()})
TEST_STATES = sorted({int(x) for x in settings.test_states.split(",") if x.strip()})
# 공통코드 TS 이름 (목록 표시용. 개발 ERP /cmm/comcode/list.do?code=TS 에서 확인, 2026-11-19)
TEST_STATE_NAMES = {1: "접수", 2: "시험중", 3: "디버깅", 4: "홀딩", 5: "RD지연"}

# 권한 (MEMBER_TB.AUTH_CODE)
ADMIN_AUTH_CODES = ("P01", "P02")   # 관리자 — 부서 상관없이 들어오고, 기술책임자와 같은 일 + 관리 탭 · 기록 수정
REV_AUTH_CODE = "P04"               # EMC 부서 + P04 = 기술책임자

# 현황판 사용 용도 (ERP 가 읽는 CHAMBER_ETC_USE_TB.PURPOSE 값. 바꿀 때는 ERP 쪽 라벨도 같이)
# TEST = 시험번호를 골라 시작한 시험. 나머지는 "시험번호 없이 사용"의 사유
PURPOSE_TEST = "TEST"
# 사유 순서: 사전 점검 / 장비 점검 / 디버깅 / 기타 (2026-11-20)
PURPOSES = {
    PURPOSE_TEST: "시험",
    "PRE": "사전 점검",
    "CHECK": "장비 점검",
    "DEBUG": "디버깅",
    "ETC": "기타",
}
# 로데이터 연동 없는 시험 (CTI · 4-39). 시험번호 없이 사용(사유)과 다르다 — 시험번호 미연결 건수에 넣지 않는다
PURPOSE_NO_RD = "NORD"
PURPOSES[PURPOSE_NO_RD] = "로데이터 연동 없음"


# 시험규격(TEST_ITEM_TB.TEST_STNDR_SEQ) → ERP 로데이터 화면 계열 (폴더, 파일 뒤붙임).
# ERP 프론트 js/test.js 의 로데이터 화면 분기와 같다
ERP_RAW_FAMILY = {10: ("html/", ""), 571: ("html/", ""),
                  7: ("html/", "_9814"), 686: ("html/", "_9814"), 774: ("html/", "_9814"), 775: ("html/", "_9814"),
                  560: ("html/", "_tel"), 14: ("html/3124/", "")}
# 시험방법 키 → 로데이터 항목 화면 (raw_update_{항목}{뒤붙임}.html, ERP 프론트 raw_basic*.js 의 data-tm 값).
# 계열에 그 항목 화면이 없으면 로데이터 기본 정보 화면(raw_update{뒤붙임}.html)으로 간다
_RAW_TAB = {"CE": "CE", "RE1": "RE", "RE2": "RE", "RE3": "RE", "ESD": "ED", "RS": "RS", "EFT": "ET",
            "SURGE": "SG", "CS": "CS", "MF": "MF", "VDIP": "VD"}
ERP_RAW_TABS = {
    "": _RAW_TAB,
    "_9814": dict(_RAW_TAB, RE1="REA", RE2="REB", RE3="REC", CLK="CL", DP="DP"),   # RE 대역별 화면
    "_tel": {"TEL": "TEL"},
}
ERP_RAW_TABS_3124 = {k: v for k, v in _RAW_TAB.items() if v != "MF"}           # 3124 에는 M-Field 화면이 없다


def erp_raw_page(stndr_seq, met_key):
    """(화면 경로, type 값). 시험규격에 로데이터 화면이 없으면 None"""
    fam = ERP_RAW_FAMILY.get(int(stndr_seq)) if stndr_seq is not None else None
    if not fam:
        return None
    folder, suffix = fam
    tabs = ERP_RAW_TABS_3124 if folder == "html/3124/" else ERP_RAW_TABS[suffix]
    tab = tabs.get((met_key or "").upper())
    if tab:
        return f"{folder}raw_update_{tab}{suffix}.html", tab
    return f"{folder}raw_update{suffix}.html", "update"


def is_no_rd_zone(zone_name) -> bool:
    return (zone_name or "").strip().upper() in {
        x.strip().upper() for x in settings.no_rd_zones.split(",") if x.strip()}

# 구역별 시험방법 매핑(CHAMBER_ZONE_MET_TB.MET_KEY)에 쓰는 키. RE 가 아니면 ERP EnvMetKey 와 같은 값.
# RE 는 시험방법(RE1~RE3) 대신 대역으로 매핑한다 (2026-11-25, ERP RawEnvServiceImpl.zoneMetKey):
#   RA = RE (Below), RB = RE (Above). 9814 는 RE1·RE2 → RA, RE3 → RB / 그 외 규격은 RE1 → RA, RE2 → RB
#   데이터 전환 : ERP src/main/resources/db/CHAMBER_ZONE_MET_RE_band.sql
MET_KEYS = ["CE", "RA", "RB", "ESD", "RS", "EFT", "SURGE", "CS", "MF", "VDIP", "CLK", "DP", "TEL"]
