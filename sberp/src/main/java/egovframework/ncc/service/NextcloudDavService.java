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

  /**
   * 성적서(report.do) 노출용 이미지 URL을 생성한다.
   *
   * <p>원본 파일은 그대로 두고, Nextcloud 저장 이미지는 ERP 리사이즈 프록시
   * ({@code Globals.report.imageUrl}) URL로 변환해 축소 이미지를 노출한다. 레거시 등 그 외 저장소는
   * {@link #resolveFileUrl(FileVO)} 결과를 그대로 반환한다.
   */
  String resolveReportImageUrl(FileVO file);

  /** 폴더/파일 목록 조회 (Depth: 1=현재폴더+자식, 0=자기 자신) */
  WebDavListResponseDTO list(String davPath, int depth) throws Exception;

  /** Nextcloud WebDAV에서 파일을 GET으로 읽기(다운로드 컨트롤러에서 사용) */
  InputStream downloadStreamByDavPath(String davPath) throws Exception;

  /** 업로드(화면에서 업로드하는 경우: 대상 폴더 davPath + 파일) */
  UploadResultDTO uploadToFolder(String folderDavPath, MultipartFile file) throws Exception;

  /**
   * 업로드(폴더 드롭·webkitRelativePath 지원).
   *
   * @param folderDavPath  화면 현재 폴더 DAV 경로 ({@code path} 파라미터)
   * @param file           multipart 파일
   * @param relativePath   base 폴더 기준 상대 경로(파일명 포함). null/blank면 flat 업로드
   * @param creatId        업로드 사용자 ID (폴더 메타 FOLDER_META_TB 등록용, null이면 폴더 메타 생략)
   */
  UploadResultDTO uploadToFolder(String folderDavPath, MultipartFile file, String relativePath,
      String creatId) throws Exception;

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

  /** 파일 존재 여부 (폴더는 false) */
  boolean existsFile(String davPath) throws Exception;

  /**
   * 바이트 배열을 DAV 경로에 PUT 업로드.
   *
   * @param overwrite false 이면 동일 경로 존재 시 412
   * @return 업로드된 DAV 경로
   */
  String uploadBytes(byte[] content, String davPath, String contentType, boolean overwrite)
      throws Exception;

}
