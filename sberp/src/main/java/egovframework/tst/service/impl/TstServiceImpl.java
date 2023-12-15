package egovframework.tst.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import egovframework.cmm.service.ComParam;
import egovframework.sbk.service.SbkDTO;
import egovframework.sys.service.TestStndr;
import egovframework.tst.dto.DebugDTO;
import egovframework.tst.dto.TestDTO.Req;
import egovframework.tst.dto.TestDTO.Res;
import egovframework.tst.service.DbgMapper;
import egovframework.tst.service.Test;
import egovframework.tst.service.TestCate;
import egovframework.tst.service.TstMapper;
import egovframework.tst.service.TstParam;
import egovframework.tst.service.TstService;

@Service("TstService")
public class TstServiceImpl implements TstService {

  @Autowired
  TstMapper tstMapper;

  @Autowired
  DbgMapper dbgMapper;

  @Override
  public List<TestCate> selectCrtfList(int topCode) {
    return tstMapper.selectCrtfList(topCode);
  }

  @Override
  public List<TestStndr> selectStndrList(TstParam param) {
    return tstMapper.selectStndrList(param);
  }

  @Override
  public Test selectDetail(Req req) {
    return tstMapper.selectDetail(req);
  }

  @Override
  public boolean insert(Req req) {
    return tstMapper.insert(req);
  }

  @Override
  public int selectListCnt(ComParam param) {
    return tstMapper.selectListCnt(param);
  }

  @Override
  public List<Res> selectList(ComParam param) {
    List<Res> result = tstMapper.selectList(param);
    
    // 번호 매기기
    for (int i=0; i<result.size(); i++) {
      result.get(i).setNo(param.getTotalCount() - ( ((param.getPageIndex() - 1) * param.getPageUnit()) + i));
    }
    
    return result; 
  }

  @Override
  public boolean testMemInsert(Req req) {
    return tstMapper.testMemInsert(req);
  }

  @Override
  public List<Res> testMemList(String testSeq) {
    return tstMapper.testMemList(testSeq);
  }

  @Override
  @Transactional
  public boolean testStateInsert(Req req) {
    boolean result = true;

    tstMapper.testStateInsert(req);

    // 디버깅 접수시 디버깅 리스트에 추가
    if ("3".equals(req.getStateCode())) {
      DebugDTO dto = new DebugDTO();
      dto.setTestStateSeq(req.getTestStateSeq());
      dto.setMemo(req.getMemo());
      dto.setInsMemId(req.getInsMemId());
      dbgMapper.insert(dto);
    }
    
    // (#18) 시험상태 변경시, 시험테이블에 최신상태 SEQ 업데이트
    tstMapper.testStateUpdate(req);

    return result;
  }

  @Override
  public List<Res> testStateList(String testSeq) {
    return tstMapper.testStateList(testSeq);
  }

  @Override
  public boolean testBoardInsert(Req req) {
    return tstMapper.testBoardInsert(req);
  }

  @Override
  public List<Res> testBoardList(String testSeq) {
    return tstMapper.testBoardList(testSeq);
  }

  @Override
  public SbkDTO.Res testBoardAppDetail(String sbkId) {
    return tstMapper.testBoardAppDetail(sbkId);
  }

  @Override
  public boolean update(Req req) {
    return tstMapper.update(req);
  }

  @Override
  public List<Res> selectSaleList(ComParam param) {
    
    List<Res> result = tstMapper.selectSaleList(param);
    // 페이징
    List<Res> page = result.stream().skip(param.getFirstIndex()).limit(param.getPageUnit()).collect(Collectors.toList());
    
    // 번호 매기기
    for (int i=0; i<page.size(); i++) {
      page.get(i).setNo(param.getTotalCount() - ( ((param.getPageIndex() - 1) * param.getPageUnit()) + i));
    }
    return page;
  }

  @Override
  public int selectSaleListCnt(ComParam param) {
    return tstMapper.selectSaleListCnt(param);
  }

}
