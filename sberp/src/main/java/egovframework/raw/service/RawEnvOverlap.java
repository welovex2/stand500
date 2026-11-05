package egovframework.raw.service;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 같은 시험원(서명)의 시간이 겹치는 측정 행 (RAW_ENV_TB). [종료]·기술책임자 수정 때 검사용.
 */
@Getter
@Setter
@ToString
public class RawEnvOverlap {
  private int envSeq;
  /** 시험 접수번호 (예: SB26-G3442-EM3328) */
  private String rawId;
  private String metKey;
  private LocalDateTime testSDt;
  /** 진행 중이면 null */
  private LocalDateTime testEDt;
}
