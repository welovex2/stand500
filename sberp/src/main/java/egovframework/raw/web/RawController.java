package egovframework.raw.web;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
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
import egovframework.cmm.util.CdnFileMngUtil;
import egovframework.cmm.util.EgovFileMngUtil;
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.raw.dto.CeDTO;
import egovframework.raw.dto.CsDTO;
import egovframework.raw.dto.CtiDTO;
import egovframework.raw.dto.EftDTO;
import egovframework.raw.dto.EsdDTO;
import egovframework.raw.dto.FileRawDTO;
import egovframework.raw.dto.ImgDTO;
import egovframework.raw.dto.InfoDTO;
import egovframework.raw.dto.MfDTO;
import egovframework.raw.dto.PicDTO;
import egovframework.raw.dto.PicVO;
import egovframework.raw.dto.RawDTO;
import egovframework.raw.dto.ReDTO;
import egovframework.raw.dto.RegStateDTO;
import egovframework.raw.dto.RsDTO;
import egovframework.raw.dto.SurgeDTO;
import egovframework.raw.dto.VdipDTO;
import egovframework.raw.service.FileRaw;
import egovframework.raw.service.RawData;
import egovframework.raw.service.RawService;
import egovframework.rte.fdl.property.EgovPropertyService;
import egovframework.sys.service.MacService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags = {"로데이터"})
@RestController
@RequestMapping("/raw")
public class RawController {

  @Resource(name = "RawService")
  private RawService rawService;

  @Resource(name = "EgovFileMngUtil")
  private EgovFileMngUtil fileUtil;

  @Resource(name = "CdnFileMngUtil")
  private CdnFileMngUtil cdnUtil;

  @Resource(name = "EgovFileMngService")
  private EgovFileMngService fileMngService;
  
  @Resource(name = "propertiesService")
  protected EgovPropertyService propertyService;

  @Resource(name = "MacService")
  private MacService macService;

