package egovframework.cmm.service.impl;

import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.transaction.Transactional;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import egovframework.cmm.service.EgovFileMngService;
import egovframework.cmm.service.FileMapper;
import egovframework.cmm.service.FileVO;
import egovframework.cmm.service.LoginVO;
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.ncc.dto.FileDetailUpdateVO;
import egovframework.ncc.dto.FileOpLogVO;
import egovframework.ncc.dto.FolderMetaVO;
import egovframework.ncc.service.FileOpLogService;
import egovframework.ncc.service.NextcloudDavService;
import egovframework.rte.fdl.cmmn.EgovAbstractServiceImpl;
import lombok.extern.slf4j.Slf4j;

/**
 * @Class Name : EgovFileMngServiceImpl.java
 * @Description : 파일정보의 관리를 위한 구현 클래스
 * @Modification Information
 *
 *               수정일 수정자 수정내용 ------- ------- ------------------- 2009. 3. 25. 이삼섭 최초생성
 *
 * @author 공통 서비스 개발팀 이삼섭
 * @since 2009. 3. 25.
 * @version
 * @see
 *
 */
@Service("EgovFileMngService")
@Slf4j
public class EgovFileMngServiceImpl extends EgovAbstractServiceImpl implements EgovFileMngService {

  @Autowired
  private FileMapper fileMapper;

  @Autowired
  private NextcloudDavService nextcloudDavService;

  @Autowired
  private FileOpLogService fileOpLogService;

  private static final String STRE_COURS_NEXTCLOUD = "NEXTCLOUD_DAV";
  private static final Pattern SBK_NO_PATTERN = Pattern.compile("(?i)(SB\\d{2}-[A-Z]\\d+)");

  /**
   * 여러 개의 파일을 삭제한다.
   *
   * @see egovframework.com.cmm.service.EgovFileMngService#deleteFileInfs(java.util.List)
   */
  @Override
  public void deleteFileInfs(List<?> fileList) throws Exception {
    Iterator<?> iter = fileList.iterator();
    FileVO vo;
    while (iter.hasNext()) {
      vo = (FileVO) iter.next();

      fileMapper.insertFileDetail(vo);
    }

  }

  /**
   * 하나의 파일에 마스터정보를 등록한다.
   *
   * @see egovframework.com.cmm.service.EgovFileMngService#insertFileInf(egovframework.com.cmm.service.FileVO)
   */
  @Override
  public String insertFileMaster(FileVO fvo) throws Exception {

    String atchFileId = fvo.getAtchFileId();

    fileMapper.insertFileMaster(fvo);

    return atchFileId;
  }

  /**
   * 하나의 파일에 대한 정보(속성 및 상세)를 등록한다. ERP에서만 사용. 파일모달에서 사용 X
   *
   * @see egovframework.com.cmm.service.EgovFileMngService#insertFileInf(egovframework.com.cmm.service.FileVO)
   */
  @Override
  public String insertFileInf(FileVO fvo) throws Exception {

    String atchFileId = fvo.getAtchFileId();

    fvo.setUploadSrc("E");
    Long logId = startErpFileOpLog("UPLOAD", fvo);
    try {
      fileMapper.insertFileMaster(fvo);
      fileMapper.insertFileDetail(fvo);
      markSuccess(logId, parseLongSafe(fvo.getFileMg()), null);
    } catch (Exception e) {
      markFail(logId, e, parseLongSafe(fvo.getFileMg()), null);
      throw e;
    }

    return atchFileId;
  }

  /**
   * 여러 개의 파일에 대한 정보(속성 및 상세)를 등록한다. ERP 사용 / 파일모달 X
   *
   * @see egovframework.com.cmm.service.EgovFileMngService#insertFileInfs(java.util.List)
   */
  @Override
  public String insertFileInfs(List<?> fileList) throws Exception {
    String atchFileId = "";

    if (fileList.size() != 0) {

      FileVO vo = (FileVO) fileList.get(0);
      atchFileId = vo.getAtchFileId();

      fileMapper.insertFileMaster(vo);

      Iterator<?> iter = fileList.iterator();
      while (iter.hasNext()) {
        vo = (FileVO) iter.next();

        Long logId = startErpFileOpLog("UPLOAD", vo);
        try {
          fileMapper.insertFileDetail(vo);
          markSuccess(logId, parseLongSafe(vo.getFileMg()), null);
        } catch (Exception e) {
          markFail(logId, e, parseLongSafe(vo.getFileMg()), null);
          throw e;
        }
      }

    }
    if (atchFileId == "") {
      atchFileId = null;
    }
    return atchFileId;
  }

