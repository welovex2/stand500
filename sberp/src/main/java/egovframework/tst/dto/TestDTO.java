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
    
  }
}
