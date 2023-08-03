package egovframework.cmm.service;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import egovframework.cnf.service.Member;
import egovframework.rte.psl.dataaccess.mapper.Mapper;
import egovframework.sys.dto.PowerDTO;

@Mapper("LoginMapper")
public interface LoginMapper {

  /**
   * 일반 로그인을 처리한다
   * 
   * @param vo LoginVO
   * @return LoginVO
   * @exception Exception
   */
  public LoginVO actionLogin(LoginVO vo) throws Exception;

  /**
   * 마지막 로그인 정보를 저장한다
   * 
   * @param vo LoginVO
   * @return LoginVO
   * @exception Exception
   */
  public void updateLogin(LoginVO vo) throws Exception;

  /**
   * 비밀번호 실패 횟수를 저장한다
   * 
   * @param vo LoginVO
   * @return LoginVO
   * @exception Exception
   */
  public void updateLoginFailCnt(LoginVO vo) throws Exception;

  /**
   * 비밀번호 실패 횟수를 확인한다
   * 
   * @param vo LoginVO
   * @return LoginVO
   * @exception Exception
   */
  public LoginVO selectLoginFailCnt(LoginVO vo) throws Exception;

  /**
   * 아이디를 찾는다.
   * 
   * @param vo LoginVO
   * @return LoginVO
   * @exception Exception
   */
  public LoginVO searchId(LoginVO vo) throws Exception;


  /**
   * 계정을 잠근다.
   * 
   * @param loginVO
   * @return
   */
  public void lockLogin(LoginVO loginVO);

  /**
   * 계정잠금해제
   * 
   * @param id
   * @return
   */
  public void clearLock(Member mem);

  /**
   * 메뉴별 권한 얻기
   * 
   * @param authCode
   * @return
   */
  public List<PowerDTO> getAuthList(@Param("authCode") String authCode);
}
