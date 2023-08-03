package egovframework.rsb.service;

import egovframework.rsb.RsbDTO.Req;

public interface RsbService {

  boolean insert(Req req);

  int selectMaxRevision(Req req);
  
}
