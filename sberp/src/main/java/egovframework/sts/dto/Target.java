package egovframework.sts.dto;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class Target {

  @ApiModelProperty(value="MEM_ID ", example = "", required = true)
  @Column
  @NotNull
  private String memId;


  @ApiModelProperty(value="YEAR ", example = "", required = true)
  @Column
  @NotNull
  private int year;


  @ApiModelProperty(value="JAN ", example = "", required = true)
  @Column
  private int mon1;


  @ApiModelProperty(value="FEB ", example = "", required = true)
  @Column
  private int mon2;


  @ApiModelProperty(value="MAR ", example = "", required = true)
  @Column
  private int mon3;


  @ApiModelProperty(value="APR ", example = "", required = true)
  @Column
  private int mon4;


  @ApiModelProperty(value="MAY ", example = "", required = true)
  @Column
  private int mon5;


  @ApiModelProperty(value="JUN ", example = "", required = true)
  @Column
  private int mon6;


  @ApiModelProperty(value="JUL ", example = "", required = true)
  @Column
  private int mon7;


  @ApiModelProperty(value="AUG ", example = "", required = true)
  @Column
  private int mon8;


  @ApiModelProperty(value="SEP ", example = "", required = true)
  @Column
  private int mon9;


  @ApiModelProperty(value="OCT ", example = "", required = true)
  @Column
  private int mon10;


  @ApiModelProperty(value="NOV ", example = "", required = true)
  @Column
  private int mon11;


  @ApiModelProperty(value="DEC ", example = "", required = true)
  @Column
  private int mon12;


  
}
