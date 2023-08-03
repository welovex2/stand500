package egovframework.ppc.service;

import java.util.List;
import egovframework.cmm.service.ComParam;
import egovframework.ppc.dto.PpDTO;
import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("PpcMapper")
public interface PpcMapper {

	public List<PpDTO> selectList(ComParam parma) throws Exception;
	
	public boolean insert(PpDTO pp);

	public boolean insertMemo(PpHis pp);

	public PpDTO selectDetail(String ppId);

	public List<PpHis> selectMemoList(String ppId) throws Exception;
	
	public void update(PpDTO pp);

	public int selectListCnt(ComParam param);

}
