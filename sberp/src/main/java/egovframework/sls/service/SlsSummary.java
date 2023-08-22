package egovframework.sls.service;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SlsSummary {
  
  @ApiModelProperty(value = "청구액총합 ", example = "0")
  int total = 0;
  @ApiModelProperty(value = "청구액총건수 ", example = "0")
  int totalCnt = 0;
  @ApiModelProperty(value = "미수금총합 ", example = "0")
  int totalArrears = 0;
  @ApiModelProperty(value = "미수금총건수 ", example = "0")
  int arrearsCnt = 0;
  @ApiModelProperty(value = "순매출총합 ", example = "0")
  int totalNetSales = 0;
  @ApiModelProperty(value = "순매출총건수 ", example = "0")
  int NetSalesCnt = 0;
  @ApiModelProperty(value = "시험비총합 ", example = "0")
  int totalTestFee = 0;
  @ApiModelProperty(value = "시험비총건수 ", example = "0")
  int testFeeCnt = 0;
  
}
