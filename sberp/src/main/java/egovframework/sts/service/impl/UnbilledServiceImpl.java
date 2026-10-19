package egovframework.sts.service.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
import egovframework.sts.dto.UnbilledDTO;
import egovframework.sts.dto.UnbilledDTO.BucketRow;
import egovframework.sts.dto.UnbilledDTO.GroupRow;
import egovframework.sts.dto.UnbilledDTO.ItemRow;
import egovframework.sts.dto.UnbilledDTO.QuoRow;
import egovframework.sts.service.UnbilledMapper;
import egovframework.sts.service.UnbilledService;
import egovframework.sts.util.DeptPnlSupport;
import egovframework.sts.util.SalesBriefSupport;

@Service("UnbilledService")
public class UnbilledServiceImpl implements UnbilledService {

  private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

  /** 경과일 구간 경계. 마지막 칸은 그 위 전부. */
  private static final int[] BUCKET_EDGE = {30, 60, 90, 180, 365};

  private static final String[] BUCKET_LABEL =
      {"30일 미만", "30~59일", "60~89일", "90~179일", "180~364일", "365일 이상"};

  /** 고객별 표에 실을 줄 수. */
  private static final int CUST_LIMIT = 20;

  /**
   * 견적 리스트 화면. 프론트는 웹 루트에 있고 이 화면은 /api 아래에 있어서
   * JSP 에서 c:url 로 만들면 컨텍스트(/api)가 앞에 붙어 주소가 깨진다.
   * nginx 도 개발·운영 모두 / 는 프론트로, /api/ 는 톰캣으로 보내므로 루트 기준 절대경로를 쓴다.
   */
  private static final String QUO_LIST_PATH = "/html/estimate_list.html";

  /** 견적서번호 검색 코드. */
  private static final String CODE_QUO_ID = "5";

  /** 작성일 구간 검색 코드. */
  private static final String CODE_WRITE_DATE = "15";

  /**
   * 작성일 구간의 시작. 사실상 제한을 두지 않는다.
   * 견적 리스트는 date 파라미터를 안 주면 작성일 최근 90일을 강제로 걸기 때문에,
   * 날짜를 넘기지 않으면 2년 전에 끝난 미청구 건을 눌러도 목록이 비어 보인다.
   */
  private static final String LIST_FROM_DATE = "2000-01-01";

  /**
   * 이 연도부터만 집계한다. 2023년은 ERP 를 막 쓰기 시작한 때라
   * 취소·무상 처리 뒤 상태만 안 바뀐 건이 섞여 있어서 미확정으로 읽으면 잘못된 보고가 된다.
   */
  private static final int FROM_YEAR = 2024;

  @Autowired
  UnbilledMapper unbilledMapper;

