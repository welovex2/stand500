"""
pdf-svc API 요청/응답 스키마.

PdfServiceClientImpl (Java) 가 보내는 JSON 필드와 1:1 대응:
  html, base_url, print_background, prefer_css_page_size
"""

from typing import Literal

from pydantic import BaseModel, Field


class PdfMargins(BaseModel):
    """page.pdf margin — 성적서는 CSS @page margin 0 이라 기본 0."""

    top: str = "0"
    right: str = "0"
    bottom: str = "0"
    left: str = "0"


class RenderPdfRequest(BaseModel):
    """POST /v1/render/pdf body."""

    html: str = Field(..., min_length=1, description="HTML 문서 전체 또는 #a4_print fragment")
    base_url: str | None = Field(
        None,
        description=(
            "HTML 안 상대경로(../css, ../img) 해석용 디렉터리 URL. "
            "예: http://host.docker.internal:8080/html/ "
            "(예: http://host:8080/report/common/css/). "
            "export 가 http://localhost:8080/css/... 절대URL이면 CSS는 base_url 무관, "
            "localhost→host.docker.internal 치환은 renderer.rewrite_erp_asset_urls 가 처리."
        ),
    )
    format: Literal["A4", "Letter", "Legal"] = "A4"
    print_background: bool = True
    prefer_css_page_size: bool = True
    landscape: bool = False
    margins: PdfMargins | None = None
    # load: networkidle 대비 빠름. 웹폰트는 document.fonts.ready 로 별도 대기.
    wait_until: Literal["load", "domcontentloaded", "networkidle", "commit"] = "load"
    timeout_ms: float | None = Field(
        None,
        description="페이지 로드 타임아웃(ms). 미설정 시 PDF_DEFAULT_TIMEOUT_MS",
    )
