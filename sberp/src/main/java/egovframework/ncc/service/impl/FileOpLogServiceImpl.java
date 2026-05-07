package egovframework.ncc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import egovframework.ncc.dto.FileOpLogVO;
import egovframework.ncc.service.FileOpLogMapper;
import egovframework.ncc.service.FileOpLogService;

@Service("FileOpLogService")
public class FileOpLogServiceImpl implements FileOpLogService {

  @Autowired
  private FileOpLogMapper fileOpLogMapper;

  @Override
  public Long start(FileOpLogVO vo) {
    if (vo == null) {
      return null;
    }
    if (vo.getResultCd() == null) {
      vo.setResultCd("START");
    }
    if (vo.getBytesSent() == null) {
      vo.setBytesSent(0L);
    }
    if (vo.getFileSize() == null) {
      vo.setFileSize(0L);
    }
    fileOpLogMapper.insertFileOpLog(vo);
    return vo.getLogId();
  }

  @Override
  public void success(Long logId, Long fileSize, Long bytesSent) {
    if (logId == null) {
      return;
    }
    FileOpLogVO vo = new FileOpLogVO();
    vo.setLogId(logId);
    vo.setResultCd("SUCCESS");
    vo.setErrMsg(null);
    if (fileSize != null) {
      vo.setFileSize(fileSize);
    }
    if (bytesSent != null) {
      vo.setBytesSent(bytesSent);
    }
    fileOpLogMapper.updateFileOpLogResult(vo);
  }

  @Override
  public void fail(Long logId, String errMsg, Long fileSize, Long bytesSent) {
    if (logId == null) {
      return;
    }
    FileOpLogVO vo = new FileOpLogVO();
    vo.setLogId(logId);
    vo.setResultCd("FAIL");
    vo.setErrMsg(trimErr(errMsg));
    if (fileSize != null) {
      vo.setFileSize(fileSize);
    }
    if (bytesSent != null) {
      vo.setBytesSent(bytesSent);
    }
    fileOpLogMapper.updateFileOpLogResult(vo);
  }

  private String trimErr(String s) {
    if (s == null) {
      return null;
    }
    String v = s.replace("\n", " ").replace("\r", " ");
    if (v.length() > 1000) {
      v = v.substring(0, 1000);
    }
    return v;
  }
}

