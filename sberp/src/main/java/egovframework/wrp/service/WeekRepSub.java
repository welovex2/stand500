package egovframework.wrp.service;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel(description = "주간보고 수치 결과")
public class WeekRepSub {
  private int wrSeq;          // 보고서 SEQ
  private String testTypeCode;      // 부서코드
  private String testType;          // 부서
  private int week;           // 주차
  private int inCnt;          // 시험 접수 건수
  private double inAmt;       // 시험 접수 금액
  private int rdCnt;          // RD 작성 완료 건수
  private int rdcCnt;         // RD 검토 완료 건수
  private int endCnt;         // 프로젝트 완료 건수
  private double endAmt;      // 프로젝트 완료 금액
  private int celCnt;         // 시험 취소 건수
}
