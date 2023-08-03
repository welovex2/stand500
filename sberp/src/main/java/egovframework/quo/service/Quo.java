package egovframework.quo.service;

import java.time.LocalDateTime;

import javax.persistence.Column;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Quo {


  @ApiModelProperty(value = "견적서 발행년월", example = "")
  @Column
  private String quoYm;


  @ApiModelProperty(value = "견적서 번호", example = "")
  @Column
  private int quoSeq;


  @ApiModelProperty(value = "취합견석서 넘버링 ", example = "")
  @Column
  private int chqSeq;


  @ApiModelProperty(value = "발행일 ", example = "2023-03-02")
  @Column
  private String issueDt;


  @ApiModelProperty(value = "대상인증 ", example = "KC")
  @Column
  private String trgtCrtfc;


  @ApiModelProperty(value = "제품설명 ", example = "이 제품은 동글이 입니다")
  @Column
  private String prdInf;


  @ApiModelProperty(value = "16A초과거나 3상제품여부 ", example = "1")
  @Column
  private int powerSuplyYn;


  @ApiModelProperty(value = "무게여부  ", example = "0")
  @Column
  private int wghtYn;


  @ApiModelProperty(value = "특이사항 ", example = "이 제품은 \n 특이합니다")
  @Column
  private String memo;


  @ApiModelProperty(value = "영문견적서 특이사항 ", example = "이 제품은 \n 특이합니다")
  @Column
  private String engMemo;

  @ApiModelProperty(value = "부가가치세여부 ", example = "1")
  @Column
  private int vatYn;


  @ApiModelProperty(value = "업무소요주(week) ", example = "2")
  @Column
  private int needWeek;


  @ApiModelProperty(value = "특약조건 ", example = "특약 \n 조건입니다")
  @Column
  private String spclCndtn;


  @ApiModelProperty(value = "서명이미지url ", example = "")
  @Column
  private String sgnUrl;


  @ApiModelProperty(value = "견적서상태 :1 매출확정, 2 수정요청, 3 수정허가, 4 수정완료 ", example = "0")
  @Column
  private String quoStateCode;


  @ApiModelProperty(value = "VERSION ", example = "1")
  @Column
  private String version;


  @ApiModelProperty(value = "예상완료일 ", example = "2023-04-05")
  @Column
  private String estCmpDt;

  @ApiModelProperty(value = "청구액총합 ", example = "3000000")
  @Column
  private String CostTotal;

  @ApiModelProperty(value = "VAT포함 총합계 ", example = "3300000")
  @Column
  private String TotalVat;

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
