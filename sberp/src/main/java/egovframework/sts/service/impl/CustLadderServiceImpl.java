package egovframework.sts.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import egovframework.sts.dto.CustLadderDTO;
import egovframework.sts.dto.CustLadderDTO.BaseRow;
import egovframework.sts.dto.CustLadderDTO.CustRow;
import egovframework.sts.dto.CustLadderDTO.DeptCode;
import egovframework.sts.dto.CustLadderDTO.StepRow;
import egovframework.sts.service.CustLadderMapper;
import egovframework.sts.service.CustLadderService;
import egovframework.sts.util.CustMergeSupport;
import egovframework.sts.util.DeptPnlSupport;
import egovframework.sts.util.SalesBriefSupport;

@Service("CustLadderService")
public class CustLadderServiceImpl implements CustLadderService {

  private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

  /** 의뢰 횟수 구간 이름. */
  private static final String[] LADDER_LABEL = {"1회만", "2~3회", "4~10회", "11~50회", "51회 이상"};

  /** 이용 시험부 개수 구간 이름. */
  private static final String[] BREADTH_LABEL = {"1개", "2개", "3개", "4개", "5개 이상"};

  /**
   * 아직 안 쓴 시험부를 권할 때 대행은 빼놓는다. 대행은 외주를 통과시키는 매출이라 청구액 대비 남는 비율이 30% 대로 낮고,
   * 우리 설비를 채우는 일이 아니다.
   */
  private static final String SKIP_DEPT = "DH";

  private static final int LIST_LIMIT = 30;

  @Autowired
  CustLadderMapper custLadderMapper;

  @Override
  public CustLadderDTO getLadder() {
    LocalDate today = LocalDate.now(SEOUL);
    LocalDate yearStart = today.withDayOfYear(1);

    CustLadderDTO dto = new CustLadderDTO();
    dto.setAsOfDate(today.toString());
    dto.setYear(today.getYear());

    List<BaseRow> base = custLadderMapper.selectCustBase();
    if (ObjectUtils.isEmpty(base)) {
      return dto;
    }

    Map<String, String> deptNames = new LinkedHashMap<String, String>();
    List<DeptCode> codes = custLadderMapper.selectDeptCodes();
    if (!ObjectUtils.isEmpty(codes)) {
      for (DeptCode c : codes) {
        deptNames.put(c.getCode(), c.getName());
      }
    }

    List<List<BaseRow>> groups = CustMergeSupport.merge(base);

    dto.setRawCnt(base.size());
    dto.setCustCnt(groups.size());
    int merged = 0;
    for (List<BaseRow> g : groups) {
      if (g.size() > 1) {
        merged++;
      }
    }
    dto.setMergedCnt(merged);

    List<Cust> custs = new ArrayList<Cust>();
    for (List<BaseRow> g : groups) {
      custs.add(build(g, deptNames, today));
    }

    // 요약: 올해 매출을 낸 고객을 올해 첫 거래와 그 전부터 거래로 나눈다.
    long netCurMan = 0L;
    long existNetMan = 0L;
    long newNetMan = 0L;
    int existCnt = 0;
    int newCnt = 0;
    for (Cust c : custs) {
      if (c.netCurMan <= 0) {
        continue;
      }
      netCurMan += c.netCurMan;
      if (c.firstDt != null && !c.firstDt.isBefore(yearStart)) {
        newCnt++;
        newNetMan += c.netCurMan;
      } else {
        existCnt++;
        existNetMan += c.netCurMan;
      }
    }
    dto.setNetCurMan(netCurMan);
    dto.setNetCurEok(SalesBriefSupport.eok(netCurMan));
    dto.setExistCnt(existCnt);
    dto.setExistNetMan(existNetMan);
    dto.setExistNetEok(SalesBriefSupport.eok(existNetMan));
    dto.setNewCnt(newCnt);
    dto.setNewNetMan(newNetMan);
    dto.setNewNetEok(SalesBriefSupport.eok(newNetMan));
    dto.setExistSharePct(DeptPnlSupport.pct(existNetMan, netCurMan));

    dto.setLadder(steps(custs, LADDER_LABEL, true));
    dto.setBreadth(steps(custs, BREADTH_LABEL, false));

    // 한 번만 의뢰하고 안 온 고객. 30일 미만은 판단하기 이르고, 540일이 넘으면 사실상 남이다.
    List<Cust> once = new ArrayList<Cust>();
    for (Cust c : custs) {
      if (c.orders == 1 && c.days >= 30 && c.days <= 540) {
        once.add(c);
      }
    }
    fill(dto, once, "once");

    // 한 시험부만 쓰는 단골. 두 번 이상 왔으니 거래는 검증됐고, 남은 건 시험 종류를 넓히는 것.
    List<Cust> cross = new ArrayList<Cust>();
    for (Cust c : custs) {
      if (c.depts == 1 && c.orders >= 2) {
        cross.add(c);
      }
    }
    fill(dto, cross, "cross");

    // 평소보다 조용해진 고객. 30일 하한이 없으면 며칠 간격으로 상시 의뢰하는 대량 고객이
    // 조금만 밀려도 "몇십 배 늦음"으로 올라와 목록을 덮어버린다.
    List<Cust> quiet = new ArrayList<Cust>();
    for (Cust c : custs) {
      if (c.orders >= 3 && c.avgGap >= 1 && c.days >= 30 && c.days >= 1.5 * c.avgGap) {
        quiet.add(c);
      }
    }
    fill(dto, quiet, "quiet");

    return dto;
  }

