package egovframework.cnf.service;

import java.util.List;
import egovframework.cmm.service.ComParam;
import egovframework.cmm.service.Dept;
import egovframework.cmm.service.LoginVO;
import egovframework.cmm.service.Pos;

public interface MemService {

  List<Member> selectList(ComParam param);

  int selectListCnt(ComParam param);

  boolean insert(Member req);

  Member detail(int cmpySeq);


  /**
   * 비밀번호 변경
   * 
   * @param loginVO
   * @return 
   * @throws Exception 
   */
  boolean updatePassword(LoginVO loginVO) throws Exception;
  
  List<Dept> selectDeptList(ComParam param);

  boolean insertDept(Dept req);
  
  List<Pos> selectPosList(ComParam param);

  boolean insertPos(Pos req);

  boolean checkId(Member req);
}
