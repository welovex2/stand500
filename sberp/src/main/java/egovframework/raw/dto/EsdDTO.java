package egovframework.raw.dto;

import java.util.List;

import javax.persistence.Column;

import egovframework.raw.service.MethodEsd;
import egovframework.raw.service.MethodEsdSub;
import egovframework.raw.service.RawMac;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class EsdDTO extends MethodEsd {

  @ApiModelProperty(value = "시험고유번호", example = "15", hidden = true)
  @Column
  int testSeq;

  @ApiModelProperty(value = "측정설비 종류(공통코드 : TM)", example = "CE")
  @Column
  String macType;

  @ApiModelProperty(value = "측정설비 리스트", example = "")
  @Column
  List<RawMac> macList;

  @ApiModelProperty(value = "시험결과 > 직접인가 ", example = "")
  @Column
  List<MethodEsdSub> subList;

  @ApiModelProperty(value = "정전기 방전 인가부위", example = "")
  @Column
  private List<PicDTO> imgList;
  
  @ApiModelProperty(value="해당됨/해당없음 ", example = "")
  @Column
  private int picYn = 1;
}
