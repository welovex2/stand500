package egovframework.tst.service;

import java.time.LocalDateTime;
import javax.persistence.Column;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel(description = "시험담당자")
public class TestMngr {
  @ApiModelProperty(value = "TEST_MNGR_SEQ ", example = "")
  @Column
  private int testMngrSeq;

  @ApiModelProperty(value = "TEST_SEQ ", example = "")
  @Column
  private int testSeq;

  @ApiModelProperty(value = "시험담당자 부서")
  @Column
  private int deptSeq;
  
  @ApiModelProperty(value = "시험담당자ID")
  @Column
  private String testMngId;

  @ApiModelProperty(value = "참여율 ", example = "")
  @Column
  private int partRate;

  @ApiModelProperty(value = "평가점수 (공통코드 TO)", example = "")
  @Column
  private String rating;
  
  @ApiModelProperty(value = "사유 ", example = "")
  @Column
  private String memo;

  @ApiModelProperty(value = "등록자 아이디 ", example = "", hidden = true)
  @Column
  private String insMemId;

  @ApiModelProperty(value = "등록 날짜시간 ", example = "", hidden = true)
  @Column
  private LocalDateTime insDt;

  @ApiModelProperty(value = "수정자 아이디 ", example = "", hidden = true)
  @Column
  private String udtMemId;

  @ApiModelProperty(value = "수정 날짜시간 ", example = "", hidden = true)
  @Column
  private LocalDateTime udtDt;

  @ApiModelProperty(value = "상태(I,U,D) ", example = "", hidden = true)
  @Column
  private String state;

}
