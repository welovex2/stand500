package egovframework.tst.service;

import java.util.List;
import egovframework.cmm.service.ComParam;
import egovframework.rte.psl.dataaccess.mapper.Mapper;
import egovframework.tst.dto.DebugDTO;
import egovframework.tst.dto.TestDTO.Req;
import egovframework.tst.dto.TestDTO.Res;

@Mapper("DbgMapper")
public interface DbgMapper {
  
    public int selectDetail(int debusSeq);
    
    public boolean update(DebugDTO req);

	public boolean insert(DebugDTO req);

	public List<DebugDTO> selectList(ComParam param);

	public int selectListCnt(ComParam param);

	public boolean debugStateInsert(DebugDTO req);

	public List<DebugDTO> debugStateList(String debugSeq);
	
	public boolean debugBoardInsert(DebugMemo req);

	public List<DebugMemo> debugBoardList(String debugSeq);
	
}