  /** 묶인 업체 줄을 고객 하나로 접는다. 이름·담당자는 가장 최근에 의뢰한 줄의 값을 쓴다. */
  static Cust build(List<BaseRow> group, Map<String, String> deptNames, LocalDate today) {
    Cust c = new Cust();
    c.mergedSeqCnt = group.size();

    BaseRow rep = null;
    Set<String> used = new LinkedHashSet<String>();
    for (BaseRow r : group) {
      c.orders += r.getOrders();
      c.netAll += r.getNetAll();
      c.netCur += r.getNetCur();

      LocalDate f = parse(r.getFirstDt());
      LocalDate l = parse(r.getLastDt());
      if (f != null && (c.firstDt == null || f.isBefore(c.firstDt))) {
        c.firstDt = f;
      }
      if (l != null && (c.lastDt == null || l.isAfter(c.lastDt))) {
        c.lastDt = l;
        rep = r;
      }
      if (r.getDeptCodes() != null) {
        for (String code : r.getDeptCodes().split(",")) {
          String t = code.trim();
          if (!t.isEmpty()) {
            used.add(t);
          }
        }
      }
      if (c.bsnsRgnmb == null && CustMergeSupport.bsnsKey(r.getBsnsRgnmb()) != null) {
        c.bsnsRgnmb = r.getBsnsRgnmb().trim();
      }
    }
    if (rep == null) {
      rep = group.get(0);
    }
    c.cmpySeq = rep.getCmpySeq();
    c.cmpyName = rep.getCmpyName();
    c.lastMngName = rep.getLastMngName();

    c.netAllMan = c.netAll / 10000L;
    c.netCurMan = c.netCur / 10000L;
    c.depts = used.size();

    List<String> usedNames = new ArrayList<String>();
    List<String> unusedNames = new ArrayList<String>();
    for (Map.Entry<String, String> e : deptNames.entrySet()) {
      if (used.contains(e.getKey())) {
        usedNames.add(e.getValue());
      } else if (!SKIP_DEPT.equals(e.getKey())) {
        unusedNames.add(e.getValue());
      }
    }
    // 코드 목록에 없는 시험부가 쓰였으면 코드 그대로 보여준다.
    for (String code : used) {
      if (!deptNames.containsKey(code)) {
        usedNames.add(code);
      }
    }
    c.usedDepts = join(usedNames);
    c.unusedDepts = join(unusedNames);

    if (c.lastDt != null) {
      c.days = (int) ChronoUnit.DAYS.between(c.lastDt, today);
      if (c.days < 0) {
        c.days = 0;
      }
    }
    if (c.firstDt != null && c.lastDt != null) {
      long span = ChronoUnit.DAYS.between(c.firstDt, c.lastDt);
      int gaps = c.orders - 1;
      if (gaps < 1) {
        gaps = 1;
      }
      c.avgGap = span / (double) gaps;
    }
    c.overdue = c.avgGap >= 1 ? DeptPnlSupport.round1(c.days / c.avgGap) : 0.0;
    return c;
  }

