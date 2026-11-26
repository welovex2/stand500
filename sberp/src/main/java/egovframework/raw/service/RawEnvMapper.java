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

  /** 기존 화면 새 행: TEST_S_DT = TEST_E_DT = 측정일, MIG_YN = 2. envSeq 채워짐 */
  void insertLegacy(RawEnv env);

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

  /** 이 구역을 쓰면 함께 점유되는 구역 번호 (자신 포함, 번호 순). 동시 사용 허용 구역이면 빈 목록 */
  List<Integer> selectOccZones(@Param("zoneSeq") int zoneSeq);

  /**
   * 구역 행 잠금 (트랜잭션 안에서, RAW_TB 잠금 뒤에 호출). 주어진 구역만 번호 순으로 잠근다 (chamber-svc 와 같은 순서).
   * 잠근 구역 번호 목록 (없는 구역은 빠진다)
   */
  List<Integer> lockZones(@Param("zoneSeqs") List<Integer> zoneSeqs);

  /**
   * 숨김 챔버(CHAMBER_TB.USE_YN=0)에서 그 시험방법이 매핑된 구역 1건 (번호가 가장 작은 것). 없으면 null.
   * TEL 처럼 챔버를 고르지 않고 고정 구역을 쓰는 시험방법용 (현황판·선택 목록에는 안 보인다)
   */
  RawEnvZone selectHiddenZone(@Param("metKey") String metKey);

  /** 로데이터 시험의 규격 (TEST_ITEM_TB.TEST_STNDR_SEQ). RE 대역(RA·RB) 판단용. 없으면 null */
  Integer selectStndrSeq(@Param("rawSeq") int rawSeq);

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

  /**
   * 1인 1시험: 같은 시험원(MEM_ID)의 진행 중 측정 행(envSeq 제외) 또는 진행 중 현황판 사용 1건. 없으면 null.
   * rs=true(이 행이 RS 측정)면 상대도 RS(RS #2·#3·#4·RE (10m)/RS (#3실))인 건은 제외한다.
   */
  RawEnvZone selectMemBusy(@Param("memId") String memId, @Param("envSeq") int envSeq,
      @Param("rs") boolean rs);

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
   * 같은 시험원(로그인 아이디 MEM_ID)의 시간이 겹치는 측정 행 또는 현황판 사용 1건 (없으면 null).
   * 겹침 = 상대 시작 &lt; 이 행 종료 AND (상대 진행 중 OR 상대 종료 &gt; 이 행 시작). 경계 시각은 겹침 아님.
   * 자기 자신·삭제·중단·이관 행, 챔버 없는 측정 행 제외. rs=true(이 행이 RS 측정)면 상대도 RS 인 건 제외.
   */
  RawEnvZone selectOverlap(@Param("envSeq") int envSeq, @Param("memId") String memId,
      @Param("testSDt") LocalDateTime testSDt, @Param("testEDt") LocalDateTime testEDt,
      @Param("rs") boolean rs);

  /** 시험 작성내역 (TEST_MEMO_TB, 시험리스트 View 모달) */
  void insertTestMemo(@Param("testSeq") int testSeq, @Param("memo") String memo,
      @Param("memId") String memId);
}
