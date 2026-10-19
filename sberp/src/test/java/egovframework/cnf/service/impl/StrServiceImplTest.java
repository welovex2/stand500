package egovframework.cnf.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import egovframework.cmm.service.ResponseMessage;
import egovframework.cnf.dto.StoreDetailDTO;
import egovframework.cnf.dto.ZoneRangeReq;
import egovframework.cnf.dto.ZoneRangeResult;
import egovframework.cnf.service.StrMapper;
import egovframework.cnf.service.Warehouse;
import egovframework.cnf.service.Zone;
import egovframework.sam.service.SamBizException;

public class StrServiceImplTest {

  private StrServiceImpl service;
  private FakeStrMapper mapper;

  @Before
  public void setUp() throws Exception {
    service = new StrServiceImpl();
    mapper = new FakeStrMapper();
    java.lang.reflect.Field field = StrServiceImpl.class.getDeclaredField("strMapper");
    field.setAccessible(true);
    field.set(service, mapper);
  }

  /** 관리자는 선택이다. 없이도 등록된다. (2026-10-16) */
  @Test
  public void insert_allowsMissingManager() {
    Warehouse req = new Warehouse();
    req.setWhName("3층 창고");
    req.setTypeCode("01");
    mapper.typeCodeCnt = 1;

    assertTrue(service.insert(req));
    assertNull(mapper.inserted.getMngId());
  }

  /** 부서만 골라도 저장된다. */
  @Test
  public void insert_allowsDeptOnly() {
    Warehouse req = new Warehouse();
    req.setWhName("부서만 창고");
    req.setTypeCode("01");
    req.setDeptSeq(Integer.valueOf(3));
    mapper.typeCodeCnt = 1;

    assertTrue(service.insert(req));
    assertNull(mapper.inserted.getMngId());
    assertEquals(Integer.valueOf(3), mapper.inserted.getDeptSeq());
  }

  /** 관리자도 부서도 없이 저장된다. (2026-10-16 결정) */
  @Test
  public void insert_allowsNeitherManagerNorDept() {
    Warehouse req = new Warehouse();
    req.setWhName("주체없음 창고");
    req.setTypeCode("01");
    mapper.typeCodeCnt = 1;

    assertTrue(service.insert(req));
    assertNull(mapper.inserted.getMngId());
    assertNull(mapper.inserted.getDeptSeq());
  }

