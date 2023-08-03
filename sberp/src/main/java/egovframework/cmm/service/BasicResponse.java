package egovframework.cmm.service;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
@AllArgsConstructor
@ApiModel(value="BasicResponse", description = "공통 Response")
public class BasicResponse<T> {

	@ApiModelProperty(value="호출 결과", example = "true", required = true)
    private boolean result;
	
	@ApiModelProperty(value="호출 실패시 메세지", example = "로그인 정보가 없습니다.", required = true)
    private String message;
	
	@ApiModelProperty(value="리스트시 페이징 정보", example = "", required = true)
    private PagingVO paging;
	
	@ApiModelProperty(value="결과데이터", example = "", required = true)
    private T data;

}
