package egovframework.raw.service;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel(description = "Technical Requirements (기술적 요구항목)")
@Getter @Setter @ToString
public class RawTchn {
	
	@ApiModelProperty(value="TCHN_SEQ ", example = "")
	@Column
	@NotNull
	private int tchnSeq;


	@ApiModelProperty(value="RAW_SEQ ", example = "")
	@Column
	@NotNull
	private String rawSeq;


	@ApiModelProperty(value="EMI / EMS ", example = "EMI")
	@Column
	private String type;


	@ApiModelProperty(value="CHECK_YN ", example = "1")
	@Column
	private int checkYn;


	@ApiModelProperty(value="TEXT ", example = "제15조 멀티미디어기기류의 전자파적합성 기준")
	@Column
	private String text;

	
	@ApiModelProperty(value="상태", notes="I:신규등록, U:수정, D:삭제한 항목", example = "")
	@Column
	private String state;
}
