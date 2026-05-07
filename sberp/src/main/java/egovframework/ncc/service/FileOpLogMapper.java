 package egovframework.ncc.service;

import egovframework.ncc.dto.FileOpLogVO;
import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("FileOpLogMapper")
public interface FileOpLogMapper {
  void insertFileOpLog(FileOpLogVO vo);

  void updateFileOpLogResult(FileOpLogVO vo);
}

