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
}
