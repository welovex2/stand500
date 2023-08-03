package egovframework.tst.service;

import java.time.LocalDateTime;

import javax.persistence.Column;

import org.springmodules.validation.bean.conf.loader.annotation.handler.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel(description = "시험항목")
public class TestItem {

  @ApiModelProperty(value = "TEST_ITEM_SEQ", notes = "신규등록시 값 보내지 않음, 수정이나 삭제시 필수", example = "")
  @Column
  @NotNull
  private int testItemSeq;

  @ApiModelProperty(value = "QUO_YM ", example = "", hidden = true)
  @Column
  @NotNull
  private String quoYm;

  @ApiModelProperty(value = "QUO_SEQ ", example = "", hidden = true)
  @Column
  @NotNull
  private int quoSeq;


  @ApiModelProperty(value = "시험규격번호", example = "")
  @Column
  @NotNull
  private int testStndrSeq;


  @ApiModelProperty(value = "인증종류3 번호", example = "")
  @Column
  @NotNull
  private int crtfTypeSeq;

  @ApiModelProperty(value = "시험부 코드", example = "")
  @Column
  @NotNull
  private String testTypeCode;


  @ApiModelProperty(value = "제품명 ", example = "")
  @Column
  private String product;


  @ApiModelProperty(value = "모델명 ", example = "")
  @Column
  private String model;


  @ApiModelProperty(value = "메모 ", example = "")
  @Column
  private String memo;

  @ApiModelProperty(value = "기술책임자 아이디 ", example = "")
  @Column
  private String revId;

  @ApiModelProperty(value = "기술책임서명 ", example = "")
  @Column
  private String revSignUrl;


  @ApiModelProperty(value = "서명요청상태(공통코드:SS)", notes = "01 서명요청전 02 서명요청완료 ", example = "")
  @Column
  private String signStateCode;


  @ApiModelProperty(value = "접수비 ", example = "")
  @Column
  private int fee;


  @ApiModelProperty(value = "면허세 ", example = "")
  @Column
  private int lcnsTax;


  @ApiModelProperty(value = "시험비 ", example = "")
  @Column
  private int testFee;


  @ApiModelProperty(value = "청구액 ", example = "")
  @Column
  private int chrgs;


  @ApiModelProperty(value = "대납분 ", example = "")
  @Column
  private int advncPymnt;


  @ApiModelProperty(value = "특별할인 ", example = "")
  @Column
  private int spclDscnt;


  @ApiModelProperty(value = "컨설팅비 ", example = "")
  @Column
  private int cnsltFee;


  @ApiModelProperty(value = "외주비 ", example = "")
  @Column
  private int otsrcFee;


  @ApiModelProperty(value = "순매출 ", example = "")
  @Column
  private int netSales;


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


  @ApiModelProperty(value = "상태", notes = "I:신규등록, U:수정, D:삭제한 항목", example = "")
  @Column
  private String state;

}
