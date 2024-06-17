package egovframework.sts.dto;

import java.util.List;
import egovframework.sts.dto.StsDTO.TestTypeList;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TmdDTO {


  @ApiModelProperty(value="시험부서명 ", example = "")
  private String testType;
  
  @ApiModelProperty(value="시험부서별 데이터 ", example = "")
  private List<TestMemList> testMemList;

  @ApiModelProperty(value="시험 월별 데이터 ", example = "")
  private List<TestMonList> testMonList;
  
  @Getter
  @Setter
  @ApiModel(value = "TmdDTO.Res", description = "시험원 데이터")
  public static class TestMemList {
    
    @ApiModelProperty(value="시험부서코드 ", example = "")
    private String testTypeCode;
    
    @ApiModelProperty(value="시험원 아이디 ", example = "")
    private String testMngId;
    @ApiModelProperty(value="시험원명 ", example = "")
    private String testMem;
       
    @ApiModelProperty(value="시험 합계 건수 ", example = "")
    private int eaCnt;
    @ApiModelProperty(value="시험 합계 표준지수 ", example = "")
    private float stand;
    @ApiModelProperty(value="시험 합계 수행지수 ", example = "")
    private float activ;
    
    @ApiModelProperty(value="메인 시험 건수 ", example = "")
    private int mainEaCnt;
    @ApiModelProperty(value="메인 시험 표준지수 ", example = "")
    private float mainStand;
    @ApiModelProperty(value="메인 시험 수행지수 ", example = "")
    private float mainActiv;
    @ApiModelProperty(value="메인 시험 참여율 ", example = "")
    private int mainPart;
    @ApiModelProperty(value="메인 시험 평가점수 ", example = "")
    private float mainRating;
    
    @ApiModelProperty(value="서브 시험 건수 ", example = "")
    private int subEaCnt;
    @ApiModelProperty(value="서브 시험 표준지수 ", example = "")
    private float subStand;
    @ApiModelProperty(value="서브 시험 수행지수 ", example = "")
    private float subActiv;
    @ApiModelProperty(value="서브 시험 참여율 ", example = "")
    private int subPart;
    @ApiModelProperty(value="서브 시험 평가점수 ", example = "")
    private float subRating;
    
  }
  
  @Getter
  @Setter
  @ApiModel(value = "TmdDTO.Res", description = "시험월별 데이터")
  public static class TestMonList {
    
    @ApiModelProperty(value="시험원 아이디 ", example = "")
    private String testMngId;
    @ApiModelProperty(value="시험원명 ", example = "")
    private String testMem;
       
    @ApiModelProperty(value="1월 표준지수 ", example = "")
    private float stand1;
    @ApiModelProperty(value="1월 수행지수 ", example = "")
    private float activ1;
    @ApiModelProperty(value="2월 표준지수 ", example = "")
    private float stand2;
    @ApiModelProperty(value="2월 수행지수 ", example = "")
    private float activ2;
    @ApiModelProperty(value="3월 표준지수 ", example = "")
    private float stand3;
    @ApiModelProperty(value="3월 수행지수 ", example = "")
    private float activ3;
    @ApiModelProperty(value="4월 표준지수 ", example = "")
    private float stand4;
    @ApiModelProperty(value="4월 수행지수 ", example = "")
    private float activ4;
    @ApiModelProperty(value="5월 표준지수 ", example = "")
    private float stand5;
    @ApiModelProperty(value="5월 수행지수 ", example = "")
    private float activ5;
    @ApiModelProperty(value="6월 표준지수 ", example = "")
    private float stand6;
    @ApiModelProperty(value="6월 수행지수 ", example = "")
    private float activ6;
    @ApiModelProperty(value="7월 표준지수 ", example = "")
    private float stand7;
    @ApiModelProperty(value="7월 수행지수 ", example = "")
    private float activ7;
    @ApiModelProperty(value="8월 표준지수 ", example = "")
    private float stand8;
    @ApiModelProperty(value="8월 수행지수 ", example = "")
    private float activ8;
    @ApiModelProperty(value="9월 표준지수 ", example = "")
    private float stand9;
    @ApiModelProperty(value="9월 수행지수 ", example = "")
    private float activ9;
    @ApiModelProperty(value="10월 표준지수 ", example = "")
    private float stand10;
    @ApiModelProperty(value="10월 수행지수 ", example = "")
    private float activ10;
    @ApiModelProperty(value="11월 표준지수 ", example = "")
    private float stand11;
    @ApiModelProperty(value="11월 수행지수 ", example = "")
    private float activ11;
    @ApiModelProperty(value="12월 표준지수 ", example = "")
    private float stand12;
    @ApiModelProperty(value="12월 수행지수 ", example = "")
    private float activ12;
    @ApiModelProperty(value="합계 표준지수 ", example = "")
    private float standSum;
    @ApiModelProperty(value="합계 수행지수 ", example = "")
    private float activSum;
  }
  
  @Getter
  @Setter
  @ApiModel(value = "TmdDTO.Res", description = "시험월별 현황")
  public static class TestResultList {
    
    @ApiModelProperty(value="시험원 아이디 ", example = "")
    private String testMngId;
    @ApiModelProperty(value="시험원명 ", example = "")
    private String testMem;
    
    @ApiModelProperty(value="월별 데이터 ", example = "")
    private List<TestMonList> testMonList;
    
    @Getter
    @Setter
    @ApiModel(value = "TmdDTO.Res", description = "시험월별 데이터")
    public static class TestMonList {
      
      @ApiModelProperty(value="현황월 ", example = "")
      private int baseMon;
      
      @ApiModelProperty(value="완료율 ", example = "")
      private String endRate = "-";
      @ApiModelProperty(value="취소율 ", example = "")
      private String celRate = "-";
      
      @ApiModelProperty(value="메인/서브 데이터 ", example = "")
      private List<TestTypeList> testTypeList;
      
      @Getter
      @Setter
      @ApiModel(value = "TmdDTO.Res", description = "메인/서브별 데이터")
      public static class TestTypeList {
        
        @ApiModelProperty(value="메인/서브 ", example = "")
        private String type;
  
        @ApiModelProperty(value="시험배정 건수 ", example = "")
        private int inCnt;
        @ApiModelProperty(value="시험중 건수 ", example = "")
        private int ingCnt;
        @ApiModelProperty(value="디버깅 건수 ", example = "")
        private int debCnt;
        @ApiModelProperty(value="홀딩 건수 ", example = "")
        private int holCnt;
        @ApiModelProperty(value="RD작성완료 건수 ", example = "")
        private int rdCnt;
        @ApiModelProperty(value="시험취소 건수 ", example = "")
        private int celCnt;
        @ApiModelProperty(value="프로젝트완료 건수 ", example = "")
        private int endCnt;
        @ApiModelProperty(value="프로젝트완료 금액 ", example = "")
        private float endAmt;
        @ApiModelProperty(value="프로젝트완료 참여율 ", example = "")
        private float endPart;
      }
      
    }
    
  }
}
