package egovframework.page.web;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import springfox.documentation.annotations.ApiIgnore;
import egovframework.cmm.service.LoginVO;
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.page.dto.PageLink;
import egovframework.sts.dto.CustLadderDTO;
import egovframework.sts.dto.DeptPnlDTO;
import egovframework.sts.dto.OwnerMixDTO;
import egovframework.sts.dto.SalesBriefDTO;
import egovframework.sts.dto.UnbilledDTO;
import egovframework.sts.service.CustLadderService;
import egovframework.sts.service.DeptPnlService;
import egovframework.sts.service.OwnerMixService;
import egovframework.sts.service.SalesBriefService;
import egovframework.sts.service.UnbilledService;
import egovframework.wrp.dto.StuckGroupDTO;
import egovframework.wrp.dto.StuckItem;
import egovframework.wrp.service.WrpService;

@ApiIgnore
@Controller
@RequestMapping("/page")
public class PageViewController {

  @Resource(name = "WrpService")
  private WrpService wrpService;

  @Resource(name = "SalesBriefService")
  private SalesBriefService salesBriefService;

  @Resource(name = "DeptPnlService")
  private DeptPnlService deptPnlService;

  @Resource(name = "CustLadderService")
  private CustLadderService custLadderService;

  @Resource(name = "OwnerMixService")
  private OwnerMixService ownerMixService;

  @Resource(name = "UnbilledService")
  private UnbilledService unbilledService;

  @GetMapping("/list.do")
  public String list(Model model) {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    if (!isAuthenticated) {
      return "forward:/err/error.do";
    }
    model.addAttribute("pages", pageLinks());
    model.addAttribute("userName", user == null ? "" : user.getEmpName());
    model.addAttribute("today", LocalDate.now(ZoneId.of("Asia/Seoul")).toString());
    return "egovframework/page/list";
  }

  @GetMapping("/sales.do")
  public String sales(Model model) {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    if (!isAuthenticated) {
      return "forward:/err/error.do";
    }
    SalesBriefDTO brief = salesBriefService.getBrief();
    model.addAttribute("brief", brief);
    model.addAttribute("userName", user == null ? "" : user.getEmpName());
    return "egovframework/page/sales";
  }

  @GetMapping("/deptpnl.do")
  public String deptPnl(Model model) {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    if (!isAuthenticated) {
      return "forward:/err/error.do";
    }
    DeptPnlDTO pnl = deptPnlService.getDeptPnl();
    model.addAttribute("pnl", pnl);
    model.addAttribute("userName", user == null ? "" : user.getEmpName());
    return "egovframework/page/deptpnl";
  }

  @GetMapping("/custladder.do")
  public String custLadder(Model model) {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    if (!isAuthenticated) {
      return "forward:/err/error.do";
    }
    CustLadderDTO ladder = custLadderService.getLadder();
    model.addAttribute("ladder", ladder);
    model.addAttribute("userName", user == null ? "" : user.getEmpName());
    return "egovframework/page/custladder";
  }

  @GetMapping("/ownermix.do")
  public String ownerMix(Model model) {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    if (!isAuthenticated) {
      return "forward:/err/error.do";
    }
    OwnerMixDTO mix = ownerMixService.getOwnerMix();
    model.addAttribute("mix", mix);
    model.addAttribute("userName", user == null ? "" : user.getEmpName());
    return "egovframework/page/ownermix";
  }

  @GetMapping("/unbilled.do")
  public String unbilled(
      @RequestParam(value = "minDays", defaultValue = "90") int minDays,
      Model model) {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    if (!isAuthenticated) {
      return "forward:/err/error.do";
    }
    if (minDays < 0) {
      minDays = 0;
    }
    UnbilledDTO ub = unbilledService.getUnbilled(minDays);
    model.addAttribute("ub", ub);
    model.addAttribute("userName", user == null ? "" : user.getEmpName());
    return "egovframework/page/unbilled";
  }

  /** JSP 화면이 늘면 여기만 추가하면 목록에 나온다. */
  static List<PageLink> pageLinks() {
    List<PageLink> pages = new ArrayList<PageLink>();
    pages.add(new PageLink("화면 목록", "이 페이지. 주소 모음", "/page/list.do"));
    pages.add(new PageLink("영업 주간", "미수·미청구", "/page/sales.do"));
    pages.add(new PageLink("시험부 손익", "건당 단가·매출 몫·소요일수", "/page/deptpnl.do"));
    pages.add(new PageLink("고객 성장 단계", "재의뢰·시험부 확대·조용해진 고객", "/page/custladder.do"));
    pages.add(new PageLink("담당자별 포트폴리오", "건당 단가·시험부 구성·담당 이동", "/page/ownermix.do"));
    pages.add(new PageLink("완료 후 매출 미확정", "보고용 상세표·경과일·담당별", "/page/unbilled.do"));
    pages.add(new PageLink("시험 적체", "막힌 일·오늘 명단", "/page/stuck.do?testTypeCode=EM"));
    return pages;
  }

  @GetMapping("/stuck.do")
  public String stuck(
      @RequestParam(value = "testTypeCode", defaultValue = "EM") String testTypeCode,
      Model model) {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    if (!isAuthenticated) {
      return "forward:/err/error.do";
    }

    List<StuckGroupDTO> groups = wrpService.getStuckDashboard(testTypeCode);
    List<StuckItem> todayList = buildTodayList(groups);
    int companyHoldCount = 0;
    for (StuckItem item : todayList) {
      if ("업체사정".equals(item.getCauseType())) {
        companyHoldCount++;
      }
    }
    model.addAttribute("testTypeCode", testTypeCode);
    model.addAttribute("groups", groups);
    model.addAttribute("todayList", todayList);
    model.addAttribute("todayCount", todayList.size());
    model.addAttribute("companyHoldCount", companyHoldCount);
    model.addAttribute("today", LocalDate.now(ZoneId.of("Asia/Seoul")).toString());
    model.addAttribute("userName", user == null ? "" : user.getEmpName());
    return "egovframework/page/stuck";
  }

  static List<StuckItem> buildTodayList(List<StuckGroupDTO> groups) {
    List<StuckItem> list = new ArrayList<StuckItem>();
    if (ObjectUtils.isEmpty(groups)) {
      return list;
    }
    for (StuckGroupDTO group : groups) {
      if (group.getSummary() != null && "TT".equals(group.getSummary().getTestTypeCode())) {
        continue;
      }
      if (!ObjectUtils.isEmpty(group.getHoldingList())) {
        list.addAll(group.getHoldingList());
      }
      if (!ObjectUtils.isEmpty(group.getRdDelayList())) {
        list.addAll(group.getRdDelayList());
      }
    }
    Collections.sort(list, new Comparator<StuckItem>() {
      @Override
      public int compare(StuckItem a, StuckItem b) {
        int d = Integer.compare(b.getStuckDays(), a.getStuckDays());
        if (d != 0) {
          return d;
        }
        return Integer.compare(a.getTestSeq(), b.getTestSeq());
      }
    });
    return list;
  }
}
