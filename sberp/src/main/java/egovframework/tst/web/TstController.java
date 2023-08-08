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
import egovframework.sbk.service.SbkDTO;
import egovframework.sys.service.TestStndr;
import egovframework.tst.dto.TestDTO;
import egovframework.tst.service.TestCate;
import egovframework.tst.service.TstParam;
import egovframework.tst.service.TstService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Api(tags = {"시험"})
@RestController
@RequestMapping("/tst")
public class TstController {
	
	@Resource(name = "TstService")
	private TstService tstService;
	
	@Resource(name = "propertiesService")
	protected EgovPropertyService propertyService;
	 
	@ApiOperation(value = "시험항목 > 인증종류리스트", notes = "시험항목 > 시험종류리스트는 공통코드 리스트로 조회 = /cmm/comcode/list.do?code=TT")
	@GetMapping(value="/crtfcList.do")
	public List<TestCate> crtfcTypeList(@ApiParam(value = "상위인증코드(없으면 값 넣지 않음)", example = "1") @RequestParam int topCode) throws Exception{
    	List<TestCate> result = new ArrayList<TestCate>();
    	
    	result = tstService.selectCrtfList(topCode);
    	
    	return result;
    }
	@ApiOperation(value = "시험항목 시험규격리스트")
	@GetMapping(value="/stndrList.do")
	public List<TestStndr> stndrList(TstParam param) throws Exception{
    	List<TestStndr> result = new ArrayList<TestStndr>();
    	
    	result = tstService.selectStndrList(param);
    	
    	return result;   	
    }
	
    @ApiOperation(value = "시험 신청하기")
    @PostMapping(value="/makeTest.do")
    public BasicResponse makeTest(@ApiParam(value = "testItemSeq, testTypeCode 값 전송") @RequestBody TestDTO.Req req) throws Exception{
    	
    	LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();
    	boolean result = true;
    	String msg = "";
    	
    	// 로그인정보
    	req.setInsMemId(user.getId());
    	req.setUdtMemId(user.getId());
    	
		System.out.println("=-===========");
		System.out.println(req.toString());
		System.out.println("=-===========");
		
    	Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    	
    	if (isAuthenticated) {
	    	// 이미 연결된 시험이 있는지 확인
	    	if (tstService.selectDetail(req) != null) {
	    		result = false;
    			msg = ResponseMessage.DUPLICATE_TEST;
	    	} else {
	    		// 시험 생성
	    		result = tstService.insert(req);
	    	}
    		
    	}
    	
    	BasicResponse res = BasicResponse.builder().result(result)
    			.message(msg)
				.build();
    	
        return res;   
        
    }
    
    
    @ApiOperation(value = "시험리스트", notes = "결과값은 TestDTO.Res 참고"
    										  + "2.검색박스는 공통코드 CS, 필요한항목만 노출시켜서 사용\n"
    										  + " 신청구분:신규-1,기술기준변경-2,동일기자재-3,기술기준외변경-4\n"
    										  + " 시험부(TT), 미배정-9999\n"
    										  + " 시험상태(TS)")
    @GetMapping(value="/list.do")
    public BasicResponse testList(@ModelAttribute ComParam param) throws Exception{
    	
    	LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();
    	boolean result = true;
    	String msg = "";
    	List<TestDTO.Res> list = new ArrayList<TestDTO.Res>();

    	//페이징
    	param.setPageUnit(param.getPageUnit() == 0 ? propertyService.getInt("pageUnit") : param.getPageUnit());
    	param.setPageSize(propertyService.getInt("pageSize"));
		
    	PagingVO pagingVO = new PagingVO();
		
    	pagingVO.setCurrentPageNo(param.getPageIndex());
    	pagingVO.setDisplayRow(param.getPageUnit());
    	pagingVO.setDisplayPage(param.getPageSize());
		
    	param.setFirstIndex(pagingVO.getFirstRecordIndex());
    	int cnt = tstService.selectListCnt(param);
    	
    	pagingVO.setTotalCount(cnt);
		pagingVO.setTotalPage((int) Math.ceil(pagingVO.getTotalCount() / (double) pagingVO.getDisplayRow()));
    	list = tstService.selectList(param);

    	if (list == null) {
    		result = false;
    		msg = ResponseMessage.NO_DATA;
    	}
    	
    	BasicResponse res = BasicResponse.builder().result(result)
    			.message(msg)
				.data(list)
				.paging(pagingVO)
				.build();
    	
        return res;
    }
    
    @ApiOperation(value = "시험담당자 저장", notes="testSeq:시험고유항목, testMngId:시험담당자ID, memo:메모")
    @PostMapping(value="/testMemInsert.do")
    public BasicResponse testMemInsert(@RequestBody TestDTO.Req req) throws Exception{
    	LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();
    	
    	// 로그인정보
    	req.setInsMemId(user.getId());
    	req.setUdtMemId(user.getId());
    	
    	boolean result = false;
    	String msg = "";
    	
    	Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    	
    	if (isAuthenticated) {
   			result = tstService.testMemInsert(req);
    	} else {
    		result = false;
    		msg = ResponseMessage.UNAUTHORIZED;
    	}
    	
    	BasicResponse res = BasicResponse.builder().result(result)
    			.message(msg)
				.build();
    	
    	return res;
    }
    
