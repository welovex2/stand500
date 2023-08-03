package egovframework.raw.service;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel(description = "로데이터 > RS")
@Getter @Setter @ToString
public class MethodRs {
	
	@ApiModelProperty(value="RS_SEQ ", example = "")
	@Column
	private int rsSeq;


	@ApiModelProperty(value="RAW_SEQ ", example = "")
	@Column
	private int rawSeq;


	@ApiModelProperty(value="시험전압_AC_YN ", example = "")
	@Column
	private int vltAcYn;


	@ApiModelProperty(value="시험전압_ETC ", example = "")
	@Column
	private String vltEtc;


	@ApiModelProperty(value="시험전압_ETC ", example = "")
	@Column
	private int vltEtcYn;


	@ApiModelProperty(value="가번유무_무_YN ", example = "")
	@Column
	private int varNYn;


	@ApiModelProperty(value="가변유무_유_YN ", example = "")
	@Column
	private int varYYn;


	@ApiModelProperty(value="RS 반무반사실 #1_YN ", example = "")
	@Column
	private int room1Yn;


	@ApiModelProperty(value="RS 반무반사실 #2_YN ", example = "")
	@Column
	private int room2Yn;


	@ApiModelProperty(value="시험장소_TEXT ", example = "")
	@Column
	private String roomEtc;


	@ApiModelProperty(value="시험장소_YN ", example = "")
	@Column
	private int roomEtcYn;


	@ApiModelProperty(value="온도 ", example = "")
	@Column
	private String temp;

	
	@ApiModelProperty(value="온도 ", example = "")
	@Column
	private String tempPlus;
	
	
	@ApiModelProperty(value="습도 ", example = "")
	@Column
	private String hmdt;

	
	@ApiModelProperty(value="습도 ", example = "")
	@Column
	private String hmdtPlus;
	
	
	@ApiModelProperty(value="기압 ", example = "")
	@Column
	private String brmt;


	@ApiModelProperty(value="기압 ", example = "")
	@Column
	private String brmtPlus;
	
	
	@ApiModelProperty(value="변조AM_YN ", example = "")
	@Column
	private int mdltAmYn;


	@ApiModelProperty(value="변조_ETC ", example = "")
	@Column
	private String mdltEtc;


	@ApiModelProperty(value="변조_ETC ", example = "")
	@Column
	private int mdltEtcYn;


	@ApiModelProperty(value="안테나_1M ", example = "")
	@Column
	private int antn1mYn;


	@ApiModelProperty(value="안테나_3M ", example = "")
	@Column
	private int antn3mYn;


	@ApiModelProperty(value="스위프1.5 x 10-3 decades/s_YN ", example = "")
	@Column
	private int swep15Yn;


	@ApiModelProperty(value="스위프_1S_YN ", example = "")
	@Column
	private int swep1sYn;


	@ApiModelProperty(value="전계강도_1_YN ", example = "")
	@Column
	private int strn1Yn;


	@ApiModelProperty(value="전계강도_3_YN ", example = "")
	@Column
	private int strn3Yn;


	@ApiModelProperty(value="전계강도_10_YN ", example = "")
	@Column
	private int strn10Yn;


	@ApiModelProperty(value="전계강도_ETC ", example = "")
	@Column
	private String strnEtc;


	@ApiModelProperty(value="전계강도_ETC ", example = "")
	@Column
	private int strnEtcYn;


	@ApiModelProperty(value="인가부위_FRONT ", example = "")
	@Column
	private int acrdtFrontYn;


	@ApiModelProperty(value="인가부위_BACK ", example = "")
	@Column
	private int acrdtBackYn;


	@ApiModelProperty(value="인가부위_LEFT ", example = "")
	@Column
	private int acrdtLeftYn;


	@ApiModelProperty(value="인가부위_RIGHT ", example = "")
	@Column
	private int acrdtRightYn;


	@ApiModelProperty(value="인가부위_ETC ", example = "")
	@Column
	private String acrdtEtc;


	@ApiModelProperty(value="인가부위_ETC ", example = "")
	@Column
	private int acrdtEtcYn;


	@ApiModelProperty(value="성능평가기준_A_YN ", example = "")
	@Column
	private int evltnAYn;


	@ApiModelProperty(value="성능평가기준_B_YN ", example = "")
	@Column
	private int evltnBYn;


	@ApiModelProperty(value="성능평가기준_C_YN ", example = "")
	@Column
	private int evltnCYn;


	@ApiModelProperty(value="주파수범위 ", example = "")
	@Column
	private String frqncArea;


	@ApiModelProperty(value="특정주파수 ", example = "")
	@Column
	private String spcFrqnc;


	@ApiModelProperty(value="측정년 ", example = "")
	@Column
	private int msrmnYear;


	@ApiModelProperty(value="측정월 ", example = "")
	@Column
	private int msrmnMon;


	@ApiModelProperty(value="측정일 ", example = "")
	@Column
	private int msrmnDay;


	@ApiModelProperty(value="측정시 ", example = "")
	@Column
	private int msrmnHour;


	@ApiModelProperty(value="측정분 ", example = "")
	@Column
	private int msrmnMin;


	@ApiModelProperty(value="서명 ", example = "")
	@Column
	private String sign;


	@ApiModelProperty(value="서명_URL ", example = "")
	@Column
	private String signUrl;


	@ApiModelProperty(value="시험결과_CODE ", example = "")
	@Column
	private String resultCode;


	@ApiModelProperty(value="수평_전면_CODE ", example = "A")
	@Column
	private String hFCode;


	@ApiModelProperty(value="수평_후면_CODE ", example = "B")
	@Column
	private String hBCode;


	@ApiModelProperty(value="수평_우측_CODE ", example = "C")
	@Column
	private String hRCode;


	@ApiModelProperty(value="수평_좌측_CODE ", example = "A")
	@Column
	private String hLCode;


	@ApiModelProperty(value="수직_전면_CODE ", example = "B")
	@Column
	private String vFCode;


	@ApiModelProperty(value="수직_후면_CODE ", example = "C")
	@Column
	private String vBCode;


	@ApiModelProperty(value="수직_우측_CODE ", example = "A")
	@Column
	private String vRCode;


	@ApiModelProperty(value="수직_좌측_CODE ", example = "B")
	@Column
	private String vLCode;


	@ApiModelProperty(value="시험자의견A_YN ", example = "")
	@Column
	private int cmntAYn;


	@ApiModelProperty(value="시험자의견_ETC ", example = "")
	@Column
	private String cmntEtc;


	@ApiModelProperty(value="시험자의견_ETC ", example = "")
	@Column
	private int cmntEtcYn;


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
