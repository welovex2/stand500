package egovframework.sls.service;

import javax.persistence.Column;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

public class PayDTO {
	
	@Getter @Setter
	@ApiModel(value="PayDTO.Req", description = "납부상태 변경")
	public static class Req extends PayStatus {
		@ApiModelProperty(value="매출번호", example = "M2303-0002")
		@Column
		private String slsId;
	}
	
	@Getter @Setter
	@ApiModel(value="PayDTO.Res", description = "납부상태 조회")
	public static class Res extends PayStatus {
		
		@ApiModelProperty(value="매출번호", example = "M2303-0002")
		@Column
		private String slsId;
		
		@ApiModelProperty(value="등록 날짜", example = "2023-03-15")
		@Column
		private String insDtStr;

		@ApiModelProperty(value="작성자", example = "고객지원부 김정미")
		@Column
		private String insName;

		@ApiModelProperty(value="상태", example = "납부완료(계좌이체)")
		@Column
		private String stateType;		
	}
}
