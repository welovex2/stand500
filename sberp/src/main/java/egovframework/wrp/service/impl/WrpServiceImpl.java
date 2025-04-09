package egovframework.wrp.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import egovframework.cmm.service.ComParam;
import egovframework.wrp.dto.WeekResultDTO;
import egovframework.wrp.dto.WeekResultDTO.Req;
import egovframework.wrp.service.WeekRep;
import egovframework.wrp.service.WeekRepSub;
import egovframework.wrp.service.WeekResult;
import egovframework.wrp.service.WrpMapper;
import egovframework.wrp.service.WrpService;

@Service("WrpService")
public class WrpServiceImpl implements WrpService {

  @Autowired
  WrpMapper wrpMapper;
  
  @Override
  public List<WeekResultDTO> getDetail(String testTypeCode) {
    /*
     * EM  EMC
     * RS  RF&SAR
     * SF  SAFETY&효율신뢰
     * MD  MEDICAL
     */
    List<WeekResultDTO> result = new ArrayList<WeekResultDTO>();  // 화면별 데이터
    WeekResultDTO item = new WeekResultDTO(); // 부서별 데이터
    WeekResult detail = new WeekResult(); // 최근 1년 데이터
    List<WeekRepSub> allData = new ArrayList<>(); // 합산할 데이터
    WeekRepSub total = new WeekRepSub();    // 합산 데이터
    
    switch (testTypeCode) {
      
      case "EM" :
        
        // 부서 전체인원
        detail = wrpMapper.getDetail(testTypeCode);
        item.setTestType(detail.getTestType());
        item.setWeekResult(detail);
        item.setWeekRepSubList(makeFourWeek(wrpMapper.getWeekList(testTypeCode)));
        result.add(item);
        
        // 출장인원 별도체크
        testTypeCode = "EMC";
        item = new WeekResultDTO();
        detail = wrpMapper.getDetail(testTypeCode);
        item.setTestType(detail.getTestType());
        item.setWeekResult(detail);
        item.setWeekRepSubList(makeFourWeek(wrpMapper.getWeekList(testTypeCode)));
        result.add(item);
        
        break;
        
      case "MD" :
        
        // 부서 전체인원
        detail = wrpMapper.getDetail(testTypeCode);
        item.setTestType(detail.getTestType());
        item.setWeekResult(detail);
        item.setWeekRepSubList(makeFourWeek(wrpMapper.getWeekList(testTypeCode)));
        result.add(item);
        
        // 출장인원 별도체크
        testTypeCode = "MDC";
        item = new WeekResultDTO();
        detail = wrpMapper.getDetail(testTypeCode);
        item.setTestType(detail.getTestType());
        item.setWeekResult(detail);
        item.setWeekRepSubList(makeFourWeek(wrpMapper.getWeekList(testTypeCode)));
        result.add(item);
        
        break;
        
      case "RS" :
        testTypeCode = "RF";
        detail = wrpMapper.getDetail(testTypeCode);
        item.setTestType(detail.getTestType());
        item.setWeekResult(detail);
        List<WeekRepSub> rsList = makeFourWeek(wrpMapper.getWeekList(testTypeCode));
        item.setWeekRepSubList(rsList);
        result.add(item);
        
        testTypeCode = "SR";
        item = new WeekResultDTO();
        detail = wrpMapper.getDetail(testTypeCode);
        item.setTestType(detail.getTestType());
        item.setWeekResult(detail);
        List<WeekRepSub> srList = makeFourWeek(wrpMapper.getWeekList(testTypeCode));
        item.setWeekRepSubList(srList);
        result.add(item);
        
        // 합계용: testTypeCode = "TT"
        item = new WeekResultDTO();
        item.setTestType("TT");
        
        allData = new ArrayList<>();
        allData.addAll(rsList);
        allData.addAll(srList);
        
        // 스트림으로 누적합
        total = new WeekRepSub();
        for (WeekRepSub w : allData) {
          
          if (w.getWeek() == 99) continue; // 🔥 제외 조건 추가
          
          total.setInCnt(total.getInCnt() + w.getInCnt());
          total.setInAmt(total.getInAmt() + w.getInAmt());
          total.setRdCnt(total.getRdCnt() + w.getRdCnt());
          total.setRdcCnt(total.getRdcCnt() + w.getRdcCnt());
          total.setEndCnt(total.getEndCnt() + w.getEndCnt());
          total.setEndAmt(total.getEndAmt() + w.getEndAmt());
          total.setCelCnt(total.getCelCnt() + w.getCelCnt());
        }
        
        item.setWeekRepSubList(Collections.singletonList(total));
        result.add(item);
        
        break;
      case "SF" :
        
        testTypeCode = "SF";
        detail = wrpMapper.getDetail(testTypeCode);
        item.setTestType(detail.getTestType());
        item.setWeekResult(detail);
        List<WeekRepSub> sfList = makeFourWeek(wrpMapper.getWeekList(testTypeCode));
        item.setWeekRepSubList(sfList);
        result.add(item);
        
        testTypeCode = "NS";
        item = new WeekResultDTO();
        detail = wrpMapper.getDetail(testTypeCode);
        item.setTestType(detail.getTestType());
        item.setWeekResult(detail);
        List<WeekRepSub> nsList = makeFourWeek(wrpMapper.getWeekList(testTypeCode));
        item.setWeekRepSubList(nsList);
        result.add(item);
        
        // 합계용: testTypeCode = "TT"
        item = new WeekResultDTO();
        item.setTestType("TT");
        
        allData = new ArrayList<>();
        allData.addAll(sfList);
        allData.addAll(nsList);
        
        // 스트림으로 누적합
        total = new WeekRepSub();
        for (WeekRepSub w : allData) {
          
          if (w.getWeek() == 99) continue; // 🔥 제외 조건 추가
          
          total.setInCnt(total.getInCnt() + w.getInCnt());
          total.setInAmt(total.getInAmt() + w.getInAmt());
          total.setRdCnt(total.getRdCnt() + w.getRdCnt());
          total.setRdcCnt(total.getRdcCnt() + w.getRdcCnt());
          total.setEndCnt(total.getEndCnt() + w.getEndCnt());
          total.setEndAmt(total.getEndAmt() + w.getEndAmt());
          total.setCelCnt(total.getCelCnt() + w.getCelCnt());
        }
        
        item.setWeekRepSubList(Collections.singletonList(total));
        result.add(item);
        
        break;
    }
    
    
    
    return result ;
  }
  
