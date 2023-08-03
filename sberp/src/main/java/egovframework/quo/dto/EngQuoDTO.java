package egovframework.quo.dto;

import java.util.List;
import javax.persistence.Column;
import egovframework.quo.service.EngTestItem;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString(callSuper = true)
@ApiModel(value="EngQuoDTO", description = "영문 견적서")
public class EngQuoDTO {

  @ApiModelProperty(value = "견적서번호", example = "Q2303-G0018")
  @Column
  private String quoId;
  
  @ApiModelProperty(value="영문견적서 특이사항 ", example = "이 제품은 \n 특이합니다")
  @Column
  private String engMemo;
  
  @ApiModelProperty(value="Test items and costs ", example = "")
  List<EngTestItem> engTestItems;
  
  @ApiModelProperty(value="등록자 아이디 ", example = "", hidden = true)
  @Column
  private String insMemId;

  @ApiModelProperty(value="수정자 아이디 ", example = "", hidden = true)
  @Column
  private String udtMemId;

}
