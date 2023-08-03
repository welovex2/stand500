package egovframework.quo.service;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.validation.constraints.Pattern;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class QuoMod {

	@ApiModelProperty(value="QUO_MOD_SEQ ", example = "", hidden = true)
	@Column
	private int quoModSeq;


	@ApiModelProperty(value="QUO_SEQ ", example = "", hidden = true)
	@Column
	private int quoSeq;


	@ApiModelProperty(value="QUO_YM ", example = "", hidden = true)
	@Column
	private String quoYm;


	@ApiModelProperty(value="MEMO ", example = "", required = true)
	@Column
	private String memo;


	@ApiModelProperty(value="공통코드(MM)", notes="1 매출확정, 2 수정요청, 3 수정허가 ,4 수정완료, 5 세금계산서 신청 ", example = "", required = true)
	@Column
	@Pattern(regexp = "[2345]", message = "2 수정요청, 3 수정허가 ,4 수정완료, 5 세금계산서 신청 상태만 입력 가능합니다.") 
	private String stateCode;


	@ApiModelProperty(value="허가_ID ", example = "", required = true)
	@Column
	private String prmsId;


	@ApiModelProperty(value="허가일 ", example = "", required = true)
	@Column
	private String prmsDt;


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
