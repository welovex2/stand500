package egovframework.ncc.service.impl;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import egovframework.cmm.service.LoginVO;
import egovframework.cmm.service.SbkInfoVO;
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.cmm.util.ErpDavPathUtil;
import egovframework.cmm.util.HttpContentDispositionUtil;
import egovframework.cmm.util.NcImagePdfPathUtil;
import egovframework.cmm.util.NcImagePdfPathUtil.NcImagePdfPathResolution;
import egovframework.cmm.util.NcImagePdfRenderer;
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
  private static final long MAX_IMAGE_BYTES = 50L * 1024L * 1024L;

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
    // PDF 파일명: browse 변환 전 원본 path 기준 — 화면에서 보이는 마지막 폴더명
    String pdfFileName = NcImagePdfPathUtil.resolvePdfFileNameFromImagePaths(rawPaths);

    List<String> browsablePaths = resolveBrowsePaths(rawPaths);
    NcImagePdfPathResolution resolution = NcImagePdfPathUtil.resolvePaths(browsablePaths);
    String outputDavPath =
        ErpDavPathUtil.normalizePath(resolution.getFolderPath() + "/" + pdfFileName);

    return new NcImagePdfResolvedRequest(resolution.getNormalizedPaths(),
        resolution.getFolderPath(), pdfFileName, outputDavPath,
        resolution.getPathsSummaryForLog());
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
    List<BufferedImage> images = loadImagesInOrder(resolved.getNormalizedPaths());
    return NcImagePdfRenderer.renderTwoPerPage(images);
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

      int imageCount = resolved.getNormalizedPaths().size();
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

  private List<BufferedImage> loadImagesInOrder(List<String> paths) throws Exception {
    List<BufferedImage> images = new ArrayList<BufferedImage>();
    for (String path : paths) {
      String fileName = NcImagePdfPathUtil.extractNameFromPath(path);
      InputStream in = null;
      try {
        in = nextcloudDavService.downloadStreamByDavPath(path);
        if (in == null) {
          throw new NcImagePdfException(404, "파일을 읽을 수 없습니다: " + fileName);
        }
        BufferedImage image = ImageIO.read(in);
        if (image == null) {
          throw new NcImagePdfException(422, "이미지 형식을 처리할 수 없습니다: " + fileName);
        }
        long approxBytes = (long) image.getWidth() * image.getHeight() * 4L;
        if (approxBytes > MAX_IMAGE_BYTES) {
          throw new NcImagePdfException(400, "이미지가 너무 큽니다: " + fileName);
        }
        images.add(image);
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
    return images;
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
