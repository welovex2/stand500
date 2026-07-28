package egovframework.raw.report;

import egovframework.raw.report.model.ReportDocumentModel;

/**
 * 성적서 PDF/HTML 공통 정적 자산 경로 ({@code webapp/report/common/}).
 *
 * <p>CSS·고정 img 는 양식별이 아니라 common 에 두고, Freemarker HTML 만 양식별로 분리한다.
 */
public final class ReportCommonPaths {

  /** Tomcat ROOT 기준 — {@code src/main/webapp/report/common/} */
  public static final String PREFIX = "/report/common";

  private ReportCommonPaths() {}

  public static String assetBase(String baseUrl) {
    return trimSlash(baseUrl) + PREFIX;
  }

  public static String css(String baseUrl, String fileName) {
    return assetBase(baseUrl) + "/css/" + fileName;
  }

  public static String img(String baseUrl, String fileName) {
    return assetBase(baseUrl) + "/img/" + fileName;
  }

  /** {@code report_tel.css} — TEL 성적서 전용 (퍼블리셔) */
  public static String reportTelCss(String baseUrl) {
    return css(baseUrl, "report_tel.css");
  }

  /** Playwright PDF 전용 override */
  public static String reportPdfCss(String baseUrl) {
    return css(baseUrl, "report-pdf.css");
  }

  /** PDF @font-face (Gulim·draft). HTML 미리보기용 — pdf-svc 는 인라인 embed */
  public static String reportFontsPdfCss(String baseUrl) {
    return css(baseUrl, "report-fonts-pdf.css");
  }

  /**
   * 앱 절대 URL — {@code /api/file/...} 등 context-path 포함 경로를 publicBaseUrl 과 합친다.
   *
   * <p>예: base {@code http://localhost:8080/api} + path {@code /api/file/reportImage.do?...}
   */
  public static String absoluteAppUrl(String appBaseUrl, String path) {
    if (path == null || path.trim().isEmpty()) {
      return "";
    }
    String p = path.trim();
    if (p.startsWith("http://") || p.startsWith("https://") || p.startsWith("data:")) {
      return p;
    }
    String base = trimSlash(appBaseUrl);
    if (!p.startsWith("/")) {
      p = "/" + p;
    }
    if (p.startsWith("/api/") && base.endsWith("/api")) {
      return base + p.substring(4);
    }
    return base + p;
  }

  public static void applyTo(ReportDocumentModel doc, String publicBaseUrl) {
    String base = trimSlash(publicBaseUrl);
    doc.setBaseUrl(base);
    doc.setAssetBaseUrl(assetBase(base));
    doc.setCssUrl(reportTelCss(base));
  }

  private static String trimSlash(String baseUrl) {
    if (baseUrl == null || baseUrl.isEmpty()) {
      return "http://localhost:8080/api";
    }
    String s = baseUrl.trim();
    while (s.endsWith("/")) {
      s = s.substring(0, s.length() - 1);
    }
    return s;
  }
}
