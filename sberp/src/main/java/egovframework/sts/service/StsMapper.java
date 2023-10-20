package egovframework.sts.service;

import java.util.List;
import egovframework.cmm.service.ComParam;
import egovframework.rte.psl.dataaccess.mapper.Mapper;
import egovframework.sts.dto.StsDTO;

@Mapper("StsMapper")
public interface StsMapper {

  public List<StsDTO.TestTypeList> selectTodayStateList(ComParam param);
  
  public List<StsDTO.TestTypeList> selectTotalStateList(ComParam param);

  public List<StsDTO> selectBillSum(ComParam param);
  
  public List<StsDTO> selectPaySum(ComParam param);
  
  public List<StsDTO.TestTypeList> selectInList(ComParam param);

  public List<StsDTO> selectStateList(ComParam param);
  
}
