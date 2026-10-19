package egovframework.wrp.util;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class HoldingCauseSupportTest {

  @Test
  public void classify_external_byKeyword() {
    assertEquals("업체사정", HoldingCauseSupport.classify("홀딩", "업체 요청으로 인한 홀딩"));
    assertEquals("업체사정", HoldingCauseSupport.classify("홀딩", "시험취하 결정되었으나 업체에서 취하공문 안주어서"));
    assertEquals("업체사정", HoldingCauseSupport.classify("홀딩", "환불비용은 50%로 구두 협의됨"));
  }

  @Test
  public void classify_holding_withoutKeyword_isUnknown() {
    assertEquals("확인필요", HoldingCauseSupport.classify("홀딩", "일정 재검토"));
    assertEquals("확인필요", HoldingCauseSupport.classify("홀딩", ""));
  }

  @Test
  public void classify_notHolding_isEmpty() {
    assertEquals("", HoldingCauseSupport.classify("RD지연", "업체 요청"));
  }
}
