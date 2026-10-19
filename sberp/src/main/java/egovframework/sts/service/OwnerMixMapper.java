package egovframework.sts.service;

import java.util.List;
import egovframework.rte.psl.dataaccess.mapper.Mapper;
import egovframework.sts.dto.OwnerMixDTO;

@Mapper("OwnerMixMapper")
public interface OwnerMixMapper {

  /** 담당자별 올해·전년동기 건수와 매출. */
  List<OwnerMixDTO.OwnerRow> selectOwner();

  /** 담당자 × 시험부 올해 매출. */
  List<OwnerMixDTO.OwnerDeptRow> selectOwnerDept();

  /** 시험부 코드와 이름 전체. */
  List<OwnerMixDTO.DeptCol> selectDeptCodes();
}
