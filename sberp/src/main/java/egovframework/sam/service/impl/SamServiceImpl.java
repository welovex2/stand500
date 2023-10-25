package egovframework.sam.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import egovframework.cmm.service.ComParam;
import egovframework.sam.dto.ImDTO;
import egovframework.sam.dto.ImSubDTO;
import egovframework.sam.service.ImSub;
import egovframework.sam.service.SamMapper;
import egovframework.sam.service.SamService;

@Service("SamService")
public class SamServiceImpl implements SamService {

  @Autowired
  SamMapper samMapper;
  
  @Override
  @Transactional
  public boolean insert(ImDTO req) {

    boolean result = true;
    
    samMapper.insert(req);
    ImDTO detail = samMapper.detail(req.getSbkId());
    if (detail != null) {
      req.setImId(detail.getImId());
    }
    
    // 시료리스트
    if (req.getItemList() != null) {
      List<ImSub> iItems = req.getItemList().stream().filter(t -> "I".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(iItems)) {
        
        for (ImSub item : iItems) {
          samMapper.insertSub(req.getInsMemId(), req.getImId(), item);
        }
      }
    }
    
    return result;
  }

  @Override
  @Transactional
  public boolean update(ImDTO req) {
    boolean result = true;
    
    samMapper.update(req);
    
    // 시료리스트
    if (!ObjectUtils.isEmpty(req.getItemList())) {
      List<ImSub> iItems = req.getItemList().stream().filter(t -> "I".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(iItems)) {
        for (ImSub item : iItems) {
          samMapper.insertSub(req.getInsMemId(), req.getImId(), item);
        }
      }
  
      List<ImSub> uItems = req.getItemList().stream().filter(t -> "U".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(uItems))
        samMapper.updateSub(req.getInsMemId(), req.getImId(), uItems);
  
      List<ImSub> dItems = req.getItemList().stream().filter(t -> "D".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(dItems))
        samMapper.deleteSub(req.getInsMemId(), req.getImId(), dItems);
    }
    return result;
  }

  
  @Override
  public ImDTO detail(String sbkId) {
    
    ImDTO result = samMapper.detail(sbkId);
    
    if (!ObjectUtils.isEmpty(result))
      result.setItemList(samMapper.subList(result.getImId()));
    
    return result;
  }

  @Override
  public int selectListCnt(ComParam param) {
    return samMapper.selectListCnt(param);
  }

  @Override
  public List<ImSubDTO> selectList(ComParam param) {
    List<ImSubDTO> result = samMapper.selectList(param);
    
    // 번호 매기기
    for (int i=0; i<result.size(); i++) {
      result.get(i).setNo(param.getTotalCount() - ( ((param.getPageIndex() - 1) * param.getPageUnit()) + i));
    }
    
    return result;
  }
  
}
