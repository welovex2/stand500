package egovframework.raw.service;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import egovframework.rte.psl.dataaccess.mapper.Mapper;

/**
 * 로데이터 측정일·환경조건 (RAW_ENV_TB).
 */
@Mapper("RawEnvMapper")
public interface RawEnvMapper {

  /** 같은 로데이터에 대한 시작·종료 동시 요청을 줄 세운다 (트랜잭션 안에서 호출) */
  Integer lockRaw(@Param("rawSeq") int rawSeq);

  /** 살아있는 행 (DEL_YN=0). metKey 가 null 이면 전체 */
  List<RawEnv> selectList(@Param("rawSeq") int rawSeq, @Param("metKey") String metKey);

  /** 행 1건 (삭제 행 포함) */
  RawEnv selectOne(@Param("envSeq") int envSeq);

  /** 다음 ROW_NO (삭제 행 포함 MAX+1) */
  int selectNextRowNo(@Param("rawSeq") int rawSeq, @Param("metKey") String metKey);

  /** 시작: TEST_S_DT = NOW(), MEM_ID = 로그인 사용자. envSeq 채워짐 */
  void insertStart(RawEnv env);

  /** 종료: TEST_E_DT = NOW(). 진행 중 행만, 갱신 건수 반환 */
  int updateEnd(@Param("envSeq") int envSeq, @Param("udtMemId") String udtMemId);

  /** 온습도·기압·이름·서명 */
  int updateValues(RawEnv env);

  /** 시작·종료 일시 (기술책임자). 시작된 행만 — 진행 중 행이면 종료 처리가 된다 */
  int updateDates(@Param("envSeq") int envSeq, @Param("testSDt") LocalDateTime testSDt,
      @Param("testEDt") LocalDateTime testEDt, @Param("udtMemId") String udtMemId);

  /** 시험 구역 변경 */
  int updateZone(@Param("envSeq") int envSeq, @Param("zoneSeq") int zoneSeq,
      @Param("udtMemId") String udtMemId);

  // ---------------------------------------------------------------- 챔버 구역

  /** 구역 행 잠금 (트랜잭션 안에서, RAW_TB 잠금 뒤에 호출). 구역이 없으면 null */
  Integer lockZone(@Param("zoneSeq") int zoneSeq);

  /** 그 시험방법에서 고를 수 있는 구역 (구역·챔버 사용 중 + 매핑). 아니면 null */
  RawEnvZone selectZoneForMet(@Param("zoneSeq") int zoneSeq, @Param("metKey") String metKey);

  /** 구역 1건 (사용 여부 상관없이). 없으면 null */
  RawEnvZone selectZone(@Param("zoneSeq") int zoneSeq);

  /** 그 시험방법에서 고를 수 있는 구역 목록 + 지금 사용 중인 건 */
  List<RawEnvZone> selectZones(@Param("metKey") String metKey);

  /**
   * 같은 구역에서 시간이 겹치는 사용 1건 (측정 행 + 로데이터 외 사용). 없으면 null.
   * testEDt 가 null 이면 진행 중(끝 없음)으로 본다. envSeq 행 자신은 제외.
   */
  RawEnvZone selectZoneConflict(@Param("envSeq") int envSeq, @Param("zoneSeq") int zoneSeq,
      @Param("testSDt") LocalDateTime testSDt, @Param("testEDt") LocalDateTime testEDt);

  /** 소프트 삭제 (중단·삭제). 살아있는 행만, 갱신 건수 반환 */
  int softDelete(@Param("envSeq") int envSeq, @Param("reason") String reason,
      @Param("memId") String memId);

  /** MEMBER_TB.REV_YN (기술책임자 여부). 회원 없으면 null */
  Integer selectRevYn(@Param("memId") String memId);

  /**
   * 측정 기간 : 종료된 행(DEL_YN=0) 중 RAW_MET_TB 에 체크된 시험방법만. 행이 없으면 null 또는 빈 값.
   * 기존 rawMapper.selectAllMsrmnDates(METHOD_*_TB UNION ALL) 를 대신한다.
   */
  RawEnvDtRange selectDtRange(@Param("rawSeq") int rawSeq);

  /**
   * 같은 시험원의 시간이 겹치는 측정 행 1건 (없으면 null).
   * 같은 시험원 = 서명 파일(SIGN_URL)이 같거나, MEMBER_SIGN_TB 에서 같은 회원의 서명 파일.
   * 겹침 = 상대 시작 &lt; 이 행 종료 AND (상대 진행 중 OR 상대 종료 &gt; 이 행 시작). 경계 시각은 겹침 아님.
   * 자기 자신·삭제·중단·이관(MIG_YN=1) 행 제외.
   */
  RawEnvOverlap selectOverlap(@Param("envSeq") int envSeq, @Param("signUrl") String signUrl,
      @Param("testSDt") LocalDateTime testSDt, @Param("testEDt") LocalDateTime testEDt);

  /** 시험 작성내역 (TEST_MEMO_TB, 시험리스트 View 모달) */
  void insertTestMemo(@Param("testSeq") int testSeq, @Param("memo") String memo,
      @Param("memId") String memId);
}
