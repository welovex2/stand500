package egovframework.sts.service;

import java.util.List;
import egovframework.cmm.service.ComParam;
import egovframework.rte.psl.dataaccess.mapper.Mapper;
import egovframework.sts.dto.TmdDTO;
import egovframework.sts.dto.TmdDTO.TestAction;
import egovframework.sts.dto.TmdDTO.TestResultList;

@Mapper("TmdMapper")
public interface TmdMapper {


  public List<TmdDTO> selectMemList(ComParam param);
  
  public List<TmdDTO> selectMonList(ComParam param);

  public List<TestResultList> selectResultList(ComParam param);

  public List<TestAction> selectActionList(ComParam param);

  public int selectActionListCnt(ComParam param);

}
