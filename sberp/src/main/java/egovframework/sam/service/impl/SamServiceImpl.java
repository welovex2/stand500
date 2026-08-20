package egovframework.sam.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import egovframework.cmm.service.ComParam;
import egovframework.cmm.service.EgovFileMngService;
import egovframework.cmm.service.FileVO;
import egovframework.cmm.service.SbkInfoVO;
import egovframework.ncc.service.NextcloudFolderService;
import egovframework.sam.dto.ImDTO;
import egovframework.sam.dto.ImSubDTO;
import egovframework.sam.dto.ImSubItemDetailDTO;
import egovframework.sam.dto.SamUploadFolderDTO;
import egovframework.sam.service.ImSub;
import egovframework.sam.service.SamMapper;
import egovframework.sam.service.SamService;
import egovframework.sam.util.SamItemIdSupport;
import egovframework.sam.util.SamItemIdSupport.Parsed;
import egovframework.sam.util.SamUploadFolderSupport;
import egovframework.sbk.service.SbkService;

@Service("SamService")
public class SamServiceImpl implements SamService {

  @Autowired
  SamMapper samMapper;

  @Autowired
  EgovFileMngService fileMngService;

  @Autowired
  SbkService sbkService;

  @Autowired
  NextcloudFolderService nextcloudFolderService;

  @Override
  @Transactional
  public boolean insert(ImDTO req) {

    boolean result = true;

    samMapper.insert(req);
    ImDTO detail = samMapper.detail(req.getSbkId());
    if (detail != null) {
      req.setImId(detail.getImId());
    }

    // 시료리스트
    if (req.getItemList() != null) {
      List<ImSub> iItems = req.getItemList().stream().filter(t -> "I".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(iItems)) {

        for (ImSub item : iItems) {
          samMapper.insertSub(req.getInsMemId(), req.getImId(), item);
        }
      }
    }

    return result;
  }

  @Override
  @Transactional
  public boolean update(ImDTO req) {
    boolean result = true;

    samMapper.update(req);

    // 시료리스트
    if (!ObjectUtils.isEmpty(req.getItemList())) {
      List<ImSub> iItems = req.getItemList().stream().filter(t -> "I".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(iItems)) {
        for (ImSub item : iItems) {
          samMapper.insertSub(req.getInsMemId(), req.getImId(), item);
        }
      }

      List<ImSub> uItems = req.getItemList().stream().filter(t -> "U".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(uItems))
        samMapper.updateSub(req.getInsMemId(), req.getImId(), uItems);

      List<ImSub> dItems = req.getItemList().stream().filter(t -> "D".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(dItems))
        samMapper.deleteSub(req.getInsMemId(), req.getImId(), dItems);
    }
    return result;
  }


  @Override
  public ImDTO detail(String sbkId) throws Exception {

    ImDTO result = samMapper.detail(sbkId);

    if (!ObjectUtils.isEmpty(result)) {
      result.setItemList(samMapper.subList(result.getImId()));

      // 시료이미지
      FileVO fileVO = new FileVO();
      fileVO.setAtchFileId(result.getPicUrl());

      List<FileVO> picResult = fileMngService.selectImageFileList(fileVO);

      for (FileVO pic : picResult) {
        try {
          pic.setFileStreCours(fileMngService.resolveImageUrl(pic));
        } catch (Exception e) {
          pic.setFileStreCours("");
        }
      }

      result.setPicList(picResult);
    }

    return result;
  }

  @Override
  public int selectListCnt(ComParam param) {
    return samMapper.selectListCnt(param);
  }

  @Override
  public List<ImSubDTO> selectList(ComParam param) {
    List<ImSubDTO> result = samMapper.selectList(param);

    // 번호 매기기
    for (int i = 0; i < result.size(); i++) {
      result.get(i)
          .setNo(param.getTotalCount() - (((param.getPageIndex() - 1) * param.getPageUnit()) + i));
    }

    return result;
  }

  @Override
  public ImSubItemDetailDTO itemDetail(String imSubId) throws Exception {
    Parsed parsed = SamItemIdSupport.parse(imSubId);
    if (parsed == null) {
      return null;
    }

    ImSubItemDetailDTO result =
        samMapper.selectSubBySbkIdAndSubSeq(parsed.getSbkId(), parsed.getImSubSeq());
    if (result == null) {
      return null;
    }

    SbkInfoVO sbk = sbkService.findBySbkNoAndProvision(result.getSbkId());
    if (sbk == null || StringUtils.isEmpty(sbk.getNcFolderPath())) {
      return result;
    }

    result.setNcFolderPath(sbk.getNcFolderPath());
    List<SamUploadFolderDTO> uploadFolders =
        SamUploadFolderSupport.buildUploadFolders(sbk.getNcFolderPath());
    for (SamUploadFolderDTO folder : uploadFolders) {
      nextcloudFolderService.ensureFolder(folder.getRelativePath());
    }
    result.setUploadFolders(uploadFolders);
    return result;
  }

}
