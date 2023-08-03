package egovframework.sys.service;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class MachineDTO extends Machine {

  private String reformDtStr;

  @ApiModelProperty(value = "화면노출번호 ", example = "")
  private int disOrdr;

  @ApiModelProperty(value = "표시여부 ", example = "")
  private int useYn;
  
  @ApiModelProperty(value = "시험장비타입 ", example = "", hidden = true)
  private String Type;
}
