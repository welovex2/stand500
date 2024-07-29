package egovframework.tst.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.util.StringUtils;
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
import egovframework.cmm.service.SearchVO;
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.rte.fdl.property.EgovPropertyService;
import egovframework.sbk.service.SbkDTO;
import egovframework.sls.service.SlsSummary;
import egovframework.sys.service.TestStndr;
import egovframework.tst.dto.CanCelDTO;
import egovframework.tst.dto.TestDTO;
import egovframework.tst.dto.TestDTO.Res;
import egovframework.tst.dto.TestItemDTO;
import egovframework.tst.dto.TestMngrDTO;
import egovframework.tst.service.TestCate;
import egovframework.tst.service.TestMngr;
import egovframework.tst.service.TstParam;
import egovframework.tst.service.TstService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Api(tags = {"시험"})
@RestController
@RequestMapping("/tst")
public class TstController {

  @Resource(name = "TstService")
  private TstService tstService;

  @Resource(name = "propertiesService")
  protected EgovPropertyService propertyService;

  @ApiOperation(value = "시험항목 > 인증종류리스트",
      notes = "시험항목 > 시험종류리스트는 공통코드 리스트로 조회 = /cmm/comcode/list.do?code=TT")
  @GetMapping(value = "/crtfcList.do")
  public List<TestCate> crtfcTypeList(
      @ApiParam(value = "상위인증코드(없으면 값 넣지 않음)", example = "1") @RequestParam int topCode)
      throws Exception {
    List<TestCate> result = new ArrayList<TestCate>();

    result = tstService.selectCrtfList(topCode);

    return result;
  }

  @ApiOperation(value = "시험항목 시험규격리스트")
  @GetMapping(value = "/stndrList.do")
  public List<TestStndr> stndrList(TstParam param) throws Exception {
    List<TestStndr> result = new ArrayList<TestStndr>();

    result = tstService.selectStndrList(param);

    return result;
  }

  @ApiOperation(value = "시험 신청하기")
  @PostMapping(value = "/makeTest.do")
  public BasicResponse makeTest(
      @ApiParam(value = "testItemSeq, testTypeCode 값 전송") @RequestBody TestDTO.Req req)
      throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());

    System.out.println("=-===========");
    System.out.println(req.toString());
    System.out.println("=-===========");

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      // 이미 연결된 시험이 있는지 확인
      if (tstService.selectDetail(req) != null) {
        result = false;
        msg = ResponseMessage.DUPLICATE_TEST;
      } 
      // 값이 제대로 넘어왔는지 확인
      else if (req.getTestItemSeq() == 0 || StringUtils.isEmpty(req.getTestTypeCode())) {
        result = false;
        msg = ResponseMessage.CHECK_DATA;
      } else {
        // 시험 생성
        result = tstService.insert(req);
      }

    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;

  }


  @ApiOperation(value = "시험리스트",
      notes = "결과값은 TestDTO.Res 참고" + "2.검색박스는 공통코드 CS, 필요한항목만 노출시켜서 사용\n"
          + " 신청구분:신규-1,기술기준변경-2,동일기자재-3,기술기준외변경-4\n" + " 시험부(TT), 미배정-9999\n" + " 시험상태(TS)")
  @GetMapping(value = "/list.do")
  public BasicResponse testList(@ModelAttribute ComParam param) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<TestDTO.Res> list = new ArrayList<TestDTO.Res>();

    param.setMemId(user.getId());
    param.setSecretYn(user.getSecretYn());
    
    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    if (!isAuthenticated) {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
      
      BasicResponse res =
          BasicResponse.builder().result(result).message(msg).build();

      return res;
    }
    
    /**
     * OR 조건 검색 처리
     */
    // 같은 CODE로 그룹핑
    Map<String, List<SearchVO>> reSearch = param.getSearchVO().stream().collect(Collectors.groupingBy(SearchVO::getSearchCode));
    
    // 받은 searchCode 삭제 후 다시 생성
