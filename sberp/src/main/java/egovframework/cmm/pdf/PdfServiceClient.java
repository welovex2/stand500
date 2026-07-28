package egovframework.cmm.pdf;

/**
 * Tomcat(ERP) → pdf-svc(Playwright) HTTP 호출 인터페이스.
 *
 * <p>성적서 PDF는 브라우저 인쇄 대신 <b>Headless Chromium</b>으로 HTML을 그려 PDF 바이트를 만든다.
 * 실제 렌더링은 {@code sberp/pdf-svc} 마이크로서비스가 담당하고, ERP는 HTML 문자열만 넘긴다.
 *
 * <pre>
 * [RepController]  Freemarker 템플릿 → HTML
 *       ↓
 * [PdfServiceClientImpl]  POST {Globals.pdf.baseUrl}/v1/render/pdf
 *       ↓
 * [pdf-svc]  Playwright page.pdf()
 *       ↓
 * byte[]  → 브라우저 다운로드 응답
 * </pre>
 *
 * <p>설정: {@code Globals.pdf.baseUrl}, {@code Globals.pdf.token}, {@code Globals.pdf.timeoutMs}
 * (context-properties.xml / erp-local.properties)
 */
public interface PdfServiceClient {

  /**
   * HTML → PDF 바이너리.
   *
   * @param html    완전한 HTML 문서 또는 {@code #a4_print} fragment
   * @param baseUrl HTML 안 <b>상대 경로</b>({@code ../css/...}) 해석용 디렉터리 URL.
   *                {@code null}이면 pdf-svc가 base_url 없이 처리.
   *                서버 템플릿은 CSS를 {@code http://host/css/...} 절대 URL로 넣으므로 보통 {@code null}.
   * @return PDF raw bytes ({@code application/pdf})
   */
  byte[] renderPdf(String html, String baseUrl) throws Exception;
}
