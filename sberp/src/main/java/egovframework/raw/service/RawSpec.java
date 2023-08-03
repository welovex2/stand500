package egovframework.raw.service;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class RawSpec {
	
	@ApiModelProperty(value="SPEC_SEQ ", example = "")
	@Column
	@NotNull
	private int specSeq;


	@ApiModelProperty(value="RAW_SEQ ", example = "")
	@Column
	@NotNull
	private int rawSeq;


	@ApiModelProperty(value="구분 ", example = "")
	@Column
	private String item;


	@ApiModelProperty(value="주요사항 및 특성 ", example = "")
	@Column
	private String memo;


	@ApiModelProperty(value="상태(I,U,D) ", example = "", hidden = true)
	@Column
	private String state;


}
