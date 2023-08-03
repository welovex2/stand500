package egovframework.cmm.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import egovframework.cmm.service.EgovLoginService;
import egovframework.cmm.service.LoginMapper;
import egovframework.cmm.service.LoginVO;
import egovframework.cmm.util.EgovFileScrty;
import egovframework.cmm.util.EgovNumberUtil;
import egovframework.cmm.util.EgovStringUtil;
import egovframework.cnf.service.Member;
import egovframework.rte.fdl.cmmn.EgovAbstractServiceImpl;
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
@Service("loginService")
public class EgovLoginServiceImpl extends EgovAbstractServiceImpl implements EgovLoginService {

  @Autowired
  private LoginMapper loginMapper;

  /**
   * 일반 로그인을 처리한다
   * 
   * @param vo LoginVO
   * @return LoginVO
   * @exception Exception
   */
  @Override
  public LoginVO actionLogin(LoginVO vo) throws Exception {

    // 1. 입력한 비밀번호를 암호화한다.
    String enpassword = EgovFileScrty.encryptPassword(vo.getPassword(), vo.getId());
    vo.setPassword(enpassword);

    // 2. 아이디와 암호화된 비밀번호가 DB와 일치하는지 확인한다.
    LoginVO loginVO = loginMapper.actionLogin(vo);

    // 3. 결과를 리턴한다.
    if (loginVO != null && !loginVO.getId().equals("")) {
      // 4. 마지막 로그인 정보를 기록한다.
      loginMapper.updateLogin(vo);
      return loginVO;
    } else {
      // 5. 로그인 실패시 실패횟수 기록한다.
      loginMapper.updateLoginFailCnt(vo);
      loginVO = null;
    }

    return loginVO;
  }

  /**
   * 아이디를 찾는다.
   * 
   * @param vo LoginVO
   * @return LoginVO
   * @exception Exception
   */
  @Override
  public LoginVO searchId(LoginVO vo) throws Exception {

    // 1. 이름, 이메일주소가 DB와 일치하는 사용자 ID를 조회한다.
    LoginVO loginVO = loginMapper.searchId(vo);

    // 2. 결과를 리턴한다.
    if (loginVO != null && !loginVO.getId().equals("")) {
      return loginVO;
    } 

    return loginVO;
  }

  /**
   * 비밀번호를 찾는다.
   * 
   * @param vo LoginVO
   * @return boolean
   * @exception Exception
   */
//  @Override
//  public boolean searchPassword(LoginVO vo) throws Exception {
//
//    boolean result = true;
//
//    // 1. 아이디, 이름, 이메일주소, 비밀번호 힌트, 비밀번호 정답이 DB와 일치하는 사용자 Password를 조회한다.
//    LoginVO loginVO = loginMapper.searchPassword(vo);
//    if (loginVO == null || loginVO.getPassword() == null || loginVO.getPassword().equals("")) {
//      return false;
//    }
//
//    // 2. 임시 비밀번호를 생성한다.(영+영+숫+영+영+숫=6자리)
//    String newpassword = "";
//    for (int i = 1; i <= 6; i++) {
//      // 영자
//      if (i % 3 != 0) {
//        newpassword += EgovStringUtil.getRandomStr('a', 'z');
//        // 숫자
//      } else {
//        newpassword += EgovNumberUtil.getRandomNum(0, 9);
//      }
//    }
//
//    // 3. 임시 비밀번호를 암호화하여 DB에 저장한다.
//    LoginVO pwVO = new LoginVO();
//    String enpassword = EgovFileScrty.encryptPassword(newpassword, vo.getId());
//    pwVO.setId(vo.getId());
//    pwVO.setPassword(enpassword);
//    // pwVO.setUserSe(vo.getUserSe());
//    loginMapper.updatePassword(pwVO);
//
//    return result;
//  }
  
  /**
   * 비밀번호 실패 횟수를 확인한다
   * 
   * @param vo LoginVO
   * @return boolean
   * @exception Exception
   */
  @Override
  public LoginVO selectLoginFailCnt(LoginVO vo) throws Exception {
    return loginMapper.selectLoginFailCnt(vo);
  }

  /**
   * 계정을 잠근다
   */
  @Override
  public void lockLogin(LoginVO loginVO) {
    loginMapper.lockLogin(loginVO);
  }

  /**
   * 계정잠금 해제
   */
  @Override
  public void clearLock(Member mem) {
    loginMapper.clearLock(mem);
  }

  /**
   * 메뉴별 권한 얻기
   */
  @Override
  public List<PowerDTO> getAuthList(String authCode) {
    return loginMapper.getAuthList(authCode);
  }
  
}
