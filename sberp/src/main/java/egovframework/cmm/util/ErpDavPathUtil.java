package egovframework.cmm.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ErpDavPathUtil {

  private ErpDavPathUtil() {}

  private static final String ROOT = "/ERP";
  private static final Pattern SBK_NO_PATTERN = Pattern.compile("(?i)(SB\\d{2}-G\\d+)");

  public static String normalizePathOrRoot(String path) {
    if (path == null)
      return ROOT;
    String p = path.trim();
    if (p.isEmpty())
      return ROOT;

    if (!p.startsWith("/"))
      p = "/" + p;
    p = p.replaceAll("/+$", "");
    if (p.isEmpty())
      return ROOT;

    if (!p.startsWith(ROOT)) {
      p = ROOT + (p.startsWith("/") ? "" : "/") + p;
      p = p.replaceAll("//+", "/");
    }
    return p;
  }

  public static String extractSbkNo(String path) {
    Matcher m = SBK_NO_PATTERN.matcher(path);
    if (!m.find())
      throw new IllegalArgumentException("신청서번호를 경로에서 찾을 수 없습니다.");
    return m.group(1).toUpperCase();
  }

  public static String formatYearMonthFromInsDt(Date insDt) {
    ZoneId zone = ZoneId.of("Asia/Seoul");
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy/MM");

    if (insDt == null) {
      return LocalDate.now(zone).format(fmt);
    }
    Instant instant = insDt.toInstant();
    return instant.atZone(zone).format(fmt);
  }

  public static String normalizePath(String p) {
    if (p == null || p.trim().isEmpty())
      return "/";

    String x = p.trim().replace("\\", "/").replaceAll("/{2,}", "/");
    if (!x.startsWith("/"))
      x = "/" + x;

    if (x.endsWith("/") && x.length() > 1)
      x = x.substring(0, x.length() - 1);

    if (x.contains("/../") || x.endsWith("/..") || x.contains("/./") || x.startsWith("../")) {
      throw new IllegalArgumentException("허용되지 않는 경로입니다.");
    }
    return x;
  }

  /**
   * Windows 방식 파일명 충돌 해결: "a.txt" -> "a (1).txt", 확장자 없으면 "a (1)"
   */
  public static String withWindowsCopySuffix(String filename, int n) {
    int dot = filename.lastIndexOf('.');
    if (dot <= 0) { // 확장자 없음 or ".gitignore" 같은 케이스
      return filename + " (" + n + ")";
    }
    String name = filename.substring(0, dot);
    String ext = filename.substring(dot);
    return name + " (" + n + ")" + ext;
  }
}
