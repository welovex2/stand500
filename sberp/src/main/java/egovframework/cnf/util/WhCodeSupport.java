package egovframework.cnf.util;

/**
 * 창고 형태 · 구역명 · 구역 용도 정규화.
 */
public final class WhCodeSupport {

  /** 구역명 최대 길이. */
  public static final int ZONE_NAME_MAX = 100;

  /** 구역설명 최대 길이. */
  public static final int ZONE_DESC_MAX = 200;

  /** 형태 기본값. 공통코드 WH 의 01 = 방. */
  public static final String DEFAULT_TYPE_CODE = "01";

  /** 용도 기본값. 일반. */
  public static final String USE_NORMAL = "01";

  /** 용도. 인수요청. */
  public static final String USE_PICK = "02";

  /** 범위 추가 한 번에 만드는 최대 개수. */
  public static final int ZONE_RANGE_MAX = 50;

  /** 용도. 장기. */
  public static final String USE_LONG = "03";

  private WhCodeSupport() {}

  /**
   * 형태 코드 정규화. 값이 없으면 기본값 01(방).
   *
   * <p>유효한 코드인지는 공통코드(TOP_CD='WH') 를 조회해서 판정한다.
   */
  public static String normalizeTypeCode(String typeCode) {
    if (typeCode == null) {
      return DEFAULT_TYPE_CODE;
    }
    String n = typeCode.trim().toUpperCase();
    return n.isEmpty() ? DEFAULT_TYPE_CODE : n;
  }

  /** 앞뒤 공백 제거. 빈 값은 null. */
  public static String normalizeZoneName(String zoneName) {
    if (zoneName == null) {
      return null;
    }
    String n = zoneName.trim();
    return n.isEmpty() ? null : n;
  }

  /** 범위 이름. 접두어 가, 번호 1 → 가-01. 10부터는 가-10. */
  public static String zoneRangeName(String prefix, int no) {
    String p = normalizeZoneName(prefix);
    if (p == null || no < 1) {
      return null;
    }
    String num = no < 10 ? "0" + no : String.valueOf(no);
    return p + "-" + num;
  }

  /** 비어 있지 않고, 100자 이내이며, 따옴표가 없어야 한다. */
  public static boolean isValidZoneName(String zoneName) {
    String n = normalizeZoneName(zoneName);
    if (n == null || n.length() > ZONE_NAME_MAX) {
      return false;
    }
    return n.indexOf('\'') < 0 && n.indexOf('"') < 0;
  }

  /** 앞뒤 공백 제거. 빈 값은 null. 길이 검사는 {@link #isValidZoneDesc(String)}. */
  public static String normalizeZoneDesc(String zoneDesc) {
    if (zoneDesc == null) {
      return null;
    }
    String n = zoneDesc.trim();
    return n.isEmpty() ? null : n;
  }

  public static boolean isValidZoneDesc(String zoneDesc) {
    String n = normalizeZoneDesc(zoneDesc);
    return n == null || n.length() <= ZONE_DESC_MAX;
  }

  /**
   * 용도 코드. 빈 값이면 null. 01/일반, 02/인수요청, 03/장기 만 받는다.
   * 화면 값 norm, pick, long 도 같은 코드로 맞춘다.
   */
  public static String normalizeUseCode(String useCode) {
    if (useCode == null) {
      return null;
    }
    String n = useCode.trim();
    if (n.isEmpty()) {
      return null;
    }
    String lower = n.toLowerCase();
    if (USE_NORMAL.equals(n) || "norm".equals(lower) || "일반".equals(n)) {
      return USE_NORMAL;
    }
    if (USE_PICK.equals(n) || "pick".equals(lower) || "인수요청".equals(n)) {
      return USE_PICK;
    }
    if (USE_LONG.equals(n) || "long".equals(lower) || "장기".equals(n)) {
      return USE_LONG;
    }
    return null;
  }

  public static String useName(String useCode) {
    if (USE_PICK.equals(useCode)) {
      return "인수요청";
    }
    if (USE_LONG.equals(useCode)) {
      return "장기";
    }
    return "일반";
  }
}
