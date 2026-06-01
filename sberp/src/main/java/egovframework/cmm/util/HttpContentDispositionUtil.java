package egovframework.cmm.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 다운로드 응답 Content-Disposition 헤더 생성.
 */
public final class HttpContentDispositionUtil {

  private HttpContentDispositionUtil() {}

  public static String buildAttachmentDisposition(String fileName, String userAgent) {
    return buildDisposition("attachment", fileName, userAgent);
  }

  public static String buildDisposition(String type, String fileName, String userAgent) {
    String original = normalizePdfFileName(fileName);
    String encoded = urlEncodeUtf8(original);

    if (isLegacyIeFamily(userAgent)) {
      return type + "; filename=\"" + encoded + "\"";
    }

    if (isAsciiFileName(original)) {
      return type + "; filename=\"" + escapeQuotes(original) + "\"";
    }

    // Tomcat 8: filename= 은 ISO-8859-1(0~255)만 허용. 한글은 RFC5987 filename* 로 전달.
    String fallback = toAsciiFallbackName(original);
    return type + "; filename=\"" + escapeQuotes(fallback) + "\"; filename*=UTF-8''" + encoded;
  }

  public static String normalizePdfFileName(String fileName) {
    if (fileName == null || fileName.trim().isEmpty()) {
      return "export.pdf";
    }
    String name = fileName.trim();
    if (!name.toLowerCase().endsWith(".pdf")) {
      name = name + ".pdf";
    }
    return name;
  }

  /** ASCII fallback. 한글은 생략하고 남은 영문·숫자만 사용. */
  public static String toAsciiFallbackName(String original) {
    original = normalizePdfFileName(original);

    int dot = original.lastIndexOf('.');
    String ext = dot >= 0 ? original.substring(dot) : ".pdf";
    String base = dot >= 0 ? original.substring(0, dot) : original;

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < base.length(); i++) {
      char c = base.charAt(i);
      if (c >= 0x20 && c < 0x7F && c != '"' && c != '\\' && c != ';') {
        sb.append(c);
      }
    }

    String asciiBase = sb.toString().replaceAll("^[._\\- ]+", "").replaceAll("[._\\- ]+$", "");
    if (asciiBase.isEmpty()) {
      return "export" + ext;
    }
    return asciiBase + ext;
  }

  public static String urlEncodeUtf8(String s) {
    try {
      return URLEncoder.encode(s, "UTF-8").replace("+", "%20");
    } catch (UnsupportedEncodingException e) {
      return s;
    }
  }

  private static boolean isAsciiFileName(String name) {
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      if (c >= 0x80) {
        return false;
      }
      if (c < 0x20 || c == '"' || c == '\\' || c == ';') {
        return false;
      }
    }
    return true;
  }

  private static String escapeQuotes(String name) {
    return name.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  private static boolean isLegacyIeFamily(String ua) {
    if (ua == null) {
      return false;
    }
    String u = ua.toLowerCase();
    return u.contains("msie") || u.contains("trident") || u.contains("edge/");
  }

}
