package egovframework.sls.service;

import java.time.LocalDateTime;

import javax.persistence.Column;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(description = "매출")
public class Sls {

  @ApiModelProperty(value = "SLS_YM ", example = "", hidden = true)
  @Column
  private String slsYm;


  @ApiModelProperty(value = "SLS_SEQ ", example = "", hidden = true)
  @Column
  private int slsSeq;


  @ApiModelProperty(value = "QUO_YM ", example = "", hidden = true)
  @Column
  private String quoYm;


  @ApiModelProperty(value = "QUO_SEQ ", example = "", hidden = true)
  @Column
  private int quoSeq;


  @ApiModelProperty(value = "미수금 ", example = "", hidden = true)
  @Column
  private int arrears;
  

  @ApiModelProperty(value = "매출확정자 ", example = "", required = true)
  @Column
  private String cnfrmId;


  @ApiModelProperty(value = "매출확정일 ", example = "", hidden = true)
  @Column
  private String cnfrmDate;


  @ApiModelProperty(value = "등록자 아이디 ", example = "", hidden = true)
  @Column
  private String insMemId;


  @ApiModelProperty(value = "등록 날짜시간 ", example = "", hidden = true)
  @Column
  private LocalDateTime insDt;


  @ApiModelProperty(value = "수정자 아이디 ", example = "", hidden = true)
  @Column
  private String udtMemId;


  @ApiModelProperty(value = "수정 날짜시간 ", example = "", hidden = true)
  @Column
  private LocalDateTime udtDt;


  @ApiModelProperty(value = "상태(I,U,D) ", example = "", hidden = true)
  @Column
  private String state;

}
