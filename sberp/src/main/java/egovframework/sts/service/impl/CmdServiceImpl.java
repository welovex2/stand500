package egovframework.sts.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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

    // 정렬 수행
    if ("payDesc".equals(param.getSort())) Collections.sort(result, payComparator);
    else if ("arrDesc".equals(param.getSort())) Collections.sort(result, arrComparator);
    else Collections.sort(result, amtComparator);
    
    return result;
  }

    
}