  private List<WeekRepSub> makeFourWeek(List<WeekRepSub> list) {
    
    String testType = "";
    // 고정된 week 값
    int[] fixedWeeks = {4, 3, 2, 1};
    
    // 이미 포함된 week 값 추적
    Set<Integer> existingWeeks = new HashSet<>();
    for (WeekRepSub weekRep : list) {
        testType = weekRep.getTestType();
        existingWeeks.add(weekRep.getWeek());
    }
    
    // 누락된 week 값 추가
    for (int week : fixedWeeks) {
        if (!existingWeeks.contains(week)) {
          WeekRepSub item = new WeekRepSub();
          item.setTestType(testType);
          item.setWeek(week);
          list.add((item));
        }
    }
    
    // week 기준으로 정렬
    list.sort(Comparator.comparingInt(WeekRepSub::getWeek)); // 오름차순 정렬

    // 합계 계산 (stream + reducing)
    WeekRepSub sumItem = list.stream().reduce(new WeekRepSub(), (acc, cur) -> {
        acc.setInCnt(acc.getInCnt() + cur.getInCnt());
        acc.setInAmt(acc.getInAmt() + cur.getInAmt());
        acc.setRdCnt(acc.getRdCnt() + cur.getRdCnt());
        acc.setRdcCnt(acc.getRdcCnt() + cur.getRdcCnt());
        acc.setEndCnt(acc.getEndCnt() + cur.getEndCnt());
        acc.setEndAmt(acc.getEndAmt() + cur.getEndAmt());
        acc.setCelCnt(acc.getCelCnt() + cur.getCelCnt());
        return acc;
    });

    sumItem.setTestType(testType);
    sumItem.setWeek(99); // 합계 표시용

    list.add(sumItem); // 마지막에 추가
    
    return list;
    
  }

