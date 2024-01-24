package egovframework.sls.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import egovframework.cmm.service.ComParam;
import egovframework.quo.service.Quo;
import egovframework.sls.service.BillDTO;
import egovframework.sls.service.PayDTO;
import egovframework.sls.service.SlsDTO;
import egovframework.sls.service.SlsDTO.Req;
import egovframework.sls.service.SlsMapper;
import egovframework.sls.service.SlsService;

@Service("SlsService")
public class SlsServiceImpl implements SlsService {

  @Autowired
  SlsMapper slsMapper;

  @Override
  public int selectListCnt(ComParam param) {
    return slsMapper.selectListCnt(param);
  }

  @Override
  public List<SlsDTO.Res> selectList(ComParam param) {
    List<SlsDTO.Res> list = slsMapper.selectList(param);
    return list;
  }

  @Override
  @Transactional
  public boolean insertChq(Req req) {
    boolean result = true;

    // 매출리스트 등록
    result = slsMapper.insert(req);
    
    // 계산서 발행내역 등록
    result = slsMapper.billInsert(req);
    
    // 견적서 상태변경
    for(String id : req.getQuoIds()) {
      req.setQuoId(id);
      result = slsMapper.updateQuoState(req);
    }

    return result;
  }
  
  @Override
  @Transactional
  public boolean insert(SlsDTO.Req req) {
    boolean result = true;

    // 매출리스트 등록
    result = slsMapper.insert(req);
    
    // 계산서 발행내역 등록
    result = slsMapper.billInsert(req);

    // 견적서 상태변경
    result = slsMapper.updateQuoState(req);

    return result;
  }

  @Override
  public SlsDTO.Res selectDetail(SlsDTO.Req req) {
    return slsMapper.selectDetail(req);
  }

  @Override
  public int selectByTestListCnt(ComParam param) {
    return slsMapper.selectByTestListCnt(param);
  }

  @Override
  public List<SlsDTO.Res> selectByTestList(ComParam param) {
    List<SlsDTO.Res> list = slsMapper.selectByTestList(param);
    return list;
  }

  @Override
  public List<PayDTO.Res> selectPayList(String slsSeq) {
    return slsMapper.selectPayList(slsSeq);
  }

  @Override
  public boolean payInsert(PayDTO.Req req) {
    return slsMapper.payInsert(req);
  }

  @Override
  public List<BillDTO.Res> selectBillList(String slsSeq) {
    return slsMapper.selectBillList(slsSeq);
  }

  @Override
  public boolean billInsert(SlsDTO.Req req) {
    
    // 납부가 완료되면 매출리스트 미수금액 조정
    if (!StringUtils.isEmpty(req.getPayCode())) {
      slsMapper.update(req);
    }
    return slsMapper.billInsert(req);
  }

  @Override
  @Transactional
  public boolean delete(String memId, List<String> slsIds) {
    boolean result = true;

    for (String item : slsIds) {
      // 견적서 ID 확인
      String quoId = slsMapper.selectQuoId(item);
      
      // 취합견적서
      if (StringUtils.isEmpty(quoId)) {
        String chqId = slsMapper.selectChqId(item);
        // 견적서 상태변경
        SlsDTO.Req req = new SlsDTO.Req();
        req.setQuoStateCode("0"); // 매출확정전 상태로 변경
        req.setUdtMemId(memId);
        req.setChqId(chqId);
        result = slsMapper.updateChqState(req);
      } 
      // 일반견적서
      else {
        // 견적서 상태변경
        SlsDTO.Req req = new SlsDTO.Req();
        req.setQuoStateCode("0"); // 매출확정전 상태로 변경
        req.setUdtMemId(memId);
        req.setQuoId(quoId);
        result = slsMapper.updateQuoState(req);
      }
      // 매출 삭제
      result = slsMapper.delete(memId, item);

    }

    return result;
  }

  @Override
  public List<String> selectQuoIdList(Req req) {
    return slsMapper.selectQuoIdList(req);
  }

  @Override
  public Quo selectQuoDetail(String quoId) {
    return slsMapper.selectQuoDetail(quoId);
  }

  @Override
  public boolean memoUpdate(Req req) {
    return slsMapper.memoUpdate(req);
  }

}
