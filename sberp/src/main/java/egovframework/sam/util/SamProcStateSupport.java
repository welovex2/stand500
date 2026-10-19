package egovframework.sam.util;

public final class SamProcStateSupport {

  private SamProcStateSupport() {}

  public static String purposeName(String purposeCode) {
    if (purposeCode == null) {
      return "";
    }
    switch (purposeCode.toUpperCase()) {
      case "IN":
        return "입고";
      case "TEST":
        return "시험";
      case "WAIT":
        return "반출대기";
      case "KEEP":
        return "보관";
      case "LONG":
        return "장기보관";
      case "TEMP":
        return "임시";
      case "BATT":
        return "배터리";
      case "MED":
        return "의료";
      case "SETUP":
        return "셋업";
      case "PERIPH":
        return "주변기기";
      default:
        return purposeCode;
    }
  }

  public static String procName(String procState) {
    if (procState == null) {
      return "";
    }
    switch (procState.toUpperCase()) {
      case "IN":
        return "입고";
      case "TEST":
        return "시험중";
      case "WAIT":
        return "반출대기";
      case "KEEP":
        return "보관";
      case "OUT":
        return "반출";
      default:
        return procState;
    }
  }

  public static String procStateForMove(String purposeCode, boolean occupied) {
    if (occupied) {
      return "TEST";
    }
    if (purposeCode == null) {
      return "IN";
    }
    switch (purposeCode.toUpperCase()) {
      case "WAIT":
        return "WAIT";
      case "KEEP":
      case "LONG":
      case "BATT":
      case "MED":
        return "KEEP";
      case "TEST":
      case "SETUP":
        return "TEST";
      default:
        return "IN";
    }
  }

  public static String normalizeTestType(String testTypeCode) {
    if (testTypeCode == null) {
      return null;
    }
    String n = testTypeCode.trim().toUpperCase();
    return n.isEmpty() ? null : n;
  }

  public static boolean isValidTestType(String testTypeCode) {
    String n = normalizeTestType(testTypeCode);
    return n != null && n.matches("[A-Z]{2,4}");
  }
}
