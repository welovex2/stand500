package egovframework.sam.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
import egovframework.cmm.util.EgovFileMngUtil;
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.rte.fdl.property.EgovPropertyService;
import egovframework.sam.dto.ImDTO;
import egovframework.sam.dto.ImSubDTO;
import egovframework.sam.service.SamService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Api(tags = {"시료"})
@RestController
@RequestMapping("/sam")
public class SamController {

  @Resource(name = "SamService")
  private SamService samService;
  
  @Resource(name = "EgovFileMngUtil")
  private EgovFileMngUtil fileUtil;
  
  @Resource(name = "EgovFileMngService")
  private EgovFileMngService fileMngService;
  
  @Resource(name = "propertiesService")
  protected EgovPropertyService propertyService;
  
  @ApiOperation(value = "시료반입 등록 ImDTO",
      notes = "0. 신규등록시 sbkId 값 필수\n" + "1. 수정시 imId 필수, 파일추가시 picUrl 필수\n"
          + "2. picList(사진파일) 같은이름으로 여러개\n"
          + "3. delFileList : atchFileId(picUrl 값), fileSn(파일순번)\n"
          + "4. itemList 시료리스트 \n"
          + "   - 반출시 update 처리와 동일\n"
          + "   - STATE:U, carryOut 에 값 넣어주기\n"
          + "5. 반입/반출과정 공통코드 : TC")
  @PostMapping(value = "/insert.do")
  public BasicResponse insert(@RequestPart(value = "req") ImDTO req,
      @RequestPart(value = "delFileList", required = false) List<FileVO> delFileList,
      @RequestPart(value = "picList", required = false) final List<MultipartFile> picList)
      throws Exception {

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

    Set<ConstraintViolation<ImDTO>> violations = validator.validate(req);

    for (ConstraintViolation<ImDTO> violation : violations) {
      msg = violation.getMessage();
      System.out.println("violation ERROR::" + msg);

      BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

      return res;
    }

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {

      List<FileVO> FileResult = null;
      FileVO oneFile = null;
      String atchFileId = "";


      // 보완파일
      if (!ObjectUtils.isEmpty(picList)) {

        // 신규등록
        if (StringUtils.isEmpty(req.getPicUrl())) {
          FileResult = fileUtil.parseFile(picList, "SAM", 0, "", "");
          atchFileId = fileMngService.insertFileInfs(FileResult);
          req.setPicUrl(atchFileId);
        }
        // 수정
        else {
          // 현재 등록된 파일 수 가져오기
          FileVO fvo = new FileVO();
          fvo.setAtchFileId(req.getPicUrl());
          int cnt = fileMngService.getMaxFileSN(fvo);

          // 추가 파일 등록
          List<FileVO> _result =
              fileUtil.parseFile(picList, "SAM", cnt, req.getPicUrl(), "");
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
        if (StringUtils.isEmpty(req.getImId()))
          result = samService.insert(req);
        else
          result = samService.update(req);

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



    BasicResponse res =
        BasicResponse.builder().result(result).message(msg).build();

    return res;
  }
  
  @ApiOperation(value = "시료 상세보기")
  @GetMapping(value = "/{sbkId}/detail.do")
  public BasicResponse detail(@ApiParam(value = "신청서 고유번호", required = true,  example = "SB23-G0016") 
                            @PathVariable(name = "sbkId") String sbkId) throws Exception {
    
    boolean result = true;
    String msg = "";
    ImDTO detail = new ImDTO();

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    if (!isAuthenticated) {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
      
      BasicResponse res =
          BasicResponse.builder().result(result).message(msg).build();

      return res;
    }
    
    detail = samService.detail(sbkId);

    if (detail == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    } else {
      // 시료이미지
      FileVO fileVO = new FileVO();
      fileVO.setAtchFileId(detail.getPicUrl());
      List<FileVO> picResult = fileMngService.selectImageFileList(fileVO);
      picResult.stream().map(pic -> {pic.setFileStreCours(propertyService.getString("img.url").concat(pic.getAtchFileId()).concat("&fileSn=").concat(pic.getFileSn())); return pic;}).collect(Collectors.toList());
      detail.setPicList(picResult);
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();

    return res;
  }

  @ApiOperation(value = "시료 리스트", notes = "검색박스는 공통코드 CS, 필요한항목만 노출시켜서 사용\n"
      + " 54    시료반출일,21   시료반입일,4 컨설팅명,12  회사명,6   제품명,27  모델명,55  시료담당자")
  @GetMapping(value = "/list.do")
  public BasicResponse list(@ModelAttribute ComParam param) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";

    List<ImSubDTO> list = new ArrayList<ImSubDTO>();
    
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
    
    // 페이징
    param.setPageUnit(param.getPageUnit());
    param.setPageSize(propertyService.getInt("pageSize"));

    PagingVO pagingVO = new PagingVO();

    pagingVO.setCurrentPageNo(param.getPageIndex());
    pagingVO.setDisplayRow(param.getPageUnit());
    pagingVO.setDisplayPage(param.getPageSize());

    param.setFirstIndex(pagingVO.getFirstRecordIndex());
    int cnt = samService.selectListCnt(param);
    
    param.setTotalCount(cnt);
    pagingVO.setTotalCount(cnt);
    pagingVO.setTotalPage(
        (int) Math.ceil(pagingVO.getTotalCount() / (double) pagingVO.getDisplayRow()));
    list = samService.selectList(param);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res =
        BasicResponse.builder().result(result).message(msg).data(list).paging(pagingVO).build();
    return res;
  }
}