  @Override
  public WeekResultDTO.Req checkReport(int wrSeq) {
    return wrpMapper.checkReport(wrSeq);
  }

  @Override
  public List<WeekResultDTO> getFixDetail(int wrSeq, String testTypeCode) {
    List<WeekResultDTO> result = new ArrayList<WeekResultDTO>();  // 화면별 데이터
    WeekResultDTO detail = new WeekResultDTO();
    WeekResult weekResult = new WeekResult();
    List<WeekRepSub> allData = new ArrayList<>();
    WeekRepSub total = new WeekRepSub();
    WeekResultDTO sumItem = new WeekResultDTO();
    
    switch (testTypeCode) {
      
      case "EM" :
      case "MD" :
        weekResult = wrpMapper.getFixDetail(wrSeq, testTypeCode);
        
        detail.setTestType(weekResult.getTestType());
        detail.setWeekResult(weekResult);
        detail.setWeekRepSubList(makeFourWeek(wrpMapper.getFixWeekList(wrSeq, testTypeCode)));
        
        result.add(detail);
        
        // 출장인원
        WeekResultDTO tripItem = new WeekResultDTO();
        String tripCode = testTypeCode.equals("EM") ? "EMC" : "MDC";
        WeekResult tripDetail = wrpMapper.getFixDetail(wrSeq, tripCode);
        
        if (!ObjectUtils.isEmpty(tripDetail)) {
          tripItem.setTestType(tripDetail.getTestType());
          tripItem.setWeekResult(tripDetail);
        } else {
          tripItem.setTestType("출장");
          tripItem.setWeekResult(new WeekResult());
        }
        tripItem.setWeekRepSubList(makeFourWeek(wrpMapper.getFixWeekList(wrSeq, tripCode)));
        result.add(tripItem);
        
        break;
        
      case "RS" :
        testTypeCode = "RF";
        weekResult = wrpMapper.getFixDetail(wrSeq, testTypeCode);
        
        detail.setTestType(weekResult.getTestType());
        detail.setWeekResult(weekResult);
        List<WeekRepSub> rfList = makeFourWeek(wrpMapper.getFixWeekList(wrSeq, testTypeCode));
        detail.setWeekRepSubList(rfList);
        
        result.add(detail);
        
        testTypeCode = "SR";
        detail = new WeekResultDTO();
        weekResult = new WeekResult();
        weekResult = wrpMapper.getFixDetail(wrSeq, testTypeCode);
        
        detail.setTestType(weekResult.getTestType());
        detail.setWeekResult(weekResult);
        List<WeekRepSub> srList = makeFourWeek(wrpMapper.getFixWeekList(wrSeq, testTypeCode));
        detail.setWeekRepSubList(srList);
        
        result.add(detail);
        
        // 합계
        allData.addAll(rfList);
        allData.addAll(srList);
        total = new WeekRepSub();
        for (WeekRepSub w : allData) {
          
            if (w.getWeek() == 99) continue; // 🔥 제외 조건 추가
          
            total.setInCnt(total.getInCnt() + w.getInCnt());
            total.setInAmt(total.getInAmt() + w.getInAmt());
            total.setRdCnt(total.getRdCnt() + w.getRdCnt());
            total.setRdcCnt(total.getRdcCnt() + w.getRdcCnt());
            total.setEndCnt(total.getEndCnt() + w.getEndCnt());
            total.setEndAmt(total.getEndAmt() + w.getEndAmt());
            total.setCelCnt(total.getCelCnt() + w.getCelCnt());
        }

        sumItem = new WeekResultDTO();
        sumItem.setTestType("TT");
        sumItem.setWeekRepSubList(Collections.singletonList(total));
        result.add(sumItem);
        
        break;
      case "SF" :
        
        testTypeCode = "SF";
        weekResult = wrpMapper.getFixDetail(wrSeq, testTypeCode);
        
        detail.setTestType(weekResult.getTestType());
        detail.setWeekResult(weekResult);
        List<WeekRepSub> sfList = makeFourWeek(wrpMapper.getFixWeekList(wrSeq, testTypeCode));
        detail.setWeekRepSubList(sfList);
        
        result.add(detail);
        
        testTypeCode = "NS";
        detail = new WeekResultDTO();
        weekResult = new WeekResult();
        weekResult = wrpMapper.getFixDetail(wrSeq, testTypeCode);
        
        detail.setTestType(weekResult.getTestType());
        detail.setWeekResult(weekResult);
        List<WeekRepSub> nsList = makeFourWeek(wrpMapper.getFixWeekList(wrSeq, testTypeCode));
        detail.setWeekRepSubList(nsList);
        
        result.add(detail);

        // 합계
        allData.addAll(sfList);
        allData.addAll(nsList);
        total = new WeekRepSub();
        for (WeekRepSub w : allData) {
          
            if (w.getWeek() == 99) continue; // 🔥 제외 조건 추가
          
            total.setInCnt(total.getInCnt() + w.getInCnt());
            total.setInAmt(total.getInAmt() + w.getInAmt());
            total.setRdCnt(total.getRdCnt() + w.getRdCnt());
            total.setRdcCnt(total.getRdcCnt() + w.getRdcCnt());
            total.setEndCnt(total.getEndCnt() + w.getEndCnt());
            total.setEndAmt(total.getEndAmt() + w.getEndAmt());
            total.setCelCnt(total.getCelCnt() + w.getCelCnt());
        }

        sumItem = new WeekResultDTO();
        sumItem.setTestType("TT");
        sumItem.setWeekRepSubList(Collections.singletonList(total));
        result.add(sumItem);
        
        break;
    }
    
    return result;
  }

