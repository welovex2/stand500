package egovframework.cmm.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class OnlyOfficeEditorHtmlTest {

  @Test
  public void normalizeDocumentServerApiJs_trimsSlashAndAppendsApiPath() {
    assertEquals("https://oo.example.com/web-apps/apps/api/documents/api.js",
        OnlyOfficeEditorHtml.normalizeDocumentServerApiJs("https://oo.example.com/"));
    assertEquals("https://oo.example.com/web-apps/apps/api/documents/api.js",
        OnlyOfficeEditorHtml.normalizeDocumentServerApiJs("https://oo.example.com"));
  }
}
