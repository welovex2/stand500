package egovframework.cmm.service;

import javax.persistence.Column;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class HisDTO {
	
	@ApiModelProperty(value="SBK_ID ", example = "")
	@Column
	private String sbkId;

	
	@ApiModelProperty(value="테이블명 ", example = "")
	@Column
	private String tb;
	
	
	@ApiModelProperty(value="컬럼명 ", example = "")
	@Column
	private String col;


	@ApiModelProperty(value="BEFORE ", example = "")
	@Column
	private String before;


	@ApiModelProperty(value="AFTER ", example = "")
	@Column
	private String after;


	@ApiModelProperty(value="변경자 ", example = "")
	@Column
	private String memId;

	@ApiModelProperty(value="직위 ", example = "")
	@Column
	private String memPos;
	
	@ApiModelProperty(value="INS_DT ", example = "")
	@Column
	private String insDtStr;


}
