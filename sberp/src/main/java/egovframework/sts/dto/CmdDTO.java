package egovframework.sts.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CmdDTO {


  @ApiModelProperty(value="아이디 ", example = "")
  private String cmpySeq;

  @ApiModelProperty(value="이름 ", example = "")
  private String cmpyName;
  
  @ApiModelProperty(value="총건수 ", example = "")
  private int totalCnt;
  @ApiModelProperty(value="총금액 ", example = "")
  private long totalAmt;
  @ApiModelProperty(value="총디버깅 ", example = "")
  private int totalDeb;
  @ApiModelProperty(value="총홀딩 ", example = "")
  private int totalHol;
  @ApiModelProperty(value="총납부금액 ", example = "")
  private long totalPay;
  @ApiModelProperty(value="총미수금액 ", example = "")
  private long totalArr;
  
  @ApiModelProperty(value="1월 건수 ", example = "")
  private int mon1Cnt;
  @ApiModelProperty(value="1월 금액 ", example = "")
  private int mon1Amt;
  @ApiModelProperty(value="1월 납부금액 ", example = "")
  private long mon1Pay;
  @ApiModelProperty(value="1월 미수금액 ", example = "")
  private long mon1Arr;
  
  @ApiModelProperty(value="2월 건수 ", example = "")
  private int mon2Cnt;
  @ApiModelProperty(value="2월 금액 ", example = "")
  private int mon2Amt;
  @ApiModelProperty(value="2월 납부금액 ", example = "")
  private long mon2Pay;
  @ApiModelProperty(value="2월 미수금액 ", example = "")
  private long mon2Arr;

  @ApiModelProperty(value="3월 건수 ", example = "")
  private int mon3Cnt;
  @ApiModelProperty(value="3월 금액 ", example = "")
  private int mon3Amt;
  @ApiModelProperty(value="3월 납부금액 ", example = "")
  private long mon3Pay;
  @ApiModelProperty(value="3월 미수금액 ", example = "")
  private long mon3Arr;

  @ApiModelProperty(value="4월 건수 ", example = "")
  private int mon4Cnt;
  @ApiModelProperty(value="4월 금액 ", example = "")
  private int mon4Amt;
  @ApiModelProperty(value="4월 납부금액 ", example = "")
  private long mon4Pay;
  @ApiModelProperty(value="4월 미수금액 ", example = "")
  private long mon4Arr;

  @ApiModelProperty(value="5월 건수 ", example = "")
  private int mon5Cnt;
  @ApiModelProperty(value="5월 금액 ", example = "")
  private int mon5Amt;
  @ApiModelProperty(value="5월 납부금액 ", example = "")
  private long mon5Pay;
  @ApiModelProperty(value="5월 미수금액 ", example = "")
  private long mon5Arr;

  @ApiModelProperty(value="6월 건수 ", example = "")
  private int mon6Cnt;
  @ApiModelProperty(value="6월 금액 ", example = "")
  private int mon6Amt;
  @ApiModelProperty(value="6월 납부금액 ", example = "")
  private long mon6Pay;
  @ApiModelProperty(value="6월 미수금액 ", example = "")
  private long mon6Arr;
  
  @Getter @Setter
  public static class Sub {
    @ApiModelProperty(value="아이디 ", example = "")
    private String cmpySeq;

    @ApiModelProperty(value="이름 ", example = "")
    private String cmpyName;
    
    @ApiModelProperty(value="월 ", example = "")
    private String mon;
    
    @ApiModelProperty(value="건수 ", example = "")
    private int inCnt;
    @ApiModelProperty(value="총금액 ", example = "")
    private long inAmt;
    @ApiModelProperty(value="납부금액 ", example = "")
    private long pay;
    @ApiModelProperty(value="미수금액 ", example = "")
    private long arr;
  }
}
