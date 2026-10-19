package egovframework.sts.util;

/**
 * 시험부별 손익 한 장: 비율·단가 계산. 분모가 0인 칸은 0으로 둔다.
 */
public final class DeptPnlSupport {

  private DeptPnlSupport() {}

  /** 소수 첫째 자리로 자른다. */
  public static double round1(double v) {
    return Math.round(v * 10.0) / 10.0;
  }

  /** part ÷ whole × 100. whole 이 0이면 0. */
  public static double pct(double part, double whole) {
    if (whole == 0.0) {
      return 0.0;
    }
    return round1(part * 100.0 / whole);
  }

  /** (cur − prev) ÷ prev × 100. prev 가 0이면 0 (비교 대상이 없다는 뜻). */
  public static double yoyPct(double cur, double prev) {
    if (prev == 0.0) {
      return 0.0;
    }
    return round1((cur - prev) * 100.0 / prev);
  }

  /** 건당 단가(만원). 건수가 0이면 0. */
  public static double perCase(long netMan, int cnt) {
    if (cnt == 0) {
      return 0.0;
    }
    return round1(netMan / (double) cnt);
  }
}
