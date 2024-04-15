package egovframework.sts.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import egovframework.cmm.service.ComParam;
import egovframework.sts.dto.TmdDTO;
import egovframework.sts.service.TmdMapper;
import egovframework.sts.service.TmdService;

@Service("TmdService")
public class TmdServiceImpl implements TmdService {

  @Autowired
  TmdMapper tmdMapper;

  @Override
  public List<TmdDTO> selectMemList(ComParam param) {
    return tmdMapper.selectMemList(param);
  }

  @Override
  public List<TmdDTO> selectMonList(ComParam param) {
    return tmdMapper.selectMonList(param);
  }
    
}
