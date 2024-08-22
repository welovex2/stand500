package egovframework.sts.service;

import java.util.List;
import egovframework.cmm.service.ComParam;
import egovframework.rte.psl.dataaccess.mapper.Mapper;
import egovframework.sts.dto.CmdDTO;

@Mapper("CmdMapper")
public interface CmdMapper {

  public List<CmdDTO> selectList(ComParam param);

  public List<CmdDTO> selectPayList(ComParam param);

}
