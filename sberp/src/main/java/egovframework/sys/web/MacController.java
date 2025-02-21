package egovframework.sys.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import egovframework.cmm.service.BasicResponse;
import egovframework.cmm.service.ComParam;
import egovframework.cmm.service.EgovFileMngService;
import egovframework.cmm.service.FileVO;
import egovframework.cmm.service.LoginVO;
import egovframework.cmm.service.PagingVO;
import egovframework.cmm.service.ResponseMessage;
import egovframework.cmm.service.SearchVO;
import egovframework.cmm.util.EgovFileMngUtil;
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.rte.fdl.property.EgovPropertyService;
import egovframework.sys.service.MacCal;
import egovframework.sys.service.MacCalDTO;
import egovframework.sys.service.MacService;
import egovframework.sys.service.MachineDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Api(tags = {"시험장비"})
@RestController
@RequestMapping("/sys/mac")
public class MacController {

  @Resource(name = "MacService")
  private MacService macService;

  @Resource(name = "propertiesService")
  protected EgovPropertyService propertyService;

  @Resource(name = "EgovFileMngService")
  private EgovFileMngService fileMngService;
  
  @Resource(name = "EgovFileMngUtil")
  private EgovFileMngUtil fileUtil;
  
  private static final Marker ACC_MARKER = MarkerFactory.getMarker("ACC_MARKER");
  
  @ApiOperation(value = "시험장비 리스트", notes = "searchCode:10, searchWord:공통코드(TM)")
  @GetMapping(value = "/list.do")
  public BasicResponse emdList(@ModelAttribute ComParam param) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<MachineDTO> list = new ArrayList<MachineDTO>();

    list = macService.selectList(param);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(list).build();

