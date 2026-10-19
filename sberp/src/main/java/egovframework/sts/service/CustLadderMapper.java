package egovframework.sts.service;

import java.util.List;
import egovframework.rte.psl.dataaccess.mapper.Mapper;
import egovframework.sts.dto.CustLadderDTO;

@Mapper("CustLadderMapper")
public interface CustLadderMapper {

  /** 업체(CMPY_SEQ) 한 줄씩. 같은 회사를 합치는 일은 서비스에서 한다. */
  List<CustLadderDTO.BaseRow> selectCustBase();

  /** 시험부 코드와 이름 전체. */
  List<CustLadderDTO.DeptCode> selectDeptCodes();
}
