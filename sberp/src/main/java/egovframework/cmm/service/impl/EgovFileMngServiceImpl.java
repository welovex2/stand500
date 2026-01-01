package egovframework.cmm.service.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import egovframework.cmm.service.EgovFileMngService;
import egovframework.cmm.service.FileMapper;
import egovframework.cmm.service.FileVO;
import egovframework.cmm.service.NextcloudDavService;
import egovframework.rte.fdl.cmmn.EgovAbstractServiceImpl;
import lombok.extern.slf4j.Slf4j;

/**
 * @Class Name : EgovFileMngServiceImpl.java
 * @Description : 파일정보의 관리를 위한 구현 클래스
 * @Modification Information
 *
 *    수정일       수정자         수정내용
 *    -------        -------     -------------------
 *    2009. 3. 25.     이삼섭    최초생성
 *
 * @author 공통 서비스 개발팀 이삼섭
 * @since 2009. 3. 25.
 * @version
 * @see
 *
 */
@Service("EgovFileMngService")
@Slf4j
public class EgovFileMngServiceImpl extends EgovAbstractServiceImpl implements EgovFileMngService {

	@Autowired
	private FileMapper fileMapper;
	
    @Autowired
    private NextcloudDavService nextcloudDavService;

    /**
     * 여러 개의 파일을 삭제한다.
     *
     * @see egovframework.com.cmm.service.EgovFileMngService#deleteFileInfs(java.util.List)
     */
    @Override
    public void deleteFileInfs(List<?> fileList) throws Exception {
		Iterator<?> iter = fileList.iterator();
		FileVO vo;
		while (iter.hasNext()) {
			vo = (FileVO) iter.next();

			fileMapper.insertFileDetail(vo);
		}
    	
    }

    /**
     * 하나의 파일에 대한 정보(속성 및 상세)를 등록한다.
     *
     * @see egovframework.com.cmm.service.EgovFileMngService#insertFileInf(egovframework.com.cmm.service.FileVO)
     */
    @Override
	public String insertFileInf(FileVO fvo) throws Exception {
    	
    	String atchFileId = fvo.getAtchFileId();

		fileMapper.insertFileMaster(fvo);
		fileMapper.insertFileDetail(fvo);

		return atchFileId;
    }

    /**
     * 여러 개의 파일에 대한 정보(속성 및 상세)를 등록한다.
     *
     * @see egovframework.com.cmm.service.EgovFileMngService#insertFileInfs(java.util.List)
     */
    @Override
    public String insertFileInfs(List<?> fileList) throws Exception {
		String atchFileId = "";
	
		if (fileList.size() != 0) {
			
			FileVO vo = (FileVO) fileList.get(0);
			atchFileId = vo.getAtchFileId();

			fileMapper.insertFileMaster(vo);

			Iterator<?> iter = fileList.iterator();
			while (iter.hasNext()) {
				vo = (FileVO) iter.next();

				fileMapper.insertFileDetail(vo);
			}
			
		}
		if(atchFileId == ""){
			atchFileId = null;
		}
		return atchFileId;
    }

    /**
     * 파일에 대한 목록을 조회한다.
     *
     * @see egovframework.com.cmm.service.EgovFileMngService#selectFileInfs(egovframework.com.cmm.service.FileVO)
     */
    @Override
	public List<FileVO> selectFileInfs(FileVO fvo) throws Exception {
    	return fileMapper.selectFileList(fvo);
    }
    
    /**
     * 파일에 대한 목록을 조회한다.
     *
     * @see egovframework.com.cmm.service.EgovFileMngService#selectFileInfs(egovframework.com.cmm.service.FileVO)
     */
    @Override
    public List<FileVO> selectFileOrdrInfs(FileVO fvo) throws Exception {
        return fileMapper.selectFileOrdrList(fvo);
    }
    

