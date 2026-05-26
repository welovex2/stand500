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
  private static final Pattern SBK_NO_PATTERN = Pattern.compile("(?i)(SB\\d{2}-[A-Z]\\d+)");

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
   * 프론트(드래그앤드롭·폴더 선택)에서 넘어온 relativePath를 DAV 하위 경로로 정규화합니다.
   *
   * <p>사용 예:
   * <ul>
   *   <li>일반 파일 1개: {@code "report.pdf"}</li>
   *   <li>폴더 드롭(webkitRelativePath): {@code "내폴더/01.서류/report.pdf"}</li>
   * </ul>
   *
   * <p>처리 내용:
   * <ul>
   *   <li>백슬래시({@code \})를 슬래시로 통일</li>
   *   <li>앞쪽 {@code /} 제거 — base 폴더와 join 시 중복 방지</li>
   *   <li>{@code ..} 경로 탈출 차단</li>
   *   <li>각 경로 세그먼트의 Windows/Nextcloud 금지 문자({@code \ / : * ? " < > |})를 {@code _}로 치환</li>
   * </ul>
   *
   * @param relativePath 프론트 FormData {@code relativePath} (null/blank면 null 반환)
   * @return 정규화된 상대 경로, 입력이 null/blank면 null
   */
  public static String sanitizeRelativePath(String relativePath) {
    if (relativePath == null) {
      return null;
    }
    String p = relativePath.trim().replace("\\", "/");
    if (p.isEmpty()) {
      return null;
    }

    // base 경로와 합칠 때 "/sub/a.pdf" 형태가 되지 않도록 선행 슬래시 제거
    while (p.startsWith("/")) {
      p = p.substring(1);
    }
    if (p.isEmpty()) {
      return null;
    }

    // ../ 등 경로 탈출 차단 (normalizePath와 동일 정책)
    if (p.contains("..")) {
      throw new IllegalArgumentException("허용되지 않는 relativePath 입니다: " + relativePath);
    }

    // "a//b" 같이 빈 세그먼트가 섞인 경우 제거하면서 금지 문자 치환
    String[] parts = p.split("/");
    StringBuilder sb = new StringBuilder();
    for (String part : parts) {
      if (part == null) {
        continue;
      }
      part = part.trim().replaceAll("[\\\\/:*?\"<>|]", "_");
      if (part.isEmpty()) {
        continue;
      }
      if (sb.length() > 0) {
        sb.append('/');
      }
      sb.append(part);
    }
    return sb.length() == 0 ? null : sb.toString();
  }

  /**
   * 업로드 기준 폴더(baseDavPath) 아래에 relativePath(하위 폴더 + 파일명)를 붙여 최종 DAV 경로를 만듭니다.
   *
   * <p>예:
   * <ul>
   *   <li>base={@code /ERP/2026/02/SB26-G0000/00.공통}, rel={@code report.pdf}
   *       → {@code /ERP/2026/02/SB26-G0000/00.공통/report.pdf}</li>
   *   <li>base={@code /ERP/.../00.공통}, rel={@code 내폴더/01.서류/report.pdf}
   *       → {@code /ERP/.../00.공통/내폴더/01.서류/report.pdf}</li>
   * </ul>
   *
   * @param baseDavPath 화면에서 선택한 현재 폴더 DAV 경로 ({@code path} 파라미터)
   * @param relativePath  sanitizeRelativePath()로 정규화된 상대 경로 (파일명 포함)
   */
  public static String joinUnderBase(String baseDavPath, String relativePath) {
    String base = normalizePath(baseDavPath);
    String rel = sanitizeRelativePath(relativePath);
    if (rel == null || rel.isEmpty()) {
      throw new IllegalArgumentException("relativePath가 비어 있습니다.");
    }
    return normalizePath(base + "/" + rel);
  }

  /**
   * 업로드 시 사용할 effective relativePath를 결정합니다.
   *
   * <ol>
   *   <li>FormData {@code relativePath} (프론트 {@code webkitRelativePath || file.name})</li>
   *   <li>없으면 multipart {@code originalFilename}에 {@code /}가 포함된 경우 fallback</li>
   * </ol>
   */
  public static String resolveUploadRelativePath(String relativePath,
      String multipartOriginalFilename) {
    String safe = sanitizeRelativePath(relativePath);
    if (safe != null && !safe.isEmpty()) {
      return safe;
    }
    if (multipartOriginalFilename == null || multipartOriginalFilename.trim().isEmpty()) {
      return null;
    }
    String orig = multipartOriginalFilename.trim().replace("\\", "/");
    if (orig.contains("/")) {
      return sanitizeRelativePath(orig);
    }
    return null;
  }

  /**
   * relativePath(파일명 포함)에서 파일명만 추출합니다.
   * {@code "내폴더/a.pdf"} → {@code "a.pdf"}, {@code "a.pdf"} → {@code "a.pdf"}
   */
  public static String fileNameFromRelativePath(String relativePath) {
    String rel = sanitizeRelativePath(relativePath);
    if (rel == null || rel.isEmpty()) {
      return null;
    }
    int lastSlash = rel.lastIndexOf('/');
    return lastSlash < 0 ? rel : rel.substring(lastSlash + 1);
  }

  /**
   * relativePath(파일명 포함)에서 부모 폴더 상대 경로만 추출합니다.
   * {@code "내폴더/01.서류/a.pdf"} → {@code "내폴더/01.서류"}, 파일만 있으면 {@code ""}
   */
  public static String parentRelativePath(String relativePath) {
    String rel = sanitizeRelativePath(relativePath);
    if (rel == null || rel.isEmpty()) {
      return "";
    }
    int lastSlash = rel.lastIndexOf('/');
    return lastSlash < 0 ? "" : rel.substring(0, lastSlash);
  }

  /**
   * 업로드 base 폴더 아래 중간 폴더 경로(DAV) 목록을 누적 생성합니다.
   *
   * <p>예: base={@code /ERP/.../EM1033-1}, folderRel={@code 내폴더/01.서류}
   * <ul>
   *   <li>{@code /ERP/.../EM1033-1/내폴더}</li>
   *   <li>{@code /ERP/.../EM1033-1/내폴더/01.서류}</li>
   * </ul>
   */
  public static java.util.List<String> expandFolderDavPathsUnderBase(String baseDavPath,
      String folderOnlyRelativePath) {
    java.util.List<String> paths = new java.util.ArrayList<>();
    String rel = sanitizeRelativePath(folderOnlyRelativePath);
    if (rel == null || rel.isEmpty()) {
      return paths;
    }
    String current = normalizePath(baseDavPath);
    for (String part : rel.split("/")) {
      if (part == null || part.trim().isEmpty()) {
        continue;
      }
      current = normalizePath(current + "/" + part.trim());
      paths.add(current);
    }
    return paths;
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
