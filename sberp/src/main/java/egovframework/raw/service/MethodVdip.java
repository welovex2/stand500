package egovframework.raw.service;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MethodVdip {

  @ApiModelProperty(value = "VDIP_SEQ ", example = "")
  @Column
  @NotNull
  private int vdipSeq;


  @ApiModelProperty(value = "RAW_SEQ ", example = "")
  @Column
  @NotNull
  private int rawSeq;


  @ApiModelProperty(value = "시험전압_AC_YN ", example = "")
  @Column
  private int vltAcYn;


  @ApiModelProperty(value = "시험전압_기타_YN ", example = "")
  @Column
  private String vltEtc;


  @ApiModelProperty(value = "시험전압_기타_YN ", example = "")
  @Column
  private int vltEtcYn;


  @ApiModelProperty(value = "가변유무_무_YN ", example = "")
  @Column
  private int varNYn;


  @ApiModelProperty(value = "가변유무_유_YN ", example = "")
  @Column
  private int varYYn;


  @ApiModelProperty(value = "ROOM1_YN ", example = "")
  @Column
  private int room1Yn;


  @ApiModelProperty(value = "ROOM2_YN ", example = "")
  @Column
  private int room2Yn;


  @ApiModelProperty(value = "ROOM_ETC ", example = "")
  @Column
  private String roomEtc;


  @ApiModelProperty(value = "ROOM_ETC ", example = "")
  @Column
  private int roomEtcYn;


  @ApiModelProperty(value = "온도 ", example = "")
  @Column
  private String temp;


  @ApiModelProperty(value = "온도 ", example = "")
  @Column
  private String tempPlus;


  @ApiModelProperty(value = "습도 ", example = "")
  @Column
  private String hmdt;


  @ApiModelProperty(value = "습도 ", example = "")
  @Column
  private String hmdtPlus;


  @ApiModelProperty(value = "기압 ", example = "")
  @Column
  private String brmt;


  @ApiModelProperty(value = "기압 ", example = "")
  @Column
  private String brmtPlus;


  @ApiModelProperty(value = "측정년 ", example = "")
  @Column
  private int msrmnYear;


  @ApiModelProperty(value = "측정월 ", example = "")
  @Column
  private int msrmnMon;


  @ApiModelProperty(value = "측정일 ", example = "")
  @Column
  private int msrmnDay;


  @ApiModelProperty(value = "츨정시 ", example = "")
  @Column
  private int msrmnHour;


  @ApiModelProperty(value = "측정분 ", example = "")
  @Column
  private int msrmnMin;


  @ApiModelProperty(value = "서명 ", example = "")
  @Column
  private String sign;


  @ApiModelProperty(value = "서명_URL ", example = "")
  @Column
  private String signUrl;


  @ApiModelProperty(value = "시험결과_CODE ", example = "")
  @Column
  private String resultCode;


  @ApiModelProperty(value = "특이사항 기재 ", example = "")
  @Column
  private int memoYn;


  @ApiModelProperty(value = "특이사항 기재 ", example = "")
  @Column
  private String memo;


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

  @ApiModelProperty(value = "95 % 초과 (전압강하) > 성능평가결과 ", example = "")
  @Column
  private String vol1ResultCode;


  @ApiModelProperty(value = "95 % 초과 (전압강하) > 시험자의견 코드 (공통코드 RV)", example = "")
  @Column
  private String vol1CmntCode;


  @ApiModelProperty(value = "95 % 초과 (전압강하) > 시험자의견 ", example = "")
  @Column
  private String vol1Cmnt;


  @ApiModelProperty(value = "30 % (전압강하) > 성능평가결과 ", example = "")
  @Column
  private String vol2ResultCode;


  @ApiModelProperty(value = "30 % (전압강하) > 시험자의견 코드 (공통코드 RV) ", example = "")
  @Column
  private String vol2CmntCode;


  @ApiModelProperty(value = "30 % (전압강하) > 시험자의견 ", example = "")
  @Column
  private String vol2Cmnt;


  @ApiModelProperty(value = "95 % 초과 (순간정전) > 성능평가결과 ", example = "")
  @Column
  private String vol3ResultCode;


  @ApiModelProperty(value = "95 % 초과 (순간정전) > 시험자의견 코드 (공통코드 RV) ", example = "")
  @Column
  private String vol3CmntCode;


  @ApiModelProperty(value = "95 % 초과 (순간정전) > 시험자의견 ", example = "")
  @Column
  private String vol3Cmnt;

  @ApiModelProperty(value = "", example = "")
  @Column
  private String vol4ResultCode;


  @ApiModelProperty(value = "", example = "")
  @Column
  private String vol4CmntCode;


  @ApiModelProperty(value = "", example = "")
  @Column
  private String vol4Cmnt;

  @ApiModelProperty(value = "", example = "")
  @Column
  private String vol4Type;
  
}
