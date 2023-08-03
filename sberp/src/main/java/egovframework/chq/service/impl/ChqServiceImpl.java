package egovframework.chq.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import egovframework.chq.service.ChqDTO;
import egovframework.chq.service.ChqDTO.Res;
import egovframework.chq.service.ChqMapper;
import egovframework.chq.service.ChqService;
import egovframework.cmm.service.ComParam;
import egovframework.sls.service.SlsDTO;

@Service("ChqService")
public class ChqServiceImpl implements ChqService {

	@Autowired
	ChqMapper chqMapper;

	@Override
	public int isCnfrmDt(List<String> quoIds) {
		return chqMapper.isCnfrmDt(quoIds);
	}

	@Override
	public int isSameCons(List<String> quoIds) {
		return chqMapper.isSameCons(quoIds);
	}

	@Override
	public int isChq(List<String> quoIds) {
		return chqMapper.isChq(quoIds);
	}

	@Override
	@Transactional
	public boolean insert(ChqDTO req) {
		boolean result = true;
		
		// 취합견적서 등록
		chqMapper.insert(req);
		
		// 견적서 업데이트
		chqMapper.updateQuo(req.getChqSeq(), req.getQuoIds());
		
		return result;
	}

  @Override
  public int selectListCnt(ComParam param) {
    return chqMapper.selectListCnt(param);
  }

  @Override
  public List<Res> selectList(ComParam param) {
    return chqMapper.selectList(param);
  }

  @Override
  public Res selectDetail(ChqDTO req) {
    
    Res detail = chqMapper.selectInfo(req);
    
    if (detail != null) {
      detail.setSub(chqMapper.selectDetail(req));
    }
    
    return detail;
  }

  @Override
  @Transactional
  public boolean delete(String memId, List<String> chqIds) {
    boolean result = true;

    for (String item : chqIds) {
      
      // 취합견적서 삭제
      chqMapper.delete(memId, item);
      
      // 견적서 업데이트
      chqMapper.deletQuo(memId, item);
    }

    return result;
  }
	
}
