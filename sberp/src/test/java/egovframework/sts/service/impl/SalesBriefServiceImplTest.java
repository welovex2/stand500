package egovframework.sts.service.impl;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import egovframework.sts.dto.SalesBriefDTO.GapRow;
import egovframework.sts.dto.SalesBriefDTO.MemberProgress;

public class SalesBriefServiceImplTest {

  @Test
  public void redList_topThreeBehindTarget() {
    List<MemberProgress> members = new ArrayList<MemberProgress>();
    members.add(member("A", 100, 90));
    members.add(member("B", 100, 10));
    members.add(member("C", 100, 40));
    members.add(member("D", 100, 20));
    List<GapRow> red = SalesBriefServiceImpl.buildRedList(members, 1, 15, 30);
    assertEquals(3, red.size());
    assertEquals("B", red.get(0).getMemName());
    assertEquals("D", red.get(1).getMemName());
    assertEquals("C", red.get(2).getMemName());
  }

  private static MemberProgress member(String name, long monthTarget, long actual) {
    MemberProgress m = new MemberProgress();
    m.setMemName(name);
    m.setM1(monthTarget);
    m.setYearActualMan(actual);
    return m;
  }
}