  /**
   * ERP에서만 호출할것. 파일모달 호출 X
   */
  @Override
  public String insertFileInfs(List<FileVO> fileList, String userId) throws Exception {
    for (FileVO vo : fileList) {
      vo.setCreatId(userId);
      vo.setUploadSrc("E");
    }
    return insertFileInfs(fileList);
  }

  /**
   * 파일에 대한 목록을 조회한다.
   *
   * @see egovframework.com.cmm.service.EgovFileMngService#selectFileInfs(egovframework.com.cmm.service.FileVO)
   */
  @Override
  public List<FileVO> selectFileInfs(FileVO fvo) throws Exception {
    return fileMapper.selectFileList(fvo);
  }

  /**
   * 파일에 대한 목록을 조회한다.
   *
   * @see egovframework.com.cmm.service.EgovFileMngService#selectFileInfs(egovframework.com.cmm.service.FileVO)
   */
  @Override
  public List<FileVO> selectFileOrdrInfs(FileVO fvo) throws Exception {
    return fileMapper.selectFileOrdrList(fvo);
  }


  /**
   * 여러 개의 파일에 대한 정보(속성 및 상세)를 수정한다.
   * 
   * @return
   *
   * @see egovframework.com.cmm.service.EgovFileMngService#updateFileInfs(java.util.List)
   */
  @Override
  public void updateFileInfs(List<?> fileList) throws Exception {
    // Delete & Insert
    FileVO vo;
    Iterator<?> iter = fileList.iterator();
    while (iter.hasNext()) {
      vo = (FileVO) iter.next();

      // 파일모달(/nc/upload)은 컨트롤러에서 이미 FILE_OP_LOG 기록 — 여기서 중복(폴더+파일 2건) 방지
      boolean skipModalFileOpLog = "A".equals(vo.getUploadSrc());
      Long logId = skipModalFileOpLog ? null : startErpFileOpLog("UPLOAD", vo);
      try {
        fileMapper.insertFileDetail(vo);
        if (!skipModalFileOpLog) {
          markSuccess(logId, parseLongSafe(vo.getFileMg()), null);
        }
      } catch (Exception e) {
        if (!skipModalFileOpLog) {
          markFail(logId, e, parseLongSafe(vo.getFileMg()), null);
        }
        throw e;
      }
    }
  }

  /**
   * ERP에서만 호출할것. 파일모달 호출 X
   */
  @Override
  public void updateFileInfs(List<FileVO> fileList, String userId) throws Exception {
    for (FileVO vo : fileList) {
      vo.setCreatId(userId);
      vo.setUploadSrc("E");
    }
    updateFileInfs(fileList);
  }

  /**
   * 하나의 파일을 수정한다.
   *
   * @see egovframework.com.cmm.service.EgovFileMngService#deleteFileInf(egovframework.com.cmm.service.FileVO)
   */
  @Override
  public void updateFileDetail(FileVO fvo) throws Exception {
    fileMapper.updateFileDetail(fvo);
  }

