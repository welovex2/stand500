package egovframework.cnf.service;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import egovframework.cmm.service.ComParam;
import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("CmyMapper")
public interface CmyMapper {

  List<CmpyDTO> selectList(ComParam param);

  int selectListCnt(ComParam param);

  boolean insert(CmpyDTO req);

  boolean insertMng(@Param("cmpySeq") int cmpySeq, @Param("mngList") List<CmpyMng> sUItems);

  boolean updateMng(@Param("cmpySeq") int cmpySeq, @Param("mngList") List<CmpyMng> sUItems);

  boolean deleteMng(@Param("cmpySeq") int cmpySeq, @Param("mngList") List<CmpyMng> sDItems);

  CmpyDTO detail(int cmpySeq);

  List<CmpyMng> selectMngList(int cmpySeq);

  List<CmpyDTO> selectSameName(@Param("cmpyCode") String cmpyCode, @Param("cmpyName") String cmpyName);
}
