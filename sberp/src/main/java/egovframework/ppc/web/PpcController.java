package egovframework.ppc.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.springframework.util.StringUtils;
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
import egovframework.ppc.dto.PpDTO;
import egovframework.ppc.service.PpcService;
import egovframework.rte.fdl.property.EgovPropertyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags = {"사전통관"})
@RestController
@RequestMapping("/ppc")
public class PpcController {

  @Resource(name = "propertiesService")
  protected EgovPropertyService propertyService;

  @Resource(name = "PpcService")
  private PpcService ppcService;


  @ApiOperation(value = "사전통관 리스트", notes = "검색박스는 공통코드 CS, 필요한항목만 노출시켜서 사용\n1-고객유형(PT), 5-접수번호, "
      + "4-컨설팅/직고객명, 12-회사명, 6-제품명, 27-모델명, 7-고지부담당자, 15-작성일")
  @GetMapping(value = "/list.do")
  public BasicResponse ppList(@ModelAttribute ComParam param) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";

    List<PpDTO> list = new ArrayList<PpDTO>();

    // 페이징
    param.setPageUnit(propertyService.getInt("pageUnit"));
    param.setPageSize(propertyService.getInt("pageSize"));

    PagingVO pagingVO = new PagingVO();

    pagingVO.setCurrentPageNo(param.getPageIndex());
    pagingVO.setDisplayRow(param.getPageUnit());
    pagingVO.setDisplayPage(param.getPageSize());

    param.setFirstIndex(pagingVO.getFirstRecordIndex());
    int cnt = ppcService.selectListCnt(param);

    pagingVO.setTotalCount(cnt);
    pagingVO.setTotalPage(
        (int) Math.ceil(pagingVO.getTotalCount() / (double) pagingVO.getDisplayRow()));
    list = ppcService.selectList(param);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res =
        BasicResponse.builder().result(result).message(msg).data(list).paging(pagingVO).build();
    return res;
  }

  @ApiOperation(value = "사전통관 저장")
  @PostMapping(value = "insert.do")
  public BasicResponse ppAdd(@ApiParam(value = "상세조회후 메모추가시 사전통관고유번호(ppId) 필수", required = true,
      example = "") @RequestBody PpDTO pp) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";

    // 로그인정보
    pp.setInsMemId(user.getId());
    pp.setUdtMemId(user.getId());

    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    Validator validator = validatorFactory.getValidator();

    Set<ConstraintViolation<PpDTO>> violations = validator.validate(pp);

    for (ConstraintViolation<PpDTO> violation : violations) {
      msg = violation.getMessage();

      System.out.println(msg);
      BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

      return res;
    }

    boolean result = false;

    // 사전통관 번호가 있으면, 회사정보 update, 상담내역 insert
    if (!StringUtils.isEmpty(pp.getPpId())) {
      result = ppcService.update(pp);
    }
    // 최초 등록이면, 회사정보 insert, 상담내역 insert
    else {
      result = ppcService.insert(pp);
    }

    BasicResponse res = BasicResponse.builder().result(result).build();

    return res;
  }

  @ApiOperation(value = "사전통관 상세보기")
  @GetMapping(value = "/{ppId}/detail.do")
  public BasicResponse ppDetail(@ApiParam(value = "사전통관 고유번호", required = true,
      example = "6") @PathVariable(name = "ppId") String ppId) throws Exception {

    boolean result = true;
    String msg = "";
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    PpDTO detail = new PpDTO();

    detail = ppcService.selectDetail(ppId);
    if (detail == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }
    BasicResponse res = BasicResponse.builder().result(result).data(detail).build();

    return res;
  }

}
