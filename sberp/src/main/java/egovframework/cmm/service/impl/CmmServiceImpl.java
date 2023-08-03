package egovframework.cmm.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import egovframework.cmm.service.CmmMapper;
import egovframework.cmm.service.CmmService;
import egovframework.cmm.service.Comcode;
import egovframework.cmm.service.Dept;
import egovframework.cnf.service.Cmpy;
import egovframework.cnf.service.CmpyMng;
import egovframework.cnf.service.Member;
import egovframework.rte.fdl.cmmn.EgovAbstractServiceImpl;

@Service("CmmService")
public class CmmServiceImpl extends EgovAbstractServiceImpl implements CmmService {

	@Autowired
	CmmMapper cmmMapper;
	
	@Override
	public List<Cmpy> cnsltList() throws Exception {
		List<Cmpy> list = cmmMapper.cnsltList();
		
		if (list == null)
            throw processException("info.nodata.msg");
    	
		return list;
	}

	@Override
	public List<Cmpy> drctCstmrList() throws Exception {
		List<Cmpy> list = cmmMapper.drctCstmrList();
		
		if (list == null)
            throw processException("info.nodata.msg");
    	
		return list;
	}

	@Override
	public Cmpy cmpyDetail(int cmpySeq) throws Exception {
		
		Cmpy detail = cmmMapper.cmpyDetail(cmpySeq);
		
		if (detail == null)
            throw processException("info.nodata.msg");
		
		return detail;
	}

	@Override
	public List<CmpyMng> cmpyMngList(int cmpySeq) throws Exception {
		
		List<CmpyMng> list = cmmMapper.cmpyMngList(cmpySeq);
		
		return list;
	}

	@Override
	public List<Comcode> comcodeList(String code) throws Exception {
		List<Comcode> list = cmmMapper.comcodeList(code);
		
		return list;
	}

	@Override
	public List<Dept> deptList() {
		List<Dept> list = cmmMapper.deptList();
		
		return list;
	}

	@Override
	public List<Member> revMemList(int deptSeq) {
		List<Member> list = cmmMapper.revMemList(deptSeq);
		
		return list;
	}

	@Override
	public List<Member> deptMemList(int deptSeq) {
		List<Member> list = cmmMapper.deptMemList(deptSeq);
		
		return list;
	}

}
