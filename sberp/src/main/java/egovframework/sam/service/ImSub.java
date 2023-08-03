package egovframework.sam.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.persistence.Column;
import org.springmodules.validation.bean.conf.loader.annotation.handler.NotNull;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class ImSub {
  
  @ApiModelProperty(value="IM_SEQ + 01 ", example = "")
  @Column
  @NotNull
  private int imSubSeq;


  @ApiModelProperty(value="IM_ID ", example = "")
  @Column
  @NotNull
  private int imId;


  @ApiModelProperty(value="본품/구성품(공통코드:TI) 01 본품 02 구성품 ", example = "")
  @Column
  private String divCode;


  @ApiModelProperty(value="수량 ", example = "")
  @Column
  private int qty;


  @ApiModelProperty(value="모델명/설명 ", example = "")
  @Column
  private String memo;


  @ApiModelProperty(value="반입날짜 ", example = "")
  @Column
  private String carryInDate;


  @ApiModelProperty(value="반입자 ", example = "")
  @Column
  private String carryInId;


  @ApiModelProperty(value="반입형태(공통코드:TC) 01 택배 02 직접전달 ", example = "")
  @Column
  private String carryInCode;


  @ApiModelProperty(value="배송인 ", example = "")
  @Column
  private String carryInDlvryName;


  @ApiModelProperty(value="배송인연락처 ", example = "")
  @Column
  private String carryInDlvryPhone;


  @ApiModelProperty(value="송장 ", example = "")
  @Column
  private String carryInDlvryInvc;


  @ApiModelProperty(value="CARRY_IN_MEMO ", example = "")
  @Column
  private String carryInMemo;


  @ApiModelProperty(value="반출날짜 ", example = "")
  @Column
  private String carryOutDate;


  @ApiModelProperty(value="반출자 ", example = "")
  @Column
  private String carryOutId;


  @ApiModelProperty(value="반출형태(공통코드:TC)\n01 택배\n02 직접전달 ", example = "")
  @Column
  private String carryOutCode;


  @ApiModelProperty(value="반출배송인 ", example = "")
  @Column
  private String carryOutDlvryName;


  @ApiModelProperty(value="반출배송인연락처 ", example = "")
  @Column
  private String carryOutDlvryPhone;


  @ApiModelProperty(value="반출송장 ", example = "")
  @Column
  private String carryOutDlvryInvc;


  @ApiModelProperty(value="CARRY_OUT_MEMO ", example = "")
  @Column
  private String carryOutMemo;


  @ApiModelProperty(value="등록자 아이디 ", example = "", hidden = true)
  @Column
  private String insMemId;


  @ApiModelProperty(value="등록 날짜시간 ", example = "", hidden = true)
  @Column
  private LocalDateTime insDt;


  @ApiModelProperty(value="수정자 아이디 ", example = "", hidden = true)
  @Column
  private String udtMemId;


  @ApiModelProperty(value="수정 날짜시간 ", example = "", hidden = true)
  @Column
  private LocalDateTime udtDt;


  @ApiModelProperty(value="상태(I,U,D) ", example = "", hidden = true)
  @Column
  private String state;


}
