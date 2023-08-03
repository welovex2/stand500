package egovframework.cns.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.BeforeClass;
import org.springframework.util.ObjectUtils;
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
import egovframework.cmm.service.PagingVO;
import egovframework.cmm.service.ResponseMessage;
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.cns.service.CnsDTO;
import egovframework.cns.service.CnsService;
import egovframework.rte.fdl.property.EgovPropertyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags = {"상담서"})
@RestController
@RequestMapping("/cns")
public class CnsController {

  @Resource(name = "propertiesService")
  protected EgovPropertyService propertyService;

  @Resource(name = "CnsService")
  private CnsService cnsService;

  private static Validator validator;


  @ApiOperation(value = "상담서 리스트", notes = "검색박스는 공통코드 CS, 필요한항목만 노출시켜서 사용\n고객유형(PT)")
  @GetMapping(value = "/list.do")
  public BasicResponse cnsList(@ModelAttribute ComParam param) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";

    List<CnsDTO.Res> list = new ArrayList<CnsDTO.Res>();

    System.out.println("================");
    System.out.println(param.toString());
    System.out.println("================");

    // 페이징
    param.setPageUnit(propertyService.getInt("pageUnit"));
    param.setPageSize(propertyService.getInt("pageSize"));

    PagingVO pagingVO = new PagingVO();

    pagingVO.setCurrentPageNo(param.getPageIndex());
    pagingVO.setDisplayRow(param.getPageUnit());
    pagingVO.setDisplayPage(param.getPageSize());

    param.setFirstIndex(pagingVO.getFirstRecordIndex());
    int cnt = cnsService.selectListCnt(param);

    pagingVO.setTotalCount(cnt);
    pagingVO.setTotalPage(
        (int) Math.ceil(pagingVO.getTotalCount() / (double) pagingVO.getDisplayRow()));
    list = cnsService.selectList(param);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res =
        BasicResponse.builder().result(result).message(msg).data(list).paging(pagingVO).build();
    return res;
  }

  @ApiOperation(value = "상담서 저장")
  @PostMapping(value = "insert.do")
  public BasicResponse cnsAdd(@ApiParam(value = "상세조회후 메모추가시 상담서고유번호(cnsSeq) 필수", required = true,
      example = "") @RequestBody CnsDTO.Req cns) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";

    // 로그인정보
    cns.setInsMemId(user.getId());
    cns.setUdtMemId(user.getId());

    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    Validator validator = validatorFactory.getValidator();

    Set<ConstraintViolation<CnsDTO.Req>> violations = validator.validate(cns);

    for (ConstraintViolation<CnsDTO.Req> violation : violations) {
      msg = violation.getMessage();

      System.out.println(msg);
      BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

      return res;
    }

    boolean result = false;

    // 상담서 번호가 있으면, 회사정보 update, 상담내역 insert
    if (cns.getCnsSeq() > 0) {
      result = cnsService.update(cns);
    }
    // 최초 등록이면, 회사정보 insert, 상담내역 insert
    else {
      result = cnsService.insert(cns);
    }

    BasicResponse res = BasicResponse.builder().result(result).build();

    return res;
  }

  @ApiOperation(value = "상담서 상세보기")
  @GetMapping(value = "/{cnsSeq}/detail.do")
  public BasicResponse cnsDetail(@ApiParam(value = "상담서 고유번호", required = true,
      example = "6") @PathVariable(name = "cnsSeq") int cnsSeq) throws Exception {

    boolean result = true;
    String msg = "";
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    CnsDTO.Res detail = new CnsDTO.Res();

    detail = cnsService.selectDetail(cnsSeq);
    if (detail == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }
    BasicResponse res = BasicResponse.builder().result(result).data(detail).build();

    return res;
  }

  @ApiOperation(value = "본건 상담서 작성")
  @PostMapping(value = "/makeCns.do")
  public BasicResponse makeCns(
      @ApiParam(value = "quoId 또는 sbkId 값만 전송") @RequestBody CnsDTO.Req req) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    CnsDTO.Res detail = new CnsDTO.Res();

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());

    System.out.println("================");
    System.out.println(req.toString());
    System.out.println("================");
    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      // 이미 연결된 상담서가 있는지 확인

      Integer seq = cnsService.checkDetail(req);
      if (!ObjectUtils.isEmpty(seq)) {
        result = true;
        msg = ResponseMessage.DUPLICATE_CNS;
        detail.setCnsSeq(seq);
      } else {

        // 상담서 생성
        result = cnsService.insert(req);

        // 상담서 정보 보내주기
        detail = cnsService.selectDetail(req.getCnsSeq());

        if (detail == null) {
          result = false;
          msg = ResponseMessage.NO_DATA;
        }

      }

    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();

    return res;

  }
}
