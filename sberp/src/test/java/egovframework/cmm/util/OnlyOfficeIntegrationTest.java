package egovframework.cmm.util;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class OnlyOfficeIntegrationTest {

  @Test
  public void disabledWhenPropertyServiceNull() {
    assertFalse(OnlyOfficeIntegration.isEnabled(null));
  }
}
