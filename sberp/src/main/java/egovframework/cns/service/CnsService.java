package egovframework.cns.service;

import java.util.List;

import egovframework.cmm.service.ComParam;
import egovframework.cns.service.CnsDTO.Req;
import egovframework.cns.service.CnsDTO.Res;

public interface CnsService {
	
	public List<Res> selectList(ComParam param) throws Exception;

	public boolean insert(Req cns) throws Exception;

	public Res selectDetail(int cnsSeq) throws Exception;

	public boolean update(Req cns) throws Exception;

	public int selectListCnt(ComParam param);

	public Integer checkDetail(Req req);
}
