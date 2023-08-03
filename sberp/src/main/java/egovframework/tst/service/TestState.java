package egovframework.tst.service;

import java.time.LocalDateTime;

import javax.persistence.Column;

import org.springmodules.validation.bean.conf.loader.annotation.handler.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "시험상태")
public class TestState {
	@ApiModelProperty(value="TEST_STATE_SEQ ", example = "", required = true)
	@Column
	@NotNull
	private int testStateSeq;


	@ApiModelProperty(value="TEST_SEQ ", example = "", required = true)
	@Column
	@NotNull
	private int testSeq;


	@ApiModelProperty(value="상태코드(공통코드:TS) 0001 접수 0002 시험중 0003 디버깅 0004 홀딩 0005 RD완료 0006 성적서완료 0007 필증완료  ", example = "", required = true)
	@Column
	private String stateCode;


	@ApiModelProperty(value="사유 ", example = "", required = true)
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


	public int getTestStateSeq() {
		return testStateSeq;
	}


	public void setTestStateSeq(int testStateSeq) {
		this.testStateSeq = testStateSeq;
	}


	public int getTestSeq() {
		return testSeq;
	}


	public void setTestSeq(int testSeq) {
		this.testSeq = testSeq;
	}


	public String getStateCode() {
		return stateCode;
	}


	public void setStateCode(String stateCode) {
		this.stateCode = stateCode;
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
