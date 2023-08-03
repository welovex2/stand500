package egovframework.ppc.service;

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
public class PpHis {
  @ApiModelProperty(value="PP_HIS_SEQ ", example = "")
  @Column
  private int ppHisSeq;

  @ApiModelProperty(value="PP_YM ", example = "")
  @Column
  private int ppYm;
  
  
  @ApiModelProperty(value="PP_SEQ ", example = "")
  @Column
  private int ppSeq;

  
  @ApiModelProperty(value="PP_ID ", example = "")
  private String ppId;
  
  
  @ApiModelProperty(value="MEMO ", example = "")
  @Column
  private String memo;


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

  
  @ApiModelProperty(value="출력용 등록자 ", example = "")
  private String insMem;
  
  
  @ApiModelProperty(value="출력용 시간 ", example = "")
  private String insDtStr;

}