  @Override
  public String getFeedback(String testTypeCode) {
    return wrpMapper.getFeedback(testTypeCode);
  }

  @Override
  public boolean insert(WeekRep req) {
    return wrpMapper.insert(req);
  }

  @Override
  public boolean update(WeekRep req) {
    return wrpMapper.update(req);
  }

  @Override
  public WeekRep getReport(int wrSeq) {
    return wrpMapper.getReport(wrSeq);
  }

  @Override
  @Transactional
  public boolean updateFix(WeekResultDTO.Req req) {
    boolean result = true;
    
    // 부서정보얻기
    WeekRep data = wrpMapper.getReport(req.getWrSeq());
    // 상태변경
    wrpMapper.updateFix(req);
    
    switch (data.getTestTypeCode()) {
      
      case "EM" :
      case "MD" :
        req.setTestTypeCode(data.getTestTypeCode());
        
        // 데이터확정
        wrpMapper.insertFixResult(req);
        wrpMapper.insertFixSub(req);
        
        // 출장 데이터 확정
        String tripCode = data.getTestTypeCode().equals("EM") ? "EMC" : "MDC";
        req.setTestTypeCode(tripCode);
        wrpMapper.insertFixResult(req);
        wrpMapper.insertFixSub(req);
        
        break;
      case "RS" :
        req.setTestTypeCode("RF");
        
        // 데이터확정
        wrpMapper.insertFixResult(req);
        wrpMapper.insertFixSub(req);
        
        req.setTestTypeCode("SR");
        
        // 데이터확정
        wrpMapper.insertFixResult(req);
        wrpMapper.insertFixSub(req);
        
        break;
      case "SF" :
        
        req.setTestTypeCode("SF");
        
        // 데이터확정
        wrpMapper.insertFixResult(req);
        wrpMapper.insertFixSub(req);
        
        req.setTestTypeCode("NS");
        
        // 데이터확정
        wrpMapper.insertFixResult(req);
        wrpMapper.insertFixSub(req);
        
        break;
    }
    

    return result;
  }

  @Override
  public int selectListCnt(ComParam param) {
    return wrpMapper.selectListCnt(param);
  }

  @Override
  public List<WeekRep> selectList(ComParam param) {
    return wrpMapper.selectList(param);
  }

  @Override
  public boolean delete(Req req) {
    return wrpMapper.delete(req);
  }

}
