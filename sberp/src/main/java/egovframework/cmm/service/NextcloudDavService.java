package egovframework.cmm.service;

import org.springframework.web.multipart.MultipartFile;

public interface NextcloudDavService {

  /**
   * Nextcloud WebDAV로 업로드하고,
   * 업로드된 DAV 경로(objectKey 역할)를 반환
   */
  String upload(MultipartFile file, String relativePath) throws Exception;

  /**
   * 필요하면 폴더를 생성(MKCOL). 없으면 자동 생성.
   */
  void ensureFolder(String relativeFolderPath) throws Exception;
  
  
  /** 폴더 1회 공유 토큰 기반 “원본 파일” direct URL */
  String buildPublicRawFileUrl(String davPath) throws Exception;

}
