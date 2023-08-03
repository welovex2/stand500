package egovframework.cns.service;

import java.util.List;

import egovframework.cmm.service.ComParam;
import egovframework.cns.service.CnsDTO.Req;
import egovframework.cns.service.CnsDTO.Res;
import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("CnsMapper")
public interface CnsMapper {

	public List<Res> selectList(ComParam parma) throws Exception;

	public boolean insert(Req cns);

	public boolean insertJob(Req cns);

	public boolean insertMemo(Req cns);

	public Res selectDetail(int cnsSeq);

	public void update(Req cns);

	public void updateJob(Req cns);
	
	public void updateJobCns(Req cns);

	public int selectListCnt(ComParam param);

	public Integer checkDetail(Req req);
}
