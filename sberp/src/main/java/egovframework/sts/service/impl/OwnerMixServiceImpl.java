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
import egovframework.sts.dto.OwnerMixDTO;
import egovframework.sts.dto.OwnerMixDTO.DeptCol;
import egovframework.sts.dto.OwnerMixDTO.DeptShare;
import egovframework.sts.dto.OwnerMixDTO.OwnerDeptRow;
import egovframework.sts.dto.OwnerMixDTO.OwnerRow;
import egovframework.sts.service.OwnerMixMapper;
import egovframework.sts.service.OwnerMixService;
import egovframework.sts.util.DeptPnlSupport;
import egovframework.sts.util.SalesBriefSupport;

@Service("OwnerMixService")
public class OwnerMixServiceImpl implements OwnerMixService {

  private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

  /** 건당 단가 최고·최저를 뽑을 때 표본이 너무 적은 담당자는 뺀다. 한두 건이면 단가가 튄다. */
  private static final int MIN_CNT_FOR_SPREAD = 20;

  @Autowired
  OwnerMixMapper ownerMixMapper;

  @Override
  public OwnerMixDTO getOwnerMix() {
    LocalDate today = LocalDate.now(SEOUL);

    OwnerMixDTO dto = new OwnerMixDTO();
    dto.setAsOfDate(today.toString());
    dto.setYear(today.getYear());
    dto.setPrevYear(today.getYear() - 1);

    List<OwnerRow> all = ownerMixMapper.selectOwner();
    if (ObjectUtils.isEmpty(all)) {
      return dto;
    }

    List<DeptCol> cols = ownerMixMapper.selectDeptCodes();
    if (cols == null) {
      cols = new ArrayList<DeptCol>();
    }
    dto.setDeptCols(cols);

    // 담당자별 시험부 매출을 (담당자 → 코드 → 줄) 로 접어 둔다.
    Map<String, Map<String, OwnerDeptRow>> byOwner =
        new HashMap<String, Map<String, OwnerDeptRow>>();
    List<OwnerDeptRow> deptRows = ownerMixMapper.selectOwnerDept();
    if (!ObjectUtils.isEmpty(deptRows)) {
      for (OwnerDeptRow d : deptRows) {
        Map<String, OwnerDeptRow> m = byOwner.get(d.getMngId());
        if (m == null) {
          m = new HashMap<String, OwnerDeptRow>();
          byOwner.put(d.getMngId(), m);
        }
        m.put(d.getDeptCode(), d);
      }
    }

    List<OwnerRow> active = new ArrayList<OwnerRow>();
    List<OwnerRow> gone = new ArrayList<OwnerRow>();

    for (OwnerRow r : all) {
      r.setCurNetEok(SalesBriefSupport.eok(r.getCurNetMan()));
      r.setCurAvgMan(DeptPnlSupport.perCase(r.getCurNetMan(), r.getCurCnt()));
      r.setPrevAvgMan(DeptPnlSupport.perCase(r.getPrevNetMan(), r.getPrevCnt()));
      r.setDirectPct(DeptPnlSupport.pct(r.getCurDirectMan(), r.getCurNetMan()));
      r.setHiPct(DeptPnlSupport.pct(r.getCurHiMan(), r.getCurNetMan()));
      r.setCntYoyPct(DeptPnlSupport.yoyPct(r.getCurCnt(), r.getPrevCnt()));
      r.setNetYoyPct(DeptPnlSupport.yoyPct(r.getCurNetMan(), r.getPrevNetMan()));
      r.setAvgYoyPct(DeptPnlSupport.yoyPct(r.getCurAvgMan(), r.getPrevAvgMan()));
      r.setGrowthSrc(growthSrc(r));
      r.setShares(shares(byOwner.get(r.getMngId()), cols, r.getCurNetMan()));

      if (r.getCurNetMan() > 0) {
        active.add(r);
      } else {
        gone.add(r);
      }
    }

    Collections.sort(active, new Comparator<OwnerRow>() {
      @Override
      public int compare(OwnerRow a, OwnerRow b) {
        return Long.compare(b.getCurNetMan(), a.getCurNetMan());
      }
    });
    Collections.sort(gone, new Comparator<OwnerRow>() {
      @Override
      public int compare(OwnerRow a, OwnerRow b) {
        return Long.compare(b.getPrevNetMan(), a.getPrevNetMan());
      }
    });
    dto.setRows(active);
    dto.setGoneRows(gone);

    int totalCnt = 0;
    long totalNet = 0L;
    for (OwnerRow r : active) {
      totalCnt += r.getCurCnt();
      totalNet += r.getCurNetMan();
    }
    dto.setOwnerCnt(active.size());
    dto.setTotalCurCnt(totalCnt);
    dto.setTotalCurNetMan(totalNet);
    dto.setTotalCurNetEok(SalesBriefSupport.eok(totalNet));
    dto.setTotalCurAvgMan(DeptPnlSupport.perCase(totalNet, totalCnt));

    OwnerRow top = null;
    OwnerRow low = null;
    for (OwnerRow r : active) {
      if (r.getCurCnt() < MIN_CNT_FOR_SPREAD) {
        continue;
      }
      if (top == null || r.getCurAvgMan() > top.getCurAvgMan()) {
        top = r;
      }
      if (low == null || r.getCurAvgMan() < low.getCurAvgMan()) {
        low = r;
      }
    }
    if (top != null) {
      dto.setTopAvgName(top.getMngName());
      dto.setTopAvgMan(top.getCurAvgMan());
    }
    if (low != null) {
      dto.setLowAvgName(low.getMngName());
      dto.setLowAvgMan(low.getCurAvgMan());
      if (low.getCurAvgMan() > 0 && top != null) {
        dto.setAvgSpreadX(DeptPnlSupport.round1(top.getCurAvgMan() / low.getCurAvgMan()));
      }
    }

    long goneNet = 0L;
    for (OwnerRow r : gone) {
      goneNet += r.getPrevNetMan();
    }
    dto.setGoneCnt(gone.size());
    dto.setGoneNetMan(goneNet);
    dto.setGoneNetEok(SalesBriefSupport.eok(goneNet));

    return dto;
  }

  /** 매출이 늘었다면 건수 때문인지 단가 때문인지 가른다. */
  static String growthSrc(OwnerRow r) {
    if (r.getPrevNetMan() == 0) {
      return "신규";
    }
    if (r.getCurNetMan() == 0) {
      return "빠짐";
    }
    if (r.getNetYoyPct() <= 0) {
      return "감소";
    }
    return r.getAvgYoyPct() >= r.getCntYoyPct() ? "단가" : "건수";
  }

  /** 시험부 칸을 표의 세로줄 순서대로 채운다. 실적이 없는 시험부도 0으로 자리를 남긴다. */
  static List<DeptShare> shares(Map<String, OwnerDeptRow> mine, List<DeptCol> cols,
      long ownerNetMan) {
    List<DeptShare> list = new ArrayList<DeptShare>();
    for (DeptCol col : cols) {
      DeptShare s = new DeptShare();
      s.setCode(col.getCode());
      s.setName(col.getName());
      OwnerDeptRow d = mine == null ? null : mine.get(col.getCode());
      if (d != null) {
        s.setCnt(d.getCnt());
        s.setNetMan(d.getNetMan());
        s.setSharePct(DeptPnlSupport.pct(d.getNetMan(), ownerNetMan));
      }
      list.add(s);
    }
    return list;
  }
}
