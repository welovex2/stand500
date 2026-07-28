package egovframework.raw.report;







import egovframework.raw.dto.ReportDTO;







/**

 * classpath HTML 템플릿 → 성적서 HTML 문자열.

 *

 * <pre>

 * ReportPageModelBuilder (Java)  →  ReportDocumentModel

 * Freemarker (report_tel_3078.html) →  HTML

 * PdfServiceClient               →  PDF

 * </pre>

 */

public interface ReportHtmlTemplateService {



  /** TEL 3078 — {@code report_tel_3078.html}, {@code /report/common/} 자산 */

  String TEL_TEMPLATE = "report_tel_3078";



  /** {@link egovframework.raw.dto.ReportDTO#getTestStndrSeq()} — TEL(유선-영상정보처리기기) */

  int TEL_TEST_STNDR_SEQ = 560;



  static String resolveTemplateName(ReportDTO report, String explicitTemplate) {

    if (explicitTemplate != null && !explicitTemplate.trim().isEmpty()) {

      return explicitTemplate.trim();

    }

    return TEL_TEMPLATE;

  }



  /**

   * @param templateName 확장자 없음 (예: {@code report_tel_3078})

   * @param publicBaseUrl CSS·이미지 절대 URL prefix ({@code Globals.report.publicBaseUrl})

   */

  String render(String templateName, ReportDTO report, String publicBaseUrl) throws Exception;



  boolean templateExists(String templateName);



  String templateErrorMessage(String templateName);

}

