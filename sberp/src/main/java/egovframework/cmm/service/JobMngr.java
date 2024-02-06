package egovframework.cmm.service;

import java.time.LocalDateTime;
import javax.persistence.Column;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@ApiModel(description = "업무담당자")
public class JobMngr {

  @ApiModelProperty(value="jobMngrSeq ", example = "")
  @Column
  private int jobMngrSeq;
  
  @ApiModelProperty(value="jobSeq ", example = "")
  @Column
  private int jobSeq;
  
  @ApiModelProperty(value="업무담당자ID ", example = "")
  @Column
  private String mngId;
  
  @ApiModelProperty(value="변경사유 ", example = "")
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
  
}
