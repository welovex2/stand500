package egovframework.ncc.service;

import egovframework.ncc.dto.DownloadLogVO;
import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("DownloadLogMapper")
public interface DownloadLogMapper {

  void insertDownloadLog(DownloadLogVO vo);

  void updateDownloadLogResult(DownloadLogVO vo);

}