  @Override
  public UnbilledDTO getUnbilled(int minDays) {
    LocalDate today = LocalDate.now(SEOUL);

    UnbilledDTO dto = new UnbilledDTO();
    dto.setAsOfDate(today.toString());
    dto.setMinDays(minDays);

    List<ItemRow> items = unbilledMapper.selectUnbilled();
    if (ObjectUtils.isEmpty(items)) {
      return dto;
    }

    // 시험 줄을 견적으로 접는다. 경과일은 그 견적의 마지막 완료일 기준이다.
    Map<String, Quo> quos = new LinkedHashMap<String, Quo>();
    for (ItemRow it : items) {
      String key = it.getQuoYm() + "-" + it.getQuoSeq();
      Quo q = quos.get(key);
      if (q == null) {
        q = new Quo();
        q.quoId = it.getQuoId();
        q.chq = it.getChqSeq() != 0;
        q.cmpySeq = it.getCmpySeq();
        q.cmpyName = it.getCmpyName();
        q.mngName = it.getMngName();
        q.mngQuit = "3".equals(it.getMngWorkState());
        q.prdctName = it.getPrdctName();
        q.issueDt = it.getIssueDt();
        q.vat = it.getQuoTotalVat();
        q.doneCnt = it.getQuoDoneCnt();
        q.cancelCnt = it.getQuoCancelCnt();
        q.skipCnt = it.getQuoSkipCnt();
        q.noReportCnt = it.getQuoNoReportCnt();
        q.hasSls = it.getHasSls() == 1;
        quos.put(key, q);
      }
      q.testCnt++;
      q.net += it.getNetSales();
      q.deptNames.add(it.getDeptName());
      LocalDate d = parse(it.getDoneDt());
      if (d != null && (q.doneDt == null || d.isAfter(q.doneDt))) {
        q.doneDt = d;
      }
    }

    // 매출확정까지 간 건은 매출 리스트에서 조회되므로 뺀다. 여기서 볼 것은
    // 프로젝트가 끝났는데 매출확정조차 안 된 건이다.
    List<Quo> list = new ArrayList<Quo>();
    Set<String> keptKeys = new LinkedHashSet<String>();
    int excludedCnt = 0;
    long excludedNet = 0L;
    int oldCnt = 0;
    long oldNet = 0L;
    for (Map.Entry<String, Quo> e : quos.entrySet()) {
      Quo q = e.getValue();
      if (q.hasSls) {
        excludedCnt++;
        excludedNet += q.net / 10000L;
        continue;
      }
      if (q.doneDt == null || q.doneDt.getYear() < FROM_YEAR) {
        oldCnt++;
        oldNet += q.net / 10000L;
        continue;
      }
      list.add(q);
      keptKeys.add(e.getKey());
    }
    dto.setFromYear(FROM_YEAR);
    dto.setExcludedCnt(excludedCnt);
    dto.setExcludedMan(excludedNet);
    dto.setExcludedEok(SalesBriefSupport.eok(excludedNet));
    dto.setOldCnt(oldCnt);
    dto.setOldMan(oldNet);
    dto.setOldEok(SalesBriefSupport.eok(oldNet));
    if (list.isEmpty()) {
      return dto;
    }

    // 시험부 집계는 시험 줄에서 세므로 같은 기준으로 걸러 둔다.
    List<ItemRow> kept = new ArrayList<ItemRow>();
    for (ItemRow it : items) {
      if (keptKeys.contains(it.getQuoYm() + "-" + it.getQuoSeq())) {
        kept.add(it);
      }
    }

    for (Quo q : list) {
      q.listUrl = listUrl(q.quoId, today);
      q.netMan = q.net / 10000L;
      q.vatMan = q.vat / 10000L;
      if (q.doneDt != null) {
        q.days = (int) ChronoUnit.DAYS.between(q.doneDt, today);
        if (q.days < 0) {
          q.days = 0;
        }
      }
    }

    long netMan = 0L;
    long vatMan = 0L;
    int testCnt = 0;
    for (Quo q : list) {
      netMan += q.netMan;
      vatMan += q.vatMan;
      testCnt += q.testCnt;
    }
    dto.setQuoCnt(list.size());
    dto.setTestCnt(testCnt);
    dto.setNetMan(netMan);
    dto.setNetEok(SalesBriefSupport.eok(netMan));
    dto.setVatMan(vatMan);
    dto.setVatEok(SalesBriefSupport.eok(vatMan));

    int d90Cnt = 0;
    long d90Man = 0L;
    int d365Cnt = 0;
    long d365Man = 0L;
    for (Quo q : list) {
      if (q.days >= 90) {
        d90Cnt++;
        d90Man += q.netMan;
      }
      if (q.days >= 365) {
        d365Cnt++;
        d365Man += q.netMan;
      }
    }
    dto.setD90Cnt(d90Cnt);
    dto.setD90Man(d90Man);
    dto.setD90Eok(SalesBriefSupport.eok(d90Man));
    dto.setD365Cnt(d365Cnt);
    dto.setD365Man(d365Man);
    dto.setD365Eok(SalesBriefSupport.eok(d365Man));

    int checkCnt = 0;
    long checkMan = 0L;
    for (Quo q : list) {
      if (q.stateWarn()) {
        checkCnt++;
        checkMan += q.netMan;
      }
    }
    dto.setCheckCnt(checkCnt);
    dto.setCheckMan(checkMan);
    dto.setCheckEok(SalesBriefSupport.eok(checkMan));

    dto.setBuckets(buckets(list, netMan));
    dto.setYears(years(list, netMan));
    dto.setDepts(depts(kept, netMan));
    dto.setOwners(group(list, netMan, true));
    List<GroupRow> custs = group(list, netMan, false);
    if (custs.size() > CUST_LIMIT) {
      custs = new ArrayList<GroupRow>(custs.subList(0, CUST_LIMIT));
    }
    dto.setCusts(custs);

    // 명단. 경과일 긴 순, 같으면 금액 큰 순.
    List<Quo> picked = new ArrayList<Quo>();
    for (Quo q : list) {
      if (q.days >= minDays) {
        picked.add(q);
      }
    }
    Collections.sort(picked, new Comparator<Quo>() {
      @Override
      public int compare(Quo a, Quo b) {
        int d = Integer.compare(b.days, a.days);
        return d != 0 ? d : Long.compare(b.netMan, a.netMan);
      }
    });
    long pickedMan = 0L;
    List<QuoRow> rows = new ArrayList<QuoRow>();
    for (Quo q : picked) {
      pickedMan += q.netMan;
      rows.add(q.toRow());
    }
    dto.setList(rows);
    dto.setListQuoCnt(picked.size());
    dto.setListNetMan(pickedMan);
    dto.setListNetEok(SalesBriefSupport.eok(pickedMan));

    return dto;
  }

