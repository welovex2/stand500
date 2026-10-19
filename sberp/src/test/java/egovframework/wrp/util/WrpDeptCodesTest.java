package egovframework.wrp.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.junit.Test;

public class WrpDeptCodesTest {

  @Test
  public void expand_em_includesTrip() {
    List<String> codes = WrpDeptCodes.expand("EM");
    assertEquals(2, codes.size());
    assertEquals("EM", codes.get(0));
    assertEquals("EMC", codes.get(1));
    assertFalse(WrpDeptCodes.needsTotal("EM"));
  }

  @Test
  public void expand_md_includesTrip() {
    List<String> codes = WrpDeptCodes.expand("MD");
    assertEquals("MDC", codes.get(1));
  }

  @Test
  public void expand_rs_needsTotal() {
    List<String> codes = WrpDeptCodes.expand("RS");
    assertEquals("RF", codes.get(0));
    assertEquals("SR", codes.get(1));
    assertTrue(WrpDeptCodes.needsTotal("RS"));
  }

  @Test
  public void expand_sf_needsTotal() {
    List<String> codes = WrpDeptCodes.expand("SF");
    assertEquals("SF", codes.get(0));
    assertEquals("NS", codes.get(1));
    assertTrue(WrpDeptCodes.needsTotal("SF"));
  }

  @Test
  public void expand_empty_isEmpty() {
    assertTrue(WrpDeptCodes.expand(null).isEmpty());
    assertTrue(WrpDeptCodes.expand("").isEmpty());
  }
}
