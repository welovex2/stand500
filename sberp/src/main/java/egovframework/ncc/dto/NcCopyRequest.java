package egovframework.ncc.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel(value = "NcCopyRequest", description = "Nextcloud WebDAV COPY 요청 DTO")
@Getter
@Setter
@ToString
public class NcCopyRequest {

  @ApiModelProperty(value = "원본 경로 예시 /ERP/2026/02/SB26-G0000/00.공통폴더", required = true)
  private String sourceDavPath;

  @ApiModelProperty(value = "목적지 경로 예시 /ERP/2026/02/SB26-G0000/00.공통폴더_복사", required = true)
  private String destDavPath;

  @ApiModelProperty(value = "목적지 동일 이름 존재 시 덮어쓰기 여부", required = false)
  private boolean overwrite;

  @ApiModelProperty(
      value = "DB 메타 반영 방식 TOP_ONLY 또는 FULL. TOP_ONLY는 최상위만 upsert, FULL은 하위 폴더 메타까지 복제",
      required = false)
  private String metaMode;
}
