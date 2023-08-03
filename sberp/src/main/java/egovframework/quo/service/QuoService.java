package egovframework.quo.service;

import java.util.List;

import egovframework.cmm.service.ComParam;
import egovframework.cmm.service.HisDTO;
import egovframework.quo.dto.EngQuoDTO;
import egovframework.quo.service.QuoDTO.Req;
import egovframework.quo.service.QuoDTO.Res;
import egovframework.rte.psl.dataaccess.util.EgovMap;
import egovframework.tst.service.TestItem;

public interface QuoService {

  public String selectRef(Req quo) throws Exception;

  public boolean insert(Req quo) throws Exception;

  public Res selectDetail(Req req) throws Exception;

  public List<Res> selectList(ComParam param) throws Exception;

  public int selectListCnt(ComParam param);

  public boolean update(Req quo) throws Exception;

  public int billCheck(String quoId);

  public boolean updateStatus(QuoModDTO.Req req);

  public List<QuoModDTO.Res> selectStatusList(String quoSeq);

  public List<HisDTO> hisList(String quoId);

  public boolean engInsert(EngQuoDTO quo) throws Exception;

  public List<EngTestItem> selectEngTestItem(String quoId);

  public int checkTestStartItem(List<TestItem> dItems);

}
