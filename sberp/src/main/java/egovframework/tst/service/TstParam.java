package egovframework.tst.service;

import javax.persistence.Column;

import io.swagger.annotations.ApiModelProperty;

public class TstParam {

	@ApiModelProperty(value="국가코드, 공통코드 : CN ", example = "KC", required = true)
	@Column
	private String countryCode;
	
	@ApiModelProperty(value="인증종류코드 ", example = "9", required = true)
	@Column
	private int crtfTypeSeq;

	@ApiModelProperty(value="시험종류(시험부)코드, 공통코드 : TT ", example = "E", required = true)
	@Column
	private String testTypeCode;


	public String getCountryCode() {
		return countryCode;
	}


	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}


	public int getCrtfTypeSeq() {
		return crtfTypeSeq;
	}


	public void setCrtfTypeSeq(int crtfTypeSeq) {
		this.crtfTypeSeq = crtfTypeSeq;
	}


	public String getTestTypeCode() {
		return testTypeCode;
	}


	public void setTestTypeCode(String testTypeCode) {
		this.testTypeCode = testTypeCode;
	}
	
	
}
