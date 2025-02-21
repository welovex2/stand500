package egovframework.cmm.util;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
//import java.util.HashMap;
import egovframework.cmm.service.FileVO;
import egovframework.raw.dto.PicDTO;
import egovframework.rte.fdl.idgnr.EgovIdGnrService;
import egovframework.rte.fdl.property.EgovPropertyService;
import net.coobird.thumbnailator.Thumbnailator;

/**
 * @Class Name  : EgovFileMngUtil.java
 * @Description : 메시지 처리 관련 유틸리티
 * @Modification Information
 *
 *     수정일         수정자                   수정내용
 *     -------          --------        ---------------------------
 *   2009.02.13       이삼섭                  최초 생성
 *   2011.08.31  JJY            경량환경 템플릿 커스터마이징버전 생성
 *
 * @author 공통 서비스 개발팀 이삼섭
 * @since 2009. 02. 13
 * @version 1.0
 * @see
 *
 */
@Component("EgovFileMngUtil")
public class EgovFileMngUtil {

    public static final int BUFF_SIZE = 2048;

    @Resource(name = "propertiesService")
    protected EgovPropertyService propertyService;

    @Resource(name = "egovFileIdGnrService")
    private EgovIdGnrService idgenService;

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovFileMngUtil.class);

    // 차단할 확장자 리스트
    private static final List<String> BLOCKED_EXTENSIONS = Arrays.asList(
        "exe", "dll", "js", "php", "jsp", "vbs", "bat", "sh", "jar",
        "com", "cmd", "sys", "scr", "msi", "iso", "img", "vhd", "dmg"
    );
    
    /**
     * 첨부파일에 대한 목록 정보를 취득한다.
     *
     * @param list
     * @return
     * @throws Exception
     */
    public List<FileVO> parsePicFile(List<PicDTO> files, String KeyStr, int fileKeyParam, String atchFileId, String storePath) throws Exception {
		int fileKey = fileKeyParam;
	
		String storePathString = "";
		String atchFileIdString = "";
	
		// 현재 날짜 구하기
		LocalDate now = LocalDate.now();
		// 포맷 정의
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM");
	    // 포맷 적용
	    String formatedNow = now.format(formatter);
	    
		if ("".equals(storePath) || storePath == null) {
		    storePathString = propertyService.getString("Globals.fileStorePath").concat(formatedNow).concat("/").concat(KeyStr);
		} else {
		    storePathString = propertyService.getString(storePath);
		}
	
		if ("".equals(atchFileId) || atchFileId == null) {
		    atchFileIdString = idgenService.getNextStringId();
		} else {
		    atchFileIdString = atchFileId;
		}
	
		File saveFolder = new File(storePathString);
	
		if (!saveFolder.exists() || saveFolder.isFile()) {
		    saveFolder.mkdirs();
		    Runtime.getRuntime().exec("chmod -R 755 " + saveFolder);
		}
	
		MultipartFile file;
		String filePath = "";
		List<FileVO> result  = new ArrayList<FileVO>();
		FileVO fvo;
	
		int i = 0;
		while (files.size() > i) {
	
		    file = files.get(i).getImage();
		    String orginFileName = "";
		    String fileExt = "";
		    String newName = "";
		    long _size = 0;
		    if (file != null) {

			    orginFileName = file.getOriginalFilename();
		
			    //--------------------------------------
			    // 원 파일명이 없는 경우 처리
			    // (첨부가 되지 않은 input file type)
			    //--------------------------------------
			    if ("".equals(orginFileName)) {
				continue;
			    }
			    ////------------------------------------
		
			    int index = orginFileName.lastIndexOf(".");
			    fileExt = orginFileName.substring(index + 1);
			    newName = EgovStringUtil.getTimeStamp() + fileKey + "." + fileExt;
			    
		
			    if (!"".equals(orginFileName)) {
					filePath = storePathString + File.separator + newName;
//					MultipartFile reFile = FileResize.compressImage(file, fileExt);
//					_size = reFile.getSize();
					//원본 파일 저장
//					reFile.transferTo(new File(filePath));
					//원본파일.transferTo(저장할파일);
					
	                //섬네일 생성 (섬네일 파일 이름은 중간에 "s_"로 시작하도록)
	                File thumbnailFile = new File(filePath);

	                BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
	                int newWidth = bufferedImage.getWidth();
	                int newHeight = bufferedImage.getHeight();
	                
	                // 350키로바이트 이하는 변환하지 않음
	                if (file.getSize() <= 358400) {
	                  
	                  _size = file.getSize();
	                  // 사진이 작으면 원본 그대로 저장
	                  file.transferTo(new File(filePath));
	                  
	                } else {
	                  
    	                if (file.getSize() < 512000) {
    	                  // 변경할 가로 길이
    	                  newWidth = (int) (bufferedImage.getWidth() * 0.5);
    	                  // 기존 이미지 비율을 유지하여 세로 길이 설정
    	                  newHeight = (bufferedImage.getHeight() * newWidth) / bufferedImage.getWidth();
    	                }
    	                else if (file.getSize() < 2097152) {
    	                  // 변경할 가로 길이
    	                  newWidth = (int) (bufferedImage.getWidth() * 0.4);
    	                  // 기존 이미지 비율을 유지하여 세로 길이 설정
    	                  newHeight = (bufferedImage.getHeight() * newWidth) / bufferedImage.getWidth();
    	                }
    	                else {
    	                  // 변경할 가로 길이
    	                  newWidth = (int) (bufferedImage.getWidth() * 0.3);
    	                  // 기존 이미지 비율을 유지하여 세로 길이 설정
    	                  newHeight = (bufferedImage.getHeight() * newWidth) / bufferedImage.getWidth();
    	                }
    	                
    	                File orgFile = new File(orginFileName);
    	                file.transferTo(orgFile);
    	                
    	                Thumbnailator.createThumbnail(orgFile, thumbnailFile, newWidth, newHeight);
    	                orgFile.delete();
    	                
    	                _size = thumbnailFile.length();
    	                
	                }
			    }
		    }
		    
		    fvo = new FileVO();
		    fvo.setFileExtsn(fileExt);
		    fvo.setFileStreCours(storePathString);
		    fvo.setFileMg(Long.toString(_size));
		    fvo.setOrignlFileNm(orginFileName);
		    fvo.setStreFileNm(newName);
		    fvo.setAtchFileId(atchFileIdString);
		    fvo.setFileSn(String.valueOf(fileKey));
		    fvo.setFileCn(files.get(i).getTitle());
		    fvo.setFileMemo(files.get(i).getMode());
		    result.add(fvo);
	
		    fileKey++;i++;
		}
	
		return result;
    }

    /**
     * 첨부파일에 대한 목록 정보를 취득한다.
     *
     * @param files
     * @return
     * @throws Exception
     */
    public List<FileVO> parseFile(List<MultipartFile> files, String KeyStr, int fileKeyParam, String atchFileId, String storePath) throws Exception {
		int fileKey = fileKeyParam;
	
		String storePathString = "";
		String atchFileIdString = "";
	
		// 현재 날짜 구하기
		LocalDate now = LocalDate.now();
		// 포맷 정의
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM");
	    // 포맷 적용
	    String formatedNow = now.format(formatter);
	    
		if ("".equals(storePath) || storePath == null) {
		    storePathString = propertyService.getString("Globals.fileStorePath").concat(formatedNow).concat("/").concat(KeyStr);
		} else {
		    storePathString = storePath;
		}
	
		if ("".equals(atchFileId) || atchFileId == null) {
		    atchFileIdString = idgenService.getNextStringId();
		} else {
		    atchFileIdString = atchFileId;
		}
	
		File saveFolder = new File(storePathString);
	
		if (!saveFolder.exists() || saveFolder.isFile()) {
		    saveFolder.mkdirs();
		    Runtime.getRuntime().exec("chmod -R 755 " + saveFolder);
		}
	
		MultipartFile file;
		String filePath = "";
		List<FileVO> result  = new ArrayList<FileVO>();
		FileVO fvo;
	
		int i = 0;
		while (files.size() > i) {
	
		    file = files.get(i);
		    String orginFileName = file.getOriginalFilename();
	
		    //--------------------------------------
		    // 원 파일명이 없는 경우 처리
		    // (첨부가 되지 않은 input file type)
		    //--------------------------------------
		    if ("".equals(orginFileName)) {
			continue;
		    }
		    ////------------------------------------
	
		    int index = orginFileName.lastIndexOf(".");
		    String fileExt = orginFileName.substring(index + 1);
		    String newName = EgovStringUtil.getTimeStamp() + fileKey;
		    long _size = file.getSize();
	
		    if (!"".equals(orginFileName)) {
				filePath = storePathString + File.separator + newName;
				file.transferTo(new File(filePath));
		    }
		    fvo = new FileVO();
		    fvo.setFileExtsn(fileExt);
		    fvo.setFileStreCours(storePathString);
		    fvo.setFileMg(Long.toString(_size));
		    fvo.setOrignlFileNm(orginFileName);
		    fvo.setStreFileNm(newName);
		    fvo.setAtchFileId(atchFileIdString);
		    fvo.setFileSn(String.valueOf(fileKey));
	
		    //writeFile(file, newName, storePathString);
		    result.add(fvo);
	
		    fileKey++;i++;
		}
	
		return result;
    }
 
    /**
     * 첨부파일에 대한 목록 정보를 취득한다. (한건)
     *
     * @param files
     * @return
     * @throws Exception
     */
    public FileVO parseFile(MultipartFile file, String KeyStr, int fileKeyParam, String atchFileId, String storePath) throws Exception {
	int fileKey = fileKeyParam;

	String storePathString = "";
	String atchFileIdString = "";

	// 현재 날짜 구하기
	LocalDate now = LocalDate.now();
	// 포맷 정의
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM");
    // 포맷 적용
    String formatedNow = now.format(formatter);
    
	if ("".equals(storePath) || storePath == null) {
	    storePathString = propertyService.getString("Globals.fileStorePath").concat(formatedNow).concat("/").concat(KeyStr);
	} else {
	    storePathString = propertyService.getString(storePath);
	}

	if ("".equals(atchFileId) || atchFileId == null) {
	    atchFileIdString = idgenService.getNextStringId();
	} else {
	    atchFileIdString = atchFileId;
	}

	File saveFolder = new File(storePathString);

	if (!saveFolder.exists() || saveFolder.isFile()) {
	    saveFolder.mkdirs();
	    Runtime.getRuntime().exec("chmod -R 755 " + saveFolder);
	}

	String filePath = "";
	FileVO fvo = new FileVO();

	if (!ObjectUtils.isEmpty(file)) {

	    String orginFileName = file.getOriginalFilename();

	    int index = orginFileName.lastIndexOf(".");
	    String fileExt = orginFileName.substring(index + 1);
	    String newName = EgovStringUtil.getTimeStamp() + fileKey;
	    long _size = file.getSize();

        // 차단된 확장자 체크
        if (BLOCKED_EXTENSIONS.contains(fileExt)) {
            throw new Exception("🚨 차단된 확장자입니다: " + fileExt);
        }
        
	    if (!"".equals(orginFileName)) {
			filePath = storePathString + File.separator + newName;
			file.transferTo(new File(filePath));
	    }
	    
	    fvo = new FileVO();
	    fvo.setFileExtsn(fileExt);
	    fvo.setFileStreCours(storePathString);
	    fvo.setFileMg(Long.toString(_size));
	    fvo.setOrignlFileNm(orginFileName);
	    fvo.setStreFileNm(newName);
	    fvo.setAtchFileId(atchFileIdString);
	    fvo.setFileSn(String.valueOf(fileKey));
	}

		return fvo;
    }
    
    /**
     * 첨부파일을 서버에 저장한다.
     *
     * @param file
     * @param newName
     * @param stordFilePath
     * @throws Exception
     */
    protected void writeUploadedFile(MultipartFile file, String newName, String stordFilePath) throws Exception {
	InputStream stream = null;
	OutputStream bos = null;
	String stordFilePathReal = (stordFilePath==null?"":stordFilePath).replaceAll("..","");
	try {
	    stream = file.getInputStream();
	    File cFile = new File(stordFilePathReal);

	    if (!cFile.isDirectory()) {
		boolean _flag = cFile.mkdir();
		if (!_flag) {
		    throw new IOException("Directory creation Failed ");
		}
	    }

	    bos = new FileOutputStream(stordFilePathReal + File.separator + newName);

	    int bytesRead = 0;
	    byte[] buffer = new byte[BUFF_SIZE];

	    while ((bytesRead = stream.read(buffer, 0, BUFF_SIZE)) != -1) {
		bos.write(buffer, 0, bytesRead);
	    }
	} catch (FileNotFoundException fnfe) {
		LOGGER.debug("fnfe: {}", fnfe);
	} catch (IOException ioe) {
		LOGGER.debug("ioe: {}", ioe);
	} catch (Exception e) {
		LOGGER.debug("e: {}", e);
	} finally {
	    if (bos != null) {
		try {
		    bos.close();
		} catch (Exception ignore) {
			LOGGER.debug("IGNORED: {}", ignore.getMessage());
		}
	    }
	    if (stream != null) {
		try {
		    stream.close();
		} catch (Exception ignore) {
			LOGGER.debug("IGNORED: {}", ignore.getMessage());
		}
	    }
	}
    }

    /**
     * 서버의 파일을 다운로드한다.
     *
     * @param request
     * @param response
     * @throws Exception
     */
    public static void downFile(HttpServletRequest request, HttpServletResponse response) throws Exception {

    String downFileName = EgovStringUtil.isNullToString(request.getAttribute("downFile")).replaceAll("..","");
    String orgFileName = EgovStringUtil.isNullToString(request.getAttribute("orgFileName")).replaceAll("..","");

	/*if ((String)request.getAttribute("downFile") == null) {
	    downFileName = "";
	} else {
	    downFileName = EgovStringUtil.isNullToString(request.getAttribute("downFile"));
	}*/

	/*if ((String)request.getAttribute("orgFileName") == null) {
	    orgFileName = "";
	} else {
	    orgFileName = (String)request.getAttribute("orginFile");
	}*/

	File file = new File(downFileName);

	if (!file.exists()) {
	    throw new FileNotFoundException(downFileName);
	}

	if (!file.isFile()) {
	    throw new FileNotFoundException(downFileName);
	}

	byte[] b = new byte[BUFF_SIZE]; //buffer size 2K.
    String fName = (new String(orgFileName.getBytes(), "UTF-8")).replaceAll("\r\n","");
	response.setContentType("application/x-msdownload");
	response.setHeader("Content-Disposition:", "attachment; filename=" + fName);
	response.setHeader("Content-Transfer-Encoding", "binary");
	response.setHeader("Pragma", "no-cache");
	response.setHeader("Expires", "0");

	BufferedInputStream fin = null;
	BufferedOutputStream outs = null;

	try {
		fin = new BufferedInputStream(new FileInputStream(file));
	    outs = new BufferedOutputStream(response.getOutputStream());
	    int read = 0;

		while ((read = fin.read(b)) != -1) {
		    outs.write(b, 0, read);
		}
	} finally {
	    if (outs != null) {
			try {
			    outs.close();
			} catch (Exception ignore) {
				LOGGER.debug("IGNORED: {}", ignore.getMessage());
			}
		    }
		    if (fin != null) {
			try {
			    fin.close();
			} catch (Exception ignore) {
				LOGGER.debug("IGNORED: {}", ignore.getMessage());
			}
		    }
		}
    }

    /**
     * 첨부로 등록된 파일을 서버에 업로드한다.
     *
     * @param file
     * @return
     * @throws Exception

    public static HashMap<String, String> uploadFile(MultipartFile file) throws Exception {

	HashMap<String, String> map = new HashMap<String, String>();
	//Write File 이후 Move File????
	String newName = "";
	String stordFilePath = EgovProperties.getProperty("Globals.fileStorePath");
	String orginFileName = file.getOriginalFilename();

	int index = orginFileName.lastIndexOf(".");
	//String fileName = orginFileName.substring(0, _index);
	String fileExt = orginFileName.substring(index + 1);
	long size = file.getSize();

	//newName 은 Naming Convention에 의해서 생성
	newName = EgovStringUtil.getTimeStamp() + "." + fileExt;
	writeFile(file, newName, stordFilePath);
	//storedFilePath는 지정
	map.put(Globals.ORIGIN_FILE_NM, orginFileName);
	map.put(Globals.UPLOAD_FILE_NM, newName);
	map.put(Globals.FILE_EXT, fileExt);
	map.put(Globals.FILE_PATH, stordFilePath);
	map.put(Globals.FILE_SIZE, String.valueOf(size));

	return map;
    }
*/
    /**
     * 파일을 실제 물리적인 경로에 생성한다.
     *
     * @param file
     * @param newName
     * @param stordFilePath
     * @throws Exception
     */
    protected static void writeFile(MultipartFile file, String newName, String stordFilePath) throws Exception {
	InputStream stream = null;
	OutputStream bos = null;
	newName = EgovStringUtil.isNullToString(newName).replaceAll("..", "");
	stordFilePath = EgovStringUtil.isNullToString(stordFilePath).replaceAll("..", "");
	try {
	    stream = file.getInputStream();
	    File cFile = new File(stordFilePath);

	    if (!cFile.isDirectory())
		cFile.mkdir();

	    bos = new FileOutputStream(stordFilePath + File.separator + newName);

	    int bytesRead = 0;
	    byte[] buffer = new byte[BUFF_SIZE];

	    while ((bytesRead = stream.read(buffer, 0, BUFF_SIZE)) != -1) {
		bos.write(buffer, 0, bytesRead);
	    }
	} catch (FileNotFoundException fnfe) {
		LOGGER.debug("fnfe: {}", fnfe);
	} catch (IOException ioe) {
		LOGGER.debug("ioe: {}", ioe);
	} catch (Exception e) {
		LOGGER.debug("e: {}", e);
	} finally {
	    if (bos != null) {
		try {
		    bos.close();
		} catch (Exception ignore) {
			LOGGER.debug("IGNORED: {}", ignore.getMessage());
		}
	    }
	    if (stream != null) {
		try {
		    stream.close();
		} catch (Exception ignore) {
			LOGGER.debug("IGNORED: {}", ignore.getMessage());
		}
	    }
	}
    }

    /**
     * 서버 파일에 대하여 다운로드를 처리한다.
     *
     * @param response
     * @param streFileNm
     *            : 파일저장 경로가 포함된 형태
     * @param orignFileNm
     * @throws Exception
     */
    public void downFile(HttpServletResponse response, String streFileNm, String orignFileNm) throws Exception {
    //	String downFileName = EgovStringUtil.isNullToString(request.getAttribute("downFile")).replaceAll("..","");
    //	String orgFileName = EgovStringUtil.isNullToString(request.getAttribute("orgFileName")).replaceAll("..","");
    String downFileName = EgovStringUtil.isNullToString(streFileNm).replaceAll("..","");
	String orgFileName = EgovStringUtil.isNullToString(orignFileNm).replaceAll("..","");

	File file = new File(downFileName);
	//log.debug(this.getClass().getName()+" downFile downFileName "+downFileName);
	//log.debug(this.getClass().getName()+" downFile orgFileName "+orgFileName);

	if (!file.exists()) {
	    throw new FileNotFoundException(downFileName);
	}

	if (!file.isFile()) {
	    throw new FileNotFoundException(downFileName);
	}

	//byte[] b = new byte[BUFF_SIZE]; //buffer size 2K.
	int fSize = (int)file.length();
	if (fSize > 0) {
	    BufferedInputStream in = null;

	    try {
		in = new BufferedInputStream(new FileInputStream(file));

    	    	String mimetype = "text/html"; //"application/x-msdownload"

    	    	response.setBufferSize(fSize);
		response.setContentType(mimetype);
		response.setHeader("Content-Disposition:", "attachment; filename=" + orgFileName);
		response.setContentLength(fSize);
		//response.setHeader("Content-Transfer-Encoding","binary");
		//response.setHeader("Pragma","no-cache");
		//response.setHeader("Expires","0");
		FileCopyUtils.copy(in, response.getOutputStream());
	    } finally {
		if (in != null) {
		    try {
			in.close();
		    } catch (Exception ignore) {
		    	LOGGER.debug("IGNORED: {}", ignore.getMessage());
		    }
		}
	    }
	    response.getOutputStream().flush();
	    response.getOutputStream().close();
	}

	/*
	String uploadPath = propertiesService.getString("fileDir");

	File uFile = new File(uploadPath, requestedFile);
	int fSize = (int) uFile.length();

	if (fSize > 0) {
	    BufferedInputStream in = new BufferedInputStream(new FileInputStream(uFile));

	    String mimetype = "text/html";

	    response.setBufferSize(fSize);
	    response.setContentType(mimetype);
	    response.setHeader("Content-Disposition", "attachment; filename=\""
					+ requestedFile + "\"");
	    response.setContentLength(fSize);

	    FileCopyUtils.copy(in, response.getOutputStream());
	    in.close();
	    response.getOutputStream().flush();
	    response.getOutputStream().close();
	} else {
	    response.setContentType("text/html");
	    PrintWriter printwriter = response.getWriter();
	    printwriter.println("<html>");
	    printwriter.println("<br><br><br><h2>Could not get file name:<br>" + requestedFile + "</h2>");
	    printwriter.println("<br><br><br><center><h3><a href='javascript: history.go(-1)'>Back</a></h3></center>");
	    printwriter.println("<br><br><br>&copy; webAccess");
	    printwriter.println("</html>");
	    printwriter.flush();
	    printwriter.close();
	}
	//*/


	/*
	response.setContentType("application/x-msdownload");
	response.setHeader("Content-Disposition:", "attachment; filename=" + new String(orgFileName.getBytes(),"UTF-8" ));
	response.setHeader("Content-Transfer-Encoding","binary");
	response.setHeader("Pragma","no-cache");
	response.setHeader("Expires","0");

	BufferedInputStream fin = new BufferedInputStream(new FileInputStream(file));
	BufferedOutputStream outs = new BufferedOutputStream(response.getOutputStream());
	int read = 0;

	while ((read = fin.read(b)) != -1) {
	    outs.write(b,0,read);
	}
	log.debug(this.getClass().getName()+" BufferedOutputStream Write Complete!!! ");

	outs.close();
    	fin.close();
	//*/
    }
}
