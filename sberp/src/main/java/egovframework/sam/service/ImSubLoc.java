package egovframework.sam.service;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ImSubLoc {

  @ApiModelProperty(value = "이력 SEQ")
  private int imSubLocSeq;

  @ApiModelProperty(value = "IM_ID", example = "IM26-G0001")
  private String imId;

  @ApiModelProperty(value = "시료번호")
  private int imSubSeq;

  @ApiModelProperty(value = "구역 SEQ")
  private int zoneSeq;

  @ApiModelProperty(value = "창고 SEQ")
  private int strSeq;

  @ApiModelProperty(value = "창고명")
  private String whName;

  @ApiModelProperty(value = "구역명", example = "가-01")
  private String zoneName;

  @ApiModelProperty(value = "메모")
  private String memo;

  private String insMemId;
  @ApiModelProperty(value = "등록일", example = "2026-10-01")
  private String insDt;
  private String udtMemId;
  @ApiModelProperty(value = "수정일", example = "2026-10-01")
  private String udtDt;
  private String state;
  private String insMemName;
}