    @ApiOperation(value = "시험담당자 변경 내역")
    @GetMapping(value="/testMemList.do")
    public BasicResponse testMemList(@ApiParam(value = "시험 고유번호", required = true, example = "1") @RequestParam(value="testSeq") String testSeq) throws Exception{
    	
    	LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();
    	boolean result = true;
    	String msg = "";
    	List<TestDTO.Res> list = new ArrayList<TestDTO.Res>();
		
    	list = tstService.testMemList(testSeq);

    	if (list == null) {
    		result = false;
    		msg = ResponseMessage.NO_DATA;
    	}
    	
    	BasicResponse res = BasicResponse.builder().result(result)
    			.message(msg)
    			.data(list)
				.build();
    	
        return res;  
    }
    
    @ApiOperation(value = "시험상태 변경", notes="testSeq:시험고유항목, 시험상태:공통코드(TS), memo:메모")
    @PostMapping(value="/testStateInsert.do")
    public BasicResponse testStateInsert(@RequestBody TestDTO.Req req) throws Exception{
    	LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();
    	
    	// 로그인정보
    	req.setInsMemId(user.getId());
    	req.setUdtMemId(user.getId());
    	
    	boolean result = false;
    	String msg = "";
    	
    	Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    	
    	if (isAuthenticated) {
   			result = tstService.testStateInsert(req);
    	} else {
    		result = false;
    		msg = ResponseMessage.UNAUTHORIZED;
    	}
    	
    	BasicResponse res = BasicResponse.builder().result(result)
    			.message(msg)
				.build();
    	
    	return res;
    }
    
    @ApiOperation(value = "시험상태 변경 내역")
    @GetMapping(value="/testStateList.do")
    public BasicResponse testStateList(@ApiParam(value = "시험 고유번호", required = true, example = "1") @RequestParam(value="testSeq") String testSeq) throws Exception{
    	
    	LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();
    	boolean result = true;
    	String msg = "";
    	List<TestDTO.Res> list = new ArrayList<TestDTO.Res>();
		
    	list = tstService.testStateList(testSeq);

    	if (list == null) {
    		result = false;
    		msg = ResponseMessage.NO_DATA;
    	}
    	
    	BasicResponse res = BasicResponse.builder().result(result)
    			.message(msg)
    			.data(list)
				.build();
    	
        return res;  
    }
 
    @ApiOperation(value = "시험게시판 등록", notes="testSeq:시험고유항목, memo:메모")
    @PostMapping(value="/testBoardInsert.do")
    public BasicResponse testBoardInsert(@RequestBody TestDTO.Req req) throws Exception{
    	LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();
    	
    	// 로그인정보
    	req.setInsMemId(user.getId());
    	req.setUdtMemId(user.getId());
    	
    	boolean result = false;
    	String msg = "";
    	
    	Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
    	
    	if (isAuthenticated) {
   			result = tstService.testBoardInsert(req);
    	} else {
    		result = false;
    		msg = ResponseMessage.UNAUTHORIZED;
    	}
    	
    	BasicResponse res = BasicResponse.builder().result(result)
    			.message(msg)
				.build();
    	
    	return res;
    }
    
    @ApiOperation(value = "시험게시판 내역")
    @GetMapping(value="/testBoardList.do")
    public BasicResponse testBoardList(@ApiParam(value = "시험 고유번호", required = true, example = "1") @RequestParam(value="testSeq") String testSeq) throws Exception{
    	
    	LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();
    	boolean result = true;
    	String msg = "";
    	List<TestDTO.Res> list = new ArrayList<TestDTO.Res>();
		
    	list = tstService.testBoardList(testSeq);

    	if (list == null) {
    		result = false;
    		msg = ResponseMessage.NO_DATA;
    	}
    	
    	BasicResponse res = BasicResponse.builder().result(result)
    			.message(msg)
    			.data(list)
				.build();
    	
        return res;  
    }

    @ApiOperation(value = "시험게시판 신청기기 내역")
    @GetMapping(value="/testBoardAppDetail.do")
    public BasicResponse testBoardAppDetail(@ApiParam(value = "신청서 고유번호", required = true, example = "SB23-G0052") @RequestParam(value="sbkId") String sbkId) throws Exception{
    	
    	LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();
    	boolean result = true;
    	String msg = "";
    	SbkDTO.Res detail = new SbkDTO.Res();
		
    	detail = tstService.testBoardAppDetail(sbkId);

    	if (detail == null) {
    		result = false;
    		msg = ResponseMessage.NO_DATA;
    	}
    	
    	BasicResponse res = BasicResponse.builder().result(result)
    			.message(msg)
    			.data(detail)
				.build();
    	
        return res;  
    }
    
    @ApiOperation(value = "성적서발급일 추가")
    @PostMapping(value="/insertReportDt.do")
    public BasicResponse insertReportDt(@ApiParam(value = "testSeq:시험고유항목, reportDt:성적서발급일 값 전송") @RequestBody TestDTO.Req req) throws Exception{
        
        LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();
        boolean result = true;
        String msg = "";
        
        // 로그인정보
        req.setInsMemId(user.getId());
        req.setUdtMemId(user.getId());
        
        System.out.println("=-===========");
        System.out.println(req.toString());
        System.out.println("=-===========");
        
        Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();
        
        if (isAuthenticated) {
            // 이미 연결된 시험이 있는지 확인
            if (tstService.selectDetail(req) != null) {
                result = false;
                msg = ResponseMessage.DUPLICATE_TEST;
            } else {
              
                try {
                  
                  // 성적서 발급일 추가
                  result = tstService.update(req);
                  
                } catch (Exception e) {
                  
                  msg = ResponseMessage.RETRY;
                  log.warn(user.getId() + " :: " + e.toString());
                  log.warn(req.toString());
                  log.warn("");
                  
                }  
            }
            
        }
        
        BasicResponse res = BasicResponse.builder().result(result)
                .message(msg)
                .build();
        
        return res;   
        
    }
    
}
