package egovframework.chq.service;

import java.util.List;
import egovframework.chq.service.ChqDTO.Res;
import egovframework.cmm.service.ComParam;

public interface ChqService {

  int isCnfrmDt(List<String> quoIds);

  int isSameCons(List<String> quoIds);

  int isChq(List<String> quoIds);

  boolean insert(ChqDTO req);

  int selectListCnt(ComParam param);

  List<Res> selectList(ComParam param);

  Res selectDetail(ChqDTO req);

  boolean delete(String memId, List<String> chqIds);

}
