package egovframework.raw.report;



import java.util.regex.Pattern;



/** Freemarker 렌더 후 HTML 후처리 (드래프트 워터마크). */

public final class ReportHtmlPostProcessor {



  private static final Pattern A4_CONTENT_OPEN = Pattern.compile(

      "(<article\\s+class=\"a4_content[^\"]*\"[^>]*>)",

      Pattern.CASE_INSENSITIVE);



  private static final String DRAFT_MARKUP = "<div class=\"draft_text\">DRAFT</div>";



  private ReportHtmlPostProcessor() {}



  /**

   * 프론트 report_tel.js 와 동일 — 각 {@code .a4_content} 상단에 DRAFT 워터마크.

   */

  public static String injectDraftWatermark(String html) {

    if (html == null || html.isEmpty()) {

      return html;

    }

    return A4_CONTENT_OPEN.matcher(html).replaceAll("$1" + DRAFT_MARKUP);

  }

}

