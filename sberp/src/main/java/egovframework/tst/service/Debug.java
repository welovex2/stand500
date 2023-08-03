package egovframework.tst.service;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Debug {
  @ApiModelProperty(value="DEBUG_SEQ ", example = "")
  @Column
  @NotNull
  private int debugSeq;


  @ApiModelProperty(value="TEST_STATE_SEQ ", example = "")
  @Column
  @NotNull
  private int testStateSeq;


  @ApiModelProperty(value="디버깅 최종결과 ", example = "")
  @Column
  private String stateCode;


  @ApiModelProperty(value="디버깅접수 사유 ", example = "")
  @Column
  private String memo;

  
  @ApiModelProperty(value="최종결과 메모 ", example = "")
  @Column
  private String etc;
  
  
  @ApiModelProperty(value="등록자 아이디 ", example = "", hidden = true)
  @Column
  private String insMemId;


  @ApiModelProperty(value="등록 날짜시간 ", example = "", hidden = true)
  @Column
  private LocalDateTime insDt;


  @ApiModelProperty(value="수정자 아이디 ", example = "", hidden = true)
  @Column
  private String udtMemId;


  @ApiModelProperty(value="수정 날짜시간 ", example = "", hidden = true)
  @Column
  private LocalDateTime udtDt;


  @ApiModelProperty(value="상태(I,U,D) ", example = "", hidden = true)
  @Column
  private String state;


}
