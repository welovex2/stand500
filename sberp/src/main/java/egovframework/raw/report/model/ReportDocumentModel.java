package egovframework.raw.report.model;



import lombok.Getter;

import lombok.Setter;



/** Freemarker 에 넘기는 최상위 모델 ({@code report_tel_3078.html}). */

@Getter

@Setter

public class ReportDocumentModel {



  private String cssUrl;

  private String baseUrl;



  /** 고정 img prefix — {@code /report/common} */

  private String assetBaseUrl;



  private ReportMeta meta;

  private ReportTel3078ViewModel tel;
}

