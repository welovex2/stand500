package egovframework.sys.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import egovframework.cmm.service.ComParam;
import egovframework.sys.service.MacMapper;
import egovframework.sys.service.MacService;
import egovframework.sys.service.MachineDTO;
import egovframework.sys.service.TestStndrDTO;

@Service("MacService")
public class MacServiceImpl implements MacService {

  @Autowired
  MacMapper macMapper;

  @Override
  public List<MachineDTO> selectList(ComParam param) {
    return macMapper.selectList(param);
  }

  @Override
  public boolean insert(List<MachineDTO> list) {
    for (MachineDTO req : list) {
      if (!"D".equals(req.getState()))
        req.setState("U");
      
      macMapper.insert(req);
    }
    return true;
  }

  @Override
  public boolean update(MachineDTO req) {
    if (!"D".equals(req.getState()))
      req.setState("U");
    else {
      req.setName("");
      req.setModel("");
      req.setMnfctSerial("");
    }
    macMapper.insert(req);
    return true;
  }

  @Override
  public boolean updateSub(String type, List<MachineDTO> list) {
    for (MachineDTO req : list) {
      req.setType(type);
      macMapper.update(req);
    }
    return true;
  }

}
