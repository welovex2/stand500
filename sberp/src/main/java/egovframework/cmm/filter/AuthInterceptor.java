package egovframework.cmm.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ModelAndViewDefiningException;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import egovframework.cmm.service.LoginVO;
import egovframework.cmm.service.ResponseMessage;
import egovframework.cmm.util.EgovUserDetailsHelper;

public class AuthInterceptor extends HandlerInterceptorAdapter {
    /**
     * 세션에 계정정보(LoginVO)가 있는지 여부로 인증 여부를 체크한다. 계정정보(LoginVO)가 없다면, 로그인 페이지로 이동한다.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LoginVO loginVO = null;
       
        try {
            loginVO = (LoginVO) request.getSession().getAttribute("LoginVO");
            
            Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
            if (isAuthenticated) {
            	
            }
            
//            ServletInputStream inputStream = request.getInputStream();
//            String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
//            System.out.println(messageBody);
//            UserData data = objectMapper.readValue(messageBody, UserData.class);
//            log.info("username={}, age={}", data.getUsername(), data.getAge());
//            return true;
            if (loginVO != null && loginVO.getId() != null) {
                return true;
            } else {
            	System.out.println(ResponseMessage.NO_LOGIN);
            	response.setContentType("application/json");
            	response.setCharacterEncoding("UTF-8");
            	response.getWriter().write("{\"result\":false,\"data\":null,\"message\":\"".concat(ResponseMessage.NO_LOGIN).concat("\"}"));
            	return false;
            }
        } catch (Exception e) {
            ModelAndView modelAndView = new ModelAndView("forward:/loginView.do");
            modelAndView.addObject("message", "세션이 만료되어 로그아웃 되었습니다. 다시 로그인 해주세요.");
            throw new ModelAndViewDefiningException(modelAndView);
        }
    }

    /**
     * 세션에 메뉴권한(LoginVO.userGubun)을 가지고 메뉴를 조회하여 권한 여부를 체크한다.
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        LoginVO loginVO = null;
        String requestURI = request.getRequestURI();
       
        try {
         if(!"/loginView.do".equals(requestURI)) {
	          loginVO = (LoginVO) request.getSession().getAttribute("loginVO");
	          
//	          if (loginVO != null && loginVO.getUserId() != null) {
//	           SearchVO searchVO = new SearchVO();
//	           searchVO.setSchMenuUrl(requestURI);
//	           searchVO.setSchUserGubun(loginVO.getUserGubun());
//	           List<?> resultList = sysadmService.getSelectRollMenuCheckList(searchVO);
	           
//	           if(resultList == null || resultList.size() == 0) {
//	                     ModelAndView mav = new ModelAndView("forward:/monitorView.do"); 
//	                     mav.addObject("message", "권한이 없습니다.");
//	                     throw new ModelAndViewDefiningException(mav);
//	           }
	           
//	          } else {
//	           ModelAndView mav = new ModelAndView("forward:/loginView.do");
//	           mav.addObject("message", "세션이 만료되어 로그아웃 되었습니다. 다시 로그인 해주세요.");
//	           throw new ModelAndViewDefiningException(mav);
//	          }
         }
        } catch (Exception e) {
            ModelAndView mav = new ModelAndView("forward:/monitorView.do"); 
            mav.addObject("message", "권한이 없습니다.");
            throw new ModelAndViewDefiningException(mav);
        }    }
}
