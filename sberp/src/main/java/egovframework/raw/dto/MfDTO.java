package egovframework.raw.dto;

import java.util.List;

import javax.persistence.Column;

import egovframework.raw.service.MethodMf;
import egovframework.raw.service.RawMac;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class MfDTO extends MethodMf {

  @ApiModelProperty(value = "시험고유번호", example = "15", hidden = true)
  @Column
  int testSeq;

  @ApiModelProperty(value = "측정설비 종류(공통코드 : TM)", example = "MF")
  @Column
  String macType;

  @ApiModelProperty(value = "측정설비 리스트", example = "")
  @Column
  List<RawMac> macList;

  @ApiModelProperty(value = "해당됨/해당없음 ", example = "")
  @Column
  private int picYn = 1;

  @ApiModelProperty(value = "서명파일 아이디", example = "")
  @Column
  private String atchFileId;
}
