package egovframework.sbk.web;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
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
import egovframework.rte.fdl.property.EgovPropertyService;
import egovframework.sbk.service.SbkDTO;
import egovframework.sbk.service.SbkService;
import egovframework.tst.dto.TestItemDTO;
import egovframework.tst.service.TestItemRej;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Api(tags = {"신청서"})
@RestController
@RequestMapping("/sbk")
public class SbkController {

  @Resource(name = "SbkService")
  private SbkService sbkService;

  @Resource(name = "EgovFileMngUtil")
  private EgovFileMngUtil fileUtil;

  @Resource(name = "EgovFileMngService")
  private EgovFileMngService fileMngService;

  @Resource(name = "propertiesService")
  protected EgovPropertyService propertyService;

  @ApiOperation(value = "본건 신청서 작성")
  @PostMapping(value = "/makeSbk.do")
  public BasicResponse makeSbk(@ApiParam(value = "quoId 값만 전송") @RequestBody SbkDTO.Req req)
      throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    SbkDTO.Res detail = new SbkDTO.Res();

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());
    req.setMemId(user.getId());
    req.setSecretYn(user.getSecretYn());
    
    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      // 이미 연결된 신청서가 있는지 확인
      if (sbkService.selectDetail(req) != null) {
        result = false;
        msg = ResponseMessage.DUPLICATE_SBK;
      } else {

        // 직고객 정보가 있는 경우 직고객 정보 초기 셋팅해주지 뭐
        detail = sbkService.selectDirtInfo(req);
        req.setCmpyName(detail.getCmpyName());
        req.setBsnsRgnmb(detail.getBsnsRgnmb());
        req.setRprsn(detail.getRprsn());
        req.setCrprtRgnmb(detail.getCrprtRgnmb());
        req.setAddress(detail.getAddress());
        req.setRsdntRgnmb(detail.getRsdntRgnmb());
        req.setCmpnyIdntf(detail.getCmpnyIdntf());
        req.setMngName(detail.getMngName());
        req.setMngTel(detail.getMngTel());
        req.setMngPhone(detail.getMngPhone());
        req.setMngEmail(detail.getMngEmail());
        req.setMngFax(detail.getMngFax());
        
        // 신청서 생성
        result = sbkService.insert(req);

        // 신청서 정보 보내주기
        detail = sbkService.selectDetail(req);

        if (detail == null) {
          result = false;
          msg = ResponseMessage.NO_DATA;
        }

      }

    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();

    return res;

  }

  @ApiOperation(value = "신청서리스트",
      notes = "1.결과값은 StbDTO.Res 참고\n" + "2.검색박스는 공통코드 CS, 필요한항목만 노출시켜서 사용\n" + " 고객유형(PT)\n"
          + " 신청구분:신규-1,기술기준변경-2,동일기자재-3,기술기준외변경-4\n" + " 시험배정(TT), 미배정-9999")
  @GetMapping(value = "/list.do")
  public BasicResponse sbkList(@ModelAttribute ComParam param) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<SbkDTO.Res> list = new ArrayList<SbkDTO.Res>();

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    if (!isAuthenticated) {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;

      BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

      return res;
    }
    param.setMemId(user.getId());
    param.setSecretYn(user.getSecretYn());
    
    /**
     * OR 조건 검색 처리
     */
    List<String> new25 = new ArrayList<String>();
    List<String> new22 = new ArrayList<String>();
    boolean new25Yn = false, new22Yn = false;
    for (SearchVO search : param.getSearchVO()) {
      // 1. 시험부(23)
      if ("23".equals(search.getSearchCode())) {
        new25Yn = true;
        new25.add(search.getSearchWord());
      }
      // 2. 신청구분(22)
      else if ("22".equals(search.getSearchCode())) {
        new22Yn = true;
        if ("1".equals(search.getSearchWord())) new22.add("SG_NEW_YN");
        else if ("2".equals(search.getSearchWord())) new22.add("SG_GB_YN");
        else if ("3".equals(search.getSearchWord())) new22.add("SG_DG_YN");
        else if ("4".equals(search.getSearchWord())) new22.add("SG_ETC_YN");
        
      }
    }
    // 받은 searchCode 삭제 후 다시 생성
    param.getSearchVO().stream().filter(x -> "23".equals(x.getSearchCode()) || "22".equals(x.getSearchCode())).collect(Collectors.toList()).forEach(x -> {param.getSearchVO().remove(x);});
    
    SearchVO newSearch = new SearchVO();
    if (new25Yn) {
      newSearch = new SearchVO();
      newSearch.setSearchCode("23");
      newSearch.setSearchWords(new25);
      param.getSearchVO().add(newSearch);
    }
    
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
    int cnt = sbkService.selectListCnt(param);

    pagingVO.setTotalCount(cnt);
    param.setTotalCount(cnt);
    pagingVO.setTotalPage(
        (int) Math.ceil(pagingVO.getTotalCount() / (double) pagingVO.getDisplayRow()));
    list = sbkService.selectList(param);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res =
        BasicResponse.builder().result(result).message(msg).data(list).paging(pagingVO).build();

    return res;
  }

  @ApiOperation(value = "신청서 저장",
      notes = "appFile=신청인서명, agreeFile=동의서명, workFile=업무상담자 서명\nsendFile=제출서류, delFileList=제출서류삭제")
  @PostMapping(value = "/insert.do",
      consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE,
          MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
  public BasicResponse insert(
      @RequestPart(value = "appFile", required = false) MultipartFile appFile,
      @RequestPart(value = "agreeFile", required = false) MultipartFile agreeFile,
//      @RequestPart(value = "workFile", required = false) MultipartFile workFile,
      @RequestPart(value = "sendFile", required = false) List<MultipartFile> sendFile,
      @RequestPart(value = "delFileList", required = false) List<FileVO> delFileList,
      @RequestPart(value = "sbk") SbkDTO.Req req) throws Exception {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";
    boolean result = false;
    // if (user == null) { return new ResponseEntity(BasicResponse.res(StatusCode.UNAUTHORIZED,
    // ResponseMessage.NO_LOGIN), HttpStatus.OK); }

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());

    System.out.println("=-===========");
    System.out.println(req.toString());
    System.out.println("=-===========");
    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    Validator validator = validatorFactory.getValidator();

    Set<ConstraintViolation<SbkDTO.Req>> violations = validator.validate(req);

    for (ConstraintViolation<SbkDTO.Req> violation : violations) {
      msg = violation.getMessage();

      System.out.println(msg);
      BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

      return res;
    }

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      
      // 필수체크
      if (StringUtils.isEmpty(req.getSbkId()) && StringUtils.isEmpty(req.getMngId())) {
        
        result = false;
        msg = ResponseMessage.ERROR_MNG;
        
        BasicResponse res = BasicResponse.builder().result(result).message(msg).build();
        return res;
        
      } 
      
      
      FileVO FileResult = null;
      List<FileVO> FileResults = null;
      String atchFileId = "";
      // 신청인 서명
      if (!ObjectUtils.isEmpty(appFile)) {
        FileResult = fileUtil.parseFile(appFile, "SBK", 0, "", "");
        atchFileId = fileMngService.insertFileInf(FileResult);
        req.setAppSignUrl(atchFileId);
      }

      // 신청인 동의 서명
      if (!ObjectUtils.isEmpty(agreeFile)) {
        FileResult = fileUtil.parseFile(agreeFile, "SBK", 0, "", "");
        atchFileId = fileMngService.insertFileInf(FileResult);
        req.setAppAgreeSignUrl(atchFileId);
      }

      // 업무자 서명
//      if (!ObjectUtils.isEmpty(workFile)) {
//        FileResult = fileUtil.parseFile(workFile, "SBK", 0, "", "");
//        atchFileId = fileMngService.insertFileInf(FileResult);
//        req.setWorkSignUrl(atchFileId);
//      }

      // 신청서류
      if (!ObjectUtils.isEmpty(sendFile)) {

        // 신규등록
        if (StringUtils.isEmpty(req.getDocUrl())) {
          FileResults = fileUtil.parseFile(sendFile, "SBK/DOC", 0, "", "");
          atchFileId = fileMngService.insertFileInfs(FileResults);
          req.setDocUrl(atchFileId);
        }
        // 수정
        else {
          // 현재 등록된 파일 수 가져오기
          FileVO fvo = new FileVO();
          fvo.setAtchFileId(req.getDocUrl());
          int cnt = fileMngService.getMaxFileSN(fvo);

          // 추가 파일 등록
          List<FileVO> _result = fileUtil.parseFile(sendFile, "SBK/DOC", cnt, req.getDocUrl(), "");
          fileMngService.updateFileInfs(_result);
        }
      }

      // 파일삭제
      FileVO delFile = null;
      if (!ObjectUtils.isEmpty(delFileList)) {
        for (FileVO del : delFileList) {
          delFile = new FileVO();
          delFile.setAtchFileId(del.getAtchFileId());
          delFile.setFileSn(del.getFileSn());
          fileMngService.deleteFileInf(delFile);
        }
      }

      try {
        if (StringUtils.isEmpty(req.getSbkId()))
          result = sbkService.insert(req);
        else
          result = sbkService.update(req);
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

  @ApiOperation(value = "신청서 상세보기", notes = "신청서류 파일 다운로드 : data > docFileList > fileStreCours")
  @GetMapping(value = "/{sbkId}/detail.do")
  public BasicResponse sbkDetail(@ApiParam(value = "신청서 고유번호", required = true,
      example = "SB23-G0003") @PathVariable(name = "sbkId") String sbkId) throws Exception {
    boolean result = true;
    String msg = "";
    SbkDTO.Res detail = new SbkDTO.Res();
    SbkDTO.Req req = new SbkDTO.Req();

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    if (!isAuthenticated) {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;

      BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

      return res;
    }

    req.setSbkId(sbkId);
    req.setMemId(user.getId());
    req.setSecretYn(user.getSecretYn());
    detail = sbkService.selectDetail(req);

    if (detail == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    // 신청서류 - 파일리스트
    FileVO fileVO = new FileVO();
    fileVO.setAtchFileId(detail.getDocUrl());
    List<FileVO> docResult = fileMngService.selectFileInfs(fileVO);
    docResult.stream().map(doc -> {
      doc.setFileStreCours("/file/fileDown.do?atchFileId=".concat(doc.getAtchFileId())
          .concat("&fileSn=").concat(doc.getFileSn()));
      return doc;
    }).collect(Collectors.toList());
    detail.setDocFileList(docResult);

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();

    return res;
  }

  @ApiOperation(value = "서명요청하기", notes = "testItemSeq, revId, sendType(S:문자, M:메일), estCmpDt")
  @PostMapping(value = "/signRequest.do")
  public BasicResponse signRequest(@RequestBody TestItemDTO req) throws Exception {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      req.setState("DT");
      result = sbkService.updateTestItemSign(req);
    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }
  
  @ApiOperation(value = "요청완료일 변경하기", notes = "testItemSeq, estCmpDt")
  @PostMapping(value = "/cmpDt/update.do")
  public BasicResponse estCmpDtUpdate(@RequestBody TestItemDTO req) throws Exception {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    System.out.println("=-===========");
    System.out.println(req.toString());
    System.out.println("=-===========");
    
    if (isAuthenticated) {
      req.setState("DT");
      result = sbkService.updateTestItemSign(req);
    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }

  @ApiOperation(value = "기술책임자 서명", notes = "testItemSeq=시험항목번호, state=I:신규, U:수정, D:삭제")
  @PostMapping(value = "/signRev.do",
      consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
  public BasicResponse updateTestItemSign(
      @RequestPart(value = "req") TestItemDTO req) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";
    boolean result = false;
    // if (user == null) { return new ResponseEntity(BasicResponse.res(StatusCode.UNAUTHORIZED,
    // ResponseMessage.NO_LOGIN), HttpStatus.OK); }

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());

    System.out.println("=-===========");
    System.out.println(req.toString());
    System.out.println("=-===========");
    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    Validator validator = validatorFactory.getValidator();

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
//      FileVO FileResult = null;
//
//      String atchFileId = "";
//      if (!ObjectUtils.isEmpty(multiRequest)) {
//        FileResult = fileUtil.parseFile(multiRequest, "SIGN", 0, "", "");
//        atchFileId = fileMngService.insertFileInf(FileResult);
//        req.setRevSignUrl(atchFileId);
//      }

      result = sbkService.updateTestItemSign(req);

    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }


  @ApiOperation(value = "반려메모 내역")
  @GetMapping(value = "/signRejectList.do")
  public BasicResponse signRejectList(
      @ApiParam(value = "시험항목 고유번호", required = true,
          example = "175") @RequestParam(value = "testItemSeq") String testItemSeq)
      throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<TestItemRej> list = new ArrayList<TestItemRej>();

    list = sbkService.signRejectList(testItemSeq);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(list).build();

    return res;
  }

  @ApiOperation(value = "반려메모 저장")
  @PostMapping(value = "/signRejectInsert.do")
  public BasicResponse signRejectInsert(@RequestBody TestItemRej req) throws Exception {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());

    boolean result = false;
    String msg = "";

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      result = sbkService.signRejectInsert(req);
    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }


  @ApiOperation(value = "신청서 수정 히스토리")
  @GetMapping(value = "/hisList.do")
  public BasicResponse hisList(@ApiParam(value = "신청서 고유번호", required = true,
      example = "SB23-G0044") @RequestParam(value = "sbkId") String sbkId) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<HisDTO> list = new ArrayList<HisDTO>();

    list = sbkService.hisList(sbkId);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(list).build();

    return res;
  }


  @ApiOperation(value = "신청서 엑셀 폼 다운")
  @GetMapping(value = "/{sbkId}/excelDown.do")
  public void excelDown(@ApiParam(value = "신청서 고유번호", required = true,example = "SB23-G0044") @PathVariable(name = "sbkId") String sbkId
      , HttpServletResponse response) throws Exception {

//    String filePath = "C:\\Users\\김정미\\Desktop\\STB_FORM.xlsx"; // 불러올 파일
    String filePath = propertyService.getString("Globals.fileStorePath").concat("STB_FORM.xlsx"); // 불러올 파일
    
    SbkDTO.Req req = new SbkDTO.Req();
    SbkDTO.Res detail = new SbkDTO.Res();
    
    req.setSbkId(sbkId);
    detail = sbkService.selectDetail(req);

    // 1. FileInputStream 으로 파일 읽기
    FileInputStream inputStream = new FileInputStream(filePath);
    
    // 2. XSSFWorkbook 객체 생성하기
    XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
    
    // 3. XSSFSheet 객체 생성 - 첫번째 시트를 가져온다
    XSSFSheet sheet = workbook.getSheetAt(0);

    if (detail != null) {
  
      // 4. XSSFRow 첫번째 Row 가져와서 수정하기
      XSSFRow row = sheet.getRow(2); // 시트의 첫번째 Row 를 가져온다
      row.getCell(3).setCellValue(detail.getCmpyName().trim());               // 회사명
      row.getCell(10).setCellValue(detail.getBsnsRgnmb().trim());      // 사업자등록번호
      
      row = sheet.getRow(3);
      row.getCell(3).setCellValue(detail.getRprsn().trim());               // 대표자
      
      row = sheet.getRow(4);
      row.getCell(3).setCellValue(detail.getAddress().trim());                 // 주소
      
      row = sheet.getRow(6);
      row.getCell(4).setCellValue(detail.getMngName().trim());          // 담당자 이름
      row.getCell(10).setCellValue(detail.getMngEmail().trim());       // 담당자 이메일
      
      row = sheet.getRow(7);
      row.getCell(4).setCellValue(detail.getMngPhone().trim());          // 담당자 전화
      row.getCell(7).setCellValue(detail.getMngTel().trim());        // 담당자 핸드폰
      row.getCell(11).setCellValue(detail.getMngFax().trim());         // 담당자 팩스
      
      row = sheet.getRow(8);
      row.getCell(3).setCellValue(detail.getPrdctName().trim());               // 제품명
      
      row = sheet.getRow(9);
      row.getCell(3).setCellValue(detail.getModelName().trim());               // 모델명
      row.getCell(10).setCellValue(detail.getAthntNmbr().trim());            // 인증번호
      
      // 신청서 엑셀 다운로드 기능 (#18) 파생모델란 추가
      row = sheet.getRow(10);
      row.getCell(10).setCellValue(detail.getExtendModel().trim());               // 파생모델
      
      row = sheet.getRow(15);
      row.getCell(3).setCellValue(detail.getMnfctCmpny().trim());               // 회사명
      row.getCell(10).setCellValue(detail.getMnfctCntry().trim());              // 제조국
      
      
      
  //    int lastColNum = row.getLastCellNum(); // 마지막 칼럼의 index 를 구한다
  //    row.createCell(lastColNum).setCellValue("메뉴4"); // 칼럼을 생성한다
  
      // 5. 2번째 Row 부터 데이터 삽입
  //    for (int i = 0; i < 2; i++) {
  //      row = sheet.createRow(i + 1); // row 를 새로 생성한다
  //      row.createCell(3).setCellValue((10 * i) + (i + 4));
  //    }
  
  //    // 6. FileOutputStream 으로 파일 저장하기
  //    FileOutputStream out = new FileOutputStream(filePath);
  //    workbook.write(out);
    }
    
    // 6. 파일다운로드로 저장하기
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    // 응답이 파일 타입이라는 것을 명시
    response.setHeader("Content-Disposition", "attachment;filename=" + sbkId + ".xlsx");
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


  @ApiOperation(value = "신청서 엑셀 업로드")
  @PostMapping(value = "/excelUpload.do")
  public void excelUpload(@RequestPart(value = "excelFile", required = true) MultipartFile excelFile,
      HttpServletResponse response) throws Exception {
    
    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      FileVO FileResult = null;
      List<FileVO> FileResults = null;
      String atchFileId = "";

      if (!ObjectUtils.isEmpty(excelFile)) {
//        FileResult = fileUtil.parseFile(excelFile, "SBK", 0, "", "");
//        atchFileId = fileMngService.insertFileInf(FileResult);
        
        // 1. FileInputStream 으로 파일 읽기
        
        // 2. XSSFWorkbook 객체 생성하기
        XSSFWorkbook workbook = new XSSFWorkbook(excelFile.getInputStream());
        
        // 3. XSSFSheet 객체 생성 - 첫번째 시트를 가져온다
        XSSFSheet sheet = workbook.getSheetAt(0);
        
        // 4. XSSFRow 첫번째 Row 가져와서 수정하기
        XSSFRow row = sheet.getRow(4); // 시트의 첫번째 Row 를 가져온다
        System.out.println(row.getCell(8).getBooleanCellValue());

        
        row = sheet.getRow(5); // 시트의 첫번째 Row 를 가져온다
        System.out.println(row.getCell(12).getBooleanCellValue());
        
      }
    }
    
  }
}
