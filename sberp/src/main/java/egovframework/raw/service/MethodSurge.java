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
public class MethodSurge {
  @ApiModelProperty(value = "SURGE_SEQ ", example = "")
  @Column
  private int surgeSeq;


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


  @ApiModelProperty(value = "가변우뮤_무_YN ", example = "")
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


  @ApiModelProperty(value = "교류전원 단자 > Line to Line ", example = "")
  @Column
  private String powerLine;


  @ApiModelProperty(value = "교류전원 단자 > Line to PE ", example = "")
  @Column
  private String powerPe;


  @ApiModelProperty(value = "직류전원 단자 > Positive to Negative ", example = "")
  @Column
  private String dcPowerPn;


  @ApiModelProperty(value = "신호선 및 통신단자 > Line to PE ", example = "")
  @Column
  private String commTmlPe;


  @ApiModelProperty(value = "신호선 및 통신단자 >  Line to Line ", example = "")
  @Column
  private String commTmlLine;


  @ApiModelProperty(value = "개방회로_50_YN ", example = "")
  @Column
  private int openCrct50Yn;


  @ApiModelProperty(value = "개방회로_700_YN ", example = "")
  @Column
  private int openCrct700Yn;


  @ApiModelProperty(value = "단락회로_20_YN ", example = "")
  @Column
  private int shrtCrct20Yn;


  @ApiModelProperty(value = "단락회로_ETC ", example = "")
  @Column
  private String shrtCrctEtc;


  @ApiModelProperty(value = "단락회로_ETC ", example = "")
  @Column
  private int shrtCrctEtcYn;


  @ApiModelProperty(value = "인가회수_YN ", example = "")
  @Column
  private int acrdtYn;


  @ApiModelProperty(value = "위상_0_YN ", example = "")
  @Column
  private int phase0Yn;


  @ApiModelProperty(value = "위상_90_YN ", example = "")
  @Column
  private int phase90Yn;


  @ApiModelProperty(value = "위상_180_YN ", example = "")
  @Column
  private int phase180Yn;


  @ApiModelProperty(value = "위상_270_YN ", example = "")
  @Column
  private int phase270Yn;


  @ApiModelProperty(value = "반복률_20_YN ", example = "")
  @Column
  private int rpttn20Yn;


  @ApiModelProperty(value = "반복률_60_YN ", example = "")
  @Column
  private int rpttn60Yn;


  @ApiModelProperty(value = "성능평가A_YN ", example = "")
  @Column
  private int evltnAYn;


  @ApiModelProperty(value = "성능평가B_YN ", example = "")
  @Column
  private int evltnBYn;


  @ApiModelProperty(value = "성능평가C_YN ", example = "")
  @Column
  private int evltnCYn;


  @ApiModelProperty(value = "COUPLING_18_YN ", example = "")
  @Column
  private int coupling18Yn;


  @ApiModelProperty(value = "COUPLING_10_YN ", example = "")
  @Column
  private int coupling10Yn;


  @ApiModelProperty(value = "COUPLING_40_YN ", example = "")
  @Column
  private int coupling40Yn;


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


  @ApiModelProperty(value = "시험자의견_A_YN ", example = "1")
  @Column
  private int cmntAYn;


  @ApiModelProperty(value = "시험자의견_B_YN ", example = "")
  @Column
  private String cmntEtc;


  @ApiModelProperty(value = "시험자의견_B_YN ", example = "")
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
