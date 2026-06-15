package egovframework.cmm.web;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import egovframework.cmm.service.EgovFileMngService;
import egovframework.cmm.service.FileVO;
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.ncc.service.NextcloudDavService;
import net.coobird.thumbnailator.Thumbnails;

/**
 * 파일 다운로드를 위한 컨트롤러 클래스
 * 
 * @author 공통서비스개발팀 이삼섭
 * @since 2009.06.01
 * @version 1.0
 * @see
 *
 *      <pre>
 * << 개정이력(Modification Information) >>
 *
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.3.25  이삼섭          최초 생성
 *
 * Copyright (C) 2009 by MOPAS  All right reserved.
 *      </pre>
 */
@Controller
public class EgovFileDownloadController {

  @Resource(name = "EgovFileMngService")
  private EgovFileMngService fileService;

  @Resource(name = "NextcloudDavService")
  private NextcloudDavService nextcloudDavService;

  private static final Logger LOGGER = LoggerFactory.getLogger(EgovFileDownloadController.class);

  /**
   * 브라우저 구분 얻기.
   *
   * @param request
   * @return
   */
  private String getBrowser(HttpServletRequest request) {
    String header = request.getHeader("User-Agent");
    if (header.indexOf("MSIE") > -1) {
      return "MSIE";
    } else if (header.indexOf("Trident") > -1) { // IE11 문자열 깨짐 방지
      return "Trident";
    } else if (header.indexOf("Chrome") > -1) {
      return "Chrome";
    } else if (header.indexOf("Opera") > -1) {
      return "Opera";
    }
    return "Firefox";
  }

  /**
   * Disposition 지정하기.
   *
   * @param filename
   * @param request
   * @param response
   * @throws Exception
   */
  private void setDisposition(String filename, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    String browser = getBrowser(request);

    String dispositionPrefix = "attachment; filename=";
    String encodedFilename = null;

    if (browser.equals("MSIE")) {
      encodedFilename = URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");
    } else if (browser.equals("Trident")) { // IE11 문자열 깨짐 방지
      encodedFilename = URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");
    } else if (browser.equals("Firefox")) {
      encodedFilename = "\"" + new String(filename.getBytes("UTF-8"), "8859_1") + "\"";
    } else if (browser.equals("Opera")) {
      encodedFilename = "\"" + new String(filename.getBytes("UTF-8"), "8859_1") + "\"";
    } else if (browser.equals("Chrome")) {
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < filename.length(); i++) {
        char c = filename.charAt(i);
        if (c > '~') {
          sb.append(URLEncoder.encode("" + c, "UTF-8"));
        } else {
          sb.append(c);
        }
      }
      encodedFilename = sb.toString();
    } else {
      // throw new RuntimeException("Not supported browser");
      throw new IOException("Not supported browser");
    }

    response.setHeader("Content-Disposition", dispositionPrefix + encodedFilename);

