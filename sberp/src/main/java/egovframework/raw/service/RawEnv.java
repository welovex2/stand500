package egovframework.raw.service;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * RAW_ENV_TB 1행 — 시험방법 표의 측정 1회 (시작~종료, 환경조건, 시험원 1명).
 */
@Getter
@Setter
@ToString
public class RawEnv {

  private int envSeq;
  private int rawSeq;
  private String metKey;
  private int rowNo;

  private LocalDateTime testSDt;
  private LocalDateTime testEDt;

  private String temp;
  private String tempPlus;
  private String hmdt;
  private String hmdtPlus;
  private String brmt;
  private String brmtPlus;

  /** [시작] 누른 로그인 사용자 (자동 저장) */
  private String memId;
  private String memName;
  /** 서명 첨부파일 ID (기존 METHOD_*.SIGN_URL 과 같은 의미) */
  private String signUrl;

  private int migYn;

  private int delYn;
  private String delReason;
  private String delMemId;

  private String insMemId;
  private String udtMemId;

  /** 시작됨 + 종료 안 됨 */
  public boolean isInProgress() {
    return testSDt != null && testEDt == null;
  }

  public boolean isEnded() {
    return testEDt != null;
  }
}
