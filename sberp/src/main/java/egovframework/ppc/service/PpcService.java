package egovframework.ppc.service;

import java.util.List;
import egovframework.cmm.service.ComParam;
import egovframework.ppc.dto.PpDTO;

public interface PpcService {
	
	public List<PpDTO> selectList(ComParam param) throws Exception;

	public boolean insert(PpDTO pp) throws Exception;

	public PpDTO selectDetail(String ppId) throws Exception;

	public boolean update(PpDTO pp) throws Exception;

	public int selectListCnt(ComParam param);

}
