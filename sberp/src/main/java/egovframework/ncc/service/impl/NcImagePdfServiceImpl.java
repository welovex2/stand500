package egovframework.ncc.service.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import egovframework.cmm.service.LoginVO;
import egovframework.cmm.service.SbkInfoVO;
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.cmm.util.ErpDavPathUtil;
import egovframework.cmm.util.HttpContentDispositionUtil;
import egovframework.cmm.util.ImageExifOrientationUtil;
import egovframework.cmm.util.NcImagePdfPathUtil;
import egovframework.cmm.util.NcImagePdfPathUtil.NcImagePdfImage;
import egovframework.cmm.util.NcImagePdfPathUtil.NcImagePdfPathResolution;
import egovframework.cmm.util.NcImagePdfRenderer;
import egovframework.cmm.util.ProductLabelContent;
import egovframework.ncc.dto.FileOpLogVO;
import egovframework.ncc.dto.NcImagePdfResolvedRequest;
import egovframework.ncc.dto.NcImagesToPdfRequest;
import egovframework.ncc.dto.NcImagesToPdfResult;
import egovframework.ncc.service.FileOpLogService;
import egovframework.ncc.service.NcBrowsePathResolver;
import egovframework.ncc.service.NcImagePdfException;
import egovframework.ncc.service.NcImagePdfService;
import egovframework.ncc.service.NextcloudDavService;
import egovframework.ncc.service.impl.NextcloudDavServiceImpl.DavAlreadyExistsException;
import egovframework.sbk.service.SbkService;

@Service("NcImagePdfService")
public class NcImagePdfServiceImpl implements NcImagePdfService {

  private static final String ROOT = "/ERP";
  /** 사진 1장 파일 크기 상한 (압축된 파일 바이트 기준). */
  private static final long MAX_IMAGE_FILE_BYTES = 20L * 1024L * 1024L;
  /** 사진 아래 파일명·제품라벨 본문용 (성적서 nanum = NanumGothic) */
  private static final String LABEL_FONT_WEBAPP_PATH =
      "/report/common/css/font/NanumGothic-Regular.ttf";
  /** 제품라벨 하단 인증문구 bold */
  private static final String LABEL_FONT_BOLD_WEBAPP_PATH =
      "/report/common/css/font/NanumGothic-Bold.ttf";
  /** 성적서와 동일 KC 마크 */
  private static final String KC_MARK_WEBAPP_PATH = "/report/common/img/label.jpg";

  @Resource
  private ServletContext servletContext;

  @Resource(name = "NextcloudDavService")
  private NextcloudDavService nextcloudDavService;

  @Resource(name = "SbkService")
  private SbkService sbkService;

  @Resource(name = "FileOpLogService")
  private FileOpLogService fileOpLogService;

  @Resource(name = "NcBrowsePathResolver")
  private NcBrowsePathResolver ncBrowsePathResolver;

  @Override
  public NcImagePdfResolvedRequest resolveRequest(NcImagesToPdfRequest req) throws Exception {
    List<String> rawPaths = req == null ? null : req.getPaths();

    List<String> browsablePaths = resolveBrowsePaths(rawPaths);
    NcImagePdfPathResolution resolution = NcImagePdfPathUtil.resolvePaths(browsablePaths);

    // 사진이 있는 폴더명 (외관도/부품배치도 여부 판정 및 파일명 조각)
    String folderName = NcImagePdfPathUtil.extractNameFromPath(resolution.getFolderPath());
    String sbkNo = ErpDavPathUtil.extractSbkNo(resolution.getFolderPath());

    // 외관도·부품배치도만 회사명/모델명 조회 (그 외 폴더는 폴더명.pdf)
    SbkInfoVO naming = null;
    String cmpyName = null;
    String modelName = null;
    if (NcImagePdfPathUtil.usesSbkNamingInPdfFileName(folderName)) {
      naming = sbkService.findNamingBySbkNo(sbkNo);
      cmpyName = naming == null ? null : naming.getCmpyName();
      modelName = naming == null ? null : naming.getModelName();
    }
    String pdfFileName =
        NcImagePdfPathUtil.buildPdfFileName(folderName, sbkNo, cmpyName, modelName);
    String outputDavPath =
        ErpDavPathUtil.normalizePath(resolution.getFolderPath() + "/" + pdfFileName);

    // 폴더명 규칙(외관도/부품배치도)에 따른 삽입 순서·라벨 확정
    List<NcImagePdfImage> orderedImages =
        NcImagePdfPathUtil.orderImagesForPdf(folderName, resolution.getNormalizedPaths());

    // 외관도: 선택 목록에 제품라벨이 없으면 자동 제품라벨(PDF 텍스트) 삽입
    orderedImages = ensureExteriorProductLabel(folderName, resolution.getNormalizedPaths(),
        orderedImages, naming);

    return new NcImagePdfResolvedRequest(resolution.getNormalizedPaths(),
        resolution.getFolderPath(), pdfFileName, outputDavPath,
        resolution.getPathsSummaryForLog(), orderedImages);
  }

