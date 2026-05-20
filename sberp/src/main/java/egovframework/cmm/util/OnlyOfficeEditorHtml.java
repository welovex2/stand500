package egovframework.cmm.util;

import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

/** ONLYOFFICE DocEditor iframe HTML 공통 출력. */
public final class OnlyOfficeEditorHtml {

  private static final ObjectMapper JSON = new ObjectMapper();

  private OnlyOfficeEditorHtml() {}

  public static void write(HttpServletResponse response, String fileName,
      Map<String, Object> editorConfig, String documentServerApiJsUrl) throws Exception {
    write(response, fileName, editorConfig, documentServerApiJsUrl, null, false);
  }

  /**
   * @param davPath 편집 대상 경로(클라이언트 저장 로그용)
   * @param reportSaveToErp true 이면 편집 저장 시 {@code onlyoffice-op-log} 로 POST
   */
  public static void write(HttpServletResponse response, String fileName,
      Map<String, Object> editorConfig, String documentServerApiJsUrl, String davPath,
      boolean reportSaveToErp) throws Exception {
    String cfgJson = JSON.writeValueAsString(editorConfig);
    String cfgJsonForHtml = cfgJson.replace("</", "<\\/");

    String eventsScript = "";
    if (reportSaveToErp && davPath != null && !davPath.trim().isEmpty()) {
      String pathJs = escapeJsString(davPath.trim());
      eventsScript = "var ooPath=" + pathJs + ",ooWasMod=false;"
          + "function ooReportSave(){try{fetch('onlyoffice-op-log',{method:'POST',credentials:'include',"
          + "headers:{'Content-Type':'application/json'},"
          + "body:JSON.stringify({path:ooPath,opType:'ONLYOFFICE_SAVE'})});}catch(e){}}"
          + "function ooHookEvents(c){c.events=c.events||{};"
          + "var p=c.events.onDocumentStateChange;"
          + "c.events.onDocumentStateChange=function(ev){"
          + "if(ev&&ev.data){ooWasMod=true;}else if(ooWasMod){ooWasMod=false;ooReportSave();}"
          + "if(typeof p==='function'){p(ev);}};}";
    }

    String html = "<!DOCTYPE html><html style=\"height:100%\"><head><meta charset=\"UTF-8\"/>"
        + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"/>"
        + "<title>" + escapeHtmlMinimal(fileName) + "</title>"
        + "<style>html,body{margin:0;height:100%;overflow:hidden}"
        + "#editor{width:100%;height:100%;min-height:100%}</style>"
        + "<script src=\"" + escapeHtmlMinimal(documentServerApiJsUrl) + "\"></script>"
        + "</head><body style=\"margin:0;height:100%\">"
        + "<div id=\"editor\" style=\"width:100%;height:100%\"></div>"
        + "<script type=\"application/json\" id=\"oo-cfg\">" + cfgJsonForHtml + "</script>"
        + "<script>document.addEventListener('DOMContentLoaded',function(){"
        + "var el=document.getElementById('oo-cfg');"
        + "var cfg=JSON.parse(el.textContent);"
        + eventsScript
        + "if(typeof ooHookEvents==='function'){ooHookEvents(cfg);}"
        + "new DocsAPI.DocEditor('editor',cfg);"
        + "});</script></body></html>";

    response.setStatus(200);
    response.setContentType("text/html;charset=UTF-8");
    response.getWriter().write(html);
  }

  public static String normalizeDocumentServerApiJs(String documentServerRoot) {
    if (documentServerRoot == null) {
      return "";
    }
    String u = documentServerRoot.trim();
    while (u.endsWith("/")) {
      u = u.substring(0, u.length() - 1);
    }
    return u + "/web-apps/apps/api/documents/api.js";
  }

  private static String escapeHtmlMinimal(String s) {
    if (s == null) {
      return "";
    }
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
  }

  private static String escapeJsString(String s) {
    if (s == null) {
      return "''";
    }
    return "'" + s.replace("\\", "\\\\").replace("'", "\\'").replace("\r", "\\r").replace("\n", "\\n")
        + "'";
  }
}
