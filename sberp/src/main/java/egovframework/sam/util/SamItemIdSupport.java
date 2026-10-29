package egovframework.sam.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SamItemIdSupport {

  private static final Pattern IM_SUB_ID =
      Pattern.compile("^(SB\\d{2}-[A-Z]\\d+(?:-\\d+)?-M\\d{2})$", Pattern.CASE_INSENSITIVE);

  private static final Pattern IM_SUB_ID_PARTS =
      Pattern.compile("^(SB\\d{2}-[A-Z]\\d+(?:-\\d+)?)-M(\\d{2})$", Pattern.CASE_INSENSITIVE);

  private SamItemIdSupport() {}

  public static boolean isValid(String imSubId) {
    return imSubId != null && IM_SUB_ID.matcher(imSubId.trim()).matches();
  }

  public static String normalize(String imSubId) {
    if (imSubId == null) {
      return null;
    }
    return imSubId.trim().toUpperCase();
  }

  /**
   * SB26-G1194-M01 → sbkId=SB26-G1194, imSubSeq=1
   */
  public static Parsed parse(String imSubId) {
    if (imSubId == null) {
      return null;
    }
    Matcher m = IM_SUB_ID_PARTS.matcher(normalize(imSubId));
    if (!m.matches()) {
      return null;
    }
    return new Parsed(m.group(1), Integer.parseInt(m.group(2)));
  }

  public static final class Parsed {
    private final String sbkId;
    private final int imSubSeq;

    public Parsed(String sbkId, int imSubSeq) {
      this.sbkId = sbkId;
      this.imSubSeq = imSubSeq;
    }

    public String getSbkId() {
      return sbkId;
    }

    public int getImSubSeq() {
      return imSubSeq;
    }
  }

}
