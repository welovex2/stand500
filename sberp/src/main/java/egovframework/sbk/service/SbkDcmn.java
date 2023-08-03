package egovframework.sbk.service;

import javax.persistence.Column;

import org.springmodules.validation.bean.conf.loader.annotation.handler.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "신청서 제출서류")
public class SbkDcmn {
	
	@ApiModelProperty(value="SBK_DCMN_SBMT_SEQ ", example = "", required = true)
	@Column
	@NotNull
	private int sbkDcmnSbmtSeq;


	@ApiModelProperty(value="SBK_SEQ ", example = "", required = true)
	@Column
	@NotNull
	private int sbkSeq;


	@ApiModelProperty(value="인증신청서 ", example = "", required = true)
	@Column
	private int aplct;


	@ApiModelProperty(value="사용설명서 ", example = "", required = true)
	@Column
	private int manual;


	@ApiModelProperty(value="회로도 ", example = "", required = true)
	@Column
	private int diagram;


	@ApiModelProperty(value="부품배치도 ", example = "", required = true)
	@Column
	private int layout;


	@ApiModelProperty(value="패턴도 ", example = "", required = true)
	@Column
	private int pattern;


	@ApiModelProperty(value="부품리스트 ", example = "", required = true)
	@Column
	private int partsList;


	@ApiModelProperty(value="주요부품 ", example = "", required = true)
	@Column
	private int mainParts;


	@ApiModelProperty(value="주요부품승인서 ", example = "", required = true)
	@Column
	private int mainPartsAprvl;


	@ApiModelProperty(value="CBReport ", example = "", required = true)
	@Column
	private int cbReport;


	@ApiModelProperty(value="승인기관인증서 ", example = "", required = true)
	@Column
	private int aprvlCrtfc;


	@ApiModelProperty(value="승인기관성적서 ", example = "", required = true)
	@Column
	private int aprvlReport;


	@ApiModelProperty(value="신청인보유 ", example = "", required = true)
	@Column
	private int retention;


	public int getSbkDcmnSbmtSeq() {
		return sbkDcmnSbmtSeq;
	}


	public void setSbkDcmnSbmtSeq(int sbkDcmnSbmtSeq) {
		this.sbkDcmnSbmtSeq = sbkDcmnSbmtSeq;
	}


	public int getSbkSeq() {
		return sbkSeq;
	}


	public void setSbkSeq(int sbkSeq) {
		this.sbkSeq = sbkSeq;
	}


	public int getAplct() {
		return aplct;
	}


	public void setAplct(int aplct) {
		this.aplct = aplct;
	}


	public int getManual() {
		return manual;
	}


	public void setManual(int manual) {
		this.manual = manual;
	}


	public int getDiagram() {
		return diagram;
	}


	public void setDiagram(int diagram) {
		this.diagram = diagram;
	}


	public int getLayout() {
		return layout;
	}


	public void setLayout(int layout) {
		this.layout = layout;
	}


	public int getPattern() {
		return pattern;
	}


	public void setPattern(int pattern) {
		this.pattern = pattern;
	}


	public int getPartsList() {
		return partsList;
	}


	public void setPartsList(int partsList) {
		this.partsList = partsList;
	}


	public int getMainParts() {
		return mainParts;
	}


	public void setMainParts(int mainParts) {
		this.mainParts = mainParts;
	}


	public int getMainPartsAprvl() {
		return mainPartsAprvl;
	}


	public void setMainPartsAprvl(int mainPartsAprvl) {
		this.mainPartsAprvl = mainPartsAprvl;
	}


	public int getCbReport() {
		return cbReport;
	}


	public void setCbReport(int cbReport) {
		this.cbReport = cbReport;
	}


	public int getAprvlCrtfc() {
		return aprvlCrtfc;
	}


	public void setAprvlCrtfc(int aprvlCrtfc) {
		this.aprvlCrtfc = aprvlCrtfc;
	}


	public int getAprvlReport() {
		return aprvlReport;
	}


	public void setAprvlReport(int aprvlReport) {
		this.aprvlReport = aprvlReport;
	}


	public int getRetention() {
		return retention;
	}


	public void setRetention(int retention) {
		this.retention = retention;
	}


}
