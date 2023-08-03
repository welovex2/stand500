package egovframework.cmm.service;

import java.time.LocalDateTime;

import javax.persistence.Column;

import org.springmodules.validation.bean.conf.loader.annotation.handler.NotNull;

import io.swagger.annotations.ApiModelProperty;

public class Comcode {

	@ApiModelProperty(value="COMCODE_SEQ ", example = "", required = true)
	@Column
	@NotNull
	private int comcodeSeq;


	@ApiModelProperty(value="코드 ", example = "", required = true)
	@Column
	private String typeCd;


	@ApiModelProperty(value="코드명 ", example = "", required = true)
	@Column
	private String typeName;


	@ApiModelProperty(value="상위코드 ", example = "", required = true)
	@Column
	private String topCd;


	@ApiModelProperty(value="사용여부 ", example = "", required = true)
	@Column
	private int uesYn;


	@ApiModelProperty(value="INS_MEM_ID ", example = "", required = true)
	@Column
	private String insMemId;


	@ApiModelProperty(value="INS_DT ", example = "", required = true)
	@Column
	private LocalDateTime insDt;


	@ApiModelProperty(value="UDT_MEM_ID ", example = "", required = true)
	@Column
	private String udtMemId;


	@ApiModelProperty(value="UDT_DT ", example = "", required = true)
	@Column
	private LocalDateTime udtDt;


	@ApiModelProperty(value="상태(I,U,D) ", example = "", required = true)
	@Column
	private String state;


	public int getComcodeSeq() {
		return comcodeSeq;
	}


	public void setComcodeSeq(int comcodeSeq) {
		this.comcodeSeq = comcodeSeq;
	}


	public String getTypeCd() {
		return typeCd;
	}


	public void setTypeCd(String typeCd) {
		this.typeCd = typeCd;
	}


	public String getTypeName() {
		return typeName;
	}


	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}


	public String getTopCd() {
		return topCd;
	}


	public void setTopCd(String topCd) {
		this.topCd = topCd;
	}


	public int getUesYn() {
		return uesYn;
	}


	public void setUesYn(int uesYn) {
		this.uesYn = uesYn;
	}


	public String getInsMemId() {
		return insMemId;
	}


	public void setInsMemId(String insMemId) {
		this.insMemId = insMemId;
	}


	public LocalDateTime getInsDt() {
		return insDt;
	}


	public void setInsDt(LocalDateTime insDt) {
		this.insDt = insDt;
	}


	public String getUdtMemId() {
		return udtMemId;
	}


	public void setUdtMemId(String udtMemId) {
		this.udtMemId = udtMemId;
	}


	public LocalDateTime getUdtDt() {
		return udtDt;
	}


	public void setUdtDt(LocalDateTime udtDt) {
		this.udtDt = udtDt;
	}


	public String getState() {
		return state;
	}


	public void setState(String state) {
		this.state = state;
	}


	
}