  /**
   * 외관도이고 선택 경로에 제품라벨 이미지가 없을 때만 자동 제품라벨을 맨 아래에 넣는다.
   */
  private List<NcImagePdfImage> ensureExteriorProductLabel(String folderName,
      List<String> normalizedPaths, List<NcImagePdfImage> ordered, SbkInfoVO naming) {

    if (!NcImagePdfPathUtil.isExteriorFolder(folderName)) {
      return ordered;
    }
    if (NcImagePdfPathUtil.hasProductLabelImage(normalizedPaths)) {
      return ordered;
    }
    // 외관도는 naming 조회가 선행됨. null이어도 빈 문구 라벨로 삽입.
    return NcImagePdfPathUtil.insertExteriorAutoProductLabel(ordered,
        ProductLabelContent.from(naming));
  }

  private List<String> resolveBrowsePaths(List<String> rawPaths) throws Exception {
    if (rawPaths == null || rawPaths.isEmpty()) {
      return rawPaths;
    }
    List<String> browsablePaths = new ArrayList<String>(rawPaths.size());
    for (String raw : rawPaths) {
      browsablePaths.add(ncBrowsePathResolver.resolveBrowsePath(raw));
    }
    return browsablePaths;
  }

  @Override
  public byte[] generatePdf(NcImagePdfResolvedRequest resolved) throws Exception {
    List<NcImagePdfImage> items = resolved.getOrderedImages();
    List<BufferedImage> images = new ArrayList<BufferedImage>(items.size());
    List<ProductLabelContent> productLabels = new ArrayList<ProductLabelContent>(items.size());
    List<String> labels = new ArrayList<String>(items.size());
    for (NcImagePdfImage item : items) {
      if (item.isAutoProductLabel()) {
        images.add(null);
        productLabels.add(item.getProductLabel());
      } else {
        images.add(loadImage(item.getDavPath()));
        productLabels.add(null);
      }
      labels.add(item.getLabel());
    }
    File regular = resolveLabelFontFile();
    File bold = resolveWebappFile(LABEL_FONT_BOLD_WEBAPP_PATH);
    File kc = resolveWebappFile(KC_MARK_WEBAPP_PATH);
    return NcImagePdfRenderer.renderTwoPerPage(images, productLabels, labels, regular, bold, kc);
  }

  /** 라벨용 한글 폰트 파일. 못 찾으면 null (라벨 미표기, 이미지는 정상 출력). */
  private File resolveLabelFontFile() {
    return resolveWebappFile(LABEL_FONT_WEBAPP_PATH);
  }

  private File resolveWebappFile(String webappPath) {
    try {
      String real = servletContext == null ? null : servletContext.getRealPath(webappPath);
      if (real != null) {
        File f = new File(real);
        if (f.isFile()) {
          return f;
        }
      }
    } catch (Exception ignore) {
      // 리소스 없으면 null
    }
    return null;
  }

  @Override
  public void download(NcImagesToPdfRequest req, HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    NcImagePdfResolvedRequest resolved = resolveRequest(req);
    validateUnderSbkBase(resolved);

    Long logId = startFileOpLog("CONVERT_TO_PDF", resolved.getOutputDavPath(),
        resolved.getPathsSummaryForLog(), null, false, resolved.getPdfFileName(),
        "application/pdf", null, request);

    try {
      byte[] pdfBytes = generatePdf(resolved);
      long bytesSent = pdfBytes.length;

      writePdfBinaryResponse(response, request, resolved.getPdfFileName(), pdfBytes);

      markFileOpLogSuccess(logId, (long) pdfBytes.length, bytesSent);
    } catch (Exception e) {
      markFileOpLogFail(logId, e, null, null);
      throw e;
    }
  }

