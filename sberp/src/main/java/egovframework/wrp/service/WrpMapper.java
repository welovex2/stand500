package egovframework.wrp.service;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import egovframework.cmm.service.ComParam;
import egovframework.rte.psl.dataaccess.mapper.Mapper;
import egovframework.wrp.dto.WeekResultDTO;
import egovframework.wrp.dto.WeekResultDTO.Req;

@Mapper("WrpMapper")
public interface WrpMapper {

  WeekResult getDetail(@Param("testTypeCode") String testTypeCode);
  List<WeekRepSub> getWeekList(@Param("testTypeCode") String testTypeCode);
  WeekResultDTO.Req checkReport(int wrSeq);
  WeekResult getFixDetail(@Param("wrSeq") int wrSeq, @Param("testTypeCode") String testTypeCode);
  List<WeekRepSub> getFixWeekList(@Param("wrSeq") int wrSeq, @Param("testTypeCode") String testTypeCode);
  String getFeedback(String testTypeCode);
  boolean insert(WeekRep req);
  boolean update(WeekRep req);
  WeekRep getReport(int wrSeq);
  boolean updateFix(WeekResultDTO.Req req);
  boolean insertFixResult(WeekResultDTO.Req req);
  boolean insertFixSub(WeekResultDTO.Req req);
  int selectListCnt(ComParam param);
  List<WeekRep> selectList(ComParam param);
  boolean delete(Req req);
}
