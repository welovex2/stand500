package egovframework.sbk.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import egovframework.cmm.service.EgovFileMngService;
import egovframework.sbk.service.Sbd;
import egovframework.sbk.service.SbdMapper;
import egovframework.sbk.service.SbdService;

@Service("SbdService")
public class SbdServiceImpl implements SbdService {

  @Autowired
  SbdMapper sbdMapper;

  @Autowired
  EgovFileMngService fileMngService;

  @Override
  public Sbd selectDriDetail(String sbkId) {
    Sbd detail = sbdMapper.selectDriDetail(sbkId);
    resolveRprsnSign(detail);
    return detail;
  }

  @Override
  public Sbd selectBhDetail(String sbkId) {
    Sbd detail = sbdMapper.selectBhDetail(sbkId);
    resolveRprsnSign(detail);
    resolveProductPics(detail);
    return detail;
  }

  @Override
  public Sbd selectJhDetail(String sbkId) {
    Sbd detail = sbdMapper.selectJhDetail(sbkId);
    resolveRprsnSign(detail);
    return detail;
  }

  @Override
  public int insertBh(Sbd req) {
    return sbdMapper.insertBh(req);
  }

  @Override
  public int insertJh(Sbd req) {
    return sbdMapper.insertJh(req);
  }

  private void resolveRprsnSign(Sbd detail) {
    if (ObjectUtils.isEmpty(detail)) {
      return;
    }
    try {
      detail.setRprsnSign(fileMngService.resolveImageUrl(detail.getRprsnSign()));
    } catch (Exception e) {
      detail.setRprsnSign("");
    }
  }

  private void resolveProductPics(Sbd detail) {
    if (ObjectUtils.isEmpty(detail)) {
      return;
    }
    try {
      if (detail.getPicFrontAtchFileId() != null && !detail.getPicFrontAtchFileId().isEmpty()) {
        String fileSn = detail.getPicFrontFileSn() != null ? detail.getPicFrontFileSn() : "0";
        detail.setPicFront(fileMngService.resolveImageUrl(detail.getPicFrontAtchFileId(), fileSn));
      }
      if (detail.getPicBackAtchFileId() != null && !detail.getPicBackAtchFileId().isEmpty()) {
        String fileSn = detail.getPicBackFileSn() != null ? detail.getPicBackFileSn() : "0";
        detail.setPicBack(fileMngService.resolveImageUrl(detail.getPicBackAtchFileId(), fileSn));
      }
    } catch (Exception e) {
      detail.setPicFront("");
      detail.setPicBack("");
    }
  }
}
