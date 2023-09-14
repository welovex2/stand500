package egovframework.ppc.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import egovframework.cmm.service.BasicResponse;
import egovframework.cmm.service.ComParam;
import egovframework.cmm.service.LoginVO;
import egovframework.cmm.service.PagingVO;
import egovframework.cmm.service.ResponseMessage;
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.cnf.service.CmpyDTO;
import egovframework.cnf.service.CmpyMng;
import egovframework.cnf.service.CmyService;
import egovframework.ppc.dto.PpDTO;
import egovframework.ppc.service.PpcService;
import egovframework.rte.fdl.property.EgovPropertyService;
import egovframework.rte.psl.dataaccess.util.EgovMap;
import egovframework.sbk.service.SbkDTO;
import egovframework.sbk.service.SbkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags = {"사전통관"})
@RestController
@RequestMapping("/ppc")
public class PpcController {

  @Resource(name = "propertiesService")
  protected EgovPropertyService propertyService;

  @Resource(name = "PpcService")
  private PpcService ppcService;

  @Resource(name = "SbkService")
  private SbkService sbkService;
  
  @Resource(name = "CmyService")
  private CmyService cmyService;
  
  @ApiOperation(value = "사전통관 리스트", notes = "검색박스는 공통코드 CS, 필요한항목만 노출시켜서 사용\n1-고객유형(PT), 5-접수번호, "
      + "4-컨설팅/직고객명, 12-회사명, 6-제품명, 27-모델명, 7-고지부담당자, 15-작성일")
  @GetMapping(value = "/list.do")
  public BasicResponse ppList(@ModelAttribute ComParam param) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";

    List<PpDTO> list = new ArrayList<PpDTO>();

    // 페이징
    param.setPageUnit(param.getPageUnit());
    param.setPageSize(propertyService.getInt("pageSize"));

    PagingVO pagingVO = new PagingVO();

    pagingVO.setCurrentPageNo(param.getPageIndex());
    pagingVO.setDisplayRow(param.getPageUnit());
    pagingVO.setDisplayPage(param.getPageSize());

    param.setFirstIndex(pagingVO.getFirstRecordIndex());
    int cnt = ppcService.selectListCnt(param);

    pagingVO.setTotalCount(cnt);
    pagingVO.setTotalPage(
        (int) Math.ceil(pagingVO.getTotalCount() / (double) pagingVO.getDisplayRow()));
    list = ppcService.selectList(param);

    if (list == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }

