package egovframework.cns.service.impl;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import egovframework.cmm.service.ComParam;
import egovframework.cns.service.CnsDTO.Req;
import egovframework.cns.service.CnsDTO.Res;
import egovframework.cns.service.CnsMapper;
import egovframework.cns.service.CnsService;
import egovframework.rte.fdl.cmmn.EgovAbstractServiceImpl;

@Service("CnsService")
public class CnsServiceImpl extends EgovAbstractServiceImpl implements CnsService {

  @Autowired
  private CnsMapper cnsMapper;

  private static final Logger LOGGER = LoggerFactory.getLogger(CnsServiceImpl.class);

  @Override
  public List<Res> selectList(ComParam param) throws Exception {

    List<Res> list = cnsMapper.selectList(param);

    return list;
  }

  @Override
  @Transactional
  public boolean insert(Req cns) throws Exception {

    boolean result = true;

    // 상담서 기본정보
    cnsMapper.insert(cns);

    if (StringUtils.isEmpty(cns.getSbkId()) && StringUtils.isEmpty(cns.getQuoId())) {
      // 상담서 내역
      cnsMapper.insertMemo(cns);
      // 업무서 공통 정보
      cnsMapper.insertJob(cns);
    } else
      cnsMapper.updateJobCns(cns);

    return result;
  }

  @Override
  public Res selectDetail(int cnsSeq) throws Exception {

    Res detail = cnsMapper.selectDetail(cnsSeq);
    return detail;
  }

  @Override
  @Transactional
  public boolean update(Req cns) throws Exception {
    boolean result = true;

    // 상담서 기본정보
    cnsMapper.update(cns);
    // 업무서 공통 정보
    cnsMapper.updateJob(cns);
    if (!StringUtils.isEmpty(cns.getMemo())) {
      // 상담서 내역
      cnsMapper.insertMemo(cns);
    }
    return result;
  }

  @Override
  public int selectListCnt(ComParam param) {
    return cnsMapper.selectListCnt(param);
  }

  @Override
  public Integer checkDetail(Req req) {
    Integer detail = cnsMapper.checkDetail(req);
    return detail;
  }
}
