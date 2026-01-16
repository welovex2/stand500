package egovframework.sts.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import egovframework.cmm.service.ComParam;
import egovframework.sts.dto.SmdDTO;
import egovframework.sts.dto.Target;
import egovframework.sts.service.SmdMapper;
import egovframework.sts.service.SmdService;

@Service("SmdService")
public class SmdServiceImpl implements SmdService {

  @Autowired
  SmdMapper smdMapper;

  @Override
  public List<SmdDTO> selectSaleList(ComParam param) {
    
    List<SmdDTO> result = new ArrayList<SmdDTO>();
    
    // 개별 데이터
    param.setSearchCode("sg");
    SmdDTO item = new SmdDTO();
    item.setGubun("sg");
    item.setMemState(smdMapper.selectSaleList(param));
    result.add(item);
    
    // 팀별 데이터 - 2025-01 삭제
//    param.setSearchCode("tm");
//    item = new SmdDTO();
//    item.setGubun("tm");
//    item.setMemState(smdMapper.selectSaleList(param));
//    result.add(item);
    
    // 합산
    param.setSearchCode("sm");
    item = new SmdDTO();
    item.setGubun("sm");
    item.setMemState(smdMapper.selectSaleList(param));
    result.add(item);
    
    return result;
  }

  @Override
  public boolean insert(List<Target> list) {
    return smdMapper.insert(list);
  }
    
}
