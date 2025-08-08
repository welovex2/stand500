package egovframework.cmm.service;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import egovframework.cnf.service.Cmpy;
import egovframework.cnf.service.CmpyMng;
import egovframework.cnf.service.Member;
import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("CmmMapper")
public interface CmmMapper {
  public List<Cmpy> cnsltList();

  public List<Cmpy> drctCstmrList();

  public Cmpy cmpyDetail(int cmpySeq);

  public List<CmpyMng> cmpyMngList(@Param("cmpySeq") int cmpySeq, @Param("prtnYn") int prtnYn, @Param("prtnSeq") int prtnSeq) throws Exception;

  public List<Comcode> comcodeList(String code);

  public List<Comcode> comcodeSearchList(String code);

  public List<Dept> deptList();

  public List<Member> revMemList(int deptSeq);

  public List<Member> deptMemList(int deptSeq);

  public boolean jobStateUpdate(Job req);

  public boolean updateJobMng(JobMngr req);
  
  public boolean insertJobMng(JobMngr req);

  public List<Job> jobMngList(int jobSeq);

  public List<Member> memList();

  public List<Member> mngList();

  public List<Cmpy> drctList(int cmpySeq);
}
