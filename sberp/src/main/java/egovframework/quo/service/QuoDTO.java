package egovframework.quo.service;

import java.util.List;
import javax.persistence.Column;
import egovframework.tst.dto.TestItemDTO;
import egovframework.tst.service.TestItem;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public class QuoDTO {

  @Getter
  @Setter
  @ToString(callSuper = true)
  @ApiModel(value = "QuoDTO.Req", description = "견적서 등록")
  public static class Req extends Quo {

    @ApiModelProperty(value = "프로젝트번호", example = "12")
    @Column
    private int jobSeq;
    
    @ApiModelProperty(value = "견적서번호", example = "Q2303-G0018")
    @Column
    private String quoId;

    @ApiModelProperty(value = "신청서번호", example = " ")
    @Column
    private String sbkId;

    @ApiModelProperty(value = "회사 고유번호", example = "1")
    @Column
    private int cmpySeq;

    @ApiModelProperty(value="담당자 고유번호", example = "3")
    @Column
    private int cmpyMngSeq;
    
    @ApiModelProperty(value = "담당자 이름 ", example = "김담당")
    @Column
    private String mngName;

    @ApiModelProperty(value = "전화번호 ", example = "02-354-9855")
    @Column
//    @Pattern(regexp = "^[0-9-]*$", message = "연락처는 10 ~ 11 자리의 숫자만 입력 가능합니다.")
//    @Size(min = 0, max = 13, message = "연락처는 10 ~ 11 자리의 숫자만 입력 가능합니다.")
    private String mngPhone;

    @ApiModelProperty(value = "담당자 이메일 ", example = "")
    @Column
//    @Pattern(regexp = "^[0-9-]*$", message = "팩스는 10 ~ 11 자리의 숫자만 입력 가능합니다.")
//    @Size(min = 0, max = 13, message = "팩스는 10 ~ 11 자리의 숫자만 입력 가능합니다.")
    private String mngEmail;

    @ApiModelProperty(value = "제품명 ", example = "블루투스 수신기")
    @Column
    private String prdctName;

    @ApiModelProperty(value = "업체명 ", example = "")
    @Column
    private String cmpyName;

    @ApiModelProperty(value = "모델명 ", example = "dong-gle")
    @Column
    private String modelName;

    @ApiModelProperty(value = "담당자 아이디 ", example = "welovex2")
    @Column
    private String mngId;

    @ApiModelProperty(value = "서명파일 아이디", example = "")
    @Column
    private String atchFileId;
    
    @ApiModelProperty(value = "시험항목 및 비용 ", example = "")
    @Column
    private List<TestItem> testItems;
  }

  @Getter
  @Setter
  @ToString(callSuper = true)
  @ApiModel(value = "QuoDTO.Res", description = "견적서 조회")
  public static class Res extends Quo {

    @ApiModelProperty(value = "게시글번호", example = "1")
    @Column
    private int no;

    @ApiModelProperty(value = "프로젝트번호", example = "12")
    @Column
    private int jobSeq;
    
    @ApiModelProperty(value = "취합번호", example = "CH2303-001")
    @Column
    private String chqId;

    @ApiModelProperty(value = "견적서번호", example = "Q2303-G0018")
    @Column
    private String quoId;

    @ApiModelProperty(value = "신청서번호", example = "SB23-G0001")
    @Column
    private String sbkId;

    @ApiModelProperty(value = "담당자", example = "김정미")
    @Column
    private String memName;

    @ApiModelProperty(value = "작성일자", example = "2023-03-03")
    @Column
    private String insDtStr;

    @ApiModelProperty(value = "작성자", example = "김가나")
    @Column
    private String insMem;
    
    @ApiModelProperty(value = "매출확정일", example = "2023-03-04")
    @Column
    private String cnfrmDtStr;

    @ApiModelProperty(value = "매출확정자", example = "김정미")
    @Column
    private String cnfrmName;

    @ApiModelProperty(value = "고객유형", example = "컨설팅 or 직고객")
    @Column
    private String cmpyType;

    @ApiModelProperty(value = "청구액", example = "3000000")
    @Column
    private int chrgs;

    @ApiModelProperty(value = "순매출", example = "2400000")
    @Column
    private int netSales;

    @ApiModelProperty(value = "상담서 고유번호", example = "63")
    @Column
    private int cnsSeq;

    @ApiModelProperty(value = "고객 상담내용", example = "어쩌구저쩌구")
    @Column
    private String lastMemo;

    @ApiModelProperty(value = "작성자 영문이름", example = "JM")
    @Column
    private String engName;
    
    @ApiModelProperty(value = "작성자 직급", example = "과장")
    @Column
    private String memPos;

    @ApiModelProperty(value = "작성자 유선번호", example = "031-1111-2222")
    @Column
    private String dir;

    @ApiModelProperty(value = "작성자 핸드폰번호", example = "010-2222-4444")
    @Column
    private String cp;

    @ApiModelProperty(value = "이메일", example = "sb@standardbank.co.kr")
    @Column
    private String email;

    @ApiModelProperty(value = "미수금", example = "0")
    @Column
    private int arrears;

    @ApiModelProperty(value = "계산서 상태", example = "-")
    @Column
    private String billState;

    @ApiModelProperty(value = "계산서 날짜", example = "-")
    @Column
    private String billDtStr;

    @ApiModelProperty(value = "납부 상태", example = "-")
    @Column
    private String payState;

    @ApiModelProperty(value = "납부 날짜", example = "-")
    @Column
    private String payDtStr;

    @ApiModelProperty(value = "고객유형 0001 컨설팅 0002 직고객 0003 없음 ", example = "")
    @Column
    private String cmpyCode;

    @ApiModelProperty(value = "CMPY_SEQ ", example = "")
    @Column
    private int cmpySeq;
    
    @ApiModelProperty(value="담당자 고유번호", example = "3")
    @Column
    private int cmpyMngSeq;
    
    @ApiModelProperty(value = "컨설팅명 ", example = "")
    @Column
    private String cmpyTitle;
    
    @ApiModelProperty(value = "컨설팅 담당자명 ", example = "")
    @Column
    private String cmpyMngName;
    
    @ApiModelProperty(value = "컨설팅 담당자 휴대번호 ", example = "")
    @Column
    private String cmpyMngTel;
    
    @ApiModelProperty(value = "컨설팅 담당자 이메일 ", example = "")
    @Column
    private String cmpyMngEmail;
    
    @ApiModelProperty(value = "업체명 ", example = "")
    @Column
    private String cmpyName;

    @ApiModelProperty(value = "담당자 이름 ", example = "")
    @Column
    private String mngName;

    @ApiModelProperty(value = "담당자 전화번호 ", example = "")
    @Column
    private String mngPhone;

    @ApiModelProperty(value = "담당자 이메일 ", example = "")
    @Column
    private String mngEmail;

    @ApiModelProperty(value = "제품명 ", example = "")
    @Column
    private String prdctName;

    @ApiModelProperty(value = "모델명 ", example = "")
    @Column
    private String modelName;

    @ApiModelProperty(value = "견적서 수정Seq", example = "")
    @Column
    private int quoModeSeq;

    @ApiModelProperty(value = "견적서 수정상태명", example = "")
    @Column
    private String quoModState;

    @ApiModelProperty(value = "재발행신청서 순번", example = "")
    private int revision;
    
    @ApiModelProperty(value = "견적서 변경 날짜", example = "")
    @Column
    private String prmsDtStr;

    @ApiModelProperty(value = "견적서 상태 변경자", example = "")
    @Column
    private String prmsName;
    
    @ApiModelProperty(value = "프로젝트 상태 변경 날짜", example = "")
    @Column
    private String stateUdtDt;

    @ApiModelProperty(value = "프로젝트 상태", example = "")
    @Column
    private String stateCode;

    @ApiModelProperty(value = "서명파일 아이디", example = "")
    @Column
    private String atchFileId;
    
    @ApiModelProperty(value = "시험항목리스트", example = "[]")
    @Column
    private List<TestItemDTO> items;

    @ApiModelProperty(value = "영문견적서 시험항목리스트", example = "[]")
    @Column
    private List<EngTestItem> engItems;

  }
}
