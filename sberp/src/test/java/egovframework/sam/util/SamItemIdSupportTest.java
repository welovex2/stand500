package egovframework.sam.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import egovframework.sam.util.SamItemIdSupport.Parsed;

public class SamItemIdSupportTest {

  @Test
  public void parse_normalApplyId() {
    Parsed p = SamItemIdSupport.parse("SB26-G1194-M01");
    assertNotNull(p);
    assertEquals("SB26-G1194", p.getSbkId());
    assertEquals(1, p.getImSubSeq());
  }

  @Test
  public void parse_reissueApplyId() {
    Parsed p = SamItemIdSupport.parse("SB26-G1194-1-M02");
    assertNotNull(p);
    assertEquals("SB26-G1194-1", p.getSbkId());
    assertEquals(2, p.getImSubSeq());
  }

  @Test
  public void parse_invalid_returnsNull() {
    assertNull(SamItemIdSupport.parse("SB26-G1194"));
    assertNull(SamItemIdSupport.parse(null));
  }

  @Test
  public void isValid_acceptsLowerCase() {
    assertTrue(SamItemIdSupport.isValid("sb26-g1194-m01"));
  }
}
