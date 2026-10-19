package egovframework.cnf.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import egovframework.cmm.service.ResponseMessage;
import egovframework.cnf.dto.StoreDetailDTO;
import egovframework.cnf.dto.ZoneRangeReq;
import egovframework.cnf.dto.ZoneRangeResult;
import egovframework.cnf.service.StrMapper;
import egovframework.cnf.service.StrService;
import egovframework.cnf.service.Warehouse;
import egovframework.cnf.service.Zone;
import egovframework.cnf.util.WhCodeSupport;
import egovframework.sam.service.SamBizException;

@Service("StrService")
public class StrServiceImpl implements StrService {

  /** 사용자에게 보이는 메시지의 줄바꿈. */
  private static final String NL = String.valueOf((char) 10);

  @Autowired
  StrMapper strMapper;

  /* ===================== 창고 ===================== */

  @Override
  public List<Warehouse> selectList(String searchWord, boolean includeStopped) {
    return strMapper.selectList(trimToNull(searchWord), includeStopped ? "Y" : "N");
  }

  @Override
  public StoreDetailDTO detail(int strSeq) {
    StoreDetailDTO dto = strMapper.selectDetailBySeq(strSeq);
    if (dto == null) {
      return null;
    }
    dto.setZones(zoneList(dto));
    return dto;
  }

  @Override
  @Transactional
  public boolean insert(Warehouse req) {
    if (req != null) {
      // 등록에는 창고 SEQ 가 없다. 본문에 실려 온 값을 그대로 두면 validate 의 창고명 중복검사가
      // 그 SEQ 를 "자기 자신"으로 보고 비교에서 빼버려, 같은 이름이 하나 더 생긴다.
      req.setStrSeq(0);
    }
    validate(req);
    return strMapper.insert(req) > 0;
  }

  @Override
  @Transactional
  public boolean update(Warehouse req) {
    Warehouse exists = strMapper.selectBySeq(req.getStrSeq());
    if (exists == null) {
      throw new SamBizException(ResponseMessage.NO_DATA);
    }
    if ("D".equals(exists.getState())) {
      throw new SamBizException(ResponseMessage.WH_READONLY);
    }
    mergeForUpdate(req, exists);
    validate(req);
    if (strMapper.update(req) < 1) {
      // 확인과 UPDATE 사이에 다른 사용자가 창고를 중지한 경우.
      // false 를 돌려주면 화면에 띄울 말이 없다.
      throw new SamBizException(ResponseMessage.WH_READONLY);
    }
    return true;
  }

  /**
   * 보낸 항목만 고친다. 본문에 없는 항목은 저장된 값을 그대로 쓴다. 구역 수정과 같은 규칙.
   *
   * <p>창고명 · 형태 · 관리자는 값이 있을 때만 「보냈다」로 본다. 위치 · 메모는 빈 문자열로
   * 지울 수 있어야 하므로 null 이 아니면 보낸 것으로 본다.
   */
  private void mergeForUpdate(Warehouse req, Warehouse exists) {
    boolean nameSent = StringUtils.hasText(req.getWhName());
    boolean typeSent = StringUtils.hasText(req.getTypeCode());
    boolean mngSent = req.getMngId() != null;
    boolean deptSent = req.getDeptSeq() != null;
    boolean locSent = req.getWhLoc() != null;
    if (!nameSent && !typeSent && !mngSent && !deptSent && !locSent) {
      throw new SamBizException(ResponseMessage.CHECK_DATA);
    }
    if (!nameSent) {
      req.setWhName(exists.getWhName());
    }
    if (!typeSent) {
      req.setTypeCode(exists.getTypeCode());
    }
    if (!mngSent) {
      req.setMngId(exists.getMngId());
    }
    if (!deptSent) {
      req.setDeptSeq(exists.getDeptSeq());
    }
    if (!locSent) {
      req.setWhLoc(exists.getWhLoc());
    }
  }

