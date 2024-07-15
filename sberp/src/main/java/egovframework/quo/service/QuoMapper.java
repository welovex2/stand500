package egovframework.quo.service;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import egovframework.cmm.service.ComParam;
import egovframework.cmm.service.HisDTO;
import egovframework.quo.dto.EngQuoDTO;
import egovframework.quo.service.QuoDTO.Req;
import egovframework.quo.service.QuoDTO.Res;
import egovframework.rte.psl.dataaccess.mapper.Mapper;
import egovframework.tst.service.TestItem;

@Mapper("QuoMapper")
public interface QuoMapper {

  public String selectRef(Req quo) throws Exception;

  public boolean insert(Req quo) throws Exception;

  public boolean update(Req quo) throws Exception;

  public boolean insertJob(Req quo);

  public boolean updateJob(Req quo);

  public boolean updateJobQuo(Req quo);
  
  public boolean updateArrears(Req quo);

  public List<Res> selectList(ComParam param);

  public Res selectDetail(Req quo);

  public int selectListCnt(ComParam param);

  public boolean insertTestItem(@Param("quoId") String quoId, @Param("memId") String memId,
      @Param("testItems") List<TestItem> testItems) throws Exception;

  public boolean updateTestItem(@Param("quoId") String quoId, @Param("memId") String memId,
      @Param("testItems") List<TestItem> testItems) throws Exception;

  public boolean deleteTestItem(@Param("quoId") String quoId, @Param("memId") String memId,
      @Param("testItems") List<TestItem> testItems) throws Exception;

  public boolean updateQuoState(QuoModDTO.Req req);

  public Integer billCheck(String quoId);

  public boolean billInsert(QuoModDTO.Req req);

  public List<QuoModDTO.Res> selectStatusList(String quoSeq);

  public boolean insertState(QuoModDTO.Req req);

  public boolean updateState(QuoModDTO.Req req);

  public List<HisDTO> hisList(String quoId);

  public void engUpdate(EngQuoDTO quo);

  public boolean insertEngTestItem(@Param("quoId") String quoId, @Param("memId") String memId,
      @Param("testItems") List<EngTestItem> testItems) throws Exception;

  public boolean updateEngTestItem(@Param("quoId") String quoId, @Param("memId") String memId,
      @Param("testItems") List<EngTestItem> testItems) throws Exception;

  public List<EngTestItem> selectEngTestItem(String quoId);

  public int checkTestStartItem(@Param("testItems") List<TestItem> testItems);
  
  public boolean updateTestItemModel(@Param("testItems") List<TestItem> testItems) throws Exception;

}
