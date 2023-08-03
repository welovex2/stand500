package egovframework.cns.service;

import java.util.List;

import javax.persistence.Column;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public class CnsDTO {
	
	@Getter @Setter @ToString
	@ApiModel(value="CnsDTO.Req", description = "상담서 등록")
    public static class Req extends Cns {
		
		@ApiModelProperty(value="회사 고유번호 (회사타입이 미정일 경우 0)", example = "2")
		@Column
		private int cmpySeq;
		
		@ApiModelProperty(value="담당자 고유번호", example = "3")
		@Column
		private int cmpyMngSeq;
		
		@ApiModelProperty(value="회사명 (회사타입이 미정일 경우만 입력)", example = "크뱅드다탠스")
		@Column
		private String cmpyName;
		
		@ApiModelProperty(value="회사 연락처 (회사타입이 미정일 경우만 입력)", example = "02-1112-5458")
		@Column
//		@Pattern(regexp = "^[0-9-]*$", message = "회사 연락처는 9 ~ 11 자리의 숫자만 입력 가능합니다.") 
//		@Size(min=0, max=13, message = "회사 연락처는 9 ~ 11 자리의 숫자만 입력 가능합니다.")
		private String cmpyPhone;
		
		@ApiModelProperty(value="담당자 (회사타입이 미정일 경우만 입력)", example = "김철중")
		@Column
		private String mngName;
		
		@ApiModelProperty(value="담당자 연락처 (회사타입이 미정일 경우만 입력)", example = "010-5455-1212")
		@Column
//		@Pattern(regexp = "^[0-9-]*$", message = "담당자 연락처는 9 ~ 11 자리의 숫자만 입력 가능합니다.") 
//		@Size(min=0, max=13, message = "담당자 연락처는 9 ~ 11 자리의 숫자만 입력 가능합니다.")
		private String mngPhone;
		
		@ApiModelProperty(value="제품명", example = "제품1")
		@Column
		private String prdctName;
		
		@ApiModelProperty(value="상담내역", example = "상담좀 합시다")
		@Column
		private String memo;
		
		@ApiModelProperty(value="견적서번호", example = "Q2303-G0018" )
		@Column
		private String quoId;
		
		@ApiModelProperty(value="신청서번호", example = "SB23-G0010" )
		@Column
		private String sbkId;
		
    }
    
	@Getter @Setter
	@ApiModel(value="CnsDTO.Res", description = "상담서 조회")
    public static class Res extends Cns {

		@ApiModelProperty(value="게시글번호", example = "1")
		@Column
		private int no;
		
		@ApiModelProperty(value="작성일자", example = "2023-02-23 08:00:12")
		@Column
		private String insDtStr;
		
		@ApiModelProperty(value="고객유형", example = "컨설팅" )
		@Column
		private String cmpyType;

		@ApiModelProperty(value="고객유형 코드", example = "회사타입 0000 협력사(컨설팅) 1000 직접고객" )
		@Column
		private String cmpyCode;
		
		@ApiModelProperty(value="작성자", example = "김정미")
		@Column
		private String insName;

		@ApiModelProperty(value="회사 고유번호", example = "1")
		@Column
		private int cmpySeq;
		
		@ApiModelProperty(value="회사명", example = "라인시스템")
		@Column
		private String cmpyName;
		
		@ApiModelProperty(value="회사연락처", example = "02-1111-2123")
		@Column
		private String cmpyPhone;

		@ApiModelProperty(value="담당자명", example = "이광근")
		@Column
		private String mngName;
		
		@ApiModelProperty(value="연락처", example = "010-8444-2222")
		@Column
		private String mngPhone;
		
		@ApiModelProperty(value="담당자 고유번호", example = "1")
		@Column
		private int cmpyMngSeq;
		
		@ApiModelProperty(value="제품명", example = "제품1")
		@Column
		private String prdctName;
		
		@ApiModelProperty(value="견적서번호", example = "Q2303-G0018" )
		@Column
		private String quoId;
		
		@ApiModelProperty(value="신청서번호", example = "SB23-G0010" )
		@Column
		private String sbkId;
		
		@ApiModelProperty(value="마지막상담내용", example = "정말궁금해요 내가")
		@Column
		private String lastMemo;

		@ApiModelProperty(value="상담리스트", example = "[]")
		@Column
		private List<CnsHisDTO> memoList;
		
    }

}
