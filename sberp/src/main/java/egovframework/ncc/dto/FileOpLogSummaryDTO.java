package egovframework.ncc.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel(description = "파일작업이력 요약")
@Getter
@Setter
public class FileOpLogSummaryDTO {

  @ApiModelProperty(value = "총 용량(바이트)")
  private Long totalBytes;

  @ApiModelProperty(value = "총 용량(GB)", example = "0.01")
  private Double totalCapacityGb;

  @ApiModelProperty(value = "총 파일 수")
  private Long totalFileCount;

  @ApiModelProperty(value = "총 폴더 수")
  private Long totalFolderCount;
}
