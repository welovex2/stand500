package egovframework.sys.service;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import egovframework.sbk.service.Sbk;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter @Getter @ToString
public class Power {

  @ApiModelProperty(value="MENU_SEQ ", example = "", required = true)
  @Column
  @NotNull
  private int menuSeq;

  
  @ApiModelProperty(value="MENU_NAME ", example = "")
  @Column
  private String menuName;
  
  
  @ApiModelProperty(value="P01_R_YN ", example = "", required = true)
  @Column
  private int p01RYn;


  @ApiModelProperty(value="P01_W_YN ", example = "", required = true)
  @Column
  private int p01WYn;


  @ApiModelProperty(value="P02_R_YN ", example = "", required = true)
  @Column
  private int p02RYn;


  @ApiModelProperty(value="P02_W_YN ", example = "", required = true)
  @Column
  private int p02WYn;


  @ApiModelProperty(value="P03_R_YN ", example = "", required = true)
  @Column
  private int p03RYn;


  @ApiModelProperty(value="P03_W_YN ", example = "", required = true)
  @Column
  private int p03WYn;


  @ApiModelProperty(value="P04_R_YN ", example = "", required = true)
  @Column
  private int p04RYn;


  @ApiModelProperty(value="P04_W_YN ", example = "", required = true)
  @Column
  private int p04WYn;


  @ApiModelProperty(value="P05_R_YN ", example = "", required = true)
  @Column
  private int p05RYn;


  @ApiModelProperty(value="P05_W_YN ", example = "", required = true)
  @Column
  private int p05WYn;


  @ApiModelProperty(value="P06_R_YN ", example = "", required = true)
  @Column
  private int p06RYn;


  @ApiModelProperty(value="P06_W_YN ", example = "", required = true)
  @Column
  private int p06WYn;


  @ApiModelProperty(value="P07_R_YN ", example = "", required = true)
  @Column
  private int p07RYn;


  @ApiModelProperty(value="P07_W_YN ", example = "", required = true)
  @Column
  private int p07WYn;


  @ApiModelProperty(value="P08_R_YN ", example = "", required = true)
  @Column
  private int p08RYn;


  @ApiModelProperty(value="P08_W_YN ", example = "", required = true)
  @Column
  private int p08WYn;


  @ApiModelProperty(value="P09_R_YN ", example = "", required = true)
  @Column
  private int p09RYn;


  @ApiModelProperty(value="P09_W_YN ", example = "", required = true)
  @Column
  private int p09WYn;


  @ApiModelProperty(value="P10_R_YN ", example = "", required = true)
  @Column
  private int p10RYn;


  @ApiModelProperty(value="P10_W_YN ", example = "", required = true)
  @Column
  private int p10WYn;


  @ApiModelProperty(value="P11_R_YN ", example = "", required = true)
  @Column
  private int p11RYn;


  @ApiModelProperty(value="P11_W_YN ", example = "", required = true)
  @Column
  private int p11WYn;


  @ApiModelProperty(value="P12_R_YN ", example = "", required = true)
  @Column
  private int p12RYn;


  @ApiModelProperty(value="P12_W_YN ", example = "", required = true)
  @Column
  private int p12WYn;


  @ApiModelProperty(value="P13_R_YN ", example = "", required = true)
  @Column
  private int p13RYn;


  @ApiModelProperty(value="P13_W_YN ", example = "", required = true)
  @Column
  private int p13WYn;


  @ApiModelProperty(value="P14_R_YN ", example = "", required = true)
  @Column
  private int p14RYn;


  @ApiModelProperty(value="P14_W_YN ", example = "", required = true)
  @Column
  private int p14WYn;


  @ApiModelProperty(value="P15_R_YN ", example = "", required = true)
  @Column
  private int p15RYn;


  @ApiModelProperty(value="P15_W_YN ", example = "", required = true)
  @Column
  private int p15WYn;


  @ApiModelProperty(value = "등록자 아이디 ", example = "", hidden = true)
  @Column
  private String insMemId;


  @ApiModelProperty(value = "등록 날짜시간 ", example = "", hidden = true)
  @Column
  private LocalDateTime insDt;


  @ApiModelProperty(value = "수정자 아이디 ", example = "", hidden = true)
  @Column
  private String udtMemId;


  @ApiModelProperty(value = "수정 날짜시간 ", example = "", hidden = true)
  @Column
  private LocalDateTime udtDt;


  @ApiModelProperty(value = "상태(I,U,D) ", example = "", hidden = true)
  @Column
  private String state;
  
  @Getter
  @Setter
  @ToString(callSuper = true)
  @ApiModel(value = "Power.AuthCode", description = "권한종류")
  public static class AuthCode {
    @ApiModelProperty(value = "P01", example = "")
    private String p01;

    @ApiModelProperty(value = "P02", example = "")
    private String p02;

    @ApiModelProperty(value = "P03", example = "")
    private String p03;

    @ApiModelProperty(value = "P04", example = "")
    private String p04;

    @ApiModelProperty(value = "P05", example = "")
    private String p05;

    @ApiModelProperty(value = "P06", example = "")
    private String p06;

    @ApiModelProperty(value = "P07", example = "")
    private String p07;

    @ApiModelProperty(value = "P08", example = "")
    private String p08;

    @ApiModelProperty(value = "P09", example = "")
    private String p09;

    @ApiModelProperty(value = "P10", example = "")
    private String p10;

    @ApiModelProperty(value = "P11", example = "")
    private String p11;

    @ApiModelProperty(value = "P12", example = "")
    private String p12;

    @ApiModelProperty(value = "P13", example = "")
    private String p13;

    @ApiModelProperty(value = "P14", example = "")
    private String p14;

    @ApiModelProperty(value = "P15", example = "")
    private String p15;    
  }

}