//    param.getSearchVO().stream().filter(x -> "23".equals(x.getSearchCode()) || "22".equals(x.getSearchCode())).collect(Collectors.toList()).forEach(x -> {param.getSearchVO().remove(x);});

    SearchVO newSearch = new SearchVO();
    // 시험부
    if (reSearch.get("23") != null) {
      newSearch = new SearchVO();
      newSearch.setSearchCode("23");
      newSearch.setSearchWords(reSearch.get("23").stream().map(m -> m.getSearchWord()).collect(Collectors.toList()));
      param.getSearchVO().add(newSearch);
    }
    
    // 신청구분
    if (reSearch.get("22") != null) {
      newSearch = new SearchVO();
      newSearch.setSearchCode("22");
      newSearch.setSearchWords(reSearch.get("22").stream().map(m -> m.getSearchWord()).collect(Collectors.toList()));
      param.getSearchVO().add(newSearch);
    }
    
    // 시험상태
    if (reSearch.get("31") != null) {
      newSearch = new SearchVO();
      newSearch.setSearchCode("31");
      newSearch.setSearchWords(reSearch.get("31").stream().map(m -> m.getSearchWord()).collect(Collectors.toList()));
      param.getSearchVO().add(newSearch);
    }
    /**
     * -- END OR 조건 검색 처리
     */
    
    // 페이징
    param.setPageUnit(param.getPageUnit());
    param.setPageSize(propertyService.getInt("pageSize"));

    PagingVO pagingVO = new PagingVO();

    pagingVO.setCurrentPageNo(param.getPageIndex());
    pagingVO.setDisplayRow(param.getPageUnit());
    pagingVO.setDisplayPage(param.getPageSize());

    param.setFirstIndex(pagingVO.getFirstRecordIndex());
    int cnt = tstService.selectListCnt(param);

    param.setTotalCount(cnt);
    pagingVO.setTotalCount(cnt);
    pagingVO.setTotalPage(
        (int) Math.ceil(pagingVO.getTotalCount() / (double) pagingVO.getDisplayRow()));
    list = tstService.selectList(param);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res =
        BasicResponse.builder().result(result).message(msg).data(list).paging(pagingVO).build();

    return res;
  }

  @ApiOperation(value = "시험리스트 (고객지원부)",
      notes = "결과값은 TestDTO.Res 참고" + "2.검색박스는 공통코드 CS, 필요한항목만 노출시켜서 사용\n"
          + " 신청구분:신규-1,기술기준변경-2,동일기자재-3,기술기준외변경-4\n" + " 시험부(TT), 미배정-9999\n" + " 시험상태(TS)")
  @GetMapping(value = "/sale/list.do")
  public BasicResponse testSaleList(@ModelAttribute ComParam param) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<Res> list = new ArrayList<Res>();

    param.setMemId(user.getId());
    param.setSecretYn(user.getSecretYn());
    
    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    if (!isAuthenticated) {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
      
      BasicResponse res =
          BasicResponse.builder().result(result).message(msg).build();

      return res;
    }
    
    /**
     * OR 조건 검색 처리
     */
    // 같은 CODE로 그룹핑
    Map<String, List<SearchVO>> reSearch = param.getSearchVO().stream().collect(Collectors.groupingBy(SearchVO::getSearchCode));
    
    SearchVO newSearch = new SearchVO();
    // 시험부
    if (reSearch.get("23") != null) {
      newSearch = new SearchVO();
      newSearch.setSearchCode("23");
      newSearch.setSearchWords(reSearch.get("23").stream().map(m -> m.getSearchWord()).collect(Collectors.toList()));
      param.getSearchVO().add(newSearch);
    }
    
    // 신청구분
    if (reSearch.get("22") != null) {
      newSearch = new SearchVO();
      newSearch.setSearchCode("22");
      newSearch.setSearchWords(reSearch.get("22").stream().map(m -> m.getSearchWord()).collect(Collectors.toList()));
      param.getSearchVO().add(newSearch);
    }
    
    // 시험상태
    if (reSearch.get("31") != null) {
      newSearch = new SearchVO();
      newSearch.setSearchCode("31");
      newSearch.setSearchWords(reSearch.get("31").stream().map(m -> m.getSearchWord()).collect(Collectors.toList()));
      param.getSearchVO().add(newSearch);
    }
    /**
     * -- END OR 조건 검색 처리
     */
    
    // 페이징
    param.setPageUnit(param.getPageUnit());
    param.setPageSize(propertyService.getInt("pageSize"));

    PagingVO pagingVO = new PagingVO();

    pagingVO.setCurrentPageNo(param.getPageIndex());
    pagingVO.setDisplayRow(param.getPageUnit());
    pagingVO.setDisplayPage(param.getPageSize());

    param.setFirstIndex(pagingVO.getFirstRecordIndex());
    int cnt = tstService.selectSaleListCnt(param);

    pagingVO.setTotalCount(cnt);
    param.setTotalCount(cnt);
    pagingVO.setTotalPage(
        (int) Math.ceil(pagingVO.getTotalCount() / (double) pagingVO.getDisplayRow()));
    list = tstService.selectSaleList(param);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    /*
     * 순매출총합
     */
    SlsSummary summ = new SlsSummary();
    summ.setTotalCnt(cnt);
    for (Res detail : list) {
      for (TestItemDTO sub : detail.getItems()) {
        summ.setTotalNetSales(summ.getTotalNetSales()+sub.getNetSales());
        summ.setNetSalesCnt(summ.getNetSalesCnt() + 1);
      }
    }
    
    BasicResponse res =
        BasicResponse.builder().result(result).message(msg).summary(summ).data(list).paging(pagingVO).build();

    return res;
  }

  @ApiOperation(value = "시험담당자 저장", notes = "testSeq:시험고유항목, activ:수행지수, items[testMngrSeq: 1~3까지 고정, testMngId:시험담당자ID, memo:평가메모]")
  @PostMapping(value = "/testMemInsert.do")
  public BasicResponse testMemInsert(@RequestBody TestMngrDTO req) throws Exception {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());

    boolean result = false;
    String msg = "";

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      
      /**
       * 필수체크
       */
      // 1. 메인시험원 필수 체크
      if (req.getItems().stream().filter(t -> 1 == t.getTestMngrSeq() && StringUtils.isEmpty(t.getTestMngId())).count() > 0) {
        result = false;
        msg = ResponseMessage.CHECK_DATA;
      }
      // 2. 참여율 합은 100퍼센트
      else if (req.getItems().stream().mapToInt(TestMngr::getPartRate).sum() != 100) {
        result = false;
        msg = ResponseMessage.CHECK_RATE_SUM;
      }
      else {
        result = tstService.testMemInsert(req);
      }
      
    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }

  @ApiOperation(value = "시험담당자 변경 내역")
  @GetMapping(value = "/testMemList.do")
  public BasicResponse testMemList(@ApiParam(value = "시험 고유번호", required = true,
      example = "1") @RequestParam(value = "testSeq") String testSeq) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    TestMngrDTO data = new TestMngrDTO();

    data = tstService.testMemList(testSeq);

    if (data == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(data).build();

    return res;
  }

  @ApiOperation(value = "시험담당자 평가확정", notes = "testSeq:시험고유항목, ratingState:평가확정 1")
  @PostMapping(value = "/testMem/stateUpdate.do")
  public BasicResponse testStateUpdate(@RequestBody TestMngrDTO req) throws Exception {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());

    boolean result = false;
    String msg = "";

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      
      /**
       * 평가박스 선택 되어있는지 확인
       */
      TestMngrDTO list = new TestMngrDTO();

      list = tstService.testMemList(Integer.toString(req.getTestSeq()));
      
      for (TestMngr detail : list.getItems()) {
        if (!StringUtils.isEmpty(detail.getTestMngId()) && StringUtils.isEmpty(detail.getRating())) {
          
          result = false;
          msg = ResponseMessage.CHECK_DATA;
          
          BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

          return res;
        }
      }
      
      result = tstService.testMemSatetUpdate(req);
    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }
  
  @ApiOperation(value = "시험상태 변경", notes = "testSeq:시험고유항목, 시험상태:공통코드(TS), memo:메모")
  @PostMapping(value = "/testStateInsert.do")
  public BasicResponse testStateInsert(@RequestBody TestDTO.Req req) throws Exception {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());

    boolean result = false;
    String msg = "";
    TestDTO.Res detail = new TestDTO.Res();
    
    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      
      // 상태 변경할 수 없는 것 체크
      detail = tstService.checkTestState(req.getTestSeq());
      if (detail != null) {
        
        // 최신 상태가 시험취소(확정)이면 상태변경 불가능
        if ("19".equals(detail.getStateCode()) && detail.getCancelYn() == 1) {
          result = false;
          msg = ResponseMessage.CHECK_TEST_STATE;
          
          BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

          return res;
        }
        // 최신 상태가 프로젝트완료이면 상태변경 불가능
        else if ("18".equals(detail.getStateCode())) {
          result = false;
          msg = ResponseMessage.CHECK_TEST_STATE_END;
          
          BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

          return res;
        }
      }
      
      // 사유 필수 입력 체크
      if (Arrays.asList("5", "19", "3", "4").contains(req.getStateCode()) && "".equals(req.getMemo().trim())) {
        result = false;
        msg = ResponseMessage.ERROR_WHY;
        
        BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

        return res;
      }
      
      result = tstService.testStateInsert(req);
    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }

  @ApiOperation(value = "시험상태 변경 내역")
  @GetMapping(value = "/testStateList.do")
  public BasicResponse testStateList(@ApiParam(value = "시험 고유번호", required = true,
      example = "1") @RequestParam(value = "testSeq") String testSeq) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<TestDTO.Res> list = new ArrayList<TestDTO.Res>();

    list = tstService.testStateList(testSeq);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(list).build();

    return res;
  }

  @ApiOperation(value = "시험게시판 등록", notes = "testSeq:시험고유항목, memo:메모")
  @PostMapping(value = "/testBoardInsert.do")
  public BasicResponse testBoardInsert(@RequestBody TestDTO.Req req) throws Exception {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());

    boolean result = false;
    String msg = "";

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      result = tstService.testBoardInsert(req);
    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }

  @ApiOperation(value = "시험게시판 내역")
  @GetMapping(value = "/testBoardList.do")
  public BasicResponse testBoardList(@ApiParam(value = "시험 고유번호", required = true,
      example = "1") @RequestParam(value = "testSeq") String testSeq) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<TestDTO.Res> list = new ArrayList<TestDTO.Res>();

    list = tstService.testBoardList(testSeq);
    
    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }
    
    // 확인요망여부
    int checkYn = tstService.selectCheckInfo(testSeq);

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(list).summary(checkYn).build();

    return res;
  }

  @ApiOperation(value = "시험게시판 신청기기 내역")
  @GetMapping(value = "/testBoardAppDetail.do")
  public BasicResponse testBoardAppDetail(@ApiParam(value = "신청서 고유번호", required = true,
      example = "SB23-G0052") @RequestParam(value = "sbkId") String sbkId) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    SbkDTO.Res detail = new SbkDTO.Res();

    detail = tstService.testBoardAppDetail(sbkId);

    if (detail == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();

    return res;
  }

  @ApiOperation(value = "성적서발급일 추가")
  @PostMapping(value = "/insertReportDt.do")
  public BasicResponse insertReportDt(
      @ApiParam(value = "testSeq:시험고유항목, reportDt:성적서발급일 값 전송") @RequestBody TestDTO.Req req)
      throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());

    System.out.println("=-===========");
    System.out.println(req.toString());
    System.out.println("=-===========");

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      // 이미 연결된 시험이 있는지 확인
      if (tstService.selectDetail(req) != null) {
        result = false;
        msg = ResponseMessage.DUPLICATE_TEST;
      } else {

        try {

          // 성적서 발급일 추가
          result = tstService.update(req);

        } catch (Exception e) {

          msg = ResponseMessage.RETRY;
          log.warn(user.getId() + " :: " + e.toString());
          log.warn(req.toString());
          log.warn("");

        }
      }

    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;

  }
  
  @ApiOperation(value = "시험리스트 (부서장) ",
      notes = "결과값은 TestDTO.Res 참고" + "2.검색박스는 공통코드 CS, 필요한항목만 노출시켜서 사용\n"
          + " 신청구분:신규-1,기술기준변경-2,동일기자재-3,기술기준외변경-4\n" + " 시험부(TT), 미배정-9999\n" + " 시험상태(TS)")
  @GetMapping(value = "/rev/list.do")
  public BasicResponse testRevList(@ModelAttribute ComParam param) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<TestDTO.Res> list = new ArrayList<TestDTO.Res>();

    param.setMemId(user.getId());
    param.setSecretYn(user.getSecretYn());
    
    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    if (!isAuthenticated) {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
      
      BasicResponse res =
          BasicResponse.builder().result(result).message(msg).build();

      return res;
    }
    
    /**
     * OR 조건 검색 처리
     */
    // 같은 CODE로 그룹핑
    Map<String, List<SearchVO>> reSearch = param.getSearchVO().stream().collect(Collectors.groupingBy(SearchVO::getSearchCode));
    
    // 받은 searchCode 삭제 후 다시 생성
