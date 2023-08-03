package egovframework.raw.service;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel(description = "로데이터 > M-Field")
@Getter @Setter @ToString
public class MethodMf {
	
	@ApiModelProperty(value="MF_SEQ ", example = "")
	@Column
	@NotNull
	private int mfSeq;


	@ApiModelProperty(value="RAW_SEQ ", example = "")
	@Column
	@NotNull
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


	@ApiModelProperty(value="가변유무_무_YN ", example = "")
	@Column
	private int varNYn;


	@ApiModelProperty(value="가변유무_유_YN ", example = "")
	@Column
	private int varYYn;


	@ApiModelProperty(value="ROOM1_YN ", example = "")
	@Column
	private int room1Yn;


	@ApiModelProperty(value="ROOM2_YN ", example = "")
	@Column
	private int room2Yn;


	@ApiModelProperty(value="ROOM_ETC ", example = "")
	@Column
	private String roomEtc;


	@ApiModelProperty(value="ROOM_ETC ", example = "")
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


	@ApiModelProperty(value="자기장_A_YN ", example = "")
	@Column
	private int mgntAYn;


	@ApiModelProperty(value="자기장_3_YN ", example = "")
	@Column
	private int mgnt3Yn;


	@ApiModelProperty(value="자기장_30_YN ", example = "")
	@Column
	private int mgnt30Yn;


	@ApiModelProperty(value="주파수_50_YN ", example = "")
	@Column
	private int frqnc50Yn;


	@ApiModelProperty(value="주파수_60_YN ", example = "")
	@Column
	private int frqnc60Yn;


	@ApiModelProperty(value="AXIS_X_YN ", example = "")
	@Column
	private int axisXYn;


	@ApiModelProperty(value="AXIS_Y_YN ", example = "")
	@Column
	private int axisYYn;


	@ApiModelProperty(value="AXIS_Z_YN ", example = "")
	@Column
	private int axisZYn;


	@ApiModelProperty(value="인가시간 ", example = "")
	@Column
	private String acrdt;

	
	@ApiModelProperty(value="Short Field (1-3 sec) ", example = "")
	@Column
	private int sf300Yn;


	@ApiModelProperty(value="Short Field (1-3 sec) ", example = "")
	@Column
	private int sfEtcYn;


	@ApiModelProperty(value="Short Field (1-3 sec) ", example = "")
	@Column
	private String sfEtc;


	@ApiModelProperty(value="성능평가기준 A ", example = "")
	@Column
	private int evltnAYn;


	@ApiModelProperty(value="성능평가기준 B ", example = "")
	@Column
	private int evltnBYn;


	@ApiModelProperty(value="성능평가기준 C ", example = "")
	@Column
	private int evltnCYn;

	
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


	@ApiModelProperty(value="서명URL ", example = "")
	@Column
	private String signUrl;


	@ApiModelProperty(value="시험결과_CODE ", example = "")
	@Column
	private String resultCode;


	@ApiModelProperty(value="X_기준 ", example = "")
	@Column
	private String xStnd;


	@ApiModelProperty(value="X_성능결과_CODE ", example = "")
	@Column
	private String xCode;


	@ApiModelProperty(value="X_MEMO ", example = "")
	@Column
	private String xMemo;


	@ApiModelProperty(value="Y_기준 ", example = "")
	@Column
	private String yStnd;


	@ApiModelProperty(value="Y_성능결과_CODE ", example = "")
	@Column
	private String yCode;


	@ApiModelProperty(value="Y_MEMO ", example = "")
	@Column
	private String yMemo;


	@ApiModelProperty(value="Z_기준 ", example = "")
	@Column
	private String zStnd;


	@ApiModelProperty(value="Z_성능결과_CODE ", example = "")
	@Column
	private String zCode;


	@ApiModelProperty(value="Z_MEMO ", example = "")
	@Column
	private String zMemo;


	@ApiModelProperty(value="시험자의견_A_YN ", example = "")
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
