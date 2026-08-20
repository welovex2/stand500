package egovframework.raw.dto;

import javax.persistence.Column;
import egovframework.raw.service.MethodTel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class TelDTO extends MethodTel {

  @ApiModelProperty(value = "시험고유번호", example = "15", hidden = true)
  @Column
  int testSeq;

  @ApiModelProperty(value = "로데이터 수정여부 (1=수정가능, 0=불가)", example = "1")
  @Column
  private int editYn = 1;
  
  @ApiModelProperty(value = "서명파일 아이디", example = "")
  @Column
  private String atchFileId;

}
