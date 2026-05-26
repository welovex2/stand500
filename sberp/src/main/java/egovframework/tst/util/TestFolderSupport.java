package egovframework.tst.util;

import egovframework.tst.dto.NcTestFolderContext;

public final class TestFolderSupport {

  private TestFolderSupport() {}

  public static String buildTestId(NcTestFolderContext ctx, String type, String paddedTestNo) {
    StringBuilder testId = new StringBuilder();
    testId.append("SB").append(ctx.getSbkYm()).append("-").append(ctx.getSbkType())
        .append(String.format("%04d", ctx.getSbkSeq()));
    testId.append("-").append(type).append(paddedTestNo);
    if (ctx.getSbkRevision() > 0) {
      testId.append("-").append(ctx.getSbkRevision());
    }
    return testId.toString();
  }

  public static String buildNormalTestFolderPath(String ncFolderPath, String type,
      String paddedTestNo) {
    return ncFolderPath + "/" + type + paddedTestNo;
  }

  public static String buildReissueTestFolderPath(String originalFolderPath, String testId) {
    return originalFolderPath + "/재발행 및 수정/" + testId;
  }

}
