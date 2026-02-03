package egovframework.ncc.service;

import egovframework.ncc.dto.DownloadLogVO;

public interface DownloadLogService {

  Long startLog(DownloadLogVO vo);

  void success(Long logId, long bytesSent);

  void fail(Long logId, String errMsg, long bytesSent);

}
