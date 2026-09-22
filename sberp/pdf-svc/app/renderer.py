"""
Playwright Chromium 으로 HTML 을 로드하고 page.pdf() 로 PDF 생성.

ERP 성적서 특화 처리:
  - 폰트: ERP report-fonts-pdf.css HTTP fetch (Gulim-subset ~1MB + draft)
  - localhost → host.docker.internal (Docker 에서 Tomcat CSS fetch)
  - <base href> (상대경로 ../css)
"""

import logging
from typing import Any

from playwright.async_api import Browser, Playwright, async_playwright

from .config import settings
from .schemas import PdfMargins, RenderPdfRequest

log = logging.getLogger("pdf-svc")

# 앱 수명 동안 유지 — 요청마다 launch 하면 느림
_playwright: Playwright | None = None
_browser: Browser | None = None


async def start_browser() -> None:
    """FastAPI lifespan — Chromium 1회 기동."""
    global _playwright, _browser
    if _browser is not None:
        return
    log.info("Starting Playwright Chromium")
    _playwright = await async_playwright().start()
    _browser = await _playwright.chromium.launch(
        args=["--font-render-hinting=none"],
    )


async def stop_browser() -> None:
    global _playwright, _browser
    if _browser is not None:
        await _browser.close()
        _browser = None
    if _playwright is not None:
        await _playwright.stop()
        _playwright = None
    log.info("Playwright Chromium stopped")


def ensure_html_document(html: str) -> str:
    """
    #a4_print fragment 만 넘어와도 Chromium 이 파싱할 수 있게 최소 문서로 감싼다.
    """
    stripped = html.lstrip().lower()
    if stripped.startswith("<!doctype") or stripped.startswith("<html"):
        return html
    return (
        "<!DOCTYPE html>"
        '<html lang="ko"><head><meta charset="utf-8"></head>'
        "<body>"
        f"{html}"
        "</body></html>"
    )


def rewrite_erp_asset_urls(html: str) -> str:
    """
    브라우저 export HTML 은 CSS/link 가 http://localhost:8080/... 으로 고정되는 경우가 많음.
    pdf-svc 컨테이너 안의 localhost 는 Tomcat 이 아니므로 host.docker.internal 로 치환.

    설정: PDF_ERP_HOST, PDF_ERP_PORT (config.py)
    """
    host = settings.pdf_erp_host.strip()
    port = settings.pdf_erp_port
    if not host:
        return html

    replacement = f"http://{host}:{port}"
    for local in ("localhost", "127.0.0.1"):
        html = html.replace(f"http://{local}:{port}/", f"{replacement}/")
        html = html.replace(f"https://{local}:{port}/", f"{replacement}/")
    return html


def inject_base_href(html: str, base_url: str) -> str:
    """
    HTML 안 상대경로(../css, ../img) 해석 기준.
    Playwright set_content() 는 url 인자가 없어 <base href> 로 대체.

    base_url 예: http://host.docker.internal:8080/html/  (디렉터리, 파일명 X)
    """
    base = base_url.strip()
    if not base:
        return html
    if not base.endswith("/"):
        base += "/"
    base_tag = f'<base href="{base}">'

    lower = html.lower()
    head_idx = lower.find("<head")
    if head_idx >= 0:
        close = lower.find(">", head_idx)
        if close >= 0:
            return html[: close + 1] + base_tag + html[close + 1 :]

    html_idx = lower.find("<html")
    if html_idx >= 0:
        close = lower.find(">", html_idx)
        if close >= 0:
            return html[: close + 1] + "<head>" + base_tag + "</head>" + html[close + 1 :]

    return base_tag + html


def margin_dict(margins: PdfMargins | None) -> dict[str, str]:
    m = margins or PdfMargins()
    return {"top": m.top, "right": m.right, "bottom": m.bottom, "left": m.left}


async def render_pdf(req: RenderPdfRequest) -> bytes:
    """
    HTML 전처리 → Playwright 로드 → PDF 바이트.

    wait_until=networkidle: CSS·폰트·이미지 fetch 완료 대기 (ERP report_fonts.css).
    prefer_css_page_size: @page { size: A4 } 반영.
    """
    if _browser is None:
        raise RuntimeError("browser not started")

    # --- HTML 전처리 (순서 중요) ---
    html = ensure_html_document(req.html)
    html = rewrite_erp_asset_urls(html)
    if req.base_url:
        html = inject_base_href(html, req.base_url)
    timeout = req.timeout_ms if req.timeout_ms is not None else settings.pdf_default_timeout_ms

    # --- Chromium: 페이지 1장 생성 → PDF → 페이지 닫기 ---
    page = await _browser.new_page()
    try:
        await page.set_content(
            html,
            wait_until=req.wait_until,
            timeout=timeout,
        )
        # 웹폰트 로드 완료 대기 (report-fonts-pdf.css Gulim-subset·draft)
        await page.evaluate("() => document.fonts.ready")
        pdf_options: dict[str, Any] = {
            "format": req.format,
            "print_background": req.print_background,
            "prefer_css_page_size": req.prefer_css_page_size,
            "landscape": req.landscape,
            "margin": margin_dict(req.margins),
        }
        return await page.pdf(**pdf_options)
    finally:
        await page.close()
