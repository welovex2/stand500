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

  @ApiModelProperty(value = "측정일·환경조건 표 (RAW_ENV_TB). 조회 전용, 저장은 /raw/env/*.do")
  private List<RawEnvDTO.Table> envList;

  @ApiModelProperty(value = "측정일 버튼 : START=[시작], END=[종료], 비우면 [저장]. 방법 데이터와 함께 처리", example = "")
  private String envAction;

  @ApiModelProperty(value = "[시작]·[종료] 대상 표 (VDIP)", example = "")
  private String envMetKey;

  @ApiModelProperty(value = "진행 중 측정 행 값 (종료된 행은 무시). [시작]할 새 행 값은 envSeq=0")
  private List<RawEnvDTO.RowReq> envRows;

  @ApiModelProperty(value = "시험고유번호", example = "15", hidden = true)
  @Column
  int testSeq;

  @ApiModelProperty(value = "로데이터 수정여부 (1=수정가능, 0=불가)", example = "1")
  @Column
  private int editYn = 1;

  @ApiModelProperty(value = "측정설비 종류(공통코드 : TM)", example = "MF")
  @Column
  String macType;

  @ApiModelProperty(value = "장비초기화 여부", example = "1")
  int macResetYn;
  
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
