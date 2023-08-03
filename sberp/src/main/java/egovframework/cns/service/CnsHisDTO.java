package egovframework.cns.service;

import javax.persistence.Column;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "상담서내역 리스트")
public class CnsHisDTO extends CnsHis {
	
	@ApiModelProperty(value="작성일자", example = "2023-02-23 08:00:12")
	@Column
	private String insDtStr;

	@ApiModelProperty(value="작성자", example = "고객지원부_김정미")
	@Column
	private String insMem;
	
	public String getInsDtStr() {
		return insDtStr;
	}

	public void setInsDtStr(String insDtStr) {
		this.insDtStr = insDtStr;
	}

	public String getInsMem() {
		return insMem;
	}

	public void setInsMem(String insMem) {
		this.insMem = insMem;
	}
	
}
