package egovframework.sts.dto;

import java.util.List;
import egovframework.sts.dto.StsDTO.TestTypeList;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SmdDTO {

  @ApiModelProperty(value="구분 ", example = "")
  private String gubun;
  
  @ApiModelProperty(value="시험원 데이터 ", example = "")
  private List<memState> memState;
  
  @Getter
  @Setter
  @ApiModel(value = "SmdDTO .Res", description = "시험원 데이터")
  public static class memState {
    @ApiModelProperty(value="아이디 ", example = "")
    private String memId;

    @ApiModelProperty(value="이름 ", example = "")
    private String memName;
    
    @ApiModelProperty(value="월별 데이터 ", example = "")
    private List<MonState> stateList;
  }
  
  @Getter
  @Setter
  @ApiModel(value = "SmdDTO .Res", description = "시험원 월별 데이터")
  public static class MonState {
    
    @ApiModelProperty(value="타입 ", example = "")
    private String typeCode;
    
    @ApiModelProperty(value="총 건수 ", example = "")
    private long totalCnt;
    @ApiModelProperty(value="총 금액 ", example = "")
    private long totalAmt;
    
    @ApiModelProperty(value="1월 건수 ", example = "")
    private int mon1Cnt;
    @ApiModelProperty(value="1월 금액 ", example = "")
    private int mon1Amt;

    @ApiModelProperty(value="2월 건수 ", example = "")
    private int mon2Cnt;
    @ApiModelProperty(value="2월 금액 ", example = "")
    private int mon2Amt;

    @ApiModelProperty(value="3월 건수 ", example = "")
    private int mon3Cnt;
    @ApiModelProperty(value="3월 금액 ", example = "")
    private int mon3Amt;

    @ApiModelProperty(value="4월 건수 ", example = "")
    private int mon4Cnt;
    @ApiModelProperty(value="4월 금액 ", example = "")
    private int mon4Amt;

    @ApiModelProperty(value="5월 건수 ", example = "")
    private int mon5Cnt;
    @ApiModelProperty(value="5월 금액 ", example = "")
    private int mon5Amt;

    @ApiModelProperty(value="6월 건수 ", example = "")
    private int mon6Cnt;
    @ApiModelProperty(value="6월 금액 ", example = "")
    private int mon6Amt;

    @ApiModelProperty(value="7월 건수 ", example = "")
    private int mon7Cnt;
    @ApiModelProperty(value="7월 금액 ", example = "")
    private int mon7Amt;

    @ApiModelProperty(value="8월 건수 ", example = "")
    private int mon8Cnt;
    @ApiModelProperty(value="8월 금액 ", example = "")
    private int mon8Amt;

    @ApiModelProperty(value="9월 건수 ", example = "")
    private int mon9Cnt;
    @ApiModelProperty(value="9월 금액 ", example = "")
    private int mon9Amt;

    @ApiModelProperty(value="10월 건수 ", example = "")
    private int mon10Cnt;
    @ApiModelProperty(value="10월 금액 ", example = "")
    private int mon10Amt;

    @ApiModelProperty(value="11월 건수 ", example = "")
    private int mon11Cnt;
    @ApiModelProperty(value="11월 금액 ", example = "")
    private int mon11Amt;

    @ApiModelProperty(value="12월 건수 ", example = "")
    private int mon12Cnt;
    @ApiModelProperty(value="12월 금액 ", example = "")
    private int mon12Amt;
    
  }
  
}
