package egovframework.cmm.service;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import egovframework.cmm.service.FileVO;
import egovframework.ncc.dto.FileDetailUpdateVO;
import egovframework.ncc.dto.FolderMetaVO;
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

  int insertFolderMeta(@Param("vo") FolderMetaVO vo);

  int updateFolderMetaByPathHash(@Param("oldPathHash") String oldPathHash,
      @Param("vo") FolderMetaVO vo);

  int updateFolderMetaByFolderPath(@Param("oldFolderPath") String oldFolderPath,
      @Param("vo") FolderMetaVO vo);

  int updateDescendantsByPrefix(@Param("oldPrefix") String oldPrefix, @Param("vo") FolderMetaVO vo);

  List<FolderMetaVO> selectFolderUploadSrcByPaths(List<String> folderPaths);

  int updateFileDetailByStreFileNm(FileDetailUpdateVO vo);

  int updateFileDetailByFolderRename(FileDetailUpdateVO vo);

  int markFileDetailDeletedByExactPath(@Param("streFileNm") String streFileNm,
      @Param("updtId") String updtId);

  int markFileDetailDeletedByPathPrefix(@Param("folderPath") String folderPath,
      @Param("updtId") String updtId);

  int deleteFolderMetaByPathPrefix(@Param("folderPath") String folderPath,
      @Param("updtId") String updtId);


}
