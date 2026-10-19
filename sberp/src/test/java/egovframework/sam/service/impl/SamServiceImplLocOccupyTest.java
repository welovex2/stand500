package egovframework.sam.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import egovframework.cmm.service.ComParam;
import egovframework.cnf.service.StrMapper;
import egovframework.cnf.service.Warehouse;
import egovframework.cnf.service.Zone;
import egovframework.sam.dto.ImDTO;
import egovframework.sam.dto.ImSubDTO;
import egovframework.sam.dto.ImSubItemDetailDTO;
import egovframework.cmm.service.ResponseMessage;
import egovframework.sam.dto.SamLocScanReq;
import egovframework.sam.dto.SamLocScanResult;
import egovframework.sam.service.ImSub;
import egovframework.sam.service.ImSubLoc;
import egovframework.sam.service.SamBizException;
import egovframework.sam.service.SamMapper;

public class SamServiceImplLocOccupyTest {

  private SamServiceImpl service;
  private FakeSamMapper samMapper;
  private FakeStrMapper strMapper;

  @Before
  public void setUp() throws Exception {
    service = new SamServiceImpl();
    samMapper = new FakeSamMapper();
    strMapper = new FakeStrMapper();
    setField("samMapper", samMapper);
    setField("strMapper", strMapper);
  }

  @Test
  public void scanLoc_savesLocationOnly() {
    Zone zone = zone(3, 1, "가-01");
    strMapper.bySeq = zone;
    samMapper.item = item("IM26-G0001", 1, "SB26-G0001");

    SamLocScanReq req = new SamLocScanReq();
    req.setZoneSeq(3);
    req.setImSubIds(Arrays.asList("SB26-G0001-M01"));

    SamLocScanResult result = service.scanLoc(req, "tester");
    assertEquals(1, result.getSavedCnt());
    assertEquals(0, result.getWarnings().size());
    assertEquals(1, samMapper.insertedLoc.size());
  }

  @Test
  public void scanLoc_rejectsStoppedZone() {
    Zone zone = zone(3, 1, "가-01");
    zone.setState("D");
    strMapper.bySeq = zone;
    samMapper.item = item("IM26-G0001", 1, "SB26-G0001");

    SamLocScanReq req = new SamLocScanReq();
    req.setZoneSeq(3);
    req.setImSubIds(Arrays.asList("SB26-G0001-M01"));
    try {
      service.scanLoc(req, "tester");
      fail("expected stopped zone");
    } catch (SamBizException e) {
      assertEquals(ResponseMessage.ZONE_LABEL_OFF, e.getMessage());
    }
  }

  private ImSubItemDetailDTO item(String imId, int seq, String sbkId) {
    ImSubItemDetailDTO dto = new ImSubItemDetailDTO();
    dto.setImId(imId);
    dto.setImSubSeq(seq);
    dto.setSbkId(sbkId);
    dto.setImSubId(sbkId + "-M" + String.format("%02d", seq));
    return dto;
  }

  private Zone zone(int zoneSeq, int strSeq, String zoneName) {
    Zone z = new Zone();
    z.setZoneSeq(zoneSeq);
    z.setStrSeq(strSeq);
    z.setZoneName(zoneName);
    return z;
  }

  private void setField(String name, Object value) throws Exception {
    java.lang.reflect.Field field = SamServiceImpl.class.getDeclaredField(name);
    field.setAccessible(true);
    field.set(service, value);
  }

  private static class FakeStrMapper implements StrMapper {
    Zone bySeq;

    @Override
    public List<Warehouse> selectList(String searchWord, String includeStopped) {
      return null;
    }

    @Override
    public Warehouse selectBySeq(int strSeq) {
      return null;
    }

    @Override
    public int insert(Warehouse req) {
      return 0;
    }

    @Override
    public int update(Warehouse req) {
      return 0;
    }

    @Override
    public int stop(Warehouse req) {
      return 0;
    }

    @Override
    public int countTypeCode(String typeCode) {
      return 0;
    }

    @Override
    public int countWhName(String whName, int strSeq) {
      return 0;
    }

    @Override
    public List<Zone> selectZoneList(int strSeq) {
      return null;
    }

    @Override
    public Zone selectZoneBySeq(int zoneSeq) {
      return bySeq;
    }

    @Override
    public int countZoneName(int strSeq, Integer zoneSeq, String zoneName) {
      return 0;
    }

    @Override
    public int insertZone(Zone req) {
      return 0;
    }

    @Override
    public int updateZoneLabel(Zone req) {
      return 0;
    }

    @Override
    public int updateZoneUse(Zone req) {
      return 0;
    }

    @Override
    public int stopZone(Zone req) {
      return 0;
    }

    @Override
    public int countItemsByWh(int strSeq) {
      return 0;
    }

    @Override
    public int countItemsByZone(int zoneSeq) {
      return 0;
    }
  }

  private static class FakeSamMapper implements SamMapper {
    ImSubItemDetailDTO item;
    List<ImSubLoc> insertedLoc = new ArrayList<ImSubLoc>();

    @Override
    public boolean insert(ImDTO req) {
      return false;
    }

    @Override
    public boolean update(ImDTO req) {
      return false;
    }

    @Override
    public ImDTO detail(String sbkId) {
      return null;
    }

    @Override
    public void insertSub(String insMemId, String imId, ImSub item) {}

    @Override
    public void updateSub(String insMemId, String imId, List<ImSub> itemList) {}

    @Override
    public void deleteSub(String insMemId, String imId, List<ImSub> itemList) {}

    @Override
    public List<ImSub> subList(String imId) {
      return null;
    }

    @Override
    public int selectListCnt(ComParam param) {
      return 0;
    }

    @Override
    public List<ImSubDTO> selectList(ComParam param) {
      return null;
    }

    @Override
    public ImSubItemDetailDTO selectSubBySbkIdAndSubSeq(String sbkId, int imSubSeq) {
      return item;
    }

    @Override
    public int insertLoc(ImSubLoc loc) {
      insertedLoc.add(loc);
      return 1;
    }

    @Override
    public int updateSubLocation(String imId, int imSubSeq, int zoneSeq, String udtMemId) {
      return 1;
    }

    @Override
    public List<ImSubLoc> selectLocHist(String imId, int imSubSeq) {
      return null;
    }
  }
}
