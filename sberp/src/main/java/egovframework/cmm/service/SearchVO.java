package egovframework.cmm.service;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class SearchVO {
	
	@ApiModelProperty(value="검색종류(공통코드:CS)", example = "10")
	private String searchCode;
	
	@ApiModelProperty(value="검색어", example = "")
	private String searchWord;
	
	@ApiModelProperty(value="검색시작날짜(검색종류가 날짜일 경우)", example = "2023-02-02")
	private String startDate;
	
	@ApiModelProperty(value="검색종료날짜(검색종류가 날짜일 경우)", example = "2023-05-30")
	private String endDate;
}
