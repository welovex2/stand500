package egovframework.cmm.util;

/**
 * 파일 미리보기 정책 단일 진실 공급원(SSOT).
 *
 * <p>프론트가 확장자 화이트리스트를 들고 있지 않도록, 백엔드에서 한 곳에서 결정해 내려준다.
 * 미리보기 가능 포맷이 늘거나 바뀌면 이 클래스만 수정한다.
 *
 * <p>출력 분류:
 * <ul>
 *   <li>{@code IMAGE} : 브라우저가 인라인 표시 가능한 이미지(jpg/png/gif/webp/bmp/svg). 프론트는 {@code /api/nc/preview} 사용</li>
 *   <li>{@code PDF}   : PDF.js 뷰어로 띄움. 프론트는 {@code /api/nc/preview-viewer} 사용</li>
   *   <li>{@code ONLYOFFICE} : {@code /api/nc/preview-onlyoffice?path=...} (NC ONLYOFFICE 커넥터가 저장)</li>
 *   <li>{@code NONE}  : 미리보기 미지원(다운로드만). 프론트는 "보기" 버튼 숨김</li>
 * </ul>
 */
public final class FilePreviewPolicy {

  public static final String TYPE_IMAGE = "IMAGE";
  public static final String TYPE_PDF = "PDF";
  public static final String TYPE_ONLYOFFICE = "ONLYOFFICE";
  public static final String TYPE_NONE = "NONE";

  private FilePreviewPolicy() {}

  /** 분류 결과 (불변). */
  public static final class Info {
    private final String mimeType;
    private final boolean previewable;
    private final String previewType;

    public Info(String mimeType, boolean previewable, String previewType) {
      this.mimeType = mimeType;
      this.previewable = previewable;
      this.previewType = previewType;
    }

    public String getMimeType() {
      return mimeType;
    }

    public boolean isPreviewable() {
      return previewable;
    }

    public String getPreviewType() {
      return previewType;
    }
  }

  /**
   * 파일명(확장자)으로 미리보기 정보를 결정한다. 폴더는 호출 측에서 분기한 뒤 사용.
   *
   * <p>중요: "image/* 면 무조건 IMAGE" 가 아니라 <b>브라우저가 실제로 인라인 렌더링 가능한 확장자만</b>
   * IMAGE 로 분류한다. dds/tiff/psd/heic 등은 image/* 이지만 브라우저가 못 그리므로 NONE.
   */
  public static Info classify(String fileName) {
    return classify(fileName, false);
  }

  /**
   * @param onlyofficeIntegrationEnabled {@link OnlyOfficeIntegration#isEnabled} 결과
   */
  public static Info classify(String fileName, boolean onlyofficeIntegrationEnabled) {
    String mime = detectMimeByExt(fileName);
    String ext = extractExt(fileName);

    if ("pdf".equals(ext)) {
      return new Info(mime, true, TYPE_PDF);
    }
    if (isBrowserRenderableImageExt(ext)) {
      return new Info(mime, true, TYPE_IMAGE);
    }
    if (onlyofficeIntegrationEnabled && OnlyOfficePreviewSupport.isPreviewableExtension(fileName)) {
      return new Info(mime, true, TYPE_ONLYOFFICE);
    }
    return new Info(mime, false, TYPE_NONE);
  }

  /** 모든 모던 브라우저(Chrome/Edge/Firefox/Safari)가 인라인 렌더링하는 이미지 확장자 화이트리스트. */
  private static boolean isBrowserRenderableImageExt(String ext) {
    if (ext == null) return false;
    return "jpg".equals(ext) || "jpeg".equals(ext) || "png".equals(ext) || "gif".equals(ext)
        || "webp".equals(ext) || "bmp".equals(ext) || "svg".equals(ext) || "avif".equals(ext)
        || "apng".equals(ext);
  }

  private static String extractExt(String fileName) {
    String lower = fileName == null ? "" : fileName.toLowerCase();
    int dot = lower.lastIndexOf('.');
    return dot < 0 ? "" : lower.substring(dot + 1);
  }

  /**
   * 확장자 기반 MIME 매핑. Tomcat MIME 매핑이 빠진 환경에서도 정확한 Content-Type 을 보장하기 위한
   * 백엔드 단일 진실. 새 확장자는 여기 한 곳에 추가하면 컨트롤러/리스트 응답이 모두 같은 정책을 따른다.
   */
  public static String detectMimeByExt(String fileName) {
    String ext = extractExt(fileName);

    if ("pdf".equals(ext)) return "application/pdf";

    // 브라우저가 그릴 수 있는 이미지
    if ("jpg".equals(ext) || "jpeg".equals(ext)) return "image/jpeg";
    if ("png".equals(ext)) return "image/png";
    if ("gif".equals(ext)) return "image/gif";
    if ("webp".equals(ext)) return "image/webp";
    if ("bmp".equals(ext)) return "image/bmp";
    if ("svg".equals(ext)) return "image/svg+xml";
    if ("avif".equals(ext)) return "image/avif";
    if ("apng".equals(ext)) return "image/apng";
    if ("ico".equals(ext)) return "image/x-icon";

    // 브라우저는 그리지 못하지만, 다운로드/로그에는 정확한 타입을 찍기 위해 매핑
    if ("dds".equals(ext)) return "image/vnd.ms-dds";
    if ("tif".equals(ext) || "tiff".equals(ext)) return "image/tiff";
    if ("psd".equals(ext)) return "image/vnd.adobe.photoshop";
    if ("heic".equals(ext)) return "image/heic";
    if ("heif".equals(ext)) return "image/heif";
    if ("tga".equals(ext)) return "image/x-tga";
    if ("raw".equals(ext)) return "image/x-raw";

    if ("txt".equals(ext) || "log".equals(ext)) return "text/plain;charset=UTF-8";
    if ("html".equals(ext) || "htm".equals(ext)) return "text/html;charset=UTF-8";
    if ("json".equals(ext)) return "application/json;charset=UTF-8";
    if ("xml".equals(ext)) return "application/xml;charset=UTF-8";
    if ("csv".equals(ext)) return "text/csv;charset=UTF-8";
    if ("zip".equals(ext)) return "application/zip";

    if ("mp4".equals(ext)) return "video/mp4";
    if ("mp3".equals(ext)) return "audio/mpeg";

    if ("doc".equals(ext)) return "application/msword";
    if ("docx".equals(ext))
      return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    if ("xls".equals(ext)) return "application/vnd.ms-excel";
    if ("xlsx".equals(ext))
      return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    if ("ppt".equals(ext)) return "application/vnd.ms-powerpoint";
    if ("pptx".equals(ext))
      return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
    if ("hwp".equals(ext)) return "application/x-hwp";
    if ("hwpx".equals(ext)) return "application/haansofthwpx";

    return "application/octet-stream";
  }
}
