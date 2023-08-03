package egovframework.cnf;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import egovframework.cmm.service.BasicResponse;
import egovframework.cmm.service.ComParam;
import egovframework.cmm.service.LoginVO;
import egovframework.cmm.service.PagingVO;
import egovframework.cmm.service.ResponseMessage;
import egovframework.cmm.service.SearchVO;
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.cnf.service.CmpyDTO;
import egovframework.cnf.service.CmyService;
import egovframework.rte.fdl.property.EgovPropertyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags = {"협력사, 직접고객"})
@RestController
@RequestMapping("/cnf/cmp")
public class CmyController {

  @Resource(name = "CmyService")
  private CmyService cmyService;

  @Resource(name = "propertiesService")
  protected EgovPropertyService propertyService;

  @ApiOperation(value = "협력사/직접고객 리스트", notes = "검색코드\n2  작성자\n12   회사명\n13 회사연락처\n41   회사종류\n15   작성일 ")
  @GetMapping(value = "/{type}/list.do")
  public BasicResponse list(
      @ApiParam(value = "partner(협력사)/direct(직접고객)", required = true,
          example = "partner") @PathVariable(name = "type") String type,
      @ModelAttribute ComParam param) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<CmpyDTO> list = new ArrayList<CmpyDTO>();

    // 내부검색 데이터
    SearchVO vo = new SearchVO();
    vo.setSearchCode("99");
    // 협력사
    if ("partner".equals(type))
      vo.setSearchWord("0000");
    // 직접고객
    else if ("direct".equals(type))
      vo.setSearchWord("1000");
    if (param.getSearchVO() != null) {
      param.getSearchVO().add(vo);
    } else {
      List<SearchVO> searchVO = new ArrayList<SearchVO>();
      searchVO.add(vo);
      param.setSearchVO(searchVO);
    }

    // 페이징
    param.setPageUnit(propertyService.getInt("pageUnit"));
    param.setPageSize(propertyService.getInt("pageSize"));

    PagingVO pagingVO = new PagingVO();

    pagingVO.setCurrentPageNo(param.getPageIndex());
    pagingVO.setDisplayRow(param.getPageUnit());
    pagingVO.setDisplayPage(param.getPageSize());

    param.setFirstIndex(pagingVO.getFirstRecordIndex());
    int cnt = cmyService.selectListCnt(param);

    pagingVO.setTotalCount(cnt);
    pagingVO.setTotalPage(
        (int) Math.ceil(pagingVO.getTotalCount() / (double) pagingVO.getDisplayRow()));
    list = cmyService.selectList(param);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(list).paging(pagingVO).build();

    return res;
  }

  @ApiOperation(value = "협력사/직접고객 등록")
  @PostMapping(value = "/{type}/insert.do")
  public BasicResponse insert(
      @ApiParam(value = "partner(협력사)/direct(직접고객)", required = true,
          example = "partner") @PathVariable(name = "type") String type,
      @ApiParam(value = "typeCode=협력사 공통코드(PK), 직고객 공통코드(ST)", required = true,
          example = "") @RequestBody CmpyDTO req)
      throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());
    // 협력사
    if ("partner".equals(type))
      req.setCmpyCode("0000");
    // 직접고객
    else if ("direct".equals(type))
      req.setCmpyCode("1000");

    System.out.println("=-===========");
    System.out.println(req.toString());
    System.out.println("=-===========");

    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    Validator validator = validatorFactory.getValidator();

    Set<ConstraintViolation<CmpyDTO>> violations = validator.validate(req);

    for (ConstraintViolation<CmpyDTO> violation : violations) {
      msg = violation.getMessage();

      BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

      return res;
    }

    boolean result = false;

    result = cmyService.insert(req);

    BasicResponse res = BasicResponse.builder().result(result).build();

    return res;
  }

  @ApiOperation(value = "협력사/직접고객 상세보기")
  @GetMapping(value = "/{cmpySeq}/detail.do")
  public BasicResponse detail(@ApiParam(value = "회사 고유번호", required = true,
      example = "0004") @PathVariable(name = "cmpySeq") int cmpySeq) throws Exception {
    boolean result = true;
    String msg = "";
    CmpyDTO detail = new CmpyDTO();

    detail = cmyService.detail(cmpySeq);

    if (detail == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();

    return res;
  }

  @ApiOperation(value = "협력사/직접고객 회사명 중복확인", notes = "")
  @GetMapping(value = "/{type}/checkName.do")
  public BasicResponse checkName(
      @ApiParam(value = "partner(협력사)/direct(직접고객)", required = true,
          example = "partner") @PathVariable(name = "type") String type,
      @RequestParam("cmpyName") String cmpyName) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<CmpyDTO> list = new ArrayList<CmpyDTO>();
    String cmpyCode = "";

    // 협력사
    if ("partner".equals(type))
      cmpyCode = "0000";
    // 직접고객
    else if ("direct".equals(type))
      cmpyCode = "1000";

    list = cmyService.selectSameName(cmpyCode, cmpyName);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(list).build();

    return res;
  }

}