  private void writePdfBinaryResponse(HttpServletResponse response, HttpServletRequest request,
      String pdfFileName, byte[] pdfBytes) throws Exception {

    if (response.isCommitted()) {
      throw new IllegalStateException("response already committed");
    }

    response.resetBuffer();
    response.setStatus(200);
    response.setCharacterEncoding(null);
    // setContentType() 호출 시 CharacterEncodingFilter(forceEncoding)가 charset=utf-8 을 붙일 수 있음
    response.setHeader("Content-Type", "application/pdf");
    response.setHeader("X-Content-Type-Options", "nosniff");
    response.setHeader("Content-Disposition",
        HttpContentDispositionUtil.buildAttachmentDisposition(pdfFileName,
            request == null ? null : request.getHeader("User-Agent")));
    response.setContentLength(pdfBytes.length);
    response.getOutputStream().write(pdfBytes);
    response.getOutputStream().flush();
  }

  @Override
  public NcImagesToPdfResult save(NcImagesToPdfRequest req, HttpServletRequest request)
      throws Exception {

    NcImagePdfResolvedRequest resolved = resolveRequest(req);
    validateUnderSbkBase(resolved);

    boolean overwrite = req != null && req.isOverwrite();
    if (!overwrite && nextcloudDavService.existsFile(resolved.getOutputDavPath())) {
      throw new NcImagePdfException(409,
          "같은 이름의 PDF가 이미 있습니다: " + resolved.getPdfFileName());
    }

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    if (user == null) {
      throw new NcImagePdfException(401, "UNAUTHORIZED");
    }

    Long logId = startFileOpLog("CONVERT_TO_PDF_SAVE", resolved.getOutputDavPath(),
        resolved.getPathsSummaryForLog(), null, false, resolved.getPdfFileName(),
        "application/pdf", null, request);

    try {
      byte[] pdfBytes = generatePdf(resolved);
      String uploadedPath = nextcloudDavService.uploadBytes(pdfBytes,
          resolved.getOutputDavPath(), "application/pdf", overwrite);

      String sbkNo = ErpDavPathUtil.extractSbkNo(uploadedPath);
      SbkInfoVO sbk = sbkService.findBySbkNoAndProvision(sbkNo);
      if (sbk == null) {
        throw new NcImagePdfException(400, "신청서를 찾을 수 없습니다: " + sbkNo);
      }

      nextcloudDavService.insertFileDetail(sbk.getAtchFileId(), resolved.getFolderPath(),
          uploadedPath, resolved.getPdfFileName(), pdfBytes.length, user.getId());

      markFileOpLogSuccess(logId, (long) pdfBytes.length, (long) pdfBytes.length);

      int imageCount = resolved.getOrderedImages().size();
      return NcImagesToPdfResult.ok(uploadedPath, resolved.getPdfFileName(),
          resolved.getFolderPath(), NcImagePdfPathUtil.pageCountForImages(imageCount), imageCount,
          pdfBytes.length);
    } catch (DavAlreadyExistsException e) {
      markFileOpLogFail(logId, e, null, null);
      throw new NcImagePdfException(409,
          "같은 이름의 PDF가 이미 있습니다: " + resolved.getPdfFileName());
    } catch (Exception e) {
      markFileOpLogFail(logId, e, null, null);
      throw e;
    }
  }

