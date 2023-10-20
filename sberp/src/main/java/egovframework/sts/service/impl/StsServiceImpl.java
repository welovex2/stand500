package egovframework.sts.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import egovframework.cmm.service.ComParam;
import egovframework.sts.dto.StsDTO;
import egovframework.sts.service.StsMapper;
import egovframework.sts.service.StsService;

@Service("StsService")
public class StsServiceImpl implements StsService {

  @Autowired
  StsMapper stsMapper;
  
  @Override
  public StsDTO selectDetail(ComParam param) {
    
    StsDTO detail = new StsDTO();
    
    // 현재 날짜 구하기
    LocalDate now = LocalDate.now();
    // 포맷 정의
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    // 포맷 적용
    String formatedNow = now.format(formatter);
    
    /* 오늘날짜 셋팅 */
    param.setStartDate(formatedNow);
    param.setEndDate(formatedNow);
    detail.setStsDt(formatedNow);
        
    // 1. 오늘 최종상태가 XX인 건
    List<StsDTO.TestTypeList> stateList = stsMapper.selectTodayStateList(param);
    
    // 2. 오늘 시험 배정된 건
    List<StsDTO.TestTypeList> inList = stsMapper.selectInList(param);
    
    /**
     * 시험부별 맞춰서 오늘 시험배정된 건수 넣기
     */
    if (!ObjectUtils.isEmpty(stateList)) {
      for (StsDTO.TestTypeList main : stateList) {
        for (StsDTO.TestTypeList item : inList) {
          if (main.getTestType().equals(item.getTestType())) {
            main.setInCnt(item.getInCnt());
            main.setInAmt(item.getInAmt());
          }
        }
      }
    }
    detail.setTestTypeList(stateList);
    
    // 3. 오늘 매출통계 (계산서발행)
    List<StsDTO> billList = stsMapper.selectBillSum(param);
    if (!ObjectUtils.isEmpty(billList)) detail.setBillAmt(billList.get(0).getBillAmt());
    
    // 4. 오늘 매출통계 (납부완료)
    List<StsDTO> payList = stsMapper.selectPaySum(param);
    if (!ObjectUtils.isEmpty(payList)) detail.setPayAmt(payList.get(0).getPayAmt());
    
    return detail;
  }

