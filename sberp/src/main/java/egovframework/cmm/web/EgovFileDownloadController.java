package egovframework.cmm.web;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import egovframework.cmm.service.EgovFileMngService;
import egovframework.cmm.service.FileVO;
import egovframework.cmm.service.NextcloudDavService;
import egovframework.cmm.util.EgovUserDetailsHelper;

/**
 * 파일 다운로드를 위한 컨트롤러 클래스
 * @author 공통서비스개발팀 이삼섭
 * @since 2009.06.01
 * @version 1.0
 * @see
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.3.25  이삼섭          최초 생성
 *
 * Copyright (C) 2009 by MOPAS  All right reserved.
 * </pre>
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
	private void setDisposition(String filename, HttpServletRequest request, HttpServletResponse response) throws Exception {
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
			//throw new RuntimeException("Not supported browser");
			throw new IOException("Not supported browser");
		}

		response.setHeader("Content-Disposition", dispositionPrefix + encodedFilename);

		if ("Opera".equals(browser)) {
			response.setContentType("application/octet-stream;charset=UTF-8");
		}
	}

	@GetMapping(value="/file/fileDown.do")
	public void cvplFileDownload(@RequestParam Map<String, Object> commandMap,
	                             HttpServletRequest request,
	                             HttpServletResponse response) throws Exception {

	    String atchFileId = (String) commandMap.get("atchFileId");
	    String fileSn     = (String) commandMap.get("fileSn");

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
	    boolean isNextcloudUrl  = (streCours != null && (streCours.startsWith("http://") || streCours.startsWith("https://")));

	    try (BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream())) {

	        // ✅ 3) Nextcloud 코드값이면: WebDAV로 받아서 스트리밍
	        if (isNextcloudCode) {
	            String davPath = normalizeDavPath(fvo.getStreFileNm(), "ERP"); // rootFolder가 ERP라면
	            LOGGER.info("[DOWN-NC] davPath(raw)={}, davPath(norm)={}", fvo.getStreFileNm(), davPath);

	            try (BufferedInputStream in = new BufferedInputStream(nextcloudDavService.downloadStreamByDavPath(davPath))) {
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
	 * streFileNm이 "/ERP/..." 형태로 저장된 경우,
	 * rootFolder가 "ERP"라면 "/ERP"를 제거해 "/board/..."로 만들어서 WebDAV 기본 prefix에 붙이기 위함.
	 */
	private String normalizeDavPath(String streFileNm, String rootFolder) {
	    if (streFileNm == null) return "";
	    String p = streFileNm.trim();
	    if (!p.startsWith("/")) p = "/" + p;

	    String rootPrefix = "/" + rootFolder;
	    if (p.startsWith(rootPrefix + "/")) {
	        p = p.substring(rootPrefix.length()); // "/board/..."
	    }
	    return p; // 항상 "/"로 시작
	}


}
