package egovframework.chq.service;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import egovframework.chq.service.ChqDTO.Res;
import egovframework.cmm.service.ComParam;
import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("ChqMapper")
public interface ChqMapper {

  int isCnfrmDt(@Param("quoIds") List<String> quoIds);

  int isSameCons(@Param("quoIds") List<String> quoIds);

  int isChq(@Param("quoIds") List<String> quoIds);

  boolean insert(ChqDTO req);

  boolean updateQuo(@Param("chqSeq") int chqSeq, @Param("quoIds") List<String> quoIds);

  public List<ChqDTO.Res> selectList(ComParam param);

  public List<ChqDTO.Sub> selectDetail(ChqDTO req);

  public int selectListCnt(ComParam param);

  Res selectInfo(ChqDTO req);

  boolean delete(@Param("memId") String memId, @Param("chqId") String chqIds);

  boolean deletQuo(@Param("memId") String memId, @Param("chqId") String chqIds);
}
