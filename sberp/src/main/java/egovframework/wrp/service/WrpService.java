package egovframework.wrp.service;

import java.util.List;
import egovframework.cmm.service.ComParam;
import egovframework.wrp.dto.WeekResultDTO;
import egovframework.wrp.dto.WeekResultDTO.Req;

public interface WrpService {

  List<WeekResultDTO> getDetail(String testTypeCode);

  WeekResultDTO.Req checkReport(int wrSeq);

  List<WeekResultDTO> getFixDetail(int wrSeq, String testTypeCode);

  String getFeedback(String testTypeCode);

  boolean insert(WeekRep req);

  boolean update(WeekRep req);

  WeekRep getReport(int wrSeq);

  boolean updateFix(WeekResultDTO.Req req);

  int selectListCnt(ComParam param);

  List<WeekRep> selectList(ComParam param);

  boolean delete(Req req);

}
