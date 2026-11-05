package egovframework.raw.service;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 로데이터 측정 기간 (RAW_ENV_TB 종료된 행, 체크된 시험방법만).
 *
 * <ul>
 * <li>startDt : 가장 이른 시작일시의 날짜</li>
 * <li>endDt : 가장 늦은 종료일시의 날짜</li>
 * </ul>
 */
@Getter
@Setter
@ToString
public class RawEnvDtRange {
  private LocalDate startDt;
  private LocalDate endDt;

  public boolean isEmpty() {
    return startDt == null || endDt == null;
  }
}
