package egovframework.raw.service;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 챔버 구역 (CHAMBER_ZONE_TB) + 그 구역을 쓰고 있는 건 1개.
 *
 * <ul>
 * <li>구역 목록(zones.do) : 사용 중인 건이 없으면 use* 가 비어 있다</li>
 * <li>사용 중·시간 겹침 확인 : 겹치는 건 1개 (use* 만 채워짐)</li>
 * </ul>
 * 사용하는 쪽은 RAW(측정 행, RAW_ENV_TB) 또는 ETC(로데이터 외 사용, CHAMBER_ETC_USE_TB).
 */
@Getter
@Setter
@ToString
public class RawEnvZone {

  private int zoneSeq;
  private int chamberSeq;
  private String chamberName;
  private String zoneName;
  /** 1이면 동시 사용 허용 (사용 중 확인 안 함) */
  private int multiYn;

  /** RAW / ETC. 비어 있으면 사용 중 아님 */
  private String useSrc;
  /** RAW 면 ENV_SEQ, ETC 면 USE_SEQ */
  private Integer useSeq;
  /** 접수번호 (RAW) */
  private String useTestId;
  /** 시험방법 키 (RAW) */
  private String useMetKey;
  /** 용도 (ETC) */
  private String usePurpose;
  private String useMemName;
  private LocalDateTime useStartDt;
  /** 진행 중이면 null */
  private LocalDateTime useEndDt;
}
