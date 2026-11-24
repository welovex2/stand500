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
    cookie_path: str = "/chamber/"  # 로컬에서 직접 띄울 때는 "/"
    cookie_secure: bool = True      # 로컬(http)에서는 false

    # 로그인 실패 한도. ERP(LoginController)와 같은 값이어야 한다 — 같은 MEMBER_TB.FAIL_PASS_CNT 를 센다
    login_fail_max: int = 10

    stale_hours: int = 10           # 이 시간을 넘긴 사용 건은 현황판에서 강조 (기획 v7: 10시간)
    history_limit: int = 500

    # 로그인할 수 있는 부서 (MEMBER_TB.DEPT_SEQ). EMC 부서 = 5.
    # 이 부서 + AUTH_CODE P04 → 기술책임자, 이 부서 → 일반 사용자, AUTH_CODE P01·P02 → 관리자 (부서 상관없음)
    emc_dept_seq: int = 5
    # 시험번호 목록에서 뺄 시험규격 (TEST_ITEM_TB.TEST_STNDR_SEQ). ERP 로데이터 화면이 있는(자동화된) 규격이라
    # 측정 기록을 ERP 로데이터에서 남긴다. 프론트 test.js 의 로데이터 화면 분기와 같은 목록. 쉼표로 구분
    rd_auto_stndr: str = "10,571,7,686,774,775,560,14"
    # 로데이터 연동 없는 시험 항목 (기획 SC-08). 구역 이름(CHAMBER_ZONE_TB.NAME)이 이 중 하나면
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
# 사유는 기획 v7_1 SC-09 순서: 사전 점검 / 장비 점검 / 디버깅 / 기타 (2026-11-20)
PURPOSES = {
    PURPOSE_TEST: "시험",
    "PRE": "사전 점검",
    "CHECK": "장비 점검",
    "DEBUG": "디버깅",
    "ETC": "기타",
    "WITNESS": "입회",     # 예전 사유. 이미 저장된 기록 표시용, 새로 고를 수 없다 (고객 입회는 시작 팝업의 체크박스)
}
PURPOSES_RETIRED = {"WITNESS"}
# 로데이터 연동 없는 시험 (CTI · 4-39). 시험번호 없이 사용(사유)과 다르다 — 시험번호 미연결 건수에 넣지 않는다
PURPOSE_NO_RD = "NORD"
PURPOSES[PURPOSE_NO_RD] = "로데이터 연동 없음"


# 시험규격(TEST_ITEM_TB.TEST_STNDR_SEQ) → ERP 로데이터 수정 화면. ERP 프론트 js/test.js 의 로데이터 화면 분기와 같다
ERP_RAW_PAGES = {10: "html/raw_update.html", 571: "html/raw_update.html",
                 7: "html/raw_update_9814.html", 686: "html/raw_update_9814.html",
                 774: "html/raw_update_9814.html", 775: "html/raw_update_9814.html",
                 560: "html/raw_update_tel.html", 14: "html/3124/raw_update.html"}


def is_no_rd_zone(zone_name) -> bool:
    return (zone_name or "").strip().upper() in {
        x.strip().upper() for x in settings.no_rd_zones.split(",") if x.strip()}

# 시험방법 키 (ERP EnvMetKey 와 같은 값. 구역별 시험방법 매핑에 쓴다)
MET_KEYS = ["CE", "RE1", "RE2", "RE3", "ESD", "RS", "EFT", "SURGE", "CS", "MF", "VDIP", "CLK", "DP", "TEL"]
