package egovframework.cmm.service;

import java.io.InputStream;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.amazonaws.services.s3.transfer.model.UploadResult;

public interface NextcloudDavService {

  /**
   * Nextcloud WebDAV로 업로드하고,
   * 업로드된 DAV 경로(objectKey 역할)를 반환
   */
  String upload(MultipartFile file, String relativePath) throws Exception;

  void deleteByDavPath(String davPath) throws Exception;

  /**
   * 필요하면 폴더를 생성(MKCOL). 없으면 자동 생성.
   */
  void ensureFolder(String relativeFolderPath) throws Exception;
  
  
  /** 폴더 1회 공유 토큰 기반 “원본 파일” direct URL */
  String buildPublicRawFileUrl(String davPath) throws Exception;
  
  
  String resolveFileUrl(FileVO file) throws Exception;

  /** ✅ 폴더/파일 목록 조회 (Depth: 1=현재폴더+자식, 0=자기 자신) */
  List<WebDavItemDTO> list(String davPath, int depth) throws Exception;

  /** ✅ 단일 리소스 메타 조회 */
  WebDavItemDTO stat(String davPath) throws Exception;

  /** ✅ Nextcloud WebDAV에서 파일을 GET으로 읽기(다운로드 컨트롤러에서 사용) */
  InputStream downloadStreamByDavPath(String davPath) throws Exception;

  /** ✅ 업로드(화면에서 업로드하는 경우: 대상 폴더 davPath + 파일) */
  UploadResultDTO uploadToFolder(String folderDavPath, MultipartFile file) throws Exception;

  String createFolder(String parentDavPath, String folderName) throws Exception;

}
