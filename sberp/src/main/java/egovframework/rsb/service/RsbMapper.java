package egovframework.rsb.service;

import egovframework.rsb.RsbDTO.Req;
import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("RsbMapper")
public interface RsbMapper {

  void copySbk(Req req);

  void copyQuo(Req req);

  void copyJob(Req req);

  void copyTestItem(Req req);

  int selectMaxRevision(Req req);

}
