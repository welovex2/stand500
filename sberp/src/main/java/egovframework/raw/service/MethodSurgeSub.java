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
public class MethodSurgeSub {
  @ApiModelProperty(value="SURGE_SUB_SEQ ", example = "")
  @Column
  @NotNull
  private int surgeSubSeq;


  @ApiModelProperty(value="SURGE_SEQ ", example = "")
  @Column
  @NotNull
  private int surgeSeq;


  @ApiModelProperty(value="포트테이블구분 > 공통코드 RT ", example = "")
  @Column
  private String applType;


  @ApiModelProperty(value="해당없음_YN ", example = "")
  @Column
  private int noYn;


  @ApiModelProperty(value="교류전원_LN_YN ", example = "")
  @Column
  private int powerLnYn;


  @ApiModelProperty(value="교류전원_LPE_YN ", example = "")
  @Column
  private int powerLpeYn;


  @ApiModelProperty(value="교류전원_NPE_YN ", example = "")
  @Column
  private int powerNpeYn;


  @ApiModelProperty(value="교류전원_ETC ", example = "")
  @Column
  private String powerEtc;


  @ApiModelProperty(value="교류전원_ETC ", example = "")
  @Column
  private int powerEtcYn;


  @ApiModelProperty(value="직류전원_PN_YN ", example = "")
  @Column
  private int dcPowerPnYn;


  @ApiModelProperty(value="직류전원_ETC ", example = "")
  @Column
  private String dcPowerEtc;


  @ApiModelProperty(value="직류전원_ETC ", example = "")
  @Column
  private int dcPowerEtcYn;


  @ApiModelProperty(value="신호선_ETC1 ", example = "")
  @Column
  private String sgnlName;


  @ApiModelProperty(value="PLUS_RESULT_CODE ", example = "")
  @Column
  private String plusResultCode;


  @ApiModelProperty(value="MINUS_RESULT_CODE ", example = "")
  @Column
  private String minusResultCode;


  @ApiModelProperty(value="모드/기타 ", example = "")
  @Column
  private String memo;


  @ApiModelProperty(value="STATE ", example = "")
  @Column
  private String state;


}
