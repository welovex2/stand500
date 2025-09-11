package egovframework.sbk.service;

public interface SbdService {

  Sbd selectDriDetail(String sbkId);

  Sbd selectBhDetail(String sbkId);
  
  Sbd selectJhDetail(String sbkId);

  int insertBh(Sbd req);
  
  int insertJh(Sbd req);
}