  private BufferedImage loadImage(String path) throws Exception {
    String fileName = NcImagePdfPathUtil.extractNameFromPath(path);
    InputStream in = null;
    try {
      in = nextcloudDavService.downloadStreamByDavPath(path);
      if (in == null) {
        throw new NcImagePdfException(404, "파일을 읽을 수 없습니다: " + fileName);
      }

      // 파일 크기(압축 바이트) 기준으로 20MB 제한 — 픽셀 메모리와 무관
      byte[] fileBytes = readBytesWithLimit(in, MAX_IMAGE_FILE_BYTES, fileName);
      BufferedImage image = ImageIO.read(new ByteArrayInputStream(fileBytes));
      if (image == null) {
        throw new NcImagePdfException(422, "이미지 형식을 처리할 수 없습니다: " + fileName);
      }
      // 핸드폰 JPEG: EXIF Orientation 반영 (ImageIO 는 무시함 → 세로 사진이 가로로 눕는 문제)
      return ImageExifOrientationUtil.applyExifOrientation(image, fileBytes);
    } catch (NcImagePdfException e) {
      throw e;
    } catch (Exception e) {
      throw new NcImagePdfException(502, "파일을 읽을 수 없습니다: " + fileName);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (Exception ignore) {
          // ignore
        }
      }
    }
  }

  /** 스트림을 읽어 byte[] 로 반환. maxBytes 초과 시 거부. */
  private static byte[] readBytesWithLimit(InputStream in, long maxBytes, String fileName)
      throws Exception {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    byte[] buf = new byte[8192];
    long total = 0L;
    int n;
    while ((n = in.read(buf)) >= 0) {
      total += n;
      if (total > maxBytes) {
        throw new NcImagePdfException(400,
            "이미지 파일이 너무 큽니다(최대 20MB): " + fileName);
      }
      bos.write(buf, 0, n);
    }
    return bos.toByteArray();
  }

  private void validateUnderSbkBase(NcImagePdfResolvedRequest resolved) throws Exception {
    for (String path : resolved.getNormalizedPaths()) {
      validatePathUnderSbkBase(path);
    }
    validatePathUnderSbkBase(resolved.getOutputDavPath());
  }

  private void validatePathUnderSbkBase(String requestPath) throws Exception {
    String sbkNo = ErpDavPathUtil.extractSbkNo(requestPath);
    SbkInfoVO sbk = sbkService.findBySbkNoAndProvision(sbkNo);
    if (sbk == null) {
      throw new NcImagePdfException(400, "신청서를 찾을 수 없습니다: " + sbkNo);
    }

    String base = ErpDavPathUtil.normalizePath(ensureRootPrefix(sbk.getNcFolderPath()));
    String req = ErpDavPathUtil.normalizePath(requestPath);
    String basePrefix = base.endsWith("/") ? base : base + "/";

    if (req.equals(base) || req.startsWith(basePrefix)) {
      return;
    }

    throw new NcImagePdfException(400, "요청 경로가 신청서 기본 폴더 하위가 아닙니다.");
  }

  private static String ensureRootPrefix(String pathFromDb) {
    String x = ErpDavPathUtil.normalizePath(pathFromDb);
    if (!x.startsWith(ROOT + "/") && !x.equals(ROOT)) {
      x = ErpDavPathUtil.normalizePath(ROOT + x);
    }
    return x;
  }

  private Long startFileOpLog(String opType, String davPath, String srcPath, String dstPath,
      boolean isFolder, String fileName, String contentType, Long fileSize,
      HttpServletRequest request) {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    FileOpLogVO log = new FileOpLogVO();
    log.setUserId(user == null ? null : user.getId());
    log.setDept(user == null ? null : user.getDeptName());

    String sbkNo = ErpDavPathUtil.extractSbkNo(davPath);
    log.setSbkNo(sbkNo);
    log.setOpType(opType);
    log.setUploadSrc("A");
    log.setDavPath(davPath);
    log.setSrcPath(srcPath);
    log.setDstPath(dstPath);
    log.setIsFolder(isFolder ? "Y" : "N");
    log.setFileName(fileName);
    log.setContentType(contentType);
    log.setFileSize(fileSize == null ? 0L : fileSize);
    log.setBytesSent(0L);
    log.setClientIp(resolveClientIp(request));
    log.setUserAgent(request == null ? null : request.getHeader("User-Agent"));
    log.setResultCd("START");
    return fileOpLogService.start(log);
  }

  private void markFileOpLogSuccess(Long logId, Long fileSize, Long bytesSent) {
    fileOpLogService.success(logId, fileSize, bytesSent);
  }

  private void markFileOpLogFail(Long logId, Exception e, Long fileSize, Long bytesSent) {
    String msg = e == null ? null : e.getMessage();
    fileOpLogService.fail(logId, msg, fileSize, bytesSent);
  }

  private String resolveClientIp(HttpServletRequest request) {
    if (request == null) {
      return null;
    }
    String xff = request.getHeader("X-Forwarded-For");
    if (xff != null && xff.trim().length() > 0) {
      String[] parts = xff.split(",");
      if (parts.length > 0) {
        return parts[0].trim();
      }
      return xff.trim();
    }
    String xrip = request.getHeader("X-Real-IP");
    if (xrip != null && xrip.trim().length() > 0) {
      return xrip.trim();
    }
    return request.getRemoteAddr();
  }

}
