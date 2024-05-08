package egovframework.tst.dto;

import java.util.List;
import javax.persistence.Column;
import egovframework.tst.service.Test;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public class TestDTO {

  @Getter
  @Setter
  @ToString(callSuper = true)
  @ApiModel(value = "TestDTO.Req", description = "시험 등록")
  public static class Req extends Test {
    @ApiModelProperty(value = "메모 ", example = "바빠서..")
    @Column
    private String memo;

    @ApiModelProperty(value = "시험상태 번호 ", example = "1")
    @Column
    private int testStateSeq;

    @ApiModelProperty(value = "시험상태 ", example = "1")
    @Column
    private String stateCode;

    @ApiModelProperty(value = "시험담당자 ID ", example = "c")
    @Column
    private String testMngId;
  }

  @Getter
  @Setter
  @ApiModel(value = "TestDTO.Res", description = "시험 조회")
  public static class Res extends Test {
    
    @ApiModelProperty(value = "게시글번호", example = "1")
    @Column
    private int no;
    
    @ApiModelProperty(value = "견적서번호", example = " ")
    @Column
    private String quoId;
    
    @ApiModelProperty(value = "견적서작성일", example = " ")
    @Column
    private String quoInsDtStr;
    
    @ApiModelProperty(value = "신청서번호", example = " ")
    @Column
    private String sbkId;
    
    @ApiModelProperty(value = "신청서작성일", example = " ")
    @Column
    private String sbkInsDtStr;
    
    @ApiModelProperty(value = "시료입고일 ", example = "2023-03-21")
    @Column
    private String carryInDt;
    
    @ApiModelProperty(value = "완료요청일 ", example = "2023-05-21")
    @Column
    private String estCmpDt;

    @ApiModelProperty(value = "컨설팅명 ", example = "")
    @Column
    private String cmpyTitle;
    
    @ApiModelProperty(value = "업체명 ", example = "")
    @Column
    private String cmpyName;
    
    @ApiModelProperty(value = "제품명 ", example = "블루투스 수신기")
    @Column
    private String prdctName;
    
    @ApiModelProperty(value = "모델명 ", example = "dong-gle")
    @Column
    private String modelName;
    
    @ApiModelProperty(value = "신청구분", example = "신규, 동일기자재")
    @Column
    private String sgText;
    
    @ApiModelProperty(value = "담당자 이름 ", example = "김담당")
    @Column
    private String mngName;
    
    @ApiModelProperty(value = "시험항목리스트")
    @Column
    private List<TestItemDTO> items;
    
    @ApiModelProperty(value = "프로젝트번호", example = "1")
    @Column
    private int jobSeq;
    
    @ApiModelProperty(value = "프로젝트상태코드", example = "공통코드:CP")
    @Column
    private String stateCode;
    
    @ApiModelProperty(value = "프로젝트상태변경일", example = "")
    @Column
    private String stateUdtDt;
    
    @ApiModelProperty(value = "게시판 글갯수", example = "1")
    private int memoCnt;
    
    @ApiModelProperty(value = "첨부파일 갯수", example = "1")
    private int fileCnt;
    
    @ApiModelProperty(value = "시험 갯수", example = "")
    private int testCnt;
    
    @ApiModelProperty(value = "시험배정일", example = "")
    private String insDtStr;
    
    @ApiModelProperty(value = "시험번호", example = "")
    private String testId;
    
    @ApiModelProperty(value = "시험상태", example = "")
    private String testState;
    
    @ApiModelProperty(value = "시험완료 경과일", example = "")
    private String addDay;
    
    @ApiModelProperty(value = "제품명", example = "")
    private String product;
    
    @ApiModelProperty(value = "모델명", example = "")
    private String model;
    
    @ApiModelProperty(value = "인증종류1", example = "")
    private String crtfc1Name;
    
    @ApiModelProperty(value = "인증종류2", example = "")
    private String crtfc2Name;
    
    @ApiModelProperty(value = "인증종류3", example = "")
    private String crtfc3Name;
    
    @ApiModelProperty(value = "인증종류4", example = "")
    private String crtfc4Name;
    
    @ApiModelProperty(value = "시험부", example = "")
    private String testType;
    
    @ApiModelProperty(value = "시험규격", example = "")
    private String testStndr;
    
    @ApiModelProperty(value = "시험규격번호", example = "")
    private String testStndrSeq;
    
    @ApiModelProperty(value = "기술책임자", example = "")
    private String revname;
 
    @ApiModelProperty(value = "제품지수", example = "")
    private float stand;
    
    @ApiModelProperty(value = "시험담당자1 이름", example = "")
    private String TestMng1Name;
    
    @ApiModelProperty(value = "시험담당자1 참여율", example = "")
    private String TestMng1Part;
    
    @ApiModelProperty(value = "시험담당자2 이름", example = "")
    private String TestMng2Name;
    
    @ApiModelProperty(value = "시험담당자2 참여율", example = "")
    private String TestMng2Part;
    
    @ApiModelProperty(value = "시험담당자3 이름", example = "")
    private String TestMng3Name;
    
    @ApiModelProperty(value = "시험담당자3 참여율", example = "")
    private String TestMng3Part;

    @ApiModelProperty(value = "평가완료 여부", example = "")
    private String ratingState;
    
    @ApiModelProperty(value = "고지부담당자", example = "")
    private String memName;
    
    @ApiModelProperty(value = "로데이터번호", example = "")
    private int rawSeq;
     
    @ApiModelProperty(value = "참고메모", example = "")
    private String memo;
    
    @ApiModelProperty(value = "보안견적서 여부", example = "")
    @Column
    private int secretYn;
    
    @ApiModelProperty(value = "성적서발급일작성일", example = "")
    private String reportDtInsDt;
    
    @ApiModelProperty(value = "성적서발급일작성자", example = "")
    private String reportDtInsName;
  }
}
