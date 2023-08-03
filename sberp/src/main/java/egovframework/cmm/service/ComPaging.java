package egovframework.cmm.service;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ComPaging {

	@ApiModelProperty(value="현재페이지 ", example = "1", required = true)
    private int pageIndex = 1;
    
	@ApiModelProperty(value="페이지갯수 ", example = "10", hidden = true)
    private int pageUnit = 10;

	@ApiModelProperty(value="페이지사이즈 ", example = "10", hidden = true)
    private int pageSize = 10;

	@ApiModelProperty(value="페이징 SQL의 조건절에 사용되는 시작 rownum", example = "1", hidden = true)
    private int firstIndex = 1;

	@ApiModelProperty(value="페이징 SQL의 조건절에 사용되는 마지막 rownum", example = "1", hidden = true)
    private int lastIndex = 1;
    
	@ApiModelProperty(value="한 페이지당 게시되는 게시물 건 수 ", example = "10", hidden = true)
    private int recordCountPerPage = 10;
    
	@ApiModelProperty(value="총 게시물 갯수 ", example = "24", hidden = true)
    private int totalCount = 10;
    
}
