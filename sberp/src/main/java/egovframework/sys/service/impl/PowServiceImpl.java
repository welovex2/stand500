package egovframework.sys.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import egovframework.cmm.service.ComParam;
import egovframework.sys.service.MachineDTO;
import egovframework.sys.service.PowMapper;
import egovframework.sys.service.PowService;
import egovframework.sys.service.Power;

@Service("PowService")
public class PowServiceImpl implements PowService {

  @Autowired
  PowMapper powMapper;

  @Override
  public List<Power> selectDetail() {
    return powMapper.selectDetail();
  }

  @Override
  public boolean insert(List<Power> list) {
    powMapper.insert(list);
    return true;
  }

}
