package egovframework.cnf.service;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel(description = "창고 구역")
public class Zone {

  @ApiModelProperty(value = "구역 고유번호", example = "10")
  private int zoneSeq;

  @ApiModelProperty(value = "창고 고유번호", example = "1", required = true)
  private int strSeq;

  @ApiModelProperty(value = "창고명", example = "1창고")
  private String whName;

  @ApiModelProperty(value = "구역명", notes = "등록시 필수, 수정시 변경할 때만 전송", example = "가-01")
  private String zoneName;

  @ApiModelProperty(value = "구역설명", notes = "선택, 수정시 빈값 전송하면 삭제", example = "창가쪽 선반")
  private String zoneDesc;

  @ApiModelProperty(value = "구역용도 01 일반 02 인수요청 03 장기",
      notes = "등록시 미전송하면 01", example = "01")
  private String useCode;

  @ApiModelProperty(value = "구역용도명", example = "일반")
  private String useName;

  @ApiModelProperty(value = "보관 시료수", example = "23")
  private int itemCnt;

  @ApiModelProperty(hidden = true)
  private String insMemId;

  @ApiModelProperty(value = "등록일", example = "2026-10-01")
  private String insDt;

  @ApiModelProperty(hidden = true)
  private String udtMemId;

  @ApiModelProperty(value = "수정일", notes = "사용중지 구역은 중지일", example = "2026-10-16")
  private String udtDt;

  @ApiModelProperty(value = "상태 I 등록 U 수정 D 사용중지", example = "I")
  private String state;
}