    if ("Opera".equals(browser)) {
      response.setContentType("application/octet-stream;charset=UTF-8");
    }
  }

  @GetMapping(value = "/file/fileDown.do")
  public void cvplFileDownload(@RequestParam Map<String, Object> commandMap,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    String atchFileId = (String) commandMap.get("atchFileId");
    String fileSn = (String) commandMap.get("fileSn");

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    if (isAuthenticated == null || !isAuthenticated) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    // 1) 파일 정보 조회
    FileVO q = new FileVO();
    q.setAtchFileId(atchFileId);
    q.setFileSn(fileSn);

    FileVO fvo = fileService.selectFileInf(q);
    if (fvo == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    LOGGER.info("[DOWN] atchFileId={}, fileSn={}, fileStreCours={}, streFileNm={}, orignl={}",
        atchFileId, fileSn, fvo.getFileStreCours(), fvo.getStreFileNm(), fvo.getOrignlFileNm());

    // 2) 다운로드 헤더 (원본 파일명)
    response.setContentType("application/octet-stream");
    setDisposition(fvo.getOrignlFileNm(), request, response);

    String streCours = fvo.getFileStreCours();
    boolean isNextcloudCode = "NEXTCLOUD_DAV".equals(streCours);
    boolean isNextcloudUrl = (streCours != null
        && (streCours.startsWith("http://") || streCours.startsWith("https://")));

    try (BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream())) {

      // ✅ 3) Nextcloud 코드값이면: WebDAV로 받아서 스트리밍
      if (isNextcloudCode) {
        String davPath = normalizeDavPath(fvo.getStreFileNm(), "ERP"); // rootFolder가 ERP라면
        LOGGER.info("[DOWN-NC] davPath(raw)={}, davPath(norm)={}", fvo.getStreFileNm(), davPath);

        try (BufferedInputStream in =
            new BufferedInputStream(nextcloudDavService.downloadStreamByDavPath(davPath))) {
          FileCopyUtils.copy(in, out);
          out.flush();
        }
        return;
      }

      // ✅ 4) Nextcloud public URL이면: URL로 받아서 스트리밍(옵션)
      if (isNextcloudUrl) {
        java.net.URL url = new java.net.URL(streCours);
        LOGGER.info("[DOWN-NC] publicUrl={}", streCours);

        try (BufferedInputStream in = new BufferedInputStream(url.openStream())) {
          FileCopyUtils.copy(in, out);
          out.flush();
        }
        return;
      }

      // ✅ 5) 그 외: 로컬 파일
      File uFile = new File(fvo.getFileStreCours(), fvo.getStreFileNm());
      if (!uFile.exists() || uFile.length() <= 0) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }

      try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(uFile))) {
        FileCopyUtils.copy(in, out);
        out.flush();
      }

    } catch (Exception ex) {
      // 무시하지 말고 원인 로그 남겨야 디버깅 됨
      LOGGER.error("[DOWN] download failed. atchFileId={}, fileSn={}", atchFileId, fileSn, ex);
      // 필요시 에러 응답
      // response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * 성적서(report.do) 노출용 이미지 리사이즈 프록시.
   *
   * <p>원본 파일은 Nextcloud에 그대로 두고, 요청 시점에 메모리에서 축소(긴 변 기준 {@code w} 픽셀, 비율 유지,
   * 업스케일 없음)해 작은 이미지로 응답한다. 성적서 화면을 브라우저에서 PDF로 저장할 때 임베드되는 이미지 용량을 줄이기 위한 용도.
   *
   * <p>이미지의 원본은 이미 공개 공유 토큰 URL로 노출되는 영역과 동일하므로 별도 인증 없이 접근 가능하다.
   *
   * @param encodedPath 원본 파일 DAV 경로의 Base64URL(무패딩) 인코딩 값
   *        (예: {@code /ERP/2026/02/SB.../01.제품사진/a.jpg}를 인코딩한 문자열)
   * @param maxSize 긴 변 최대 픽셀 (기본 1280)
   * @param quality JPEG 품질 0~1 (기본 0.6, 낮을수록 용량 작음)
   */
  @GetMapping("/file/reportImage.do")
  public void reportImage(@RequestParam("path") String encodedPath,
      @RequestParam(value = "w", required = false, defaultValue = "1280") int maxSize,
      @RequestParam(value = "q", required = false, defaultValue = "0.6") float quality,
      HttpServletResponse response) {

    if (encodedPath == null || encodedPath.trim().isEmpty()) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    // path는 Base64URL(무패딩)로 전달됨 → DAV 경로로 디코딩
    String davPath;
    try {
      davPath = new String(Base64.getUrlDecoder().decode(encodedPath.trim()), StandardCharsets.UTF_8);
    } catch (IllegalArgumentException ex) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    // 경로 검증 (디렉터리 트래버설 차단)
    if (davPath.trim().isEmpty() || davPath.contains("..")) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    String lower = davPath.toLowerCase();
    boolean isPng = lower.endsWith(".png");
    String outputFormat = isPng ? "png" : "jpg";
    int size = maxSize <= 0 ? 1280 : maxSize;
    // 품질 범위 보정 (0.1 ~ 1.0)
    float jpegQuality = quality;
    if (jpegQuality <= 0f || jpegQuality > 1f) {
      jpegQuality = 0.6f;
    }

    try (InputStream in = nextcloudDavService.downloadStreamByDavPath(davPath)) {

      BufferedImage src = ImageIO.read(in);
      if (src == null) {
        // 이미지로 디코딩 불가
        response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        return;
      }

      int longSide = Math.max(src.getWidth(), src.getHeight());

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      Thumbnails.Builder<BufferedImage> builder = Thumbnails.of(src);

      if (longSide > size) {
        // 긴 변을 size에 맞춰 비율 유지 축소 (업스케일 없음)
        builder.size(size, size).keepAspectRatio(true);
      } else {
        // 이미 충분히 작으면 크기 유지(JPEG는 품질 압축만)
        builder.scale(1.0d);
      }

      // PNG는 무손실이라 품질 옵션 미적용
      if (!isPng) {
        builder.outputQuality(jpegQuality);
      }

      builder.outputFormat(outputFormat).toOutputStream(baos);
      byte[] bytes = baos.toByteArray();

      response.setContentType(isPng ? "image/png" : "image/jpeg");
      response.setContentLength(bytes.length);
      // 동일 이미지 반복 요청 캐시 (1일)
      response.setHeader("Cache-Control", "public, max-age=86400");

      try (OutputStream out = response.getOutputStream()) {
        out.write(bytes);
        out.flush();
      }

    } catch (Exception e) {
      LOGGER.error("[REPORT-IMG] resize failed. path={}, w={}", davPath, maxSize, e);
      if (!response.isCommitted()) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
    }
  }

  /**
   * streFileNm이 "/ERP/..." 형태로 저장된 경우, rootFolder가 "ERP"라면 "/ERP"를 제거해 "/board/..."로 만들어서 WebDAV 기본
   * prefix에 붙이기 위함.
   */
  private String normalizeDavPath(String streFileNm, String rootFolder) {
    if (streFileNm == null)
      return "";
    String p = streFileNm.trim();
    if (!p.startsWith("/"))
      p = "/" + p;

    String rootPrefix = "/" + rootFolder;
    if (p.startsWith(rootPrefix + "/")) {
      p = p.substring(rootPrefix.length()); // "/board/..."
    }
    return p; // 항상 "/"로 시작
  }


}