    /**
     * 여러 개의 파일에 대한 정보(속성 및 상세)를 수정한다.
     *
     * @see egovframework.com.cmm.service.EgovFileMngService#updateFileInfs(java.util.List)
     */
    @Override
	public void updateFileInfs(List<?> fileList) throws Exception {
    	//Delete & Insert
    	FileVO vo;
		Iterator<?> iter = fileList.iterator();
		while (iter.hasNext()) {
			vo = (FileVO) iter.next();

			fileMapper.insertFileDetail(vo);
		}
    }

    
    /**
     * 하나의 파일을 수정한다.
     *
     * @see egovframework.com.cmm.service.EgovFileMngService#deleteFileInf(egovframework.com.cmm.service.FileVO)
     */
    @Override
	public void updateFileDetail(FileVO fvo) throws Exception {
    	fileMapper.updateFileDetail(fvo);
    }
    
    /**
     * 하나의 파일을 삭제한다.
     *
     * @see egovframework.com.cmm.service.EgovFileMngService#deleteFileInf(egovframework.com.cmm.service.FileVO)
     */
    @Override
	public void deleteFileInf(FileVO fvo) throws Exception {
        
      FileVO file = fileMapper.selectFileInf(fvo);
      if (file == null) return;
      
      // 2) 외부 파일 삭제는 실패해도 DB 삭제는 진행
      try {
          if ("NEXTCLOUD_DAV".equals(file.getFileStreCours())) {
              nextcloudDavService.deleteByDavPath(file.getStreFileNm());
          }
      } catch (Exception e) {
          // 실패해도 업무 흐름은 유지
          log.warn("외부 파일 삭제 실패(무시하고 DB 삭제 진행). atchFileId={}, fileSn={}, path={}",
                   file.getAtchFileId(), file.getFileSn(), file.getStreFileNm(), e);

          // (선택) 삭제 실패를 DB에 기록하고 싶으면 여기서 업데이트 한 번 더
          // fileDAO.updateDeleteFailInfo(file.getAtchFileId(), file.getFileSn(), e.getMessage());
      }
    
      fileMapper.deleteFileDetail(fvo);
    }

    /**
     * 파일에 대한 상세정보를 조회한다.
     *
     * @see egovframework.com.cmm.service.EgovFileMngService#selectFileInf(egovframework.com.cmm.service.FileVO)
     */
    @Override
	public FileVO selectFileInf(FileVO fvo) throws Exception {
    	return fileMapper.selectFileInf(fvo);
    }

    /**
     * 파일 구분자에 대한 최대값을 구한다.
     *
     * @see egovframework.com.cmm.service.EgovFileMngService#getMaxFileSN(egovframework.com.cmm.service.FileVO)
     */
    @Override
	public int getMaxFileSN(FileVO fvo) throws Exception {
    	return fileMapper.getMaxFileSN(fvo);
    }

    /**
     * 전체 파일을 삭제한다.
     *
     * @see egovframework.com.cmm.service.EgovFileMngService#deleteAllFileInf(egovframework.com.cmm.service.FileVO)
     */
    @Override
	public void deleteAllFileInf(FileVO fvo) throws Exception {
    	fileMapper.deleteCOMTNFILE(fvo);
    }
    @Override
	public void deletePicAll(FileVO fvo) throws Exception {
    	fileMapper.deletePicAll(fvo);
    }
    
    /**
     * 파일명 검색에 대한 목록을 조회한다.
     *
     * @see egovframework.com.cmm.service.EgovFileMngService#selectFileListByFileNm(egovframework.com.cmm.service.FileVO)
     */
    @Override
	public Map<String, Object> selectFileListByFileNm(FileVO fvo) throws Exception {
		List<FileVO>  result = fileMapper.selectFileListByFileNm(fvo);
		int cnt = fileMapper.selectFileListCntByFileNm(fvo);
	
		Map<String, Object> map = new HashMap<String, Object>();
	
		map.put("resultList", result);
		map.put("resultCnt", Integer.toString(cnt));
	
		return map;
    }

    /**
     * 이미지 파일에 대한 목록을 조회한다.
     *
     * @see egovframework.com.cmm.service.EgovFileMngService#selectImageFileList(egovframework.com.cmm.service.FileVO)
     */
    @Override
	public List<FileVO> selectImageFileList(FileVO vo) throws Exception {
    	return fileMapper.selectImageFileList(vo);
    }
}
