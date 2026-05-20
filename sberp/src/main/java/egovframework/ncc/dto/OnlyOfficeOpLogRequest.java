package egovframework.ncc.dto;

import lombok.Getter;
import lombok.Setter;

/** ONLYOFFICE 편집기(브라우저) → ERP 저장 이력 보고. */
@Getter
@Setter
public class OnlyOfficeOpLogRequest {
  /** WebDAV 경로 예: /ERP/.../book.xlsx */
  private String path;
  /** {@code ONLYOFFICE_SAVE} 등 */
  private String opType;
}
