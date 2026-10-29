package egovframework.sam.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel(description = "시료 사진 업로드 폴더")
public class SamUploadFolderDTO {

  @ApiModelProperty(value = "폴더 코드", example = "PARTS")
  private String code;

  @ApiModelProperty(value = "화면 표시명", example = "부품도")
  private String label;

  @ApiModelProperty(value = "신청서 기준 상대경로")
  private String relativePath;

  @ApiModelProperty(value = "WebDAV 전체 경로 (/nc/list, /nc/upload path)")
  private String davPath;

}
