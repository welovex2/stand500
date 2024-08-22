package egovframework.cnf.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import egovframework.cmm.service.ComParam;
import egovframework.cnf.service.CmpyDTO;
import egovframework.cnf.service.CmpyMng;
import egovframework.cnf.service.CmyMapper;
import egovframework.cnf.service.CmyService;
import egovframework.sts.dto.CmdDTO;
import egovframework.sts.dto.StsDTO;
import egovframework.sts.dto.CmdDTO.Sub;

@Service("CmyService")
public class CmyServiceImpl implements CmyService {

  @Autowired
  CmyMapper cmyMapper;

  private static final Logger LOGGER = LoggerFactory.getLogger(CmyServiceImpl.class);
  
  @Override
  public List<CmpyDTO> selectList(ComParam param) {
    return cmyMapper.selectList(param);
  }

  @Override
  public int selectListCnt(ComParam param) {
    return cmyMapper.selectListCnt(param);
  }

  @Override
  @Transactional 
  public boolean insert(CmpyDTO req) {
    
    if (req.getCmpySeq() == 0 && cmyMapper.selectAllSameName(req) > 0) {
      return false;
    }
    
    cmyMapper.insert(req);
    
    if (req.getMngList() != null) {
      List<CmpyMng> cIItems = req.getMngList().stream().filter(t -> "I".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(cIItems))
        cmyMapper.insertMng(req.getCmpySeq(), cIItems);
  
      List<CmpyMng> cUItems = req.getMngList().stream().filter(t -> "U".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(cUItems))
        cmyMapper.updateMng(req.getCmpySeq(), cUItems);
  
      List<CmpyMng> cDItems = req.getMngList().stream().filter(t -> "D".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(cDItems))
        cmyMapper.deleteMng(req.getCmpySeq(), cDItems);
    }
    
    return true;
  }
  
  @Override
  public boolean delete(CmpyDTO req) {
    
    return cmyMapper.delete(req);
    
  }
  
  @Override
  public CmpyDTO detail(int cmpySeq) {
    CmpyDTO detail = cmyMapper.detail(cmpySeq);
    
    if (detail != null) {
      detail.setMngList(cmyMapper.selectMngList(cmpySeq));
    }
    
    return detail;
  }
  
  @Override
  public List<CmpyDTO> selectSameName(String cmpyCode, String cmpyName) {
    return cmyMapper.selectSameName(cmpyCode, cmpyName);
  }

  @Override
  public List<CmdDTO.Sub> selectCmdList(ComParam param) {
    
    List<CmdDTO.Sub> result = cmyMapper.selectCmdList(param);
    
    /**
     * 날짜별 합계 리스트에 추가
     */
    CmdDTO.Sub year = new CmdDTO.Sub();
    
    year.setMon("년 계");
    year.setInCnt((int) result.stream().mapToInt(amt -> amt.getInCnt()).summaryStatistics().getSum());
    year.setInAmt((long) result.stream().mapToLong(amt -> amt.getInAmt()).summaryStatistics().getSum());
    year.setPay((long) result.stream().mapToLong(amt -> amt.getPay()).summaryStatistics().getSum());
    year.setArr((long) result.stream().mapToLong(amt -> amt.getArr()).summaryStatistics().getSum());
    
    result.add(year);
    
    /**
     * 총합계 리스트에 추가
     */
    result.add(cmyMapper.selectCmdTotal(param));
    
    return result;
  }

}
