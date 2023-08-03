package egovframework.raw.service;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MethodCsSub {
  @ApiModelProperty(value = "CS_SUB_SEQ ", example = "")
  @Column
  @NotNull
  private int csSubSeq;


  @ApiModelProperty(value = "CS_SEQ ", example = "")
  @Column
  @NotNull
  private int csSeq;


  @ApiModelProperty(value = "인가부위_종류 > 공통코드 RS ", example = "")
  @Column
  private String applType;


  @ApiModelProperty(value = "해당없음_YN ", example = "")
  @Column
  private int noYn;


  @ApiModelProperty(value = "인가부위_명칭 ", example = "")
  @Column
  private String applName;


  @ApiModelProperty(value = "CDN_M2_YN ", example = "")
  @Column
  private int cdnM2Yn;


  @ApiModelProperty(value = "CDN_M3_YN ", example = "")
  @Column
  private int cdnM3Yn;


  @ApiModelProperty(value = "EM Injection clamp_YN ", example = "")
  @Column
  private int emYn;


  @ApiModelProperty(value = "ETC_YN ", example = "")
  @Column
  private int etcYn;


  @ApiModelProperty(value = "ETC ", example = "")
  @Column
  private String etc;


  @ApiModelProperty(value = "RESULT_CODE ", example = "")
  @Column
  private String resultCode;


  @ApiModelProperty(value = "모드/기타 ", example = "")
  @Column
  private String memo;


  @ApiModelProperty(value = "STATE ", example = "")
  @Column
  private String state;


}
