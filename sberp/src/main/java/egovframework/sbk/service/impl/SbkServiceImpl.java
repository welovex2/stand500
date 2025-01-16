package egovframework.sbk.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import egovframework.cmm.service.CmmMapper;
import egovframework.cmm.service.ComParam;
import egovframework.cmm.service.HisDTO;
import egovframework.cmm.service.JobMngr;
import egovframework.sbk.service.SbkDTO;
import egovframework.sbk.service.SbkDTO.Req;
import egovframework.sbk.service.SbkDTO.Res;
import egovframework.sbk.service.SbkMapper;
import egovframework.sbk.service.SbkService;
import egovframework.tst.dto.TestItemDTO;
import egovframework.tst.service.TestItemRej;

@Service("SbkService")
public class SbkServiceImpl implements SbkService {

	@Autowired
	SbkMapper sbkMapper;

	@Autowired
	CmmMapper cmmMapper;
	   
	@Override
	public SbkDTO.Res selectDetail(Req req) {
		SbkDTO.Res detail;
		
		detail = sbkMapper.selectDetail(req);
		if (detail != null) {
			detail.setItems(sbkMapper.selectTestItemList(req));
		}
		return detail;
	}

	@Override
	@Transactional
	public boolean insert(Req req) throws Exception {
		boolean result = true;
		
		// 신청서 생성
		sbkMapper.insert(req);
		req.setSbkId(sbkMapper.selectRef(req));
		
		// 업무서 공통 정보
		if (StringUtils.isEmpty(req.getQuoId())) {
			sbkMapper.insertJob(req);
			// 업무담당자 히스토리 저장
			JobMngr job = new JobMngr();
			job.setJobSeq(req.getJobSeq());
			job.setMngId(req.getMngId());
			job.setInsMemId(req.getInsMemId());
			job.setUdtMemId(req.getUdtMemId());
			cmmMapper.insertJobMng(job);
		}
		else
			sbkMapper.updateJobSbk(req);
		
		
		return result; 
	}

	@Override
	@Transactional
	public boolean update(Req sbk) {
		boolean result = true;
		
		// 신청서 수정
		result = sbkMapper.update(sbk);
		
		// 업무서 공통 정보
		result = sbkMapper.updateJob(sbk);
		
		return result;
	}

	@Override
	public int selectListCnt(ComParam param) {
		return sbkMapper.selectListCnt(param);
	}

	@Override
	public List<Res> selectList(ComParam param) throws Exception {
		List<SbkDTO.Res> list = sbkMapper.selectList(param);
		
		// 세부 아이템 정렬
	    for (Res item : list) {
	      
	      if (item.getTestItemCnt() > 1) {
	        
	        
	        List<TestItemDTO> subList = sbkMapper.selectSubList(item.getSbkId(), param.getSearchVO());
	        
	        if (!ObjectUtils.isEmpty(subList)) {
	          item.setItems(subList);
	        }
	      }
	    }
	    
	    // 번호 매기기
	    for (int i=0; i<list.size(); i++) {
	      list.get(i).setNo(param.getTotalCount() - ( ((param.getPageIndex() - 1) * param.getPageUnit()) + i));
	    }
	    
		return list;
	}

	@Override
	public boolean updateTestItemSign(TestItemDTO req) {
		return sbkMapper.updateTestItemSign(req);
	}

	@Override
	public List<TestItemRej> signRejectList(String testItemSeq) {
		return sbkMapper.signRejectList(testItemSeq);
	}

	@Override
	public boolean signRejectInsert(TestItemRej req) {
		return sbkMapper.signRejectInsert(req);
	}

	@Override
	public List<HisDTO> hisList(String sbkId) {
		return sbkMapper.hisList(sbkId);
	}

  @Override
  public Res selectDirtInfo(Req req) {
    return sbkMapper.selectDirtInfo(req);
  }

}
