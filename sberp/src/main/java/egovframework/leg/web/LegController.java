package egovframework.leg.web;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import egovframework.cmm.service.BasicResponse;
import egovframework.cmm.service.ComParam;
import egovframework.cmm.service.LoginVO;
import egovframework.cmm.service.PagingVO;
import egovframework.cmm.service.ResponseMessage;
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.leg.dto.LedgerDTO;
import egovframework.leg.service.LegService;
import egovframework.rte.fdl.property.EgovPropertyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Api(tags = {"총괄대장"})
@RestController
@RequestMapping("/leg")
public class LegController {

  @Resource(name = "LegService")
  private LegService legService;

  @Resource(name = "propertiesService")
  protected EgovPropertyService propertyService;

  @ApiOperation(value = "시험접수 관리대장",
      notes = "검색박스는 공통코드 CS, 필요한항목만 노출시켜서 사용\n" + " 54    시료반출일,21   시료반입일")
  @GetMapping(value = "/list.do")
  public BasicResponse ppList(@ModelAttribute ComParam param) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";

    List<LedgerDTO> list = new ArrayList<LedgerDTO>();

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    if (!isAuthenticated) {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
      
      BasicResponse res =
          BasicResponse.builder().result(result).message(msg).build();

      return res;
    }

    // 페이징
    param.setPageUnit(param.getPageUnit());
    param.setPageSize(propertyService.getInt("pageSize"));

    PagingVO pagingVO = new PagingVO();

    pagingVO.setCurrentPageNo(param.getPageIndex());
    pagingVO.setDisplayRow(param.getPageUnit());
    pagingVO.setDisplayPage(param.getPageSize());

    param.setFirstIndex(pagingVO.getFirstRecordIndex());
    int cnt = legService.selectListCnt(param);

    pagingVO.setTotalCount(cnt);
    pagingVO.setTotalPage(
        (int) Math.ceil(pagingVO.getTotalCount() / (double) pagingVO.getDisplayRow()));
    list = legService.selectList(param);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res =
        BasicResponse.builder().result(result).message(msg).data(list).paging(pagingVO).build();
    return res;
  }

  @ApiOperation(value = "시험접수 관리대장",
      notes = "검색박스는 공통코드 CS, 필요한항목만 노출시켜서 사용\n" + " 54    시료반출일,21   시료반입일")
  @GetMapping(value = "/excel.do")
  public ModelAndView excel(@ModelAttribute ComParam param, HttpServletRequest request, HttpServletResponse response) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";

    ModelAndView mav = new ModelAndView("excelView");
    List<LedgerDTO> list = new ArrayList<LedgerDTO>();
    String[] columnArr = {"No.", "신청서 접수번호", "신청서 접수일", "시료 반입일","시료 접수자","시험 접수일","시험 접수번호","업체명","제품명","모델명","인증종류1","적용규격","시료반입","시료반출","시료 반출일","시료 반출자","시료 반출형태","수거인","송장번호", "구분", "성적서발급일"};
    String[] columnVarArr = {"no", "sbkId", "rcptDt", "carryInDate","carryInName","testDtStr","testId","cmpyName","prdctName","modelName","crtfc2Name","testStndr","inCnt","outCnt","carryOutDate","carryOutName","carryOutType","carryOutDlvryName","carryOutDlvryInvc", "testRprt", "reportDt"};
    int[] columnWidth = {1500, 4000, 3500, 3500, 3000, 3500, 5500, 6000, 12000, 8000, 5000, 15000, 1500, 1500, 3500, 3000, 3500, 3000, 3000, 3000, 3500};
    
    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    if (isAuthenticated) {

      // 페이징
      param.setPageUnit(10000);
      param.setPageSize(propertyService.getInt("pageSize"));
  
      PagingVO pagingVO = new PagingVO();
  
      pagingVO.setCurrentPageNo(1);
      pagingVO.setDisplayRow(param.getPageUnit());
      pagingVO.setDisplayPage(param.getPageSize());
  
      param.setFirstIndex(pagingVO.getFirstRecordIndex());
      int cnt = legService.selectListCnt(param);
  
      pagingVO.setTotalCount(cnt);
      pagingVO.setTotalPage(
          (int) Math.ceil(pagingVO.getTotalCount() / (double) pagingVO.getDisplayRow()));
      list = legService.selectList(param);
  
      String filename = "시험접수관리대장_"+DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss").format(LocalDateTime.now());;
      Map<String, Object> dataMap = new HashMap<String, Object>();
      
      dataMap.put("columnArr", columnArr);
      dataMap.put("columnVarArr", columnVarArr);
      dataMap.put("columnWidth", columnWidth);
      dataMap.put("sheetNm", "시험접수 관리대장");    
      dataMap.put("list", list);
  
      mav.addObject("dataMap", dataMap);
      mav.addObject("filename", filename);
  
      
      if (list == null) {
        result = false;
        msg = ResponseMessage.NO_DATA;
      }
      
    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;

      Map<String, Object> dataMap = new HashMap<String, Object>();
      
      dataMap.put("columnArr", columnArr);
      dataMap.put("columnVarArr", columnVarArr);
      dataMap.put("columnWidth", columnWidth);
      dataMap.put("sheetNm", "시험접수 관리대장");    
      dataMap.put("list", list);
      
      mav.addObject("dataMap", dataMap);
      mav.addObject("filename", ResponseMessage.UNAUTHORIZED);
      
    }
    
    return mav;
  }

}
