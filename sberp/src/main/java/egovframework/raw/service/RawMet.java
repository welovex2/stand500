package egovframework.raw.service;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel(description = "4-2. method (시험방법)")
@Getter @Setter @ToString
public class RawMet {
	@ApiModelProperty(value="MET_SEQ ", example = "", required = true)
	@Column
	@NotNull
	private int metSeq;


	@ApiModelProperty(value="RAW_SEQ ", example = "")
	@Column
	@NotNull
	private String rawSeq;


	@ApiModelProperty(value="적용여부 ", example = "")
	@Column
	private int checkYn;


	@ApiModelProperty(value="시험규격 ", example = "")
	@Column
	private String type;


	@ApiModelProperty(value="비고(특이사항 기재) ")
	@Column
	private String memo;

	
	@ApiModelProperty(value="성적서 > 3.2 시험항목 > NOTE")
	@Column
	private String reportMemo;

	
	@ApiModelProperty(value="성적서 > 3.2 시험항목 > 비고")
	@Column
	private String note;
}
