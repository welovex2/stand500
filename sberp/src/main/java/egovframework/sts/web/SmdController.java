package egovframework.sts.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import egovframework.cmm.service.BasicResponse;
import egovframework.cmm.service.ComParam;
import egovframework.cmm.service.LoginVO;
import egovframework.cmm.service.ResponseMessage;
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.rte.fdl.property.EgovPropertyService;
import egovframework.sts.dto.SmdDTO;
import egovframework.sts.dto.Target;
import egovframework.sts.service.SmdService;
import egovframework.sys.service.Power;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Api(tags = {"영업 통계표"})
@RestController
@RequestMapping("/smd")
public class SmdController {

  @Resource(name = "SmdService")
  private SmdService smdService;

  @Resource(name = "propertiesService")
  protected EgovPropertyService propertyService;

  @ApiOperation(value = "영업 통계표", notes = "75-평가완료일/23-시험부/9-시험담당자")
  @GetMapping(value = "/memList.do")
  public BasicResponse memList(@ModelAttribute ComParam param) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";

    List<SmdDTO> list = new ArrayList<SmdDTO>();

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    if (!isAuthenticated) {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
      
      BasicResponse res =
          BasicResponse.builder().result(result).message(msg).build();

      return res;
    }

    list = smdService.selectSaleList(param);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res =
        BasicResponse.builder().result(result).message(msg).data(list).build();
    return res;
  }

  @ApiOperation(value = "목표금액 저장", notes = "")
  @PostMapping(value = "/insert.do")
  public BasicResponse insert(
      @ApiParam(value = "", required = true, example = "") @RequestBody List<Target> list)
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
    
    for (Target req : list) {

      // 로그인정보
//      req.setInsMemId(user.getId());
//      req.setUdtMemId(user.getId());


      System.out.println("=-===========");
      System.out.println(req.toString());
      System.out.println("=-===========");

      ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
      Validator validator = validatorFactory.getValidator();

      Set<ConstraintViolation<Target>> violations = validator.validate(req);

      for (ConstraintViolation<Target> violation : violations) {
        msg = violation.getMessage();

        BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

        return res;
      }
    }

    result = smdService.insert(list);
    BasicResponse res = BasicResponse.builder().result(result).build();

    return res;
  }
}
