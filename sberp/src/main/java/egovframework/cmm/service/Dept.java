package egovframework.cmm.service;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class Dept {
	
	@ApiModelProperty(value="DEPT_SEQ ", example = "")
	@Column
	@NotNull
	private int deptSeq;


	@ApiModelProperty(value="부서명 ", example = "")
	@Column
	private String name;


	@ApiModelProperty(value="MEMO ", example = "")
	@Column
	private String memo;


	@ApiModelProperty(value="시험부서 여부 ", example = "")
	@Column
	private int testYn;


	@ApiModelProperty(value="ORDER ", example = "")
	@Column
	private int oder;


	@ApiModelProperty(value="등록자 아이디 ", example = "", hidden=true)
	@Column
	private String insMemId;


	@ApiModelProperty(value="등록 날짜시간 ", example = "", hidden=true)
	@Column
	private LocalDateTime insDt;


	@ApiModelProperty(value="수정자 아이디 ", example = "", hidden=true)
	@Column
	private String udtMemId;


	@ApiModelProperty(value="수정 날짜시간 ", example = "", hidden=true)
	@Column
	private LocalDateTime udtDt;


	@ApiModelProperty(value="상태(I,U,D) ", example = "", hidden=true)
	@Column
	private String state;


}
