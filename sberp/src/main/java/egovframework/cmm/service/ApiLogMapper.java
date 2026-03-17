package egovframework.cmm.service;

import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("ApiLogMapper")
public interface ApiLogMapper {
 void insertApiLog(ApiLogVO logVO);
}
