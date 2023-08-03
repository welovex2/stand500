package egovframework.cmm.service;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@ApiModel(value="PagingVO", description = "페이징정보")
public class PagingVO {

	@ApiModelProperty(value="전체 게시글 수 (자동계산)", example = "50", required = true)
	private int totalCount; 
	
	@ApiModelProperty(value="전체 페이지 수 (자동계산)", example = "4", required = true)
	private int totalPage;
	
	@ApiModelProperty(value="현재 페이지 (get)", example = "1", required = true)
	private int currentPageNo;
	
	@ApiModelProperty(value="한 페이지에 몇 개의 페이지 (고정)", example = "10", required = true)
	private int displayPage;
	
	@ApiModelProperty(value="한 페이지에 몇 개의 게시글 (고정)", example = "15", required = true)
	private int displayRow;
	
//	private int beginPage; // 페이징 시작 페이지 수
//	private int endPage; // 페이징 종료 페이지 수
	
	@ApiModelProperty(value="페이징 SQL의 조건절에 사용되는 시작 rownum.", example = "0", required = true)
	private int firstRecordIndex;
	
	public int getFirstRecordIndex() {
		firstRecordIndex = (getCurrentPageNo() - 1) * getDisplayRow();
		return firstRecordIndex;
	}

}