  static List<BucketRow> buckets(List<Quo> list, long totalMan) {
    int n = BUCKET_LABEL.length;
    BucketRow[] arr = new BucketRow[n];
    for (int i = 0; i < n; i++) {
      arr[i] = new BucketRow();
      arr[i].setLabel(BUCKET_LABEL[i]);
    }
    for (Quo q : list) {
      add(arr[bucketIndex(q.days)], q);
    }
    return finish(arr, totalMan);
  }

  static int bucketIndex(int days) {
    for (int i = 0; i < BUCKET_EDGE.length; i++) {
      if (days < BUCKET_EDGE[i]) {
        return i;
      }
    }
    return BUCKET_EDGE.length;
  }

  static List<BucketRow> years(List<Quo> list, long totalMan) {
    Map<String, BucketRow> byYear = new LinkedHashMap<String, BucketRow>();
    List<String> keys = new ArrayList<String>();
    for (Quo q : list) {
      String y = q.doneDt == null ? "-" : String.valueOf(q.doneDt.getYear());
      BucketRow r = byYear.get(y);
      if (r == null) {
        r = new BucketRow();
        r.setLabel(y + "년 완료");
        byYear.put(y, r);
        keys.add(y);
      }
      add(r, q);
    }
    Collections.sort(keys);
    BucketRow[] arr = new BucketRow[keys.size()];
    for (int i = 0; i < keys.size(); i++) {
      arr[i] = byYear.get(keys.get(i));
    }
    return finish(arr, totalMan);
  }

  /** 시험부별은 시험 줄에서 바로 센다. 한 견적이 여러 시험부에 걸치기 때문이다. */
  static List<GroupRow> depts(List<ItemRow> items, long totalMan) {
    Map<String, GroupRow> map = new LinkedHashMap<String, GroupRow>();
    Map<String, Set<String>> quoKeys = new LinkedHashMap<String, Set<String>>();
    for (ItemRow it : items) {
      String name = it.getDeptName();
      GroupRow r = map.get(name);
      if (r == null) {
        r = new GroupRow();
        r.setName(name);
        map.put(name, r);
        quoKeys.put(name, new LinkedHashSet<String>());
      }
      r.setTestCnt(r.getTestCnt() + 1);
      r.setNetMan(r.getNetMan() + it.getNetSales() / 10000L);
      quoKeys.get(name).add(it.getQuoYm() + "-" + it.getQuoSeq());
    }
    List<GroupRow> out = new ArrayList<GroupRow>(map.values());
    for (GroupRow r : out) {
      r.setQuoCnt(quoKeys.get(r.getName()).size());
      r.setNetEok(SalesBriefSupport.eok(r.getNetMan()));
      r.setSharePct(DeptPnlSupport.pct(r.getNetMan(), totalMan));
    }
    sortByNet(out);
    return out;
  }

  /** 담당자별(owner=true) 또는 고객별 집계. 견적 단위. */
  static List<GroupRow> group(List<Quo> list, long totalMan, boolean owner) {
    Map<String, GroupRow> map = new LinkedHashMap<String, GroupRow>();
    for (Quo q : list) {
      String name = owner ? q.mngName : q.cmpyName;
      if (name == null || name.isEmpty()) {
        name = "-";
      }
      GroupRow r = map.get(name);
      if (r == null) {
        r = new GroupRow();
        r.setName(name);
        map.put(name, r);
      }
      r.setQuoCnt(r.getQuoCnt() + 1);
      r.setTestCnt(r.getTestCnt() + q.testCnt);
      r.setNetMan(r.getNetMan() + q.netMan);
      if (q.days > r.getMaxDays()) {
        r.setMaxDays(q.days);
      }
      if (owner && q.mngQuit) {
        r.setQuit(true);
      }
    }
    List<GroupRow> out = new ArrayList<GroupRow>(map.values());
    for (GroupRow r : out) {
      r.setNetEok(SalesBriefSupport.eok(r.getNetMan()));
      r.setSharePct(DeptPnlSupport.pct(r.getNetMan(), totalMan));
    }
    sortByNet(out);
    return out;
  }