  @Override
  @Transactional
  public boolean stop(Warehouse req) {
    Warehouse exists = strMapper.selectBySeq(req.getStrSeq());
    if (exists == null) {
      throw new SamBizException(ResponseMessage.NO_DATA);
    }
    if ("D".equals(exists.getState())) {
      throw new SamBizException(ResponseMessage.WH_ALREADY_STOPPED);
    }
    int itemCnt = strMapper.countItemsByWh(req.getStrSeq());
    if (itemCnt > 0) {
      throw new SamBizException(exists.getWhName() + " 에 보관 중인 시료가 " + itemCnt + "건 있습니다." + NL
          + "시료를 모두 꺼내거나 다른 창고로 옮긴 뒤에 중지할 수 있습니다.");
    }
    // 구역은 건드리지 않는다. 구역 중지는 「마지막 남은 구역은 중지할 수 없다」는 규칙을 가진
    // 별도 절차이고, 창고 중지가 그 규칙을 우회해 구역을 내리면 안 된다.
    // 중지된 창고의 구역 QR 은 스캔 쪽에서 거절한다(SamServiceImpl.resolveZone).
    if (strMapper.stop(req) < 1) {
      // 확인과 UPDATE 사이에 다른 사용자가 먼저 중지한 경우.
      // false 를 돌려주면 화면에 띄울 말이 없다.
      throw new SamBizException(ResponseMessage.WH_ALREADY_STOPPED);
    }
    return true;
  }

  /* ===================== 구역 ===================== */

  @Override
  @Transactional
  public boolean insertZone(Zone req) {
    Warehouse wh = loadWhForZone(req);
    applyZone(req, null, null);
    boolean result = strMapper.insertZone(req) > 0;
    fillZoneDisplay(req, wh);
    return result;
  }

  @Override
  @Transactional
  public ZoneRangeResult insertZoneRange(ZoneRangeReq req) {
    if (req == null || req.getStrSeq() <= 0) {
      throw new SamBizException(ResponseMessage.CHECK_DATA);
    }
    int from = req.getFromNo();
    int to = req.getToNo();
    if (!WhCodeSupport.isValidZoneName(req.getPrefix()) || from < 1 || to < from) {
      throw new SamBizException(ResponseMessage.CHECK_ZONE_RANGE);
    }
    if (to - from + 1 > WhCodeSupport.ZONE_RANGE_MAX) {
      throw new SamBizException(ResponseMessage.ZONE_RANGE_LIMIT);
    }
    String useCode = WhCodeSupport.normalizeUseCode(req.getUseCode());
    if (useCode == null) {
      if (req.getUseCode() == null || req.getUseCode().trim().isEmpty()) {
        useCode = WhCodeSupport.USE_NORMAL;
      } else {
        throw new SamBizException(ResponseMessage.CHECK_ZONE_USE);
      }
    }

    List<Zone> created = new ArrayList<Zone>();
    List<String> skipped = new ArrayList<String>();
    for (int no = from; no <= to; no++) {
      String name = WhCodeSupport.zoneRangeName(req.getPrefix(), no);
      if (!WhCodeSupport.isValidZoneName(name)) {
        throw new SamBizException(ResponseMessage.CHECK_ZONE_NAME);
      }
      if (strMapper.countZoneName(req.getStrSeq(), null, name) > 0) {
        skipped.add(name);
        continue;
      }
      Zone zone = new Zone();
      zone.setStrSeq(req.getStrSeq());
      zone.setZoneName(name);
      // 구역설명은 범위 추가에서 넣지 않는다. 만든 뒤 구역 수정에서 하나씩 적는다.
      zone.setUseCode(useCode);
      zone.setInsMemId(req.getInsMemId());
      zone.setUdtMemId(req.getUdtMemId());
      insertZone(zone);
      created.add(zone);
    }
    if (created.isEmpty()) {
      throw new SamBizException(ResponseMessage.ZONE_RANGE_NONE);
    }
    ZoneRangeResult result = new ZoneRangeResult();
    result.setAddedCnt(created.size());
    result.setZones(created);
    result.setSkippedNames(skipped);
    return result;
  }

