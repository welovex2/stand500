package egovframework.ncc.service;

import java.util.List;

import egovframework.cmm.service.ComParam;
import egovframework.ncc.dto.FileOpLogListItemDTO;
import egovframework.ncc.dto.FileOpLogSummaryDTO;
import egovframework.ncc.dto.FileOpLogVO;

public interface FileOpLogService {

  Long start(FileOpLogVO vo);

  void success(Long logId, Long fileSize, Long bytesSent);

  void fail(Long logId, String errMsg, Long fileSize, Long bytesSent);

  int selectListCnt(ComParam param);

  List<FileOpLogListItemDTO> selectList(ComParam param);

  FileOpLogSummaryDTO selectSummary(ComParam param);
}
