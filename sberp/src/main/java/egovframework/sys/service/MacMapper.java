package egovframework.sys.service;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import egovframework.cmm.service.ComParam;
import egovframework.cmm.service.FileVO;
import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("MacMapper")
public interface MacMapper {

  MachineDTO selectDetail(int machineSeq);
  
  int selectTotalListCnt(ComParam param);
  
  List<MachineDTO> selectTotalList(ComParam param);
  
  List<MachineDTO> selectList(ComParam param);

  void insert(MachineDTO req);

  void update(MachineDTO req);

  int calUpdate(List<MacCal> uptFileList);

  void calInsert(MacCal req);

  List<MacCal> selectMacCal(int machineSeq);
  
  void macCalUpdate(MacCal req);

  void macCalFileDelete(@Param("machineSeq") int machineSeq, @Param("delFile") FileVO delFile);
  void macCalListDelete(@Param("machineSeq") int machineSeq, @Param("delFile") FileVO delFile);

  int selectNextMgmtNo(MachineDTO req);
  
  void rprInsert(@Param("machineSeq") int machineSeq, @Param("list") List<RprHist> insertList);
  void rprUpdate(@Param("machineSeq") int machineSeq, @Param("list") List<RprHist> updateList);
  void rprDelete(@Param("machineSeq") int machineSeq, @Param("list") List<RprHist> deleteList);
  List<RprHist> selectRprHist(int machineSeq);

}
