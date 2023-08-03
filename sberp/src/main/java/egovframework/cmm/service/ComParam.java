package egovframework.cmm.service;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel(description = "검색파라메터")
@Getter @Setter @ToString
public class ComParam extends ComPaging {
    
	@ApiModelProperty(value="검색시작날짜-삭제예정", example = "2023-02-02", hidden = true)
	private String startDate;
	
	@ApiModelProperty(value="검색종료날짜-삭제예정", example = "2023-05-30", hidden = true)
	private String endDate;

	@ApiModelProperty(value="검색종류(공통코드:CS)-삭제예정", example = "00", hidden = true)
	private String searchCode;
	
	@ApiModelProperty(value="검색어-삭제예정", example = "", hidden = true)
	private String searchWord;
	
	@ApiModelProperty(value="검색 ", example = "")
	private List<SearchVO> searchVO;

}

