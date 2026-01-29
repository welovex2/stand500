package egovframework.cmm.service;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("FileMapper")
public interface FileMapper {

  void insertFileDetail(FileVO vo);

  void insertFileMaster(FileVO fvo);

  void updateFileDetail(FileVO fvo);

  List<FileVO> selectFileList(FileVO fvo);

  void deleteFileDetail(FileVO fvo);

  FileVO selectFileInf(FileVO fvo);

  int getMaxFileSN(FileVO fvo);

  void deleteCOMTNFILE(FileVO fvo);

  void deletePicAll(FileVO fvo);

  List<FileVO> selectFileListByFileNm(FileVO fvo);

  int selectFileListCntByFileNm(FileVO fvo);

  List<FileVO> selectImageFileList(FileVO vo);

  List<FileVO> selectFileOrdrList(FileVO fvo);

  List<FileVO> selectUploadSrcByPaths(@Param("fileNm") List<String> fileNm);

  void upsertFolderMeta(FolderMetaVO meta);

}
