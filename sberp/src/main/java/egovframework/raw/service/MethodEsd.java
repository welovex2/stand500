package egovframework.raw.service;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel(description = "로데이터 > Esd")
@Getter @Setter @ToString
public class MethodEsd {
	
	@ApiModelProperty(value="ESD_SEQ ", example = "")
	@Column
	private int esdSeq;


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


	@ApiModelProperty(value="방전간격1초_YN ", example = "")
	@Column
	private int dc1sYn;


	@ApiModelProperty(value="반전간격_ETC ", example = "")
	@Column
	private String dcEtc;


	@ApiModelProperty(value="반전간격_ETC ", example = "")
	@Column
	private int dcEtcYn;


	@ApiModelProperty(value="방전횟수_기중_YN ", example = "")
	@Column
	private int dcGinYn;


	@ApiModelProperty(value="방전횟수_접촉_YN ", example = "")
	@Column
	private int dcConYn;


	@ApiModelProperty(value="방전임피던스_YN (330W / 150pF ± 10%)", example = "")
	@Column
	private int dcImpYn;


	@ApiModelProperty(value="방전임피던스_ETC ", example = "")
	@Column
	private String dcImpEtc;


	@ApiModelProperty(value="방전임피던스_ETC ", example = "")
	@Column
	private int dcImpEtcYn;


	@ApiModelProperty(value="성능평가기준A_YN ", example = "")
	@Column
	private int evltnAYn;


	@ApiModelProperty(value="성능평가기준B_YN ", example = "")
	@Column
	private int evltnBYn;


	@ApiModelProperty(value="성능평가기준C_YN ", example = "")
	@Column
	private int evltnCYn;


	@ApiModelProperty(value="접촉방전_2_YN ", example = "")
	@Column
	private int dcCon2Yn;


	@ApiModelProperty(value="접촉방전_4_YN ", example = "")
	@Column
	private int dcCon4Yn;


	@ApiModelProperty(value="접촉방전_6_YN ", example = "")
	@Column
	private int dcCon6Yn;


	@ApiModelProperty(value="접촉방전_8_YN ", example = "")
	@Column
	private int dcCon8Yn;


	@ApiModelProperty(value="접촉방전_ETC ", example = "")
	@Column
	private String dcConEtc;


	@ApiModelProperty(value="접촉방전_ETC ", example = "")
	@Column
	private int dcConEtcYn;


	@ApiModelProperty(value="기중반전_2_YN ", example = "")
	@Column
	private int dcGin2Yn;


	@ApiModelProperty(value="기중반전_4_YN ", example = "")
	@Column
	private int dcGin4Yn;


	@ApiModelProperty(value="기중반전_6_YN ", example = "")
	@Column
	private int dcGin6Yn;


	@ApiModelProperty(value="기중반전_8_YN ", example = "")
	@Column
	private int dcGin8Yn;


	@ApiModelProperty(value="기중반전_ETC ", example = "")
	@Column
	private String dcGinEtc;


	@ApiModelProperty(value="기중반전_ETC ", example = "")
	@Column
	private int dcGinEtcYn;


	@ApiModelProperty(value="수평결합_2_YN ", example = "")
	@Column
	private int h2Yn;


	@ApiModelProperty(value="수평결합_4_YN ", example = "")
	@Column
	private int h4Yn;


	@ApiModelProperty(value="수평결합_6_YN ", example = "")
	@Column
	private int h6Yn;


	@ApiModelProperty(value="수평결합_8_YN ", example = "")
	@Column
	private int h8Yn;


	@ApiModelProperty(value="수평결합_ETC ", example = "")
	@Column
	private String hEtc;


	@ApiModelProperty(value="수평결합_ETC ", example = "")
	@Column
	private int hEtcYn;


	@ApiModelProperty(value="수직결합_2_YN ", example = "")
	@Column
	private int v2Yn;


	@ApiModelProperty(value="수직결합_4_YN ", example = "")
	@Column
	private int v4Yn;


	@ApiModelProperty(value="수직결합_6_YN ", example = "")
	@Column
	private int v6Yn;


	@ApiModelProperty(value="수직결합_8_YN ", example = "")
	@Column
	private int v8Yn;


	@ApiModelProperty(value="수직결합_ETC ", example = "")
	@Column
	private String vEtc;


	@ApiModelProperty(value="수직결합_ETC ", example = "")
	@Column
	private int vEtcYn;


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


	@ApiModelProperty(value="간접_인가부위_수평_YN ", example = "")
	@Column
	private int indrcHYn;


	@ApiModelProperty(value="간접_수평결합면_결과_CODE ", example = "")
	@Column
	private String indrcHResultCode;


	@ApiModelProperty(value="간접_수평결합면_MEMO ", example = "")
	@Column
	private String indrcHMemo;


	@ApiModelProperty(value="간접_인가부위_수직_YN ", example = "")
	@Column
	private int indrcVYn;


	@ApiModelProperty(value="간접_수직결합면_결과_CODE ", example = "")
	@Column
	private String indrcVResultCode;


	@ApiModelProperty(value="간접_수직결합면_MEMO ", example = "")
	@Column
	private String indrcVMemo;


	@ApiModelProperty(value="시험자의견A_YN ", example = "")
	@Column
	private int cmntAYn;


	@ApiModelProperty(value="시험자의견_ETC ", example = "")
	@Column
	private String cmntEtc;


	@ApiModelProperty(value="시험자의견_ETC ", example = "")
	@Column
	private int cmntEtcYn;

	
    @ApiModelProperty(value="정전기 방전 인가부위 ", example = "")
    @Column
    private String imgUrl;

    
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
