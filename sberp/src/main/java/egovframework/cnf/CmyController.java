package egovframework.cnf;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.apache.poi.util.StringUtil;
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
import egovframework.cmm.service.LoginVO;
import egovframework.cmm.service.PagingVO;
import egovframework.cmm.service.ResponseMessage;
import egovframework.cmm.service.SearchVO;
import egovframework.cmm.util.EgovFileMngUtil;
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.cnf.service.CmpyDTO;
import egovframework.cnf.service.CmyService;
import egovframework.rte.fdl.property.EgovPropertyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags = {"협력사, 직접고객"})
@RestController
@RequestMapping("/cnf/cmp")
public class CmyController {

  @Resource(name = "CmyService")
  private CmyService cmyService;

  @Resource(name = "EgovFileMngUtil")
  private EgovFileMngUtil fileUtil;

  @Resource(name = "EgovFileMngService")
  private EgovFileMngService fileMngService;
  
  @Resource(name = "propertiesService")
  protected EgovPropertyService propertyService;

  @ApiOperation(value = "협력사/직접고객 리스트", notes = "검색코드\n2  작성자\n12   회사명\n13 회사연락처\n41   회사종류\n15   작성일 ")
  @GetMapping(value = "/{type}/list.do")
  public BasicResponse list(
      @ApiParam(value = "partner(협력사)/direct(직접고객)", required = true,
          example = "partner") @PathVariable(name = "type") String type,
      @ModelAttribute ComParam param) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<CmpyDTO> list = new ArrayList<CmpyDTO>();

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    if (!isAuthenticated) {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
      
      BasicResponse res =
          BasicResponse.builder().result(result).message(msg).build();

      return res;
    }
    
    // 내부검색 데이터
    SearchVO vo = new SearchVO();
    vo.setSearchCode("99");
    // 협력사
    if ("partner".equals(type))
      vo.setSearchWord("0000");
    // 직접고객
    else if ("direct".equals(type))
      vo.setSearchWord("1000");
    if (param.getSearchVO() != null) {
      param.getSearchVO().add(vo);
    } else {
      List<SearchVO> searchVO = new ArrayList<SearchVO>();
      searchVO.add(vo);
      param.setSearchVO(searchVO);
    }

    // 페이징
    param.setPageUnit(param.getPageUnit());
    param.setPageSize(propertyService.getInt("pageSize"));

    PagingVO pagingVO = new PagingVO();

    pagingVO.setCurrentPageNo(param.getPageIndex());
    pagingVO.setDisplayRow(param.getPageUnit());
    pagingVO.setDisplayPage(param.getPageSize());

    param.setFirstIndex(pagingVO.getFirstRecordIndex());
    int cnt = cmyService.selectListCnt(param);

    pagingVO.setTotalCount(cnt);
    pagingVO.setTotalPage(
        (int) Math.ceil(pagingVO.getTotalCount() / (double) pagingVO.getDisplayRow()));
    list = cmyService.selectList(param);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(list).paging(pagingVO).build();

    return res;
  }

  @ApiOperation(value = "협력사/직접고객 등록")
  @PostMapping(value = "/{type}/insert.do")
  public BasicResponse insert(
      @ApiParam(value = "partner(협력사)/direct(직접고객)", required = true, example = "partner") @PathVariable(name = "type") String type,
      @ApiParam(value = "typeCode=협력사 공통코드(PK), 직고객 공통코드(ST)", required = true, example = "") @RequestPart CmpyDTO req,
      @RequestPart(value = "delFileList", required = false) List<FileVO> delFileList,
      @RequestPart(value = "files", required = false) final List<MultipartFile> files)
      throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());
    // 협력사
    if ("partner".equals(type))
      req.setCmpyCode("0000");
    // 직접고객
    else if ("direct".equals(type))
      req.setCmpyCode("1000");

    System.out.println("=-===========");
    System.out.println(req.toString());
    System.out.println("=-===========");

    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    Validator validator = validatorFactory.getValidator();

    Set<ConstraintViolation<CmpyDTO>> violations = validator.validate(req);

    for (ConstraintViolation<CmpyDTO> violation : violations) {
      msg = violation.getMessage();

      BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

      return res;
    }

    boolean result = false;

    // 파일처리
    List<FileVO> FileResult = null;
    String atchFileId = "";
    if (!ObjectUtils.isEmpty(files)) {
      
      // 신규
      if (StringUtils.isEmpty(req.getAtchFileId())) {
        FileResult = fileUtil.parseFile(files, "CMY", 0, "", "");
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
        List<FileVO> _result = fileUtil.parseFile(files, "CMY", cnt, req.getAtchFileId(), "");
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
    //-- END 파일처리
    
    result = cmyService.insert(req);

    BasicResponse res = BasicResponse.builder().result(result).build();

    return res;
  }

  @ApiOperation(value = "협력사/직접고객 삭제")
  @PostMapping(value = "/{cmpySeq}/delete.do")
  public BasicResponse delete(@ApiParam(value = "회사 고유번호", required = true, example = "0004") @PathVariable(name = "cmpySeq") int cmpySeq) throws Exception {
    
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    CmpyDTO req = new CmpyDTO();

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    if (!isAuthenticated) {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
      
      BasicResponse res =
          BasicResponse.builder().result(result).message(msg).build();

      return res;
    }
    
    // 로그인정보
    req.setUdtMemId(user.getId());
    req.setCmpySeq(cmpySeq);
    result = cmyService.delete(req);
    
    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }
  
  @ApiOperation(value = "협력사/직접고객 상세보기")
  @GetMapping(value = "/{cmpySeq}/detail.do")
  public BasicResponse detail(@ApiParam(value = "회사 고유번호", required = true,
      example = "0004") @PathVariable(name = "cmpySeq") int cmpySeq) throws Exception {
    boolean result = true;
    String msg = "";
    CmpyDTO detail = new CmpyDTO();

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    if (!isAuthenticated) {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
      
      BasicResponse res =
          BasicResponse.builder().result(result).message(msg).build();

      return res;
    }
    
    detail = cmyService.detail(cmpySeq);

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

  @ApiOperation(value = "협력사/직접고객 회사명 중복확인", notes = "")
  @GetMapping(value = "/{type}/checkName.do")
  public BasicResponse checkName(
      @ApiParam(value = "partner(협력사)/direct(직접고객)", required = true,
          example = "partner") @PathVariable(name = "type") String type,
      @RequestParam("cmpyName") String cmpyName) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<CmpyDTO> list = new ArrayList<CmpyDTO>();
    String cmpyCode = "";

    // 협력사
    if ("partner".equals(type))
      cmpyCode = "0000";
    // 직접고객
    else if ("direct".equals(type))
      cmpyCode = "1000";

    list = cmyService.selectSameName(cmpyCode, cmpyName);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(list).build();

    return res;
  }

}
