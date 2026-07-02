package egovframework.raw.service;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Range;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel(description = "로데이터 > TEL")
@Getter
@Setter
@ToString
public class MethodTel {
  @ApiModelProperty(value = "tel_SEQ ", example = "", hidden = true)
  @Column
  private int telSeq;

  @ApiModelProperty(value = "로데이터 SEQ ", example = "")
  @Column
  private int rawSeq;

  @ApiModelProperty(value = "환경_온도 ", example = "")
  @Column
  private String temp;

  @ApiModelProperty(value = "환경_온도 ", example = "")
  @Column
  private String tempPlus;

  @ApiModelProperty(value = "환경_습도 ", example = "")
  @Column
  private String hmdt;

  @ApiModelProperty(value = "환경_습도 ", example = "")
  @Column
  private String hmdtPlus;

  @ApiModelProperty(value = "측정년 ", example = "2023")
  @Column
  @Min(value = 0, message = "측정년은 4 자리의 숫자만 입력 가능합니다.")
  @Max(value = 9999, message = "측정년은 4 자리의 숫자만 입력 가능합니다.")
  private int msrmnYear;


  @ApiModelProperty(value = "측정월 ", example = "01")
  @Column
  @Min(value = 0, message = "측정분은 2 자리의 숫자만 입력 가능합니다.")
  @Max(value = 12, message = "측정월은 2 자리의 숫자만 입력 가능합니다.")
  private int msrmnMon;


  @ApiModelProperty(value = "측정일 ", example = "01")
  @Column
  @Min(value = 0, message = "측정분은 2 자리의 숫자만 입력 가능합니다.")
  @Max(value = 31, message = "측정일은 2 자리의 숫자만 입력 가능합니다.")
  private int msrmnDay;


  @ApiModelProperty(value = "측정시 ", example = "01")
  @Column
  @Min(value = 0, message = "측정분은 2 자리의 숫자만 입력 가능합니다.")
  @Max(value = 24, message = "측정시는 2 자리의 숫자만 입력 가능합니다.")
  private int msrmnHour;


  @ApiModelProperty(value = "측정분 ", example = "01")
  @Column
  @Min(value = 0, message = "측정분은 2 자리의 숫자만 입력 가능합니다.")
  @Max(value = 60, message = "측정분은 2 자리의 숫자만 입력 가능합니다.")
  private int msrmnMin;


  @ApiModelProperty(value = "서명 ", example = "")
  @Column
  private String sign;


  @ApiModelProperty(value = "서명_URL ", example = "")
  @Column
  private String signUrl;


  @ApiModelProperty(value = "시험결과여부 ", example = "")
  @Column
  private String resultCode;


  @ApiModelProperty(value = "비밀번호  시험결과여부 ", example = "")
  @Column
  private String resultPassCode;

  @ApiModelProperty(value = "공장초기 시험결과여부 ", example = "")
  @Column
  private String resultResetCode;

  @ApiModelProperty(value = "초기비밀번호  시험결과여부 ", example = "")
  @Column
  private String resultNoPassCode;

  @ApiModelProperty(value = "비밀번호변경  시험결과여부 ", example = "")
  @Column
  private String resultNewPassCode;

  @ApiModelProperty(value = "1.1-4 3조합 8자리 이상 비밀번호 결과 ", example = "")
  @Column
  private String resultPwd3Code;

  @ApiModelProperty(value = "1.1-4 3조합 비밀번호 길이 수기입력 ", example = "")
  @Column
  private String resultPwd3Memo;

  @ApiModelProperty(value = "1.1-5 2조합 10자리 이상 비밀번호 결과 ", example = "")
  @Column
  private String resultPwd2Code;

  @ApiModelProperty(value = "1.1-5 2조합 비밀번호 길이 수기입력 ", example = "")
  @Column
  private String resultPwd2Memo;

  @ApiModelProperty(value = "1.1-6 접속 차단 연속 오입력 횟수 결과 ", example = "")
  @Column
  private String resultLockCntCode;

  @ApiModelProperty(value = "1.1-7 비밀번호 인증 실패 시 접속 차단 시간 결과 ", example = "")
  @Column
  private String resultLockTimeCode;

  @ApiModelProperty(value = "1.2 시험 결과 ", example = "")
  @Column
  private String resultCloudCode;

  @ApiModelProperty(value = "1.2-1 공장초기화 후 클라우드 등록 전 제어 불가 확인 ", example = "")
  @Column
  private String resultCloudResetCode;

  @ApiModelProperty(value = "1.2-2 3조합 8자리 이상 비밀번호 결과 ", example = "")
  @Column
  private String resultCloudPwd3Code;

  @ApiModelProperty(value = "1.2-2 3조합 비밀번호 길이 수기입력 ", example = "")
  @Column
  private String resultCloudPwd3Memo;

  @ApiModelProperty(value = "1.2-3 2조합 10자리 이상 비밀번호 결과 ", example = "")
  @Column
  private String resultCloudPwd2Code;

  @ApiModelProperty(value = "1.2-3 2조합 비밀번호 길이 수기입력 ", example = "")
  @Column
  private String resultCloudPwd2Memo;

  @ApiModelProperty(value = "1.2-4 보안 등록 과정 확인 결과 ", example = "")
  @Column
  private String resultCloudRegCode;

  @ApiModelProperty(value = "1.2-5 등록 후 조회/제어 동작 확인 결과 ", example = "")
  @Column
  private String resultCloudAccessCode;

  @ApiModelProperty(value = "1.2-6 접속 차단 연속 오입력 횟수 결과 ", example = "")
  @Column
  private String resultCloudLockCntCode;

  @ApiModelProperty(value = "1.2-7 비밀번호 인증 실패 시 접속 차단 시간 결과 ", example = "")
  @Column
  private String resultCloudLockTimeCode;


  @ApiModelProperty(value = "시험자의견A_YN ", example = "")
  @Column
  private int cmntAYn;

  @ApiModelProperty(value = "시험자의견B_YN ", example = "")
  @Column
  private int cmntBYn;


  @ApiModelProperty(value = "시험자의견_B_YN ", example = "")
  @Column
  private String cmntEtc;


  @ApiModelProperty(value = "시험자의견_B_YN ", example = "")
  @Column
  private int cmntEtcYn;

  
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
