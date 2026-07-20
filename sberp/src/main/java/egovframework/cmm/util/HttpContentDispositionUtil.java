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

    /*
     * 한글 파일명:
     * 1) filename*=UTF-8'' — 브라우저 저장명 (한글)
     * 2) filename= ASCII — fetch·구형 클라이언트용 (확장자 .pdf 보장, mojibake 없음)
     * ISO-8859-1 passthrough 는 사용하지 않음.
     */
    String ascii = toAsciiFallbackName(original);
    return type + "; filename=\"" + escapeQuotes(ascii) + "\"; filename*=UTF-8''" + encoded;
  }

  /**
   * Content-Disposition 헤더에서 파일명 추출 (fetch 다운로드용).
   * filename* (RFC 5987) 우선, 없으면 filename=.
   */
  public static String parseFileNameFromDisposition(String contentDisposition) {
    if (contentDisposition == null || contentDisposition.isEmpty()) {
      return null;
    }
    int starIdx = contentDisposition.indexOf("filename*=UTF-8''");
    if (starIdx >= 0) {
      String rest = contentDisposition.substring(starIdx + "filename*=UTF-8''".length());
      int end = rest.indexOf(';');
      String encoded = end >= 0 ? rest.substring(0, end) : rest;
      return urlDecodeUtf8(encoded.trim());
    }
    int idx = contentDisposition.toLowerCase().indexOf("filename=");
    if (idx < 0) {
      return null;
    }
    String rest = contentDisposition.substring(idx + "filename=".length()).trim();
    if (rest.startsWith("\"")) {
      int close = rest.indexOf('"', 1);
      if (close > 0) {
        return rest.substring(1, close);
      }
    }
    int end = rest.indexOf(';');
    return (end >= 0 ? rest.substring(0, end) : rest).trim();
  }

  public static String urlDecodeUtf8(String s) {
    try {
      return java.net.URLDecoder.decode(s, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return s;
    }
  }

  /** 프론트 fetch 등에서 읽을 수 있는 URL 인코딩 파일명 */
  public static String urlEncodedFileName(String fileName) {
    return urlEncodeUtf8(normalizePdfFileName(fileName));
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

  /** ASCII fallback — 비한글 파일명용 */
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

    String asciiBase = sb.toString().replaceAll("_{2,}", "_").replaceAll("^[._\\- ]+", "")
        .replaceAll("[._\\- ]+$", "");
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
