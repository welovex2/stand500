package egovframework.tst.dto;

import java.util.List;
import javax.persistence.Column;
import egovframework.tst.service.TestMngr;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class TestMngrDTO {

  @ApiModelProperty(value = "시험 고유번호 ", example = "")
  @Column
  private int testSeq;
    
  @ApiModelProperty(value = "제품지수", example = "")
  private float stand;
  
  @ApiModelProperty(value = "수행지수 ", example = "")
  @Column
  private float activ;
   
  @ApiModelProperty(value = "기술책임자", example = "")
  private String revname;
  
  @ApiModelProperty(value = "배정자 ", example = "")
  @Column
  private String assignName;
    
  @ApiModelProperty(value = "평가자 ", example = "")
  @Column
  private String ratingName;
  
  @ApiModelProperty(value = "평가상태 ", example = "")
  @Column
  private String ratingState;

  @ApiModelProperty(value = "평가날짜 ", example = "")
  @Column
  private String ratingDt;
  
  @ApiModelProperty(value = "수정가능여부 ", example = "")
  @Column
  private int updateYn;
  
  @ApiModelProperty(value = "등록자 아이디 ", example = "")
  @Column
  private String insMemId;

  @ApiModelProperty(value = "수정자 아이디 ", example = "")
  @Column
  private String udtMemId;
  
  @ApiModelProperty(value = "시험담당자리스트", example = "")
  private List<TestMngr> items;
}
