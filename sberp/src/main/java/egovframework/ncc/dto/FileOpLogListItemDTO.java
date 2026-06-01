package egovframework.ncc.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel(description = "파일작업이력 목록 행")
@Getter
@Setter
public class FileOpLogListItemDTO {

  @ApiModelProperty(value = "로그 ID")
  private Long logId;

  @ApiModelProperty(value = "형식", example = "파일")
  private String itemType;

  @ApiModelProperty(value = "최종 위치(폴더명)")
  private String folderLocation;

  @ApiModelProperty(value = "파일/폴더명")
  private String fileName;

  @ApiModelProperty(value = "부서")
  private String dept;

  @ApiModelProperty(value = "이용자 ID")
  private String userId;

  @ApiModelProperty(value = "이용자명")
  private String userName;

  @ApiModelProperty(value = "작업유형 코드")
  private String opType;

  @ApiModelProperty(value = "작업유형 표시명")
  private String opTypeLabel;

  @ApiModelProperty(value = "용량(바이트)")
  private Long fileSize;

  @ApiModelProperty(value = "용량 표시", example = "2.4MB")
  private String fileSizeLabel;

  @ApiModelProperty(value = "구분 코드", example = "A")
  private String uploadSrc;

  @ApiModelProperty(value = "구분 표시", example = "연동")
  private String uploadSrcLabel;

  @ApiModelProperty(value = "작업일시", example = "2026-04-20 09:10")
  private String creatDt;

  @ApiModelProperty(value = "대상 경로")
  private String davPath;

  @ApiModelProperty(value = "신청서번호")
  private String sbkNo;

  @ApiModelProperty(value = "결과코드")
  private String resultCd;
}
