package egovframework.raw.service;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel(description = "로데이터 > RE")
@Getter @Setter @ToString
public class MethodRe {
	
	@ApiModelProperty(value="RE_SEQ ", example = "")
	@Column
	private int reSeq;


	@ApiModelProperty(value="RAW_SEQ ", example = "")
	@Column
	private int rawSeq;


	@ApiModelProperty(value="VLT_AC_YN ", example = "")
	@Column
	private int vltAcYn;


	@ApiModelProperty(value="시험전압_ETC ", example = "")
	@Column
	private String vltEtc;

	
	@ApiModelProperty(value="시험전압_ETC ", example = "")
	@Column
	private int vltEtcYn;
	
	
	@ApiModelProperty(value="가변유무N ", example = "")
	@Column
	private int varNYn;


	@ApiModelProperty(value="가변유무Y_YN ", example = "")
	@Column
	private int varYYn;


	@ApiModelProperty(value="대역1_CODE ", example = "")
	@Column
	private String hz1Code;


	@ApiModelProperty(value="대역1_시험전압10M_YN ", example = "")
	@Column
	private int hz1Vlt10mYn;


	@ApiModelProperty(value="대역1_시험전압3M_YN ", example = "")
	@Column
	private int hz1Vlt3mYn;


	@ApiModelProperty(value="대역1_시험전압_ETC1 ", example = "")
	@Column
	private String hz1VltEtc1;


	@ApiModelProperty(value="대역1_시험전압_ETC1 ", example = "")
	@Column
	private int hz1VltEtc1Yn;


	@ApiModelProperty(value="대역1_시험전압_ETC2 ", example = "")
	@Column
	private String hz1VltEtc2;


	@ApiModelProperty(value="대역1_시험전압_ETC2 ", example = "")
	@Column
	private int hz1VltEtc2Yn;


	@ApiModelProperty(value="대역1_측정거리_1M ", example = "")
	@Column
	private int hz1Lngth1mYn;


	@ApiModelProperty(value="대역1_측정거리_3M ", example = "")
	@Column
	private int hz1Lngth3mYn;


	@ApiModelProperty(value="대역1_측정거리_10M ", example = "")
	@Column
	private int hz1Lngth10mYn;


	@ApiModelProperty(value="대역1_측정거리_30M ", example = "")
	@Column
	private int hz1Lngth30mYn;


	@ApiModelProperty(value="대역1_안테나_2M ", example = "")
	@Column
	private int hz1Antn2mYn;


	@ApiModelProperty(value="대역1_안테나_3M ", example = "")
	@Column
	private int hz1Antn3mYn;


	@ApiModelProperty(value="대역1_안테나_4M ", example = "")
	@Column
	private int hz1Antn4mYn;


	@ApiModelProperty(value="대역1_온도 ", example = "")
	@Column
	private String hz1Temp;

	
	@ApiModelProperty(value="대역1_온도 ", example = "")
	@Column
	private String hz1TempPlus;
	
	
	@ApiModelProperty(value="대역1_습도 ", example = "")
	@Column
	private String hz1Hmdt;


	@ApiModelProperty(value="대역1_습도 ", example = "")
	@Column
	private String hz1HmdtPlus;
	
	
	@ApiModelProperty(value="대역1_측정년 ", example = "")
	@Column
	private int hz1MsrmnYear;


	@ApiModelProperty(value="대역1_측년월 ", example = "")
	@Column
	private int hz1MsrmnMon;


	@ApiModelProperty(value="대역1_측정일 ", example = "")
	@Column
	private int hz1MsrmnDay;


	@ApiModelProperty(value="대역1_측정시 ", example = "")
	@Column
	private int hz1MsrmnHour;


	@ApiModelProperty(value="대역1_측정분 ", example = "")
	@Column
	private int hz1MsrmnMin;


	@ApiModelProperty(value="대역1_서명 ", example = "")
	@Column
	private String hz1Sign;


	@ApiModelProperty(value="대역1_서명_URL ", example = "")
	@Column
	private String hz1SignUrl;


	@ApiModelProperty(value="대역1_시험결과_CODE ", example = "")
	@Column
	private String hz1ResultCode;


	@ApiModelProperty(value="대역2_CODE ", example = "")
	@Column
	private String hz2Code;


	@ApiModelProperty(value="대역2_시험전압10M_YN ", example = "")
	@Column
	private int hz2Vlt10mYn;


	@ApiModelProperty(value="대역2_시험전압3M_YN ", example = "")
	@Column
	private int hz2Vlt3mYn;


	@ApiModelProperty(value="대역2_시험전압_ETC1 ", example = "")
	@Column
	private String hz2VltEtc1;


	@ApiModelProperty(value="대역2_시험전압_ETC1 ", example = "")
	@Column
	private int hz2VltEtc1Yn;


	@ApiModelProperty(value="대역2_시험전압_ETC2 ", example = "")
	@Column
	private String hz2VltEtc2;


	@ApiModelProperty(value="대역2_시험전압_ETC2 ", example = "")
	@Column
	private int hz2VltEtc2Yn;


	@ApiModelProperty(value="대역2_측정거리_1M ", example = "")
	@Column
	private int hz2Lngth1mYn;


	@ApiModelProperty(value="대역2_측정거리_3M ", example = "")
	@Column
	private int hz2Lngth3mYn;


	@ApiModelProperty(value="대역2_측정거리_10M ", example = "")
	@Column
	private int hz2Lngth10mYn;


	@ApiModelProperty(value="대역2_측정거리_30M ", example = "")
	@Column
	private int hz2Lngth30mYn;


	@ApiModelProperty(value="대역2_온도 ", example = "")
	@Column
	private String hz2Temp;


	@ApiModelProperty(value="대역2_온도 ", example = "")
	@Column
	private String hz2TempPlus;

	
	@ApiModelProperty(value="대역2_습도 ", example = "")
	@Column
	private String hz2Hmdt;

	
	@ApiModelProperty(value="대역2_습도 ", example = "")
	@Column
	private String hz2HmdtPlus;
	
	
	@ApiModelProperty(value="대역2_측정년 ", example = "")
	@Column
	private int hz2MsrmnYear;


	@ApiModelProperty(value="대역2_측년월 ", example = "")
	@Column
	private int hz2MsrmnMon;


	@ApiModelProperty(value="대역2_측정일 ", example = "")
	@Column
	private int hz2MsrmnDay;


	@ApiModelProperty(value="대역2_측정시 ", example = "")
	@Column
	private int hz2MsrmnHour;


	@ApiModelProperty(value="대역2_측정분 ", example = "")
	@Column
	private int hz2MsrmnMin;


	@ApiModelProperty(value="대역2_서명 ", example = "")
	@Column
	private String hz2Sign;


	@ApiModelProperty(value="대역2_서명_URL ", example = "")
	@Column
	private String hz2SignUrl;


	@ApiModelProperty(value="대역2_시험결과_CODE ", example = "")
	@Column
	private String hz2ResultCode;


	@ApiModelProperty(value="시험결과_URL ", example = "")
	@Column
	private String resultUrl;


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
