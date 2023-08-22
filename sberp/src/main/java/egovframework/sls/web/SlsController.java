package egovframework.sls.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.quo.service.Quo;
import egovframework.rte.fdl.property.EgovPropertyService;
import egovframework.sls.service.BillDTO;
import egovframework.sls.service.SlsDTO;
import egovframework.sls.service.SlsService;
import egovframework.sls.service.SlsSummary;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Api(tags = {"매출"})
@RestController
@RequestMapping("/sls")
public class SlsController {

  @Resource(name = "propertiesService")
  protected EgovPropertyService propertyService;

  @Resource(name = "SlsService")
  private SlsService slsService;

  @ApiOperation(value = "매출리스트",
      notes = "1. 결과값은 SlsDTO.Res 참고\n" + "2.검색박스는 공통코드 CS, 필요한항목만 노출시켜서 사용\n" + " 고객유형(PT)\n"
          + " 납부상태:미납-0, 납부-1" + " 계산서발행여부:미발행-0, 발행-1" + " 수정요청(MM)")
  @GetMapping(value = "/list.do")
  public BasicResponse slsList(@ModelAttribute ComParam param) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<SlsDTO.Res> list = new ArrayList<SlsDTO.Res>();

    // 페이징
    param.setPageUnit(param.getPageUnit());
    param.setPageSize(propertyService.getInt("pageSize"));

    PagingVO pagingVO = new PagingVO();

    pagingVO.setCurrentPageNo(param.getPageIndex());
    pagingVO.setDisplayRow(param.getPageUnit());
    pagingVO.setDisplayPage(param.getPageSize());

    param.setFirstIndex(pagingVO.getFirstRecordIndex());
    int cnt = slsService.selectListCnt(param);

    pagingVO.setTotalCount(cnt);
    param.setTotalCount(cnt);
    pagingVO.setTotalPage(
        (int) Math.ceil(pagingVO.getTotalCount() / (double) pagingVO.getDisplayRow()));
    list = slsService.selectList(param);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }
    
    /*
     * 미수금액총합 / 순매출총합 / 청구액총합
     * 미수금 총합 = 미수금이 없는 수
     * 순매출, 청구액 총합 = 화면리스트 수
     */
    SlsSummary summ = new SlsSummary();
    summ.setTotal(list.stream().mapToInt(SlsDTO.Res::getChrgs).sum());
    summ.setTotalCnt(list.size());
    summ.setTotalArrears(list.stream().mapToInt(SlsDTO.Res::getArrears).sum());
    summ.setArrearsCnt((int) list.stream().filter(t -> t.getArrears() > 0).count());
    summ.setTotalNetSales(list.stream().mapToInt(SlsDTO.Res::getNetSales).sum());
    summ.setNetSalesCnt(list.size());
    
    BasicResponse res =
        BasicResponse.builder().result(result).message(msg).summary(summ).data(list).paging(pagingVO).build();

    return res;
  }

  @ApiOperation(value = "취합견적 매출확정 등록")
  @PostMapping(value = "/chq/insert.do")
  public BasicResponse slsInserts(@ApiParam(value = "chqId 값만 전송") @RequestBody SlsDTO.Req req)
      throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());

    System.out.println("===================");
    System.out.println(req.toString());
    System.out.println("===================");

    boolean result = false;
    String msg = "";

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {

      // 취합견적서에 속한 견적서 아이디 리스트
      List<String> quoIds = slsService.selectQuoIdList(req);

      // 기등록 매출 데이터 확인
//      for (String id : quoIds) {
        if (slsService.selectDetail(req) != null) {
          result = false;
          msg = ResponseMessage.DUPLICATE_SLS;

          BasicResponse res = BasicResponse.builder().result(result).message(msg).build();
          return res;
        }
//      }

      // 매출 등록
      req.setQuoStateCode("1"); // 매출확정 코드
      req.setQuoIds(quoIds);
      result = slsService.insertChq(req);

    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }

  @ApiOperation(value = "매출확정 등록")
  @PostMapping(value = "/insert.do")
  public BasicResponse slsInsert(@ApiParam(value = "quoId 값, 신청금액 전송") @RequestBody SlsDTO.Req req)
      throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());

    System.out.println("===================");
    System.out.println(req.toString());
    System.out.println("===================");

    boolean result = false;
    String msg = "";

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      
      try {
        // 신청금액 필수값 체크
        if (req.getBill() <= 0) {
          msg = ResponseMessage.CHECK_DATA;
        } 
        // 기등록된 매출이 있는지 확인
        else if (slsService.selectDetail(req) != null) {
          msg = ResponseMessage.DUPLICATE_SLS;
        } 
        else {
          
          Quo quo = slsService.selectQuoDetail(req.getQuoId());
          if (quo == null) throw new Exception(ResponseMessage.NO_DATA); 
          
          // 신청금액은 미수금보다 크면 오류
          if (Integer.parseInt(quo.getTotalVat()) < req.getBill()) {
            msg = ResponseMessage.ERROR_BILL;
          } 
          // 취합견적서에 포함된 견적서는 취합견적서로 이동
          else if (quo.getChqSeq() > 0) {
            msg = ResponseMessage.GO_CHQ;
          }
          else { 
            req.setQuoStateCode("1"); // 매출확정 코드
            result = slsService.insert(req);
          }
          
        }
      } catch (Exception e) {
        
        msg = ResponseMessage.RETRY;
        log.warn(user.getId() + " :: " + e.toString());
        log.warn(req.toString());
        
      }

    } else {
      msg = ResponseMessage.UNAUTHORIZED;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }

  @ApiOperation(value = "매출리스트(시험부별)", notes = "1. 결과값은 SlsDTO.Res 참고\n"
      + "2.검색박스는 공통코드 CS, 필요한항목만 노출시켜서 사용\n" + " 고객유형(PT)\n" + " 납부상태:미납-0, 납부-1" + " 시험부(TT)")
  @GetMapping(value = "/byTestlist.do")
  public BasicResponse slsByTestList(@ModelAttribute ComParam param) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<SlsDTO.Res> list = new ArrayList<SlsDTO.Res>();

    // 페이징
    param.setPageUnit(param.getPageUnit());
    param.setPageSize(propertyService.getInt("pageSize"));

    PagingVO pagingVO = new PagingVO();

    pagingVO.setCurrentPageNo(param.getPageIndex());
    pagingVO.setDisplayRow(param.getPageUnit());
    pagingVO.setDisplayPage(param.getPageSize());

    param.setFirstIndex(pagingVO.getFirstRecordIndex());
    int cnt = slsService.selectByTestListCnt(param);

    pagingVO.setTotalCount(cnt);
    pagingVO.setTotalPage(
        (int) Math.ceil(pagingVO.getTotalCount() / (double) pagingVO.getDisplayRow()));
    list = slsService.selectByTestList(param);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    /*
     * 시험비총합 / 순매출액총합
     * 시험비, 순매출액 총합 = 화면리스트 수
     */
    SlsSummary summ = new SlsSummary();
    summ.setTotalTestFee(list.stream().mapToInt(SlsDTO.Res::getTestFee).sum());
    summ.setTestFeeCnt(list.size());
    summ.setTotalNetSales(list.stream().mapToInt(SlsDTO.Res::getNetSales).sum());
    summ.setNetSalesCnt(list.size());
    
    BasicResponse res =
        BasicResponse.builder().result(result).message(msg).summary(summ).data(list).paging(pagingVO).build();

    return res;
  }