  @ApiOperation(value = "로데이터 작성여부 확인")
  @GetMapping(value = "/{testSeq}/regState.do")
  public BasicResponse regState(@ApiParam(value = "시험 고유번호", required = true,
      example = "9") @PathVariable(name = "testSeq") int testSeq) throws Exception {
    boolean result = true;
    String msg = "";
    RegStateDTO detail = new RegStateDTO();
    RawData rawData = new RawData();

    // 기본정보
    rawData = rawService.detail(testSeq);
    if (!ObjectUtils.isEmpty(rawData)) {
      detail.setRawSeq(rawData.getRawSeq());
      detail.setRawYn(1);
    }

    // CE
    if (!ObjectUtils.isEmpty(rawService.ceDetail(detail.getRawSeq())))
      detail.setCeYn(1);

    // RE
    if (!ObjectUtils.isEmpty(rawService.reDetail(detail.getRawSeq())))
      detail.setReYn(1);

    // ESD
    if (!ObjectUtils.isEmpty(rawService.esdDetail(detail.getRawSeq())))
      detail.setEdYn(1);

    // RS
    if (!ObjectUtils.isEmpty(rawService.rsDetail(detail.getRawSeq())))
      detail.setRsYn(1);

    // EFT / B U R S T
    if (!ObjectUtils.isEmpty(rawService.eftDetail(detail.getRawSeq())))
      detail.setEtYn(1);

    // SURGE
    if (!ObjectUtils.isEmpty(rawService.surgeDetail(detail.getRawSeq())))
      detail.setSgYn(1);

    // CS
    if (!ObjectUtils.isEmpty(rawService.csDetail(detail.getRawSeq())))
      detail.setCsYn(1);

    // M-Field
    if (!ObjectUtils.isEmpty(rawService.mfDetail(detail.getRawSeq())))
      detail.setMfYn(1);

    // V-Dip
    if (!ObjectUtils.isEmpty(rawService.vdipDetail(detail.getRawSeq())))
      detail.setVdYn(1);

    // 시험장면 사진
    ImgDTO pic = new ImgDTO();
    pic.setRawSeq(detail.getRawSeq());
    for (int i = 1; i < 14; i++) {
      pic.setPicId(Integer.toString(i));
      if (!ObjectUtils.isEmpty(rawService.imgDetail(pic))) {
        detail.setPcYn(1);
        break;
      }
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();

    return res;
  }


  @ApiOperation(value = "로데이터 신규작성시 연동할 데이터 불러오기")
  @GetMapping(value = "/{testSeq}/info.do")
  public BasicResponse info(@ApiParam(value = "시험 고유번호", required = true,
      example = "39") @PathVariable(name = "testSeq") int testSeq) throws Exception {
    boolean result = true;
    String msg = "";
    InfoDTO detail = new InfoDTO();

    detail = rawService.info(testSeq);

    if (detail == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();

    return res;
  }

  @ApiOperation(value = "로데이터 기본정보 불러오기")
  @GetMapping(value = "/{testId}/view.do")
  public BasicResponse rawDetail(@ApiParam(value = "시험 접수번호", required = true, example = "SB23-G0178-MD0014") @PathVariable(name = "testId") String testId) throws Exception {
    boolean result = true;
    String msg = "";
    RawData detail = new RawData();
    int testSeq = rawService.getTestSeq(testId);
    
    if (testSeq == 0) {
      
      result = false;
      msg = ResponseMessage.NO_DATA;
    
    } else {
    
      detail = rawService.detail(testSeq);
      
      if (detail == null) {
        result = false;
        msg = ResponseMessage.NO_DATA;
      } else {
        
        // 사진정보는 불러오지 않음
        detail.setTestSignUrl("");
        detail.setRevSignUrl("");
        detail.setModUrl("");
        detail.setSetupUrl("");
        detail.setModFileList(new ArrayList<PicDTO>());
        detail.setSetupList(new ArrayList<PicDTO>());
        
      }
      
    }
    
    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();

    return res;
  }
  
  @ApiOperation(value = "로데이터 기본정보 상세보기")
  @GetMapping(value = "/{testSeq}/detail.do")
  public BasicResponse rawDetail(@ApiParam(value = "시험 고유번호", required = true, example = "9") @PathVariable(name = "testSeq") int testSeq) throws Exception {
    boolean result = true;
    String msg = "";
    RawData detail = new RawData();

    detail = rawService.detail(testSeq);

    if (detail == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();

    return res;
  }

  @ApiOperation(value = "기본정보 등록",
      notes = "0. 신규등록시 TestSeq 값 필수\n"
          + "1. 수정시 rawseq 필수, 파일추가시 ModUrl 과 SetupUrl 필수, 리스트항목은 견적서 시험항목 처리와 동일(state)\n"
          + "2. modFileList(보완사항파일) 같은이름으로 여러개\n"
          + "3. delFileList(리스트파일일 경우만) : atchFileId(ModUrl 과 SetupUrl 값), fileSn(파일순번)\n"
          + "4. 기술제원 주파수구분(RC)\n"
          + "5. uptFileList(셋업파일리트스 타이틀 수정시 사용)-atchFileId(SetupUrl), fileSn(파일순번), fileCn(타이틀)")
  @PostMapping(value = "/insert.do")
  public BasicResponse insert(@RequestPart(value = "rawData") RawData req,
      @RequestPart(value = "delFileList", required = false) List<FileVO> delFileList,
      @RequestPart(value = "uptFileList", required = false) List<FileVO> uptFileList,
      @ModelAttribute RawDTO raw) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";
    boolean result = false;

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());

    System.out.println("=-===========");
    System.out.println(req.toString());
    System.out.println("=-===========");
    System.out.println(uptFileList);
    System.out.println("=-===========");

    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    Validator validator = validatorFactory.getValidator();

    Set<ConstraintViolation<RawData>> violations = validator.validate(req);

    for (ConstraintViolation<RawData> violation : violations) {
      msg = violation.getMessage();
      System.out.println("violation ERROR::" + msg);

      BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

      return res;
    }

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {

      // 이미 등록된 로데이터가 있는지 확인
      if (req.getRawSeq() == 0 && !ObjectUtils.isEmpty(rawService.detail(req.getTestSeq()))) {
        result = false;
        msg = ResponseMessage.DUPLICATE_RAW;
      } else {

        List<FileVO> FileResult = null;
        FileVO oneFile = null;
        String atchFileId = "";

        // 시험자 서명
        if (!ObjectUtils.isEmpty(raw.getTestSign())) {
          oneFile = fileUtil.parseFile(raw.getTestSign(), "RAW/SIGN", 0, "", "");
          atchFileId = fileMngService.insertFileInf(oneFile);
          req.setTestSignUrl(atchFileId);
        }

        // 기술책임자 서명
        if (!ObjectUtils.isEmpty(raw.getRevSign())) {
          oneFile = fileUtil.parseFile(raw.getRevSign(), "RAW/SIGN", 0, "", "");
          atchFileId = fileMngService.insertFileInf(oneFile);
          req.setRevSignUrl(atchFileId);
        }

        // 보완파일
        if (!ObjectUtils.isEmpty(raw.getModFileList())) {

          // 신규등록
          if (StringUtils.isEmpty(req.getModUrl())) {
            FileResult = fileUtil.parseFile(raw.getModFileList(), "RAW", 0, "", "");
            atchFileId = fileMngService.insertFileInfs(FileResult);
            req.setModUrl(atchFileId);
          }
          // 수정
          else {
            // 현재 등록된 파일 수 가져오기
            FileVO fvo = new FileVO();
            fvo.setAtchFileId(req.getModUrl());
            int cnt = fileMngService.getMaxFileSN(fvo);

            // 추가 파일 등록
            List<FileVO> _result =
                fileUtil.parseFile(raw.getModFileList(), "RAW", cnt, req.getModUrl(), "");
            fileMngService.updateFileInfs(_result);
          }
        }

        // 셋업파일
        if (!ObjectUtils.isEmpty(raw.getSetupList())) {

          // 신규등록
          if (StringUtils.isEmpty(req.getSetupUrl())) {
            FileResult = fileUtil.parsePicFile(raw.getSetupList(), "RAW", 0, "", "");
            atchFileId = fileMngService.insertFileInfs(FileResult);
            req.setSetupUrl(atchFileId);
          }
          // 수정
          else {
            // 현재 등록된 파일 수 가져오기
            FileVO fvo = new FileVO();
            fvo.setAtchFileId(req.getSetupUrl());
            int cnt = fileMngService.getMaxFileSN(fvo);

            // 추가 파일 등록
            List<FileVO> _result =
                fileUtil.parsePicFile(raw.getSetupList(), "RAW", cnt, req.getSetupUrl(), "");
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

        // 14. Test Set-up Configuraiotn for EUT 타이틀 수정
        FileVO uptFile = null;
        if (!ObjectUtils.isEmpty(uptFileList)) {
          for (FileVO del : uptFileList) {
            delFile = new FileVO();
            delFile.setAtchFileId(del.getAtchFileId());
            delFile.setFileSn(del.getFileSn());
            delFile.setFileCn(del.getFileCn());
            fileMngService.updateFileDetail(delFile);
          }
        }

        try {
          if (req.getRawSeq() == 0)
            result = rawService.insert(req);
          else
            result = rawService.update(req);

        } catch (Exception e) {
          msg = e.getCause().toString();
        }

      }

    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
    }



    BasicResponse res =
        BasicResponse.builder().result(result).data(req.getRawSeq()).message(msg).build();

    return res;
  }

  @ApiOperation(value = "로데이터 CE 상세보기")
  @GetMapping(value = "/{rawSeq}/ce/detail.do")
  public BasicResponse ceDetail(@ApiParam(value = "로데이터 고유번호", required = true,
      example = "7") @PathVariable(name = "rawSeq") int rawSeq) throws Exception {
    boolean result = true;
    String msg = "";
    CeDTO detail = new CeDTO();

    detail = rawService.ceDetail(rawSeq);

    if (detail == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();

    return res;
  }

  @ApiOperation(value = "로데이터 > CE 등록",
      notes = "0. 신규등록시 rawSeq 또는 TestSeq(rawSeq없을때만)값 필수\n"
          + "1. 수정시 rawSeq 필수, 파일추가시 resultUrl 필수\n "
          + "2. delFileList(리스트파일일  경우만) : atchFileId(resultUrl값), fileCn(파일순번)\n"
          + "3. signFile 시험자 서명, resultFiles 시험결과")
  @PostMapping(value = "/ce/insert.do")
  public BasicResponse insertCe(@RequestPart(value = "ceDTO") CeDTO req,
//      @RequestPart(value = "delFileList", required = false) List<FileVO> delFileList,
      @RequestPart(value = "signFile", required = false) MultipartFile signFile
//      @RequestPart(value = "resultFiles", required = false) final List<MultipartFile> resultFiles
      )
      throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";
    boolean result = false;

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());
    req.setMacType("CE");

    System.out.println("=-===========");
    System.out.println(req.toString());
    System.out.println("=-===========");

    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    Validator validator = validatorFactory.getValidator();

    Set<ConstraintViolation<CeDTO>> violations = validator.validate(req);

    for (ConstraintViolation<CeDTO> violation : violations) {
      msg = violation.getMessage();

      System.out.println("violation ERROR::" + msg);
      BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

      return res;
    }

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    // 신규등록시 필수 값 확인
    if (req.getRawSeq() == 0 && req.getTestSeq() == 0) {
      result = false;
      msg = ResponseMessage.CHECK_DATA;
    } else {

      if (isAuthenticated) {

        List<FileVO> FileResult = null;
        FileVO oneFile = null;
        String atchFileId = "";

        // 시험자 서명
        if (!ObjectUtils.isEmpty(signFile)) {
          oneFile = fileUtil.parseFile(signFile, "RAW/SIGN", 0, "", "");
          atchFileId = fileMngService.insertFileInf(oneFile);
          req.setSignUrl(atchFileId);
        }

        result = rawService.insertCe(req);

      } else {
        result = false;
        msg = ResponseMessage.UNAUTHORIZED;
      }

    }

    BasicResponse res =
        BasicResponse.builder().result(result).data(req.getRawSeq()).message(msg).build();

    return res;
  }

  @ApiOperation(value = "로데이터 RE 상세보기")
  @GetMapping(value = "/{rawSeq}/re/detail.do")
  public BasicResponse reDetail(@ApiParam(value = "로데이터 고유번호", required = true,
      example = "7") @PathVariable(name = "rawSeq") int rawSeq) throws Exception {
    boolean result = true;
    String msg = "";
    ReDTO detail = new ReDTO();

    detail = rawService.reDetail(rawSeq);

    if (detail == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();

    return res;
  }

  @ApiOperation(value = "로데이터 > RE 등록",
      notes = "0. 신규등록시 rawSeq 또는 TestSeq(rawSeq없을때만)값 필수\n"
          + "1. 수정시 rawSeq 필수, 파일추가시 resultUrl 필수\n "
          + "2. delFileList(리스트파일일  경우만) : atchFileId(resultUrl값), fileCn(파일순번)\n"
          + "3. signFile1 대역1 서명, signFile2 대역2 서명, resultFiles 시험결과\n" + "4. 대역코드(RH)")
  @PostMapping(value = "/re/insert.do")
  public BasicResponse insertRe(@RequestPart(value = "reDTO") ReDTO req,
//      @RequestPart(value = "delFileList", required = false) List<FileVO> delFileList,
      @RequestPart(value = "signFile1", required = false) MultipartFile signFile1,
      @RequestPart(value = "signFile2", required = false) MultipartFile signFile2
//      @RequestPart(value = "resultFiles", required = false) final List<MultipartFile> resultFiles
      )
      throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";
    boolean result = false;

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());
    req.setMacType("RE");

    System.out.println("=-===========");
    System.out.println(req.toString());
    System.out.println("=-===========");

    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    Validator validator = validatorFactory.getValidator();

    Set<ConstraintViolation<ReDTO>> violations = validator.validate(req);

    for (ConstraintViolation<ReDTO> violation : violations) {
      msg = violation.getMessage();

      System.out.println("violation ERROR::" + msg);
      BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

      return res;
    }

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    // 신규등록시 필수 값 확인
    if (req.getRawSeq() == 0 && req.getTestSeq() == 0) {
      result = false;
      msg = ResponseMessage.CHECK_DATA;
    } else {

      if (isAuthenticated) {

        List<FileVO> FileResult = null;
        FileVO oneFile = null;
        String atchFileId = "";

        // 대역1 시험자 서명
        if (!ObjectUtils.isEmpty(signFile1)) {
          oneFile = fileUtil.parseFile(signFile1, "RAW/SIGN", 0, "", "");
          atchFileId = fileMngService.insertFileInf(oneFile);
          req.setHz1SignUrl(atchFileId);
        }
        // 대역2 시험자 서명
        if (!ObjectUtils.isEmpty(signFile2)) {
          oneFile = fileUtil.parseFile(signFile2, "RAW/SIGN", 0, "", "");
          atchFileId = fileMngService.insertFileInf(oneFile);
          req.setHz2SignUrl(atchFileId);
        }

//        // 시험결과 별도첨부
//        if (!ObjectUtils.isEmpty(resultFiles)) {
//
//          // 신규등록
//          if (StringUtils.isEmpty(req.getResultUrl())) {
//            FileResult = fileUtil.parseFile(resultFiles, "RAW", 0, "", "");
//            atchFileId = fileMngService.insertFileInfs(FileResult);
//            req.setResultUrl(atchFileId);
//          }
//          // 수정
//          else {
//            // 현재 등록된 파일 수 가져오기
//            FileVO fvo = new FileVO();
//            fvo.setAtchFileId(req.getResultUrl());
//            int cnt = fileMngService.getMaxFileSN(fvo);
//
//            // 추가 파일 등록
//            List<FileVO> _result =
//                fileUtil.parseFile(resultFiles, "RAW", cnt, req.getResultUrl(), "");
//            fileMngService.updateFileInfs(_result);
//          }
//        }
//
//        // 파일삭제
//        FileVO delFile = null;
//        if (!ObjectUtils.isEmpty(delFileList)) {
//          for (FileVO del : delFileList) {
//            delFile = new FileVO();
//            delFile.setAtchFileId(del.getAtchFileId());
//            delFile.setFileSn(del.getFileSn());
//            fileMngService.deleteFileInf(delFile);
//          }
//        }

        result = rawService.insertRe(req);

      } else {
        result = false;
        msg = ResponseMessage.UNAUTHORIZED;
      }

    }

    BasicResponse res =
        BasicResponse.builder().result(result).data(req.getRawSeq()).message(msg).build();

    return res;
  }

  @ApiOperation(value = "로데이터 ESD 상세보기")
  @GetMapping(value = "/{rawSeq}/esd/detail.do")
  public BasicResponse esdDetail(@ApiParam(value = "로데이터 고유번호", required = true,
      example = "7") @PathVariable(name = "rawSeq") int rawSeq) throws Exception {
    boolean result = true;
    String msg = "";
    EsdDTO detail = new EsdDTO();

    detail = rawService.esdDetail(rawSeq);

    if (detail == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();

    return res;
  }

  @ApiOperation(value = "로데이터 > ESD 등록",
      notes = "0. 신규등록시 rawSeq 또는 TestSeq(rawSeq없을때만)값 필수\n"
          + "1. 수정시 rawSeq 필수, 파일추가시 imgUrl 필수\n " + "2. signFile 시험자 서명\n" + "3. 시험결과(A,B,C)"
          + "4. uptFileList(타이틀 수정시 사용) : atchFileId(imgUrl), fileSn(파일순번), fileCn(타이틀)\n"
          + "5. delFileList : atchFileId(imgUrl), fileSn(파일순번)\n")
  @PostMapping(value = "/esd/insert.do")
  public BasicResponse insertEsd(@RequestPart(value = "esdDTO") EsdDTO req,
      @RequestPart(value = "signFile", required = false) MultipartFile signFile,
      @RequestPart(value = "delFileList", required = false) List<FileVO> delFileList,
      @RequestPart(value = "uptFileList", required = false) List<FileVO> uptFileList,
      @ModelAttribute ImgDTO img) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";
    boolean result = false;

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());
    req.setMacType("ED");

    System.out.println("=-===========");
    System.out.println(req.toString());
    System.out.println("=-===========");

    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    Validator validator = validatorFactory.getValidator();

    Set<ConstraintViolation<EsdDTO>> violations = validator.validate(req);

    for (ConstraintViolation<EsdDTO> violation : violations) {
      msg = violation.getMessage();

      System.out.println("violation ERROR::" + msg);
      BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

      return res;
    }

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    // 신규등록시 필수 값 확인
    if (req.getRawSeq() == 0 && req.getTestSeq() == 0) {
      result = false;
      msg = ResponseMessage.CHECK_DATA;
    } else {

      if (isAuthenticated) {

        FileVO oneFile = null;
        String atchFileId = "";
        List<FileVO> FileResult = null;

        // 시험자 서명
        if (!ObjectUtils.isEmpty(signFile)) {
          oneFile = fileUtil.parseFile(signFile, "RAW/SIGN", 0, "", "");
          atchFileId = fileMngService.insertFileInf(oneFile);
          req.setSignUrl(atchFileId);
        }

        // 정전기 방전 인가부위
        if (!ObjectUtils.isEmpty(img.getImgList())) {

          // 신규등록
          if (StringUtils.isEmpty(req.getImgUrl())) {
            FileResult = fileUtil.parsePicFile(img.getImgList(), "RAW", 0, "", "");
            atchFileId = fileMngService.insertFileInfs(FileResult);
            req.setImgUrl(atchFileId);
          }
          // 수정
          else {
            // 현재 등록된 파일 수 가져오기
            FileVO fvo = new FileVO();
            fvo.setAtchFileId(req.getImgUrl());
            int cnt = fileMngService.getMaxFileSN(fvo);

            // 추가 파일 등록
            List<FileVO> _result =
                fileUtil.parsePicFile(img.getImgList(), "RAW", cnt, req.getImgUrl(), "");
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

        // 14. 정전기 방전 인가부위 타이틀 수정
        FileVO uptFile = null;
        if (!ObjectUtils.isEmpty(uptFileList)) {
          for (FileVO upt : uptFileList) {
            uptFile = new FileVO();
            uptFile.setAtchFileId(upt.getAtchFileId());
            uptFile.setFileSn(upt.getFileSn());
            uptFile.setFileCn(upt.getFileCn());
            fileMngService.updateFileDetail(uptFile);
          }
        }

        result = rawService.insertEsd(req);

      } else {
        result = false;
        msg = ResponseMessage.UNAUTHORIZED;
      }

    }

    BasicResponse res =
        BasicResponse.builder().result(result).data(req.getRawSeq()).message(msg).build();

    return res;
  }

  @ApiOperation(value = "로데이터 RS 상세보기")
  @GetMapping(value = "/{rawSeq}/rs/detail.do")
  public BasicResponse rsDetail(@ApiParam(value = "로데이터 고유번호", required = true,
      example = "7") @PathVariable(name = "rawSeq") int rawSeq) throws Exception {
    boolean result = true;
    String msg = "";
    RsDTO detail = new RsDTO();

    detail = rawService.rsDetail(rawSeq);

    if (detail == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();

    return res;
  }

  @ApiOperation(value = "로데이터 > RS 등록", notes = "0. 신규등록시 rawSeq 또는 TestSeq(rawSeq없을때만)값 필수\n"
      + "1. 수정시 rawSeq 필수\n " + "2. signFile 시험자 서명")
  @PostMapping(value = "/rs/insert.do")
  public BasicResponse insertRs(@RequestPart(value = "rsDTO") RsDTO req,
      @RequestPart(value = "signFile", required = false) MultipartFile signFile) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";
    boolean result = false;

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());
    req.setMacType("RS");

    System.out.println("=-===========");
    System.out.println(req.toString());
    System.out.println("=-===========");

    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    Validator validator = validatorFactory.getValidator();

    Set<ConstraintViolation<RsDTO>> violations = validator.validate(req);

    for (ConstraintViolation<RsDTO> violation : violations) {
      msg = violation.getMessage();

      System.out.println("violation ERROR::" + msg);
      BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

      return res;
    }

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    // 신규등록시 필수 값 확인
    if (req.getRawSeq() == 0 && req.getTestSeq() == 0) {
      result = false;
      msg = ResponseMessage.CHECK_DATA;
    } else {

      if (isAuthenticated) {

        FileVO oneFile = null;
        String atchFileId = "";

        // 시험자 서명
        if (!ObjectUtils.isEmpty(signFile)) {
          oneFile = fileUtil.parseFile(signFile, "RAW/SIGN", 0, "", "");
          atchFileId = fileMngService.insertFileInf(oneFile);
          req.setSignUrl(atchFileId);
        }

        result = rawService.insertRs(req);

      } else {
        result = false;
        msg = ResponseMessage.UNAUTHORIZED;
      }

    }

    BasicResponse res =
        BasicResponse.builder().result(result).data(req.getRawSeq()).message(msg).build();

    return res;
  }

  @ApiOperation(value = "로데이터 EFT / B U R S T 상세보기")
  @GetMapping(value = "/{rawSeq}/eft/detail.do")
  public BasicResponse eftDetail(@ApiParam(value = "로데이터 고유번호", required = true,
      example = "7") @PathVariable(name = "rawSeq") int rawSeq) throws Exception {
    boolean result = true;
    String msg = "";
    EftDTO detail = new EftDTO();

    detail = rawService.eftDetail(rawSeq);

    if (detail == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();

    return res;
  }

  @ApiOperation(value = "로데이터 > EFT / B U R S T 등록",
      notes = "0. 신규등록시 rawSeq 또는 TestSeq(rawSeq없을때만)값 필수\n" + "1. 수정시 rawSeq 필수\n "
          + "2. signFile 시험자 서명")
  @PostMapping(value = "/eft/insert.do")
  public BasicResponse insertEft(@RequestPart(value = "eftDTO") EftDTO req,
      @RequestPart(value = "signFile", required = false) MultipartFile signFile) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";
    boolean result = false;

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());
    req.setMacType("ET");

    System.out.println("=-===========");
    System.out.println(req.toString());
    System.out.println("=-===========");

    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    Validator validator = validatorFactory.getValidator();

    Set<ConstraintViolation<EftDTO>> violations = validator.validate(req);

    for (ConstraintViolation<EftDTO> violation : violations) {
      msg = violation.getMessage();

      System.out.println("violation ERROR::" + msg);
      BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

      return res;
    }

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    // 신규등록시 필수 값 확인
    if (req.getRawSeq() == 0 && req.getTestSeq() == 0) {
      result = false;
      msg = ResponseMessage.CHECK_DATA;
    } else {

      if (isAuthenticated) {

        FileVO oneFile = null;
        String atchFileId = "";

        // 시험자 서명
        if (!ObjectUtils.isEmpty(signFile)) {
          oneFile = fileUtil.parseFile(signFile, "RAW/SIGN", 0, "", "");
          atchFileId = fileMngService.insertFileInf(oneFile);
          req.setSignUrl(atchFileId);
        }

        result = rawService.insertEft(req);

      } else {
        result = false;
        msg = ResponseMessage.UNAUTHORIZED;
      }

    }

    BasicResponse res =
        BasicResponse.builder().result(result).data(req.getRawSeq()).message(msg).build();

    return res;
  }

