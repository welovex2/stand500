package egovframework.tst.service;

import java.util.List;
import egovframework.cmm.service.ComParam;
import egovframework.sbk.service.SbkDTO;
import egovframework.sys.service.TestStndr;
import egovframework.tst.dto.TestDTO.Req;
import egovframework.tst.dto.TestDTO.Res;
import egovframework.tst.dto.TestMngrDTO;

public interface TstService {

  List<TestCate> selectCrtfList(int topCode);

  List<TestStndr> selectStndrList(TstParam param);

  Test selectDetail(Req req);

  boolean insert(Req req);

  int selectListCnt(ComParam param);

  List<Res> selectList(ComParam param);

  boolean testMemInsert(TestMngrDTO req);

  TestMngrDTO testMemList(String testSeq);
  
  boolean testMemSatetUpdate(TestMngrDTO req);

  boolean testStateInsert(Req req);

  List<Res> testStateList(String testSeq);

  boolean testBoardInsert(Req req);

  List<Res> testBoardList(String testSeq);

  SbkDTO.Res testBoardAppDetail(String sbkId);

  boolean update(Req req);

  List<Res> selectSaleList(ComParam param);

  int selectSaleListCnt(ComParam param);
  
  List<Res> selectRevList(ComParam param);

}
