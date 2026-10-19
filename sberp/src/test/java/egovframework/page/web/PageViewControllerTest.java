package egovframework.page.web;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import egovframework.wrp.dto.StuckGroupDTO;
import egovframework.wrp.dto.StuckItem;
import egovframework.wrp.dto.StuckSummary;

public class PageViewControllerTest {

  @Test
  public void buildTodayList_skipsTotal_andSortsByDays() {
    StuckGroupDTO em = group("EM", item("A", 10), item("B", 20));
    StuckGroupDTO trip = group("EMC", item("C", 8), null);
    StuckGroupDTO total = group("TT", null, null);

    List<StuckGroupDTO> groups = new ArrayList<StuckGroupDTO>();
    groups.add(em);
    groups.add(trip);
    groups.add(total);

    List<StuckItem> list = PageViewController.buildTodayList(groups);
    assertEquals(3, list.size());
    assertEquals("B", list.get(0).getTestId());
    assertEquals("A", list.get(1).getTestId());
    assertEquals("C", list.get(2).getTestId());
  }

  private static StuckGroupDTO group(String code, StuckItem holding, StuckItem rd) {
    StuckGroupDTO g = new StuckGroupDTO();
    StuckSummary s = new StuckSummary();
    s.setTestTypeCode(code);
    g.setSummary(s);
    if (holding != null) {
      g.getHoldingList().add(holding);
    }
    if (rd != null) {
      g.getRdDelayList().add(rd);
    }
    return g;
  }

  private static StuckItem item(String id, int days) {
    StuckItem i = new StuckItem();
    i.setTestId(id);
    i.setStuckDays(days);
    return i;
  }
}
