package egovframework.wrp.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    
    switch (testTypeCode) {
      
      case "EM" :
      case "MD" :
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
        item.setWeekRepSubList(makeFourWeek(wrpMapper.getWeekList(testTypeCode)));
        result.add(item);
        
        testTypeCode = "SR";
        item = new WeekResultDTO();
        detail = wrpMapper.getDetail(testTypeCode);
        item.setTestType(detail.getTestType());
        item.setWeekResult(detail);
        item.setWeekRepSubList(makeFourWeek(wrpMapper.getWeekList(testTypeCode)));
        result.add(item);
        
        break;
      case "SF" :
        
        testTypeCode = "SF";
        detail = wrpMapper.getDetail(testTypeCode);
        item.setTestType(detail.getTestType());
        item.setWeekResult(detail);
        item.setWeekRepSubList(makeFourWeek(wrpMapper.getWeekList(testTypeCode)));
        result.add(item);
        
        testTypeCode = "NS";
        item = new WeekResultDTO();
        detail = wrpMapper.getDetail(testTypeCode);
        item.setTestType(detail.getTestType());
        item.setWeekResult(detail);
        item.setWeekRepSubList(makeFourWeek(wrpMapper.getWeekList(testTypeCode)));
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
    list.sort(Comparator.comparingInt(WeekRepSub::getWeek).reversed()); // 내림차순 정렬

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
    
    switch (testTypeCode) {
      
      case "EM" :
      case "MD" :
        weekResult = wrpMapper.getFixDetail(wrSeq, testTypeCode);
        
        detail.setTestType(weekResult.getTestType());
        detail.setWeekResult(weekResult);
        detail.setWeekRepSubList(makeFourWeek(wrpMapper.getFixWeekList(wrSeq, testTypeCode)));
        
        result.add(detail);
        
        break;
      case "RS" :
        testTypeCode = "RF";
        weekResult = wrpMapper.getFixDetail(wrSeq, testTypeCode);
        
        detail.setTestType(weekResult.getTestType());
        detail.setWeekResult(weekResult);
        detail.setWeekRepSubList(makeFourWeek(wrpMapper.getFixWeekList(wrSeq, testTypeCode)));
        
        result.add(detail);
        
        testTypeCode = "SR";
        detail = new WeekResultDTO();
        weekResult = new WeekResult();
        weekResult = wrpMapper.getFixDetail(wrSeq, testTypeCode);
        
        detail.setTestType(weekResult.getTestType());
        detail.setWeekResult(weekResult);
        detail.setWeekRepSubList(makeFourWeek(wrpMapper.getFixWeekList(wrSeq, testTypeCode)));
        
        result.add(detail);
        
        break;
      case "SF" :
        
        testTypeCode = "SF";
        weekResult = wrpMapper.getFixDetail(wrSeq, testTypeCode);
        
        detail.setTestType(weekResult.getTestType());
        detail.setWeekResult(weekResult);
        detail.setWeekRepSubList(makeFourWeek(wrpMapper.getFixWeekList(wrSeq, testTypeCode)));
        
        result.add(detail);
        
        testTypeCode = "NS";
        detail = new WeekResultDTO();
        weekResult = new WeekResult();
        weekResult = wrpMapper.getFixDetail(wrSeq, testTypeCode);
        
        detail.setTestType(weekResult.getTestType());
        detail.setWeekResult(weekResult);
        detail.setWeekRepSubList(makeFourWeek(wrpMapper.getFixWeekList(wrSeq, testTypeCode)));
        
        result.add(detail);
        
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
