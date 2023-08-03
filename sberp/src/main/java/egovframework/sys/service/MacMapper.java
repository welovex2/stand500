package egovframework.sys.service;

import java.util.List;
import egovframework.cmm.service.ComParam;
import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("MacMapper")
public interface MacMapper {

  List<MachineDTO> selectList(ComParam param);

  void insert(MachineDTO req);

  void update(MachineDTO req);
}
