package egovframework.wrp.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import egovframework.cmm.service.BasicResponse;
import egovframework.cmm.service.BoardVO;
import egovframework.cmm.service.ComParam;
import egovframework.cmm.service.EgovFileMngService;
import egovframework.cmm.service.FileVO;
import egovframework.cmm.service.LoginVO;
import egovframework.cmm.service.PagingVO;
import egovframework.cmm.service.ResponseMessage;
import egovframework.cmm.service.SearchVO;
import egovframework.cmm.util.EgovFileMngUtil;
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.cmm.util.MinIoFileMngUtil;
import egovframework.rte.fdl.property.EgovPropertyService;
import egovframework.wrp.dto.WeekRepDTO;
import egovframework.wrp.dto.WeekResultDTO;
import egovframework.wrp.service.WeekRep;
import egovframework.wrp.service.WrpService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Api(tags = {"주간보고"})
@RestController
@RequestMapping("/wrp")
public class WrpController {

  @Resource(name = "WrpService")
  private WrpService wrpService;
  
  @Resource(name = "MinIoFileMngUtil")
  private MinIoFileMngUtil fileUtil;
  
  @Resource(name = "EgovFileMngService")
  private EgovFileMngService fileMngService;
  
  @Resource(name = "propertiesService")
  protected EgovPropertyService propertyService;
  
  @ApiOperation(value = "주간 시험 결과 (작성시 화면)")
  @GetMapping(value = "/insert/detail.do")
  public BasicResponse insertDetail(
      @ApiParam(value = "시험부서코드(공통코드:WT)", required = true) 
      @RequestParam(value = "testTypeCode") String testTypeCode) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    
    WeekRepDTO data = new WeekRepDTO();
    List<WeekResultDTO> detail = new ArrayList<WeekResultDTO>();
    
    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      
      // 최종보고 전까지 실시간 집계
      detail = wrpService.getDetail(testTypeCode);
      if (detail == null) {
        result = false;
        msg = ResponseMessage.NO_DATA;
      }
      
      // 전회차 보고서 피드백
      data.setTotalResult(detail);
      data.setFeedBack(wrpService.getFeedback(testTypeCode));
      
    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(data).build();

