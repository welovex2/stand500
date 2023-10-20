package egovframework.sts.service;

import java.util.List;
import egovframework.cmm.service.ComParam;
import egovframework.sts.dto.StsDTO;

public interface StsService {

  StsDTO selectDetail(ComParam param);
 
  List<StsDTO> selectList(ComParam param);
}
