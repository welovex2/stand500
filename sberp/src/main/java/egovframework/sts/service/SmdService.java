package egovframework.sts.service;

import java.util.List;
import egovframework.cmm.service.ComParam;
import egovframework.sts.dto.SmdDTO;
import egovframework.sts.dto.Target;

public interface SmdService {

  List<SmdDTO> selectSaleList(ComParam param);

  boolean insert(List<Target> list);

}
