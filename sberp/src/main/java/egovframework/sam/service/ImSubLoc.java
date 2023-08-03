package egovframework.sam.service;

import java.time.LocalDateTime;

import javax.persistence.Column;

import org.springmodules.validation.bean.conf.loader.annotation.handler.NotNull;

import io.swagger.annotations.ApiModelProperty;

public class ImSubLoc {
	@ApiModelProperty(value="IM_SUB_LOC_SEQ ", example = "", required = true)
	@Column
	@NotNull
	private int imSubLocSeq;


	@ApiModelProperty(value="시료번호 ", example = "", required = true)
	@Column
	@NotNull
	private int imSubSeq;


	@ApiModelProperty(value="창고코드 ", example = "", required = true)
	@Column
	@NotNull
	private int storeSeq;


	@ApiModelProperty(value="창고인/창고아웃 ", example = "", required = true)
	@Column
	private String storeState;


	@ApiModelProperty(value="기타사유 ", example = "", required = true)
	@Column
	private String memo;


	@ApiModelProperty(value="등록자 아이디 ", example = "", required = true)
	@Column
	private String insMemId;


	@ApiModelProperty(value="등록 날짜시간 ", example = "", required = true)
	@Column
	private LocalDateTime insDt;


	@ApiModelProperty(value="수정자 아이디 ", example = "", required = true)
	@Column
	private String udtMemId;


	@ApiModelProperty(value="수정 날짜시간 ", example = "", required = true)
	@Column
	private LocalDateTime udtDt;


	@ApiModelProperty(value="상태(I,U,D) ", example = "", required = true)
	@Column
	private String state;


	public int getImSubLocSeq() {
		return imSubLocSeq;
	}


	public void setImSubLocSeq(int imSubLocSeq) {
		this.imSubLocSeq = imSubLocSeq;
	}


	public int getImSubSeq() {
		return imSubSeq;
	}


	public void setImSubSeq(int imSubSeq) {
		this.imSubSeq = imSubSeq;
	}


	public int getStoreSeq() {
		return storeSeq;
	}


	public void setStoreSeq(int storeSeq) {
		this.storeSeq = storeSeq;
	}


	public String getStoreState() {
		return storeState;
	}


	public void setStoreState(String storeState) {
		this.storeState = storeState;
	}


	public String getMemo() {
		return memo;
	}


	public void setMemo(String memo) {
		this.memo = memo;
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
