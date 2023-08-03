package egovframework.tst.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import egovframework.cmm.service.ComParam;
import egovframework.tst.dto.DebugDTO;
import egovframework.tst.dto.TestDTO.Req;
import egovframework.tst.service.DbgMapper;
import egovframework.tst.service.DbgService;
import egovframework.tst.service.DebugMemo;
import egovframework.tst.service.TstMapper;

@Service("DbgService")
public class DbgServiceImpl implements DbgService {

  @Autowired
  DbgMapper dbgMapper;

  @Autowired
  TstMapper tstMapper;
  
  @Override
  @Transactional
  public boolean update(DebugDTO req) {
    boolean result = true;
    // 디버깅 최종결과
    dbgMapper.update(req);
    
    // 최종결과 나오면 시험에도 업데이트
    Req tst = new Req();
    tst.setTestSeq(dbgMapper.selectDetail(req.getDebugSeq()));
    tst.setStateCode("8");  // 디버깅 완료코드
    tst.setMemo(req.getEtc());
    tst.setInsMemId(req.getInsMemId());
    tst.setUdtMemId(req.getUdtMemId());
    tstMapper.testStateInsert(tst);
    
    return result;
  }

  @Override
  public boolean insert(DebugDTO req) {
    return dbgMapper.insert(req);
  }

  @Override
  public int selectListCnt(ComParam param) {
    return dbgMapper.selectListCnt(param);
  }

  @Override
  public List<DebugDTO> selectList(ComParam param) {
    return dbgMapper.selectList(param);
  }

  @Override
  public boolean debugStateInsert(DebugDTO req) {
    return dbgMapper.debugStateInsert(req);
  }

  @Override
  public List<DebugDTO> debugStateList(String debugSeq) {
    return dbgMapper.debugStateList(debugSeq);
  }

  @Override
  public boolean debugBoardInsert(DebugMemo req) {
    return dbgMapper.debugBoardInsert(req);
  }

  @Override
  public List<DebugMemo> debugBoardList(String debugSeq) {
    return dbgMapper.debugBoardList(debugSeq);
  }

}
