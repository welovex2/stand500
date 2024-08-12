package egovframework.quo.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFCreationHelper;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.MediaType;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import egovframework.cmm.service.BasicResponse;
import egovframework.cmm.service.ComParam;
import egovframework.cmm.service.EgovFileMngService;
import egovframework.cmm.service.FileVO;
import egovframework.cmm.service.HisDTO;
import egovframework.cmm.service.LoginVO;
import egovframework.cmm.service.PagingVO;
import egovframework.cmm.service.ResponseMessage;
import egovframework.cmm.service.SearchVO;
import egovframework.cmm.util.EgovFileMngUtil;
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.quo.dto.EngQuoDTO;
import egovframework.quo.service.QuoDTO;
import egovframework.quo.service.QuoModDTO;
import egovframework.quo.service.QuoService;
import egovframework.rte.fdl.property.EgovPropertyService;
import egovframework.sls.service.SlsDTO;
import egovframework.sls.service.SlsSummary;
import egovframework.tst.dto.TestItemDTO;
import egovframework.tst.dto.TestDTO.Res;
import egovframework.tst.service.TestItem;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Api(tags = {"견적서"})
@RestController
@RequestMapping("/quo")
public class QuoController {

  @Resource(name = "QuoService")
  private QuoService quoService;

  @Resource(name = "EgovFileMngUtil")
  private EgovFileMngUtil fileUtil;

  @Resource(name = "EgovFileMngService")
  private EgovFileMngService fileMngService;

  @Resource(name = "propertiesService")
  protected EgovPropertyService propertyService;

  @ApiOperation(value = "견적서리스트",
      notes = "1. 결과값은 QuoDTO.Res 참고\n" + "2. 검색박스는 공통코드 CS, 필요한항목만 노출시켜서 사용\n" + "3. 고객유형(PT)")
  @GetMapping(value = "/list.do")
  public BasicResponse quoList(@ModelAttribute ComParam param) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<QuoDTO.Res> list = new ArrayList<QuoDTO.Res>();

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    if (!isAuthenticated) {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
      
      BasicResponse res =
          BasicResponse.builder().result(result).message(msg).build();

      return res;
    }
    
    param.setMemId(user.getId());
    param.setSecretYn(user.getSecretYn());
    
    /**
     * OR 조건 검색 처리
     */
    List<String> new22 = new ArrayList<String>();
    boolean new22Yn = false;
    for (SearchVO search : param.getSearchVO()) {
      
      if ("22".equals(search.getSearchCode())) {
        new22Yn = true;
        if ("1".equals(search.getSearchWord())) new22.add("SG_NEW_YN");
        else if ("2".equals(search.getSearchWord())) new22.add("SG_GB_YN");
        else if ("3".equals(search.getSearchWord())) new22.add("SG_DG_YN");
        else if ("4".equals(search.getSearchWord())) new22.add("SG_ETC_YN");
        
      }
      
    }
    // 받은 searchCode 삭제 후 다시 생성
    param.getSearchVO().stream().filter(x -> "22".equals(x.getSearchCode())).collect(Collectors.toList()).forEach(x -> {param.getSearchVO().remove(x);});

    SearchVO newSearch = new SearchVO();
    if (new22Yn) {
      newSearch = new SearchVO();
      newSearch.setSearchCode("22");
      newSearch.setSearchWords(new22);
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
    int cnt = quoService.selectListCnt(param);

    param.setTotalCount(cnt);
    pagingVO.setTotalCount(cnt);
    pagingVO.setTotalPage(
        (int) Math.ceil(pagingVO.getTotalCount() / (double) pagingVO.getDisplayRow()));
    list = quoService.selectList(param);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    /*
     * 순매출총합
     */
    SlsSummary summ = new SlsSummary();
    summ.setTotalCnt(cnt);
    summ.setTotal(list.stream().mapToInt(QuoDTO.Res::getChrgs).sum());
    summ.setTotalCnt(list.size());
    summ.setTotalNetSales(list.stream().mapToInt(QuoDTO.Res::getNetSales).sum());
    summ.setNetSalesCnt(list.size());
    
    BasicResponse res =
        BasicResponse.builder().result(result).message(msg).summary(summ).data(list).paging(pagingVO).build();

    return res;
  }