//  @ApiOperation(value = "납부상태 리스트", notes = "결과값은 PayDTO.Res 참고")
//  @GetMapping(value = "/payList.do")
//  public BasicResponse payList(@ApiParam(value = "매출 고유번호", required = true,
//      example = "M2303-0002") @RequestParam(value = "slsSeq") String slsSeq) throws Exception {
//
//    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
//    boolean result = true;
//    String msg = "";
//    List<PayDTO.Res> list = new ArrayList<PayDTO.Res>();
//
//    list = slsService.selectPayList(slsSeq);
//
//    if (list == null) {
//      result = false;
//      msg = ResponseMessage.NO_DATA;
//    }
//
//    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(list).build();
//
//    return res;
//  }

//  @ApiOperation(value = "납부상태 변경", notes = "납부상태 공통코드 : MP")
//  @PostMapping(value = "/payInsert.do")
//  public BasicResponse payInsert(@RequestBody PayDTO.Req req) throws Exception {
//
//    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
//
//    // 로그인정보
//    req.setInsMemId(user.getId());
//    req.setUdtMemId(user.getId());
//
//    boolean result = false;
//    String msg = "";
//
//    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
//
//    if (isAuthenticated) {
//      result = slsService.payInsert(req);
//    } else {
//      result = false;
//      msg = ResponseMessage.UNAUTHORIZED;
//    }
//
//    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();
//
//    return res;
//  }


  @ApiOperation(value = "계산서 상태 리스트", notes = "결과값은 BillDTO.Res 참고")
  @GetMapping(value = "/billList.do")
  public BasicResponse billList(@ApiParam(value = "매출 고유번호", required = true,
      example = "M2303-0002") @RequestParam(value = "slsSeq") String slsSeq) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<BillDTO.Res> list = new ArrayList<BillDTO.Res>();

    list = slsService.selectBillList(slsSeq);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(list).build();

    return res;
  }

  @ApiOperation(value = "계산서 상태 변경",
      notes = "계산서 발행여부 : slsId, billSeq, billYn=1 필수\n계산서 발행 작성일 : slsId, billSeq, otherBillDt\n납부상태 변경 : slsId, billSeq, payCode(MP)")
  @PostMapping(value = "/billInsert.do")
  public BasicResponse billInsert(@RequestBody SlsDTO.Req req) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());

    boolean result = false;
    String msg = "";

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      result = slsService.billInsert(req);
    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }

  @ApiOperation(value = "매출삭제", notes = "")
  @PostMapping(value = "/delete.do")
  public BasicResponse delete(@ApiParam(value = "매출 고유번호", required = true,
      example = "M2303-0002") @RequestBody List<String> slsIds) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";

    result = slsService.delete(user.getId(), slsIds);

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }


}
