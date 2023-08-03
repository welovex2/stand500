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
import egovframework.sys.service.MacService;
import egovframework.sys.service.MachineDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags = {"시험장비"})
@RestController
@RequestMapping("/sys/mac")
public class MacController {

  @Resource(name = "MacService")
  private MacService macService;

  @Resource(name = "propertiesService")
  protected EgovPropertyService propertyService;

  @ApiOperation(value = "시험장비 리스트", notes = "searchCode:10, searchWord:공통코드(TM)")
  @GetMapping(value = "/list.do")
  public BasicResponse list(@ModelAttribute ComParam param) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<MachineDTO> list = new ArrayList<MachineDTO>();

    if (param != null && param.getSearchVO() != null) {
      if ( 1 == param.getSearchVO().stream().filter(t -> "10".equals(t.getSearchCode())).collect(Collectors.toList()).size())
      {
        SearchVO detail = new SearchVO();
        detail.setSearchCode("-9");
        param.getSearchVO().add(0, detail);
      }
    }
    
    list = macService.selectList(param);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(list).build();

    return res;
  }
  
  @ApiOperation(value = "시험장비 등록", notes = "")
  @PostMapping(value = "/insert.do")
  public BasicResponse insert(
      @ApiParam(value = "", required = true, example = "") @RequestBody List<MachineDTO> list)
      throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";

    for (MachineDTO req : list) {

      // 로그인정보
      req.setInsMemId(user.getId());
      req.setUdtMemId(user.getId());


      System.out.println("=-===========");
      System.out.println(req.toString());
      System.out.println("=-===========");

      ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
      Validator validator = validatorFactory.getValidator();

      Set<ConstraintViolation<MachineDTO>> violations = validator.validate(req);

      for (ConstraintViolation<MachineDTO> violation : violations) {
        msg = violation.getMessage();

        BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

        return res;
      }
    }


    boolean result = false;

    result = macService.insert(list);
    BasicResponse res = BasicResponse.builder().result(result).build();

    return res;
  }

  @ApiOperation(value = "시험장비 수정", notes = "machineSeq 필수\n삭제시, state='D' 추가")
  @PostMapping(value = "/update.do")
  public BasicResponse update(
      @ApiParam(value = "", required = true, example = "") @RequestBody MachineDTO req)
      throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";


    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());


    System.out.println("=-===========");
    System.out.println(req.toString());
    System.out.println("=-===========");

    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    Validator validator = validatorFactory.getValidator();

    Set<ConstraintViolation<MachineDTO>> violations = validator.validate(req);

    for (ConstraintViolation<MachineDTO> violation : violations) {
      msg = violation.getMessage();

      BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

      return res;
    }


    boolean result = false;

    result = macService.update(req);
    BasicResponse res = BasicResponse.builder().result(result).build();

    return res;
  }
  
  @ApiOperation(value = "시험장비 순서,표시여부 수정", notes = "machineSeq, disOrdr(no), useYn")
  @PostMapping(value = "/{type}/update.do")
  public BasicResponse updateSub(
      @ApiParam(value = "시험타입 : 공통코드(TM)", required = true,
      example = "MF") @PathVariable(name = "type") String type,
      @ApiParam(value = "", required = true, example = "") @RequestBody List<MachineDTO> list)
      throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";

    for (MachineDTO req : list) {

      // 로그인정보
      req.setInsMemId(user.getId());
      req.setUdtMemId(user.getId());


      System.out.println("=-===========");
      System.out.println(req.toString());
      System.out.println("=-===========");

      ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
      Validator validator = validatorFactory.getValidator();

      Set<ConstraintViolation<MachineDTO>> violations = validator.validate(req);

      for (ConstraintViolation<MachineDTO> violation : violations) {
        msg = violation.getMessage();

        BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

        return res;
      }
    }


    boolean result = false;

    result = macService.updateSub(type, list);
    BasicResponse res = BasicResponse.builder().result(result).build();

    return res;
  }
}
