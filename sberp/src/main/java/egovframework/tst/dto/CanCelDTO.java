package egovframework.tst.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CanCelDTO {


  @ApiModelProperty(value = "시험규격 고유번호")
  private int testItemSeq;

  @ApiModelProperty(value = "부서명")
  private String insPos;

  @ApiModelProperty(value = "사용자명")
  private String insName;

  @ApiModelProperty(value = "사유")
  private String memo;
  
  @ApiModelProperty(value = "청구액")
  private int chrgs;
  
  @ApiModelProperty(value = "순매출")
  private int netSales;
  
  @ApiModelProperty(value = "취소금액")
  private int cancelFee;
 
  @ApiModelProperty(value = "견적서 청구액")
  private int costTotal;
  
  @ApiModelProperty(value = "등록자 아이디 ", hidden = true)
  private String insMemId;

  @ApiModelProperty(value = "수정자 아이디 ", hidden = true)
  private String udtMemId;
}
