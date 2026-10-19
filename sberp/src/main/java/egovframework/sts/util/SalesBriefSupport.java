package egovframework.sts.util;

/**
 * 영업 주간 한 장: 만원 단위 숫자를 착지·억 표시로 바꾼다.
 */
public final class SalesBriefSupport {

  private SalesBriefSupport() {}

  /** 만원 → "93.0억" */
  public static String eok(long manwon) {
    return String.format("%.1f억", manwon / 10000.0);
  }

  /** 이번 달 실적 / 경과일 × 말일. 1일 이전이면 실적 그대로. */
  public static long monthForecast(long monthActualMan, int dayOfMonth, int daysInMonth) {
    if (dayOfMonth <= 0 || daysInMonth <= 0) {
      return monthActualMan;
    }
    return Math.round(monthActualMan * (daysInMonth / (double) dayOfMonth));
  }

  /** 올해 실적 / 경과일 × 연일수. */
  public static long yearForecast(long yearActualMan, int dayOfYear, int daysInYear) {
    if (dayOfYear <= 0 || daysInYear <= 0) {
      return yearActualMan;
    }
    return Math.round(yearActualMan * (daysInYear / (double) dayOfYear));
  }

  /**
   * 월 목표 12칸(1월=0). 끝난 달은 전액, 이번 달은 일할로 더한 연 목표 진행분.
   */
  public static long prorateYearTarget(long[] monthTargets, int month /*1-12*/, int dayOfMonth,
      int daysInMonth) {
    if (monthTargets == null || monthTargets.length < 12 || month < 1 || month > 12) {
      return 0L;
    }
    long sum = 0L;
    for (int i = 0; i < month - 1; i++) {
      sum += monthTargets[i];
    }
    long thisMonth = monthTargets[month - 1];
    if (daysInMonth <= 0 || dayOfMonth <= 0) {
      return sum;
    }
    sum += Math.round(thisMonth * (dayOfMonth / (double) daysInMonth));
    return sum;
  }
}
