package egovframework.tst.service;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import egovframework.cmm.service.ComParam;
import egovframework.cmm.service.NcGrantDTO;
import egovframework.cmm.service.SearchVO;
import egovframework.rte.psl.dataaccess.mapper.Mapper;
import egovframework.sbk.service.SbkDTO;
import egovframework.sys.service.TestStndr;
import egovframework.tst.dto.CanCelDTO;
import egovframework.tst.dto.TestDTO.Req;
import egovframework.tst.dto.TestDTO.Res;
import egovframework.tst.dto.TestItemDTO;
import egovframework.tst.dto.TestMngrDTO;

@Mapper("TstMapper")
public interface TstMapper {

  public List<TestCate> selectCrtfList(int topCode);

  public List<TestStndr> selectStndrList(TstParam param);

  public Test selectDetail(Req req);

  public boolean insert(Req req);

  public List<Res> selectList(ComParam param);

  public int selectListCnt(ComParam param);

  public boolean testInfoUpate(TestMngrDTO req);
  
  public boolean testMemInsert(TestMngr req);
  
  public boolean testMemSatetUpdate(TestMngrDTO req);

  public TestMngrDTO testMemInfo(String testSeq);
  
  public List<TestMngr> testMemList(String testSeq);

  public boolean testStateInsert(Req req);

  public List<Res> testStateList(String testSeq);

  public boolean testBoardInsert(Req req);

  public List<Res> testBoardList(String testSeq);

  public SbkDTO.Res testBoardAppDetail(String sbkId);

  public boolean update(Req req);

  public List<Res> selectSaleList(ComParam param);

  public int selectSaleListCnt(ComParam param);

  public boolean testStateUpdate(Req req);
  
  public List<TestItemDTO> selectSubList(@Param("sbkId") String sbkId, @Param("searchVO") List<SearchVO> param);

  public List<Res> selectRevList(ComParam param);
  
  public CanCelDTO cancelInfo(int testItemSeq);

  public boolean cancelInsert(CanCelDTO req);
  
  public boolean cancelQuoUpdate(CanCelDTO req);

  public Res checkTestState(int testSeq);

  public boolean checkInsert(Req req);
  
  public int selectCheckInfo(String testSeq);

  public boolean saleMemoInsert(Req req);

  public String selectNcGrantByApplyNo(int testSeq);
}