  @Override
  public List<StsDTO> selectList(ComParam param) {
    
    // 1. 해당일에 시험상태 합산건
    List<StsDTO> list = stsMapper.selectStateList(param);
    
    // 2. 해당일에 시험 배정된 건
    List<StsDTO.TestTypeList> inList = stsMapper.selectInList(param);
    
    // 3. 오늘 매출통계 (계산서발행)
    List<StsDTO> billList = stsMapper.selectBillSum(param);

    // 4. 오늘 매출통계 (납부완료)
    List<StsDTO> payList = stsMapper.selectPaySum(param);

    /**
     * 시험부별 날짜 맞춰서 시험배정된 건수 넣기
     */
    if (!ObjectUtils.isEmpty(list)) {
      // 날짜별
      for (StsDTO dayData : list) {
        
        // 시험부서별
        for (StsDTO.TestTypeList subMain : dayData.getTestTypeList()) {
          // 시험접수일과 시험부서가 동일하면 셋팅
          for (StsDTO.TestTypeList item : inList) {
            if (dayData.getStsDt().equals(item.getStsDt()) && subMain.getTestType().equals(item.getTestType())) {
              subMain.setInCnt(item.getInCnt());
              subMain.setInAmt(item.getInAmt());
            }
          }
        }
        /**
         * 날짜별 합계 리스트에 추가
         */
        StsDTO.TestTypeList total = new StsDTO.TestTypeList();
        total.setTestType("Total");
        // 시험접수 건수
        total.setInCnt((int) dayData.getTestTypeList().stream().mapToInt(amt -> amt.getInCnt()).summaryStatistics().getSum());
        // 시험접수 금액
        total.setInAmt((int) dayData.getTestTypeList().stream().mapToInt(amt -> amt.getInAmt()).summaryStatistics().getSum());
        // 시험중 건수
        total.setIngCnt((int) dayData.getTestTypeList().stream().mapToInt(amt -> amt.getIngCnt()).summaryStatistics().getSum());
        // 시험중 금액
        total.setIngAmt((int) dayData.getTestTypeList().stream().mapToInt(amt -> amt.getIngAmt()).summaryStatistics().getSum());
        // 디버깅 건수
        total.setDebCnt((int) dayData.getTestTypeList().stream().mapToInt(amt -> amt.getDebCnt()).summaryStatistics().getSum());
        // 디버깅 금액
        total.setDebAmt((int) dayData.getTestTypeList().stream().mapToInt(amt -> amt.getDebAmt()).summaryStatistics().getSum());
        // 홀딩 건수
        total.setHolCnt((int) dayData.getTestTypeList().stream().mapToInt(amt -> amt.getHolCnt()).summaryStatistics().getSum());
        // 홀딩 금액
        total.setHolAmt((int) dayData.getTestTypeList().stream().mapToInt(amt -> amt.getHolAmt()).summaryStatistics().getSum());
        // 시험완료 건수
        total.setEndCnt((int) dayData.getTestTypeList().stream().mapToInt(amt -> amt.getEndCnt()).summaryStatistics().getSum());
        // 시험완료 금액
        total.setEndAmt((int) dayData.getTestTypeList().stream().mapToInt(amt -> amt.getEndAmt()).summaryStatistics().getSum());
        // 성적서발급완료 건수
        total.setRepCnt((int) dayData.getTestTypeList().stream().mapToInt(amt -> amt.getRepCnt()).summaryStatistics().getSum());
        // 성적서발급완료 금액
        total.setRepAmt((int) dayData.getTestTypeList().stream().mapToInt(amt -> amt.getRepAmt()).summaryStatistics().getSum());
        
        dayData.getTestTypeList().add(total);
        //--END 날짜별 합계 리스트에 추가
        
        // 날짜가 동일하면 셋팅
        for (StsDTO item : billList) {
          if (dayData.getStsDt().equals(item.getStsDt())) {
            dayData.setBillAmt(item.getBillAmt());
          }
        }
        
        // 날짜가 동일하면 셋팅
        for (StsDTO item : payList) {
          if (dayData.getStsDt().equals(item.getStsDt())) {
            dayData.setPayAmt(item.getPayAmt());
          }
        }
        
        
      } //-- END 날짜별 FOR

      /**
       * 토탈 결과 추가
       */
      StsDTO total = new StsDTO();
      total.setStsDt("Total");
      total.setBillAmt((int) billList.stream().mapToInt(bill -> bill.getBillAmt()).summaryStatistics().getSum());
      total.setPayAmt((int) payList.stream().mapToInt(pay -> pay.getPayAmt()).summaryStatistics().getSum());
      // 부서별 토탈
      List<StsDTO.TestTypeList> typeTotal = stsMapper.selectTotalStateList(param);
      // 2. 해당일에 시험 배정된 건
      List<StsDTO.TestTypeList> inTotal = stsMapper.selectInList(param);

      // 시험부별 맞춰서 해당이시험배정된 건수 넣기
      if (!ObjectUtils.isEmpty(typeTotal)) {
        for (StsDTO.TestTypeList main : typeTotal) {
          for (StsDTO.TestTypeList item : inTotal) {
            if (main.getTestType().equals(item.getTestType())) {
              main.setInCnt(main.getInCnt() + item.getInCnt());
              main.setInAmt(main.getInAmt() + item.getInAmt());
            }
          }
        }
      }
      total.setTestTypeList(typeTotal);
      
      // 토탈의 토탈 추가
      StsDTO.TestTypeList endTotal = new StsDTO.TestTypeList();
      endTotal.setTestType("Total");
      // 시험접수 건수
      endTotal.setInCnt((int) total.getTestTypeList().stream().mapToInt(amt -> amt.getInCnt()).summaryStatistics().getSum());
      // 시험접수 금액
      endTotal.setInAmt((int) total.getTestTypeList().stream().mapToInt(amt -> amt.getInAmt()).summaryStatistics().getSum());
      // 시험중 건수
      endTotal.setIngCnt((int) total.getTestTypeList().stream().mapToInt(amt -> amt.getIngCnt()).summaryStatistics().getSum());
      // 시험중 금액
      endTotal.setIngAmt((int) total.getTestTypeList().stream().mapToInt(amt -> amt.getIngAmt()).summaryStatistics().getSum());
      // 디버깅 건수
      endTotal.setDebCnt((int) total.getTestTypeList().stream().mapToInt(amt -> amt.getDebCnt()).summaryStatistics().getSum());
      // 디버깅 금액
      endTotal.setDebAmt((int) total.getTestTypeList().stream().mapToInt(amt -> amt.getDebAmt()).summaryStatistics().getSum());
      // 홀딩 건수
      endTotal.setHolCnt((int) total.getTestTypeList().stream().mapToInt(amt -> amt.getHolCnt()).summaryStatistics().getSum());
      // 홀딩 금액
      endTotal.setHolAmt((int) total.getTestTypeList().stream().mapToInt(amt -> amt.getHolAmt()).summaryStatistics().getSum());
      // 시험완료 건수
      endTotal.setEndCnt((int) total.getTestTypeList().stream().mapToInt(amt -> amt.getEndCnt()).summaryStatistics().getSum());
      // 시험완료 금액
      endTotal.setEndAmt((int) total.getTestTypeList().stream().mapToInt(amt -> amt.getEndAmt()).summaryStatistics().getSum());
      // 성적서발급완료 건수
      endTotal.setRepCnt((int) total.getTestTypeList().stream().mapToInt(amt -> amt.getRepCnt()).summaryStatistics().getSum());
      // 성적서발급완료 금액
      endTotal.setRepAmt((int) total.getTestTypeList().stream().mapToInt(amt -> amt.getRepAmt()).summaryStatistics().getSum());
      
      total.getTestTypeList().add(endTotal);
      //-- END 토탈의 토탈 추가
      
      //-- END 토탈 결과 추가
      
      list.add(total);
    }
    
    
    return list;
  }
  
}
