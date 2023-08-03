package egovframework.raw.service;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class MethodEsdSub {
	
	@ApiModelProperty(value="ESD_SUB_SEQ ", example = "", hidden=true)
	@Column
	private int esdSubSeq;


	@ApiModelProperty(value="ESD_SEQ ", example = "", hidden=true)
	@Column
	private int esdSeq;


	@ApiModelProperty(value="직접인가 인가부위", example = "")
	@Column
	private String drctl;


	@ApiModelProperty(value="방전방법_CODE(RD)", example = "2")
	@Column
	private String dcCode;


	@ApiModelProperty(value="RESULT_CODE ", example = "B")
	@Column
	private String resultCode;


	@ApiModelProperty(value="MEMO ", example = "")
	@Column
	private String memo;

	
	@ApiModelProperty(value="상태 ", example = "")
	@Column
	private String state;
}
