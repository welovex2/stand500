package egovframework.raw.service;

import javax.persistence.Column;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class FileRaw {
	
	@ApiModelProperty(value="FILE_RAW_SEQ ", example = "")
	@Column
	private int fileRawSeq;


	@ApiModelProperty(value="TEST_SEQ ", example = "", required = true)
	@Column
	private int testSeq;


	@ApiModelProperty(value="FILE_TYPE ", example = "")
	@Column
	private String fileType;


	@ApiModelProperty(value="TITLE ", example = "")
	@Column
	private String title;


	@ApiModelProperty(value="ATCH_FILE_ID ", example = "")
	@Column
	private String atchFileId;


	@ApiModelProperty(value="INS_MEM_ID ", example = "", hidden = true)
	@Column
	private String insMemId;


	@ApiModelProperty(value="INS_DT ", example = "", hidden = true)
	@Column
	private String insDt;


	@ApiModelProperty(value="UDT_MEM_ID ", example = "", hidden = true)
	@Column
	private String udtMemId;


	@ApiModelProperty(value="UDT_DT ", example = "", hidden = true)
	@Column
	private String udtDt;


	@ApiModelProperty(value="STATE ", example = "", hidden = true)
	@Column
	private String state;


}
