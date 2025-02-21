package egovframework.sys.service;

import java.util.List;

import egovframework.cmm.service.ComParam;
import egovframework.cmm.service.FileVO;

public interface MacService {

  List<MachineDTO> selectList(ComParam param);

  boolean insert(MachineDTO req, MacCalDTO macCal);

  boolean update(MachineDTO req);

  boolean updateSub(String type, List<MachineDTO> list);

  void macCalDelete(int machineSeq, FileVO delFile);

  MachineDTO selectDetail(int machineSeq);

  int selectTotalListCnt(ComParam param);

  List<MachineDTO> selecTotaltList(ComParam param);

  List<MacCal> selectMacCal(int machineSeq);

}
