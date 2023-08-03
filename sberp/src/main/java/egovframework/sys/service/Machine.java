package egovframework.sys.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "Machine", description = "시험장비")
public class Machine {

  @ApiModelProperty(value = "MACHINE_SEQ ", example = "")
  @Column
  @NotNull
  private int machineSeq;


  @ApiModelProperty(value = "NAME ", example = "")
  @Column
  private String name;


  @ApiModelProperty(value = "MODEL ", example = "")
  @Column
  private String model;


  @ApiModelProperty(value = "제조사 ", example = "")
  @Column
  private String mnfctCmpny;


  @ApiModelProperty(value = "제조번호 ", example = "")
  @Column
  private String mnfctSerial;


  @ApiModelProperty(value = "차기교정일 ", example = "")
  @Column
  private String reformDt;


  @ApiModelProperty(value = "교정주기(년) ", example = "")
  @Column
  private int reformPeriod;


  @ApiModelProperty(value = "MEMO ", example = "")
  @Column
  private String memo;


  @ApiModelProperty(value = "CEA_YN ", example = "")
  @Column
  private int ceaYn;


  @ApiModelProperty(value = "CEB_YN ", example = "")
  @Column
  private int cebYn;


  @ApiModelProperty(value = "RE_YN ", example = "")
  @Column
  private int reYn;


  @ApiModelProperty(value = "RE_YN ", example = "")
  @Column
  private int reaYn;
  
  
  @ApiModelProperty(value = "RE_YN ", example = "")
  @Column
  private int rebYn;
  
    
  @ApiModelProperty(value = "ESD_YN ", example = "")
  @Column
  private int esdYn;


  @ApiModelProperty(value = "RS_YN ", example = "")
  @Column
  private int rsYn;


  @ApiModelProperty(value = "EFT_YN ", example = "")
  @Column
  private int eftYn;


  @ApiModelProperty(value = "SURGE_YN ", example = "")
  @Column
  private int surgeYn;


  @ApiModelProperty(value = "CS_YN ", example = "")
  @Column
  private int csYn;


  @ApiModelProperty(value = "MFLD_YN ", example = "")
  @Column
  private int mfldYn;


  @ApiModelProperty(value = "VDIP_YN ", example = "")
  @Column
  private int vdipYn;


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


  @ApiModelProperty(value = "상태(I,U,D) ", example = "")
  @Column
  private String state;


}
