package egovframework.tst.dto;

import egovframework.tst.service.TestCate;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TestCateDTO extends TestCate {

  @ApiModelProperty(value = "국가 selectBox 선택값 ", example = "")
  private int depth1Seq;
  
  @ApiModelProperty(value = "국가 직접입력값 ", example = "")
  private String depth1Text;
  
  @ApiModelProperty(value = "인증종류1 선택값 ", example = "")
  private int depth2Seq;
  
  @ApiModelProperty(value = "인증종류1 직접입력값 ", example = "")
  private String depth2Text;
  
  @ApiModelProperty(value = "인증종류2 선택값 ", example = "")
  private int depth3Seq;
  
  @ApiModelProperty(value = "인증종류2 직접입력값 ", example = "")
  private String depth3Text;
  
  @ApiModelProperty(value = "인증종류3 선택값 ", example = "")
  private int depth4Seq;
  
  @ApiModelProperty(value = "인증종류3 직접입력값 ", example = "")
  private String depth4Text;
}
