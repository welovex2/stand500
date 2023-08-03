package egovframework.raw.service;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel(description = "로데이터 > CTI")
@Getter @Setter @ToString
public class MethodCti {
	
	@ApiModelProperty(value="CTI_SEQ ", example = "")
	@Column
	@NotNull
	private int ctiSeq;


	@ApiModelProperty(value="RAW_SEQ ", example = "")
	@Column
	@NotNull
	private int rawSeq;


	@ApiModelProperty(value="시험전압_DC12_YN ", example = "")
	@Column
	private int vltDc12Yn;


	@ApiModelProperty(value="시험전압_DC24_YN ", example = "")
	@Column
	private int vltDc24Yn;


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
