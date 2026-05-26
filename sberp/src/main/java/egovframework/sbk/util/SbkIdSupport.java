package egovframework.sbk.util;

public final class SbkIdSupport {

  private SbkIdSupport() {}

  /**
   * 재발행 신청서번호 여부 (예: SB25-G1578-1)
   */
  public static boolean isReissueSbkId(String sbkId) {
    return sbkId != null && sbkId.length() > 10 && sbkId.charAt(10) == '-';
  }

  /**
   * 재발행 신청서번호에서 원본 신청서번호 추출 (예: SB25-G1578-1 → SB25-G1578)
   */
  public static String toOriginalSbkId(String reissueSbkId) {
    if (!isReissueSbkId(reissueSbkId)) {
      return reissueSbkId;
    }
    return reissueSbkId.substring(0, 10);
  }

}
