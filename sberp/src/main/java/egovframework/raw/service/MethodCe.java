package egovframework.raw.service;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Range;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel(description = "로데이터 > CE")
@Getter @Setter @ToString
public class MethodCe {
	@ApiModelProperty(value="CE_SEQ ", example = "", hidden = true)
	@Column
	private int ceSeq;


	@ApiModelProperty(value="로데이터 SEQ ", example = "")
	@Column
	private int rawSeq;


	@ApiModelProperty(value="시험전압_AC220  ", example = "")
	@Column
	private int vltAcYn;


	@ApiModelProperty(value="시험전압_기타 ", example = "")
	@Column
	private String vltEtc;

	
	@ApiModelProperty(value="시험전압_기타 ", example = "")
	@Column
	private int vltEtcYn;
	
	
	@ApiModelProperty(value="가변유무 무 ", example = "")
	@Column
	private int varNYn;


	@ApiModelProperty(value="가변유무 유 ", example = "")
	@Column
	private int varYYn;


	@ApiModelProperty(value="시험장소1 ", example = "")
	@Column
	private int room1Yn;


	@ApiModelProperty(value="시험장소2 ", example = "")
	@Column
	private int room2Yn;


	@ApiModelProperty(value="시험장소3 ", example = "")
	@Column
	private int room3Yn;


	@ApiModelProperty(value="환경_온도 ", example = "")
	@Column
	private String temp;

	@ApiModelProperty(value="환경_온도 ", example = "")
	@Column
	private String tempPlus;
	
	@ApiModelProperty(value="환경_습도 ", example = "")
	@Column
	private String hmdt;

	@ApiModelProperty(value="환경_습도 ", example = "")
	@Column
	private String hmdtPlus;
	
	@ApiModelProperty(value="측정년 ", example = "2023")
	@Column
	@Min(value=0, message = "측정년은 4 자리의 숫자만 입력 가능합니다.")
	@Max(value=9999, message = "측정년은 4 자리의 숫자만 입력 가능합니다.")
	private int msrmnYear;


	@ApiModelProperty(value="측정월 ", example = "01")
	@Column
	@Min(value=0, message = "측정분은 2 자리의 숫자만 입력 가능합니다.")
	@Max(value=12, message = "측정월은 2 자리의 숫자만 입력 가능합니다.")
	private int msrmnMon;


	@ApiModelProperty(value="측정일 ", example = "01")
	@Column
	@Min(value=0, message = "측정분은 2 자리의 숫자만 입력 가능합니다.")
	@Max(value=31, message = "측정일은 2 자리의 숫자만 입력 가능합니다.")
	private int msrmnDay;


	@ApiModelProperty(value="측정시 ", example = "01")
	@Column
	@Min(value=0, message = "측정분은 2 자리의 숫자만 입력 가능합니다.")
	@Max(value=24, message = "측정시는 2 자리의 숫자만 입력 가능합니다.")
	private int msrmnHour;


	@ApiModelProperty(value="측정분 ", example = "01")
	@Column
	@Min(value=0, message = "측정분은 2 자리의 숫자만 입력 가능합니다.")
	@Max(value=60, message = "측정분은 2 자리의 숫자만 입력 가능합니다.")
	private int msrmnMin;


	@ApiModelProperty(value="서명 ", example = "")
	@Column
	private String sign;


	@ApiModelProperty(value="서명_URL ", example = "")
	@Column
	private String signUrl;


	@ApiModelProperty(value="시험결과여부 ", example = "")
	@Column
	private String resultCode;


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
