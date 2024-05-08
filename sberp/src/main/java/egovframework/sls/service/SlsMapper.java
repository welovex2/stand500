package egovframework.sls.service;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import egovframework.cmm.service.ComParam;
import egovframework.quo.service.Quo;
import egovframework.rte.psl.dataaccess.mapper.Mapper;
import egovframework.sls.service.SlsDTO.Req;

@Mapper("SlsMapper")
public interface SlsMapper {

  int selectListCnt(ComParam param);

  List<SlsDTO.Res> selectList(ComParam param);

  boolean insert(SlsDTO.Req req);

  SlsDTO.Res selectDetail(SlsDTO.Req req);

  boolean updateQuoState(SlsDTO.Req req);

  boolean updateChqState(SlsDTO.Req req);

  int selectByTestListCnt(ComParam param);

  List<SlsDTO.Res> selectByTestList(ComParam param);

  List<PayDTO.Res> selectPayList(String slsSeq);

  boolean payInsert(PayDTO.Req req);

  BillDTO.Res selectSlsInfo(String slsSeq);
  
  List<BillDTO.Res> selectBillList(String slsSeq);

  boolean billInsert(SlsDTO.Req req);

  boolean delete(@Param("memId") String memId, @Param("slsId") String slsIds);

  String selectQuoId(String slsSeq);

  String selectChqId(String slsSeq);

  List<String> selectQuoIdList(Req req);

  boolean update(Req req);

  Quo selectQuoDetail(String quoId);

  boolean memoUpdate(Req req);
}
