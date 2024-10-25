package egovframework.cnf.service;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Member {

  @ApiModelProperty(value = "no ", example = "")
  @Column
  private int no;
  
  @ApiModelProperty(value = "MEMBER_SEQ ", example = "")
  @Column
  private int memberSeq;


  @ApiModelProperty(value = "입사일 ", example = "", required = true)
  @Column
  private String inDt;


  @ApiModelProperty(value = "부서번호 ", example = "")
  @Column
  private int deptSeq;

  
  @ApiModelProperty(value = "부서 이름 ", example = "")
  @Column
  private String deptName;

  @ApiModelProperty(value = "아이디 ", example = "", required = true)
  @Column
  @NotNull
  private String id;


  @ApiModelProperty(value = "PASSWORD ", example = "")
  @Column
  private String password;


  @ApiModelProperty(value = "MEM_NAME ", example = "", required = true)
  @Column
  private String memName;


  @ApiModelProperty(value = "ENG_NAME ", example = "")
  @Column
  private String engName;
  
  
  @ApiModelProperty(value = "직위 번호 ", example = "")
  @Column
  private int posSeq;

  
  @ApiModelProperty(value = "직위 이름 ", example = "")
  @Column
  private String posName;

  
  @ApiModelProperty(value = "기술책임자 여부 ", example = "")
  @Column
  private int revYn;


  @ApiModelProperty(value = "업무당당자 여부 ", example = "")
  @Column
  private int mngYn;
  
  
  @ApiModelProperty(value = "email ", example = "")
  @Column
  private String email;
  
  
  @ApiModelProperty(value = "PHONE ", example = "")
  @Column
  private String phone;


  @ApiModelProperty(value = "AUTH_CODE ", example = "")
  @Column
  private String authCode;


  @ApiModelProperty(value = "재직상태 (공통코드 : CW) ", example = "")
  @Column
  private String workState;

  @ApiModelProperty(value = "재직상태 (공통코드 : CW) ", example = "")
  @Column
  private String workSate;
  
  @ApiModelProperty(value = "계정잠김상태 ", example = "")
  @Column
  private int lockYn;
  
  
  @ApiModelProperty(value = "휴직시작일 ", example = "")
  @Column
  private String leaveSDt;

  
  @ApiModelProperty(value = "휴직종료일 ", example = "")
  @Column
  private String leaveEDt;

  
  @ApiModelProperty(value = "퇴사일 ", example = "")
  @Column
  private String quitDt;

  @ApiModelProperty(value = "서명파일 ", example = "")
  @Column
  private String atchFileId;
 
  @ApiModelProperty(value = "서명파일 미리보기 ", example = "")
  @Column
  private String sgnUrl;
  
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


}
