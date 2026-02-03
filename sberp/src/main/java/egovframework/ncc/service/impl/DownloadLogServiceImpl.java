package egovframework.ncc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import egovframework.ncc.dto.DownloadLogVO;
import egovframework.ncc.service.DownloadLogMapper;
import egovframework.ncc.service.DownloadLogService;
import lombok.extern.slf4j.Slf4j;

@Service("DownloadLogService")
@Slf4j
public class DownloadLogServiceImpl implements DownloadLogService {

  @Autowired
  private DownloadLogMapper downloadLogMapper;

  @Override
  public Long startLog(DownloadLogVO vo) {
    if (vo.getResultCd() == null)
      vo.setResultCd("START");
    if (vo.getBytesSent() == null)
      vo.setBytesSent(0L);
    downloadLogMapper.insertDownloadLog(vo);
    return vo.getLogId();
  }

  @Override
  public void success(Long logId, long bytesSent) {
    if (logId == null)
      return;
    DownloadLogVO vo = new DownloadLogVO();
    vo.setLogId(logId);
    vo.setResultCd("SUCCESS");
    vo.setErrMsg(null);
    vo.setBytesSent(bytesSent);
    downloadLogMapper.updateDownloadLogResult(vo);
  }

  @Override
  public void fail(Long logId, String errMsg, long bytesSent) {
    if (logId == null)
      return;
    DownloadLogVO vo = new DownloadLogVO();
    vo.setLogId(logId);
    vo.setResultCd("FAIL");
    vo.setErrMsg(trimErr(errMsg));
    vo.setBytesSent(bytesSent);
    downloadLogMapper.updateDownloadLogResult(vo);
  }

  private String trimErr(String s) {
    if (s == null)
      return null;
    String v = s;
    v = v.replace("\n", " ").replace("\r", " ");
    if (v.length() > 1000)
      v = v.substring(0, 1000);
    return v;
  }
}
