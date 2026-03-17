package egovframework.cmm.code;

public enum TestStateCode {

  //@formatter:off
  DASH("0",                   "-"),
  RECEIPT("1",                "접수"),
  TESTING("2",                "시험중"),
  DEBUGGING("3",              "디버깅"),
  HOLDING("4",                "홀딩"),
  RD_DELAY("5",               "RD지연"),
  RD_WRITE_DONE("6",          "RD작성완료"),
  RD_REVIEW_DONE("7",         "RD검토완료"),
  WRITING_SCORE("8",          "성적서작성중"),
  DEBUGGING_DONE("9",         "디버깅 완료"),
  SCORE_WRITE_DONE("10",      "성적서작성완료"),
  SCORE_DR("11",              "성적서(DR)"),
  SCORE_REVIEW_DONE_WAIT("12","성적서 검토완료(대기)"),
  SCORE_ISSUE_REQUEST("13",   "성적서발급요청"),
  SCORE_ISSUE_DONE("14",      "성적서발급완료"),
  EDITING_AFTER_ISSUE("15",   "발급 후 수정중"),
  CIVIL_REQUEST("16",         "민원접수 요청중"),
  CERTIFICATE_DONE("17",      "필증완료"),
  PROJECT_DONE("18",          "프로젝트완료"),
  TEST_CANCEL("19",           "시험취소"),
  TEST_NOT_REQUIRED("20",     "시험미필요"),
  RD_REVIEW_99("21",          "RD검토99%");
  //@formatter:on

  private final String code;
  private final String desc;

  TestStateCode(String code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public String getCode() {
    return code;
  }

  public String getDesc() {
    return desc;
  }

  // code 값으로 enum 찾기
  public static TestStateCode fromCode(String code) {
    for (TestStateCode state : values()) {
      if (state.code.equals(code)) {
        return state;
      }
    }
    throw new IllegalArgumentException("Unknown TestStateCode: " + code);
  }
}