  @Override
  @Transactional
  public boolean updateZone(Zone req) {
    Zone exists = strMapper.selectZoneBySeq(req.getZoneSeq());
    if (exists == null) {
      throw new SamBizException(ResponseMessage.NO_DATA);
    }
    if ("D".equals(exists.getState())) {
      throw new SamBizException(ResponseMessage.ZONE_READONLY);
    }
    req.setStrSeq(exists.getStrSeq());
    Warehouse wh = loadWhForZone(req);
    boolean nameSent = StringUtils.hasText(req.getZoneName());
    boolean descSent = req.getZoneDesc() != null;
    boolean useSent = StringUtils.hasText(req.getUseCode());
    if (!nameSent && !descSent && !useSent) {
      throw new SamBizException(ResponseMessage.CHECK_DATA);
    }
    if (nameSent || descSent) {
      applyZoneLabel(req, exists, nameSent, descSent);
    } else {
      req.setZoneName(exists.getZoneName());
      req.setZoneDesc(exists.getZoneDesc());
    }
    if (useSent) {
      String useCode = WhCodeSupport.normalizeUseCode(req.getUseCode());
      if (useCode == null) {
        throw new SamBizException(ResponseMessage.CHECK_ZONE_USE);
      }
      req.setUseCode(useCode);
    } else {
      req.setUseCode(exists.getUseCode());
    }
    // 0행이면 예외라야 한다. false 를 돌려주면 화면에 띄울 말이 없고,
    // 이름과 용도를 함께 보낸 경우 앞의 이름 변경만 커밋된 채 남는다.
    if ((nameSent || descSent) && strMapper.updateZoneLabel(req) < 1) {
      throw new SamBizException(ResponseMessage.ZONE_READONLY);
    }
    if (useSent && !req.getUseCode().equals(exists.getUseCode())
        && strMapper.updateZoneUse(req) < 1) {
      throw new SamBizException(ResponseMessage.ZONE_READONLY);
    }
    fillZoneDisplay(req, wh);
    return true;
  }

  @Override
  @Transactional
  public boolean stopZone(Zone req) {
    Zone exists = strMapper.selectZoneBySeq(req.getZoneSeq());
    if (exists == null) {
      throw new SamBizException(ResponseMessage.NO_DATA);
    }
    if ("D".equals(exists.getState())) {
      throw new SamBizException(ResponseMessage.ZONE_ALREADY_STOPPED);
    }
    int itemCnt = strMapper.countItemsByZone(req.getZoneSeq());
    if (itemCnt > 0) {
      throw new SamBizException(exists.getZoneName() + " 에 보관 중인 시료가 " + itemCnt + "건 있습니다." + NL
          + "시료를 모두 꺼내거나 다른 구역으로 옮긴 뒤에 중지할 수 있습니다.");
    }
    Warehouse wh = strMapper.selectBySeq(exists.getStrSeq());
    if (wh == null) {
      throw new SamBizException(ResponseMessage.NO_DATA);
    }
    if ("D".equals(wh.getState())) {
      throw new SamBizException(ResponseMessage.ZONE_WH_STOPPED);
    }
    // 구역이 하나도 없으면 QR 을 만들 수 없어 시료를 넣을 방법이 없는 창고가 된다.
    if (strMapper.countActiveZones(exists.getStrSeq()) <= 1) {
      throw new SamBizException(ResponseMessage.ZONE_LAST_ONE);
    }
    req.setStrSeq(exists.getStrSeq());
    if (strMapper.stopZone(req) < 1) {
      throw new SamBizException(ResponseMessage.ZONE_ALREADY_STOPPED);
    }
    return true;
  }

  /* ===================== 내부 ===================== */

