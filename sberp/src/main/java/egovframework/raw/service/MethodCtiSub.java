package egovframework.raw.service;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class MethodCtiSub {
	
	@ApiModelProperty(value="ctiSubSeq", example = "")
	@Column
	@NotNull
	private int ctiSubSeq;


	@ApiModelProperty(value="CTI_SEQ ", example = "", hidden=true)
	@Column
	@NotNull
	private int ctiSeq;


	@ApiModelProperty(value="12 또는 24 ", example = "")
	@Column
	private String dcType;


	@ApiModelProperty(value="시험 펄스 ", example = "")
	@Column
	private String pulse;


	@ApiModelProperty(value="기준 ", example = "")
	@Column
	private String standard;


	@ApiModelProperty(value="시험결과(A,B,C) ", example = "A")
	@Column
	private String resultCode;


	@ApiModelProperty(value="시험자 의견 ", example = "")
	@Column
	private String testMemo;


}
