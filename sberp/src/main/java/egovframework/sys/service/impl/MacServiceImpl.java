package egovframework.sys.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import egovframework.cmm.service.ComParam;
import egovframework.cmm.service.FileVO;
import egovframework.sys.service.MacCal;
import egovframework.sys.service.MacCalDTO;
import egovframework.sys.service.MacMapper;
import egovframework.sys.service.MacService;
import egovframework.sys.service.MachineDTO;

@Service("MacService")
public class MacServiceImpl implements MacService {

  @Autowired
  MacMapper macMapper;

  @Override
  public MachineDTO selectDetail(int machineSeq) {
    
    MachineDTO detail = new MachineDTO();
    
    detail = macMapper.selectDetail(machineSeq);;
    
    // 교정정보 추가하기
    if (!ObjectUtils.isEmpty(detail)) {
      detail.setMacCalList(macMapper.selectMacCal(machineSeq));
    }
    
    return detail;
  }
  
  @Override
  public List<MachineDTO> selectList(ComParam param) {
    return macMapper.selectList(param);
  }

  @Override
  @Transactional
  public boolean insert(MachineDTO req, MacCalDTO macCal) {

    
    // 관리번호 얻기
    req.setMgmtNo(macMapper.selectNextMgmtNo(req));
    // 장비정보 저장
    macMapper.insert(req);
    
    
    // 교정정보 라인 추가
    if (!ObjectUtils.isEmpty(macCal.getMacCal())) {
      macCal.getMacCal().setMachineSeq(req.getMachineSeq());
      macMapper.calInsert(macCal.getMacCal());
      macMapper.macCalUpdate(macCal.getMacCal());
    }
    
    // 교정정보 파일정보 수정
    if (!ObjectUtils.isEmpty(macCal.getUptFileList())) {
      int cnt = macMapper.calUpdate(macCal.getUptFileList());
      
      System.out.println(cnt);
    }
    
    return true;
  }

  @Override
  public boolean update(MachineDTO req) {
    if (!"D".equals(req.getState()))
      req.setState("U");
    else {
      req.setName("");
      req.setModel("");
      req.setMnfctSerial("");
    }
    macMapper.insert(req);
    return true;
  }

  @Override
  public boolean updateSub(String type, List<MachineDTO> list) {
    for (MachineDTO req : list) {
      req.setType(type);
      macMapper.update(req);
    }
    return true;
  }

  @Override
  @Transactional
  public void macCalDelete(int machineSeq, FileVO delFile) {
    macMapper.macCalFileDelete(machineSeq, delFile);
    macMapper.macCalListDelete(machineSeq, delFile);
  }

  @Override
  public int selectTotalListCnt(ComParam param) {
    return macMapper.selectTotalListCnt(param);
  }

  @Override
  public List<MachineDTO> selecTotaltList(ComParam param) {
    
    List<MachineDTO> list = macMapper.selectTotalList(param);
    
    list.stream().map(mac -> {
        if (!"-".equals(mac.getPhoto())) { // '-' 값이 아닐 경우에만 변환
            mac.setPhoto("/api/file/getImage.do?atchFileId=" + mac.getPhoto() + "&fileSn=0");
        }
        return mac;
    }).collect(Collectors.toList());
    
    return list;
  }

  @Override
  public List<MacCal> selectMacCal(int machineSeq) {
    return macMapper.selectMacCal(machineSeq);
  }

}
