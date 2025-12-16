package egovframework.cmm.service;

import java.util.List;

public interface NextcloudFolderService {

  /**
   * ERP 하위 폴더의 파일/폴더 목록 조회
   * @param erpRelativeFolder  예: "2025/12/RAW" 또는 "/2025/12/RAW"
   */
  List<NcFileDTO> listErpFolder(String erpRelativeFolder) throws Exception;
  
  /**
   * 신청서 폴더 생성 후 특정 사용자/그룹에 권한 부여
   *
   * @param yearMonth   "2025/12" 형태
   * @param applyNo     "SB25-G1845" 신청서번호
   * @param targetId    Nextcloud 사용자ID 또는 그룹ID
   * @param isGroup     true=그룹공유, false=사용자공유
   * @return davPath (예: "/ERP/2025/12/SB25-G1845")
   */
  String createApplyFolderAndGrant(
          String yearMonth,
          String applyNo,
          String targetId,
          boolean isGroup
  ) throws Exception;
  
}
