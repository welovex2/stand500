package egovframework.cmm.service;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel(description = "검색파라메터")
@Getter
@Setter
@ToString
public class ComParam extends ComPaging {

  @ApiModelProperty(value = "검색시작날짜", example = "2023-10-17")
  private String startDate;

  @ApiModelProperty(value = "검색종료날짜", example = "2023-10-19")
  private String endDate;

  @ApiModelProperty(value = "검색종류(내부처리)", example = "00", hidden = true)
  private String searchCode;

  @ApiModelProperty(value = "검색어", example = "", hidden = true)
  private String searchWord;

  @ApiModelProperty(value = "정렬컬럼", example = "공통코드 CO")
  private String sort = "newDesc";
  
  @ApiModelProperty(value = "로그인사용자", example = "")
  private String memId = "";
  
  @ApiModelProperty(value = "보안문서 접근여부", example = "")
  private int secretYn;
  
  @ApiModelProperty(value = "검색 ", example = "")
  private List<SearchVO> searchVO;

}

