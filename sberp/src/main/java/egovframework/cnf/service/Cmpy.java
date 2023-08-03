package egovframework.cnf.service;

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
@ApiModel(description = "회사")
public class Cmpy {

  @ApiModelProperty(value = "CMPY_SEQ ", example = "")
  @Column
  @NotNull
  private int cmpySeq;


  @ApiModelProperty(value = "회사타입 0000 협력사(컨설팅) 1000 직접고객 ", example = "")
  @Column
  private String cmpyCode;


  @ApiModelProperty(value = "업체명 ", example = "", required = true)
  @Column
  @NotNull
  private String cmpyName;


  @ApiModelProperty(value = "영문회사명 ", example = "")
  @Column
  private String engName;


  @ApiModelProperty(value = "회사종류(공통코드:PK) 01 컨설팅 02 시험소 03 협력사 ", example = "")
  @Column
  private String typeCode;


  @ApiModelProperty(value = "신청서종류 ", example = "")
  @Column
  private String applType;


  @ApiModelProperty(value = "전화번호 ", example = "")
  @Column
  private String cmpyPhone;


  @ApiModelProperty(value = "팩스번호 ", example = "")
  @Column
  private String cmpyFax;


  @ApiModelProperty(value = "회사식별부호 ", example = "")
  @Column
  private String cmpnyIdntf;


  @ApiModelProperty(value = "기타아이디 ", example = "")
  @Column
  private String etcId;


  @ApiModelProperty(value = "사업자등록번호 ", example = "")
  @Column
  private String bsnsRgnmb;


  @ApiModelProperty(value = "법인번호 ", example = "")
  @Column
  private String crprtRgnmb;


  @ApiModelProperty(value = "국가_CODE(공통코드:PC) 01 한국 02 중국 03 대만 04 미국 05 일본 06 브라질 07 이탈리아 ",
      example = "")
  @Column
  private String cntryCode;


  @ApiModelProperty(value = "홈페이지주소 ", example = "")
  @Column
  private String homepage;


  @ApiModelProperty(value = "대표이사명 ", example = "")
  @Column
  private String rprsn;


  @ApiModelProperty(value = "영문대표이사명 ", example = "")
  @Column
  private String engRprsn;


  @ApiModelProperty(value = "대표이사주민번호 ", example = "")
  @Column
  private String rsdntRgnmb;


  @ApiModelProperty(value = "주소 ", example = "")
  @Column
  private String addr;


  @ApiModelProperty(value = "영문주소 ", example = "")
  @Column
  private String engAddr;


  @ApiModelProperty(value = "기타 ", example = "")
  @Column
  private String memo;


  @ApiModelProperty(value = "등록자 아이디 ", example = "")
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
