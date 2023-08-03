package egovframework.sam.service;

import java.util.List;
import egovframework.cmm.service.ComParam;
import egovframework.sam.dto.ImDTO;
import egovframework.sam.dto.ImSubDTO;

public interface SamService {

  boolean insert(ImDTO req);

  boolean update(ImDTO req);

  ImDTO detail(String sbkId);

  int selectListCnt(ComParam param);

  List<ImSubDTO> selectList(ComParam param);
  
}
