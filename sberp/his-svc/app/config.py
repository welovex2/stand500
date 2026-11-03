from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    # 읽기 전용 계정(his_ro)으로 접속한다. 권한은 README 의 GRANT 참고
    db_host: str = "127.0.0.1"
    db_port: int = 3306
    db_name: str = "erp"
    db_user: str = "his_ro"
    db_password: str = ""

    page_size: int = 50
    csv_max_rows: int = 10000

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")


settings = Settings()
