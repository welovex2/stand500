package egovframework.cmm.util;

import java.util.LinkedHashMap;
import java.util.Map;

/** ONLYOFFICE DocEditor 설정 보정. */
public final class OnlyOfficeEditorConfig {

  private OnlyOfficeEditorConfig() {}

  /**
   * 스프레드시트 편집 시 공동 편집 모드를 {@code strict} 로 고정한다.
   */
  @SuppressWarnings("unchecked")
  public static void applyStrictCoEditing(Map<String, Object> cfg) {
    if (cfg == null) {
      return;
    }
    Object ecObj = cfg.get("editorConfig");
    Map<String, Object> ec;
    if (ecObj instanceof Map) {
      ec = new LinkedHashMap<String, Object>((Map<String, Object>) ecObj);
    } else {
      ec = new LinkedHashMap<String, Object>();
    }
    Map<String, Object> coEditing = new LinkedHashMap<String, Object>();
    coEditing.put("mode", "strict");
    coEditing.put("change", Boolean.FALSE);
    ec.put("coEditing", coEditing);
    cfg.put("editorConfig", ec);
  }

  public static boolean isSpreadsheetFile(String fileName) {
    return "cell".equals(OnlyOfficePreviewSupport.documentType(fileName));
  }
}
