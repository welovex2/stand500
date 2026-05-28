package egovframework.ncc.service;

import java.util.List;

import egovframework.cmm.service.ComParam;
import egovframework.ncc.dto.FileOpLogListItemDTO;
import egovframework.ncc.dto.FileOpLogSummaryDTO;
import egovframework.ncc.dto.FileOpLogVO;
import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("FileOpLogMapper")
public interface FileOpLogMapper {
  void insertFileOpLog(FileOpLogVO vo);

  void updateFileOpLogResult(FileOpLogVO vo);

  int selectListCnt(ComParam param);

  List<FileOpLogListItemDTO> selectList(ComParam param);

  FileOpLogSummaryDTO selectSummary(ComParam param);
}