  /**
   * 하나의 파일을 삭제한다.
   *
   * @see egovframework.com.cmm.service.EgovFileMngService#deleteFileInf(egovframework.com.cmm.service.FileVO)
   */
  @Override
  public void deleteFileInf(FileVO fvo) throws Exception {

    FileVO file = fileMapper.selectFileInf(fvo);
    if (file == null)
      return;

    Long logId = startErpFileOpLog("DELETE", file);
    // 2) 외부 파일 삭제는 실패해도 DB 삭제는 진행
    try {
      if (STRE_COURS_NEXTCLOUD.equals(file.getFileStreCours())) {
        nextcloudDavService.deleteByDavPath(file.getStreFileNm());
      }
    } catch (Exception e) {
      // 실패해도 업무 흐름은 유지
      log.warn("외부 파일 삭제 실패(무시하고 DB 삭제 진행). atchFileId={}, fileSn={}, path={}",
          file.getAtchFileId(), file.getFileSn(), file.getStreFileNm(), e);
      // 외부 삭제 실패는 기록만 FAIL 처리. DB 삭제는 계속 진행.
      markFail(logId, e, parseLongSafe(file.getFileMg()), null);

      // (선택) 삭제 실패를 DB에 기록하고 싶으면 여기서 업데이트 한 번 더
      // fileDAO.updateDeleteFailInfo(file.getAtchFileId(), file.getFileSn(), e.getMessage());
    }

    fileMapper.deleteFileDetail(fvo);
    // 외부 삭제 실패로 이미 FAIL을 찍었어도, DB 삭제까지 완료됐으면 SUCCESS로 덮어쓰지 않는다.
    // (FAIL이 더 강한 의미이므로 그대로 둠)
    if (logId != null) {
      // FAIL로 찍힌 게 아니면 SUCCESS 처리
      // (FileOpLogService는 상태를 덮어쓰므로, try/catch로 판단)
      try {
        fileOpLogService.success(logId, parseLongSafe(file.getFileMg()), null);
      } catch (Exception ignore) {
        // ignore
      }
    }
  }

  private Long startErpFileOpLog(String opType, FileVO f) {
    if (f == null) {
      return null;
    }
    if (!STRE_COURS_NEXTCLOUD.equals(f.getFileStreCours())) {
      return null;
    }
    // 빈 이미지 슬롯(파일명/경로 없음)은 FILE_DETAIL_TB에는 적재하되 FILE_OP_LOG에는 남기지 않는다.
    if (isBlank(f.getOrignlFileNm()) && isBlank(f.getStreFileNm())) {
      return null;
    }
    String davPath = f.getStreFileNm();
    FileOpLogVO vo = new FileOpLogVO();
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    vo.setUserId(user != null ? user.getId() : f.getCreatId());
    vo.setDept(user == null ? null : user.getDeptName());
    vo.setSbkNo(extractSbkNoOrNull(davPath));
    vo.setOpType(opType);
    String uploadSrc = f.getUploadSrc();
    vo.setUploadSrc(uploadSrc == null || uploadSrc.trim().isEmpty() ? "E" : uploadSrc);
    vo.setDavPath(davPath);
    vo.setSrcPath(null);
    vo.setDstPath(null);
    vo.setIsFolder("N");
    vo.setFileName(f.getOrignlFileNm());
    vo.setContentType(guessContentTypeForFileOpLog(f));
    vo.setFileSize(parseLongSafe(f.getFileMg()));
    vo.setBytesSent(0L);
    vo.setClientIp(null);
    vo.setUserAgent(null);
    vo.setResultCd("START");
    vo.setErrMsg(null);
    try {
      return fileOpLogService.start(vo);
    } catch (Exception e) {
      // 로깅 실패가 본업무(업로드/DB반영)를 막으면 안 됨
      log.warn("FILE_OP_LOG_TB insert fail (ignore). opType={}, davPath={}", opType, davPath, e);
      return null;
    }
  }

  /**
   * FileVO에 MIME이 없으므로 원본명/확장자로 유추 (로그용).
   */
  private String guessContentTypeForFileOpLog(FileVO f) {
    if (f == null) {
      return null;
    }
    String name = f.getOrignlFileNm();
    if (name != null && !name.trim().isEmpty()) {
      String ct = URLConnection.guessContentTypeFromName(name.trim());
      if (ct != null && !ct.isEmpty()) {
        return ct;
      }
    }
    String ext = f.getFileExtsn();
    if (ext == null || ext.trim().isEmpty()) {
      if (name != null && name.contains(".")) {
        int dot = name.lastIndexOf('.');
        if (dot >= 0 && dot < name.length() - 1) {
          ext = name.substring(dot + 1);
        }
      }
    }
    if (ext == null) {
      return null;
    }
    ext = ext.trim().toLowerCase();
    switch (ext) {
      case "jpg":
      case "jpeg":
        return "image/jpeg";
      case "png":
        return "image/png";
      case "gif":
        return "image/gif";
      case "bmp":
        return "image/bmp";
      case "webp":
        return "image/webp";
      case "pdf":
        return "application/pdf";
      case "zip":
        return "application/zip";
      case "txt":
        return "text/plain";
      case "html":
      case "htm":
        return "text/html";
      case "xlsx":
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
      case "xls":
        return "application/vnd.ms-excel";
      case "doc":
        return "application/msword";
      case "docx":
        return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
      case "hwp":
        return "application/x-hwp";
      default:
        return "application/octet-stream";
    }
  }

