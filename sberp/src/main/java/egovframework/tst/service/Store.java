package egovframework.tst.service;

import java.time.LocalDateTime;

import javax.persistence.Column;

import org.springmodules.validation.bean.conf.loader.annotation.handler.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "창고")
public class Store {
	@ApiModelProperty(value="'0001' 4자리 고정 ", example = "", required = true)
	@Column
	@NotNull
	private int storeSeq;


	@ApiModelProperty(value="창고명 ", example = "", required = true)
	@Column
	private String storeName;


	@ApiModelProperty(value="구역 ", example = "", required = true)
	@Column
	private String zone;


	@ApiModelProperty(value="층수(공통코드:SF) ", example = "", required = true)
	@Column
	private String floorCode;


	@ApiModelProperty(value="열(공통코드:SC) ", example = "", required = true)
	@Column
	private String columnCode;


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


	public int getStoreSeq() {
		return storeSeq;
	}


	public void setStoreSeq(int storeSeq) {
		this.storeSeq = storeSeq;
	}


	public String getStoreName() {
		return storeName;
	}


	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}


	public String getZone() {
		return zone;
	}


	public void setZone(String zone) {
		this.zone = zone;
	}


	public String getFloorCode() {
		return floorCode;
	}


	public void setFloorCode(String floorCode) {
		this.floorCode = floorCode;
	}


	public String getColumnCode() {
		return columnCode;
	}


	public void setColumnCode(String columnCode) {
		this.columnCode = columnCode;
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
