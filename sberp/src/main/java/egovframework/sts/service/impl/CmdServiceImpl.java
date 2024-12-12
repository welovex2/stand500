package egovframework.sts.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import egovframework.cmm.service.ComParam;
import egovframework.sts.dto.CmdDTO;
import egovframework.sts.service.CmdMapper;
import egovframework.sts.service.CmdService;

@Service("CmdService")
public class CmdServiceImpl implements CmdService {

  @Autowired
  CmdMapper cmdMapper;

  @Override
  public List<CmdDTO> selectList(ComParam param) {
    
    List<CmdDTO> result = new ArrayList<CmdDTO>();
    
    // 월별 시험 현황
    result = cmdMapper.selectList(param);
    
    // 월별 납부 현황
    List<CmdDTO> payResult = cmdMapper.selectPayList(param);
    
    // 납부현황 데이터 넣기
    for (CmdDTO item : result) {
      for (CmdDTO sub : payResult) {
        
        if (item.getCmpySeq().equals(sub.getCmpySeq())) {
          
          item.setTotalPay(sub.getTotalPay());
          item.setTotalArr(sub.getTotalArr());
          item.setMon1Pay(sub.getMon1Pay());
          item.setMon1Arr(sub.getMon1Arr());
          item.setMon2Pay(sub.getMon2Pay());
          item.setMon2Arr(sub.getMon2Arr());
          item.setMon3Pay(sub.getMon3Pay());
          item.setMon3Arr(sub.getMon3Arr());
          item.setMon4Pay(sub.getMon4Pay());
          item.setMon4Arr(sub.getMon4Arr());
          item.setMon5Pay(sub.getMon5Pay());
          item.setMon5Arr(sub.getMon5Arr());
          item.setMon6Pay(sub.getMon6Pay());
          item.setMon6Arr(sub.getMon6Arr());
        }
      }
    }
    
    
    // Comparator를 사용한 내림차순 정렬
    Comparator<CmdDTO> amtComparator = new Comparator<CmdDTO>() {
        @Override
        public int compare(CmdDTO p1, CmdDTO p2) {
            return Long.compare(p2.getTotalAmt(), p1.getTotalAmt()); // p2와 p1을 비교하여 내림차순으로 정렬
        }
    };
    
    // Comparator를 사용한 내림차순 정렬
    Comparator<CmdDTO> payComparator = new Comparator<CmdDTO>() {
        @Override
        public int compare(CmdDTO p1, CmdDTO p2) {
            return Long.compare(p2.getTotalPay(), p1.getTotalPay()); // p2와 p1을 비교하여 내림차순으로 정렬
        }
    };
    
    // Comparator를 사용한 내림차순 정렬
    Comparator<CmdDTO> arrComparator = new Comparator<CmdDTO>() {
        @Override
        public int compare(CmdDTO p1, CmdDTO p2) {
            return Long.compare(p2.getTotalArr(), p1.getTotalArr()); // p2와 p1을 비교하여 내림차순으로 정렬
        }
    };

    // Comparator를 사용한 내림차순 정렬
    Comparator<CmdDTO> debComparator = new Comparator<CmdDTO>() {
        @Override
        public int compare(CmdDTO p1, CmdDTO p2) {
            return Long.compare(p2.getTotalDeb(), p1.getTotalDeb()); // p2와 p1을 비교하여 내림차순으로 정렬
        }
    };
    
    // Comparator를 사용한 내림차순 정렬
    Comparator<CmdDTO> holComparator = new Comparator<CmdDTO>() {
        @Override
        public int compare(CmdDTO p1, CmdDTO p2) {
            return Long.compare(p2.getTotalHol(), p1.getTotalHol()); // p2와 p1을 비교하여 내림차순으로 정렬
        }
    };
    
    
    // 정렬 수행
    if ("payDesc".equals(param.getSort())) Collections.sort(result, payComparator);
    else if ("arrDesc".equals(param.getSort())) Collections.sort(result, arrComparator);
    else if ("debDesc".equals(param.getSort())) Collections.sort(result, debComparator);
    else if ("holDesc".equals(param.getSort())) Collections.sort(result, holComparator);
    else Collections.sort(result, amtComparator);
    
    // 큰 숫자부터 순번 매기기
    AtomicInteger counter = new AtomicInteger(result.size()); // AtomicInteger로 초기값 설정
    result.forEach(dto -> dto.setNo(counter.getAndDecrement())); // 순번 감소
    
    // 합계
    CmdDTO total = new CmdDTO();
    total.setNo(0);
    total.setCmpyName("합계");
    total.setTotalCnt(result.stream().mapToInt(CmdDTO::getTotalCnt).sum());
    total.setTotalAmt(result.stream().mapToLong(CmdDTO::getTotalAmt).sum());
    total.setTotalDeb(result.stream().mapToInt(CmdDTO::getTotalDeb).sum());
    total.setTotalHol(result.stream().mapToInt(CmdDTO::getTotalHol).sum());
    total.setTotalPay(result.stream().mapToLong(CmdDTO::getTotalPay).sum());
    total.setTotalArr(result.stream().mapToLong(CmdDTO::getTotalArr).sum());
    
    total.setMon1Cnt(result.stream().mapToInt(CmdDTO::getMon1Cnt).sum());
    total.setMon1Amt(result.stream().mapToInt(CmdDTO::getMon1Amt).sum());
    total.setMon1Pay(result.stream().mapToLong(CmdDTO::getMon1Pay).sum());
    total.setMon1Arr(result.stream().mapToLong(CmdDTO::getMon1Arr).sum());
    
    total.setMon2Cnt(result.stream().mapToInt(CmdDTO::getMon2Cnt).sum());
    total.setMon2Amt(result.stream().mapToInt(CmdDTO::getMon2Amt).sum());
    total.setMon2Pay(result.stream().mapToLong(CmdDTO::getMon2Pay).sum());
    total.setMon2Arr(result.stream().mapToLong(CmdDTO::getMon2Arr).sum());
    
    total.setMon3Cnt(result.stream().mapToInt(CmdDTO::getMon3Cnt).sum());
    total.setMon3Amt(result.stream().mapToInt(CmdDTO::getMon3Amt).sum());
    total.setMon3Pay(result.stream().mapToLong(CmdDTO::getMon3Pay).sum());
    total.setMon3Arr(result.stream().mapToLong(CmdDTO::getMon3Arr).sum());
    
    total.setMon4Cnt(result.stream().mapToInt(CmdDTO::getMon4Cnt).sum());
    total.setMon4Amt(result.stream().mapToInt(CmdDTO::getMon4Amt).sum());
    total.setMon4Pay(result.stream().mapToLong(CmdDTO::getMon4Pay).sum());
    total.setMon4Arr(result.stream().mapToLong(CmdDTO::getMon4Arr).sum());
    
    total.setMon5Cnt(result.stream().mapToInt(CmdDTO::getMon5Cnt).sum());
    total.setMon5Amt(result.stream().mapToInt(CmdDTO::getMon5Amt).sum());
    total.setMon5Pay(result.stream().mapToLong(CmdDTO::getMon5Pay).sum());
    total.setMon5Arr(result.stream().mapToLong(CmdDTO::getMon5Arr).sum());
    
    total.setMon6Cnt(result.stream().mapToInt(CmdDTO::getMon6Cnt).sum());
    total.setMon6Amt(result.stream().mapToInt(CmdDTO::getMon6Amt).sum());
    total.setMon6Pay(result.stream().mapToLong(CmdDTO::getMon6Pay).sum());
    total.setMon6Arr(result.stream().mapToLong(CmdDTO::getMon6Arr).sum());
    
    result.add(total);
    //-- END 합계
    
    return result;
  }

    
}
