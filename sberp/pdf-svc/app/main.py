"""
pdf-svc — HTML → PDF (Playwright Headless Chromium)

Tomcat(ERP) 이 POST /v1/render/pdf 로 HTML 을 내면 PDF 바이트를 반환한다.

처리 흐름 (renderer.render_pdf):
  1. fragment → 완전한 HTML 문서로 감싸기
  2. localhost URL → host.docker.internal 치환 (Docker→호스트 Tomcat CSS)
  3. base_url → <base href> 주입 (상대경로 CSS)
  4. page.set_content → document.fonts.ready → page.pdf()
     (폰트는 ERP report_fonts.css HTTP fetch)
"""

import logging
from contextlib import asynccontextmanager

from fastapi import Depends, FastAPI, Header, HTTPException
from fastapi.responses import Response

from .config import settings
from .renderer import render_pdf, start_browser, stop_browser
from .schemas import RenderPdfRequest

logging.basicConfig(level=logging.INFO)
log = logging.getLogger("pdf-svc")


@asynccontextmanager
async def lifespan(app: FastAPI):
    """앱 기동 시 Chromium 1회 실행, 종료 시 정리 (요청마다 브라우저 재기동하지 않음)."""
    await start_browser()
    yield
    await stop_browser()


app = FastAPI(title="sberp pdf-svc", version="0.1.0", lifespan=lifespan)


def verify_token(x_internal_token: str = Header(default="")) -> None:
    """
    컨테이너 간 공유 시크릿 (PDF_INTERNAL_TOKEN ↔ Globals.pdf.token).
    토큰 미설정 시 검증 생략 — 로컬 PoC 편의.
    """
    if settings.pdf_internal_token and x_internal_token != settings.pdf_internal_token:
        raise HTTPException(status_code=401, detail="invalid internal token")


@app.get("/health")
def health() -> dict:
    """상태 확인 — erpHostRewrite 로 CSS·폰트 fetch 대상 점검."""
    return {
        "ok": True,
        "service": "pdf-svc",
        "tokenConfigured": bool(settings.pdf_internal_token),
        "fontsSource": "erp-report-fonts-css",
        "erpHostRewrite": f"http://{settings.pdf_erp_host}:{settings.pdf_erp_port}/",
    }


@app.post(
    "/v1/render/pdf",
    response_class=Response,
    dependencies=[Depends(verify_token)],
    responses={200: {"content": {"application/pdf": {}}}},
)
async def render_pdf_endpoint(req: RenderPdfRequest) -> Response:
    """
    HTML → PDF.

    요청: RenderPdfRequest (schemas.py) — PdfServiceClientImpl 과 동일 JSON.
    응답: application/pdf raw bytes.
    """
    try:
        pdf_bytes = await render_pdf(req)
    except Exception as e:
        log.exception("PDF render failed")
        raise HTTPException(status_code=502, detail=str(e)) from e

    if not pdf_bytes:
        raise HTTPException(status_code=502, detail="empty pdf")

    return Response(
        content=pdf_bytes,
        media_type="application/pdf",
        headers={"Content-Disposition": 'inline; filename="report.pdf"'},
    )
