package egovframework.sts.dto;

import java.util.List;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class StsDTO {


  @ApiModelProperty(value="통계날짜 ", example = "")
  private String stsDt;
  
  @ApiModelProperty(value="계산서발행 금액 ", example = "")
  private int billAmt;
  @ApiModelProperty(value="납부완료 금액 ", example = "")
  private long payAmt;
  
  @ApiModelProperty(value="시험부서별 데이터 ", example = "")
  private List<TestTypeList> testTypeList;
  
  @Getter
  @Setter
  @ApiModel(value = "StsDTO.Res", description = "시험부서별 데이터")
  public static class TestTypeList {
    
    @ApiModelProperty(value="통계날짜 ", example = "")
    private String stsDt;
    @ApiModelProperty(value="시험부서명 ", example = "")
    private String testType;
       
    @ApiModelProperty(value="시험접수 건수 ", example = "")
    private int inCnt;
    @ApiModelProperty(value="시험접수 금액 ", example = "")
    private long inAmt;  
    
    @ApiModelProperty(value="시험중 건수 ", example = "")
    private int ingCnt;
    @ApiModelProperty(value="시험중 금액 ", example = "")
    private long ingAmt;
    @ApiModelProperty(value="디버깅 건수 ", example = "")
    private int debCnt;
    @ApiModelProperty(value="디버깅 금액 ", example = "")
    private long debAmt;
    @ApiModelProperty(value="홀딩 건수 ", example = "")
    private int holCnt;
    @ApiModelProperty(value="홀딩 금액 ", example = "")
    private long holAmt;
    @ApiModelProperty(value="시험완료 건수 ", example = "")
    private int endCnt;
    @ApiModelProperty(value="시험완료 금액 ", example = "")
    private long endAmt;
    @ApiModelProperty(value="성적서발급완료 건수 ", example = "")
    private int repCnt;
    @ApiModelProperty(value="성적서발급완료 금액 ", example = "")
    private long repAmt;
    @ApiModelProperty(value=" ", example = "")
    private int etcCnt;
    @ApiModelProperty(value=" ", example = "")
    private long etcAmt;
    @ApiModelProperty(value=" ", example = "")
    private int cnt;
    @ApiModelProperty(value=" ", example = "")
    private long amt;
    
  }
}
