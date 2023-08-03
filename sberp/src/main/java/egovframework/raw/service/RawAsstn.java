package egovframework.raw.service;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel(description = "Assistance Device and Cable(시험기기 전체구성)")
@Getter @Setter @ToString
public class RawAsstn {
	
	@ApiModelProperty(value="ASSTN_SEQ ", example = "")
	@Column
	@NotNull
	private int asstnSeq;


	@ApiModelProperty(value="RAW_SEQ ", example = "")
	@Column
	@NotNull
	private String rawSeq;


	@ApiModelProperty(value="기자재명칭  ", example = "")
	@Column
	private String eqpmn;


	@ApiModelProperty(value="모델명  ", example = "")
	@Column
	private String model;


	@ApiModelProperty(value="제조번호 ", example = "")
	@Column
	private String serialNo;


	@ApiModelProperty(value="제조자 ", example = "")
	@Column
	private String mnfctCmpny;


	@ApiModelProperty(value="비고 ", example = "")
	@Column
	private String memo;

	@ApiModelProperty(value="상태", notes="I:신규등록, U:수정, D:삭제한 항목", example = "")
	@Column
	private String state;
}
