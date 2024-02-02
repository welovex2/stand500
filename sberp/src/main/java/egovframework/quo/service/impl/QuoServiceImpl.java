package egovframework.quo.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import egovframework.cmm.service.CmmMapper;
import egovframework.cmm.service.ComParam;
import egovframework.cmm.service.HisDTO;
import egovframework.cmm.service.JobMngr;
import egovframework.quo.dto.EngQuoDTO;
import egovframework.quo.service.EngTestItem;
import egovframework.quo.service.QuoDTO.Req;
import egovframework.quo.service.QuoDTO.Res;
import egovframework.quo.service.QuoMapper;
import egovframework.quo.service.QuoModDTO;
import egovframework.quo.service.QuoService;
import egovframework.tst.service.TestItem;

@Service("QuoService")
public class QuoServiceImpl implements QuoService {

  @Autowired
  QuoMapper quoMapper;

  @Autowired
  CmmMapper cmmMapper;
  
  @Override
  public String selectRef(Req quo) throws Exception {
    return quoMapper.selectRef(quo);
  }

  @Override
  @Transactional
  public boolean insert(Req quo) throws Exception {
    boolean result = true;

    // 견적서 기본정보
    quoMapper.insert(quo);
    quo.setQuoId(quoMapper.selectRef(quo));

    // 견적서 시험항목
    if (!ObjectUtils.isEmpty(quo.getTestItems())) {
//      List<TestItem> iItems = quo.getTestItems().stream().filter(t -> "DH".equals(t.getTestTypeCode())).map(item -> {
//        item.setSignStateCode("3");
//        return item;
//      }).collect(Collectors.toList());
      quoMapper.insertTestItem(quo.getQuoId(), quo.getInsMemId(), quo.getTestItems());
    }

    // 업무서 공통 정보
    if (StringUtils.isEmpty(quo.getSbkId())) {
      quoMapper.insertJob(quo);
      
      // 업무담당자 히스토리 저장
      JobMngr job = new JobMngr();
      job.setJobSeq(quo.getJobSeq());
      job.setMngId(quo.getMngId());
      job.setInsMemId(quo.getInsMemId());
      job.setUdtMemId(quo.getUdtMemId());
      cmmMapper.insertJobMng(job);
    }
    else
      quoMapper.updateJobQuo(quo);

    return result;
  }

  @Override
  public Res selectDetail(Req quo) throws Exception {
    Res detail = quoMapper.selectDetail(quo);
    return detail;
  }

  @Override
  public List<Res> selectList(ComParam param) throws Exception {
    List<Res> list = quoMapper.selectList(param);
    return list;
  }

  @Override
  public int selectListCnt(ComParam param) {
    return quoMapper.selectListCnt(param);
  }

  @Override
  @Transactional
  public boolean update(Req quo) throws Exception {
    boolean result = true;

    // 견적서 기본정보
    quoMapper.update(quo);
    
    // 견적서 시험항목
    List<TestItem> iItems = quo.getTestItems().stream().filter(t -> "I".equals(t.getState()))
        .collect(Collectors.toList());
    if (!ObjectUtils.isEmpty(iItems))
      quoMapper.insertTestItem(quo.getQuoId(), quo.getInsMemId(), iItems);

    List<TestItem> uItems = quo.getTestItems().stream().filter(t -> "U".equals(t.getState()))
        .collect(Collectors.toList());
    if (!ObjectUtils.isEmpty(uItems))
      quoMapper.updateTestItem(quo.getQuoId(), quo.getInsMemId(), uItems);

    List<TestItem> dItems = quo.getTestItems().stream().filter(t -> "D".equals(t.getState()))
        .collect(Collectors.toList());
    if (!ObjectUtils.isEmpty(dItems))
      quoMapper.deleteTestItem(quo.getQuoId(), quo.getInsMemId(), dItems);

    // 업무서 공통 정보
    quoMapper.updateJob(quo);

    return result;
  }

  @Override
  @Transactional
  public boolean updateStatus(QuoModDTO.Req req) {
    boolean result = true;

    // 견정서 수정
    if (!"5".equals(req.getStateCode())) {
      // 견적서 상태 변경
      quoMapper.updateQuoState(req);

    }
    // 세금계산서 신청
    else {
      quoMapper.billInsert(req);
    }


    if ("2".equals(req.getStateCode()) || "5".equals(req.getStateCode())) {
      // 수정 히스토리 등록
      quoMapper.insertState(req);
    } else {
      // 수정 히스토리 수정
      quoMapper.updateState(req);
    }

    return result;
  }

  @Override
  public List<QuoModDTO.Res> selectStatusList(String quoSeq) {
    return quoMapper.selectStatusList(quoSeq);
  }

  @Override
  public List<HisDTO> hisList(String quoId) {
    return quoMapper.hisList(quoId);
  }

  @Override
  public int billCheck(String quoId) {
    
    int result = 0;
    
    Integer detail = quoMapper.billCheck(quoId);
    if (detail == null)
        result = 0;
    else
        result = detail;
    
    return result;
  }

  @Override
  @Transactional
  public boolean engInsert(EngQuoDTO quo) throws Exception {
    
    boolean result = true;

    // 견적서 기본정보
    quoMapper.engUpdate(quo);

    // 견적서 시험항목
    List<EngTestItem> iItems = quo.getEngTestItems().stream().filter(t -> "I".equals(t.getState()))
        .collect(Collectors.toList());
    if (!ObjectUtils.isEmpty(iItems))
      quoMapper.insertEngTestItem(quo.getQuoId(), quo.getInsMemId(), iItems);

    List<EngTestItem> uItems = quo.getEngTestItems().stream().filter(t -> "U".equals(t.getState()))
        .collect(Collectors.toList());
    if (!ObjectUtils.isEmpty(uItems))
      quoMapper.updateEngTestItem(quo.getQuoId(), quo.getInsMemId(), uItems);

    List<EngTestItem> dItems = quo.getEngTestItems().stream().filter(t -> "D".equals(t.getState()))
        .collect(Collectors.toList());
    if (!ObjectUtils.isEmpty(dItems))
      quoMapper.updateEngTestItem(quo.getQuoId(), quo.getInsMemId(), dItems);
    
    return result;
  }

  @Override
  public List<EngTestItem> selectEngTestItem(String quoId) {
    return quoMapper.selectEngTestItem(quoId);
  }

  @Override
  public int checkTestStartItem(List<TestItem> dItems) {
    return quoMapper.checkTestStartItem(dItems);
  }

  @Override
  public boolean updateTestItemModel(List<TestItem> req) throws Exception {
    return quoMapper.updateTestItemModel(req);
  }
}
