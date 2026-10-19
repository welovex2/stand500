package egovframework.sts.service;

import java.util.List;
import egovframework.rte.psl.dataaccess.mapper.Mapper;
import egovframework.sts.dto.DeptPnlDTO;

@Mapper("DeptPnlMapper")
public interface DeptPnlMapper {

  /** 수주(견적 발행) 기준 시험부별 건수·순매출·청구액. 올해와 전년동기를 한 줄에 담는다. */
  List<DeptPnlDTO.BookingRow> selectBooking();

  /** 완료 처리량과 리드타임. 올해와 전년동기. */
  List<DeptPnlDTO.ThroughputRow> selectThroughput();

  /** 지금 미완료로 묶여 있는 건수·금액·경과일. */
  List<DeptPnlDTO.WipRow> selectWip();
}
