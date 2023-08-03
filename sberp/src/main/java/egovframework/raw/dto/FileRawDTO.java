package egovframework.raw.dto;

import java.util.List;

import javax.persistence.Column;

import egovframework.cmm.service.FileVO;
import egovframework.raw.service.FileRaw;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class FileRawDTO extends FileRaw {

	@ApiModelProperty(value="게시글번호", example = "1")
	@Column
	private int no;
	
	@ApiModelProperty(value="작성일자", example = "2023-03-03")
	@Column
	private String insDtStr;
	
	@ApiModelProperty(value="작성자", example = "김정미")
	@Column
	private String memName;
	
	List<FileVO> fileList;
}
