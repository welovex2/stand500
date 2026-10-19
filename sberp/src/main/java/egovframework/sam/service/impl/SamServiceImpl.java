package egovframework.sam.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import egovframework.cmm.service.ComParam;
import egovframework.cmm.service.EgovFileMngService;
import egovframework.cmm.service.FileVO;
import egovframework.cmm.service.ResponseMessage;
import egovframework.cmm.service.SbkInfoVO;
import egovframework.cnf.service.StrMapper;
import egovframework.cnf.service.Warehouse;
import egovframework.cnf.service.Zone;
import egovframework.ncc.service.NextcloudFolderService;
import egovframework.sam.dto.ImDTO;
import egovframework.sam.dto.ImSubDTO;
import egovframework.sam.dto.ImSubItemDetailDTO;
import egovframework.sam.dto.SamLocScanReq;
import egovframework.sam.dto.SamLocScanResult;
import egovframework.sam.dto.SamUploadFolderDTO;
import egovframework.sam.service.ImSub;
import egovframework.sam.service.ImSubLoc;
import egovframework.sam.service.SamBizException;
import egovframework.sam.service.SamMapper;
import egovframework.sam.service.SamService;
import egovframework.sam.util.SamItemIdSupport;
import egovframework.sam.util.SamItemIdSupport.Parsed;
import egovframework.sam.util.SamProcStateSupport;
import egovframework.sam.util.SamUploadFolderSupport;
import egovframework.sbk.service.SbkService;

@Service("SamService")
public class SamServiceImpl implements SamService {

  @Autowired
  SamMapper samMapper;

  @Autowired
  EgovFileMngService fileMngService;

  @Autowired
  SbkService sbkService;

  @Autowired
  NextcloudFolderService nextcloudFolderService;

  @Autowired
  StrMapper strMapper;

  @Override
  @Transactional
  public boolean insert(ImDTO req) {

    boolean result = true;

    samMapper.insert(req);
    ImDTO detail = samMapper.detail(req.getSbkId());
    if (detail != null) {
      req.setImId(detail.getImId());
    }

    // 시료리스트
    if (req.getItemList() != null) {
      List<ImSub> iItems = req.getItemList().stream().filter(t -> "I".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(iItems)) {

        for (ImSub item : iItems) {
          samMapper.insertSub(req.getInsMemId(), req.getImId(), item);
        }
      }
    }

    return result;
  }

  @Override
  @Transactional
  public boolean update(ImDTO req) {
    boolean result = true;

    samMapper.update(req);

    // 시료리스트
    if (!ObjectUtils.isEmpty(req.getItemList())) {
      List<ImSub> iItems = req.getItemList().stream().filter(t -> "I".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(iItems)) {
        for (ImSub item : iItems) {
          samMapper.insertSub(req.getInsMemId(), req.getImId(), item);
        }
      }

      List<ImSub> uItems = req.getItemList().stream().filter(t -> "U".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(uItems))
        samMapper.updateSub(req.getInsMemId(), req.getImId(), uItems);

      List<ImSub> dItems = req.getItemList().stream().filter(t -> "D".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(dItems))
        samMapper.deleteSub(req.getInsMemId(), req.getImId(), dItems);
    }
    return result;
  }


  @Override
  public ImDTO detail(String sbkId) throws Exception {

    ImDTO result = samMapper.detail(sbkId);

    if (!ObjectUtils.isEmpty(result)) {
      result.setItemList(samMapper.subList(result.getImId()));

      // 시료이미지
      FileVO fileVO = new FileVO();
      fileVO.setAtchFileId(result.getPicUrl());

      List<FileVO> picResult = fileMngService.selectImageFileList(fileVO);

      for (FileVO pic : picResult) {
        try {
          pic.setFileStreCours(fileMngService.resolveImageUrl(pic));
        } catch (Exception e) {
          pic.setFileStreCours("");
        }
      }

      result.setPicList(picResult);
    }

    return result;
  }

  @Override
  public int selectListCnt(ComParam param) {
    return samMapper.selectListCnt(param);
  }

