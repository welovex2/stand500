package egovframework.raw.report;



import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertFalse;

import static org.junit.Assert.assertTrue;



import org.junit.Test;



public class ReportHtmlPostProcessorTest {



  @Test

  public void injectDraftWatermark_insertsIntoEachA4Content() {

    String html = "<section><article class=\"a4_content\">x</article>"

        + "<article class=\"a4_content no_margin\">y</article></section>";

    String out = ReportHtmlPostProcessor.injectDraftWatermark(html);

    assertEquals(2, countOccurrences(out, "<div class=\"draft_text\">DRAFT</div>"));

    assertTrue(out.contains("<article class=\"a4_content\"><div class=\"draft_text\">DRAFT</div>"));

    assertTrue(out.contains(

        "<article class=\"a4_content no_margin\"><div class=\"draft_text\">DRAFT</div>"));

  }



  @Test

  public void injectDraftWatermark_noOpWhenEmpty() {

    assertEquals("", ReportHtmlPostProcessor.injectDraftWatermark(""));

    assertEquals(null, ReportHtmlPostProcessor.injectDraftWatermark(null));

  }



  @Test

  public void injectDraftWatermark_skipsWhenNoA4Content() {

    String html = "<div class=\"a4_content\">not article</div>";

    assertFalse(ReportHtmlPostProcessor.injectDraftWatermark(html).contains("draft_text"));

  }



  private static int countOccurrences(String s, String sub) {

    int n = 0;

    int idx = 0;

    while ((idx = s.indexOf(sub, idx)) >= 0) {

      n++;

      idx += sub.length();

    }

    return n;

  }

}

