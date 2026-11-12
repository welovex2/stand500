from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    # 전용 계정(chamber_svc)으로 접속한다. erp 는 읽기, ops 는 쓰기. 권한은 sql/chamber_svc_user.sql
    db_host: str = "127.0.0.1"
    db_port: int = 3306
    db_user: str = "chamber_svc"
    db_password: str = ""
    erp_schema: str = "erp"
    ops_schema: str = "ops"
    # 계정의 MAX_USER_CONNECTIONS(5) 보다 작게 둔다
    db_max_conn: int = 4

    # 세션 쿠키 서명 키. 32자 이상. 바꾸면 모든 로그인이 풀린다
    session_secret: str = ""
    session_hours: int = 1
    monitor_mem_id: str = ""        # 모니터 전용 계정 (조회만, 7일 유지)
    monitor_days: int = 7
    cookie_path: str = "/chamber/"  # 로컬에서 직접 띄울 때는 "/"
    cookie_secure: bool = True      # 로컬(http)에서는 false

    login_fail_max: int = 5
    login_block_min: int = 10

    stale_hours: int = 12           # 이 시간을 넘긴 사용 건은 현황판에서 강조
    history_limit: int = 500

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")


settings = Settings()

# 빈 값이면 기동 단계에서 바로 실패시킨다
if not settings.db_password.strip():
    raise RuntimeError("환경변수 DB_PASSWORD 가 설정되지 않았거나 비어 있습니다.")
if len(settings.session_secret.strip()) < 32:
    raise RuntimeError("환경변수 SESSION_SECRET 은 32자 이상이어야 합니다.")
for _name in (settings.erp_schema, settings.ops_schema):
    if not _name.isidentifier():
        raise RuntimeError("스키마 이름이 올바르지 않습니다: " + _name)

# 로데이터 외 사용 용도 (ERP 가 읽는 ops.CHAMBER_ETC_USE_TB.PURPOSE 값. 바꿀 때는 ERP 쪽 라벨도 같이)
PURPOSES = {
    "PRE": "예비시험",
    "DEBUG": "고객 디버깅",
    "CHECK": "점검",
    "WITNESS": "입회",
    "ETC": "기타",
}
