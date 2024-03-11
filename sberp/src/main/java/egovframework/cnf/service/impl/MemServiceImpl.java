package egovframework.cnf.service.impl;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import egovframework.cmm.service.ComParam;
import egovframework.cmm.service.Dept;
import egovframework.cmm.service.LoginVO;
import egovframework.cmm.service.Pos;
import egovframework.cmm.util.EgovFileScrty;
import egovframework.cnf.service.MemMapper;
import egovframework.cnf.service.MemService;
import egovframework.cnf.service.Member;

@Service("MemService")
public class MemServiceImpl implements MemService {

  @Autowired
  MemMapper memMapper;

  private static final Logger LOGGER = LoggerFactory.getLogger(MemServiceImpl.class);

  @Override
  public List<Member> selectList(ComParam param) {
    return memMapper.selectList(param);
  }

  @Override
  public int selectListCnt(ComParam param) {
    return memMapper.selectListCnt(param);
  }

  @Override
  @Transactional
  public boolean insert(Member req) {

    memMapper.insert(req);
    
    // 서명파일 저장
    if (!StringUtils.isEmpty(req.getAtchFileId())) {
      memMapper.insertSign(req);
    }
    
    return true;
  }

  @Override
  public Member detail(int cmpySeq) {
    Member detail = memMapper.detail(cmpySeq);
    return detail;
  }


  /**
   * 비밀번호 변경
   * 
   * @throws Exception
   */
  @Override
  public boolean updatePassword(LoginVO vo) throws Exception {
    boolean result = true;

    // 현재 비밀번호 확인
    vo.setPassword(EgovFileScrty.encryptPassword(vo.getPassword(), vo.getId()));
    String pass = memMapper.searchPassword(vo);
    if (pass == null || "".equals(pass)) {
      return false;
    }

    // 변경된 비밀번호 저장
    vo.setPassword(EgovFileScrty.encryptPassword(vo.getNewPassword(), vo.getId()));
    memMapper.updatePassword(vo);

    return result;
  }

  @Override
  public List<Dept> selectDeptList(ComParam param) {
    return memMapper.selectDeptList(param);
  }

  @Override
  public boolean insertDept(Dept req) {

    if (!"D".equals(req.getState()))
      req.setState("U");
    else 
      req.setName("");
    
    return memMapper.insertDept(req);
  }

  @Override
  public List<Pos> selectPosList(ComParam param) {
    return memMapper.selectPosList(param);
  }

  @Override
  public boolean insertPos(Pos req) {

    if (!"D".equals(req.getState()))
      req.setState("U");
    else 
      req.setName("");
    
    return memMapper.insertPos(req);
  }

  @Override
  public boolean checkId(Member req) {
    return memMapper.checkId(req);
  }

}
