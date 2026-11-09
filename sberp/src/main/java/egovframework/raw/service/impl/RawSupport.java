package egovframework.raw.service.impl;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import egovframework.cmm.service.ResponseMessage;
import egovframework.raw.service.MethodMapper;
import egovframework.raw.service.RawData;
import egovframework.raw.service.RawEnvDtRange;
import egovframework.raw.service.RawEnvMapper;
import egovframework.raw.service.RawMac;
import egovframework.raw.service.RawMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * 로데이터 저장 공통 규칙 — RawServiceImpl(시험방법 저장)과 RawEnvServiceImpl(측정일·환경조건)이 같이 쓴다.
 * 호출하는 쪽 트랜잭션 안에서 동작한다.
 */
@Component("RawSupport")
@Slf4j
public class RawSupport {

  @Autowired
  RawMapper rawMapper;

  @Autowired
  MethodMapper methodMapper;

  @Autowired
  RawEnvMapper rawEnvMapper;

  /** 로데이터가 없을 때 기본 로데이터(RAW_TB) 먼저 등록 */
  public int beforeRawInsert(int testSeq, String insMemId) {
    RawData req = new RawData();
    req.setTestSeq(testSeq);
    req.setInsMemId(insMemId);
    req.setUdtMemId(insMemId);
    req.setSbkId(rawMapper.getSbkId(testSeq));
    rawMapper.insert(req);

    return req.getRawSeq();
  }

  /** testSeq 가 없으면 로데이터에 연결된 시험 */
  public int resolveTestSeq(int testSeq, int rawSeq) {
    if (testSeq == 0 && rawSeq > 0) {
      return rawMapper.selectTestSeqByRawSeq(rawSeq);
    }
    return testSeq;
  }

  /** 시험(TEST_SEQ)의 로데이터 RAW_SEQ (재발행은 원본 시험의 로데이터). 시험·로데이터가 없으면 0 */
  public int resolveRawSeqByTestSeq(int testSeq) {
    if (testSeq == 0) {
      return 0;
    }
    Integer rawSeq = rawMapper.selectRawSeqByTestSeq(testSeq);
    return rawSeq == null ? 0 : rawSeq;
  }

  /**
   * 성적서 발급일이 시험기간·측정기간(RAW_ENV_TB) 중 가장 늦은 날짜보다 앞서면 저장 중단(당일은 허용).
   * testSDt·testEDt 는 호출하는 쪽의 시험기간(요청값 또는 RAW_TB 값), null 이면 측정기간만 본다.
   */
  public void checkReportDtAfterTest(LocalDate reportDt, int rawSeq, LocalDate testSDt,
      LocalDate testEDt) {
    if (reportDt == null) {
      return;
    }

    // 요청값과 실제 측정 기간(RAW_ENV_TB)을 함께 반영 (더 이른 시작·더 늦은 종료 기준)
    RawEnvDtRange range = selectDtRange(rawSeq);
    if (range != null) {
      LocalDate min = range.getStartDt();
      LocalDate max = range.getEndDt();
      if (testSDt == null || min.isBefore(testSDt)) {
        testSDt = min;
      }
      if (testEDt == null || max.isAfter(testEDt)) {
        testEDt = max;
      }
    }

    if ((testSDt != null && reportDt.isBefore(testSDt))
        || (testEDt != null && reportDt.isBefore(testEDt))) {
      throw new IllegalArgumentException(ResponseMessage.CHECK_REPORT_DT);
    }
  }

  /**
   * 시험(TEST_SEQ)의 성적서 발급일 저장 전 검증 — 로데이터가 있는 시험만 (없으면 통과).
   * <ul>
   * <li>프로젝트완료 등으로 로데이터 수정 불가면 저장 불가</li>
   * <li>발급일이 RAW_TB 시험기간·RAW_ENV 측정기간보다 앞서면 저장 불가</li>
   * </ul>
   */
  public void checkReportDtSave(int testSeq, LocalDate reportDt) {
    int rawSeq = resolveRawSeqByTestSeq(testSeq);
    if (rawSeq == 0) {
      return;
    }

    if (rawMapper.selectEditYn(testSeq) == 0) {
      throw new IllegalArgumentException(ResponseMessage.CHECK_RAW_EDIT);
    }

    RawEnvDtRange testDt = rawMapper.selectTestDtByRawSeq(rawSeq);
    checkReportDtAfterTest(reportDt, rawSeq, testDt == null ? null : testDt.getStartDt(),
        testDt == null ? null : testDt.getEndDt());
  }

