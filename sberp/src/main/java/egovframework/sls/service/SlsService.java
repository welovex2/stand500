package egovframework.sls.service;

import java.util.List;

import egovframework.cmm.service.ComParam;
import egovframework.quo.service.Quo;
import egovframework.sls.service.PayDTO.Res;
import egovframework.sls.service.SlsDTO.Req;

public interface SlsService {

  int selectListCnt(ComParam param);

  List<SlsDTO.Res> selectList(ComParam param);

  boolean insert(SlsDTO.Req req);

  SlsDTO.Res selectDetail(SlsDTO.Req req);

  int selectByTestListCnt(ComParam param);

  List<SlsDTO.Res> selectByTestList(ComParam param);

  List<PayDTO.Res> selectPayList(String slsSeq);

  boolean payInsert(PayDTO.Req req);

  List<BillDTO.Res> selectBillList(String slsSeq);

  boolean billInsert(SlsDTO.Req req);

  boolean delete(String memId, List<String> slsSeqs);

  List<String> selectQuoIdList(Req req);

  boolean insertChq(Req req);

  Quo selectQuoDetail(String quoId);

  boolean memoUpdate(Req req);

}
