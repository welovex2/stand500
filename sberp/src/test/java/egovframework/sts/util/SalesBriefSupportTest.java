package egovframework.sts.util;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class SalesBriefSupportTest {

  @Test
  public void eok_roundsOneDecimal() {
    assertEquals("93.0억", SalesBriefSupport.eok(930000L));
    assertEquals("1.7억", SalesBriefSupport.eok(16500L));
  }

  @Test
  public void monthForecast_scalesByDaysElapsed() {
    assertEquals(90L, SalesBriefSupport.monthForecast(30L, 10, 30));
    assertEquals(15L, SalesBriefSupport.monthForecast(15L, 0, 30));
  }

  @Test
  public void yearForecast_scalesByDayOfYear() {
    assertEquals(365L, SalesBriefSupport.yearForecast(100L, 100, 365));
  }

  @Test
  public void prorateYearTarget_fullPriorMonthsPlusFraction() {
    long[] t = new long[] {100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100};
    assertEquals(250L, SalesBriefSupport.prorateYearTarget(t, 3, 15, 30));
  }
}
