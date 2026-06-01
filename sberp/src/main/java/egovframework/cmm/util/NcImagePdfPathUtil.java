package egovframework.cmm.util;

import java.util.ArrayList;
import java.util.List;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 이미지 → PDF 변환 경로·파일명 유틸.
 */
public final class NcImagePdfPathUtil {

  public static final int MAX_IMAGES = 50;

  private static final Pattern BASE_APP_PATH = Pattern
      .compile("^/ERP/(\\d{4})/(0[1-9]|1[0-2])/(SB\\d{2}-[GM]\\d{4})(?:/.*)?$");

  private NcImagePdfPathUtil() {}

  public static String parentFolderPath(String fileDavPath) {
    String p = ErpDavPathUtil.normalizePath(fileDavPath);
    int idx = p.lastIndexOf('/');
    if (idx <= 0) {
      return p;
    }
    return p.substring(0, idx);
  }

  public static String extractNameFromPath(String davPath) {
    if (davPath == null) {
      return "file";
    }
    String p = davPath;
    if (p.endsWith("/")) {
      p = p.substring(0, p.length() - 1);
    }
    int idx = p.lastIndexOf("/");
    if (idx < 0) {
      return p;
    }
    String name = p.substring(idx + 1);
    return name == null || name.trim().isEmpty() ? "file" : name;
  }

  /** download-folder ZIP 명명과 동일: 부모 폴더명 + .pdf */
  public static String buildPdfFileNameFromFolderPath(String folderPath) {
    String base = extractNameFromPath(folderPath);
    if (base == null || base.trim().isEmpty() || "/".equals(base)) {
      base = "export";
    }
    base = trimTrailingDotOrSpace(base);
    base = base.replaceAll("[\\\\/:*?\"<>|]", "_");
    if (base.isEmpty()) {
      base = "export";
    }
    return base + ".pdf";
  }

  public static String trimTrailingDotOrSpace(String s) {
    if (s == null) {
      return null;
    }
    String v = s;
    while (v.endsWith(".") || v.endsWith(" ")) {
      v = v.substring(0, v.length() - 1);
    }
    return v.isEmpty() ? "export" : v;
  }

  public static int pageCountForImages(int imageCount) {
    if (imageCount <= 0) {
      return 0;
    }
    return (imageCount + 1) / 2;
  }

  /**
   * 이미지 파일 path 목록에서 공통 부모 폴더(직계)의 마지막 세그먼트 + {@code .pdf} 를 반환한다.
   * browse 경로 변환 전 원본 paths 기준으로 파일명을 정할 때 사용한다.
   *
   * <p>예: {@code /ERP/.../00.시험사진/a.jpg} → {@code 00.시험사진.pdf}
   */
  public static String resolvePdfFileNameFromImagePaths(List<String> imageFilePaths) {
    String folderPath = resolveCommonParentFolderPath(imageFilePaths);
    return buildPdfFileNameFromFolderPath(folderPath);
  }

  /** 이미지들이 들어 있는 공통 직계 부모 폴더 DAV 경로. */
  public static String resolveCommonParentFolderPath(List<String> imageFilePaths) {
    if (imageFilePaths == null || imageFilePaths.isEmpty()) {
      throw pdfError(400, "변환할 이미지를 1장 이상 선택해 주세요.");
    }

    String folderPath = null;
    for (String raw : imageFilePaths) {
      if (raw == null || raw.trim().isEmpty()) {
        throw pdfError(400, "경로가 비어있습니다.");
      }

      String path = normalizeImageFilePath(raw);
      if (path.endsWith("/")) {
        throw pdfError(400, "폴더는 PDF 변환 대상이 아닙니다.");
      }

      String parent = parentFolderPath(path);
      if (folderPath == null) {
        folderPath = parent;
      } else if (!folderPath.equals(parent)) {
        throw pdfError(400, "같은 폴더의 이미지만 함께 변환할 수 있습니다.");
      }
    }
    return folderPath;
  }

  private static String normalizeImageFilePath(String raw) {
    String p = raw == null ? "" : raw.trim();
    if (p.isEmpty()) {
      return p;
    }
    try {
      if (p.indexOf('%') >= 0) {
        p = URLDecoder.decode(p, "UTF-8");
      }
    } catch (Exception ignore) {
      // decode 실패 시 원본 유지
    }
    return ErpDavPathUtil.normalizePath(p);
  }

