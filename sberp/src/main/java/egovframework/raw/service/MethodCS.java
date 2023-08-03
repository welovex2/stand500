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
public class MethodCS {

  @ApiModelProperty(value = "CS_SEQ ", example = "")
  @Column
  private int csSeq;


  @ApiModelProperty(value = "RAW_SEQ ", example = "")
  @Column
  private int rawSeq;


  @ApiModelProperty(value = "시험전압_AC_YN ", example = "")
  @Column
  private int vltAcYn;


  @ApiModelProperty(value = "시험전압_ETC ", example = "")
  @Column
  private String vltEtc;


  @ApiModelProperty(value = "시험전압_ETC ", example = "")
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


  @ApiModelProperty(value = "전계강도_1V_YN ", example = "")
  @Column
  private int strn1vYn;


  @ApiModelProperty(value = "전계강도_3V_YN ", example = "")
  @Column
  private int strn3vYn;


  @ApiModelProperty(value = "전계강도_10V_YN ", example = "")
  @Column
  private int strn10vYn;


  @ApiModelProperty(value = "전계강도_ETC ", example = "")
  @Column
  private String strnEtc;


  @ApiModelProperty(value = "전계강도_ETC ", example = "")
  @Column
  private int strnEtcYn;


  @ApiModelProperty(value = "스위프율15_YN ", example = "")
  @Column
  private int swep15Yn;


  @ApiModelProperty(value = "스위프율1_YN ", example = "")
  @Column
  private int swep1Yn;


  @ApiModelProperty(value = "변조_80_YN ", example = "")
  @Column
  private int mdlt80Yn;


  @ApiModelProperty(value = "변조_ETC ", example = "")
  @Column
  private String mdltEtc;


  @ApiModelProperty(value = "변조_ETC ", example = "")
  @Column
  private int mdltEtcYn;


  @ApiModelProperty(value = "성능평가기준_A_YN ", example = "")
  @Column
  private int evltnAYn;


  @ApiModelProperty(value = "성능평가기준_B_YN ", example = "")
  @Column
  private int evltnBYn;


  @ApiModelProperty(value = "성능평가기준_C_YN ", example = "")
  @Column
  private int evltnCYn;


  @ApiModelProperty(value = "주파수범위 ", example = "")
  @Column
  private String frqncArea;


  @ApiModelProperty(value = "특정주파수 ", example = "")
  @Column
  private String spcFrqnc;


  @ApiModelProperty(value = "측정년 ", example = "")
  @Column
  private int msrmnYear;


  @ApiModelProperty(value = "측정월 ", example = "")
  @Column
  private int msrmnMon;


  @ApiModelProperty(value = "측정일 ", example = "")
  @Column
  private int msrmnDay;


  @ApiModelProperty(value = "측정시 ", example = "")
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

  @ApiModelProperty(value = "시험자의견_A_YN ", example = "")
  @Column
  private int cmntAYn;


  @ApiModelProperty(value = "시험자의견_ETC ", example = "")
  @Column
  private String cmntEtc;


  @ApiModelProperty(value = "시험자의견_ETC ", example = "")
  @Column
  private int cmntEtcYn;


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
