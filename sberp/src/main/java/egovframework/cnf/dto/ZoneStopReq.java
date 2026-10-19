package egovframework.cnf.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel(description = "구역 사용중지")
public class ZoneStopReq {

  @ApiModelProperty(value = "구역 고유번호", example = "10", required = true)
  private int zoneSeq;
}