  @ApiOperation(value = "견적서 상세보기", notes = "견적서상태 : 0 매출확정전, 1 매출확정, 2 수정요청, 3 수정허가, 4 수정완료")
  @GetMapping(value = "/{quoId}/detail.do")
  public BasicResponse quoDetail(@ApiParam(value = "견적서 고유번호", required = true,
      example = "Q2303-G0018") @PathVariable(name = "quoId") String quoId) throws Exception {

    boolean result = true;
    String msg = "";
    QuoDTO.Res detail = new QuoDTO.Res();
    QuoDTO.Req req = new QuoDTO.Req();

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    if (!isAuthenticated) {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
      
      BasicResponse res =
          BasicResponse.builder().result(result).message(msg).build();

      return res;
    }
    req.setMemId(user.getId());
    req.setSecretYn(user.getSecretYn());
    
    req.setQuoId(quoId);
    detail = quoService.selectDetail(req);

    if (detail == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();

    return res;
  }

  // @ApiOperation(value = "견적서 발급번호 생성")
  // @GetMapping(value="/getRef.do")
  // public String quoRef(@ApiParam(value = "견적서 종류(G:일반견적서, M:의료견적서)", required = true, example =
  // "G") @RequestParam String type) throws Exception{
  //
  // String req = quoService.selectRef(type);
  //
  // return req;
  // }

  @ApiOperation(value = "시험규격만 수정", notes = "testItemSeq, product, model에만 값넣어서 전송")
  @PostMapping(value = "/item/update.do")
  public BasicResponse quoItemUpdate(@RequestBody List<TestItem> req) throws Exception {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";
    boolean result = false;

    // 로그인정보
//    req.setInsMemId(user.getId());
//    req.setUdtMemId(user.getId());

    System.out.println("=-===========");
    System.out.println(req.toString());
    System.out.println("=-===========");
    
    try {
      
      List<TestItem> iItems = req.stream().map(item -> {
                                                          item.setUdtMemId(user.getId());
                                                          return item;
                                                          }).collect(Collectors.toList());
      
        result = quoService.updateTestItemModel(req);
    } catch (Exception e) {
      
      msg = ResponseMessage.RETRY;
      log.warn(user.getId() + " :: " + e.toString());
      log.warn(req.toString());
      log.warn("");
      
    }
    
    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
    
  }
  
  @ApiOperation(value = "견적서 등록", notes = "견적서 수정시 고유번호(quoSeq) 필수")
  @PostMapping(value = "/insert.do")
  public BasicResponse quoInsert(
      @RequestPart(value = "quo") QuoDTO.Req req) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";
    boolean result = false;

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());

    System.out.println("=-===========");
    System.out.println(req.toString());
    System.out.println("=-===========");
    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    Validator validator = validatorFactory.getValidator();

    Set<ConstraintViolation<QuoDTO.Req>> violations = validator.validate(req);

    for (ConstraintViolation<QuoDTO.Req> violation : violations) {
      msg = violation.getMessage();

      System.out.println(msg);
      BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

      return res;
    }

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      
      // 삭제 불가능한 시험항목이 있는지 확인
      List<TestItem> dItems = req.getTestItems().stream().filter(t -> "D".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(dItems)) {
          int cnt = quoService.checkTestStartItem(dItems);
          
          if (cnt > 0) {
            result = false;
            msg = ResponseMessage.ERROR_ITEM_DELETE;
            
            BasicResponse res = BasicResponse.builder().result(result).message(msg).build();
            return res;
          }
      }
      // 필수체크
      else if (StringUtils.isEmpty(req.getQuoId()) && StringUtils.isEmpty(req.getMngId())) {
        
        result = false;
        msg = ResponseMessage.ERROR_MNG;
        
        BasicResponse res = BasicResponse.builder().result(result).message(msg).build();
        return res;
        
      } 
      
//      List<FileVO> FileResult = null;
//
//      final List<MultipartFile> files = multiRequest;
//      String atchFileId = "";
//      if (!files.isEmpty()) {
//        FileResult = fileUtil.parseFile(files, "QUO", 0, "", "");
//        atchFileId = fileMngService.insertFileInfs(FileResult);
//        req.setSgnUrl(atchFileId);
//      }

      try {
        if (StringUtils.isEmpty(req.getQuoId()))
          result = quoService.insert(req);
        else
          result = quoService.update(req);
      } catch (Exception e) {
        
        msg = ResponseMessage.RETRY;
        log.warn(user.getId() + " :: " + e.toString());
        log.warn(req.toString());
        log.warn("");
        
      }
    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(req.getQuoId()).build();

