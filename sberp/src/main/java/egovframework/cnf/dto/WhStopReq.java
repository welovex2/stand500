package egovframework.cnf.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel(description = "창고 사용중지")
public class WhStopReq {

  @ApiModelProperty(value = "창고 고유번호", example = "1", required = true)
  private int strSeq;
}