  private void validate(Warehouse req) {
    if (req == null || !StringUtils.hasText(req.getWhName())) {
      throw new SamBizException(ResponseMessage.CHECK_DATA);
    }
    String typeCode = WhCodeSupport.normalizeTypeCode(req.getTypeCode());
    if (strMapper.countTypeCode(typeCode) < 1) {
      throw new SamBizException(ResponseMessage.CHECK_WH_TYPE);
    }
    req.setTypeCode(typeCode);
    req.setWhName(req.getWhName().trim());
    req.setWhLoc(trimToNull(req.getWhLoc()));
    // 관리자와 담당 부서는 둘 다 선택이다. 하나만 골라도 저장된다.
    req.setMngId(trimToNull(req.getMngId()));
    if (req.getDeptSeq() != null && req.getDeptSeq().intValue() <= 0) {
      req.setDeptSeq(null);
    }
    if (strMapper.countWhName(req.getWhName(), req.getStrSeq()) > 0) {
      throw new SamBizException(ResponseMessage.DUPLICATE_WH_NAME);
    }
  }

  private Warehouse loadWhForZone(Zone req) {
    if (req == null || req.getStrSeq() <= 0) {
      throw new SamBizException(ResponseMessage.CHECK_DATA);
    }
    Warehouse wh = strMapper.selectBySeq(req.getStrSeq());
    if (wh == null) {
      throw new SamBizException(ResponseMessage.NO_DATA);
    }
    if ("D".equals(wh.getState())) {
      throw new SamBizException(ResponseMessage.WH_READONLY);
    }
    return wh;
  }

  private void applyZone(Zone req, Integer excludeZoneSeq, String keptUseCode) {
    String name = WhCodeSupport.normalizeZoneName(req.getZoneName());
    if (!WhCodeSupport.isValidZoneName(name)) {
      throw new SamBizException(ResponseMessage.CHECK_ZONE_NAME);
    }
    if (!WhCodeSupport.isValidZoneDesc(req.getZoneDesc())) {
      throw new SamBizException(ResponseMessage.CHECK_ZONE_DESC);
    }
    String useCode = WhCodeSupport.normalizeUseCode(req.getUseCode());
    if (useCode == null) {
      if (req.getUseCode() == null || req.getUseCode().trim().isEmpty()) {
        useCode = keptUseCode != null ? keptUseCode : WhCodeSupport.USE_NORMAL;
      } else {
        throw new SamBizException(ResponseMessage.CHECK_ZONE_USE);
      }
    }
    if (strMapper.countZoneName(req.getStrSeq(), excludeZoneSeq, name) > 0) {
      throw new SamBizException(ResponseMessage.DUPLICATE_ZONE);
    }
    req.setZoneName(name);
    req.setZoneDesc(WhCodeSupport.normalizeZoneDesc(req.getZoneDesc()));
    req.setUseCode(useCode);
  }

  private void applyZoneLabel(Zone req, Zone exists, boolean nameSent, boolean descSent) {
    if (!nameSent) {
      req.setZoneName(exists.getZoneName());
    }
    String name = WhCodeSupport.normalizeZoneName(req.getZoneName());
    if (!WhCodeSupport.isValidZoneName(name)) {
      throw new SamBizException(ResponseMessage.CHECK_ZONE_NAME);
    }
    String desc = descSent ? req.getZoneDesc() : exists.getZoneDesc();
    if (!WhCodeSupport.isValidZoneDesc(desc)) {
      throw new SamBizException(ResponseMessage.CHECK_ZONE_DESC);
    }
    if (strMapper.countZoneName(req.getStrSeq(), req.getZoneSeq(), name) > 0) {
      throw new SamBizException(ResponseMessage.DUPLICATE_ZONE);
    }
    req.setZoneName(name);
    req.setZoneDesc(WhCodeSupport.normalizeZoneDesc(desc));
  }

  private List<Zone> zoneList(Warehouse wh) {
    List<Zone> zones = strMapper.selectZoneList(wh.getStrSeq());
    if (zones != null) {
      for (Zone zone : zones) {
        fillZoneDisplay(zone, wh);
      }
    }
    return zones;
  }

  private void fillZoneDisplay(Zone zone, Warehouse wh) {
    zone.setWhName(wh.getWhName());
    zone.setUseName(WhCodeSupport.useName(zone.getUseCode()));
  }

  private String trimToNull(String s) {
    if (s == null) {
      return null;
    }
    String t = s.trim();
    return t.isEmpty() ? null : t;
  }
}
