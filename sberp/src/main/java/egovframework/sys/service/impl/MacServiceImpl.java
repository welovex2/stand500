package egovframework.sys.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import egovframework.cmm.service.ComParam;
import egovframework.cmm.service.EgovFileMngService;
import egovframework.cmm.service.FileVO;
import egovframework.cmm.service.NextcloudDavService;
import egovframework.sys.service.MacCal;
import egovframework.sys.service.MacCalDTO;
import egovframework.sys.service.MacMapper;
import egovframework.sys.service.MacService;
import egovframework.sys.service.MachineDTO;
import egovframework.sys.service.RprHist;

@Service("MacService")
public class MacServiceImpl implements MacService {

  @Autowired
  MacMapper macMapper;

  @Autowired
  EgovFileMngService fileMngService;
  
  @Autowired
  private NextcloudDavService nextcloudDavService;
  
  @Override
  public MachineDTO selectDetail(int machineSeq) {
    
    try {
      MachineDTO detail = new MachineDTO();
      
      detail = macMapper.selectDetail(machineSeq);;
      // 장비이미지
      if (!ObjectUtils.isEmpty(detail)) {
        FileVO fileVO = new FileVO();
        fileVO.setAtchFileId(detail.getAtchFileId());
        FileVO photoVO = fileMngService.selectFileInf(fileVO);
        detail.setPhoto(nextcloudDavService.resolveFileUrl(photoVO));
      }
      
      // 교정정보 추가하기
      if (!ObjectUtils.isEmpty(detail)) {
        detail.setMacCalList(macMapper.selectMacCal(machineSeq));
      }
      
      // 수리내역 추가하기
      if (!ObjectUtils.isEmpty(detail)) {
        detail.setRprHistList(macMapper.selectRprHist(machineSeq));
      }
    
      return detail;
      
    } catch (Exception e) {
      // log.error("selectDetail error. machineSeq={}", machineSeq, e);
      throw new RuntimeException("장비 상세 조회 중 오류가 발생했습니다. machineSeq=" + machineSeq, e);
    }
  }
  
  @Override
  public List<MachineDTO> selectList(ComParam param) {
    return macMapper.selectList(param);
  }

  @Override
  @Transactional
  public MachineDTO insertBase(MachineDTO req) {
    // 관리번호 얻기
    req.setMgmtNo(macMapper.selectNextMgmtNo(req));
    
    // 장비정보 저장
    macMapper.insert(req);
    return req;
  }
  
  @Override
  @Transactional
  public boolean insert(MachineDTO req, MacCalDTO macCal) {
    
    // 파일정보 추가
    if (!ObjectUtils.isEmpty(req.getPhoto())) {
      macMapper.updateMachineFile(req);
    }
    
    // 교정정보 라인 추가
    if (!ObjectUtils.isEmpty(macCal.getMacCal())) {
      macCal.getMacCal().setMachineSeq(req.getMachineSeq());
      macMapper.calInsert(macCal.getMacCal());
      macMapper.macCalUpdate(macCal.getMacCal());
    }
    
    // 교정정보 파일정보 수정
    if (!ObjectUtils.isEmpty(macCal.getUptFileList())) {
      int cnt = macMapper.calUpdate(macCal.getUptFileList());
    }
    
    // 수리내역
    List<RprHist> insertList = new ArrayList<>();
    List<RprHist> updateList = new ArrayList<>();
    List<RprHist> deleteList = new ArrayList<>();

    if (!ObjectUtils.isEmpty(req.getRprHistList())) {
      for (RprHist dto : req.getRprHistList()) {
          if (dto.getRprSeq() == null || dto.getRprSeq() == 0) {
            insertList.add(dto); // 등록
          } else if ("D".equals(dto.getState())){
            deleteList.add(dto); // 삭제
          } else {
            updateList.add(dto); // 수정
          }
      }
    }
    
    if (!insertList.isEmpty()) {
      macMapper.rprInsert(req.getMachineSeq(), insertList);
    }
    if (!deleteList.isEmpty()) {
      macMapper.rprDelete(req.getMachineSeq(), deleteList);
    }
    if (!updateList.isEmpty()) {
      macMapper.rprUpdate(req.getMachineSeq(), updateList);
    }
    
    //
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
    
    // 번호 매기기
    for (int i=0; i<list.size(); i++) {
      list.get(i).setNo(param.getTotalCount() - ( ((param.getPageIndex() - 1) * param.getPageUnit()) + i));
    }
    
    return list;
  }

  @Override
  public List<MacCal> selectMacCal(int machineSeq) {
    return macMapper.selectMacCal(machineSeq);
  }

  @Override
  public List<RprHist> selectRprHist(int machineSeq) {
    return macMapper.selectRprHist(machineSeq);
  }

}
