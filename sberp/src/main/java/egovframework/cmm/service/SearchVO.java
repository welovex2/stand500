package egovframework.cmm.service;

import java.util.List;
import egovframework.cmm.util.EgovStringUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.ToString;

@ToString
public class SearchVO {
	
	@ApiModelProperty(value="검색종류(공통코드:CS)", example = "10")
	private String searchCode;
	
	@ApiModelProperty(value="검색어", example = "")
	private String searchWord;
	
	@ApiModelProperty(value="검색어", example = "")
	private List<String> searchWords;
	   
	@ApiModelProperty(value="검색시작날짜(검색종류가 날짜일 경우)", example = "2023-02-02")
	private String startDate;
	
	@ApiModelProperty(value="검색종료날짜(검색종류가 날짜일 경우)", example = "2023-05-30")
	private String endDate;

	
  public String getSearchCode() {
    return searchCode;
  }

  public void setSearchCode(String searchCode) {
    this.searchCode = searchCode;
  }

  public String getSearchWord() {
    return searchWord;
  }

  public void setSearchWord(String searchWord) {
    this.searchWord = EgovStringUtil.getHtmlStrCnvr(searchWord);
  }

  public List<String> getSearchWords() {
    return searchWords;
  }

  public void setSearchWords(List<String> searchWords) {
    this.searchWords = searchWords;
  }

  public String getStartDate() {
    return startDate;
  }

  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }
	
	
}
