package egovframework.cmm.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

@SuppressWarnings("unchecked")
public class OnlyOfficeEditorConfigTest {

  @Test
  public void spreadsheetDetection() {
    assertTrue(OnlyOfficeEditorConfig.isSpreadsheetFile("a.xlsx"));
    assertFalse(OnlyOfficeEditorConfig.isSpreadsheetFile("a.docx"));
  }

  @Test
  public void applyStrictCoEditing() {
    Map<String, Object> cfg = new HashMap<String, Object>();
    OnlyOfficeEditorConfig.applyStrictCoEditing(cfg);
    Map<String, Object> ec = (Map<String, Object>) cfg.get("editorConfig");
    Map<String, Object> co = (Map<String, Object>) ec.get("coEditing");
    assertEquals("strict", co.get("mode"));
    assertEquals(Boolean.FALSE, co.get("change"));
  }
}
