package egovframework.leg.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import egovframework.cmm.service.ComParam;
import egovframework.leg.dto.LedgerDTO;
import egovframework.leg.service.LegMapper;
import egovframework.leg.service.LegService;

@Service("LegService")
public class LegServiceImpl implements LegService {

  @Autowired
  LegMapper legMapper;
  
   @Override
  public int selectListCnt(ComParam param) {
    return legMapper.selectListCnt(param);
  }

  @Override
  public List<LedgerDTO> selectList(ComParam param) {
    return legMapper.selectList(param);
  }
  
}