    return res;
  }

  @ApiOperation(value = "견적서 수정요청",
      notes = "stateCode : 수정요청 = 2,  수정허용 = 3, 수정완료 = 4, 세금계산서신청 = 5\nbill = 세금계산서 신청금액")
  @PostMapping(value = "/updateStatus.do")
  public BasicResponse updateStatus(
      @ApiParam(value = "quoId, memo, stateCode, bill 입력") @RequestBody QuoModDTO.Req req)
      throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());

    boolean result = false;
    String msg = "";

    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    Validator validator = validatorFactory.getValidator();

    Set<ConstraintViolation<QuoModDTO.Req>> violations = validator.validate(req);

    for (ConstraintViolation<QuoModDTO.Req> violation : violations) {
      msg = violation.getMessage();

      BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

      return res;
    }

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      
      try {
        // 신청금액 필수값 체크
        if ("5".equals(req.getStateCode())) {
          
          // 신청금액은 0 보다 커야함
          if (req.getBill() <= 0) {
            result = false;
            msg = ResponseMessage.NO_DATA;
          } else {
            int arrerars = quoService.billCheck(req.getQuoId());
            // 신청금액은 미수금보다 크면 오류
            if (arrerars < req.getBill()) {
              result = false;
              msg = ResponseMessage.ERROR_BILL;
            } else {
               result = quoService.updateStatus(req);
            }
          }
        } else {
          // 구버전 견적서 수정은 허용되지 않음
          // result = quoService.updateStatus(req);
          result = false;
          msg = "더이상 견적서 수정요청 기능을 사용할 수 없습니다.";
          
        }
      } catch (Exception e) {
        
        msg = ResponseMessage.RETRY;
        log.warn(user.getId() + " :: " + e.toString());
        log.warn(req.toString());
        log.warn("");
        
      }
      
    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }

  @ApiOperation(value = "수정요청리스트")
  @GetMapping(value = "/statusList.do")
  public BasicResponse statusList(@ApiParam(value = "견적서 고유번호", required = true,
      example = "Q2303-G0032") @RequestParam(value = "quoSeq") String quoSeq) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<QuoModDTO.Res> list = new ArrayList<QuoModDTO.Res>();

    list = quoService.selectStatusList(quoSeq);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(list).build();

    return res;
  }

  @ApiOperation(value = "본건 견적서 작성")
  @PostMapping(value = "/makeQuo.do")
  public BasicResponse makeQuo(@ApiParam(value = "sbkId 값만 전송") @RequestBody QuoDTO.Req req)
      throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    QuoDTO.Res detail = new QuoDTO.Res();

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());

    System.out.println("================");
    System.out.println(req.toString());
    System.out.println("================");
    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      // 이미 연결된 견적서가 있는지 확인
      if (quoService.selectDetail(req) != null) {
        result = false;
        msg = ResponseMessage.DUPLICATE_QUO;
      } else {

        // 견적서 생성
        req.setVatYn(1);
        result = quoService.insert(req);

        // 견적서 정보 보내주기
        detail = quoService.selectDetail(req);

        if (detail == null) {
          result = false;
          msg = ResponseMessage.NO_DATA;
        }

      }

    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();

    return res;

  }

  @ApiOperation(value = "견적서 수정 히스토리")
  @GetMapping(value = "/hisList.do")
  public BasicResponse hisList(@ApiParam(value = "견적서 고유번호", required = true,
      example = "Q2303-G0086") @RequestParam(value = "quoId") String quoId) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<HisDTO> list = new ArrayList<HisDTO>();

    list = quoService.hisList(quoId);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(list).build();

    return res;
  }

  @ApiOperation(value = "영문견적서 등록", notes = "견적서 고유번호(quoId) 필수, engMemo, engTestItems")
  @PostMapping(value = "/eng/insert.do")
  public BasicResponse quoInsert(@RequestBody EngQuoDTO quo) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";
    boolean result = false;

    // 로그인정보
    quo.setInsMemId(user.getId());
    quo.setUdtMemId(user.getId());

    System.out.println("=-===========");
    System.out.println(quo.toString());
    System.out.println("=-===========");
    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    Validator validator = validatorFactory.getValidator();

    Set<ConstraintViolation<EngQuoDTO>> violations = validator.validate(quo);

    for (ConstraintViolation<EngQuoDTO> violation : violations) {
      msg = violation.getMessage();

      System.out.println(msg);
      BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

      return res;
    }

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {

      result = quoService.engInsert(quo);

    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }

  @ApiOperation(value = "영문 견적서 상세보기", notes = "engName, engMemo, engItems")
  @GetMapping(value = "/eng/{quoId}/detail.do")
  public BasicResponse quoEngDetail(@ApiParam(value = "견적서 고유번호", required = true,
      example = "Q2303-G0018") @PathVariable(name = "quoId") String quoId) throws Exception {

    boolean result = true;
    String msg = "";
    QuoDTO.Res detail = new QuoDTO.Res();
    QuoDTO.Req req = new QuoDTO.Req();
    EngQuoDTO engDetail = new EngQuoDTO();
    
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    
    req.setMemId(user.getId());
    req.setSecretYn(user.getSecretYn());
    req.setQuoId(quoId);
    detail = quoService.selectDetail(req);

    if (detail == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    } else {
      detail.setEngItems(quoService.selectEngTestItem(quoId));
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();

    return res;
  }
  
  @ApiOperation(value = "견적서 엑셀 폼 다운")
  @GetMapping(value = "/{quoId}/excelDown.do")
  public void excelDown(@ApiParam(value = "견적서 고유번호", required = true,example = "Q2312-G1306") @PathVariable(name = "quoId") String quoId
      , HttpServletResponse response) throws Exception {

//    String filePath = "C:\\Users\\김정미\\Desktop\\STB_FORM.xlsx"; // 불러올 파일
    String filePath = propertyService.getString("Globals.fileStorePath").concat("QUO_FORM.xlsx"); // 불러올 파일
    
    QuoDTO.Req req = new QuoDTO.Req();
    QuoDTO.Res detail = new QuoDTO.Res();
    
    req.setQuoId(quoId);
    detail = quoService.selectDetail(req);

    // 1. FileInputStream 으로 파일 읽기
    FileInputStream inputStream = new FileInputStream(filePath);
    
    // 2. XSSFWorkbook 객체 생성하기
    XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
    
    // 3. XSSFSheet 객체 생성 - 첫번째 시트를 가져온다
    XSSFSheet sheet = workbook.getSheetAt(0);

    if (detail != null) {
      
//      try { 
        // 4. XSSFRow 첫번째 Row 가져와서 수정하기
        XSSFRow row = sheet.getRow(3); // 시트의 4번째 Row 를 가져온다
//        row.getCell(8).setCellValue(detail.getVersion().trim());               // 견적서 Ver
        
        row = sheet.getRow(4);  // 견적서 발급번호
        row.getCell(4).setCellValue(detail.getQuoId().trim());               
        
        row = sheet.getRow(10);  // 업체명, 발행일
        row.getCell(3).setCellValue(detail.getCmpyName().trim());               
        row.getCell(8).setCellValue(detail.getIssueDt().trim());
        
        row = sheet.getRow(11);  // 담당자, 전화번호
        row.getCell(3).setCellValue(detail.getMngName().trim());               
        row.getCell(8).setCellValue(detail.getMngPhone().trim());
        
        row = sheet.getRow(12);  // 작성자, 팩스번호
        row.getCell(3).setCellValue(detail.getMemName().trim());               
        row.getCell(8).setCellValue("");
        
        row = sheet.getRow(15);  // 제품명
        row.getCell(3).setCellValue(detail.getPrdctName().trim());
        row = sheet.getRow(16);  // 모델명
        row.getCell(3).setCellValue(detail.getModelName().trim());
        row = sheet.getRow(17);  // 대상인증
        row.getCell(3).setCellValue(detail.getTrgtCrtfc().trim());
        row = sheet.getRow(18);  // 제품설명
//        row.getCell(3).setCellValue(detail.getPrdInf().trim());
        
        row = sheet.getRow(19);  // 사용전원?
        row.getCell(9).setCellValue(detail.getPowerSuplyYn() == 1 ? "YES" : "NO");
        
        row = sheet.getRow(20);  // 제품무게?
        row.getCell(9).setCellValue(detail.getWghtYn() == 1 ? "YES" : "NO");
        
        row = sheet.getRow(23);  // 제품 특이사항
        row.getCell(1).setCellValue(detail.getMemo().trim());
        
        int r = 28;
        for (TestItemDTO item : detail.getItems()) {
          r++;
          
          row = sheet.getRow(r);  // 시험규격
          row.getCell(1).setCellValue(item.getTestStndr().trim());
          row.getCell(3).setCellValue(item.getTestType().trim());
          row.getCell(4).setCellValue(item.getMemo().trim());
          row.getCell(6).setCellValue(item.getFee() * 1.0);    // 접수비
          row.getCell(7).setCellValue(item.getLcnsTax() * 1.0);    // 먼허세
          row.getCell(8).setCellValue(item.getSpclDscnt() * -1.0);  // 개정금액
          row.getCell(9).setCellValue(item.getTestFee() * 1.0);  // 시험비
          
          if (r == 32) break;
        }
        
        row = sheet.getRow(33); // 부가가치세, 청구액 
        row.getCell(3).setCellValue(detail.getVatYn() == 1 ? "YES" : "NO");
        row.getCell(8).setCellValue(Double.valueOf(detail.getCostTotal().trim()));
        
        row = sheet.getRow(34); // 총합계 
        row.getCell(8).setCellValue(Double.valueOf(detail.getTotalVat().trim()));
        
        row = sheet.getRow(37); // 소요시간
        row.getCell(1).setCellValue("상기 의뢰 업무를 수행하는 데 ".concat(Integer.toString(detail.getNeedWeek())).concat("주 정도 소요될 것으로 예상됩니다. 일정은 사전에 협의되어야 하며 업무 시작일은 시험비용 납입 확인 및 시료 도착, \r\n" + 
            "신청 정보 회신이 모두 이루어진 시점을 기준으로 합니다. 긴급 진행의 경우 담당자와 상담 해주시기 바랍니다."));
        row = sheet.getRow(38); // 소요시간
        row.getCell(1).setCellValue("Please anticipate a ".concat(Integer.toString(detail.getNeedWeek())).concat("Weeks for the scope listed above. It need to be scheduled in advance and business start is needed to satisfy all the \r\n" + 
            "conditions as cost payment, sample arrives, receiving application form. The expedited scheduling shall be consultation with representatives"));
    
        row = sheet.getRow(42); // 특약조건
        row.getCell(1).setCellValue(detail.getSpclCndtn().trim());
        
        row = sheet.getRow(48); // 이름
        row.getCell(1).setCellValue(detail.getMemPos().trim());
        row = sheet.getRow(49); // dir
        row.getCell(1).setCellValue("Dir : 031-393-9394");
        row = sheet.getRow(50); // cp
        row.getCell(1).setCellValue("C.P : ".concat(detail.getCp().trim()));
        row = sheet.getRow(51); // email
        row.getCell(1).setCellValue("E-Mail : ".concat(detail.getEmail().trim()));
        
        /*
         * 서명 삭제
        // 사진
        FileVO fileVO = new FileVO();
        
        String url = detail.getSgnUrl();

        // 정규식 패턴
        String pattern = "atchFileId=([A-Za-z0-9_]+)";

        // 패턴을 컴파일
        Pattern regexPattern = Pattern.compile(pattern);

        // 문자열과 패턴을 매치
        Matcher matcher = regexPattern.matcher(url);

        // 매치된 경우 값 출력
        if (matcher.find()) {
            String fileId = matcher.group(1);
            fileVO.setAtchFileId(fileId);
            fileVO.setFileSn("0");
        }
        FileVO fvo = fileMngService.selectFileInf(fileVO);
        
        
        File file = new File(fvo.getFileStreCours(), fvo.getStreFileNm());
        InputStream is = new FileInputStream(file);

        byte[] bytes = IOUtils.toByteArray(is);
        int picIdx = workbook.addPicture(bytes, XSSFWorkbook.PICTURE_TYPE_PNG);
        is.close();

        XSSFCreationHelper helper = workbook.getCreationHelper();
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = helper.createClientAnchor();
        
        // 이미지 출력할 cell 위치
        anchor.setCol1(2);
        anchor.setRow1(45);
        anchor.setCol2(4);
        anchor.setRow2(47);
        
        // 이미지 그리기
        XSSFPicture pic = drawing.createPicture(anchor, picIdx);
//        pic.resize();
        */
 
        
//      } catch (Exception e) {
//        log.error(e.getMessage());
//      }
    }
    
    // 6. 파일다운로드로 저장하기
    String fileName = URLEncoder.encode(quoId + "_견적서" + detail.getCmpyName().trim() + "_" + detail.getModelName() + "_" + detail.getTrgtCrtfc(), "UTF-8");
    fileName = fileName.replaceAll("\\+", "%20");
    
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    // 응답이 파일 타입이라는 것을 명시
    response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
    ServletOutputStream servletOutputStream = response.getOutputStream();
//    XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
    workbook.setForceFormulaRecalculation(true);
    workbook.write(servletOutputStream);
    
    // 7. 자원 반환
//    out.close();
    servletOutputStream.close();
    workbook.close();
    inputStream.close();

//    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(list).build();

//    return res;
  }

}
