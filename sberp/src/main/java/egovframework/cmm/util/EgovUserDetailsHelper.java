package egovframework.cmm.util;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import egovframework.cmm.service.LoginVO;
import egovframework.rte.fdl.string.EgovObjectUtil;
import egovframework.sys.dto.PowerDTO;

/**
 * EgovUserDetails Helper 클래스
 * 
 * @author sjyoon
 * @since 2009.06.01
 * @version 1.0
 * @see
 *
 *      <pre>
 * << 개정이력(Modification Information) >>
 *   
 *   수정일      수정자           수정내용
 *  -------    -------------    ----------------------
 *   2009.03.10  sjyoon    최초 생성
 *   2011.08.31  JJY            경량환경 템플릿 커스터마이징버전 생성
 *
 *      </pre>
 */

public class EgovUserDetailsHelper {

  /**
   * 인증된 사용자객체를 VO형식으로 가져온다.
   * 
   * @return Object - 사용자 ValueObject
   */
  public static Object getAuthenticatedUser() {
    return (LoginVO) RequestContextHolder.getRequestAttributes().getAttribute("LoginVO",
        RequestAttributes.SCOPE_SESSION);

  }

  /**
   * 인증된 사용자의 권한 정보를 가져온다. 예) [ROLE_ADMIN, ROLE_USER, ROLE_A, ROLE_B, ROLE_RESTRICTED,
   * IS_AUTHENTICATED_FULLY, IS_AUTHENTICATED_REMEMBERED, IS_AUTHENTICATED_ANONYMOUSLY]
   * 
   * @return List - 사용자 권한정보 목록
   */
  public static List<String> getAuthorities() {
    List<String> listAuth = new ArrayList<String>();

    if (EgovObjectUtil.isNull((LoginVO) RequestContextHolder.getRequestAttributes()
        .getAttribute("LoginVO", RequestAttributes.SCOPE_SESSION))) {
      // log.debug("## authentication object is null!!");
      return null;
    }

    return listAuth;
  }

  /**
   * 인증된 사용자 여부를 체크한다.
   * 
   * @return Boolean - 인증된 사용자 여부(TRUE / FALSE)
   */
  public static Boolean isAuthenticated() {
    
    LoginVO loginVO = (LoginVO) RequestContextHolder.getRequestAttributes().getAttribute("LoginVO", RequestAttributes.SCOPE_SESSION);
    
    HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    String thisUrl = req.getRequestURI();
    
    System.out.println("///////////////////////////////////////////");
    System.out.println(req.getRequestURI());

    if (EgovObjectUtil.isNull(loginVO)) {
      return Boolean.FALSE;
    }
    
    List<PowerDTO> power = loginVO.getPower();
    
    for (PowerDTO p : power) {
      
      // 현재 나의 페이지의 권한 확인
      if (thisUrl.indexOf(p.getMenuCode()) > -1) {
        
        System.out.println("누구="+loginVO.getEmpName());
        System.out.println("체크URL="+p.getMenuCode());
        System.out.println("읽기권한="+p.isRYn());
        System.out.println("쓰기권한="+p.isWYn());
        System.out.println("///////////////////////////////////////////");

        if (!p.isRYn() && (thisUrl.toLowerCase().indexOf("list") > -1 
                            || thisUrl.toLowerCase().indexOf("detail") > -1)
                            || thisUrl.toLowerCase().indexOf("excel") > -1) {
          System.out.println("R 권한없음");
          return Boolean.FALSE;
        }
        else if (!p.isWYn() && thisUrl.toLowerCase().indexOf("insert") > -1) {
          System.out.println("W 권한없음");
          return Boolean.FALSE;
        }
      }
    }
    
    return Boolean.TRUE;
  }
}
