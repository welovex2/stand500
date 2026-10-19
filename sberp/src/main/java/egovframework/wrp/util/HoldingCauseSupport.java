package egovframework.wrp.util;

/**
 * 홀딩 메모로 업체 사정 여부를 가린다. 코드 구분이 없어 문구 기준으로만 본다.
 */
public final class HoldingCauseSupport {

  private static final String[] EXTERNAL_WORDS = {
      "업체", "고객", "의뢰", "취하", "공문", "환불", "요청", "시료", "미입고", "미반입",
      "구두", "협의", "자료", "미제출", "컨펌", "회신", "연락 없", "대기 요청", "홀딩 요청"
  };

  private HoldingCauseSupport() {}

  /** 업체사정 / 확인필요 / 빈값(홀딩 아님) */
  public static String classify(String testState, String memo) {
    if (testState == null || !testState.contains("홀딩")) {
      return "";
    }
    if (memo == null || memo.trim().isEmpty()) {
      return "확인필요";
    }
    String text = memo.replaceAll("\\s+", " ");
    for (int i = 0; i < EXTERNAL_WORDS.length; i++) {
      if (text.contains(EXTERNAL_WORDS[i])) {
        return "업체사정";
      }
    }
    return "확인필요";
  }

  public static String shortMemo(String memo) {
    if (memo == null) {
      return "";
    }
    String text = memo.replaceAll("\\s+", " ").trim();
    if (text.length() > 40) {
      return text.substring(0, 40) + "…";
    }
    return text;
  }
}
