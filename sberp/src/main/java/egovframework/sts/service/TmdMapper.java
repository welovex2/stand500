package egovframework.sts.service;

import java.util.List;
import egovframework.cmm.service.ComParam;
import egovframework.rte.psl.dataaccess.mapper.Mapper;
import egovframework.sts.dto.TmdDTO;

@Mapper("TmdMapper")
public interface TmdMapper {


  public List<TmdDTO> selectMemList(ComParam param);
  
  public List<TmdDTO> selectMonList(ComParam param);
  
}
