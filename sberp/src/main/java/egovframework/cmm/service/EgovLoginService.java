package egovframework.cmm.service;

import java.util.List;
import egovframework.cnf.service.Member;
import egovframework.sys.dto.PowerDTO;

/**
 * 일반 로그인을 처리하는 비즈니스 구현 클래스
 * 
 * @author 공통서비스 개발팀 박지욱
 * @since 2009.03.06
 * @version 1.0
 * @see
 *
 *      <pre>
 * << 개정이력(Modification Information) >>
 *
 *   수정일      수정자          수정내용
 *  -------    --------    ---------------------------
 *  2009.03.06  박지욱          최초 생성
 *  2011.08.31  JJY            경량환경 템플릿 커스터마이징버전 생성
 *
 *      </pre>
 */
public interface EgovLoginService {

  /**
   * 일반 로그인을 처리한다
   * 
   * @return LoginVO
   *
   * @param vo LoginVO
   * @exception Exception Exception
   */
  public LoginVO actionLogin(LoginVO vo) throws Exception;

  /**
   * 아이디를 찾는다.
   * 
   * @return LoginVO
   *
   * @param vo LoginVO
   * @exception Exception Exception
   */
  public LoginVO searchId(LoginVO vo) throws Exception;

  /**
   * 비밀번호를 찾는다.
   * 
   * @return boolean
   *
   * @param vo LoginVO
   * @exception Exception Exception
   */
//  public boolean searchPassword(LoginVO vo) throws Exception;

  /**
   * 비밀번호 실패 횟수를 확인한다
   * 
   * @param vo LoginVO
   * @return boolean
   * @exception Exception
   */
  public LoginVO selectLoginFailCnt(LoginVO vo) throws Exception;

  /**
   * 계정을 잠근다
   * 
   * @param vo LoginVO
   * @return boolean
   * @exception Exception
   */
  public void lockLogin(LoginVO loginVO);

  /**
   * 계정잠금 해제
   * 
   * @param mem
   */
  public void clearLock(Member mem);

  /**
   * 메뉴별 권한정보 얻기
   * @param authCode
   * @return
   */
  public List<PowerDTO> getAuthList(String authCode);


}