  private String extractSbkNoOrNull(String path) {
    if (path == null) {
      return null;
    }
    Matcher m = SBK_NO_PATTERN.matcher(path);
    if (!m.find()) {
      return null;
    }
    return m.group(1).toUpperCase();
  }

  private boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }

  private Long parseLongSafe(String s) {
    if (s == null) {
      return 0L;
    }
    try {
      return Long.parseLong(s);
    } catch (Exception e) {
      return 0L;
    }
  }

  private void markSuccess(Long logId, Long fileSize, Long bytesSent) {
    if (logId == null) {
      return;
    }
    try {
      fileOpLogService.success(logId, fileSize, bytesSent);
    } catch (Exception e) {
      log.warn("FILE_OP_LOG_TB success update fail (ignore). logId={}", logId, e);
    }
  }

  private void markFail(Long logId, Exception e, Long fileSize, Long bytesSent) {
    if (logId == null) {
      return;
    }
    String msg = e == null ? null : e.getMessage();
    try {
      fileOpLogService.fail(logId, msg, fileSize, bytesSent);
    } catch (Exception ex) {
      log.warn("FILE_OP_LOG_TB fail update fail (ignore). logId={}", logId, ex);
    }
  }

  /**
   * 파일에 대한 상세정보를 조회한다.
   *
   * @see egovframework.com.cmm.service.EgovFileMngService#selectFileInf(egovframework.com.cmm.service.FileVO)
   */
  @Override
  public FileVO selectFileInf(FileVO fvo) throws Exception {
    return fileMapper.selectFileInf(fvo);
  }

  @Override
  public String resolveImageUrl(String atchFileId) throws Exception {
    return resolveImageUrl(atchFileId, "0");
  }

  @Override
  public String resolveImageUrl(String atchFileId, String fileSn) throws Exception {
    if (atchFileId == null || atchFileId.trim().isEmpty()) {
      return "";
    }
    FileVO q = new FileVO();
    q.setAtchFileId(atchFileId.trim());
    q.setFileSn(fileSn == null || fileSn.trim().isEmpty() ? "0" : fileSn.trim());
    FileVO file = fileMapper.selectFileInf(q);
    if (file == null) {
      return "";
    }
    String url = nextcloudDavService.resolveFileUrl(file);
    return url == null ? "" : url;
  }

  @Override
  public String resolveImageUrl(FileVO fileQuery) throws Exception {
    if (fileQuery == null) {
      return "";
    }
    String atchFileId = fileQuery.getAtchFileId();
    if (atchFileId == null || atchFileId.trim().isEmpty()) {
      return "";
    }
    if (fileQuery.getFileStreCours() != null && !fileQuery.getFileStreCours().trim().isEmpty()) {
      String url = nextcloudDavService.resolveFileUrl(fileQuery);
      return url == null ? "" : url;
    }
    return resolveImageUrl(atchFileId, fileQuery.getFileSn());
  }

  /**
   * 파일 구분자에 대한 최대값을 구한다.
   *
   * @see egovframework.com.cmm.service.EgovFileMngService#getMaxFileSN(egovframework.com.cmm.service.FileVO)
   */
  @Override
  public int getMaxFileSN(FileVO fvo) throws Exception {
    return fileMapper.getMaxFileSN(fvo);
  }

  /**
   * 전체 파일을 삭제한다.
   *
   * @see egovframework.com.cmm.service.EgovFileMngService#deleteAllFileInf(egovframework.com.cmm.service.FileVO)
   */
  @Override
  public void deleteAllFileInf(FileVO fvo) throws Exception {
    fileMapper.deleteCOMTNFILE(fvo);
  }

  @Override
  public void deletePicAll(FileVO fvo) throws Exception {
    fileMapper.deletePicAll(fvo);
  }

  /**
   * 파일명 검색에 대한 목록을 조회한다.
   *
   * @see egovframework.com.cmm.service.EgovFileMngService#selectFileListByFileNm(egovframework.com.cmm.service.FileVO)
   */
  @Override
  public Map<String, Object> selectFileListByFileNm(FileVO fvo) throws Exception {
    List<FileVO> result = fileMapper.selectFileListByFileNm(fvo);
    int cnt = fileMapper.selectFileListCntByFileNm(fvo);

    Map<String, Object> map = new HashMap<String, Object>();

    map.put("resultList", result);
    map.put("resultCnt", Integer.toString(cnt));

    return map;
  }

  /**
   * 이미지 파일에 대한 목록을 조회한다.
   *
   * @see egovframework.com.cmm.service.EgovFileMngService#selectImageFileList(egovframework.com.cmm.service.FileVO)
   */
  @Override
  public List<FileVO> selectImageFileList(FileVO vo) throws Exception {
    return fileMapper.selectImageFileList(vo);
  }

  @Override
  public List<FileVO> selectUploadSrcByPaths(List<String> fileNm) {
    return fileMapper.selectUploadSrcByPaths(fileNm);
  }

  @Override
  public int insertFolderMeta(FolderMetaVO vo) throws Exception {
    if (vo == null) {
      throw new IllegalArgumentException("FolderMetaVO가 null 입니다.");
    }
    return fileMapper.insertFolderMeta(vo);
  }

  @Override
  @Transactional
  public int updateFolderMetaByPathHash(String targetDavPath, FolderMetaVO vo) throws Exception {
    if (isEmpty(targetDavPath)) {
      throw new IllegalArgumentException("oldPathHash가 비어있습니다.");
    }
    if (vo == null) {
      throw new IllegalArgumentException("FolderMetaVO가 null 입니다.");
    }

    String oldPathHash = DigestUtils.sha256Hex(targetDavPath);
    FileDetailUpdateVO fileDetail = new FileDetailUpdateVO();
    fileDetail.setNewDavPath(vo.getFolderPath());
    fileDetail.setOldDavPath(targetDavPath);

    int result = 0;

    // rename 대상 폴더 메타 1건을 갱신한다
    result = fileMapper.updateFolderMetaByPathHash(oldPathHash, vo);
    // rename 대상 폴더 하위에 속한 모든 폴더 메타의 경로를 prefix 치환한다
    result += fileMapper.updateFolderMetaPathPrefix(targetDavPath, vo);
    // 폴더 rename 이후, 해당 폴더 하위의 파일 상세 테이블에 저장된 파일 경로도 prefix 치환한다
    result += fileMapper.updateFileDetailByFolderRename(fileDetail);

    return result;
  }

  @Override
  public int updateFolderMetaByFolderPath(String oldFolderPath, FolderMetaVO vo) throws Exception {
    if (isEmpty(oldFolderPath)) {
      throw new IllegalArgumentException("oldFolderPath가 비어있습니다.");
    }
    if (vo == null) {
      throw new IllegalArgumentException("FolderMetaVO가 null 입니다.");
    }
    return fileMapper.updateFolderMetaByFolderPath(oldFolderPath, vo);
  }

  private boolean isEmpty(String s) {
    return s == null || s.trim().isEmpty();
  }


  @Override
  public List<FolderMetaVO> selectFolderUploadSrcByPaths(List<String> folderPaths) {
    return fileMapper.selectFolderUploadSrcByPaths(folderPaths);
  }

  @Override
  public int updateFileDetailByStreFileNm(FileDetailUpdateVO vo) {
    return fileMapper.updateFileDetailByStreFileNm(vo);
  }

  @Override
  public int markFileDetailDeletedByExactPath(String streFileNm, String updtId) throws Exception {
    if (isEmpty(streFileNm)) {
      throw new IllegalArgumentException("streFileNm이 비어있습니다.");
    }
    if (isEmpty(updtId)) {
      throw new IllegalArgumentException("updtId가 비어있습니다.");
    }
    return fileMapper.markFileDetailDeletedByExactPath(streFileNm, updtId);
  }

  @Override
  public int markFileDetailDeletedByPathPrefix(String folderPath, String updtId) throws Exception {
    if (isEmpty(folderPath)) {
      throw new IllegalArgumentException("folderPath가 비어있습니다.");
    }
    if (isEmpty(updtId)) {
      throw new IllegalArgumentException("updtId가 비어있습니다.");
    }
    return fileMapper.markFileDetailDeletedByPathPrefix(folderPath, updtId);
  }

  @Override
  public int deleteFolderMetaByPathPrefix(String folderPath, String updtId) throws Exception {
    if (isEmpty(folderPath)) {
      throw new IllegalArgumentException("folderPath가 비어있습니다.");
    }
    return fileMapper.deleteFolderMetaByPathPrefix(folderPath, updtId);
  }

  @Override
  @Transactional
  public void updateFolderMetaPathPrefix(String oldPrefix, String newPrefix, String userId)
      throws Exception {
    if (oldPrefix == null || oldPrefix.trim().isEmpty()) {
      throw new IllegalArgumentException("oldPrefix가 비어있습니다.");
    }
    if (newPrefix == null || newPrefix.trim().isEmpty()) {
      throw new IllegalArgumentException("newPrefix가 비어있습니다.");
    }
    if (userId == null || userId.trim().isEmpty()) {
      userId = "SYSTEM";
    }

    String op = normalizePrefix(oldPrefix);
    String np = normalizePrefix(newPrefix);

    FolderMetaVO meta = new FolderMetaVO();
    meta.setFolderPath(np);
    meta.setPathHash(DigestUtils.sha256Hex(np));
    meta.setUploadSrc("A");
    meta.setCreatId(userId);

    fileMapper.updateFolderMetaPathPrefix(op, meta);
  }

  @Override
  @Transactional
  public void copyFolderMetaByPathPrefix(String oldPrefix, String newPrefix, String userId)
      throws Exception {
    if (oldPrefix == null || oldPrefix.trim().isEmpty()) {
      throw new IllegalArgumentException("oldPrefix가 비어있습니다.");
    }
    if (newPrefix == null || newPrefix.trim().isEmpty()) {
      throw new IllegalArgumentException("newPrefix가 비어있습니다.");
    }
    if (userId == null || userId.trim().isEmpty()) {
      userId = "SYSTEM";
    }

    String op = normalizePrefix(oldPrefix);
    String np = normalizePrefix(newPrefix);

    Map<String, Object> param = new HashMap<String, Object>();
    param.put("oldPrefix", op);
    param.put("newPrefix", np);
    param.put("userId", userId);

    fileMapper.copyFolderMetaByPathPrefix(param);
  }

  @Override
  @Transactional
  public void copyFileDetailByPathPrefix(String oldPrefix, String newPrefix, String userId)
      throws Exception {
    if (oldPrefix == null || oldPrefix.trim().isEmpty()) {
      throw new IllegalArgumentException("oldPrefix가 비어있습니다.");
    }
    if (newPrefix == null || newPrefix.trim().isEmpty()) {
      throw new IllegalArgumentException("newPrefix가 비어있습니다.");
    }
    if (userId == null || userId.trim().isEmpty()) {
      userId = "SYSTEM";
    }

    String op = normalizePrefix(oldPrefix);
    String np = normalizePrefix(newPrefix);

    Map<String, Object> param = new HashMap<String, Object>();
    param.put("oldPrefix", op);
    param.put("newPrefix", np);
    param.put("userId", userId);

    fileMapper.copyFileDetailByPathPrefix(param);
  }

  private String normalizePrefix(String p) {
    String v = p.trim();
    if (!v.startsWith("/")) {
      v = "/" + v;
    }
    if (v.length() > 1 && v.endsWith("/")) {
      v = v.substring(0, v.length() - 1);
    }
    return v;
  }

}
