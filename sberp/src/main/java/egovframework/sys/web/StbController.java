package egovframework.sys.web;

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
import egovframework.cmm.service.PagingVO;
import egovframework.cmm.service.ResponseMessage;
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.rte.fdl.property.EgovPropertyService;
import egovframework.sys.service.StbService;
import egovframework.sys.service.TestStndrDTO;
import egovframework.tst.dto.TestCateDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags = {"시험규격"})
@RestController
@RequestMapping("/sys/stb")
public class StbController {

  @Resource(name = "StbService")
  private StbService stbService;

  @Resource(name = "propertiesService")
  protected EgovPropertyService propertyService;

  @ApiOperation(value = "시험규격 리스트", notes = "2  작성자\n15  작성일\n40  시험규격")
  @GetMapping(value = "/list.do")
  public BasicResponse list(@ModelAttribute ComParam param) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<TestStndrDTO> list = new ArrayList<TestStndrDTO>();

    // 페이징
//    param.setPageUnit(20);
//    param.setPageSize(propertyService.getInt("pageSize"));

//    PagingVO pagingVO = new PagingVO();

//    pagingVO.setCurrentPageNo(param.getPageIndex());
//    pagingVO.setDisplayRow(param.getPageUnit());
//    pagingVO.setDisplayPage(param.getPageSize());

//    param.setFirstIndex(pagingVO.getFirstRecordIndex());
//    int cnt = stbService.selectListCnt(param);

//    pagingVO.setTotalCount(cnt);
//    pagingVO.setTotalPage(
//        (int) Math.ceil(pagingVO.getTotalCount() / (double) pagingVO.getDisplayRow()));
    list = stbService.selectList(param);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(list).build();

    return res;
  }

  @ApiOperation(value = "시험규격 등록", notes = "수정시, testStndrSeq 필수\n삭제시, testStndrSeq, state='D'")
  @PostMapping(value = "/insert.do")
  public BasicResponse insert(
      @ApiParam(value = "", required = true, example = "") @RequestBody List<TestStndrDTO> list)
      throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";

    for (TestStndrDTO req : list) {

      // 로그인정보
      req.setInsMemId(user.getId());
      req.setUdtMemId(user.getId());


      System.out.println("=-===========");
      System.out.println(req.toString());
      System.out.println("=-===========");

      ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
      Validator validator = validatorFactory.getValidator();

      Set<ConstraintViolation<TestStndrDTO>> violations = validator.validate(req);

      for (ConstraintViolation<TestStndrDTO> violation : violations) {
        msg = violation.getMessage();

        BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

        return res;
      }
    }


    boolean result = false;

    result = stbService.insert(list);
    BasicResponse res = BasicResponse.builder().result(result).build();

    return res;
  }

  @ApiOperation(value = "시험규격 수정", notes = "")
  @PostMapping(value = "/update.do")
  public BasicResponse update(
      @ApiParam(value = "", required = true, example = "") @RequestBody TestStndrDTO req)
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

    Set<ConstraintViolation<TestStndrDTO>> violations = validator.validate(req);

    for (ConstraintViolation<TestStndrDTO> violation : violations) {
      msg = violation.getMessage();

      BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

      return res;
    }


    boolean result = false;

    result = stbService.update(req);
    BasicResponse res = BasicResponse.builder().result(result).build();

    return res;
  }

  @ApiOperation(value = "인증종류 리스트", notes = "15    국작성일\n35    국가\n36   인증종류1\n37   인증종류2\n38   인증종류3\n2-작성자")
  @GetMapping(value = "/cate/list.do")
  public BasicResponse cateList(@ModelAttribute ComParam param) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<TestCateDTO> list = new ArrayList<TestCateDTO>();

    // 페이징
//    param.setPageUnit(20);
//    param.setPageSize(propertyService.getInt("pageSize"));
//
//    PagingVO pagingVO = new PagingVO();
//
//    pagingVO.setCurrentPageNo(param.getPageIndex());
//    pagingVO.setDisplayRow(param.getPageUnit());
//    pagingVO.setDisplayPage(param.getPageSize());
//
//    param.setFirstIndex(pagingVO.getFirstRecordIndex());
//    int cnt = stbService.selectCateListCnt(param);
//
//    pagingVO.setTotalCount(cnt);
//    pagingVO.setTotalPage(
//        (int) Math.ceil(pagingVO.getTotalCount() / (double) pagingVO.getDisplayRow()));
    list = stbService.selectCateList(param);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(list).build();

    return res;
  }
  
  @ApiOperation(value = "인증종류 등록", notes = "삭제시 testCateSeq 필수, state='D' 추가")
  @PostMapping(value = "/cate/insert.do")
  public BasicResponse cateInsert(
      @ApiParam(value = "", required = true, example = "") @RequestBody TestCateDTO req)
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

    Set<ConstraintViolation<TestCateDTO>> violations = validator.validate(req);

    for (ConstraintViolation<TestCateDTO> violation : violations) {
      msg = violation.getMessage();

      BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

      return res;
    }


    boolean result = false;

    result = stbService.insertCate(req);
    BasicResponse res = BasicResponse.builder().result(result).build();

    return res;
  }

}
