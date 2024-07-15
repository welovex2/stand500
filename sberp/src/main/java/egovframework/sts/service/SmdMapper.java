package egovframework.sts.service;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import egovframework.cmm.service.ComParam;
import egovframework.rte.psl.dataaccess.mapper.Mapper;
import egovframework.sts.dto.SmdDTO;
import egovframework.sts.dto.Target;

@Mapper("SmdMapper")
public interface SmdMapper {

  public List<SmdDTO.memState> selectSaleList(ComParam param);

  public boolean insert(@Param("req") List<Target> req);
  
}
