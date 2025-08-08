package egovframework.cmm.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import egovframework.cmm.service.CmmMapper;
import egovframework.cmm.service.CmmService;
import egovframework.cmm.service.Comcode;
import egovframework.cmm.service.Dept;
import egovframework.cmm.service.Job;
import egovframework.cmm.service.JobMngr;
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
  public List<CmpyMng> cmpyMngList(int cmpySeq, int prtnYn, int prtnSeq) throws Exception {

    List<CmpyMng> list = cmmMapper.cmpyMngList(cmpySeq, prtnYn, prtnSeq);

    return list;
  }

  @Override
  public List<Comcode> comcodeSearchList(String code) throws Exception {
    List<Comcode> list = cmmMapper.comcodeSearchList(code);

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

  @Override
  public boolean jobStateUpdate(Job req) {
    return cmmMapper.jobStateUpdate(req);
  }

  @Override
  @Transactional
  public boolean insertJobMng(JobMngr req) {
    cmmMapper.updateJobMng(req);
    return cmmMapper.insertJobMng(req);
  }

  @Override
  public List<Job> jobMngList(int jobSeq) {
    return cmmMapper.jobMngList(jobSeq);
  }

  @Override
  public List<Member> memList() {
    return cmmMapper.memList();
  }

  @Override
  public List<Member> mngList() {
    return cmmMapper.mngList();
  }

  @Override
  public List<Cmpy> drctList(int cmpySeq) {
    return cmmMapper.drctList(cmpySeq);
  }

}