  @Override
  public List<ImSubDTO> selectList(ComParam param) {
    List<ImSubDTO> result = samMapper.selectList(param);

    // 번호 매기기
    for (int i = 0; i < result.size(); i++) {
      ImSubDTO row = result.get(i);
      row.setNo(param.getTotalCount() - (((param.getPageIndex() - 1) * param.getPageUnit()) + i));
      row.setProcStateName(SamProcStateSupport.procName(row.getProcState()));
    }

    return result;
  }

  @Override
  public ImSubItemDetailDTO itemDetail(String imSubId) throws Exception {
    Parsed parsed = SamItemIdSupport.parse(imSubId);
    if (parsed == null) {
      return null;
    }

    ImSubItemDetailDTO result =
        samMapper.selectSubBySbkIdAndSubSeq(parsed.getSbkId(), parsed.getImSubSeq());
    if (result == null) {
      return null;
    }

    result.setProcStateName(SamProcStateSupport.procName(result.getProcState()));
    result.setLocHist(samMapper.selectLocHist(result.getImId(), result.getImSubSeq()));

    SbkInfoVO sbk = sbkService.findBySbkNoAndProvision(result.getSbkId());
    if (sbk == null || StringUtils.isEmpty(sbk.getNcFolderPath())) {
      return result;
    }

    result.setNcFolderPath(sbk.getNcFolderPath());
    List<SamUploadFolderDTO> uploadFolders =
        SamUploadFolderSupport.buildUploadFolders(sbk.getNcFolderPath());
    for (SamUploadFolderDTO folder : uploadFolders) {
      nextcloudFolderService.ensureFolder(folder.getRelativePath());
    }
    result.setUploadFolders(uploadFolders);
    return result;
  }

  @Override
  @Transactional
  public SamLocScanResult scanLoc(SamLocScanReq req, String memId) {
    Zone zone = resolveZone(req);
    if (ObjectUtils.isEmpty(req.getImSubIds())) {
      throw new SamBizException(ResponseMessage.CHECK_DATA);
    }

    SamLocScanResult result = new SamLocScanResult();
    for (String rawId : req.getImSubIds()) {
      ImSubItemDetailDTO item = loadItem(rawId);
      if (item == null) {
        throw new SamBizException(ResponseMessage.NO_DATA + " (" + rawId + ")");
      }
      if (StringUtils.hasText(item.getCarryOutCode())) {
        throw new SamBizException(ResponseMessage.SAMPLE_CARRY_OUT + " (" + rawId + ")");
      }

      ImSubLoc loc = new ImSubLoc();
      loc.setImId(item.getImId());
      loc.setImSubSeq(item.getImSubSeq());
      loc.setZoneSeq(zone.getZoneSeq());
      loc.setMemo(req.getMemo());
      loc.setInsMemId(memId);
      loc.setUdtMemId(memId);
      samMapper.insertLoc(loc);
      samMapper.updateSubLocation(item.getImId(), item.getImSubSeq(), zone.getZoneSeq(), memId);
      result.setSavedCnt(result.getSavedCnt() + 1);
    }
    return result;
  }

  private ImSubItemDetailDTO loadItem(String imSubId) {
    Parsed parsed = SamItemIdSupport.parse(imSubId);
    if (parsed == null) {
      throw new SamBizException("시료 QR을 확인하세요. (" + imSubId + ")");
    }
    return samMapper.selectSubBySbkIdAndSubSeq(parsed.getSbkId(), parsed.getImSubSeq());
  }

  private Zone resolveZone(SamLocScanReq req) {
    if (req == null || req.getZoneSeq() <= 0) {
      throw new SamBizException(ResponseMessage.CHECK_DATA);
    }
    Zone zone = strMapper.selectZoneBySeq(req.getZoneSeq());
    if (zone == null) {
      throw new SamBizException(ResponseMessage.NO_DATA);
    }
    if ("D".equals(zone.getState())) {
      throw new SamBizException(ResponseMessage.ZONE_LABEL_OFF);
    }
    // 창고를 중지해도 구역은 그대로 남는다. 창고 쪽을 보지 않으면
    // 중지된 창고에 시료를 넣을 수 있다.
    Warehouse wh = strMapper.selectBySeq(zone.getStrSeq());
    if (wh == null || "D".equals(wh.getState())) {
      throw new SamBizException(ResponseMessage.WH_LABEL_OFF);
    }
    return zone;
  }
}
