package egovframework.cmm.web;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import egovframework.cmm.service.BasicResponse;
import egovframework.cmm.service.BbsService;
import egovframework.cmm.service.BoardVO;
import egovframework.cmm.service.ComParam;
import egovframework.cmm.service.EgovFileMngService;
import egovframework.cmm.service.FileVO;
import egovframework.cmm.service.LoginVO;
import egovframework.cmm.service.NextcloudDavService;
import egovframework.cmm.service.PagingVO;
import egovframework.cmm.service.ResponseMessage;
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.cmm.util.MinIoFileMngUtil;
import egovframework.rte.fdl.property.EgovPropertyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

@Api(tags = {"게시판"})
@RestController
@RequestMapping("/bbs")
@Slf4j
public class BbsController {

  @Resource(name = "propertiesService")
  protected EgovPropertyService propertyService;

  @Resource(name = "MinIoFileMngUtil")
  private MinIoFileMngUtil fileUtil;

  @Resource(name = "EgovFileMngService")
  private EgovFileMngService fileMngService;

  @Resource(name = "BbsService")
  private BbsService bbsMngService;

  @Resource(name = "NextcloudDavService")
  private NextcloudDavService nextcloudDavService;
  
  /**
   * 게시물을 등록한다.
   *
   * @param boardVO
   * @param board
   * @param sessionVO
   * @param model
   * @return
   * @throws Exception
   */
  @ApiOperation(value = "게시글 저장", notes = "1. 수정시 게시물아이디(nttId), atchFileId(있을시) 필수\n2. delFileList=파일삭제")
  @PostMapping("/insert.do")
  public BasicResponse insertBoardArticle(
      @RequestPart(value = "files", required = false) final List<MultipartFile> files,
      @RequestPart(value = "delFileList", required = false) List<FileVO> delFileList,
      @RequestPart(value = "board") BoardVO req) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    // P01만 권한부여 (임시)
    if (!isAuthenticated || !"P01".equals(user.getAuthCode())) {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;

      BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

      return res;
    }

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());
    req.setBbsId("BBSMSTR_A");

    List<FileVO> FileResult = null;
    String atchFileId = "";

    // 파일이 있을때만 처리
    if (!ObjectUtils.isEmpty(files)) {

      // 신규
      if ("".equals(req.getAtchFileId())) {
        FileResult = fileUtil.parseFile(files, "board", 0, atchFileId, "");
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
        List<FileVO> _result = fileUtil.parseFile(files, "board", cnt, req.getAtchFileId(), "");
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
      
      if (req.getNttId() == 0)
        result = bbsMngService.insertBoardArticle(req);
      else
        result = bbsMngService.updateBoardArticle(req);
      
    } catch (Exception e) {

      msg = ResponseMessage.RETRY;
      log.warn(user.getId() + " :: " + e.toString());
      log.warn(req.toString());
      log.warn("");

    }

    // 문의글 작성할 경우 SMS 문자
    // if ("BBSMSTR_B".equals(board.getBbsId()) && !"Y".equals(board.getReplyAt()))
    // sendSms(board.getProductName());

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();
    return res;
  }

  
  @ApiOperation(value = "게시판 목록", notes = "검색코드\n2  작성자, 33 제목, 15  작성일")
  @GetMapping("/list.do")
  public BasicResponse<BoardVO> selectBoardArticles(@ModelAttribute ComParam param)
      throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";

    List<BoardVO> list = new ArrayList<BoardVO>();

    // 페이징
    param.setPageUnit(param.getPageUnit());
    param.setPageSize(propertyService.getInt("pageSize"));

    PagingVO pagingVO = new PagingVO();

    pagingVO.setCurrentPageNo(param.getPageIndex());
    pagingVO.setDisplayRow(param.getPageUnit());
    pagingVO.setDisplayPage(param.getPageSize());

    param.setFirstIndex(pagingVO.getFirstRecordIndex());

    // 내부처리 ( 게시판 종류 )
    param.setSearchCode("bbsId");
    param.setSearchCode("BBSMSTR_A");
    
    int cnt = bbsMngService.selectBoardArticleListCnt(param);
    pagingVO.setTotalCount(cnt);
    pagingVO.setTotalPage(
        (int) Math.ceil(pagingVO.getTotalCount() / (double) pagingVO.getDisplayRow()));

    list = bbsMngService.selectBoardArticleList(param);
    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res =
        BasicResponse.builder().result(result).message(msg).data(list).paging(pagingVO).build();
    return res;
  }

  @ApiOperation(value = "공지사항 상세보기", notes = "bbsId=BBSMSTR_A(고정값)")
  @GetMapping("/{bbsId}/{nttId}/detail.do")
  public BasicResponse selectBoardArticle(
      @ApiParam(value = "게시판 고유번호", required = true, example = "BBSMSTR_A") @PathVariable(name = "bbsId") String bbsId,
      @ApiParam(value = "게시글 고유번호", required = true, example = "4") @PathVariable(name = "nttId") int nttId
      ) throws Exception {

    boolean result = true;
    String msg = "";
    LoginVO user = new LoginVO();
    if (EgovUserDetailsHelper.isAuthenticated()) {
      user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    }

    BoardVO req = new BoardVO();
    req.setBbsId(bbsId);
    req.setNttId(nttId);
    
    BoardVO detail = bbsMngService.selectBoardArticle(req);

    if (detail == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    // 파일리스트
    FileVO fileVO = new FileVO();
    fileVO.setAtchFileId(detail.getAtchFileId());
    List<FileVO> docResult = fileMngService.selectFileInfs(fileVO);
    
    docResult.stream().map(doc -> {
      doc.setFileStreCours("/file/fileDown.do?atchFileId=".concat(doc.getAtchFileId())
          .concat("&fileSn=").concat(doc.getFileSn()));
      return doc;
    }).collect(Collectors.toList());
    
    detail.setFileList(docResult);

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();

    return res;
  }

  /**
   * 게시물에 대한 비밀번호를 확인한다.
   *
   * @param boardVO
   * @param sessionVO
   * @param model
   * @return
   * @throws Exception
   */
  // @PostMapping("/cop/bbs/selectBoardPassword.do")
  // public BasicResponse getPasswordInf(@ModelAttribute BoardVO boardVO) throws Exception {
  // BasicResponse res = new BasicResponse();
  //
  // LoginVO user = new LoginVO();
  // if (EgovUserDetailsHelper.isAuthenticated()) {
  // user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
  // }
  //
  // boardVO.setLastUpdusrId(user.getUniqId());
  // BoardVO vo = bbsMngService.selectBoardArticle(boardVO);
  //
  // if (vo.getPassword().equals(boardVO.getPassword())) {
  // res.setResult(true);
  // } else {
  // res.setResult(false);
  // res.setMsg("비밀번호가 맞지 않습니다.");
  // }
  //
  // return res;
  // }

  /**
   * 상세조회한 이전글/다음글
   *
   * @param boardVO
   * @param sessionVO
   * @param model
   * @return
   * @throws Exception
   */
  // @RequestMapping("/cop/bbs/selectBoardMovePN.do")
  // public BasicResponse selectBoardMovePN(@ModelAttribute BoardVO boardVO) throws Exception {
  // BasicResponse res = new BasicResponse();
  //
  // List<Board> list = bbsMngService.selectBoardMovePN(boardVO);
  //
  // if (list == null) {
  // res.setResult(false);
  // res.setMsg(egovMessageSource.getMessage("info.nodata.msg"));
  // }
  // for (Board item : list) {
  // res.setResult(true);
  //
  // if (item.getNttId() > boardVO.getNttId()) {
  // item.setNtceEnddeView("Y");
  // } else {
  // item.setNtceBgndeView("Y");
  // }
  //
  // res.setList(list);
  // }
  // return res;
  // }

  @ApiOperation(value = "게시판 삭제" , notes = "1. 게시물아이디(nttId), BBSMSTR_A(bbsId) 필수")
  @PostMapping("/{bbsId}/{nttId}/delete.do")
  public BasicResponse deleteBoardArticle(
      @ApiParam(value = "게시판 고유번호", required = true, example = "BBSMSTR_A") @PathVariable(name = "bbsId") String bbsId,
      @ApiParam(value = "게시글 고유번호", required = true, example = "4") @PathVariable(name = "nttId") int nttId
      ) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    BoardVO board = new BoardVO();

    // 로그인정보
    board.setUdtMemId(user.getId());

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    if (isAuthenticated) {
      board.setBbsId(bbsId);
      board.setNttId(nttId);
      bbsMngService.deleteBoardArticle(board);
    } else {
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }

  /**
   * 게시물에 대한 답변을 등록한다.
   *
   * @param boardVO
   * @param board
   * @param sessionVO
   * @param model
   * @return
   * @throws Exception
   */
  // @RequestMapping("/cop/bbs/replyBoardArticle.do")
  // public BasicResponse replyBoardArticle(
  // @RequestPart(value = "files", required = false) final List<MultipartFile> multiRequest,
  // @RequestPart(value = "board") Board board, BindingResult bindingResult) throws Exception {
  //
  // BasicResponse res = new BasicResponse();
  //
  // // 사용자권한 처리
  // if (!EgovUserDetailsHelper.isAuthenticated()) {
  // res.setResult(false);
  // res.setMsg(egovMessageSource.getMessage("fail.common.login"));
  // return res;
  // }
  //
  // LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
  // Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
  //
  // beanValidator.validate(board, bindingResult);
  //
  // if (isAuthenticated) {
  //
  // List<FileVO> FileResult = null;
  // String atchFileId = "";
  //
  //
  // final List<MultipartFile> files = multiRequest;
  // if (!files.isEmpty()) {
  // FileResult = fileUtil.parseFile(files, "BBS_", 0, "", "");
  // atchFileId = fileMngService.insertFileInfs(FileResult);
  // }
  //
  // board.setAtchFileId(atchFileId);
  // board.setReplyAt("Y");
  // board.setFrstRegisterId(user.getUniqId());
  // board.setBbsId(board.getBbsId());
  // board.setParnts(Long.toString(board.getNttId()));
  // board.setSortOrdr(board.getSortOrdr());
  // board.setReplyLc(Integer.toString(Integer.parseInt(board.getReplyLc()) + 1));
  //
  // board.setNttCn(unscript(board.getNttCn())); // XSS 방지
  //
  // bbsMngService.insertBoardArticle(board);
  //
  // res.setResult(true);
  // }
  //
  // return res;
  // }


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

}
