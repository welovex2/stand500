package egovframework.wrp.dto;

import java.util.List;
import egovframework.wrp.service.WeekRep;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel(description = "주간보고 상세보기")
public class WeekRepDTO {
  
  @ApiModelProperty(value = "전회차 피드백")
  private String feedBack;
  
  @ApiModelProperty(value = "시험부 결과")
  private List<WeekResultDTO> totalResult;
  
  @ApiModelProperty(value = "보고사항")
  private WeekRep report;
}
