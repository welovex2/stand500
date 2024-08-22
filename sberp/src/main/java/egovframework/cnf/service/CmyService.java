package egovframework.cnf.service;

import java.util.List;
import egovframework.cmm.service.ComParam;
import egovframework.sts.dto.CmdDTO;

public interface CmyService {

  List<CmpyDTO> selectList(ComParam param);

  int selectListCnt(ComParam param);

  boolean insert(CmpyDTO req);
  
  boolean delete(CmpyDTO req);

  CmpyDTO detail(int cmpySeq);

  List<CmpyDTO> selectSameName(String cmpyCode, String cmpyName);

  List<CmdDTO.Sub> selectCmdList(ComParam param);
}
