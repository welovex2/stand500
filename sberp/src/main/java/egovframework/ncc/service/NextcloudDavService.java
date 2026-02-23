package egovframework.ncc.service;

import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;
import egovframework.cmm.service.FileVO;
import egovframework.ncc.dto.UploadResultDTO;
import egovframework.ncc.dto.WebDavListResponseDTO;

public interface NextcloudDavService {

  /**
   * Nextcloud WebDAV로 업로드하고, 업로드된 DAV 경로(objectKey 역할)를 반환
   */
  String upload(MultipartFile file, String relativePath) throws Exception;

  void deleteByDavPath(String davPath) throws Exception;


  /** 폴더 1회 공유 토큰 기반 “원본 파일” direct URL */
  String buildPublicRawFileUrl(String davPath) throws Exception;


  String resolveFileUrl(FileVO file);

  /** 폴더/파일 목록 조회 (Depth: 1=현재폴더+자식, 0=자기 자신) */
  WebDavListResponseDTO list(String davPath, int depth) throws Exception;

  /** Nextcloud WebDAV에서 파일을 GET으로 읽기(다운로드 컨트롤러에서 사용) */
  InputStream downloadStreamByDavPath(String davPath) throws Exception;

  /** 업로드(화면에서 업로드하는 경우: 대상 폴더 davPath + 파일) */
  UploadResultDTO uploadToFolder(String folderDavPath, MultipartFile file) throws Exception;

  String createFolder(String parentDavPath, String folderName, String string) throws Exception;

  /**
   * 업로드 완료 후 FILE_TB/FILE_DETAIL_TB에 메타 저장
   * 
   * @return 저장된 FILE_SN
   * @throws Exception
   */
  int insertFileDetail(String atchFileId, String reqPath, String davPath, String originalFilename,
      long size, String creatId) throws Exception;

  /**
   * 덮어쓰기 방지 업로드: 동일 경로에 파일이 이미 있으면 412(Precondition Failed) 발생. - Windows 스타일 이름 증가 "(1)(2)..."
   * 재시도를 위해 사용합니다.
   */
  String uploadIfNotExists(MultipartFile file, String relativePath) throws Exception;

  /**
   * 폴더 존재 유무 확인
   */
  boolean existsDirectory(String davPath) throws Exception;

  String rename(String targetDavPath, String newName, boolean overwrite, String userId)
      throws Exception;

  String move(String sourceDavPath, String destDavPath, boolean overwrite, String userId)
      throws Exception;

  void deleteWithDbSync(String davPath, boolean recursive, String userId) throws Exception;

  String moveWithDbSync(String sourceDavPath, String destDavPath, boolean overwrite, String userId)
      throws Exception;

  String copyWithDbSync(String sourceDavPath, String destDavPath, boolean overwrite, String userId,
      String metaMode) throws Exception;

}