  @Test
  public void insert_rejectsUnknownType() {
    Warehouse req = new Warehouse();
    req.setWhName("3층 창고");
    req.setMngId("hong");
    req.setTypeCode("99");
    mapper.typeCodeCnt = 0;
    try {
      service.insert(req);
      fail("expected type check");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.CHECK_WH_TYPE, e.getMessage());
    }
  }

  @Test
  public void insert_assignsSeq() {
    Warehouse req = new Warehouse();
    req.setWhName("3층 창고");
    req.setMngId("hong");
    req.setTypeCode("01");
    mapper.typeCodeCnt = 1;
    mapper.nextStrSeq = 3;

    assertTrue(service.insert(req));
    assertEquals(3, req.getStrSeq());
  }

  /* ===================== 창고 사용 중지 ===================== */

  @Test
  public void stop_rejectsMissingWarehouse() {
    mapper.existing = null;
    Warehouse req = new Warehouse();
    req.setStrSeq(99);
    try {
      service.stop(req);
      fail("expected no data");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.NO_DATA, e.getMessage());
    }
  }

  @Test
  public void stop_rejectsAlreadyStopped() {
    Warehouse exists = new Warehouse();
    exists.setStrSeq(1);
    exists.setState("D");
    mapper.existing = exists;

    Warehouse req = new Warehouse();
    req.setStrSeq(1);
    try {
      service.stop(req);
      fail("expected already stopped");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.WH_ALREADY_STOPPED, e.getMessage());
    }
  }

  /**
   * 창고를 중지해도 구역은 그대로 둔다.
   *
   * <p>구역 중지는 「마지막 남은 구역은 중지할 수 없다」는 규칙을 가진 별도 절차다.
   * 창고 중지가 구역을 함께 내리면 그 규칙을 우회하게 된다. (2026-10-15 결정)
   */
  @Test
  public void stop_doesNotTouchZones() {
    Warehouse exists = new Warehouse();
    exists.setStrSeq(1);
    exists.setState("I");
    mapper.existing = exists;
    mapper.zones.add(zone(10, "가-01", "01", "I"));
    mapper.zones.add(zone(11, "가-02", "01", "I"));

    Warehouse req = new Warehouse();
    req.setStrSeq(1);
    req.setUdtMemId("hong");

    assertTrue(service.stop(req));
    assertTrue(mapper.stoppedZoneSeqs.isEmpty());
  }

  @Test
  public void stop_succeedsWhenNoZones() {
    Warehouse exists = new Warehouse();
    exists.setStrSeq(1);
    exists.setState("I");
    mapper.existing = exists;

    Warehouse req = new Warehouse();
    req.setStrSeq(1);
    req.setUdtMemId("hong");

    assertTrue(service.stop(req));
    assertTrue(mapper.stoppedZoneSeqs.isEmpty());
  }

  /**
   * 확인과 UPDATE 사이에 다른 사용자가 먼저 중지해 0행이 나오면 예외여야 한다.
   *
   * <p>예전에는 false 를 돌려줬다. 그러면 화면에 띄울 말이 없고, 앞서 중지한 구역만
   * 커밋된 채 남아 「살아 있는데 구역이 0개인 창고」가 생겼다.
   */
  @Test
  public void stop_throwsWhenWarehouseRowNotUpdated() {
    Warehouse exists = new Warehouse();
    exists.setStrSeq(1);
    exists.setState("I");
    mapper.existing = exists;
    mapper.zones.add(zone(10, "가-01", "01", "I"));
    mapper.stopResult = 0;

    Warehouse req = new Warehouse();
    req.setStrSeq(1);
    req.setUdtMemId("hong");
    try {
      service.stop(req);
      fail("expected already stopped");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.WH_ALREADY_STOPPED, e.getMessage());
    }
  }

  /** 거절 메시지에 창고명과 건수가 들어가야 화면이 그대로 띄울 수 있다. */
  @Test
  public void stop_messageNamesWarehouseAndItemCount() {
    Warehouse exists = new Warehouse();
    exists.setStrSeq(2);
    exists.setWhName("2창고");
    exists.setState("I");
    mapper.existing = exists;
    mapper.itemCntByWh = 3;

    Warehouse req = new Warehouse();
    req.setStrSeq(2);
    try {
      service.stop(req);
      fail("expected items remain");
    } catch (SamBizException e) {
      assertTrue(e.getMessage().contains("2창고"));
      assertTrue(e.getMessage().contains("3건"));
      assertTrue(e.getMessage().contains("중지할 수 있습니다"));
    }
  }

  /* ===================== 창고 상세 ===================== */

  @Test
  public void detail_returnsNullWhenWarehouseMissing() {
    mapper.detailRow = null;
    assertNull(service.detail(99));
  }

  /**
   * 상세는 매퍼가 담아 준 항목을 하나도 흘리지 않아야 한다.
   *
   * <p>예전에는 서비스가 Warehouse 를 StoreDetailDTO 로 손수 옮겼고, 그때 등록일 · 수정일 ·
   * 수정자가 빠져 목록 응답과 달랐다. 손 복사를 없앴으니 다시 끼워 넣으면 여기서 깨진다.
   */
  @Test
  public void detail_keepsEveryFieldFromMapper() {
    StoreDetailDTO row = detailRow();
    mapper.detailRow = row;

    StoreDetailDTO dto = service.detail(1);
    assertEquals(1, dto.getStrSeq());
    assertEquals("1창고", dto.getWhName());
    assertEquals("본관 1층", dto.getWhLoc());
    assertEquals("방", dto.getTypeName());
    assertEquals("박형태", dto.getMngName());
    assertEquals("전자파", dto.getMngDeptName());
    assertEquals(5, dto.getZoneCnt());
    assertEquals(1, dto.getStopZoneCnt());
    assertEquals(45, dto.getItemCnt());
    assertEquals("I", dto.getState());
    // 화면의 「마지막 수정 이준우 · 2026-10-01」 과 사용 중지 배지가 쓰는 항목
    assertEquals("2026-09-20", dto.getInsDt());
    assertEquals("2026-10-01", dto.getUdtDt());
    assertEquals("이준우", dto.getUdtMemName());
  }

  /** 사용 중지된 창고는 state=D 로 내려가야 화면이 배지를 띄운다. */
  @Test
  public void detail_exposesStoppedStateAndDate() {
    StoreDetailDTO row = detailRow();
    row.setState("D");
    row.setUdtDt("2026-06-30");
    mapper.detailRow = row;

    StoreDetailDTO dto = service.detail(1);
    assertEquals("D", dto.getState());
    assertEquals("2026-06-30", dto.getUdtDt());
  }

  private StoreDetailDTO detailRow() {
    StoreDetailDTO row = new StoreDetailDTO();
    row.setStrSeq(1);
    row.setWhName("1창고");
    row.setWhLoc("본관 1층");
    row.setTypeCode("01");
    row.setTypeName("방");
    row.setMngId("hong");
    row.setMngName("박형태");
    row.setMngDeptName("전자파");
    row.setZoneCnt(5);
    row.setStopZoneCnt(1);
    row.setItemCnt(45);
    row.setInsDt("2026-09-20");
    row.setUdtDt("2026-10-01");
    row.setUdtMemName("이준우");
    row.setState("I");
    return row;
  }

  @Test
  public void detail_fillsWhNameAndUseNameOnEveryZone() {
    mapper.detailRow = detailRow();
    mapper.zones.add(zone(10, "가-01", "01", "I"));
    mapper.zones.add(zone(11, "가-02", "02", "I"));
    mapper.zones.add(zone(12, "가-03", "03", "I"));

    List<Zone> zones = service.detail(1).getZones();
    assertEquals(3, zones.size());
    for (Zone z : zones) {
      assertEquals("1창고", z.getWhName());
    }
    assertEquals("일반", zones.get(0).getUseName());
    assertEquals("인수요청", zones.get(1).getUseName());
    assertEquals("장기", zones.get(2).getUseName());
  }

  @Test
  public void detail_keepsStoppedZonesInList() {
    mapper.detailRow = detailRow();
    mapper.zones.add(zone(10, "가-01", "01", "I"));
    mapper.zones.add(zone(11, "가-02", "01", "D"));

    List<Zone> zones = service.detail(1).getZones();
    assertEquals(2, zones.size());
    assertEquals("D", zones.get(1).getState());
  }

  @Test
  public void detail_opensStoppedWarehouse() {
    StoreDetailDTO row = detailRow();
    row.setState("D");
    mapper.detailRow = row;

    assertEquals("D", service.detail(1).getState());
  }

  @Test
  public void detail_returnsEmptyZoneListWhenNoZones() {
    mapper.detailRow = detailRow();

    assertTrue(service.detail(1).getZones().isEmpty());
  }

  /**
   * 용도명은 서비스가 다시 채운다. 매퍼의 CASE 가 넣어 준 값은 버려진다.
   * 두 곳에 같은 규칙이 있다는 뜻이라, 한쪽만 고치면 어긋난다.
   */
  @Test
  public void detail_serviceOverwritesUseNameFromMapper() {
    mapper.detailRow = detailRow();
    Zone z = zone(10, "가-01", "02", "I");
    z.setUseName("매퍼가 넣은 값");
    mapper.zones.add(z);

    assertEquals("인수요청", service.detail(1).getZones().get(0).getUseName());
  }

  private Zone zone(int zoneSeq, String zoneName, String useCode, String state) {
    Zone z = new Zone();
    z.setZoneSeq(zoneSeq);
    z.setStrSeq(1);
    z.setZoneName(zoneName);
    z.setUseCode(useCode);
    z.setState(state);
    return z;
  }

  @Test
  public void stop_blockedWhenItemsRemain() {
    Warehouse req = new Warehouse();
    req.setStrSeq(1);
    mapper.existing = new Warehouse();
    mapper.existing.setStrSeq(1);
    mapper.existing.setState("I");
    mapper.itemCntByWh = 4;
    try {
      service.stop(req);
      fail("expected item guard");
    } catch (SamBizException e) {
      assertTrue(e.getMessage().contains("4건"));
      assertTrue(e.getMessage().contains("중지"));
    }
  }

  @Test
  public void insert_rejectsDuplicateName() {
    Warehouse req = new Warehouse();
    req.setWhName("1창고");
    req.setMngId("hong");
    req.setTypeCode("01");
    mapper.typeCodeCnt = 1;
    mapper.whNameCnt = 1;
    try {
      service.insert(req);
      fail("expected duplicate name");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.DUPLICATE_WH_NAME, e.getMessage());
    }
  }

  /** 등록 본문에 남의 strSeq 를 실어도 창고명 중복검사를 빠져나가지 못한다. */
  @Test
  public void insert_ignoresClientStrSeqWhenCheckingDuplicateName() {
    mapper.takenWhNames.put("1창고", Integer.valueOf(7));
    Warehouse req = new Warehouse();
    req.setWhName("1창고");
    req.setMngId("hong");
    req.setTypeCode("01");
    req.setStrSeq(7); // 클라이언트가 실어 보낸 값
    mapper.typeCodeCnt = 1;
    try {
      service.insert(req);
      fail("expected duplicate name");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.DUPLICATE_WH_NAME, e.getMessage());
    }
  }

  /** 반대편 가드. 수정은 자기 이름을 그대로 두는 것이 중복이 아니다. */
  @Test
  public void update_allowsKeepingOwnName() {
    mapper.takenWhNames.put("1창고", Integer.valueOf(7));
    Warehouse exists = new Warehouse();
    exists.setStrSeq(7);
    exists.setState("I");
    mapper.existing = exists;

    Warehouse req = new Warehouse();
    req.setStrSeq(7);
    req.setWhName("1창고");
    req.setMngId("hong");
    req.setTypeCode("01");
    mapper.typeCodeCnt = 1;

    assertTrue(service.update(req));
  }

  /** 수정으로 남의 이름을 가져가는 것은 막는다. */
  @Test
  public void update_rejectsOtherWarehouseName() {
    mapper.takenWhNames.put("1창고", Integer.valueOf(7));
    Warehouse exists = new Warehouse();
    exists.setStrSeq(9);
    exists.setState("I");
    mapper.existing = exists;

    Warehouse req = new Warehouse();
    req.setStrSeq(9);
    req.setWhName("1창고");
    req.setMngId("hong");
    req.setTypeCode("01");
    mapper.typeCodeCnt = 1;
    try {
      service.update(req);
      fail("expected duplicate name");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.DUPLICATE_WH_NAME, e.getMessage());
    }
  }

  @Test
  public void update_rejectsMissingWarehouse() {
    mapper.existing = null;
    Warehouse req = new Warehouse();
    req.setStrSeq(99);
    req.setWhName("1창고");
    req.setMngId("hong");
    try {
      service.update(req);
      fail("expected no data");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.NO_DATA, e.getMessage());
    }
  }

  /** 수정에서도 관리자를 비울 수 있다. */
  @Test
  public void update_allowsClearingManager() {
    Warehouse stored = active(1);
    stored.setWhName("1창고");
    stored.setMngId("lee");
    stored.setTypeCode("01");
    mapper.existing = stored;
    mapper.typeCodeCnt = 1;

    Warehouse req = new Warehouse();
    req.setStrSeq(1);
    req.setMngId("");
    assertTrue(service.update(req));
    assertNull(mapper.updated.getMngId());
  }

  /** 담당 부서만 보내면 부서만 바뀌고 나머지는 그대로다. */
  @Test
  public void update_keepsOthersWhenOnlyDeptSent() {
    Warehouse stored = active(1);
    stored.setWhName("1창고");
    stored.setWhLoc("본관 1층");
    stored.setMngId("lee");
    stored.setTypeCode("02");
    mapper.existing = stored;
    mapper.typeCodeCnt = 1;

    Warehouse req = new Warehouse();
    req.setStrSeq(1);
    req.setDeptSeq(Integer.valueOf(5));
    assertTrue(service.update(req));
    assertEquals(Integer.valueOf(5), mapper.updated.getDeptSeq());
    assertEquals("lee", mapper.updated.getMngId());
    assertEquals("본관 1층", mapper.updated.getWhLoc());
    assertEquals("02", mapper.updated.getTypeCode());
  }

  @Test
  public void update_rejectsUnknownType() {
    mapper.existing = active(1);
    Warehouse req = new Warehouse();
    req.setStrSeq(1);
    req.setWhName("1창고");
    req.setMngId("hong");
    req.setTypeCode("99");
    mapper.typeCodeCnt = 0;
    try {
      service.update(req);
      fail("expected type check");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.CHECK_WH_TYPE, e.getMessage());
    }
  }

  @Test
  public void update_trimsWhitespace() {
    mapper.existing = active(1);
    mapper.typeCodeCnt = 1;
    Warehouse req = new Warehouse();
    req.setStrSeq(1);
    req.setWhName("  1창고  ");
    req.setWhLoc("   ");
    req.setMngId(" hong ");
    req.setTypeCode("01");

    assertTrue(service.update(req));
    assertEquals("1창고", mapper.updated.getWhName());
    assertNull(mapper.updated.getWhLoc());
    assertEquals("hong", mapper.updated.getMngId());
  }

  /**
   * 본문에 없는 항목은 저장된 값을 지키고, UPDATE 로 그대로 다시 넘어가야 한다.
   *
   * <p>예전에는 조건 없이 덮어써서 화면이 창고명과 관리자만 보내면 위치 · 메모가 지워지고
   * 형태가 01(방) 로 되돌아갔다. 구역 수정과 같은 부분 수정 규칙으로 맞췄다.
   */
  @Test
  public void update_keepsOmittedFields() {
    Warehouse stored = active(1);
    stored.setWhName("1창고");
    stored.setWhLoc("본관 1층 시료실");
    stored.setTypeCode("02");
    stored.setMngId("lee");
    stored.setDeptSeq(Integer.valueOf(3));
    mapper.existing = stored;
    mapper.typeCodeCnt = 1;

    Warehouse req = new Warehouse();
    req.setStrSeq(1);
    req.setWhName("1창고");
    req.setMngId("hong");

    assertTrue(service.update(req));
    assertEquals("본관 1층 시료실", mapper.updated.getWhLoc());
    assertEquals("02", mapper.updated.getTypeCode());
    assertEquals(Integer.valueOf(3), mapper.updated.getDeptSeq());
    assertEquals("hong", mapper.updated.getMngId());
  }

  /** 위치는 빈 문자열로 지울 수 있어야 한다. */
  @Test
  public void update_clearsFieldWhenEmptyStringSent() {
    Warehouse stored = active(1);
    stored.setWhName("1창고");
    stored.setWhLoc("본관 1층 시료실");
    stored.setTypeCode("01");
    stored.setMngId("hong");
    mapper.existing = stored;
    mapper.typeCodeCnt = 1;

    Warehouse req = new Warehouse();
    req.setStrSeq(1);
    req.setWhLoc("");

    assertTrue(service.update(req));
    assertNull(mapper.updated.getWhLoc());
    assertEquals("1창고", mapper.updated.getWhName());
  }

  @Test
  public void update_rejectsBodyWithNothingToChange() {
    mapper.existing = active(1);
    Warehouse req = new Warehouse();
    req.setStrSeq(1);
    try {
      service.update(req);
      fail("expected check data");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.CHECK_DATA, e.getMessage());
    }
  }

  /** 확인과 UPDATE 사이에 다른 사용자가 중지해 0행이 나오면 예외여야 한다. */
  @Test
  public void update_throwsWhenRowNotUpdated() {
    Warehouse stored = active(1);
    stored.setWhName("1창고");
    stored.setMngId("hong");
    stored.setTypeCode("01");
    mapper.existing = stored;
    mapper.typeCodeCnt = 1;
    mapper.updateResult = 0;

    Warehouse req = new Warehouse();
    req.setStrSeq(1);
    req.setWhName("1창고 수정");
    try {
      service.update(req);
      fail("expected readonly");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.WH_READONLY, e.getMessage());
    }
  }

  private Warehouse active(int strSeq) {
    Warehouse wh = new Warehouse();
    wh.setStrSeq(strSeq);
    wh.setState("I");
    return wh;
  }

  @Test
  public void update_blockedWhenStopped() {
    Warehouse req = new Warehouse();
    req.setStrSeq(1);
    req.setWhName("1창고");
    req.setMngId("hong");
    mapper.existing = new Warehouse();
    mapper.existing.setState("D");
    try {
      service.update(req);
      fail("expected readonly");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.WH_READONLY, e.getMessage());
    }
  }

  @Test
  public void insertZoneRange_createsPaddedNamesAndSkipsTaken() {
    mapper.existing = new Warehouse();
    mapper.existing.setStrSeq(1);
    mapper.takenNames.add("가-02");
    ZoneRangeReq req = new ZoneRangeReq();
    req.setStrSeq(1);
    req.setPrefix("가");
    req.setFromNo(1);
    req.setToNo(3);

    ZoneRangeResult result = service.insertZoneRange(req);
    assertEquals(2, result.getAddedCnt());
    assertEquals("가-01", result.getZones().get(0).getZoneName());
    assertEquals("가-03", result.getZones().get(1).getZoneName());
    // 구역설명은 범위 추가에서 넣지 않는다
    assertNull(result.getZones().get(0).getZoneDesc());
    assertEquals("01", result.getZones().get(0).getUseCode());
    assertEquals(1, result.getSkippedNames().size());
    assertEquals("가-02", result.getSkippedNames().get(0));
  }

  @Test
  public void insertZoneRange_rejectsReversedRange() {
    mapper.existing = active(1);
    ZoneRangeReq req = new ZoneRangeReq();
    req.setStrSeq(1);
    req.setPrefix("가");
    req.setFromNo(12);
    req.setToNo(3);
    try {
      service.insertZoneRange(req);
      fail("expected range check");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.CHECK_ZONE_RANGE, e.getMessage());
    }
  }

  @Test
  public void insertZoneRange_rejectsZeroOrNegativeStart() {
    mapper.existing = active(1);
    for (int bad : new int[] {0, -1}) {
      ZoneRangeReq req = new ZoneRangeReq();
      req.setStrSeq(1);
      req.setPrefix("가");
      req.setFromNo(bad);
      req.setToNo(5);
      try {
        service.insertZoneRange(req);
        fail("expected range check for " + bad);
      } catch (SamBizException e) {
        assertEquals(ResponseMessage.CHECK_ZONE_RANGE, e.getMessage());
      }
    }
  }

  @Test
  public void insertZoneRange_rejectsBadPrefix() {
    mapper.existing = active(1);
    ZoneRangeReq req = new ZoneRangeReq();
    req.setStrSeq(1);
    req.setPrefix("  ");
    req.setFromNo(1);
    req.setToNo(3);
    try {
      service.insertZoneRange(req);
      fail("expected range check");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.CHECK_ZONE_RANGE, e.getMessage());
    }
  }

  @Test
  public void insertZoneRange_rejectsWhenEveryNameTaken() {
    mapper.existing = active(1);
    mapper.takenNames.add("가-01");
    mapper.takenNames.add("가-02");
    ZoneRangeReq req = new ZoneRangeReq();
    req.setStrSeq(1);
    req.setPrefix("가");
    req.setFromNo(1);
    req.setToNo(2);
    try {
      service.insertZoneRange(req);
      fail("expected none created");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.ZONE_RANGE_NONE, e.getMessage());
    }
  }

  @Test
  public void insertZoneRange_startsFromGivenNumber() {
    Warehouse wh = active(1);
    wh.setWhName("1창고");
    mapper.existing = wh;
    ZoneRangeReq req = new ZoneRangeReq();
    req.setStrSeq(1);
    req.setPrefix("가");
    req.setFromNo(5);
    req.setToNo(7);

    ZoneRangeResult result = service.insertZoneRange(req);
    assertEquals(3, result.getAddedCnt());
    assertEquals("가-05", result.getZones().get(0).getZoneName());
    assertEquals("가-07", result.getZones().get(2).getZoneName());
  }

  @Test
  public void insertZoneRange_appliesUseCodeToEveryZone() {
    mapper.existing = active(1);
    ZoneRangeReq req = new ZoneRangeReq();
    req.setStrSeq(1);
    req.setPrefix("나");
    req.setFromNo(1);
    req.setToNo(3);
    req.setUseCode("pick");

    ZoneRangeResult result = service.insertZoneRange(req);
    assertEquals(3, result.getAddedCnt());
    for (Zone z : result.getZones()) {
      assertEquals("02", z.getUseCode());
    }
  }

  @Test
  public void insertZoneRange_rejectsBadUseCode() {
    mapper.existing = active(1);
    ZoneRangeReq req = new ZoneRangeReq();
    req.setStrSeq(1);
    req.setPrefix("가");
    req.setFromNo(1);
    req.setToNo(2);
    req.setUseCode("zz");
    try {
      service.insertZoneRange(req);
      fail("expected use check");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.CHECK_ZONE_USE, e.getMessage());
    }
  }

  @Test
  public void insertZoneRange_rejectsWhenWarehouseStopped() {
    Warehouse wh = active(1);
    wh.setState("D");
    mapper.existing = wh;
    ZoneRangeReq req = new ZoneRangeReq();
    req.setStrSeq(1);
    req.setPrefix("가");
    req.setFromNo(1);
    req.setToNo(2);
    try {
      service.insertZoneRange(req);
      fail("expected readonly");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.WH_READONLY, e.getMessage());
    }
  }

  @Test
  public void insertZoneRange_rejectsOverFifty() {
    mapper.existing = new Warehouse();
    mapper.existing.setStrSeq(1);
    ZoneRangeReq req = new ZoneRangeReq();
    req.setStrSeq(1);
    req.setPrefix("가");
    req.setFromNo(1);
    req.setToNo(51);
    try {
      service.insertZoneRange(req);
      fail("expected range limit");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.ZONE_RANGE_LIMIT, e.getMessage());
    }
  }

  @Test
  public void insertZone_rejectsMissingWarehouse() {
    mapper.existing = null;
    Zone req = new Zone();
    req.setStrSeq(9);
    req.setZoneName("가-01");
    try {
      service.insertZone(req);
      fail("expected no data");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.NO_DATA, e.getMessage());
    }
  }

  @Test
  public void insertZone_rejectsWhenWarehouseStopped() {
    Warehouse wh = active(1);
    wh.setState("D");
    mapper.existing = wh;
    Zone req = new Zone();
    req.setStrSeq(1);
    req.setZoneName("가-01");
    try {
      service.insertZone(req);
      fail("expected readonly");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.WH_READONLY, e.getMessage());
    }
  }

  @Test
  public void insertZone_rejectsMissingStrSeq() {
    Zone req = new Zone();
    req.setZoneName("가-01");
    try {
      service.insertZone(req);
      fail("expected check data");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.CHECK_DATA, e.getMessage());
    }
  }

  @Test
  public void insertZone_rejectsBadName() {
    mapper.existing = active(1);
    Zone req = new Zone();
    req.setStrSeq(1);
    req.setZoneName("   ");
    try {
      service.insertZone(req);
      fail("expected name check");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.CHECK_ZONE_NAME, e.getMessage());
    }
  }

  @Test
  public void insertZone_rejectsBadUseCode() {
    mapper.existing = active(1);
    Zone req = new Zone();
    req.setStrSeq(1);
    req.setZoneName("가-01");
    req.setUseCode("zz");
    try {
      service.insertZone(req);
      fail("expected use check");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.CHECK_ZONE_USE, e.getMessage());
    }
  }

  @Test
  public void insertZone_defaultsUseCodeToNormal() {
    Warehouse wh = active(1);
    wh.setWhName("1창고");
    mapper.existing = wh;
    Zone req = new Zone();
    req.setStrSeq(1);
    req.setZoneName("  가-01  ");

    assertTrue(service.insertZone(req));
    assertEquals("가-01", req.getZoneName());
    assertEquals("01", req.getUseCode());
    assertEquals("일반", req.getUseName());
    assertEquals("1창고", req.getWhName());
    assertNull(req.getZoneDesc());
  }

  @Test
  public void insertZone_mapsScreenUseCodes() {
    mapper.existing = active(1);
    Zone pick = new Zone();
    pick.setStrSeq(1);
    pick.setZoneName("나-01");
    pick.setUseCode("pick");
    assertTrue(service.insertZone(pick));
    assertEquals("02", pick.getUseCode());
    assertEquals("인수요청", pick.getUseName());

    Zone lng = new Zone();
    lng.setStrSeq(1);
    lng.setZoneName("나-02");
    lng.setUseCode("장기");
    assertTrue(service.insertZone(lng));
    assertEquals("03", lng.getUseCode());
    assertEquals("장기", lng.getUseName());
  }

  @Test
  public void insertZone_rejectsDuplicateName() {
    Zone req = new Zone();
    req.setStrSeq(1);
    req.setZoneName("가-01");
    mapper.existing = new Warehouse();
    mapper.existing.setStrSeq(1);
    mapper.zoneNameCnt = 1;
    try {
      service.insertZone(req);
      fail("expected duplicate zone");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.DUPLICATE_ZONE, e.getMessage());
    }
  }

  /* ===================== 구역 사용 중지 ===================== */

  @Test
  public void stopZone_rejectsMissingZone() {
    mapper.zoneBySeq = null;
    Zone req = new Zone();
    req.setZoneSeq(99);
    try {
      service.stopZone(req);
      fail("expected no data");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.NO_DATA, e.getMessage());
    }
  }

  @Test
  public void stopZone_rejectsAlreadyStopped() {
    mapper.zoneBySeq = zone(10, "가-01", "01", "D");
    Zone req = new Zone();
    req.setZoneSeq(10);
    try {
      service.stopZone(req);
      fail("expected already stopped");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.ZONE_ALREADY_STOPPED, e.getMessage());
    }
  }

  @Test
  public void stopZone_blockedWhenItemsRemain() {
    mapper.existing = active(1);
    mapper.zoneBySeq = zone(10, "가-01", "01", "I");
    mapper.itemCntByZone = 3;
    Zone req = new Zone();
    req.setZoneSeq(10);
    try {
      service.stopZone(req);
      fail("expected item guard");
    } catch (SamBizException e) {
      assertTrue(e.getMessage().contains("가-01"));
      assertTrue(e.getMessage().contains("3건"));
    }
  }

  @Test
  public void stopZone_succeedsWhenEmpty() {
    mapper.existing = active(1);
    mapper.zoneBySeq = zone(10, "가-01", "01", "I");
    Zone req = new Zone();
    req.setZoneSeq(10);
    req.setUdtMemId("hong");

    assertTrue(service.stopZone(req));
    assertEquals(1, mapper.stoppedZoneSeqs.size());
    assertEquals(Integer.valueOf(10), mapper.stoppedZoneSeqs.get(0));
    // 소속 창고를 요청 본문에서 받지 않고 저장된 값에서 가져온다
    assertEquals(1, req.getStrSeq());
  }

  @Test
  public void stopZone_rejectsLastActiveZone() {
    mapper.existing = active(1);
    mapper.zoneBySeq = zone(10, "가-01", "01", "I");
    mapper.activeZoneCnt = 1;
    Zone req = new Zone();
    req.setZoneSeq(10);
    try {
      service.stopZone(req);
      fail("expected last zone guard");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.ZONE_LAST_ONE, e.getMessage());
    }
  }

  @Test
  public void stopZone_rejectsWhenWarehouseStopped() {
    Warehouse wh = active(1);
    wh.setState("D");
    mapper.existing = wh;
    mapper.zoneBySeq = zone(10, "가-01", "01", "I");
    Zone req = new Zone();
    req.setZoneSeq(10);
    try {
      service.stopZone(req);
      fail("expected warehouse stopped");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.ZONE_WH_STOPPED, e.getMessage());
    }
  }

  @Test
  public void stopZone_throwsWhenRowNotUpdated() {
    mapper.existing = active(1);
    mapper.zoneBySeq = zone(10, "가-01", "01", "I");
    mapper.stopZoneResult = 0;
    Zone req = new Zone();
    req.setZoneSeq(10);
    try {
      service.stopZone(req);
      fail("expected already stopped");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.ZONE_ALREADY_STOPPED, e.getMessage());
    }
  }

  /** 이름과 용도를 함께 보냈을 때 용도 UPDATE 가 0행이면 이름 변경도 롤백되어야 한다. */
  @Test
  public void updateZone_throwsWhenUseRowNotUpdated() {
    mapper.existing = active(1);
    mapper.zoneBySeq = zone(10, "가-01", "01", "I");
    mapper.useResult = 0;
    Zone req = new Zone();
    req.setZoneSeq(10);
    req.setZoneName("가-05");
    req.setUseCode("02");
    try {
      service.updateZone(req);
      fail("expected readonly");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.ZONE_READONLY, e.getMessage());
    }
  }

  @Test
  public void updateZone_throwsWhenLabelRowNotUpdated() {
    mapper.existing = active(1);
    mapper.zoneBySeq = zone(10, "가-01", "01", "I");
    mapper.labelResult = 0;
    Zone req = new Zone();
    req.setZoneSeq(10);
    req.setZoneName("가-05");
    try {
      service.updateZone(req);
      fail("expected readonly");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.ZONE_READONLY, e.getMessage());
    }
  }

  @Test
  public void updateZone_labelLeavesUse() {
    readyZone("01");
    Zone req = new Zone();
    req.setZoneSeq(10);
    req.setZoneName("가-02");
    req.setZoneDesc("창가");
    assertTrue(service.updateZone(req));
    assertEquals("가-02", mapper.labelUpdate.getZoneName());
    assertEquals("창가", mapper.labelUpdate.getZoneDesc());
    assertEquals(null, mapper.useUpdate);
    assertEquals("01", req.getUseCode());
  }

  @Test
  public void updateZone_useLeavesName() {
    readyZone("01");
    Zone req = new Zone();
    req.setZoneSeq(10);
    req.setUseCode("02");
    assertTrue(service.updateZone(req));
    assertEquals(null, mapper.labelUpdate);
    assertEquals("02", mapper.useUpdate.getUseCode());
    assertEquals("가-01", req.getZoneName());
  }

  private void readyZone(String useCode) {
    mapper.existing = new Warehouse();
    mapper.existing.setStrSeq(1);
    mapper.existing.setWhName("1창고");
    Zone zone = new Zone();
    zone.setZoneSeq(10);
    zone.setStrSeq(1);
    zone.setZoneName("가-01");
    zone.setZoneDesc("기존");
    zone.setUseCode(useCode);
    zone.setState("I");
    mapper.zoneBySeq = zone;
  }

  private static class FakeStrMapper implements StrMapper {
    int typeCodeCnt;
    int nextStrSeq = 1;
    int zoneNameCnt;
    int whNameCnt;
    /** 이미 쓰고 있는 창고명 -> 그 창고의 STR_SEQ */
    Map<String, Integer> takenWhNames = new HashMap<String, Integer>();
    Set<String> takenNames = new HashSet<String>();
    int itemCntByWh;
    Warehouse existing;
    StoreDetailDTO detailRow;
    Zone zoneBySeq;
    List<Zone> zones = new ArrayList<Zone>();

    @Override
    public List<Warehouse> selectList(String searchWord, String includeStopped) {
      return null;
    }

    @Override
    public Warehouse selectBySeq(int strSeq) {
      return existing;
    }

    @Override
    public StoreDetailDTO selectDetailBySeq(int strSeq) {
      return detailRow;
    }

    Warehouse inserted;

    @Override
    public int insert(Warehouse req) {
      inserted = req;
      req.setStrSeq(nextStrSeq);
      return 1;
    }

    Warehouse updated;
    int updateResult = 1;

    @Override
    public int update(Warehouse req) {
      updated = req;
      return updateResult;
    }

    int stopResult = 1;
    List<Integer> stoppedZoneSeqs = new ArrayList<Integer>();

    @Override
    public int stop(Warehouse req) {
      return stopResult;
    }

    @Override
    public int countTypeCode(String typeCode) {
      return typeCodeCnt;
    }

    @Override
    public int countWhName(String whName, int strSeq) {
      // 실제 SQL: STATE != 'D' 인 같은 이름을 세되, strSeq > 0 이면 그 행은 뺀다.
      Integer owner = takenWhNames.get(whName);
      if (owner != null && (strSeq <= 0 || owner.intValue() != strSeq)) {
        return 1;
      }
      return whNameCnt;
    }

    @Override
    public List<Zone> selectZoneList(int strSeq) {
      return zones;
    }

    @Override
    public Zone selectZoneBySeq(int zoneSeq) {
      return zoneBySeq;
    }

    @Override
    public int countZoneName(int strSeq, Integer zoneSeq, String zoneName) {
      if (takenNames.contains(zoneName)) {
        return 1;
      }
      return zoneNameCnt;
    }

    @Override
    public int insertZone(Zone req) {
      zones.add(req);
      return 1;
    }

    Zone labelUpdate;
    Zone useUpdate;

    int labelResult = 1;
    int useResult = 1;

    @Override
    public int updateZoneLabel(Zone req) {
      labelUpdate = req;
      return labelResult;
    }

    @Override
    public int updateZoneUse(Zone req) {
      useUpdate = req;
      return useResult;
    }

    int stopZoneResult = 1;

    @Override
    public int stopZone(Zone req) {
      stoppedZoneSeqs.add(Integer.valueOf(req.getZoneSeq()));
      return stopZoneResult;
    }

    @Override
    public int countItemsByWh(int strSeq) {
      return itemCntByWh;
    }

    int itemCntByZone;
    int activeZoneCnt = 2;

    @Override
    public int countItemsByZone(int zoneSeq) {
      return itemCntByZone;
    }

    @Override
    public int countActiveZones(int strSeq) {
      return activeZoneCnt;
    }
  }
}
