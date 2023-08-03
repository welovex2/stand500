package egovframework.leg.service;

import java.util.List;
import egovframework.cmm.service.ComParam;
import egovframework.leg.dto.LedgerDTO;
import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("LegMapper")
public interface LegMapper {

  public int selectListCnt(ComParam param);

  public List<LedgerDTO> selectList(ComParam param);
  
}
