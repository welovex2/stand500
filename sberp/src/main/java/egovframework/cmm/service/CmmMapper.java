package egovframework.cmm.service;

import java.util.List;

import egovframework.cnf.service.Cmpy;
import egovframework.cnf.service.CmpyMng;
import egovframework.cnf.service.Member;
import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("CmmMapper")
public interface CmmMapper {
	public List<Cmpy> cnsltList();
	public List<Cmpy> drctCstmrList();
	public Cmpy cmpyDetail(int cmpySeq);
	public List<CmpyMng> cmpyMngList(int cmpySeq) throws Exception;
	public List<Comcode> comcodeList(String code);
	public List<Dept> deptList();
	public List<Member> revMemList(int deptSeq);
	public List<Member> deptMemList(int deptSeq);
}
