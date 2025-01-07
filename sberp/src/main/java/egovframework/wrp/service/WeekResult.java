package egovframework.wrp.service;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel(description = "주간보고 12개월 시험결과")
public class WeekResult {
  private int wrCnt;                // 보고 회차
  private String testType;          // 부서
  private int twt;                  // 12개월 접수 대기 건수
  private int tin;                  // 12개월 접수 건수
  private int ing;                  // 12개월 시험 중 건수
  private int deb;                  // 12개월 디버깅 건수
  private int hol;                  // 12개월 홀딩 건수
  private int rdd;                  // 12개월 RD 지연 건수
  private int rdc;                  // 12개월 RD 검토 건수
}
