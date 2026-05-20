package egovframework.cmm.util;

import java.util.Locale;

/** ONLYOFFICE 브라우저 미리보기(뷰 모드)용 확장자·문서 종류. */
public final class OnlyOfficePreviewSupport {

  private OnlyOfficePreviewSupport() {}

  public static boolean isPreviewableExtension(String fileName) {
    String ext = extractExt(fileName);
    return ext != null && isPreviewableExt(ext);
  }

  private static boolean isPreviewableExt(String ext) {
    switch (ext) {
      case "doc":
      case "docx":
      case "docm":
      case "dot":
      case "dotx":
      case "odt":
      case "rtf":
      case "txt":
      case "hwp":
      case "hwpx":
      case "xls":
      case "xlsx":
      case "xlsm":
      case "ods":
      case "csv":
      case "ppt":
      case "pptx":
      case "pptm":
      case "odp":
        return true;
      default:
        return false;
    }
  }

  /**
   * ONLYOFFICE documentType: word | cell | slide
   */
  public static String documentType(String fileName) {
    String ext = extractExt(fileName);
    if (ext == null) {
      return "word";
    }
    switch (ext) {
      case "xls":
      case "xlsx":
      case "xlsm":
      case "ods":
      case "csv":
        return "cell";
      case "ppt":
      case "pptx":
      case "pptm":
      case "odp":
        return "slide";
      default:
        return "word";
    }
  }

  /** 확장자만 (소문자, 점 제외). */
  public static String fileType(String fileName) {
    String ext = extractExt(fileName);
    return ext == null ? "" : ext;
  }

  private static String extractExt(String fileName) {
    if (fileName == null) {
      return null;
    }
    String lower = fileName.toLowerCase(Locale.ROOT);
    int dot = lower.lastIndexOf('.');
    if (dot < 0 || dot >= lower.length() - 1) {
      return null;
    }
    return lower.substring(dot + 1);
  }
}
