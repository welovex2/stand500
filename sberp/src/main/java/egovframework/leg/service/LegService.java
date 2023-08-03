package egovframework.leg.service;

import java.util.List;
import egovframework.cmm.service.ComParam;
import egovframework.leg.dto.LedgerDTO;

public interface LegService {

  int selectListCnt(ComParam param);

  List<LedgerDTO> selectList(ComParam param);
  
}