  @ApiOperation(value = "등록")
  @PostMapping(value = "/insert2.do")
  public BasicResponse insert2(@RequestBody VdipDTO req) {
    boolean result = false;

    System.out.println(req.toString());

    // rawService.insert(req);

    BasicResponse res = BasicResponse.builder().result(result).message("").build();

    return res;
  }

  @ApiOperation(value = "로데이터 S U R G E 상세보기")
  @GetMapping(value = "/{rawSeq}/surge/detail.do")
  public BasicResponse surgeDetail(@ApiParam(value = "로데이터 고유번호", required = true,
      example = "7") @PathVariable(name = "rawSeq") int rawSeq) throws Exception {
    boolean result = true;
    String msg = "";
    SurgeDTO detail = new SurgeDTO();

    detail = rawService.surgeDetail(rawSeq);

    if (detail == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();

    return res;
  }

  @ApiOperation(value = "로데이터 > S U R G E 등록",
      notes = "0. 신규등록시 rawSeq 또는 TestSeq(rawSeq없을때만)값 필수\n" + "1. 수정시 rawSeq 필수\n "
          + "2. signFile 시험자 서명")
  @PostMapping(value = "/surge/insert.do")
  public BasicResponse insertSurge(@RequestPart(value = "surgeDTO") SurgeDTO req,
      @RequestPart(value = "signFile", required = false) MultipartFile signFile) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";
    boolean result = false;

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());
    req.setMacType("SG");

