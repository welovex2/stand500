package egovframework.sts.service;

import java.util.List;
import egovframework.rte.psl.dataaccess.mapper.Mapper;
import egovframework.sts.dto.SalesBriefDTO;

@Mapper("SalesBriefMapper")
public interface SalesBriefMapper {

  SalesBriefDTO.TargetRow selectTarget();

  SalesBriefDTO.NetRow selectNet();

  SalesBriefDTO.AgingRow selectAging();

  SalesBriefDTO.CountAmt selectUnbilled();

  Long selectWeekPayMan();

  SalesBriefDTO.CountAmt selectStuck();

  List<SalesBriefDTO.ArRow> selectArList();

  List<SalesBriefDTO.UnbilledRow> selectUnbilledList();

  List<SalesBriefDTO.MemberProgress> selectMemberProgress();
}