  /** 'yyyy-MM-dd'(뒤에 시각이 붙어도 앞 10자리) → LocalDate. 비었거나 형식이 틀리면 null */
  public static LocalDate parseLocalDate(String value) {
    if (!StringUtils.hasText(value)) {
      return null;
    }
    String trimmed = value.trim();
    if (trimmed.length() >= 10) {
      trimmed = trimmed.substring(0, 10);
    }
    try {
      return LocalDate.parse(trimmed);
    } catch (DateTimeException e) {
      return null;
    }
  }

  /** 로데이터 수정 가능 여부 (프로젝트완료면 불가) */
  public boolean isRawEditable(int testSeq, int rawSeq) {
    int seq = resolveTestSeq(testSeq, rawSeq);
    return !(seq > 0 && rawMapper.selectEditYn(seq) == 0);
  }

  /**
   * 프로젝트완료 등으로 로데이터 수정 불가인지 확인. testSeq 우선, 없으면 RAW에 연결된 TEST_SEQ로 조회.
   */
  public void assertRawEditable(int testSeq, int rawSeq) {
    if (!isRawEditable(testSeq, rawSeq)) {
      throw new IllegalArgumentException(ResponseMessage.CHECK_RAW_EDIT);
    }
  }

  /**
   * 성적서 발급일이 있으면 측정일은 발급일 이전(당일 포함)인지 검증. testSeq 기준(본시험·재발행 각각 자신의 REPORT_DT).
   */
  public void validateMsrmnDt(int testSeq, LocalDate msrmnDt) {
    if (testSeq == 0 || msrmnDt == null) {
      return;
    }

    LocalDate reportDt = rawMapper.selectReportDt(testSeq);
    if (reportDt == null) {
      return;
    }

    if (msrmnDt.isAfter(reportDt)) {
      throw new IllegalArgumentException(ResponseMessage.CHECK_MSRMN_DT);
    }
  }

  /**
   * 그 로데이터·측정설비 구분(macType)에 등록된 장비의 개조일(차기 교정일)이 측정일보다 빠르면 저장 중단.
   */
  public void checkMacReformDate(int rawSeq, String macType, LocalDate measurementDate) {
    if (measurementDate == null) {
      return;
    }

    List<RawMac> insertedMacList = methodMapper.selectReformDate(rawSeq, macType);

    for (RawMac mac : insertedMacList) {
      if (mac.getReformDt() != null) {
        LocalDate reformDate = LocalDate.parse(mac.getReformDt());
        if (reformDate.isBefore(measurementDate)) {
          log.info("장비 {}는 개조일({})이 측정일({})보다 빠릅니다. 저장 중단.", mac.getMachineSeq(), reformDate,
              measurementDate);
          throw new IllegalArgumentException(ResponseMessage.CHECK_MAC_DATE);
        }
      }
    }
  }

  /**
   * 측정 기간 (RAW_ENV_TB 종료된 행, 체크된 시험방법만). 행이 없으면 null.
   */
  public RawEnvDtRange selectDtRange(int rawSeq) {
    if (rawSeq == 0)
      return null;

    RawEnvDtRange range = rawEnvMapper.selectDtRange(rawSeq);
    return range == null || range.isEmpty() ? null : range;
  }

  /**
   * 측정 기간으로 시험시작일(가장 이른 시작일)·종료일(가장 늦은 종료일)을 갱신한다. 종료된 측정 행이 없으면 둘 다 NULL.
   * testSeq(본시험·재발행 공통)의 성적서발급일이 있으면, 그 이후 측정일이 없는지 검증한다.
   */
  public void recalcAndUpdateTestDtRange(int rawSeq, int testSeq) {
    if (rawSeq == 0)
      return;

    RawEnvDtRange range = selectDtRange(rawSeq);
    if (range == null) {
      rawMapper.updateTestDtRange(rawSeq, null, null);
      return;
    }

    LocalDate startDt = range.getStartDt();
    LocalDate endDt = range.getEndDt();

    // 시험별(testSeq) 성적서발급일 — 본시험·재발행 모두 동일 로직
    if (testSeq > 0) {
      LocalDate reportDt = rawMapper.selectReportDt(testSeq);
      if (reportDt != null && (startDt.isAfter(reportDt) || endDt.isAfter(reportDt))) {
        throw new IllegalArgumentException(ResponseMessage.CHECK_MSRMN_DT);
      }
    }

    rawMapper.updateTestDtRange(rawSeq, startDt, endDt);
  }
}
