package egovframework.tst.web;

import java.util.ArrayList;
import java.util.List;
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
import egovframework.rte.fdl.property.EgovPropertyService;
import egovframework.tst.dto.DebugDTO;
import egovframework.tst.dto.TestDTO;
import egovframework.tst.service.DbgService;
import egovframework.tst.service.DebugMemo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags = {"디버깅"})
@RestController
@RequestMapping("/dbg")
public class DbgController {

  @Resource(name = "DbgService")
  private DbgService dbgService;

  @Resource(name = "propertiesService")
  protected EgovPropertyService propertyService;


  @ApiOperation(value = "디버깅리스트",
      notes = "결과값은 TestDTO.Res 참고" + "2.검색박스는 공통코드 CS, 필요한항목만 노출시켜서 사용\n" + "15  작성일\n"
          + "20  완료일\n" + "31  시험상태\n" + "49  최종결과\n" + "50  시험번호\n" + "6   제품명\n" + "27  모델명\n")
  @GetMapping(value = "/list.do")
  public BasicResponse debugList(@ModelAttribute ComParam param) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<DebugDTO> list = new ArrayList<DebugDTO>();

    // 페이징
    param.setPageUnit(propertyService.getInt("pageUnit"));
    param.setPageSize(propertyService.getInt("pageSize"));

    PagingVO pagingVO = new PagingVO();

    pagingVO.setCurrentPageNo(param.getPageIndex());
    pagingVO.setDisplayRow(param.getPageUnit());
    pagingVO.setDisplayPage(param.getPageSize());

    param.setFirstIndex(pagingVO.getFirstRecordIndex());
    int cnt = dbgService.selectListCnt(param);

    pagingVO.setTotalCount(cnt);
    pagingVO.setTotalPage(
        (int) Math.ceil(pagingVO.getTotalCount() / (double) pagingVO.getDisplayRow()));
    list = dbgService.selectList(param);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res =
        BasicResponse.builder().result(result).message(msg).data(list).paging(pagingVO).build();

    return res;
  }


  @ApiOperation(value = "디버깅 완료하기", notes = "debugSeq:디버깅고유항목, stateCode:디버깅상태 공통코드(TR), etc:기타")
  @PostMapping(value = "/update.do")
  public BasicResponse update(@RequestBody DebugDTO req) throws Exception {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());

    boolean result = false;
    String msg = "";

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      result = dbgService.update(req);
    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }
  
  @ApiOperation(value = "디버깅상태 변경", notes = "debugSeq:디버깅고유항목, stateCode:디버깅상태 공통코드(TD), memo:메모")
  @PostMapping(value = "/stateInsert.do")
  public BasicResponse debugStateInsert(@RequestBody DebugDTO req) throws Exception {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());

    boolean result = false;
    String msg = "";

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      result = dbgService.debugStateInsert(req);
    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }

  @ApiOperation(value = "디버깅상태 변경 내역")
  @GetMapping(value = "/stateList.do")
  public BasicResponse debugStateList(@ApiParam(value = "디버깅 고유번호", required = true,
      example = "1") @RequestParam(value = "debugSeq") String debugSeq) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<DebugDTO> list = new ArrayList<DebugDTO>();

    list = dbgService.debugStateList(debugSeq);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(list).build();

    return res;
  }

  @ApiOperation(value = "디버깅게시판 등록", notes = "debugSeq:디버깅고유항목, memo:메모")
  @PostMapping(value = "/boardInsert.do")
  public BasicResponse debugBoardInsert(@RequestBody DebugMemo req) throws Exception {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());

    boolean result = false;
    String msg = "";

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      result = dbgService.debugBoardInsert(req);
    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }

  @ApiOperation(value = "디버깅게시판 내역")
  @GetMapping(value = "/boardList.do")
  public BasicResponse debugBoardList(@ApiParam(value = "디버깅 고유번호", required = true,
      example = "1") @RequestParam(value = "debugSeq") String debugSeq) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<DebugMemo> list = new ArrayList<DebugMemo>();

    list = dbgService.debugBoardList(debugSeq);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(list).build();

    return res;
  }


}
