package egovframework.tst.service;

import java.util.List;
import egovframework.cmm.service.ComParam;
import egovframework.rte.psl.dataaccess.mapper.Mapper;
import egovframework.sbk.service.SbkDTO;
import egovframework.sys.service.TestStndr;
import egovframework.tst.dto.TestDTO.Req;
import egovframework.tst.dto.TestDTO.Res;

@Mapper("TstMapper")
public interface TstMapper {

  public List<TestCate> selectCrtfList(int topCode);

  public List<TestStndr> selectStndrList(TstParam param);

  public Test selectDetail(Req req);

  public boolean insert(Req req);

  public List<Res> selectList(ComParam param);

  public int selectListCnt(ComParam param);

  public boolean testMemInsert(Req req);

  public List<Res> testMemList(String testSeq);

  public boolean testStateInsert(Req req);

  public List<Res> testStateList(String testSeq);

  public boolean testBoardInsert(Req req);

  public List<Res> testBoardList(String testSeq);

  public SbkDTO.Res testBoardAppDetail(String sbkId);

  public boolean update(Req req);

  public List<Res> selectSaleList(ComParam param);

  public int selectSaleListCnt(ComParam param);

  public boolean testStateUpdate(Req req);
}
