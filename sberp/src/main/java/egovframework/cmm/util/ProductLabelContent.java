package egovframework.cmm.util;

import egovframework.cmm.service.SbkInfoVO;

/**
 * 성적서 자동 제품라벨과 동일한 문구 데이터.
 * PDF 에는 비트맵이 아니라 {@link NcImagePdfRenderer} 가 텍스트로 그린다.
 */
public final class ProductLabelContent {

  private final String cmpyName;
  private final String prdctName;
  private final String modelName;
  private final String mnfctCmpny;
  private final String mnfctCntry;
  private final String authBottom;

  public ProductLabelContent(String cmpyName, String prdctName, String modelName,
      String mnfctCmpny, String mnfctCntry, String authBottom) {
    this.cmpyName = nvl(cmpyName);
    this.prdctName = nvl(prdctName);
    this.modelName = nvl(modelName);
    this.mnfctCmpny = nvl(mnfctCmpny);
    this.mnfctCntry = nvl(mnfctCntry);
    this.authBottom = nvl(authBottom);
  }

  public static ProductLabelContent from(SbkInfoVO naming) {
    return new ProductLabelContent(
        naming == null ? null : naming.getCmpyName(),
        naming == null ? null : naming.getPrdctName(),
        naming == null ? null : naming.getModelName(),
        naming == null ? null : naming.getMnfctCmpny(),
        naming == null ? null : naming.getMnfctCntry(),
        buildAuthBottom(naming));
  }

  /** 성적서 TelReportTemplateHelper.productLabelAuthText 와 동일. */
  public static String buildAuthBottom(SbkInfoVO naming) {
    if (naming == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    appendIfPresent(sb, naming.getAthntNmbr());
    appendIfPresent(sb, naming.getMdlIdntf());
    return sb.toString();
  }

  private static void appendIfPresent(StringBuilder sb, String value) {
    if (value == null) {
      return;
    }
    String t = value.trim();
    if (t.isEmpty()) {
      return;
    }
    if (sb.length() > 0) {
      sb.append(',');
    }
    sb.append(t);
  }

  private static String nvl(String s) {
    return s == null ? "" : s.trim();
  }

  public String getCmpyName() {
    return cmpyName;
  }

  public String getPrdctName() {
    return prdctName;
  }

  public String getModelName() {
    return modelName;
  }

  public String getMnfctCmpny() {
    return mnfctCmpny;
  }

  public String getMnfctCntry() {
    return mnfctCntry;
  }

  public String getAuthBottom() {
    return authBottom;
  }

  public String[] bodyLines() {
    return new String[] {
        "상호 : " + cmpyName,
        "기기명칭 : " + prdctName,
        "모델명 : " + modelName,
        "제조년월일 : 별도표기",
        "제조자 : " + mnfctCmpny,
        "제조국가 : " + mnfctCntry
    };
  }
}
