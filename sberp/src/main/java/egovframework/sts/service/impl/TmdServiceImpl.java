package egovframework.sts.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import egovframework.cmm.service.ComParam;
import egovframework.sts.dto.TmdDTO;
import egovframework.sts.dto.TmdDTO.TestResultList;
import egovframework.sts.dto.TmdDTO.TestResultList.TestMonList;
import egovframework.sts.dto.TmdDTO.TestResultList.TestMonList.TestTypeList;
import egovframework.sts.service.TmdMapper;
import egovframework.sts.service.TmdService;

@Service("TmdService")
public class TmdServiceImpl implements TmdService {

  @Autowired
  TmdMapper tmdMapper;

  @Override
  public List<TmdDTO> selectMemList(ComParam param) {
    return tmdMapper.selectMemList(param);
  }

  @Override
  public List<TmdDTO> selectMonList(ComParam param) {
    return tmdMapper.selectMonList(param);
  }

  @Override
  public List<TestResultList> selectResultList(ComParam param) {
    
    List<TestResultList> result = new ArrayList<TmdDTO.TestResultList>();
    
    result = tmdMapper.selectResultList(param);
    
    // 시험원별 묶음
    for (TestResultList mon : result) {
      
        // 월별 묶음
        for (TestMonList item : mon.getTestMonList()) {
          
          
          
          // 메인/서브 리스트 메꾸기
          if (item.getTestTypeList().size() == 1) {
            
            // 아무것도 없으면 메인 넣어주기
            if (item.getTestTypeList().get(0).getType() == null) {
              item.getTestTypeList().get(0).setType("main");
              
              TestTypeList sub = new TestTypeList();
              sub.setType("sub");
              item.getTestTypeList().add(sub);
              
            } else if ("main".equals(item.getTestTypeList().get(0).getType())) {
              TestTypeList sub = new TestTypeList();
              sub.setType("sub");
              item.getTestTypeList().add(sub);
            }
          }
          
          
          // 완료건수
          double endCnt = (double) item.getTestTypeList().stream().mapToInt(cnt -> cnt.getEndCnt()).summaryStatistics().getSum();
          // 배정건수
          double inCnt = (double) item.getTestTypeList().stream().mapToInt(cnt -> cnt.getInCnt()).summaryStatistics().getSum();
          // 취소건수
          double celCnt = (double) item.getTestTypeList().stream().mapToInt(cnt -> cnt.getCelCnt()).summaryStatistics().getSum();
          
          
          if (inCnt != 0) {
            
            //완료율 (메인 시험 프로젝트 완료 건수 + 서브 시험 프로젝트 완료 건수) / (메인 시험 배정 건수 + 서브 시험 배정 건수)
            item.setEndRate(String.format("%.1f", endCnt / inCnt * 100));
            
            //취소율 (메인 시험 취소 건수 + 서브시험 취소 건수) / (메인 시험 배정 건수 + 서브시험배정 건수)
            item.setCelRate(String.format("%.1f", celCnt / inCnt * 100));
          }
      
      }
    }
    
    return result;
  }
    
}
