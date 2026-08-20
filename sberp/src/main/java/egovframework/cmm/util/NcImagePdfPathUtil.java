package egovframework.cmm.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    return buildPdfFileNameFromFolderName(extractNameFromPath(folderPath));
  }

  /** 폴더명(세그먼트) → 폴더명.pdf (경로 불가문자 치환). */
  public static String buildPdfFileNameFromFolderName(String folderName) {
    String base = folderName;
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

  /**
   * 최종 PDF 파일명.
   *
   * <ul>
   *   <li><b>외관도·부품배치도</b>: {@code {신청서번호}_{폴더명}_{회사명}_{모델명}.pdf}
   *       — 비어 있는 조각은 생략하고 언더바로 연결.</li>
   *   <li>그 외 폴더: {@code 폴더명.pdf} (기존 규칙).</li>
   * </ul>
   */
  public static String buildPdfFileName(String folderName, String sbkNo, String cmpyName,
      String modelName) {
    String fn = folderName == null ? "" : folderName.trim();
    if (isExteriorFolder(fn) || isPartsLayoutFolder(fn)) {
      StringBuilder sb = new StringBuilder();
      appendFileNameSegment(sb, sbkNo);
      appendFileNameSegment(sb, fn);
      appendFileNameSegment(sb, cmpyName);
      appendFileNameSegment(sb, modelName);
      String base = sb.length() == 0 ? "export" : sb.toString();
      return base + ".pdf";
    }
    return buildPdfFileNameFromFolderName(fn);
  }

  private static void appendFileNameSegment(StringBuilder sb, String raw) {
    String seg = sanitizeFileNameSegment(raw);
    if (seg.isEmpty()) {
      return;
    }
    if (sb.length() > 0) {
      sb.append('_');
    }
    sb.append(seg);
  }

  /** 파일명 조각 정규화 — 경로 불가문자/구분자 치환, 앞뒤 점·공백 제거. 빈 값은 "". */
  static String sanitizeFileNameSegment(String s) {
    if (s == null) {
      return "";
    }
    String v = s.trim().replaceAll("[\\\\/:*?\"<>|]", "_");
    while (v.endsWith(".") || v.endsWith(" ")) {
      v = v.substring(0, v.length() - 1);
    }
    while (v.startsWith(".") || v.startsWith(" ")) {
      v = v.substring(1);
    }
    return v;
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

  /** 외관도 폴더명 */
  public static final String FOLDER_EXTERIOR = "외관도";
  /** 부품배치도 폴더명 */
  private static final String FOLDER_PARTS_LAYOUT = "부품배치도";
  /** 부품배치도: 항상 최우선으로 배치되는 키워드 (공백 무시 비교) */
  private static final String PARTS_LAYOUT_FIRST_KEYWORD = "제품내부";
  /** 외관도 제품라벨 파일명 키워드 (공백 무시). 예: 08_제품라벨 */
  private static final String PRODUCT_LABEL_KEYWORD = "제품라벨";

  /**
   * 폴더명 규칙에 따라 이미지 순서를 정하고, PDF 사진 아래 표기할 라벨을 만든다.
   *
   * <ul>
   *   <li><b>외관도</b>: 파일명 앞 숫자(01,02,..)로 오름차순 정렬. 라벨은 순서대로
   *       {@code 1_,2_,3_} 로 다시 매기고 언더바 뒤 문자열은 그대로 사용.
   *       단 제품라벨 이미지는 항상 맨 마지막.</li>
   *   <li><b>부품배치도</b>: 파일명 오름차순(한글→영문). 단 {@code 제품 내부} 포함 파일은 항상 먼저.
   *       라벨은 확장자 제외 파일명.</li>
   *   <li>그 외: 입력 순서 유지. 라벨은 확장자 제외 파일명.</li>
   * </ul>
   */
  public static List<NcImagePdfImage> orderImagesForPdf(String folderName,
      List<String> normalizedPaths) {
    List<String> list = new ArrayList<String>(normalizedPaths == null
        ? new ArrayList<String>() : normalizedPaths);
    String fn = folderName == null ? "" : folderName.trim();

    if (isExteriorFolder(fn)) {
      Collections.sort(list, new Comparator<String>() {
        @Override
        public int compare(String p1, String p2) {
          String b1 = baseName(p1);
          String b2 = baseName(p2);
          // 제품라벨은 항상 맨 아래
          boolean l1 = isProductLabelFileName(b1);
          boolean l2 = isProductLabelFileName(b2);
          if (l1 != l2) {
            return l1 ? 1 : -1;
          }
          int n1 = leadingNumber(b1);
          int n2 = leadingNumber(b2);
          if (n1 != n2) {
            return n1 < n2 ? -1 : 1;
          }
          return b1.compareTo(b2);
        }
      });
      List<NcImagePdfImage> out = new ArrayList<NcImagePdfImage>();
      int seq = 1;
      for (String p : list) {
        String suffix = labelSuffixAfterUnderscore(baseName(p));
        out.add(new NcImagePdfImage(p, seq + "_" + suffix));
        seq++;
      }
      return out;
    }

    if (isPartsLayoutFolder(fn)) {
      Collections.sort(list, new Comparator<String>() {
        @Override
        public int compare(String p1, String p2) {
          String b1 = baseName(p1);
          String b2 = baseName(p2);
          int g1 = isPartsLayoutFirst(b1) ? 0 : 1;
          int g2 = isPartsLayoutFirst(b2) ? 0 : 1;
          if (g1 != g2) {
            return g1 - g2;
          }
          return compareKoreanFirst(b1, b2);
        }
      });
      List<NcImagePdfImage> out = new ArrayList<NcImagePdfImage>();
      for (String p : list) {
        out.add(new NcImagePdfImage(p, baseName(p)));
      }
      return out;
    }

    List<NcImagePdfImage> out = new ArrayList<NcImagePdfImage>();
    for (String p : list) {
      out.add(new NcImagePdfImage(p, baseName(p)));
    }
    return out;
  }

  public static boolean isExteriorFolder(String folderName) {
    return FOLDER_EXTERIOR.equals(folderName == null ? "" : folderName.trim());
  }

  public static boolean isPartsLayoutFolder(String folderName) {
    return FOLDER_PARTS_LAYOUT.equals(folderName == null ? "" : folderName.trim());
  }

  /** 파일명에 신청서 회사명·모델명을 붙이는 폴더 (외관도·부품배치도). */
  public static boolean usesSbkNamingInPdfFileName(String folderName) {
    return isExteriorFolder(folderName) || isPartsLayoutFolder(folderName);
  }

  /** 선택 경로에 제품라벨 이미지(파일명에 '제품라벨')가 있는지. */
  public static boolean hasProductLabelImage(List<String> paths) {
    if (paths == null) {
      return false;
    }
    for (String p : paths) {
      if (isProductLabelFileName(baseName(p))) {
        return true;
      }
    }
    return false;
  }

  /** 예: 08_제품라벨, 08_제품 라벨 */
  public static boolean isProductLabelFileName(String base) {
    if (base == null) {
      return false;
    }
    return base.replace(" ", "").contains(PRODUCT_LABEL_KEYWORD);
  }

  /**
   * 자동 제품라벨을 맨 마지막에 넣고 {@code 1_,2_,…} 로 재번호한다.
   * 호출 전에 제품라벨 파일 부재를 확인한 뒤 호출한다.
   */
  public static List<NcImagePdfImage> insertExteriorAutoProductLabel(
      List<NcImagePdfImage> ordered, ProductLabelContent content) {
    if (content == null) {
      return ordered;
    }
    List<NcImagePdfImage> src =
        ordered == null ? new ArrayList<NcImagePdfImage>() : ordered;

    List<NcImagePdfImage> merged = new ArrayList<NcImagePdfImage>(src.size() + 1);
    merged.addAll(src);
    merged.add(NcImagePdfImage.autoProductLabel(content, "제품라벨"));

    // 외관도 규칙: 순서대로 1_,2_,… 재번호 (제품라벨은 항상 마지막)
    List<NcImagePdfImage> out = new ArrayList<NcImagePdfImage>(merged.size());
    int seq = 1;
    for (NcImagePdfImage item : merged) {
      if (item.isAutoProductLabel()) {
        out.add(NcImagePdfImage.autoProductLabel(item.getProductLabel(), seq + "_제품라벨"));
      } else {
        String suffix = labelSuffixAfterUnderscore(baseName(item.getDavPath()));
        out.add(new NcImagePdfImage(item.getDavPath(), seq + "_" + suffix));
      }
      seq++;
    }
    return out;
  }

  /** 파일명(확장자 제외). */
  static String baseName(String davPath) {
    String name = extractNameFromPath(davPath);
    int dot = name.lastIndexOf('.');
    return dot > 0 ? name.substring(0, dot) : name;
  }

  /** 파일명 앞쪽 연속 숫자. 없으면 {@link Integer#MAX_VALUE}(뒤로 정렬). */
  static int leadingNumber(String base) {
    if (base == null) {
      return Integer.MAX_VALUE;
    }
    int i = 0;
    while (i < base.length() && base.charAt(i) >= '0' && base.charAt(i) <= '9') {
      i++;
    }
    if (i == 0) {
      return Integer.MAX_VALUE;
    }
    try {
      return Integer.parseInt(base.substring(0, i));
    } catch (NumberFormatException e) {
      return Integer.MAX_VALUE;
    }
  }

  /** 외관도 라벨용 — 첫 언더바 뒤 문자열. 없으면 앞 숫자 제거한 나머지. */
  static String labelSuffixAfterUnderscore(String base) {
    if (base == null) {
      return "";
    }
    int u = base.indexOf('_');
    if (u >= 0) {
      return base.substring(u + 1);
    }
    int i = 0;
    while (i < base.length() && base.charAt(i) >= '0' && base.charAt(i) <= '9') {
      i++;
    }
    return base.substring(i);
  }

  /** 부품배치도: '제품 내부'(공백 무시) 포함 여부. */
  static boolean isPartsLayoutFirst(String base) {
    if (base == null) {
      return false;
    }
    return base.replace(" ", "").contains(PARTS_LAYOUT_FIRST_KEYWORD);
  }

  /** 한글 우선 → 영문 오름차순. */
  static int compareKoreanFirst(String a, String b) {
    int ga = startsWithHangul(a) ? 0 : 1;
    int gb = startsWithHangul(b) ? 0 : 1;
    if (ga != gb) {
      return ga - gb;
    }
    int c = a.compareToIgnoreCase(b);
    if (c != 0) {
      return c;
    }
    return a.compareTo(b);
  }

  private static boolean startsWithHangul(String s) {
    if (s == null || s.isEmpty()) {
      return false;
    }
    char c = s.charAt(0);
    return (c >= 0xAC00 && c <= 0xD7A3) // 완성형 음절
        || (c >= 0x1100 && c <= 0x11FF) // 자모
        || (c >= 0x3130 && c <= 0x318F); // 호환 자모
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

    // PDF 파일명·저장 경로는 서비스에서 신청서 naming 반영 후 결정 (여기서는 검증·정규화만)
    return new NcImagePdfPathResolution(normalized, folderPath,
        buildPathsSummaryForLog(normalized));
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

  /** resolvePaths 결과 — 경로 검증·정규화만. PDF 파일명은 서비스에서 결정. */
  public static final class NcImagePdfPathResolution {
    private final List<String> normalizedPaths;
    private final String folderPath;
    private final String pathsSummaryForLog;

    public NcImagePdfPathResolution(List<String> normalizedPaths, String folderPath,
        String pathsSummaryForLog) {
      this.normalizedPaths = normalizedPaths;
      this.folderPath = folderPath;
      this.pathsSummaryForLog = pathsSummaryForLog;
    }

    public List<String> getNormalizedPaths() {
      return normalizedPaths;
    }

    public String getFolderPath() {
      return folderPath;
    }

    public String getPathsSummaryForLog() {
      return pathsSummaryForLog;
    }
  }

  /**
   * PDF에 넣을 슬롯 1개 — 사진(DAV) 또는 자동 제품라벨(텍스트).
   * 자동 제품라벨은 비트맵이 아니라 PDF 텍스트로 그린다.
   */
  public static final class NcImagePdfImage {
    private final String davPath;
    private final String label;
    private final ProductLabelContent productLabel;

    public NcImagePdfImage(String davPath, String label) {
      this(davPath, label, null);
    }

    private NcImagePdfImage(String davPath, String label, ProductLabelContent productLabel) {
      this.davPath = davPath;
      this.label = label;
      this.productLabel = productLabel;
    }

    public static NcImagePdfImage autoProductLabel(ProductLabelContent content, String label) {
      return new NcImagePdfImage(null, label, content);
    }

    public String getDavPath() {
      return davPath;
    }

    public String getLabel() {
      return label;
    }

    public ProductLabelContent getProductLabel() {
      return productLabel;
    }

    public boolean isAutoProductLabel() {
      return productLabel != null;
    }
  }

}
