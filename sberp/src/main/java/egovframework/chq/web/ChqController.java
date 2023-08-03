package egovframework.chq.web;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import egovframework.chq.service.ChqDTO;
import egovframework.chq.service.ChqDTO.Res;
import egovframework.chq.service.ChqService;
import egovframework.cmm.service.BasicResponse;
import egovframework.cmm.service.ComParam;
import egovframework.cmm.service.LoginVO;
import egovframework.cmm.service.PagingVO;
import egovframework.cmm.service.ResponseMessage;
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.rte.fdl.property.EgovPropertyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags = {"취합견적서"})
@RestController
@RequestMapping("/chq")
public class ChqController {

  @Resource(name = "ChqService")
  private ChqService chqService;

  @Resource(name = "propertiesService")
  protected EgovPropertyService propertyService;

  @ApiOperation(value = "취합견적서 리스트", notes = "1. 결과값은 ChqDTO.Res 참고\n"
      + "2. 검색박스는 공통코드 CS, 필요한항목만 노출시켜서 사용\n3. -- 4   컨설팅명\n-- 7  취합자\n-- 15  취합생성일\n-- 16    매출확정일")
  @GetMapping(value = "/list.do")
  public BasicResponse chqList(@ModelAttribute ComParam param) throws Exception {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<ChqDTO.Res> list = new ArrayList<ChqDTO.Res>();

    // 페이징
    param.setPageUnit(propertyService.getInt("pageUnit"));
    param.setPageSize(propertyService.getInt("pageSize"));

    PagingVO pagingVO = new PagingVO();

    pagingVO.setCurrentPageNo(param.getPageIndex());
    pagingVO.setDisplayRow(param.getPageUnit());
    pagingVO.setDisplayPage(param.getPageSize());

    param.setFirstIndex(pagingVO.getFirstRecordIndex());
    int cnt = chqService.selectListCnt(param);

    pagingVO.setTotalCount(cnt);
    pagingVO.setTotalPage(
        (int) Math.ceil(pagingVO.getTotalCount() / (double) pagingVO.getDisplayRow()));
    list = chqService.selectList(param);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res =
        BasicResponse.builder().result(result).message(msg).data(list).paging(pagingVO).build();

    return res;


  }

  @ApiOperation(value = "취합 견적서 작성")
  @PostMapping(value = "/insert.do")
  public BasicResponse insert(@ApiParam(value = "quoId 값만 전송") @RequestBody List<String> quoIds)
      throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    ChqDTO req = new ChqDTO();

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());

    System.out.println("================");
    System.out.println(quoIds);
    System.out.println("================");
    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {

      // 2개 이상부터 취합 가능
      if (quoIds.size() <= 1) {
        result = false;
        msg = ResponseMessage.INSERT_TWO;
      }
      // 매출확정일이 있는 데이터가 있는지 확인
      else if (chqService.isCnfrmDt(quoIds) == 1) {
        result = false;
        msg = ResponseMessage.DUPLICATE_CNFRMED;
      }
      // 동일한 컨설팅 회사인지 확인
      else if (chqService.isSameCons(quoIds) == 1) {
        result = false;
        msg = ResponseMessage.DIFFERENT_CONS;
      }
      // 이미 취합된 견적서인지 확인
      else if (chqService.isChq(quoIds) == 1) {
        result = false;
        msg = ResponseMessage.DUPLICATE_CHQ;
      } else {

        // 취합견적서 생성
        req.setQuoIds(quoIds);
        result = chqService.insert(req);

      }

    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }

  @ApiOperation(value = "취합견적서 상세보기", notes = "1. 견적서별 건수는 data.sub.items.lenth 사용")
  @GetMapping(value = "/{chqId}/detail.do")
  public BasicResponse chqDetail(@ApiParam(value = "취합견적서 고유번호", required = true,
      example = "CH2305-001") @PathVariable(name = "chqId") String chqId) throws Exception {
    boolean result = true;
    String msg = "";
    Res detail = new Res();
    ChqDTO req = new ChqDTO();

    req.setChqId(chqId);
    detail = chqService.selectDetail(req);

    if (detail == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();

    return res;
  }


  @ApiOperation(value = "취합견적서 삭제")
  @PostMapping(value = "/delete.do")
  public BasicResponse delete(@ApiParam(value = "취합견적서 고유번호", required = true,
      example = "[CH2305-001]") @RequestBody List<String> chqIds) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";

    // 매출확정일이 있는 데이터가 있는지 확인
    for (String id : chqIds) {
      Res detail = new Res();
      ChqDTO req = new ChqDTO();

      req.setChqId(id);
      detail = chqService.selectDetail(req);

      if (!StringUtils.isEmpty(detail.getCnfrmDtStr())) {
        result = false;
        msg = ResponseMessage.DUPLICATE_CNFRMED;
        BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

        return res;
      }
    }

    result = chqService.delete(user.getId(), chqIds);

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }
}