    return res;
  }
  
  @ApiOperation(value = "주간 시험 결과 (보고서 Seq 있을때 화면)")
  @GetMapping(value = "/update/detail.do")
  public BasicResponse updateDetail(
      @ApiParam(value = "보고서 Seq", required = true) 
      @RequestParam(value = "wrSeq") int wrSeq) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    
    WeekRepDTO data = new WeekRepDTO();
    // 주간보고시 통계데이터
    List<WeekResultDTO> detail = new ArrayList<WeekResultDTO>();
    
    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      WeekResultDTO.Req check = wrpService.checkReport(wrSeq);
      
      // seq가 있으나 데이터가 없으면 오류
      if (check == null) {
        result = false;
        msg = ResponseMessage.NO_DATA;
      } 
      else {
        // 최종보고가 된 보고서인지 확인
        if (check != null && !check.isFixYn()) {
          // 최종보고 전까지 실시간 집계
          detail = wrpService.getDetail(check.getTestTypeCode());
          data.setFeedBack(wrpService.getFeedback(check.getTestTypeCode())); 
        } else {
          // 최종보고 후 저장데이터 호출
          detail = wrpService.getFixDetail(wrSeq, check.getTestTypeCode());
          data.setFeedBack(check.getMemo());
        }

        // 주간보고 작성내역
        WeekRep report = wrpService.getReport(wrSeq);
        // 파일리스트
        FileVO fileVO = new FileVO();
        fileVO.setAtchFileId(report.getAtchFileId());
        List<FileVO> docResult = fileMngService.selectFileInfs(fileVO);
        docResult.stream().map(doc -> {
          doc.setFileStreCours("/file/fileDown.do?atchFileId=".concat(doc.getAtchFileId())
              .concat("&fileSn=").concat(doc.getFileSn()));
          return doc;
        }).collect(Collectors.toList());
        report.setFileList(docResult);
        
        
        // 작성내역등 추가데이터 호출
        data.setTotalResult(detail);
        data.setReport(report);
        
        if (detail == null) {
          result = false;
          msg = ResponseMessage.NO_DATA;
        }
      }
      
    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(data).build();

    return res;
  }
  
  @ApiOperation(value = "주간보고 저장", notes = "")
  @PostMapping("/insert.do")
  public BasicResponse insertBoardArticle(
      @RequestPart(value = "files", required = false) final List<MultipartFile> files,
      @RequestPart(value = "delFileList", required = false) List<FileVO> delFileList,
      @RequestPart(value = "weekRep") WeekRep req) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();


    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());

    List<FileVO> FileResult = null;
    String atchFileId = "";

    // 파일이 있을때만 처리
    if (!ObjectUtils.isEmpty(files)) {
      
      // 폴더명 얻기
      String typeName = "";
//      if (req.getWrSeq() == 0) {
        typeName = testTypeConvert(req.getTestTypeCode());
//      } else {
//        WeekResultDTO.Req check = wrpService.checkReport(req.getWrSeq());
//        typeName = testTypeConvert(check.getTestTypeCode());
//      }

      // 신규
      if (StringUtils.isEmpty((req.getAtchFileId()))) {
        FileResult = fileUtil.parseFile(files, "", 0, atchFileId, "week/".concat(typeName));
        atchFileId = fileMngService.insertFileInfs(FileResult);
        req.setAtchFileId(atchFileId);
      } 
      // 수정
      else {
        // 현재 등록된 파일 수 가져오기
        FileVO fvo = new FileVO();
        fvo.setAtchFileId(req.getAtchFileId());
        int cnt = fileMngService.getMaxFileSN(fvo);
        
        // 추가파일 등록
        List<FileVO> _result = fileUtil.parseFile(files, "", cnt, req.getAtchFileId(), "week/".concat(typeName));
        fileMngService.updateFileInfs(_result);
      }

    }
    req.setNttCn(unscript(req.getNttCn())); // XSS 방지

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
    
    // 게시글 저장 or 업데이트
    try {
      
      if (req.getWrSeq() == 0) {

        if (StringUtils.isEmpty(req.getTestTypeCode())) {
          result = false;
          msg = "부서명 선택은 필수사항입니다.";
          BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

          return res;
        } 
        
        result = wrpService.insert(req);
      }
      else
        result = wrpService.update(req);
      
    } catch (Exception e) {

      msg = ResponseMessage.RETRY;

    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(req.getWrSeq()).build();
    return res;
  }
  
  @ApiOperation(value = "게시판 목록", notes = "검색코드\n23 시험부, 2 보고자, 20  최종보고일")
  @GetMapping("/list.do")
  public BasicResponse<BoardVO> selectList(@ModelAttribute ComParam param)
      throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";

    List<WeekRep> list = new ArrayList<WeekRep>();
    
    /**
     * OR 조건 검색 처리
     */
    // 같은 CODE로 그룹핑
    if (!ObjectUtils.isEmpty(param.getSearchVO())) {
      Map<String, List<SearchVO>> reSearch = param.getSearchVO().stream().collect(Collectors.groupingBy(SearchVO::getSearchCode));
      
      SearchVO newSearch = new SearchVO();
      // 시험부
      if (reSearch.get("23") != null) {
        newSearch = new SearchVO();
        newSearch.setSearchCode("23");
        newSearch.setSearchWords(reSearch.get("23").stream().map(m -> m.getSearchWord()).collect(Collectors.toList()));
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

    int cnt = wrpService.selectListCnt(param);
    pagingVO.setTotalCount(cnt);
    pagingVO.setTotalPage(
        (int) Math.ceil(pagingVO.getTotalCount() / (double) pagingVO.getDisplayRow()));

    list = wrpService.selectList(param);
    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res =
        BasicResponse.builder().result(result).message(msg).data(list).paging(pagingVO).build();
    return res;
  }
  
  @ApiOperation(value = "최종보고 완료하기", notes = "wrSeq:시험고유항목, memo:피드백내용")
  @PostMapping(value = "/fix/insert.do")
  public BasicResponse fixInsert(@RequestBody WeekResultDTO.Req req) throws Exception {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    // 로그인정보
    req.setUdtMemId(user.getId());
    boolean result = false;
    String msg = "";

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      
      result = wrpService.updateFix(req);
    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }
  
  @ApiOperation(value = "보고서 삭제", notes = "wrSeq:시험고유항목")
  @PostMapping(value = "/delete.do")
  public BasicResponse delete(@RequestBody WeekResultDTO.Req req) throws Exception {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    // 로그인정보
    req.setUdtMemId(user.getId());
    boolean result = false;
    String msg = "";

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      
      // 최종보고가 완료된건은 삭제 불가
      WeekRep report = wrpService.getReport(req.getWrSeq());
      if (report.getWrCnt() != 0) {
        result = false;
        msg = "최종보고가 완료되어 삭제할 수 없습니다.";
        BasicResponse res = BasicResponse.builder().result(result).message(msg).build();
        return res;
      }
      
      result = wrpService.delete(req);
    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }
  
  /**
   * XSS 방지 처리.
   *
   * @param data
   * @return
   */
  protected String unscript(String data) {
    if (data == null || data.trim().equals("")) {
      return "";
    }

    String ret = data;

    ret = ret.replaceAll("<(S|s)(C|c)(R|r)(I|i)(P|p)(T|t)", "&lt;script");
    ret = ret.replaceAll("</(S|s)(C|c)(R|r)(I|i)(P|p)(T|t)", "&lt;/script");

    ret = ret.replaceAll("<(O|o)(B|b)(J|j)(E|e)(C|c)(T|t)", "&lt;object");
    ret = ret.replaceAll("</(O|o)(B|b)(J|j)(E|e)(C|c)(T|t)", "&lt;/object");

    ret = ret.replaceAll("<(A|a)(P|p)(P|p)(L|l)(E|e)(T|t)", "&lt;applet");
    ret = ret.replaceAll("</(A|a)(P|p)(P|p)(L|l)(E|e)(T|t)", "&lt;/applet");

    ret = ret.replaceAll("<(E|e)(M|m)(B|b)(E|e)(D|d)", "&lt;embed");
    ret = ret.replaceAll("</(E|e)(M|m)(B|b)(E|e)(D|d)", "&lt;embed");

    ret = ret.replaceAll("<(F|f)(O|o)(R|r)(M|m)", "&lt;form");
    ret = ret.replaceAll("</(F|f)(O|o)(R|r)(M|m)", "&lt;form");

    return ret;
  }
  

  private static final Map<String, String> TEST_TYPE_MAP = new HashMap<>();

  static {
      TEST_TYPE_MAP.put("EM", "EMC");
      TEST_TYPE_MAP.put("RS", "RF&SAR");
      TEST_TYPE_MAP.put("SF", "SAFETY&효율신뢰");
      TEST_TYPE_MAP.put("MD", "MEDICAL");
  }

  public static String testTypeConvert(String testTypeCode) {
      return TEST_TYPE_MAP.getOrDefault(testTypeCode, "UNKNOWN");
  }
      
}
