package egovframework.rsb.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import egovframework.quo.service.QuoMapper;
import egovframework.rsb.RsbDTO;
import egovframework.rsb.RsbDTO.Req;
import egovframework.rsb.service.RsbMapper;
import egovframework.rsb.service.RsbService;

@Service("RsbService")
public class RsbServiceImpl implements RsbService {

  @Autowired
  QuoMapper quoMapper;
  
  @Autowired
  RsbMapper rsbMapper;
  
  @Override
  @Transactional
  public boolean insert(RsbDTO.Req req) {
 
    // SBK_TB 등록
    rsbMapper.copySbk(req);
    // QUO_TB 등록
    rsbMapper.copyQuo(req);
    // JOB_TB 등록
    rsbMapper.copyJob(req);
    // 시험항목 등록
    rsbMapper.copyTestItem(req);

    return true;
  }

  @Override
  public int selectMaxRevision(Req req) {
    return rsbMapper.selectMaxRevision(req);
  }

}
