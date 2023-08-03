package egovframework.cns.service;

import java.time.LocalDateTime;

import javax.persistence.Column;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Cns {

	@ApiModelProperty(value="CNS_SEQ(등록시 값 넣지 않음, 메모만 추가시 조회한 상담서 고유번호)", example = "0", required = true)
	@Column
	private int cnsSeq;

	@ApiModelProperty(value="문의종류 ", example = "국가인증", required = true)
	@Column
	private String crtfcType;

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
