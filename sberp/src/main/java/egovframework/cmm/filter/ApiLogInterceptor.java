package egovframework.cmm.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import egovframework.cmm.service.ApiLogMapper;
import egovframework.cmm.service.ApiLogVO;
import egovframework.cmm.service.LoginVO;
import egovframework.cmm.util.EgovUserDetailsHelper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApiLogInterceptor extends HandlerInterceptorAdapter {

  @Autowired
  private ApiLogMapper apiLogMapper; // DB 저장을 위한 Mapper

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
      
      // 1. 정보 추출
      String requestURI = request.getRequestURI();
      String method = request.getMethod();
      String remoteIP = request.getRemoteAddr();
      
      // 2. 사용자 ID 추출
      String userId = "GUEST";
      if (EgovUserDetailsHelper.isAuthenticated()) {
          LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
          userId = user.getId();
      }

      // 3. DB 저장 (객체 생성 및 호출)
      ApiLogVO logVO = new ApiLogVO();
      logVO.setUserId(userId);
      logVO.setRequestUri(requestURI);
      logVO.setMethod(method);
      logVO.setRemoteIp(remoteIP);

      try {
          apiLogMapper.insertApiLog(logVO);
      } catch (Exception e) {
          // 로그 저장 실패가 실제 서비스에 영향을 주지 않도록 예외 처리
          System.err.println("API Log 저장 실패: " + e.getMessage());
      }

      return true; 
  }
}