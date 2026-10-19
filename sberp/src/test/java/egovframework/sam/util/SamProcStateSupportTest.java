package egovframework.sam.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SamProcStateSupportTest {

  @Test
  public void occupied_keepsTest() {
    assertEquals("TEST", SamProcStateSupport.procStateForMove("KEEP", true));
    assertEquals("TEST", SamProcStateSupport.procStateForMove("IN", true));
  }

  @Test
  public void purpose_mapsProc() {
    assertEquals("IN", SamProcStateSupport.procStateForMove("IN", false));
    assertEquals("WAIT", SamProcStateSupport.procStateForMove("WAIT", false));
    assertEquals("KEEP", SamProcStateSupport.procStateForMove("LONG", false));
    assertEquals("KEEP", SamProcStateSupport.procStateForMove("BATT", false));
    assertEquals("TEST", SamProcStateSupport.procStateForMove("SETUP", false));
    assertEquals("IN", SamProcStateSupport.procStateForMove("TEMP", false));
  }

  @Test
  public void testType() {
    assertTrue(SamProcStateSupport.isValidTestType("em"));
    assertTrue(SamProcStateSupport.isValidTestType("EMC"));
    assertFalse(SamProcStateSupport.isValidTestType(""));
    assertFalse(SamProcStateSupport.isValidTestType("1"));
    assertEquals("EM", SamProcStateSupport.normalizeTestType(" em "));
  }
}