    return res;
  }
  
  @ApiOperation(value = "시험장비 등록", notes = "")
  @PostMapping(value = "/old/insert.do")
  public BasicResponse oldInsert(
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

//    result = macService.insert(list);
    
    BasicResponse res = BasicResponse.builder().result(result).build();

    return res;
  }

  @ApiOperation(value = "시험장비 수정", notes = "machineSeq 필수\n삭제시, state='D' 추가")
  @PostMapping(value = "/old/update.do")
  public BasicResponse oldUpdate(
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
  
  @ApiOperation(value = "시험장비 순서,표시여부 수정", notes = "machineSeq, disOrdr(no)")
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
    
    log.info(ACC_MARKER, "User: {}, Method: {}, Request: {}", user.getId(), "/sys/mac/{type}/update.do", list.size());
    
    BasicResponse res = BasicResponse.builder().result(result).build();

    return res;
  }
  
  @ApiOperation(value = "시험장비 상세보기", notes = "searchCode:10, searchWord:공통코드(TM)")
  @GetMapping(value = "/{machineSeq}/detail.do")
  public BasicResponse detail(@ApiParam(value = "시험장비 고유번호", required = true,
      example = "208") @PathVariable(name = "machineSeq") int machineSeq) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    MachineDTO detail = new MachineDTO();

    detail = macService.selectDetail(machineSeq);

    if (detail == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();

    return res;
  }
  
  @ApiOperation(value = "시험장비 리스트", notes = "51 부서명, 47 차기교정일, 81 대상분류, 48 사용장비명, 82 한글태그명, 27 모델명, 83 장비번호, 84 관리번호, 85 HCT번호")
  @GetMapping(value = "/total/list.do")
  public BasicResponse totalList(@ModelAttribute ComParam param) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<MachineDTO> list = new ArrayList<MachineDTO>();

    /**
     * OR 조건 검색 처리
     */
    if (!ObjectUtils.isEmpty(param.getSearchVO())) {
      // 같은 CODE로 그룹핑
      Map<String, List<SearchVO>> reSearch = param.getSearchVO().stream().collect(Collectors.groupingBy(SearchVO::getSearchCode));
      
  
      SearchVO newSearch = new SearchVO();
      // 관리부
      if (reSearch.get("51") != null) {
        newSearch = new SearchVO();
        newSearch.setSearchCode("51");
        newSearch.setSearchWords(reSearch.get("51").stream().map(m -> m.getSearchWord()).collect(Collectors.toList()));
        param.getSearchVO().add(newSearch);
      }
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
    int cnt = macService.selectTotalListCnt(param);

    param.setTotalCount(cnt);
    pagingVO.setTotalCount(cnt);
    pagingVO.setTotalPage(
        (int) Math.ceil(pagingVO.getTotalCount() / (double) pagingVO.getDisplayRow()));    
    list = macService.selecTotaltList(param);

    log.info(ACC_MARKER, "User: {}, Method: {}, Request: {}", user.getId(), "/sys/mac/total/list.do", param);
    
    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(list).paging(pagingVO).build();

    return res;
  }
  
  @ApiOperation(value = "교정내역 리스트", notes = "")
  @GetMapping(value = "/cal/{machineSeq}/list.do")
  public BasicResponse calList(@ApiParam(value = "시험장비 고유번호", required = true,
      example = "208") @PathVariable(name = "machineSeq") int machineSeq) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<MacCal> list = new ArrayList<MacCal>();

    

    list = macService.selectMacCal(machineSeq);
    
    log.info(ACC_MARKER, "User: {}, Method: {}, Request: {}", user.getId(), "/sys/mac/cal/{machineSeq}/list.do", machineSeq);
    
    if (list == null) {
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(list).build();

    return res;
  }
  
  @ApiOperation(value = "시험장비 등록", notes = "")
  @PostMapping(value = "/insert.do")
  public BasicResponse insert(
      @RequestPart(value = "mac") MachineDTO req,
      @RequestPart(value = "macPic", required = false) MultipartFile macPic,
      @ModelAttribute MacCalDTO macCal) // 교정성적서 추가
      throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";


    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());


    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    Validator validator = validatorFactory.getValidator();

    Set<ConstraintViolation<MachineDTO>> violations = validator.validate(req);

    for (ConstraintViolation<MachineDTO> violation : violations) {
      msg = violation.getMessage();

      BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

      return res;
    }

    boolean result = false;

    try {
      
      // 파일삭제
      FileVO delFile = null;
      if (!ObjectUtils.isEmpty(req.getDelFileList())) {
        for (String del : req.getDelFileList()) {
          delFile = new FileVO();
          delFile.setAtchFileId(del);
          delFile.setFileSn("0");
          fileMngService.deleteFileInf(delFile);
          
          // 장비테이블에서도 삭제
          macService.macCalDelete(req.getMachineSeq(), delFile);
        }
      }
      
      String atchFileId = "";
      FileVO FileResult = null;
      
      // 파일 수정
      if (!ObjectUtils.isEmpty(macCal.getUptFileList())) {
        for (MacCal uptMacCal : macCal.getUptFileList()) {
          
          atchFileId = "";
          FileResult = null;
          
          if (!ObjectUtils.isEmpty(uptMacCal.getFile())) {
      
            FileResult = fileUtil.parseFile(uptMacCal.getFile(), "MAC", 0, "", "");
            atchFileId = fileMngService.insertFileInf(FileResult);
            uptMacCal.setCalFile(atchFileId);
    
          }
        }
      }
      
      // 교정장비 신규등록
      if (!ObjectUtils.isEmpty(macCal.getMacCal()) && !ObjectUtils.isEmpty(macCal.getMacCal().getFile())) {
        FileResult = fileUtil.parseFile(macCal.getMacCal().getFile(), "MAC", 0, "", "");
        atchFileId = fileMngService.insertFileInf(FileResult);
        macCal.getMacCal().setCalFile(atchFileId);
      }
      
      // 시험장비 사진
      if (!ObjectUtils.isEmpty(macPic)) {
        FileResult = fileUtil.parseFile(macPic, "MAC/PIC", 0, "", "");
        atchFileId = fileMngService.insertFileInf(FileResult);
        req.setPhoto(atchFileId);
      }
      
      // 장비추가
      result = macService.insert(req, macCal);
    
      log.info(ACC_MARKER, "User: {}, Method: {}, Request: {}", user.getId(), "/sys/mac/insert.do", req);
      
    } catch (Exception e) {

      msg = ResponseMessage.RETRY;
      
      System.out.println(e);
      log.warn("{} :: 오류 발생", user.getId(), e);
    }
    

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(req.getMachineSeq()).build();

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
}
