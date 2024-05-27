package egovframework.sbk.service;

import java.util.List;
import egovframework.cmm.service.ComParam;
import egovframework.cmm.service.HisDTO;
import egovframework.sbk.service.SbkDTO.Req;
import egovframework.sbk.service.SbkDTO.Res;
import egovframework.tst.dto.TestItemDTO;
import egovframework.tst.service.TestItemRej;

public interface SbkService {

  SbkDTO.Res selectDetail(Req req);

  boolean insert(Req req) throws Exception;

  boolean update(Req sbk);

  int selectListCnt(ComParam param);

  List<Res> selectList(ComParam param) throws Exception;

  boolean updateTestItemSign(TestItemDTO req);

  List<TestItemRej> signRejectList(String testItemSeq);

  boolean signRejectInsert(TestItemRej req);

  List<HisDTO> hisList(String sbkId);

  Res selectDirtInfo(Req req);

}
