package egovframework.cnf.service;

import java.util.List;
import egovframework.cmm.service.ComParam;
import egovframework.cmm.service.Dept;
import egovframework.cmm.service.LoginVO;
import egovframework.cmm.service.Pos;
import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("MemMapper")
public interface MemMapper {

  List<Member> selectList(ComParam param);

  int selectListCnt(ComParam param);

  boolean insert(Member req);

  Member detail(int cmpySeq);


  /**
   * 비밀번호를 찾는다.
   * 
   * @param vo LoginVO
   * @return LoginVO
   * @exception Exception
   */
  public String searchPassword(LoginVO vo) throws Exception;

  /**
   * 변경된 비밀번호를 저장한다.
   * 
   * @param vo LoginVO
   * @exception Exception
   */
  public void updatePassword(LoginVO vo) throws Exception;

  List<Dept> selectDeptList(ComParam param);

  boolean insertDept(Dept req);
  
  List<Pos> selectPosList(ComParam param);

  boolean insertPos(Pos req);
  
  boolean checkId(Member req);
}
