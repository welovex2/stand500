package egovframework.cnf.service;

import java.util.List;
import egovframework.cmm.service.ComParam;

public interface CmyService {

  List<CmpyDTO> selectList(ComParam param);

  int selectListCnt(ComParam param);

  boolean insert(CmpyDTO req);

  CmpyDTO detail(int cmpySeq);

  List<CmpyDTO> selectSameName(String cmpyCode, String cmpyName);

}
