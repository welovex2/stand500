package egovframework.ncc.service;

import java.net.URI;
import java.util.List;
import egovframework.ncc.dto.NcFileDTO;

public interface NextcloudFolderService {

  /**
   * ERP 하위 폴더의 파일/폴더 목록 조회
   * 
   * @param erpRelativeFolder 예: "2025/12/RAW" 또는 "/2025/12/RAW"
   */
  List<NcFileDTO> listErpFolder(String erpRelativeFolder) throws Exception;

  /**
   * 신청서 폴더 생성 후 특정 사용자/그룹에 권한 부여
   *
   * @param yearMonth "2025/12" 형태
   * @param applyNo "SB25-G1845" 신청서번호
   * @param targetId Nextcloud 사용자ID 또는 그룹ID
   * @param isGroup true=그룹공유, false=사용자공유
   * @return davPath (예: "/ERP/2025/12/SB25-G1845")
   */
  String createApplyFolderAndGrant(String yearMonth, String applyNo, String targetId,
      boolean isGroup) throws Exception;

  /**
   * 폴더만 생성 (신청서)
   */
  String ensureApplyFolder(String yearMonth, String applyNo) throws Exception;

  /**
   * 재발행 시험 폴더 생성 (하위 폴더 없음)
   *
   * @param originalFolderPath 원본 신청서 NC_FOLDER_PATH
   * @param reissueTestId 재발행 시험번호 (예: "SB25-G1578-EM0001-1")
   * @return 재발행 시험 폴더 상대 경로
   */
  String ensureReissueTestFolder(String originalFolderPath, String reissueTestId) throws Exception;


  /**
   * 필요하면 폴더를 생성(MKCOL). 없으면 자동 생성.
   */
  void ensureFolder(String relativeFolderPath) throws Exception;

  /** rootFolder 자체를 생성 (없으면 MKCOL) */
  void ensureRootFolder() throws Exception;

  /** MKCOL 공통 실행 */
  int mkcol(URI uri) throws Exception;

}
