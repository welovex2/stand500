package egovframework.cmm.service;

import java.util.List;

import egovframework.cnf.service.Cmpy;
import egovframework.cnf.service.CmpyMng;
import egovframework.cnf.service.Member;

public interface CmmService {

	public List<Cmpy> cnsltList() throws Exception;
	public List<Cmpy> drctCstmrList() throws Exception;
	public Cmpy cmpyDetail(int cmpySeq) throws Exception;
	public List<CmpyMng> cmpyMngList(int cmpySeq) throws Exception;
	public List<Comcode> comcodeList(String code) throws Exception;
	public List<Dept> deptList();
	public List<Member> revMemList(int deptSeq);
	public List<Member> deptMemList(int deptSeq);
}
