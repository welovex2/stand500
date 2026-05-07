package egovframework.ncc.service;

import egovframework.ncc.dto.FileOpLogVO;

public interface FileOpLogService {

  Long start(FileOpLogVO vo);

  void success(Long logId, Long fileSize, Long bytesSent);

  void fail(Long logId, String errMsg, Long fileSize, Long bytesSent);
}

