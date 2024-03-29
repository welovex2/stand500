package egovframework.cmm.web;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import egovframework.cmm.service.BasicResponse;
import egovframework.cmm.service.CmmService;
import egovframework.cmm.service.Job;
import egovframework.cmm.service.JobMngr;
import egovframework.cmm.service.JobVO;
import egovframework.cmm.service.LoginVO;
import egovframework.cmm.service.ResponseMessage;
import egovframework.cmm.util.EgovUserDetailsHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
  

  @ApiOperation(value = "프로젝트상태 일괄변경")
  @PostMapping(value = "/all/testStateInsert.do")
  public BasicResponse testStateAllInert(@ApiParam(value = "jobSeqs[], stateCode 전송") @RequestBody JobVO req)
      throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";

    // 로그인정보
    req.setInsMemId(user.getId());

    System.out.println("================");
    System.out.println(req);
    System.out.println("================");
    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    int success = 0;
    Job item = new Job();
    if (isAuthenticated) {

      // 2개 이상부터 취합 가능
      if (ObjectUtils.isEmpty(req.getJobSeqs())) {
        result = false;
        msg = ResponseMessage.CHECK_JOB_SEQ;
      }
      else {
        item.setStateCode(req.getStateCode());
        for (int d : req.getJobSeqs()) {
          
          item.setJobSeq(d);
          if (cmmService.jobStateUpdate(item)) success++;
          
        }
  
        msg = Integer.toString(success).concat("건 처리완료되었습니다.");
      }
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }
  
  @ApiOperation(value = "업무담당자 저장", notes = "jobSeq:프로젝트고유번호, mngId:업무담당자ID, memo:메모")
  @PostMapping(value = "/mngInsert.do")
  public BasicResponse mngInsert(@RequestBody JobMngr req) throws Exception {
    
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());

    boolean result = false;
    String msg = "";

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      result = cmmService.insertJobMng(req);
    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }

  @ApiOperation(value = "업무담당자 변경 내역")
  @GetMapping(value = "/testMemList.do")
  public BasicResponse testMemList(@ApiParam(value = "프로젝트고유번호", required = true, example = "1") 
                                   @RequestParam(value = "jobSeq") int jobSeq) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<Job> list = new ArrayList<Job>();

    list = cmmService.jobMngList(jobSeq);

    if (list == null) {
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(list).build();

    return res;
  }
  
}
