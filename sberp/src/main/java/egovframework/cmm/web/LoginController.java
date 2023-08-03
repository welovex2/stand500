package egovframework.cmm.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import egovframework.cmm.service.BasicResponse;
import egovframework.cmm.service.EgovLoginService;
import egovframework.cmm.service.LoginVO;
import egovframework.cmm.service.ResponseMessage;
import egovframework.cmm.util.EgovFileScrty;
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.cnf.service.Member;
import egovframework.sys.dto.PowerDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags = {"로그인"})
@RestController
@RequestMapping("/user/login")
public class LoginController {

  /** EgovLoginService */
  @Resource(name = "loginService")
  private EgovLoginService loginService;


  public static void main(String[] args) {

    try {
      
      /*
       * 
       * 
       *최재연 / cjy300 / 1054
        정윤진 / jyunj / 1466
        이영범 / zlklz / 1649
        오지연 / jaecie / 0292
        최서우 / gloria / 1465
        전지훈 / jeon / 1648
        
        김지영 / jykim / 1055
        이상희 / allo / 1382
       * */
      Map<String, String> mem = new HashMap<String, String>();
      mem.put("cjy300", "1054");
      mem.put("jyunj", "1466");
      mem.put("zlklz", "1649");
      mem.put("jaecie", "0292");
      mem.put("gloria", "1465");
      mem.put("jeon", "1648");
      mem.put("jykim", "1055");
      mem.put("allo", "1382");
      
      String enpassword = EgovFileScrty.encryptPassword("aa", "a");
      System.out.println("a>> " + enpassword);
      
      for ( String key : mem.keySet() ) {
        enpassword = EgovFileScrty.encryptPassword(mem.get(key), key);
        System.out.println(key+">> " + enpassword);
      }


    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }


  }

  @ApiOperation(value = "계정잠금해제")
  @PostMapping(value = "/{id}/lockClear.do")
  public BasicResponse actionLock(@ApiParam(value = "잠금해제아이디", required = true,
      example = "a") @PathVariable(name = "id") String id) throws Exception {

    boolean result = true;
    String msg = "";

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    if ("P01".equals(user.getAuthCode())) {
      
      // 기본 패스워드 설정
      Member mem = new Member();
      System.out.println("비번="+id.concat("300"));
      mem.setPassword(EgovFileScrty.encryptPassword(id.concat("300"), id));
      mem.setId(id);
      
      loginService.clearLock(mem);
      msg = ResponseMessage.UPDATE_USER;
    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
    }
    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }

  @ApiOperation(value = "로그인")
  @PostMapping(value = "/login.do")
  public BasicResponse actionLogin(@RequestBody LoginVO loginVO, HttpServletRequest request)
      throws Exception {

    boolean result = true;
    String msg = "";

    // 1. 일반 로그인 처리
    loginVO.setLastIp(getClientIP(request));
    LoginVO resultVO = loginService.actionLogin(loginVO);

    boolean loginPolicyYn = true;

    if (resultVO != null && resultVO.getId() != null && !resultVO.getId().equals("")
        && resultVO.getLockYn() == 0) {

      // 1-1. 권한정보 가져오기
      if (!StringUtils.isEmpty(resultVO.getAuthCode())) {
        List<PowerDTO> resultAuth = loginService.getAuthList(resultVO.getAuthCode());
        resultVO.setPower(resultAuth);
      }
      
      request.getSession().setAttribute("LoginVO", resultVO);
      msg = ResponseMessage.LOGIN_SUCCESS;
      result = true;

    } else {

      // 암호 실패 횟수 가져오기
      LoginVO failVO = loginService.selectLoginFailCnt(loginVO);

      if (failVO != null && failVO.getFailPassCnt() == 10) {
        // 계정잠금처리
        loginService.lockLogin(loginVO);
        msg = ResponseMessage.LOGIN_LOCK;
      } else if (failVO != null && failVO.getLockYn() == 1) {
        msg = ResponseMessage.LOGIN_LOCK;
      } else if (failVO == null) {
        msg = ResponseMessage.NOT_FOUND_USER;
      } else {
        msg = ResponseMessage.LOGIN_FAIL.concat("\r\n(암호 10회중 / 현재 ")
            .concat(Integer.toString(failVO.getFailPassCnt())).concat("회 오류)");
      }

      result = false;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }

  /**
   * 로그아웃한다.
   * 
   * @return String
   * @exception Exception
   */
  @GetMapping(value = "/logout.do")
  public boolean actionLogout(HttpServletRequest request, ModelMap model) throws Exception {

    RequestContextHolder.getRequestAttributes().removeAttribute("LoginVO",
        RequestAttributes.SCOPE_SESSION);

    return true;
  }

  @ApiOperation(value = "로그인사용자정보")
  @GetMapping(value = "/info.do")
  public BasicResponse userInfo(HttpServletRequest request) throws Exception {

    boolean result = true;
    String msg = "";

    LoginVO resultVO = (LoginVO) request.getSession().getAttribute("LoginVO");

    if (resultVO == null) {
      result = false;
      msg = ResponseMessage.NO_LOGIN;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(resultVO).build();

    return res;
  }

  public static String getClientIP(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    System.out.println("> X-FORWARDED-FOR : " + ip);

    if (ip == null) {
      ip = request.getHeader("Proxy-Client-IP");
      System.out.println("> Proxy-Client-IP : " + ip);
    }
    if (ip == null) {
      ip = request.getHeader("WL-Proxy-Client-IP");
      System.out.println(">  WL-Proxy-Client-IP : " + ip);
    }
    if (ip == null) {
      ip = request.getHeader("HTTP_CLIENT_IP");
      System.out.println("> HTTP_CLIENT_IP : " + ip);
    }
    if (ip == null) {
      ip = request.getHeader("HTTP_X_FORWARDED_FOR");
      System.out.println("> HTTP_X_FORWARDED_FOR : " + ip);
    }
    if (ip == null) {
      ip = request.getRemoteAddr();
      System.out.println("> getRemoteAddr : " + ip);
    }
    System.out.println("> Result : IP Address : " + ip);

    return ip;
  }

}