    System.out.println("=-===========");
    System.out.println(req.toString());
    System.out.println("=-===========");

    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    Validator validator = validatorFactory.getValidator();

    Set<ConstraintViolation<SurgeDTO>> violations = validator.validate(req);

    for (ConstraintViolation<SurgeDTO> violation : violations) {
      msg = violation.getMessage();

      System.out.println("violation ERROR::" + msg);
      BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

      return res;
    }

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    // 신규등록시 필수 값 확인
    if (req.getRawSeq() == 0 && req.getTestSeq() == 0) {
      result = false;
      msg = ResponseMessage.CHECK_DATA;
    } else {

      if (isAuthenticated) {

        FileVO oneFile = null;
        String atchFileId = "";

        // 시험자 서명
        if (!ObjectUtils.isEmpty(signFile)) {
          oneFile = fileUtil.parseFile(signFile, "RAW/SIGN", 0, "", "");
          atchFileId = fileMngService.insertFileInf(oneFile);
          req.setSignUrl(atchFileId);
        }

        result = rawService.insertSurge(req);

      } else {
        result = false;
        msg = ResponseMessage.UNAUTHORIZED;
      }

    }

    BasicResponse res =
        BasicResponse.builder().result(result).data(req.getRawSeq()).message(msg).build();

    return res;
  }

  @ApiOperation(value = "로데이터 CS 상세보기")
  @GetMapping(value = "/{rawSeq}/cs/detail.do")
  public BasicResponse csDetail(@ApiParam(value = "로데이터 고유번호", required = true,
      example = "7") @PathVariable(name = "rawSeq") int rawSeq) throws Exception {
    boolean result = true;
    String msg = "";
    CsDTO detail = new CsDTO();

    detail = rawService.csDetail(rawSeq);

    if (detail == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();

    return res;
  }

  @ApiOperation(value = "로데이터 > CS 등록", notes = "0. 신규등록시 rawSeq 또는 TestSeq(rawSeq없을때만)값 필수\n"
      + "1. 수정시 rawSeq 필수\n " + "2. signFile 시험자 서명")
  @PostMapping(value = "/cs/insert.do")
  public BasicResponse insertCs(@RequestPart(value = "csDTO") CsDTO req,
      @RequestPart(value = "signFile", required = false) MultipartFile signFile) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";
    boolean result = false;

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());
    req.setMacType("CS");

    System.out.println("=-===========");
    System.out.println(req.toString());
    System.out.println("=-===========");

    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    Validator validator = validatorFactory.getValidator();

    Set<ConstraintViolation<CsDTO>> violations = validator.validate(req);

    for (ConstraintViolation<CsDTO> violation : violations) {
      msg = violation.getMessage();

      System.out.println("violation ERROR::" + msg);
      BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

      return res;
    }

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    // 신규등록시 필수 값 확인
    if (req.getRawSeq() == 0 && req.getTestSeq() == 0) {
      result = false;
      msg = ResponseMessage.CHECK_DATA;
    } else {

      if (isAuthenticated) {

        FileVO oneFile = null;
        String atchFileId = "";

        // 시험자 서명
        if (!ObjectUtils.isEmpty(signFile)) {
          oneFile = fileUtil.parseFile(signFile, "RAW/SIGN", 0, "", "");
          atchFileId = fileMngService.insertFileInf(oneFile);
          req.setSignUrl(atchFileId);
        }

        result = rawService.insertCs(req);

      } else {
        result = false;
        msg = ResponseMessage.UNAUTHORIZED;
      }

    }

    BasicResponse res =
        BasicResponse.builder().result(result).data(req.getRawSeq()).message(msg).build();

    return res;
  }

  @ApiOperation(value = "로데이터 M-Field 상세보기")
  @GetMapping(value = "/{rawSeq}/mfield/detail.do")
  public BasicResponse mfDetail(@ApiParam(value = "로데이터 고유번호", required = true,
      example = "7") @PathVariable(name = "rawSeq") int rawSeq) throws Exception {
    boolean result = true;
    String msg = "";
    MfDTO detail = new MfDTO();

    detail = rawService.mfDetail(rawSeq);

    if (detail == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();

    return res;
  }

  @ApiOperation(value = "로데이터 > M-Field 등록", notes = "0. 신규등록시 rawSeq 또는 TestSeq(rawSeq없을때만)값 필수\n"
      + "1. 수정시 rawSeq 필수\n " + "2. signFile 시험자 서명")
  @PostMapping(value = "/mfield/insert.do")
  public BasicResponse insertMf(@RequestPart(value = "mfDTO") MfDTO req,
      @RequestPart(value = "signFile", required = false) MultipartFile signFile) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";
    boolean result = false;

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());
    req.setMacType("MF");

    System.out.println("=-===========");
    System.out.println(req.toString());
    System.out.println("=-===========");

    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    Validator validator = validatorFactory.getValidator();

    Set<ConstraintViolation<MfDTO>> violations = validator.validate(req);

    for (ConstraintViolation<MfDTO> violation : violations) {
      msg = violation.getMessage();

      System.out.println("violation ERROR::" + msg);
      BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

      return res;
    }

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    // 신규등록시 필수 값 확인
    if (req.getRawSeq() == 0 && req.getTestSeq() == 0) {
      result = false;
      msg = ResponseMessage.CHECK_DATA;
    } else {

      if (isAuthenticated) {

        FileVO oneFile = null;
        String atchFileId = "";

        // 시험자 서명
        if (!ObjectUtils.isEmpty(signFile)) {
          oneFile = fileUtil.parseFile(signFile, "RAW/SIGN", 0, "", "");
          atchFileId = fileMngService.insertFileInf(oneFile);
          req.setSignUrl(atchFileId);
        }

        result = rawService.insertMf(req);

      } else {
        result = false;
        msg = ResponseMessage.UNAUTHORIZED;
      }

    }

    BasicResponse res =
        BasicResponse.builder().result(result).data(req.getRawSeq()).message(msg).build();

    return res;
  }


  @ApiOperation(value = "로데이터 V-Dip 상세보기")
  @GetMapping(value = "/{rawSeq}/vdip/detail.do")
  public BasicResponse vdDetail(@ApiParam(value = "로데이터 고유번호", required = true,
      example = "7") @PathVariable(name = "rawSeq") int rawSeq) throws Exception {
    boolean result = true;
    String msg = "";
    VdipDTO detail = new VdipDTO();

    detail = rawService.vdipDetail(rawSeq);

    if (detail == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();

    return res;
  }

  @ApiOperation(value = "로데이터 > V-Dip 등록", notes = "0. 신규등록시 rawSeq 또는 TestSeq(rawSeq없을때만)값 필수\n"
      + "1. 수정시 rawSeq 필수\n " + "2. signFile 시험자 서명")
  @PostMapping(value = "/vdip/insert.do")
  public BasicResponse insertVd(@RequestPart(value = "vdipDTO") VdipDTO req,
      @RequestPart(value = "signFile", required = false) MultipartFile signFile) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";
    boolean result = false;

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());
    req.setMacType("VD");

    System.out.println("=-===========");
    System.out.println(req.toString());
    System.out.println("=-===========");

    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    Validator validator = validatorFactory.getValidator();

    Set<ConstraintViolation<VdipDTO>> violations = validator.validate(req);

    for (ConstraintViolation<VdipDTO> violation : violations) {
      msg = violation.getMessage();

      System.out.println("violation ERROR::" + msg);
      BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

      return res;
    }

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    // 신규등록시 필수 값 확인
    if (req.getRawSeq() == 0 && req.getTestSeq() == 0) {
      result = false;
      msg = ResponseMessage.CHECK_DATA;
    } else {

      if (isAuthenticated) {

        FileVO oneFile = null;
        String atchFileId = "";

        // 시험자 서명
        if (!ObjectUtils.isEmpty(signFile)) {
          oneFile = fileUtil.parseFile(signFile, "RAW/SIGN", 0, "", "");
          atchFileId = fileMngService.insertFileInf(oneFile);
          req.setSignUrl(atchFileId);
        }

        result = rawService.insertVdip(req);

      } else {
        result = false;
        msg = ResponseMessage.UNAUTHORIZED;
      }

    }

    BasicResponse res =
        BasicResponse.builder().result(result).data(req.getRawSeq()).message(msg).build();

    return res;
  }

  @ApiOperation(value = "로데이터 CTI 상세보기")
  @GetMapping(value = "/{rawSeq}/cti/detail.do")
  public BasicResponse ctiDetail(@ApiParam(value = "로데이터 고유번호", required = true,
      example = "7") @PathVariable(name = "rawSeq") int rawSeq) throws Exception {
    boolean result = true;
    String msg = "";
    CtiDTO detail = new CtiDTO();

    detail = rawService.ctiDetail(rawSeq);

    if (detail == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();

    return res;
  }

  @ApiOperation(value = "로데이터 > CTI 등록",
      notes = "0. 신규등록시 rawSeq 또는 TestSeq(rawSeq없을때만)값 필수\n" + "1. 수정시 rawSeq 필수\n "
          + "2. signFile 시험자 서명\n"
          + "3. ctiDTO.subList (시험결과 고정리스트, ctiSubSeq=고정값으로 신규등록시부터 값 보내주기, dcType=12 또는 24")
  @PostMapping(value = "/cti/insert.do")
  public BasicResponse insertCti(@RequestPart(value = "ctiDTO") CtiDTO req,
      @RequestPart(value = "signFile", required = false) MultipartFile signFile) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";
    boolean result = false;

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());
    req.setMacType("CT");

    System.out.println("=-===========");
    System.out.println(req.toString());
    System.out.println("=-===========");

    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    Validator validator = validatorFactory.getValidator();

    Set<ConstraintViolation<CtiDTO>> violations = validator.validate(req);

    for (ConstraintViolation<CtiDTO> violation : violations) {
      msg = violation.getMessage();

      System.out.println("violation ERROR::" + msg);
      BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

      return res;
    }

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    // 신규등록시 필수 값 확인
    if (req.getRawSeq() == 0 && req.getTestSeq() == 0) {
      result = false;
      msg = ResponseMessage.CHECK_DATA;
    } else {

      if (isAuthenticated) {

        FileVO oneFile = null;
        String atchFileId = "";

        // 시험자 서명
        if (!ObjectUtils.isEmpty(signFile)) {
          oneFile = fileUtil.parseFile(signFile, "RAW/SIGN", 0, "", "");
          atchFileId = fileMngService.insertFileInf(oneFile);
          req.setSignUrl(atchFileId);
        }

        result = rawService.insertCti(req);

      } else {
        result = false;
        msg = ResponseMessage.UNAUTHORIZED;
      }

    }

    BasicResponse res =
        BasicResponse.builder().result(result).data(req.getRawSeq()).message(msg).build();

    return res;
  }

  @ApiOperation(value = "로데이터 시험장면사진 상세보기")
  @GetMapping(value = "/{rawSeq}/pic/{picId}/detail.do")
  public BasicResponse picDetail(
      @ApiParam(value = "로데이터 고유번호", required = true,
          example = "7") @PathVariable(name = "rawSeq") int rawSeq,
      @ApiParam(value = "사진 순번", required = true,
          example = "1") @PathVariable(name = "picId") String picId)
      throws Exception {
    boolean result = true;
    String msg = "";
    ImgDTO detail = new ImgDTO();

    detail.setRawSeq(rawSeq);
    detail.setPicId(picId);

    ImgDTO img = rawService.imgDetail(detail);

    if (rawSeq == 0 && picId == null) {
      result = false;
      msg = ResponseMessage.CHECK_DATA;
    } else {

      if (img != null) {
        FileVO fileVO = new FileVO();
        fileVO.setAtchFileId(img.getAtchFileId());
        // 해당없음이 있기 때문에 fileInf로 확인
        List<FileVO> fileReulst = fileMngService.selectFileInfs(fileVO);
        List<PicDTO> resultList = new ArrayList<PicDTO>();
        if (fileReulst != null) {
          for (FileVO item : fileReulst) {
            PicDTO pic = new PicDTO();
            if ("CDN".contentEquals(item.getFileLoc())) {
              pic.setImageUrl(propertyService.getString("cdn.url").concat(item.getFileStreCours()).concat("/")
                  .concat(item.getStreFileNm()).concat(".").concat(item.getFileExtsn()));
            } else {
              pic.setImageUrl(propertyService.getString("img.url").concat(img.getAtchFileId())
                  .concat("&fileSn=").concat(item.getFileSn()));
            }
            pic.setTitle(item.getFileCn());
            pic.setMode(item.getFileMemo());
            pic.setFileSn(item.getFileSn());
            resultList.add(pic);

          }
        }
        detail.setPicYn(img.getPicYn());
        detail.setImgList(resultList);
      } else {
        result = false;
        msg = ResponseMessage.NO_DATA;
      }
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();

    return res;
  }


  @ApiOperation(value = "시험장면사진 등록",
      notes = "0. 신규등록시 rawSeq 또는 TestSeq(rawSeq없을때만)값 필수\n" + "1. 수정시 rawSeq, picId 필수\n "
          + "2. delFileList : fileCn(파일순번)\n" + "3. 시험장면사진 순번 : 공통코드 RP\n"
          + "4. picList[0].mode=모드, picList[0].title=구분(공통코드 RG)\n" + "5. picYn : 해당됨1, 해당없음0")
  @PostMapping(value = "/pic/insert.do")
  public BasicResponse insertPic(@RequestPart(value = "imgDTO") ImgDTO req,
      @RequestPart(value = "delFileList", required = false) List<FileVO> delFileList,
      @ModelAttribute PicVO pic) throws Exception {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";
    boolean result = true;

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());

    System.out.println("=-===========");
    System.out.println(req.toString());
    System.out.println("=-===========");
    System.out.println(delFileList);
    System.out.println("=-===========");
    
    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    Validator validator = validatorFactory.getValidator();

    Set<ConstraintViolation<ImgDTO>> violations = validator.validate(req);

    for (ConstraintViolation<ImgDTO> violation : violations) {
      msg = violation.getMessage();

      System.out.println("violation ERROR::" + msg);
      BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

      return res;
    }

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    // 신규등록시 필수 값 확인
    if (req.getRawSeq() == 0 && req.getTestSeq() == 0) {
      result = false;
      msg = ResponseMessage.CHECK_DATA;
    } else {

      if (isAuthenticated) {

        List<FileVO> FileResult = null;
        ImgDTO atchFile = rawService.imgDetail(req);
        String atchFileId = "";
        if (atchFile != null) {
          atchFileId = atchFile.getAtchFileId();
        } else {
          atchFileId = "";
        }

        // 해당없음이 바뀔경우, 기존데이터 삭제
        FileVO upt = new FileVO();
        upt.setAtchFileId(atchFileId);
        if (req.getPicYn() == 0) {
          upt.setOrignlFileNm("ALL");
        } else {
          upt.setOrignlFileNm("");
        }
        fileMngService.deletePicAll(upt);

        // 시험장면사진
        if (!ObjectUtils.isEmpty(pic.getPicList())) {

          // 신규등록
          if (StringUtils.isEmpty(atchFileId)) {
            FileResult = fileUtil.parsePicFile(pic.getPicList(), "RAW", 0, "", "");
            atchFileId = fileMngService.insertFileInfs(FileResult);
            req.setAtchFileId(atchFileId);
          }
          // 수정
          else {
            // 현재 등록된 파일 수 가져오기
            FileVO fvo = new FileVO();
            fvo.setAtchFileId(atchFileId);
            int cnt = fileMngService.getMaxFileSN(fvo);

            // 추가 파일 등록
            List<FileVO> _result =
                fileUtil.parsePicFile(pic.getPicList(), "RAW", cnt, atchFileId, "");
            fileMngService.updateFileInfs(_result);
          }
          rawService.insertImg(req);
        }

        // 파일삭제
        FileVO delFile = null;
        if (!ObjectUtils.isEmpty(delFileList)) {
          for (FileVO del : delFileList) {
            delFile = new FileVO();
            delFile.setAtchFileId(atchFileId);
            delFile.setFileSn(del.getFileSn());
            fileMngService.deleteFileInf(delFile);
          }
        }

      } else {
        result = false;
        msg = ResponseMessage.UNAUTHORIZED;
      }

    }

    BasicResponse res =
        BasicResponse.builder().result(result).data(req.getRawSeq()).message(msg).build();

    return res;
  }

  @ApiOperation(value = "로데이터 수정 히스토리")
  @GetMapping(value = "/hisList.do")
  public BasicResponse hisList(@ApiParam(value = "로데이터 고유번호", required = true,
      example = "10") @RequestParam(value = "rawSeq") String rawSeq) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    List<HisDTO> list = new ArrayList<HisDTO>();

    list = rawService.hisList(rawSeq);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(list).build();

    return res;
  }

  @ApiOperation(value = "로데이터 > 첨부파일 로데이터 등록", notes = "0. testSeq값 필수\n" + "1. fileType : 공통코드 RF")
  @PostMapping(value = "/file/insert.do")
  public BasicResponse insertFile(@RequestPart(value = "fileRaw") FileRaw req,
      @RequestPart(value = "files", required = false) final List<MultipartFile> files)
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

    Set<ConstraintViolation<FileRaw>> violations = validator.validate(req);

    for (ConstraintViolation<FileRaw> violation : violations) {
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

      // 파일첨부
      if (!ObjectUtils.isEmpty(files)) {

        // 현재 날짜 구하기
        LocalDate now = LocalDate.now();
        // 포맷 정의
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM");
        // 포맷 적용
        String formatedNow = now.format(formatter);

        // 신규등록
        FileResult = fileUtil.parseFile(files, "", 0, "",
            propertyService.getString("Globals.fileStorePath").concat(formatedNow).concat("/").concat("FILE").concat("/")
                .concat(Integer.toString(req.getTestSeq())));
        atchFileId = fileMngService.insertFileInfs(FileResult);
        req.setAtchFileId(atchFileId);

      }

      result = rawService.insertFile(req);

    } else {
      result = false;
      msg = ResponseMessage.UNAUTHORIZED;
    }


    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }

  @ApiOperation(value = "파일로데이터 상세보기",
      notes = "0. 파일다운링크 : /file/fileDown.do?atchFileId=FILE_FILE_000000000001782&fileSn=1\n"
          + "1. 2	작성자\n15 작성일\n32 첨부파일용도\n33 제목\n34 파일명")
  @GetMapping(value = "/file/{testSeq}/detail.do")
  public BasicResponse fileDetail(
      @ApiParam(value = "시험 고유번호", required = true,
          example = "22") @PathVariable(name = "testSeq") int testSeq,
      @ModelAttribute ComParam param) throws Exception {
    boolean result = true;
    String msg = "";
    List<FileRawDTO> list = new ArrayList<FileRawDTO>();

    // 페이징
    param.setPageUnit(propertyService.getInt("pageUnit"));
    param.setPageSize(propertyService.getInt("pageSize"));

    PagingVO pagingVO = new PagingVO();

    pagingVO.setCurrentPageNo(param.getPageIndex());
    pagingVO.setDisplayRow(param.getPageUnit());
    pagingVO.setDisplayPage(param.getPageSize());

    param.setFirstIndex(pagingVO.getFirstRecordIndex());
    int cnt = rawService.fileRawListCnt(testSeq, param);

    pagingVO.setTotalCount(cnt);
    param.setTotalCount(cnt);
    pagingVO.setTotalPage(
        (int) Math.ceil(pagingVO.getTotalCount() / (double) pagingVO.getDisplayRow()));
    list = rawService.fileRawList(testSeq, param);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res =
        BasicResponse.builder().result(result).message(msg).data(list).paging(pagingVO).build();

    return res;
  }

  @ApiOperation(value = "로데이터 > 첨부파일 로데이터 파일 삭제")
  @PostMapping(value = "/file/delete.do")
  public BasicResponse deleteFile(@RequestBody FileVO req) throws Exception {
    boolean result = true;
    String msg = "";

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      // 파일삭제
      FileVO delFile = null;
      delFile = new FileVO();
      delFile.setAtchFileId(req.getAtchFileId());
      delFile.setFileSn(req.getFileSn());
      fileMngService.deleteFileInf(req);
    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).build();

    return res;
  }

}
