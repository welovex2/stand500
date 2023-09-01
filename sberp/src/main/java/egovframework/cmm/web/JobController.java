package egovframework.cmm.web;

import javax.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import egovframework.cmm.service.BasicResponse;
import egovframework.cmm.service.CmmService;
import egovframework.cmm.service.Job;
import egovframework.cmm.service.LoginVO;
import egovframework.cmm.service.ResponseMessage;
import egovframework.cmm.util.EgovUserDetailsHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Api(tags = {"프로젝트"})
@RestController
@RequestMapping("/job")
public class JobController {

  @Resource(name = "CmmService")
  private CmmService cmmService;
  
  
  @ApiOperation(value = "프로젝트상태 변경", notes="jobSeq:프로젝트고유항목, 시험상태:공통코드(CP)")
  @PostMapping(value="/testStateInsert.do")
  public BasicResponse testStateInsert(@RequestBody Job req) throws Exception{
      LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();
      
      // 로그인정보
      req.setInsMemId(user.getId());
      req.setUdtMemId(user.getId());
      
      boolean result = false;
      String msg = "";
      
      Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
      
      if (isAuthenticated) {
          result = cmmService.jobStateUpdate(req);
      } else {
          result = false;
          msg = ResponseMessage.UNAUTHORIZED;
      }
      
      BasicResponse res = BasicResponse.builder().result(result)
              .message(msg)
              .build();
      
      return res;
  }
  
}
