package egovframework.sbk.web;

import javax.annotation.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import egovframework.cmm.service.BasicResponse;
import egovframework.cmm.service.LoginVO;
import egovframework.cmm.service.ResponseMessage;
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.rte.fdl.property.EgovPropertyService;
import egovframework.sbk.service.Sbd;
import egovframework.sbk.service.SbdService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Api(tags = {"신청서 민원서류"})
@RestController
@RequestMapping("/sbd")
public class SbdController {

  @Resource(name = "propertiesService")
  protected EgovPropertyService propertyService;
  
  @Resource(name = "SbdService")
  private SbdService sbdService;
  
  @ApiOperation(value = "대리인지정서 다운로드")
  @GetMapping(value = "/dri/{sbkId}/detail.do")
  public BasicResponse downloadDriExcel(
      @ApiParam(value = "신청서 고유번호", required = true, example = "SB23-G0044")
      @PathVariable(name = "sbkId") String sbkId) throws Exception {
  
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";
    boolean result = false;
    Sbd detail = null;
    
    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      
      // 1. 상세 데이터 조회
       detail = sbdService.selectDriDetail(sbkId);

    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
    }
  
    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();
    
    return res;
  }

  @ApiOperation(value = "부합증명서 다운로드")
  @GetMapping(value = "/bh/{sbkId}/detail.do")
  public BasicResponse downloadBhExcel(
      @ApiParam(value = "신청서 고유번호", required = true, example = "SB23-G0044")
      @PathVariable(name = "sbkId") String sbkId) throws Exception {
  
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";
    boolean result = false;
    Sbd detail = null;
    
    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      
      // 1. 상세 데이터 조회
       detail = sbdService.selectBhDetail(sbkId);

    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
    }
  
    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();
    
    return res;
  }

  @ApiOperation(value = "작합성평가 다운로드")
  @GetMapping(value = "/jh/{sbkId}/detail.do")
  public BasicResponse downloadJhExcel(
      @ApiParam(value = "신청서 고유번호", required = true, example = "SB23-G0044")
      @PathVariable(name = "sbkId") String sbkId) throws Exception {
  
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";
    boolean result = false;
    Sbd detail = null;
    
    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      
      // 1. 상세 데이터 조회
       detail = sbdService.selectJhDetail(sbkId);

    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
    }
  
    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();
    
    return res;
  }
  
  @ApiOperation(value = "부합증명서 저장", notes = "")
  @PostMapping(value = "/bh/{sbkId}/insert.do")
  public BasicResponse saveBh(      
      @PathVariable(name = "sbkId") String sbkId,
      @RequestBody Sbd req) throws Exception {
      LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
      boolean result = true;
      String msg = "";

      Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

      if (isAuthenticated) {
          // 등록/수정자 세팅
          if (req.getInsMemId() == null || req.getInsMemId().isEmpty()) {
              req.setInsMemId(user.getId());
          }
          req.setUdtMemId(user.getId());

          // 간단 유효성 체크 (필요하면 강화)
          if (StringUtils.isEmpty(sbkId)) {
              result = false;
              msg = "신청서번호를 확인하세요.";
          } else {
              req.setSbkId(sbkId);
              sbdService.insertBh(req);
          }
      } else {
          result = false;
          msg = ResponseMessage.UNAUTHORIZED;
      }

      BasicResponse res = BasicResponse.builder().result(result).message(msg).build();
      
      return res;
  }

  
  @ApiOperation(value = "적합성평가 저장", notes = "sbkYm, sbkSeq, sbkType, sbkRevision, cfType, cfDate")
  @PostMapping(value = "/jh/{sbkId}/insert.do")
  public BasicResponse saveJh(
      @PathVariable(name = "sbkId") String sbkId,
      @RequestBody Sbd req) throws Exception {
      LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
      boolean result = true;
      String msg = "";

      Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

      if (isAuthenticated) {
        // 간단 유효성 체크 (필요하면 강화)
        if (StringUtils.isEmpty(sbkId)) {
            result = false;
            msg = "신청서번호를 확인하세요.";
        } else {
            req.setSbkId(sbkId);
            sbdService.insertJh(req);
        }
      } else {
          result = false;
          msg = ResponseMessage.UNAUTHORIZED;
      }

      BasicResponse res = BasicResponse.builder().result(result).message(msg).build();
      
      return res;
  }
}
