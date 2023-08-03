package egovframework.cmm.service;

import java.time.LocalDateTime;

import javax.persistence.Column;

import org.springmodules.validation.bean.conf.loader.annotation.handler.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@ApiModel(description = "업무공통")
public class Job {
	
	@ApiModelProperty(value="JOB_SEQ ", example = "", required = true)
	@Column
	private int jobSeq;

	@ApiModelProperty(value="CMPY_SEQ ", example = "", required = true)
	@Column
	private int cmpySeq;

	@ApiModelProperty(value="CMPY_MNG_SEQ ", example = "", required = true)
	@Column
	private int cmpyMngSeq;

	@ApiModelProperty(value="CNS_SEQ ", example = "", required = true)
	@Column
	private int cnsSeq;

	@ApiModelProperty(value="SBK_YM ", example = "", required = true)
	@Column
	private String sbkYm;

	@ApiModelProperty(value="SBK_SEQ ", example = "", required = true)
	@Column
	private int sbkSeq;

	@ApiModelProperty(value="QUO_YM ", example = "", required = true)
	@Column
	private String quoYm;

	@ApiModelProperty(value="QUO_SEQ ", example = "", required = true)
	@Column
	private int quoSeq;

	@ApiModelProperty(value="고객유형 0001 컨설팅 0002 직고객 0003 없음 ", example = "", required = true)
	@Column
	private String cstmCode;

	@ApiModelProperty(value="업체명 ", example = "", required = true)
	@Column
	private String cmpyName;

	@ApiModelProperty(value="회사전화번호 ", example = "", required = true)
	@Column
	private String cmpyPhone;
	
	@ApiModelProperty(value="담당자 이름 ", example = "", required = true)
	@Column
	private String mngName;

	@ApiModelProperty(value="담당자전화번호 ", example = "", required = true)
	@Column
	private String mngPhone;

	@ApiModelProperty(value="담당자 팩스 ", example = "", required = true)
	@Column
	private String mngFax;

	@ApiModelProperty(value="제품명 ", example = "", required = true)
	@Column
	private String prdctName;

	@ApiModelProperty(value="모델명 ", example = "", required = true)
	@Column
	private String modelName;

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
