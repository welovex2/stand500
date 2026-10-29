package egovframework.sbk.util;

import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.util.StringUtils;

public final class SbkAppDateSupport {

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  private SbkAppDateSupport() {}

  /**
   * 신청년·월·일이 오늘(한국 시간)과 같은지 검사한다. 월·일은 1~2자리 숫자 모두 허용한다.
   */
  public static boolean matchesToday(String appYear, String appMon, String appDay) {
    if (!StringUtils.hasText(appYear) || !StringUtils.hasText(appMon) || !StringUtils.hasText(appDay)) {
      return false;
    }

    try {
      int year = Integer.parseInt(appYear.trim());
      int month = Integer.parseInt(appMon.trim());
      int day = Integer.parseInt(appDay.trim());
      LocalDate appDate = LocalDate.of(year, month, day);
      return appDate.equals(LocalDate.now(KST));
    } catch (RuntimeException e) {
      return false;
    }
  }
}