  static void sortByNet(List<GroupRow> rows) {
    Collections.sort(rows, new Comparator<GroupRow>() {
      @Override
      public int compare(GroupRow a, GroupRow b) {
        return Long.compare(b.getNetMan(), a.getNetMan());
      }
    });
  }

  static void add(BucketRow r, Quo q) {
    r.setQuoCnt(r.getQuoCnt() + 1);
    r.setTestCnt(r.getTestCnt() + q.testCnt);
    r.setNetMan(r.getNetMan() + q.netMan);
    r.setVatMan(r.getVatMan() + q.vatMan);
  }

  static List<BucketRow> finish(BucketRow[] arr, long totalMan) {
    List<BucketRow> out = new ArrayList<BucketRow>();
    for (BucketRow r : arr) {
      r.setNetEok(SalesBriefSupport.eok(r.getNetMan()));
      r.setSharePct(DeptPnlSupport.pct(r.getNetMan(), totalMan));
      out.add(r);
    }
    return out;
  }

  /** 견적 리스트에서 그 견적 한 건만 걸러 보여주는 주소를 만든다. */
  static String listUrl(String quoId, LocalDate today) {
    if (quoId == null || quoId.isEmpty()) {
      return "";
    }
    return QUO_LIST_PATH
        + "?code=" + CODE_QUO_ID
        + "&word=" + enc(quoId)
        + "&date=" + CODE_WRITE_DATE
        + "&start=" + LIST_FROM_DATE
        + "&end=" + today.toString();
  }

  static String enc(String v) {
    try {
      return URLEncoder.encode(v, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return v;
    }
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

  /** 견적 하나로 접은 것. */
  static class Quo {
    String quoId;
    String listUrl;
    boolean chq;
    int cmpySeq;
    String cmpyName;
    String mngName;
    boolean mngQuit;
    String prdctName;
    String issueDt;
    LocalDate doneDt;
    Set<String> deptNames = new LinkedHashSet<String>();
    int testCnt;
    long net;
    long vat;
    long netMan;
    long vatMan;
    int days;
    int doneCnt;
    int cancelCnt;
    int skipCnt;
    int noReportCnt;
    boolean hasSls;

    /** 상태 구성이 매끈하지 않으면 눈으로 확인해야 한다. */
    boolean stateWarn() {
      return noReportCnt > 0 || cancelCnt > 0 || skipCnt > 0;
    }

    /** "완료 2 · 취소 1 · 성적서 없음 1" 처럼 그 견적의 시험 상태를 한 칸에 적는다. */
    String stateText() {
      StringBuilder sb = new StringBuilder();
      sb.append("완료 ").append(doneCnt);
      if (cancelCnt > 0) {
        sb.append(" · 취소 ").append(cancelCnt);
      }
      if (skipCnt > 0) {
        sb.append(" · 미필요 ").append(skipCnt);
      }
      if (noReportCnt > 0) {
        sb.append(" · 성적서 없음 ").append(noReportCnt);
      }
      return sb.toString();
    }

    QuoRow toRow() {
      QuoRow r = new QuoRow();
      r.setQuoId(quoId);
      r.setListUrl(listUrl);
      r.setChq(chq);
      r.setCmpySeq(cmpySeq);
      r.setCmpyName(cmpyName);
      r.setMngName(mngName);
      r.setMngQuit(mngQuit);
      r.setPrdctName(prdctName);
      StringBuilder sb = new StringBuilder();
      for (String d : deptNames) {
        if (sb.length() > 0) {
          sb.append(", ");
        }
        sb.append(d);
      }
      r.setDeptNames(sb.toString());
      r.setTestCnt(testCnt);
      r.setTestStateText(stateText());
      r.setNoReportCnt(noReportCnt);
      r.setCancelCnt(cancelCnt);
      r.setStateWarn(stateWarn());
      r.setNetMan(netMan);
      r.setVatMan(vatMan);
      r.setIssueDt(issueDt);
      r.setDoneDt(doneDt == null ? "" : doneDt.toString());
      r.setDays(days);
      return r;
    }
  }
}
