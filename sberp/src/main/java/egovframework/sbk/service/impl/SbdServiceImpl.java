package egovframework.sbk.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import egovframework.sbk.service.Sbd;
import egovframework.sbk.service.SbdMapper;
import egovframework.sbk.service.SbdService;

@Service("SbdService")
public class SbdServiceImpl implements SbdService {

  @Autowired
  SbdMapper sbdMapper;
  
  @Override
  public Sbd selectDriDetail(String sbkId) {
    return sbdMapper.selectDriDetail(sbkId);
  }

  @Override
  public Sbd selectBhDetail(String sbkId) {
    return sbdMapper.selectBhDetail(sbkId);
  }

  @Override
  public Sbd selectJhDetail(String sbkId) {
    return sbdMapper.selectJhDetail(sbkId);
  }

  @Override
  public int insertBh(Sbd req) {
    return sbdMapper.insertBh(req);
  }

  @Override
  public int insertJh(Sbd req) {
    return sbdMapper.insertJh(req);
  }
}
