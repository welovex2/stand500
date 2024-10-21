package egovframework.ppc.dto;

import java.util.List;
import javax.persistence.Column;
import egovframework.ppc.service.Pp;
import egovframework.ppc.service.PpHis;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class PpDTO extends Pp {

  @ApiModelProperty(value="사전통관 ID ", example = "")
  String ppId;
  
  @ApiModelProperty(value="메모리스트 ", example = "", hidden = true)
  List<PpHis> memoList;
  
  @ApiModelProperty(value="고객유형 코드", example = "회사타입 0000 협력사(컨설팅) 1000 직접고객" )
  @Column
  private String cmpyCode;
  
  @ApiModelProperty(value = "컨설팅명 ", example = "")
  @Column
  private String cmpyTitle;
  
  @ApiModelProperty(value = "VERSION ", example = "1")
  @Column
  private String sbkVersion;
}
