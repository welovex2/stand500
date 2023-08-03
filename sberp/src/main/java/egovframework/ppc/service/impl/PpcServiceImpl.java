package egovframework.ppc.service.impl;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import egovframework.cmm.service.ComParam;
import egovframework.ppc.dto.PpDTO;
import egovframework.ppc.service.PpHis;
import egovframework.ppc.service.PpcMapper;
import egovframework.ppc.service.PpcService;
import egovframework.rte.fdl.cmmn.EgovAbstractServiceImpl;

@Service("PpcService")
public class PpcServiceImpl extends EgovAbstractServiceImpl implements PpcService {

  @Autowired
  private PpcMapper ppcMapper;

  private static final Logger LOGGER = LoggerFactory.getLogger(PpcServiceImpl.class);

  @Override
  public List<PpDTO> selectList(ComParam param) throws Exception {

    List<PpDTO> list = ppcMapper.selectList(param);

    return list;
  }

  @Override
  @Transactional
  public boolean insert(PpDTO pp) throws Exception {

    boolean result = true;

    // 상담서 기본정보
    ppcMapper.insert(pp);

    // 상담서 내역
    PpHis his = new PpHis();
    his.setPpSeq(pp.getPpSeq());
    his.setMemo(pp.getMemo());
    his.setInsMemId(pp.getInsMemId());
    ppcMapper.insertMemo(his);

    return result;
  }

  @Override
  public PpDTO selectDetail(String ppId) throws Exception {

    PpDTO detail = ppcMapper.selectDetail(ppId);
    
    if (detail != null) {
      detail.setMemoList(ppcMapper.selectMemoList(ppId));
    }
    return detail;
  }

  @Override
  @Transactional
  public boolean update(PpDTO pp) throws Exception {
    boolean result = true;

    // 상담서 기본정보
    ppcMapper.update(pp);
    if (!StringUtils.isEmpty(pp.getMemo())) {
      // 상담서 내역
      PpHis his = new PpHis();
      his.setPpId(pp.getPpId());
      his.setMemo(pp.getMemo());
      his.setInsMemId(pp.getInsMemId());
      ppcMapper.insertMemo(his);
    }
    return result;
  }

  @Override
  public int selectListCnt(ComParam param) {
    return ppcMapper.selectListCnt(param);
  }

}
