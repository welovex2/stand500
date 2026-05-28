package egovframework.ncc.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import egovframework.cmm.service.ComParam;
import egovframework.cmm.service.SearchVO;
import egovframework.ncc.dto.FileOpLogListItemDTO;
import egovframework.ncc.dto.FileOpLogSummaryDTO;
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

  @Override
  public int selectListCnt(ComParam param) {
    prepareSearchParam(param);
    return fileOpLogMapper.selectListCnt(param);
  }

  @Override
  public List<FileOpLogListItemDTO> selectList(ComParam param) {
    prepareSearchParam(param);
    List<FileOpLogListItemDTO> list = fileOpLogMapper.selectList(param);
    if (list != null) {
      for (FileOpLogListItemDTO row : list) {
        row.setFileSizeLabel(formatFileSize(row.getFileSize(), row.getItemType()));
      }
    }
    return list;
  }

  @Override
  public FileOpLogSummaryDTO selectSummary(ComParam param) {
    prepareSearchParam(param);
    FileOpLogSummaryDTO summary = fileOpLogMapper.selectSummary(param);
    if (summary == null) {
      summary = new FileOpLogSummaryDTO();
      summary.setTotalBytes(0L);
      summary.setTotalFileCount(0L);
      summary.setTotalFolderCount(0L);
      summary.setTotalCapacityGb(0.0);
      return summary;
    }
    long bytes = summary.getTotalBytes() == null ? 0L : summary.getTotalBytes();
    summary.setTotalCapacityGb(Math.round(bytes / (1024.0 * 1024.0 * 1024.0) * 100.0) / 100.0);
    return summary;
  }

  private void prepareSearchParam(ComParam param) {
    if (param == null) {
      return;
    }
    if (param.getSearchVO() == null) {
      param.setSearchVO(new ArrayList<SearchVO>());
    }
    if (!hasSearchCode(param.getSearchVO(), "92")) {
      SearchVO resultFilter = new SearchVO();
      resultFilter.setSearchCode("92");
      resultFilter.setSearchWord("SUCCESS");
      param.getSearchVO().add(resultFilter);
    }
  }

  private boolean hasSearchCode(List<SearchVO> searchVO, String code) {
    for (SearchVO item : searchVO) {
      if (item != null && code.equals(item.getSearchCode())) {
        return true;
      }
    }
    return false;
  }

  private String formatFileSize(Long bytes, String itemType) {
    if ("폴더".equals(itemType)) {
      return "-";
    }
    if (bytes == null || bytes <= 0L) {
      return "-";
    }
    double b = bytes.doubleValue();
    if (b >= 1024.0 * 1024.0 * 1024.0) {
      return String.format("%.2fGB", b / (1024.0 * 1024.0 * 1024.0));
    }
    if (b >= 1024.0 * 1024.0) {
      return String.format("%.1fMB", b / (1024.0 * 1024.0));
    }
    if (b >= 1024.0) {
      return String.format("%.1fKB", b / 1024.0);
    }
    return bytes + "B";
  }
}