//    param.getSearchVO().stream().filter(x -> "23".equals(x.getSearchCode()) || "22".equals(x.getSearchCode())).collect(Collectors.toList()).forEach(x -> {param.getSearchVO().remove(x);});

    SearchVO newSearch = new SearchVO();
    // 시험부
    if (reSearch.get("23") != null) {
      newSearch = new SearchVO();
      newSearch.setSearchCode("23");
      newSearch.setSearchWords(reSearch.get("23").stream().map(m -> m.getSearchWord()).collect(Collectors.toList()));
      param.getSearchVO().add(newSearch);
    }
    
    // 신청구분
    if (reSearch.get("22") != null) {
      newSearch = new SearchVO();
      newSearch.setSearchCode("22");
      newSearch.setSearchWords(reSearch.get("22").stream().map(m -> m.getSearchWord()).collect(Collectors.toList()));
      param.getSearchVO().add(newSearch);
    }
    
    // 시험상태
    if (reSearch.get("31") != null) {
      newSearch = new SearchVO();
      newSearch.setSearchCode("31");
      newSearch.setSearchWords(reSearch.get("31").stream().map(m -> m.getSearchWord()).collect(Collectors.toList()));
      param.getSearchVO().add(newSearch);
    }
    /**
     * -- END OR 조건 검색 처리
     */
    
    // 페이징
    param.setPageUnit(param.getPageUnit());
    param.setPageSize(propertyService.getInt("pageSize"));

    PagingVO pagingVO = new PagingVO();

    pagingVO.setCurrentPageNo(param.getPageIndex());
    pagingVO.setDisplayRow(param.getPageUnit());
    pagingVO.setDisplayPage(param.getPageSize());

    param.setFirstIndex(pagingVO.getFirstRecordIndex());
    int cnt = tstService.selectListCnt(param);

    param.setTotalCount(cnt);
    pagingVO.setTotalCount(cnt);
    pagingVO.setTotalPage(
        (int) Math.ceil(pagingVO.getTotalCount() / (double) pagingVO.getDisplayRow()));
    list = tstService.selectRevList(param);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res =
        BasicResponse.builder().result(result).message(msg).data(list).paging(pagingVO).build();

    return res;
  }

  @ApiOperation(value = "시험취소 내역")
  @GetMapping(value = "/cancel/detail.do")
  public BasicResponse canCelDetail(@ApiParam(value = "시험규격 고유번호", required = true) @RequestParam(value = "testItemSeq") int testItemSeq) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    CanCelDTO detail = new CanCelDTO();

    detail = tstService.cancelInfo(testItemSeq);

    if (detail == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();

    return res;
  }
  
  @ApiOperation(value = "시험취소 등록")
  @PostMapping(value = "/cancel/insert.do")
  public BasicResponse cancelInsert(@RequestBody CanCelDTO req) throws Exception {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());

    boolean result = false;
    String msg = "";

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      result = tstService.cancelInsert(req);
    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }
  
  @ApiOperation(value = "시험게시판 확인요망", notes = "testSeq:시험고유항목, checkYn:확인요망여부")
  @PostMapping(value = "/check/insert.do")
  public BasicResponse testCheckInsert(@RequestBody TestDTO.Req req) throws Exception {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());

    boolean result = false;
    String msg = "";

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      result = tstService.checkInsert(req);
    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }
  
  @ApiOperation(value = "시험게시판 고지부메모", notes = "sbkId:신청서번호, saleMemo:고지부메모")
  @PostMapping(value = "/saleMemo/insert.do")
  public BasicResponse saleMemoInsert(@RequestBody TestDTO.Req req) throws Exception {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());

    boolean result = false;
    String msg = "";

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      result = tstService.saleMemoInsert(req);
    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }
  
}
