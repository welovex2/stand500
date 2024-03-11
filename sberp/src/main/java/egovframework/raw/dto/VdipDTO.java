package egovframework.raw.dto;

import java.util.List;

import javax.persistence.Column;

import egovframework.raw.service.MethodVdip;
import egovframework.raw.service.RawMac;
import io.swagger.annotations.ApiModelProperty;
import lombok.ToString;
import lombok.Setter;
import lombok.Getter;

@Getter
@Setter
@ToString(callSuper = true)
public class VdipDTO extends MethodVdip {

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
  
  @ApiModelProperty(value = "성적서용 시험결과 ", example = "")
  @Column
  private String vol1Report;
  
  
  @ApiModelProperty(value = "성적서용 시험결과 ", example = "")
  @Column
  private String vol2Report;
  
  
  @ApiModelProperty(value = "성적서용 시험결과 ", example = "")
  @Column
  private String vol3Report;
  
  @ApiModelProperty(value = "서명파일 아이디", example = "")
  @Column
  private String atchFileId;
}