  /**
   * paths 검증 후 정규화된 목록·폴더·PDF 파일명을 반환한다.
   *
   * @throws egovframework.ncc.service.NcImagePdfException 검증 실패
   */
  public static NcImagePdfPathResolution resolvePaths(List<String> paths) {
    if (paths == null || paths.isEmpty()) {
      throw pdfError(400, "변환할 이미지를 1장 이상 선택해 주세요.");
    }
    if (paths.size() > MAX_IMAGES) {
      throw pdfError(400, "한 번에 변환 가능한 이미지는 최대 " + MAX_IMAGES + "장입니다.");
    }

    List<String> normalized = new ArrayList<String>();
    String folderPath = null;

    for (String raw : paths) {
      if (raw == null || raw.trim().isEmpty()) {
        throw pdfError(400, "경로가 비어있습니다.");
      }

      String path = normalizeImageFilePath(raw);
      String pathErr = validateDownloadPathStrong(path);
      if (pathErr != null) {
        throw pdfError(400, pathErr);
      }

      if (path.endsWith("/")) {
        throw pdfError(400, "폴더는 PDF 변환 대상이 아닙니다.");
      }

      String fileName = extractNameFromPath(path);
      if (!FilePreviewPolicy.TYPE_IMAGE.equals(
          FilePreviewPolicy.classify(fileName).getPreviewType())) {
        throw pdfError(400, "이미지 파일만 변환할 수 있습니다: " + fileName);
      }

      String parent = parentFolderPath(path);
      if (folderPath == null) {
        folderPath = parent;
      } else if (!folderPath.equals(parent)) {
        throw pdfError(400, "같은 폴더의 이미지만 함께 변환할 수 있습니다.");
      }

      normalized.add(path);
    }

    String pdfFileName = resolvePdfFileNameFromImagePaths(paths);
    String outputDavPath = ErpDavPathUtil.normalizePath(folderPath + "/" + pdfFileName);
    String pathsSummary = buildPathsSummaryForLog(normalized);

    return new NcImagePdfPathResolution(normalized, folderPath, pdfFileName, outputDavPath,
        pathsSummary);
  }

  private static String buildPathsSummaryForLog(List<String> paths) {
    StringBuilder sb = new StringBuilder();
    sb.append('[');
    for (int i = 0; i < paths.size(); i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append('"').append(paths.get(i).replace("\\", "\\\\").replace("\"", "\\\""))
          .append('"');
      if (sb.length() > 1950) {
        sb.append(",...");
        break;
      }
    }
    sb.append(']');
    String s = sb.toString();
    return s.length() > 2000 ? s.substring(0, 2000) : s;
  }

  private static String validateDownloadPathStrong(String path) {
    if (path == null) {
      return "경로가 비어있습니다.";
    }

    String p = path.trim();
    if (p.isEmpty()) {
      return "경로가 비어있습니다.";
    }

    if (p.indexOf('\\') >= 0) {
      return "경로에 '\\'는 사용할 수 없습니다.";
    }

    String lower = p.toLowerCase();
    if (lower.contains("%2f") || lower.contains("%5c")) {
      return "경로에 인코딩된 경로 구분자는 사용할 수 없습니다.";
    }

    String segErr = validatePathSegmentsNoTraversal(p);
    if (segErr != null) {
      return segErr;
    }

    Matcher m = BASE_APP_PATH.matcher(p);
    if (!m.matches()) {
      return "허용되지 않은 경로입니다. /ERP/년도/월/신청서번호 아래에서만 허용됩니다.";
    }
    return null;
  }

  private static String validatePathSegmentsNoTraversal(String path) {
    String[] parts = path.split("/");
    for (String part : parts) {
      if (part == null || part.isEmpty()) {
        continue;
      }
      if (".".equals(part) || "..".equals(part)) {
        return "경로에 '.' 또는 '..' 세그먼트는 사용할 수 없습니다.";
      }
      for (int i = 0; i < part.length(); i++) {
        if (Character.isISOControl(part.charAt(i))) {
          return "경로에 제어문자는 사용할 수 없습니다.";
        }
      }
    }
    return null;
  }

  private static egovframework.ncc.service.NcImagePdfException pdfError(int code, String msg) {
    return new egovframework.ncc.service.NcImagePdfException(code, msg);
  }

  /** resolvePaths 결과. */
  public static final class NcImagePdfPathResolution {
    private final List<String> normalizedPaths;
    private final String folderPath;
    private final String pdfFileName;
    private final String outputDavPath;
    private final String pathsSummaryForLog;

    public NcImagePdfPathResolution(List<String> normalizedPaths, String folderPath,
        String pdfFileName, String outputDavPath, String pathsSummaryForLog) {
      this.normalizedPaths = normalizedPaths;
      this.folderPath = folderPath;
      this.pdfFileName = pdfFileName;
      this.outputDavPath = outputDavPath;
      this.pathsSummaryForLog = pathsSummaryForLog;
    }

    public List<String> getNormalizedPaths() {
      return normalizedPaths;
    }

    public String getFolderPath() {
      return folderPath;
    }

    public String getPdfFileName() {
      return pdfFileName;
    }

    public String getOutputDavPath() {
      return outputDavPath;
    }

    public String getPathsSummaryForLog() {
      return pathsSummaryForLog;
    }
  }

}
