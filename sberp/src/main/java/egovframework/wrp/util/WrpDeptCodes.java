package egovframework.wrp.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 주간보고와 동일한 시험부 펼침. EM/MD 는 부서+출장, RS/SF 는 하위 부서 합산.
 */
public final class WrpDeptCodes {

  private WrpDeptCodes() {}

  public static List<String> expand(String testTypeCode) {
    if (testTypeCode == null || testTypeCode.isEmpty()) {
      return Collections.emptyList();
    }
    switch (testTypeCode) {
      case "EM":
        return Arrays.asList("EM", "EMC");
      case "MD":
        return Arrays.asList("MD", "MDC");
      case "RS":
        return Arrays.asList("RF", "SR");
      case "SF":
        return Arrays.asList("SF", "NS");
      default:
        return Collections.singletonList(testTypeCode);
    }
  }

  public static boolean needsTotal(String testTypeCode) {
    return "RS".equals(testTypeCode) || "SF".equals(testTypeCode);
  }
}
