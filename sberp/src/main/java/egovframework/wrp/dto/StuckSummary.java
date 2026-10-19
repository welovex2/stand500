package egovframework.wrp.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel(description = "막힘·적체 주간 3숫자")
public class StuckSummary {

  @ApiModelProperty(value = "시험부 코드 (EMC/MDC 는 출장)")
  private String testTypeCode;

  @ApiModelProperty(value = "시험부명")
  private String testType;

  @ApiModelProperty(value = "지금 막힌 건수 (디버깅+홀딩+RD지연)")
  private int stuckCnt;

  @ApiModelProperty(value = "디버깅 건수")
  private int debugCnt;

  @ApiModelProperty(value = "홀딩 건수")
  private int holdingCnt;

  @ApiModelProperty(value = "RD지연 건수")
  private int rdDelayCnt;

  @ApiModelProperty(value = "막힌 일 중 14일 이상")
  private int stuck14dCnt;

  @ApiModelProperty(value = "지난주 접수 건수")
  private int lastWeekInCnt;

  @ApiModelProperty(value = "지난주 완료 건수")
  private int lastWeekEndCnt;

  @ApiModelProperty(value = "지난주 접수-완료 (양수면 적체)")
  private int lastWeekNet;
}
