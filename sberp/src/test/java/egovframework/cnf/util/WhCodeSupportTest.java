package egovframework.cnf.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WhCodeSupportTest {

  @Test
  public void zoneName_allowsKoreanAndSymbols() {
    assertTrue(WhCodeSupport.isValidZoneName("가-01"));
    assertTrue(WhCodeSupport.isValidZoneName("박형태"));
    assertTrue(WhCodeSupport.isValidZoneName("전체"));
    assertTrue(WhCodeSupport.isValidZoneName("가/01"));
  }

  @Test
  public void zoneName_rejectsEmptyQuotesAndTooLong() {
    assertFalse(WhCodeSupport.isValidZoneName(null));
    assertFalse(WhCodeSupport.isValidZoneName("   "));
    assertFalse(WhCodeSupport.isValidZoneName("가'01"));
    assertFalse(WhCodeSupport.isValidZoneName("가\"01"));
    assertTrue(WhCodeSupport.isValidZoneName(repeat('가', 100)));
    assertFalse(WhCodeSupport.isValidZoneName(repeat('가', 101)));
  }

  @Test
  public void zoneName_trims() {
    assertEquals("가-01", WhCodeSupport.normalizeZoneName("  가-01 "));
    assertNull(WhCodeSupport.normalizeZoneName("   "));
  }

  @Test
  public void zoneDesc_optionalAndLimited() {
    assertNull(WhCodeSupport.normalizeZoneDesc(null));
    assertNull(WhCodeSupport.normalizeZoneDesc("  "));
    assertEquals("창가쪽 선반", WhCodeSupport.normalizeZoneDesc(" 창가쪽 선반 "));
    assertTrue(WhCodeSupport.isValidZoneDesc(repeat('가', 200)));
    assertFalse(WhCodeSupport.isValidZoneDesc(repeat('가', 201)));
  }

  @Test
  public void typeCode_defaultsToRoom() {
    assertEquals("01", WhCodeSupport.normalizeTypeCode(null));
    assertEquals("01", WhCodeSupport.normalizeTypeCode(""));
    assertEquals("01", WhCodeSupport.normalizeTypeCode("   "));
    assertEquals("02", WhCodeSupport.normalizeTypeCode(" 02 "));
    assertEquals("03", WhCodeSupport.normalizeTypeCode("03"));
  }

  @Test
  public void useCode_mapsScreenValues() {
    assertNull(WhCodeSupport.normalizeUseCode(null));
    assertNull(WhCodeSupport.normalizeUseCode(" "));
    assertEquals("01", WhCodeSupport.normalizeUseCode("01"));
    assertEquals("01", WhCodeSupport.normalizeUseCode("norm"));
    assertEquals("02", WhCodeSupport.normalizeUseCode("인수요청"));
    assertEquals("03", WhCodeSupport.normalizeUseCode("long"));
    assertNull(WhCodeSupport.normalizeUseCode("99"));
    assertEquals("일반", WhCodeSupport.useName("01"));
    assertEquals("인수요청", WhCodeSupport.useName("02"));
    assertEquals("장기", WhCodeSupport.useName("03"));
  }

  private static String repeat(char c, int n) {
    StringBuilder sb = new StringBuilder(n);
    for (int i = 0; i < n; i++) {
      sb.append(c);
    }
    return sb.toString();
  }
}
