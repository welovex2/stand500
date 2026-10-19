package egovframework.sts.service;

import egovframework.sts.dto.UnbilledDTO;

public interface UnbilledService {

  /** @param minDays 명단에 실을 최소 경과일 */
  UnbilledDTO getUnbilled(int minDays);
}
