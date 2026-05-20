package egovframework.cmm.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class OnlyOfficePreviewSupportTest {

  @Test
  public void previewableOfficeExtensions() {
    assertTrue(OnlyOfficePreviewSupport.isPreviewableExtension("report.xlsx"));
    assertTrue(OnlyOfficePreviewSupport.isPreviewableExtension("a.DOCX"));
    assertTrue(OnlyOfficePreviewSupport.isPreviewableExtension("slides.pptx"));
    assertFalse(OnlyOfficePreviewSupport.isPreviewableExtension("image.png"));
    assertFalse(OnlyOfficePreviewSupport.isPreviewableExtension("noext"));
  }

  @Test
  public void documentTypeByExtension() {
    assertEquals("cell", OnlyOfficePreviewSupport.documentType("a.xlsx"));
    assertEquals("slide", OnlyOfficePreviewSupport.documentType("a.pptx"));
    assertEquals("word", OnlyOfficePreviewSupport.documentType("a.docx"));
  }

  @Test
  public void fileTypeLowercaseWithoutDot() {
    assertEquals("xlsx", OnlyOfficePreviewSupport.fileType("Book.XLSX"));
  }
}