    BasicResponse res =
        BasicResponse.builder().result(result).message(msg).data(list).paging(pagingVO).build();
    return res;
  }

  @ApiOperation(value = "사전통관 저장")
  @PostMapping(value = "insert.do")
  public BasicResponse ppAdd(@ApiParam(value = "수정시 사전통관고유번호(ppId) 필수", required = true,
      example = "") @RequestBody PpDTO pp) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String msg = "";

    // 로그인정보
    pp.setInsMemId(user.getId());
    pp.setUdtMemId(user.getId());

    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    Validator validator = validatorFactory.getValidator();

    Set<ConstraintViolation<PpDTO>> violations = validator.validate(pp);

    for (ConstraintViolation<PpDTO> violation : violations) {
      msg = violation.getMessage();

      System.out.println(msg);
      BasicResponse res = BasicResponse.builder().result(false).message(msg).build();

      return res;
    }

    boolean result = false;

    // 사전통관 번호가 있으면, 회사정보 update, 상담내역 insert
    if (!StringUtils.isEmpty(pp.getPpId())) {
      result = ppcService.update(pp);
    }
    // 최초 등록이면, 회사정보 insert, 상담내역 insert
    else {
      result = ppcService.insert(pp);
    }

    BasicResponse res = BasicResponse.builder().result(result).build();

    return res;
  }

  @ApiOperation(value = "사전통관 상세보기")
  @GetMapping(value = "/{ppId}/detail.do")
  public BasicResponse ppDetail(@ApiParam(value = "사전통관 고유번호", required = true,
      example = "6") @PathVariable(name = "ppId") String ppId) throws Exception {

    boolean result = true;
    String msg = "";
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    PpDTO detail = new PpDTO();

    detail = ppcService.selectDetail(ppId);
    if (detail == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    }
    BasicResponse res = BasicResponse.builder().result(result).data(detail).build();

    return res;
  }
  
  @ApiOperation(value = "프로젝트상태 변경", notes="ppId:사전통관 고유번호, 상태:공통코드(CK)")
  @PostMapping(value="/stateInsert.do")
  public BasicResponse testStateInsert(@RequestBody PpDTO req) throws Exception{
      LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();
      
      // 로그인정보
      req.setInsMemId(user.getId());
      req.setUdtMemId(user.getId());
      
      boolean result = false;
      String msg = "";
      
      Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
      
      if (isAuthenticated) {
          result = ppcService.stateUpdate(req);
      } else {
          result = false;
          msg = ResponseMessage.UNAUTHORIZED;
      }
      
      BasicResponse res = BasicResponse.builder().result(result)
              .message(msg)
              .build();
      
      return res;
  }
  
  @ApiOperation(value = "본건 신청서 작성")
  @PostMapping(value = "/makeSbk.do")
  public BasicResponse makePpSbk(@ApiParam(value = "ppId 값만 전송") @RequestBody String ppId)
      throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    SbkDTO.Req req = new SbkDTO.Req();
    SbkDTO.Res detail = new SbkDTO.Res();
    PpDTO pp = new PpDTO();

    // 로그인정보
    pp.setInsMemId(user.getId());
    pp.setUdtMemId(user.getId());

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {
      // 이미 연결된 신청서가 있는지 확인
      pp = ppcService.selectDetail(ppId);
      if (pp != null && !StringUtils.isEmpty(pp.getSbkId())) {
        result = false;
        msg = ResponseMessage.DUPLICATE_SBK;
      } else {

        /**
         * 사전통관에 등록된 정보를 토대로 신청서를 새로 작성한다.
         */
        // 1
        req.setCmpySeq(pp.getCmpySeq());
        req.setCmpyMngSeq(pp.getCmpyMngSeq());
        // 2
        req.setCmpyName(pp.getCmpnyName());
        // 3
        req.setRprsn(pp.getRprsn());
        // 4
        req.setMngName(pp.getContact());
        // 5
        req.setMngPhone(pp.getContactPhone());
        // 6
        req.setMngEmail(pp.getContactEmail());
        // 7
        req.setPrdctName(pp.getPrdctName());
        // 8
        req.setModelName(pp.getModelName());
        // 9
        req.setMnfctCmpny(pp.getMnfctCmpny());
        
        // 직고객일 경우 신청인 정보 다시 셋팅
        if (pp.getCmpySeq() >= 1000) {
          CmpyDTO cmpyDetail = new CmpyDTO();
          cmpyDetail = cmyService.detail(pp.getCmpySeq());
          // 10 사업자등록번호 cmpy 테이블에서..
          req.setBsnsRgnmb(cmpyDetail.getBsnsRgnmb());
          // 11 법인등록번호 cmpy 테이블에서..
          req.setCrprtRgnmb(cmpyDetail.getCrprtRgnmb());
          // 12 회사명 cmpy 테이블에서..
          req.setCmpyName(cmpyDetail.getCmpyName());
          // 대표자
          req.setRprsn(cmpyDetail.getRprsn());
          
          for (CmpyMng mng : cmpyDetail.getMngList()) {
            if (req.getCmpyMngSeq() == mng.getCmpyMngSeq()) {
              // 담당자이름
              req.setMngName(mng.getName());
              // 담당자핸드폰
              req.setMngPhone(mng.getPhone());
              // 담당자이메일
              req.setMngEmail(mng.getEmail());
            }
          }
        }
        
        // 13
        req.setPpBl(pp.getBl());
        // 14
        req.setPpCnt(Integer.toString(pp.getImQty()));
        // 15
        req.setPpYn(1);
        req.setPpName("스탠다드뱅크");
        req.setPpNum(ppId);
        
        // 신청서 생성
        result = sbkService.insert(req);

        // 신청서 정보 보내주기
        detail = sbkService.selectDetail(req);

        if (detail == null) {
          result = false;
          msg = ResponseMessage.NO_DATA;
        }

        // 사전통관에 연결된 신청서 정보 저장
        pp.setPpId(ppId);
        pp.setSbkId(detail.getSbkId());
        ppcService.sbkIdUpdate(pp);
        

      }

    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();

    return res;

  }
}