  /** 의뢰 횟수 구간(ladder=true) 또는 이용 시험부 개수 구간으로 접는다. */
  static List<StepRow> steps(List<Cust> custs, String[] labels, boolean ladder) {
    int n = labels.length;
    int[] cnt = new int[n];
    long[] all = new long[n];
    long[] cur = new long[n];
    for (Cust c : custs) {
      int i = ladder ? ladderIndex(c.orders) : Math.min(Math.max(c.depts, 1), n) - 1;
      cnt[i]++;
      all[i] += c.netAllMan;
      cur[i] += c.netCurMan;
    }
    List<StepRow> rows = new ArrayList<StepRow>();
    long prevPer = 0L;
    for (int i = 0; i < n; i++) {
      StepRow r = new StepRow();
      r.setStep(i + 1);
      r.setLabel(labels[i]);
      r.setCustCnt(cnt[i]);
      r.setNetAllMan(all[i]);
      r.setNetAllEok(SalesBriefSupport.eok(all[i]));
      r.setNetCurMan(cur[i]);
      long per = cnt[i] == 0 ? 0L : all[i] / cnt[i];
      r.setPerCustMan(per);
      r.setStepUpX(prevPer > 0 ? DeptPnlSupport.round1(per / (double) prevPer) : 0.0);
      prevPer = per;
      rows.add(r);
    }
    return rows;
  }

  static int ladderIndex(int orders) {
    if (orders <= 1) {
      return 0;
    }
    if (orders <= 3) {
      return 1;
    }
    if (orders <= 10) {
      return 2;
    }
    if (orders <= 50) {
      return 3;
    }
    return 4;
  }

  /** 목록을 금액 큰 순으로 잘라 DTO 에 담고, 자르기 전 전체 건수·금액도 같이 담는다. */
  static void fill(CustLadderDTO dto, List<Cust> picked, String kind) {
    Collections.sort(picked, new Comparator<Cust>() {
      @Override
      public int compare(Cust a, Cust b) {
        return Long.compare(b.netAllMan, a.netAllMan);
      }
    });
    long sum = 0L;
    for (Cust c : picked) {
      sum += c.netAllMan;
    }
    List<CustRow> rows = new ArrayList<CustRow>();
    for (int i = 0; i < picked.size() && i < LIST_LIMIT; i++) {
      rows.add(picked.get(i).toRow());
    }
    if ("once".equals(kind)) {
      dto.setOnceList(rows);
      dto.setOnceTotalCnt(picked.size());
      dto.setOnceTotalMan(sum);
      dto.setOnceTotalEok(SalesBriefSupport.eok(sum));
    } else if ("cross".equals(kind)) {
      dto.setCrossList(rows);
      dto.setCrossTotalCnt(picked.size());
      dto.setCrossTotalMan(sum);
      dto.setCrossTotalEok(SalesBriefSupport.eok(sum));
    } else {
      dto.setQuietList(rows);
      dto.setQuietTotalCnt(picked.size());
      dto.setQuietTotalMan(sum);
      dto.setQuietTotalEok(SalesBriefSupport.eok(sum));
    }
  }

  static String join(List<String> parts) {
    if (parts.isEmpty()) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (String p : parts) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(p);
    }
    return sb.toString();
  }

  static LocalDate parse(String yyyyMMdd) {
    if (yyyyMMdd == null || yyyyMMdd.length() < 10) {
      return null;
    }
    try {
      return LocalDate.parse(yyyyMMdd.substring(0, 10));
    } catch (RuntimeException e) {
      return null;
    }
  }

  /** 합쳐진 고객 하나. 화면에 나갈 값은 toRow() 로 옮긴다. */
  static class Cust {
    int cmpySeq;
    String cmpyName;
    String bsnsRgnmb;
    String lastMngName;
    int mergedSeqCnt;
    int orders;
    int depts;
    String usedDepts = "";
    String unusedDepts = "";
    long netAll;
    long netCur;
    long netAllMan;
    long netCurMan;
    LocalDate firstDt;
    LocalDate lastDt;
    int days;
    double avgGap;
    double overdue;

    CustRow toRow() {
      CustRow r = new CustRow();
      r.setCmpySeq(cmpySeq);
      r.setCmpyName(cmpyName);
      r.setBsnsRgnmb(bsnsRgnmb == null ? "" : bsnsRgnmb);
      r.setMngName(lastMngName == null ? "-" : lastMngName);
      r.setMergedSeqCnt(mergedSeqCnt);
      r.setOrders(orders);
      r.setDepts(depts);
      r.setUsedDepts(usedDepts);
      r.setUnusedDepts(unusedDepts);
      r.setNetAllMan(netAllMan);
      r.setNetAllEok(SalesBriefSupport.eok(netAllMan));
      r.setFirstDt(firstDt == null ? "" : firstDt.toString());
      r.setLastDt(lastDt == null ? "" : lastDt.toString());
      r.setDays(days);
      r.setAvgGap((int) Math.round(avgGap));
      r.setOverdue(overdue);
      return r;
    }
  }
}
