package egovframework.cnf.service;

import java.time.LocalDateTime;

import javax.persistence.Column;

import org.springmodules.validation.bean.conf.loader.annotation.handler.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel(description = "회사담당자")
@Getter
@Setter
public class CmpyMng {
  @ApiModelProperty(value = "CMPY_MNG_SEQ ", example = "")
  @Column
  @NotNull
  private int cmpyMngSeq;


  @ApiModelProperty(value = "CMPY_SEQ ", example = "", hidden=true)
  @Column
  @NotNull
  private int cmpySeq;


  @ApiModelProperty(value = "이름 ", example = "")
  @Column
  private String name;


  @ApiModelProperty(value = "전화번호 ", example = "")
  @Column
  private String phone;


  @ApiModelProperty(value = "팩스번호 ", example = "")
  @Column
  private String fax;


  @ApiModelProperty(value = "휴대폰번호 ", example = "")
  @Column
  private String tel;


  @ApiModelProperty(value = "이메일 ", example = "")
  @Column
  private String email;


  @ApiModelProperty(value = "부서 ", example = "")
  @Column
  private String dept;


  @ApiModelProperty(value = "직급 ", example = "")
  @Column
  private String level;


  @ApiModelProperty(value = "참고사항 ", example = "")
  @Column
  private String memo;


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
