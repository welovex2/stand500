package egovframework.sts.service;

import java.util.List;
import egovframework.cmm.service.ComParam;
import egovframework.sts.dto.TmdDTO;

public interface TmdService {

  public List<TmdDTO> selectMemList(ComParam param);
  
  public List<TmdDTO> selectMonList(ComParam param);
  
  public List<TmdDTO.TestResultList> selectResultList(ComParam param);

  public List<TmdDTO.TestAction> selectTestList(ComParam param);

  public int selectTestListCnt(ComParam param);
}
