package egovframework.tst.service;

import java.util.List;
import egovframework.cmm.service.ComParam;
import egovframework.tst.dto.DebugDTO;
import egovframework.tst.dto.TestDTO.Req;
import egovframework.tst.dto.TestDTO.Res;

public interface DbgService {

  boolean update(DebugDTO req);

  boolean insert(DebugDTO req);

  int selectListCnt(ComParam param);

  List<DebugDTO> selectList(ComParam param);

  boolean debugStateInsert(DebugDTO req);

  List<DebugDTO> debugStateList(String debugSeq);

  boolean debugBoardInsert(DebugMemo req);

  List<DebugMemo> debugBoardList(String debugSeq);

}
