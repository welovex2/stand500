package egovframework.cmm.service;

import java.util.List;
import javax.persistence.Column;
import egovframework.sys.dto.PowerDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @Class Name : LoginVO.java
 * @Description : Login VO class
 * @Modification Information
 * @ @ 수정일 수정자 수정내용 @ ------- -------- --------------------------- @ 2009.03.03 박지욱 최초 생성
 *
 * @author 공통서비스 개발팀 박지욱
 * @since 2009.03.03
 * @version 1.0
 * @see
 * 
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@ApiModel(value = "LoginVO", description = "로그인정보")
public class LoginVO {

  @ApiModelProperty(value = "사원번호", example = "0001", hidden = true)
  @Column
  private String empNo;

  @ApiModelProperty(value = "아이디 ", example = "system")
  @Column
  private String id;

  @ApiModelProperty(value = "이름", example = "문동은", hidden = true)
  @Column
  private String empName;

  @ApiModelProperty(value = "이메일주소", example = "welovex2@standardbank.co.kr", hidden = true)
  @Column
  private String email;

  @ApiModelProperty(value = "비밀번호", example = "akstp!")
  @Column
  private String password;

  @ApiModelProperty(value = "비밀번호", example = "akstp!")
  @Column
  private String newPassword;

  @ApiModelProperty(value = "비밀번호 힌트", example = "1", hidden = true)
  @Column
  private String passwordHint;

  @ApiModelProperty(value = "비밀번호 정답 ", example = "1", hidden = true)
  @Column
  private String passwordCnsr;

  @ApiModelProperty(value = "부서코드 ", example = "", hidden = true)
  private String deptCode;

  @ApiModelProperty(value = "부서명 ", example = "", hidden = true)
  private String deptName;

  @ApiModelProperty(value = "입사일 ", example = "", hidden = true)
  private String hireDt;

  @ApiModelProperty(value = "직위코드", example = "1", hidden = true)
  @Column
  private String positionCode;

  @ApiModelProperty(value = "직위", example = "1", hidden = true)
  @Column
  private String position;

  @ApiModelProperty(value = "연락처", example = "1", hidden = true)
  @Column
  private String phoneNum;

  @ApiModelProperty(value = "유선번호", example = "1", hidden = true)
  @Column
  private String lineNum;

  @ApiModelProperty(value = "권한코드", example = "1", hidden = true)
  @Column
  private String authCode;

  @ApiModelProperty(value = "권한", example = "1", hidden = true)
  @Column
  private String auth;

  @ApiModelProperty(value = "재직상태코드", example = "1", hidden = true)
  @Column
  private String statusCode;

  @ApiModelProperty(value = "재직상태", example = "1", hidden = true)
  @Column
  private String status;

  @ApiModelProperty(value = "휴직 시작기간", example = "1", hidden = true)
  @Column
  private String leaveStartDt;

  @ApiModelProperty(value = "휴직 종료기간", example = "1", hidden = true)
  @Column
  private String leaveEndtDt;

  @ApiModelProperty(value = "로그인 후 이동할 페이지", example = "1", hidden = true)
  @Column
  private String url;

  @ApiModelProperty(value = "사용자 IP정보", example = "1", hidden = true)
  @Column
  private String lastIp;

  @ApiModelProperty(value = "마지막 로그인 날짜", example = "1", hidden = true)
  @Column
  private String lastLoginDt;

  @ApiModelProperty(value = "비밀번호실패횟수", example = "1", hidden = true)
  @Column
  private int failPassCnt;

  @ApiModelProperty(value = "계정잠김여부", example = "1", hidden = true)
  @Column
  private int lockYn;
  
  @ApiModelProperty(value = "서명 파일아이디", example = "", hidden = true)
  @Column
  private String atchFileId;
  
  @ApiModelProperty(value = "서명 미리보기 URL", example = "", hidden = true)
  @Column
  private String sgnUrl;
  
  @ApiModelProperty(value = "보안문서 권한", example = "", hidden = true)
  @Column
  private int secretYn;
  
  @ApiModelProperty(value = "권한", example = "1", hidden = true)
  @Column
  private List<PowerDTO> power;
  
  @ApiModelProperty(value = "로그인 성공여부", example = "1", hidden = true)
  @Column
  private String successYn;
}
