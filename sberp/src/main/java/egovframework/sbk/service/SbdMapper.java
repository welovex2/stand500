package egovframework.sbk.service;

import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("SbdMapper")
public interface SbdMapper {

  Sbd selectDriDetail(String sbkId);

  Sbd selectBhDetail(String sbkId);

  Sbd selectJhDetail(String sbkId);
  
  int insertBh(Sbd sbd);
  
  int insertJh(Sbd sbd);
}
