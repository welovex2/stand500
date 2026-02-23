package egovframework.ncc.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel(value = "NcMoveRequest", description = "Nextcloud WebDAV MOVE 요청 DTO")
@Getter
@Setter
@ToString
public class NcMoveRequest {

  @ApiModelProperty(value = "원본 경로 예시 /ERP/2026/02/SB26-G0000/00.공통폴더", required = true)
  private String sourceDavPath;

  @ApiModelProperty(value = "목적지 경로 예시 /ERP/2026/02/SB26-G0000/01.신청서", required = true)
  private String destDavPath;

  @ApiModelProperty(value = "목적지 동일 이름 존재 시 덮어쓰기 여부", required = false)
  private boolean overwrite;

}
