package egovframework.sts.service;

import java.util.List;
import egovframework.cmm.service.ComParam;
import egovframework.sts.dto.CmdDTO;

public interface CmdService {

  List<CmdDTO> selectList(ComParam param);

}
