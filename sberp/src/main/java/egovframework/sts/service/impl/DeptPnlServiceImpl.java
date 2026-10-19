package egovframework.sts.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import egovframework.sts.dto.DeptPnlDTO;
import egovframework.sts.dto.DeptPnlDTO.BookingRow;
import egovframework.sts.dto.DeptPnlDTO.DeptRow;
import egovframework.sts.dto.DeptPnlDTO.ThroughputRow;
import egovframework.sts.dto.DeptPnlDTO.WipRow;
import egovframework.sts.service.DeptPnlMapper;
import egovframework.sts.service.DeptPnlService;
import egovframework.sts.util.DeptPnlSupport;
import egovframework.sts.util.SalesBriefSupport;

@Service("DeptPnlService")
public class DeptPnlServiceImpl implements DeptPnlService {

  private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

  @Autowired
  DeptPnlMapper deptPnlMapper;

  @Override
  public DeptPnlDTO getDeptPnl() {
    LocalDate today = LocalDate.now(SEOUL);

    DeptPnlDTO dto = new DeptPnlDTO();
    dto.setAsOfDate(today.toString());
    dto.setYear(today.getYear());
    dto.setPrevYear(today.getYear() - 1);

    List<BookingRow> bookings = deptPnlMapper.selectBooking();
    if (ObjectUtils.isEmpty(bookings)) {
      return dto;
    }

    Map<String, ThroughputRow> tpMap = new HashMap<String, ThroughputRow>();
    List<ThroughputRow> tps = deptPnlMapper.selectThroughput();
    if (!ObjectUtils.isEmpty(tps)) {
      for (ThroughputRow tp : tps) {
        tpMap.put(tp.getDeptCode(), tp);
      }
    }

    Map<String, WipRow> wipMap = new HashMap<String, WipRow>();
    List<WipRow> wips = deptPnlMapper.selectWip();
    if (!ObjectUtils.isEmpty(wips)) {
      for (WipRow wip : wips) {
        wipMap.put(wip.getDeptCode(), wip);
      }
    }

    // 비중을 내려면 합계가 먼저 필요하다.
    int totalCurCnt = 0;
    long totalCurNetMan = 0L;
    int totalPrevCnt = 0;
    long totalPrevNetMan = 0L;
    for (BookingRow b : bookings) {
      totalCurCnt += b.getCurCnt();
      totalCurNetMan += b.getCurNetMan();
      totalPrevCnt += b.getPrevCnt();
      totalPrevNetMan += b.getPrevNetMan();
    }

    List<DeptRow> rows = new ArrayList<DeptRow>();
    int totalWipCnt = 0;
    long totalWipMan = 0L;
    int totalStuckCnt = 0;
    long totalStuckMan = 0L;

    for (BookingRow b : bookings) {
      DeptRow r = new DeptRow();
      r.setDeptCode(b.getDeptCode());
      r.setDeptName(b.getDeptName());

      r.setCurCnt(b.getCurCnt());
      r.setCurNetMan(b.getCurNetMan());
      r.setCurChrgsMan(b.getCurChrgsMan());
      r.setPrevCnt(b.getPrevCnt());
      r.setPrevNetMan(b.getPrevNetMan());
      r.setCurNetEok(SalesBriefSupport.eok(b.getCurNetMan()));

      r.setCurAvgMan(DeptPnlSupport.perCase(b.getCurNetMan(), b.getCurCnt()));
      r.setPrevAvgMan(DeptPnlSupport.perCase(b.getPrevNetMan(), b.getPrevCnt()));
      r.setCntYoyPct(DeptPnlSupport.yoyPct(b.getCurCnt(), b.getPrevCnt()));
      r.setNetYoyPct(DeptPnlSupport.yoyPct(b.getCurNetMan(), b.getPrevNetMan()));
      r.setAvgYoyPct(DeptPnlSupport.yoyPct(r.getCurAvgMan(), r.getPrevAvgMan()));
      r.setNetRatePct(DeptPnlSupport.pct(b.getCurNetMan(), b.getCurChrgsMan()));

      r.setCntSharePct(DeptPnlSupport.pct(b.getCurCnt(), totalCurCnt));
      r.setNetSharePct(DeptPnlSupport.pct(b.getCurNetMan(), totalCurNetMan));
      r.setShareGapPct(DeptPnlSupport.round1(r.getNetSharePct() - r.getCntSharePct()));

      ThroughputRow tp = tpMap.get(b.getDeptCode());
      if (tp != null) {
        r.setCurDoneCnt(tp.getCurDoneCnt());
        r.setCurLeadDays(tp.getCurLeadDays());
        r.setCurOver60Cnt(tp.getCurOver60Cnt());
        r.setPrevDoneCnt(tp.getPrevDoneCnt());
        r.setPrevLeadDays(tp.getPrevLeadDays());
        r.setLeadDeltaDays(
            DeptPnlSupport.round1(tp.getCurLeadDays() - tp.getPrevLeadDays()));
        r.setOver60Pct(DeptPnlSupport.pct(tp.getCurOver60Cnt(), tp.getCurDoneCnt()));
      }

      WipRow wip = wipMap.get(b.getDeptCode());
      if (wip != null) {
        r.setWipCnt(wip.getWipCnt());
        r.setWipMan(wip.getWipMan());
        r.setWipEok(SalesBriefSupport.eok(wip.getWipMan()));
        r.setWipAvgAge(wip.getWipAvgAge());
        r.setStuckCnt(wip.getStuckCnt());
        r.setStuckMan(wip.getStuckMan());
        r.setStuckEok(SalesBriefSupport.eok(wip.getStuckMan()));
        r.setAged90Cnt(wip.getAged90Cnt());

        totalWipCnt += wip.getWipCnt();
        totalWipMan += wip.getWipMan();
        totalStuckCnt += wip.getStuckCnt();
        totalStuckMan += wip.getStuckMan();
      } else {
        r.setWipEok(SalesBriefSupport.eok(0L));
        r.setStuckEok(SalesBriefSupport.eok(0L));
      }

      rows.add(r);
    }

    Collections.sort(rows, new Comparator<DeptRow>() {
      @Override
      public int compare(DeptRow a, DeptRow b) {
        return Long.compare(b.getCurNetMan(), a.getCurNetMan());
      }
    });
    dto.setRows(rows);

    dto.setTotalCurCnt(totalCurCnt);
    dto.setTotalCurNetMan(totalCurNetMan);
    dto.setTotalCurNetEok(SalesBriefSupport.eok(totalCurNetMan));
    dto.setTotalPrevCnt(totalPrevCnt);
    dto.setTotalPrevNetMan(totalPrevNetMan);
    dto.setTotalPrevNetEok(SalesBriefSupport.eok(totalPrevNetMan));
    dto.setTotalNetYoyPct(DeptPnlSupport.yoyPct(totalCurNetMan, totalPrevNetMan));
    dto.setTotalCntYoyPct(DeptPnlSupport.yoyPct(totalCurCnt, totalPrevCnt));
    dto.setTotalCurAvgMan(DeptPnlSupport.perCase(totalCurNetMan, totalCurCnt));
    dto.setTotalPrevAvgMan(DeptPnlSupport.perCase(totalPrevNetMan, totalPrevCnt));

    dto.setTotalWipCnt(totalWipCnt);
    dto.setTotalWipMan(totalWipMan);
    dto.setTotalWipEok(SalesBriefSupport.eok(totalWipMan));
    dto.setTotalStuckCnt(totalStuckCnt);
    dto.setTotalStuckMan(totalStuckMan);
    dto.setTotalStuckEok(SalesBriefSupport.eok(totalStuckMan));

    return dto;
  }
}
