"""
pdf-svc 환경변수.

로컬 Docker compose (.env):
  PDF_ERP_HOST=host.docker.internal
  PDF_ERP_PORT=8080
  PDF_INTERNAL_TOKEN=          # 비우면 토큰 검증 생략
  PDF_DEFAULT_TIMEOUT_MS=120000
"""

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    pdf_internal_token: str = ""
    pdf_default_timeout_ms: float = 120_000.0
    # rewrite_erp_asset_urls: localhost:8080 → 이 호스트 (Docker→호스트 Tomcat)
    pdf_erp_host: str = "host.docker.internal"
    pdf_erp_port: int = 8080

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")


settings = Settings()
