package egovframework.cmm.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import egovframework.cmm.service.CmmService;
import egovframework.cmm.service.Comcode;
import egovframework.cmm.service.Dept;
import egovframework.cnf.service.Cmpy;
import egovframework.cnf.service.CmpyMng;
import egovframework.cnf.service.Member;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = {"공통정보(selectBox)"})
@RestController
@RequestMapping("/cmm")
public class CmmController {

	@Resource(name = "CmmService")
	private CmmService cmmService;
	
    @ApiOperation(value = "컨설팅 이름 리스트")
    @GetMapping(value="/cnslt/list.do")
    public List<Cmpy> cnsltList() throws Exception{
    	List<Cmpy> list = new ArrayList<Cmpy>();
    	
    	list = cmmService.cnsltList();
    	
        return list;
    }
    
    @ApiOperation(value = "직접고객 이름 리스트")
    @GetMapping(value="/drct-cstmr/list.do")
    public List<Cmpy> drctCstmrList() throws Exception{
    	List<Cmpy> list = new ArrayList<Cmpy>();
    	
    	list = cmmService.drctCstmrList();
    	
        return list;
    }
    
    @ApiOperation(value = "회사상세정보")
    @ApiResponses(value = {
    	    @ApiResponse(code = 200, message = "조회성공", response = Map.class, responseContainer = "Map"),
    	    })
    @GetMapping(value="/cmpy/detail.do")
    public Map<String, Object> cmpyDetail(@ApiParam(value = "회사 고유값", required = true, example = "1") @RequestParam(value="cmpySeq") int cmpySeq) throws Exception{
    	Cmpy cmpy = new Cmpy();
    	List<CmpyMng> cmpyMng = new ArrayList<CmpyMng>();
    	
    	Map<String, Object> map = new HashMap<>();
    	
    	cmpy = cmmService.cmpyDetail(cmpySeq);
    	cmpyMng = cmmService.cmpyMngList(cmpySeq);
    	
    	map.put("cmpy", cmpy);
    	map.put("cmpyMng", cmpyMng);
 
        return map;
    }
    
    @ApiOperation(value = "공통코드 리스트")
    @GetMapping(value="/comcode/list.do")
    public List<Comcode> comcodeList(@ApiParam(value = "코드분류값", required = true, example = "00") @RequestParam(value="code") String code) throws Exception{
    	List<Comcode> list = new ArrayList<Comcode>();
    	
    	list = cmmService.comcodeList(code);
    	
        return list;
    }
    
    @ApiOperation(value = "시험부서 리스트")
    @GetMapping(value="/test/deptList.do")
    public List<Dept> deptList() throws Exception{
    	List<Dept> list = new ArrayList<Dept>();
    	
    	list = cmmService.deptList();
    	
        return list;
    }
    
    @ApiOperation(value = "기술책임자 리스트")
    @GetMapping(value="/test/revMemList.do")
    public List<Member> revMemList(@ApiParam(value = "부서고유번호", required = true, example = "1") @RequestParam(value="deptSeq") int deptSeq) throws Exception{
    	List<Member> list = new ArrayList<Member>();
    	
    	list = cmmService.revMemList(deptSeq);
    	
        return list;
    }
    
    @ApiOperation(value = "부서원 리스트")
    @GetMapping(value="/test/deptMemList.do")
    public List<Member> deptMemList(@ApiParam(value = "부서고유번호", required = true, example = "1") @RequestParam(value="deptSeq") int deptSeq) throws Exception{
    	List<Member> list = new ArrayList<Member>();
    	
    	list = cmmService.deptMemList(deptSeq);
    	
        return list;
    }
    
}
