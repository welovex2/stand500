package egovframework.ncc.service;

import java.util.Map;

/**
 * Nextcloud ONLYOFFICE 커넥터 앱(OCS)을 통해 편집기 설정을 조회한다. 저장·콜백은 Nextcloud가 처리한다.
 */
public interface NextcloudOnlyofficeConnectorService {

  /**
   * WebDAV 경로({@code /ERP/...})에 해당하는 Nextcloud 내부 fileId 를 조회한다.
   */
  long resolveFileId(String davPath) throws Exception;

  /**
   * ONLYOFFICE 커넥터 OCS {@code /api/v1/config/{fileId}} 응답(편집기 DocEditor 설정 JSON).
   *
   * @param davPath 정규화된 경로
   * @param viewOnly true 이면 읽기 전용으로 덮어씀
   */
  Map<String, Object> fetchEditorConfig(String davPath, boolean viewOnly) throws Exception;
}
