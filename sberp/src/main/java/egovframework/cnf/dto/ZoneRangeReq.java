package egovframework.cnf.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel(description = "구역 범위등록")
public class ZoneRangeReq {

  @ApiModelProperty(value = "창고 고유번호", example = "1", required = true)
  private int strSeq;

  @ApiModelProperty(value = "구역명 접두어", example = "가", required = true)
  private String prefix;

  @ApiModelProperty(value = "시작번호", notes = "1 이상", example = "1", required = true)
  private int fromNo;

  @ApiModelProperty(value = "끝번호", notes = "시작번호 이상, 1회 50개까지", example = "12",
      required = true)
  private int toNo;

  @ApiModelProperty(value = "구역용도 01 일반 02 인수요청 03 장기",
      notes = "미전송시 01, 등록되는 구역에 공통 적용", example = "01")
  private String useCode;

  @ApiModelProperty(hidden = true)
  private String insMemId;

  @ApiModelProperty(hidden = true)
  private String udtMemId;
}
