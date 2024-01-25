package egovframework.cmm.service;

import java.util.List;
import javax.persistence.Column;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
@ApiModel(description = "업무상태변경")
public class JobVO {
  
  @ApiModelProperty(value="JOB_SEQ ", example = "")
  @Column
  private int jobSeq;

  @ApiModelProperty(value="JOB_SEQ ", example = "")
  private List<Integer> jobSeqs;

  @ApiModelProperty(value="프로젝트상태코드(공통코드:CP) ", example = "")
  @Column
  private String stateCode;
  
  @ApiModelProperty(value="등록자 아이디 ", example = "", hidden = true)
  @Column
  private String insMemId;
}
