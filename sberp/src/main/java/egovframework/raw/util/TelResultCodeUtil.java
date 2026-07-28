package egovframework.raw.util;

import egovframework.raw.dto.TelDTO;

/**
 * TEL(3078) 시험결과 코드 정규화·표시.
 * 라디오 미선택 시 프론트가 {@code null}, 필드 생략, {@code ""} 로 넘기는 경우를 DB NULL 로 통일한다.
 */
public final class TelResultCodeUtil {

  private TelResultCodeUtil() {}

  /** insert/update 직전 호출 — 빈 문자열 result 필드를 null 로 변환 */
  public static void normalizeBlankResults(TelDTO tel) {
    if (tel == null) {
      return;
    }
    tel.setResultCode(blankToNull(tel.getResultCode()));
    tel.setResultPassCode(blankToNull(tel.getResultPassCode()));
    tel.setResultResetCode(blankToNull(tel.getResultResetCode()));
    tel.setResultNoPassCode(blankToNull(tel.getResultNoPassCode()));
    tel.setResultNewPassCode(blankToNull(tel.getResultNewPassCode()));
    tel.setResultPwd3Code(blankToNull(tel.getResultPwd3Code()));
    tel.setResultPwd3Memo(blankToNull(tel.getResultPwd3Memo()));
    tel.setResultPwd2Code(blankToNull(tel.getResultPwd2Code()));
    tel.setResultPwd2Memo(blankToNull(tel.getResultPwd2Memo()));
    tel.setResultLockCntCode(blankToNull(tel.getResultLockCntCode()));
    tel.setResultLockTimeCode(blankToNull(tel.getResultLockTimeCode()));
    tel.setResultCloudCode(blankToNull(tel.getResultCloudCode()));
    tel.setResultCloudResetCode(blankToNull(tel.getResultCloudResetCode()));
    tel.setResultCloudPwd3Code(blankToNull(tel.getResultCloudPwd3Code()));
    tel.setResultCloudPwd3Memo(blankToNull(tel.getResultCloudPwd3Memo()));
    tel.setResultCloudPwd2Code(blankToNull(tel.getResultCloudPwd2Code()));
    tel.setResultCloudPwd2Memo(blankToNull(tel.getResultCloudPwd2Memo()));
    tel.setResultCloudRegCode(blankToNull(tel.getResultCloudRegCode()));
    tel.setResultCloudAccessCode(blankToNull(tel.getResultCloudAccessCode()));
    tel.setResultCloudLockCntCode(blankToNull(tel.getResultCloudLockCntCode()));
    tel.setResultCloudLockTimeCode(blankToNull(tel.getResultCloudLockTimeCode()));
  }

  /** 성적서·화면 표시용 — 1=적합, 0=부적합, 그 외(null 포함)=해당사항 없음 */
  public static String resultLabel(String code) {
    if ("1".equals(code)) {
      return "적합";
    }
    if ("0".equals(code)) {
      return "부적합";
    }
    return "해당사항 없음";
  }

  /** 1.1·1.2 시험결과가 적합/부적합으로 확정된 경우 (해당사항 없음 제외) */
  public static boolean isApplicable(String code) {
    return "1".equals(code) || "0".equals(code);
  }

  /** 3.1 시험결과 — 1.1 또는 1.2 중 하나라도 부적합 */
  public static boolean isSection3Unfit(TelDTO tel) {
    if (tel == null) {
      return false;
    }
    return "0".equals(tel.getResultCode()) || "0".equals(tel.getResultCloudCode());
  }

  /** 3.1 시험결과 — 부적합이 없고 1.1 또는 1.2 중 하나라도 적합 */
  public static boolean isSection3Fit(TelDTO tel) {
    if (tel == null || isSection3Unfit(tel)) {
      return false;
    }
    return "1".equals(tel.getResultCode()) || "1".equals(tel.getResultCloudCode());
  }

  /**
   * 3.1 페이지 열 — 1.1(직접)·1.2(클라우드) 각 시작 페이지.
   * 해당사항 없음은 제외, 둘 다 해당 시 콤마 구분.
   */
  public static String section3DetailPages(TelDTO tel, int directStartPage, int cloudStartPage) {
    if (tel == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    if (isApplicable(tel.getResultCode())) {
      sb.append(directStartPage);
    }
    if (isApplicable(tel.getResultCloudCode())) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(cloudStartPage);
    }
    return sb.toString();
  }

  static String blankToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
