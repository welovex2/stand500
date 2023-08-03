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
public class MethodRsSub {
  
  @ApiModelProperty(value="RS_SUB_SEQ ", example = "")
  @Column
  @NotNull
  private int rsSubSeq;


  @ApiModelProperty(value="RS_SEQ ", example = "")
  @Column
  @NotNull
  private int rsSeq;


  @ApiModelProperty(value="인가부위_종류 > 공통코드 RA ", example = "")
  @Column
  private String applType;


  @ApiModelProperty(value="해당없음_YN ", example = "")
  @Column
  private int noYn;


  @ApiModelProperty(value="인가부위_명칭 ", example = "")
  @Column
  private String applName;


  @ApiModelProperty(value="수평 성능평가결과 ", example = "")
  @Column
  private String hResultCode;


  @ApiModelProperty(value="수직 성능평가결과 ", example = "")
  @Column
  private String vResultCode;


  @ApiModelProperty(value="모드/기타 ", example = "")
  @Column
  private String memo;


  @ApiModelProperty(value="STATE ", example = "")
  @Column
  private String state;




}
