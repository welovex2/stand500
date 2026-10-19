package egovframework.sts.service;

import java.util.List;
import egovframework.rte.psl.dataaccess.mapper.Mapper;
import egovframework.sts.dto.UnbilledDTO;

@Mapper("UnbilledMapper")
public interface UnbilledMapper {

  /** 완료 후 미청구 시험 한 건씩. 견적으로 묶는 일은 서비스에서 한다. */
  List<UnbilledDTO.ItemRow> selectUnbilled();
}
