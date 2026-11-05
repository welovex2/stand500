package egovframework.raw.service;

import java.util.List;

import egovframework.raw.dto.RawEnvDTO;

/**
 * 로데이터 측정일·환경조건 (RAW_ENV_TB).
 *
 * 검증 실패는 IllegalArgumentException(메시지 = ResponseMessage.CHECK_ENV_* 등)으로 던진다.
 */
public interface RawEnvService {

  /** 표 조회. metKey 가 비면 행이 있는 표 전체 */
  RawEnvDTO.ListRes list(int rawSeq, int testSeq, String metKey, String loginId);

  /**
   * 지정한 시험방법 표들 (행이 없어도 빈 표). 방법별 detail 응답의 envList 용. 예) RE → RE1, RE2, RE3
   */
  List<RawEnvDTO.Table> tables(int rawSeq, int testSeq, String loginId, String... metKeys);

  /**
   * 방법별 insert.do 에 실려 온 측정 행 처리. 방법 데이터 저장과 같은 트랜잭션에서 호출한다.
   *
   * <ol>
   * <li>envRows 중 진행 중 행(envSeq &gt; 0)의 온습도·이름·서명 저장. 종료·삭제된 행은 무시</li>
   * <li>action=START : envMetKey 표에 새 행 + 시작일시. envSeq=0 인 envRows 값을 새 행에 저장</li>
   * <li>action=END : envMetKey 표의 진행 중 행에 종료일시. 온도·습도(·기압)·시험자명 필수</li>
   * </ol>
   *
   * @param allowedKeys 그 시험방법 화면이 다루는 표 (CE → CE, RE → RE1·RE2·RE3)
   */
  void applyFromMethod(int rawSeq, int testSeq, String loginId, String action, String metKey,
      List<RawEnvDTO.RowReq> rows, String... allowedKeys);

  /** [중단] 진행 중 행 삭제 + 작성내역 기록 */
  boolean stop(int envSeq, RawEnvDTO.ReasonReq req, String loginId);

  /** 종료된 행 수정 (기술책임자) : 일시·온습도·기압·이름·서명 */
  RawEnvDTO.Row update(int envSeq, RawEnvDTO.UpdateReq req, String loginId);

  /** 행 삭제 (기술책임자) + 작성내역 기록 */
  boolean delete(int envSeq, RawEnvDTO.ReasonReq req, String loginId);
}
