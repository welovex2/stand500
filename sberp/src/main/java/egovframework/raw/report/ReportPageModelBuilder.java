package egovframework.raw.report;



import egovframework.raw.dto.ReportDTO;

import egovframework.raw.report.model.ReportDocumentModel;



/**

 * DB {@link ReportDTO} → PDF용 Freemarker 모델 변환.

 *

 * <p>Freemarker 템플릿({@code report_tel_3078.html})에 {@code document} 모델을 넘긴다.

 * <b>가변 로직</b>(페이지 분할, 섹션 조립 등)은 퍼블리셔 HTML + Freemarker 에 둔다.

 *

 * <p>templateName 1개 ↔ Builder 1개.

 */

public interface ReportPageModelBuilder {



  /** Freemarker 파일명과 매칭 (확장자 제외, 예: {@code report_tel_3078}) */

  String templateName();



  /**

   * @param publicBaseUrl CSS·이미지 절대 URL prefix.

   *                      예: {@code http://host:8080} → {@code .../report/common/css/report-base.css}

   */

  ReportDocumentModel build(ReportDTO report, String publicBaseUrl);

}
