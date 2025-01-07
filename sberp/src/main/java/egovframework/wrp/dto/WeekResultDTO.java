package egovframework.wrp.dto;

import java.util.List;
import egovframework.wrp.service.WeekRepSub;
import egovframework.wrp.service.WeekResult;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel(description = "주간보고시 통계수치")
public class WeekResultDTO {
  
  @ApiModelProperty(value = "시험부명")
  private String testType;
  
  @ApiModelProperty(value = "시험부 시험 결과")
  private WeekResult weekResult;
  
  @ApiModelProperty(value = "시험부 최근4주 결과리스트")
  private List<WeekRepSub> weekRepSubList;

  @Getter
  @Setter
  @ToString(callSuper = true)
  @ApiModel(value = "WeekRepDTO.Req", description = "입력값")
  public static class Req {
    
    private int wrSeq;
    private boolean fixYn;
    private String testTypeCode;
    private String memo;
    private int wrCnt;
    private String udtMemId;
  }
  
}
