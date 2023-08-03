package egovframework.sys.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import egovframework.cmm.service.ComParam;
import egovframework.sys.service.StbMapper;
import egovframework.sys.service.StbService;
import egovframework.sys.service.TestStndrDTO;
import egovframework.tst.dto.TestCateDTO;

@Service("StbService")
public class StbServiceImpl implements StbService {

  @Autowired
  StbMapper stbMapper;

  @Override
  public List<TestStndrDTO> selectList(ComParam param) {
    return stbMapper.selectList(param);
  }

  @Override
  public int selectListCnt(ComParam param) {
    return stbMapper.selectListCnt(param);
  }

  @Override
  @Transactional
  public boolean insert(List<TestStndrDTO> list) {

    for (TestStndrDTO req : list) {
      if (!"D".equals(req.getState()))
        req.setState("U");
      stbMapper.insert(req);
    }
    return true;
  }

  @Override
  public boolean update(TestStndrDTO req) {

    if (!"D".equals(req.getState()))
      req.setState("U");
    stbMapper.insert(req);
    return true;
  }

  @Override
  public boolean insertCate(TestCateDTO req) {
    
    // 국가 직접입력
    if (req.getDepth1Seq() == 0 &&  !StringUtils.isEmpty(req.getDepth1Text())) { 
      req.setDepth(1);
      req.setName(req.getDepth1Text());
      req.setTopDepthSeq(0);
      stbMapper.insertCate(req);
    }
    
    // 인증종류1 직접입력
    if (req.getDepth2Seq() == 0 &&  !StringUtils.isEmpty(req.getDepth2Text())) { 
      req.setDepth(2);
      req.setName(req.getDepth2Text());
      req.setTopDepthSeq(req.getDepth1Seq() > 0 ? req.getDepth1Seq() : req.getTestCateSeq());
      req.setTestCateSeq(0);
      stbMapper.insertCate(req);
    }
 
    // 인증종류2 직접입력
    if (req.getDepth3Seq() == 0 &&  !StringUtils.isEmpty(req.getDepth3Text())) { 
      req.setDepth(3);
      req.setName(req.getDepth3Text());
      req.setTopDepthSeq(req.getDepth2Seq() > 0 ? req.getDepth2Seq() : req.getTestCateSeq());
      req.setTestCateSeq(0);
      stbMapper.insertCate(req);
    }

    // 인증종류3 직접입력
    if (req.getDepth4Seq() == 0 &&  !StringUtils.isEmpty(req.getDepth4Text())) { 
      req.setDepth(4);
      req.setName(req.getDepth4Text());
      req.setTopDepthSeq(req.getDepth3Seq() > 0 ? req.getDepth3Seq() : req.getTestCateSeq());
      req.setTestCateSeq(0);
      stbMapper.insertCate(req);
    }
    
    // 삭제
    if ("D".equals(req.getState())) {
      stbMapper.insertCate(req);
    }
    
    return true;
  }

  @Override
  public List<TestCateDTO> selectCateList(ComParam param) {
    return stbMapper.selectCateList(param);
  }

  @Override
  public int selectCateListCnt(ComParam param) {
    return stbMapper.selectCateListCnt(param);
  }

}
