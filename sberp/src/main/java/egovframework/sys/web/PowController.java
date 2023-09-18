package egovframework.sys.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import egovframework.cmm.service.BasicResponse;
import egovframework.cmm.service.ComParam;
import egovframework.cmm.service.LoginVO;
import egovframework.cmm.service.ResponseMessage;
import egovframework.cmm.service.SearchVO;
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.rte.fdl.property.EgovPropertyService;
import egovframework.sys.service.PowService;
import egovframework.sys.service.Power;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags = {"계정권한"})
@RestController
@RequestMapping("/sys/pow")
public class PowController {

  @Resource(name = "PowService")
  private PowService powService;

  @Resource(name = "propertiesService")
  protected EgovPropertyService propertyService;

  @ApiOperation(value = "계정권한 설정", notes = "")
  @GetMapping(value = "/detail.do")
  public BasicResponse detail() throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<Power> list = new ArrayList<Power>();

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    if (!isAuthenticated) {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
      
      BasicResponse res =
          BasicResponse.builder().result(result).message(msg).build();

      return res;
    }
    
    list = powService.selectDetail();

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(list).build();

    return res;
  }

  @ApiOperation(value = "계정권한 저장", notes = "")
  @PostMapping(value = "/insert.do")
  public BasicResponse insert(
      @ApiParam(value = "", required = true, example = "") @RequestBody List<Power> list)
      throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";
    boolean result = false;
    
    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    if (!isAuthenticated) {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
      
      BasicResponse res =
          BasicResponse.builder().result(result).message(msg).build();

      return res;
    }
    
    for (Power req : list) {

      // 로그인정보
      req.setInsMemId(user.getId());
      req.setUdtMemId(user.getId());


      System.out.println("=-===========");
      System.out.println(req.toString());
      System.out.println("=-===========");

      ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
      Validator validator = validatorFactory.getValidator();

      Set<ConstraintViolation<Power>> violations = validator.validate(req);

      for (ConstraintViolation<Power> violation : violations) {
        msg = violation.getMessage();

        BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

        return res;
      }
    }

    result = powService.insert(list);
    BasicResponse res = BasicResponse.builder().result(result).build();

    return res;
  }

}
