package egovframework.raw.service.impl;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
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
import egovframework.cmm.service.HisDTO;
import egovframework.raw.dto.CeDTO;
import egovframework.raw.dto.ClkDTO;
import egovframework.raw.dto.CsDTO;
import egovframework.raw.dto.CtiDTO;
import egovframework.raw.dto.DpDTO;
import egovframework.raw.dto.EftDTO;
import egovframework.raw.dto.EsdDTO;
import egovframework.raw.dto.FileRawDTO;
import egovframework.raw.dto.ImgDTO;
import egovframework.raw.dto.InfoDTO;
import egovframework.raw.dto.MfDTO;
import egovframework.raw.dto.PicDTO;
import egovframework.raw.dto.RawSearchDTO;
import egovframework.raw.dto.ReDTO;
import egovframework.raw.dto.ReportDTO;
import egovframework.raw.dto.RsDTO;
import egovframework.raw.dto.SurgeDTO;
import egovframework.raw.dto.TableType;
import egovframework.raw.dto.TelDTO;
import egovframework.raw.dto.VdipDTO;
import egovframework.raw.service.FileRaw;
import egovframework.raw.service.MethodCsSub;
import egovframework.raw.service.MethodCtiSub;
import egovframework.raw.service.MethodEftSub;
import egovframework.raw.service.MethodEsdSub;
import egovframework.raw.service.MethodMapper;
import egovframework.raw.service.MethodRsSub;
import egovframework.raw.service.MethodSurgeSub;
import egovframework.raw.service.RawAsstn;
import egovframework.raw.service.RawCable;
import egovframework.raw.service.RawData;
import egovframework.raw.service.RawMac;
import egovframework.raw.service.RawMapper;
import egovframework.raw.service.RawMet;
import egovframework.raw.service.RawService;
import egovframework.raw.service.RawSpec;
import egovframework.raw.service.RawSys;
import egovframework.raw.service.RawTchn;
import egovframework.rte.fdl.property.EgovPropertyService;
import egovframework.tst.service.Test;
import lombok.extern.slf4j.Slf4j;

@Service("RawService")
@Slf4j
public class RawServiceImpl implements RawService {

  @Autowired
  RawMapper rawMapper;

  @Autowired
  MethodMapper methodMapper;

  @Autowired
  EgovFileMngService fileMngService;

  @Autowired
  EgovPropertyService propertyService;

  
  @Override
  @Transactional
  public boolean insert(RawData req) {
    boolean result = true;

    req.setSbkId(rawMapper.getSbkId(req.getTestSeq()));
    rawMapper.insert(req);

    // 성적서 발급정보 TEST_TB에 저장
    rawMapper.updateReport(req);
    
    // 4-2. method (시험방법), 고정리스트
    if (!ObjectUtils.isEmpty(req.getMethodList())) rawMapper.insertMethod(req.getRawSeq(), req.getMethodList());

    // 기술제원
    if (!ObjectUtils.isEmpty(req.getRawSpecList())) {
      List<RawSpec> pIItems = req.getRawSpecList().stream().filter(t -> "I".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(pIItems))
        rawMapper.insertSpec(req.getRawSeq(), pIItems);
    }

    // 시험기기 전체구성
    if (!ObjectUtils.isEmpty(req.getRawAsstnList())) {
      List<RawAsstn> aIItems = req.getRawAsstnList().stream().filter(t -> "I".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(aIItems))
        rawMapper.insertAsstn(req.getRawSeq(), aIItems);
    }
    
    // 시스템구성
    if (!ObjectUtils.isEmpty(req.getRawSysList())) {
      List<RawSys> sIItems = req.getRawSysList().stream().filter(t -> "I".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(sIItems))
        rawMapper.insertSys(req.getRawSeq(), sIItems);
    }
    
    // 접속케이블
    if (!ObjectUtils.isEmpty(req.getRawCableList())) {
      List<RawCable> cIItems = req.getRawCableList().stream().filter(t -> "I".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(cIItems))
        rawMapper.insertCable(req.getRawSeq(), cIItems);
    }
    
    return result;
  }

  @Override
  @Transactional
  public boolean update(RawData req) {
    boolean result = true;

    rawMapper.update(req);

    // 성적서 발급정보 TEST_TB에 저장
    rawMapper.updateReport(req);
    
    // 4-2. method (시험방법), 고정리스트
    if (!ObjectUtils.isEmpty(req.getMethodList())) rawMapper.insertMethod(req.getRawSeq(), req.getMethodList());

    // 기술제원
    if (!ObjectUtils.isEmpty(req.getRawSpecList())) {
      List<RawSpec> pIItems = req.getRawSpecList().stream().filter(t -> "I".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(pIItems))
        rawMapper.insertSpec(req.getRawSeq(), pIItems);
  
      List<RawSpec> pUItems = req.getRawSpecList().stream().filter(t -> "U".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(pUItems))
        rawMapper.updateSpec(req.getRawSeq(), pUItems);
  
      List<RawSpec> pDItems = req.getRawSpecList().stream().filter(t -> "D".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(pDItems))
        rawMapper.deleteSpec(req.getRawSeq(), pDItems);
    }


    // 시험기기 전체구성
    if (!ObjectUtils.isEmpty(req.getRawAsstnList())) {
      List<RawAsstn> aIItems = req.getRawAsstnList().stream().filter(t -> "I".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(aIItems))
        rawMapper.insertAsstn(req.getRawSeq(), aIItems);
  
      List<RawAsstn> aUItems = req.getRawAsstnList().stream().filter(t -> "U".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(aUItems))
        rawMapper.updateAsstn(req.getRawSeq(), aUItems);
  
      List<RawAsstn> aDItems = req.getRawAsstnList().stream().filter(t -> "D".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(aDItems))
        rawMapper.deleteAsstn(req.getRawSeq(), aDItems);
    }
    
    
    // 시스템구성
    if (!ObjectUtils.isEmpty(req.getRawSysList())) {
      List<RawSys> sIItems = req.getRawSysList().stream().filter(t -> "I".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(sIItems))
        rawMapper.insertSys(req.getRawSeq(), sIItems);
  
      List<RawSys> sUItems = req.getRawSysList().stream().filter(t -> "U".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(sUItems))
        rawMapper.updateSys(req.getRawSeq(), sUItems);
  
      List<RawSys> sDItems = req.getRawSysList().stream().filter(t -> "D".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(sDItems))
        rawMapper.deleteSys(req.getRawSeq(), sDItems);
    }
    
    // 접속케이블
    if (!ObjectUtils.isEmpty(req.getRawCableList())) {
      List<RawCable> cIItems = req.getRawCableList().stream().filter(t -> "I".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(cIItems))
        rawMapper.insertCable(req.getRawSeq(), cIItems);
  
      List<RawCable> cUItems = req.getRawCableList().stream().filter(t -> "U".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(cUItems))
        rawMapper.updateCable(req.getRawSeq(), cUItems);
  
      List<RawCable> cDItems = req.getRawCableList().stream().filter(t -> "D".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(cDItems))
        rawMapper.deleteCable(req.getRawSeq(), cDItems);
    }
    
    return result;
  }

  @Override
  @Transactional
  public boolean insertCe(CeDTO req) {
    boolean result = true;
    boolean isNew = isNew(TableType.CE, req.getRawSeq());

    // 기본로데이터가 없을때 로데이터 먼저 등록
    if (req.getRawSeq() == 0)
      req.setRawSeq(beforeRawInsert(req.getTestSeq(), req.getInsMemId()));

    methodMapper.insertCe(req);

    // 측정설비
    if (!ObjectUtils.isEmpty(req.getMacList())) {
      insertMac(req.getMacList(), req.getRawSeq(), req.getMacType(), req.getMacResetYn());
    
      // 교정일 체크 (신규저장시에만 체크)
      if (isNew)
        valMacReformDate(req.getMsrmnYear(), req.getMsrmnMon(), req.getMsrmnDay(), req.getMacList(), req.getRawSeq(), req.getMacType());
    }
    
    return result;
  }

  @Override
  public CeDTO ceDetail(int rawSeq) {
    CeDTO detail = methodMapper.ceDetail(rawSeq);

    if (detail != null) {
      // 측정설비
      detail.setMacList(macList("CA", rawSeq));
      detail.getMacList().addAll(macList("CB", rawSeq));
    }

    return detail;
  }
  
  @Override
  @Transactional
  public boolean insertRe(ReDTO req) {
    boolean result = true;
    boolean isNew = isNew(TableType.RE, req.getRawSeq());
    
    // 기본로데이터가 없을때 로데이터 먼저 등록
    if (req.getRawSeq() == 0)
      req.setRawSeq(beforeRawInsert(req.getTestSeq(), req.getInsMemId()));
    
    methodMapper.insertRe(req);
    
    // 측정설비
    if (!ObjectUtils.isEmpty(req.getMacList())) {
      
      // 규격 3235은 예외처리
      // 규격 3235는 REA, REB가 한페이지에 있어 머신타입이 MacList안에 있음
      if (!StringUtils.isEmpty(req.getHz1ResultCode()) && !StringUtils.isEmpty(req.getHz2ResultCode())) {
        
        if (req.getMacResetYn() == 1) {
          methodMapper.deleteMac(req.getRawSeq(), "RA");
          methodMapper.deleteMac(req.getRawSeq(), "RB");
        }
        
        // 신규
        if (req.getMacList().stream().anyMatch(mac -> mac.getRawMacSeq() == 0))
          methodMapper.insertTwoMac(req.getRawSeq(), req.getMacList());
        // 수정
        else
          methodMapper.updateTwoMac(req.getRawSeq(), req.getMacList());
        
        // 교정일 체크 (신규저장시에만 체크)
        if (isNew) {
          valMacReformDate(req.getHz1MsrmnYear(), req.getHz1MsrmnMon(), req.getHz1MsrmnDay(), req.getMacList(), req.getRawSeq(), "RA");
          valMacReformDate(req.getHz2MsrmnYear(), req.getHz2MsrmnMon(), req.getHz2MsrmnDay(), req.getMacList(), req.getRawSeq(), "RB");
        }
        
      } else {
        
        insertMac(req.getMacList(), req.getRawSeq(), req.getMacType(), req.getMacResetYn());
        
        // 교정일 체크 (신규저장시에만 체크)
        if (isNew) {
          
          int[] date = extractMeasurementDate(req);
          int year = date[0], mon = date[1], day = date[2];
          
          valMacReformDate(year, mon, day, req.getMacList(), req.getRawSeq(), req.getMacType());
        }
      }
      
    }
    
    return result;
  }

  @Override
  public ReDTO reDetail(int rawSeq) {
    ReDTO detail = methodMapper.reDetail(rawSeq);

    if (detail != null) {
      // 측정설비
      // 3235
      if (detail.getTestStndrSeq() == 10 || detail.getTestStndrSeq() == 571 || detail.getTestStndrSeq() == 14) {
        detail.setMacList(macList("RA", rawSeq));
        detail.getMacList().addAll(macList("RB", rawSeq));
      } 
      // 9814
      else {
        detail.setMacList(new ArrayList<RawMac>());
        detail.getMacList().addAll(macList("RE1", rawSeq));
        detail.getMacList().addAll(macList("RE2", rawSeq));
        detail.getMacList().addAll(macList("RE3", rawSeq));
      }
    }
    return detail;
  }

  @Override
  @Transactional
  public boolean insertEsd(EsdDTO req) {
    boolean result = true;
    boolean isNew = isNew(TableType.ESD, req.getRawSeq());
    
    // 기본로데이터가 없을때 로데이터 먼저 등록
    if (req.getRawSeq() == 0)
      req.setRawSeq(beforeRawInsert(req.getTestSeq(), req.getInsMemId()));

    methodMapper.insertEsd(req);

    // 측정설비
    if (!ObjectUtils.isEmpty(req.getMacList())) {
      insertMac(req.getMacList(), req.getRawSeq(), req.getMacType(), req.getMacResetYn());
    
      // 교정일 체크 (신규저장시에만 체크)
      if (isNew)
        valMacReformDate(req.getMsrmnYear(), req.getMsrmnMon(), req.getMsrmnDay(), req.getMacList(), req.getRawSeq(), req.getMacType());
    }
    // 시험결과 > 직접인가
    if (req.getSubList() != null) {
      List<MethodEsdSub> cIItems = req.getSubList().stream().filter(t -> "I".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(cIItems))
        methodMapper.insertEsdSub(req.getEsdSeq(), cIItems);

      List<MethodEsdSub> cUItems = req.getSubList().stream().filter(t -> "U".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(cUItems))
        methodMapper.updateEsdSub(req.getEsdSeq(), cUItems);

      List<MethodEsdSub> cDItems = req.getSubList().stream().filter(t -> "D".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(cDItems))
        methodMapper.deleteEsdSub(req.getEsdSeq(), cDItems);
    }

    return result;
  }

  @Override
  public EsdDTO esdDetail(int rawSeq) throws Exception {
    EsdDTO detail = methodMapper.esdDetail(rawSeq);

    if (detail != null) {
      // 측정설비
      detail.setMacList(macList("ED", rawSeq));
      // 직접인가
      detail.setSubList(esdSubList(detail.getEsdSeq()));

      // 정전기 방전 인가부위 - 타이틀&파일 리스트
      FileVO fileVO = new FileVO();
      fileVO.setAtchFileId(detail.getImgUrl());
      List<FileVO> setupReulst = fileMngService.selectImageFileList(fileVO);
      List<PicDTO> setupList = new ArrayList<PicDTO>();
      if (setupReulst != null) {
        for (FileVO item : setupReulst) {
          PicDTO map = new PicDTO();
          map.setTitle(item.getFileCn());
          map.setFileSn(item.getFileSn());
          
//          if ("CDN".contentEquals(item.getFileLoc())) {
//            map.setImageUrl(propertyService.getString("cdn.url").concat(item.getFileStreCours()).concat("/")
//                .concat(item.getStreFileNm()).concat(".").concat(item.getFileExtsn()));
//          } else {
            map.setImageUrl(propertyService.getString("img.url").concat(detail.getImgUrl())
                .concat("&fileSn=").concat(item.getFileSn()));
//          }
          
          setupList.add(map);

        }
      }
      detail.setImgList(setupList);

    }

    return detail;
  }

  @Override
  public int getTestSeq(String testId) {
    return rawMapper.getTestSeq(testId);
  }
  
  @Override
  public RawData basicDetail(RawSearchDTO req) throws Exception {
    
    RawData detail = new RawData();
    detail = rawMapper.detail(req);
    
    if (detail != null) {
      // 4-2. method (시험방법)
      detail.setMethodList(methodList(detail.getRawSeq()));
    }
    
    return detail;
  }
  
  @Override
  public RawData detail(RawSearchDTO req) throws Exception {
    
    RawData detail = new RawData();
    detail = rawMapper.detail(req);
    
    
    if (detail != null) {
      // 성적서 발급내역 리스트 가져오기
      detail.setReportList(rawMapper.reportDetail(req.getTestSeq()));
      
      /* 세부데이터 추가로 가지고 오기 */
      // 4-1. Technical Requirements (기술적 요구항목)
      detail.setRawTchnList(tchnList(detail.getRawSeq()));
      // 4-2. method (시험방법)
      detail.setMethodList(methodList(detail.getRawSeq()));
      // 6. Technical specifications (기술제원)
      detail.setRawSpecList(specList(detail.getRawSeq()));
      // 8. EUT Modifications (보완사항) - 파일리스트
      FileVO fileVO = new FileVO();
      fileVO.setAtchFileId(detail.getModUrl());
      List<FileVO> modResult = fileMngService.selectImageFileList(fileVO);
      // detail.setModFileList(modReulst.stream().map(FileVO::getFileSn).collect(Collectors.toList()));
      List<PicDTO> modList = new ArrayList<PicDTO>();
      if (modResult != null) {
        for (FileVO item : modResult) {
          PicDTO map = new PicDTO();
          
//          if ("CDN".contentEquals(item.getFileLoc())) {
//            map.setImageUrl(propertyService.getString("cdn.url").concat(item.getFileStreCours()).concat("/")
//                .concat(item.getStreFileNm()).concat(".").concat(item.getFileExtsn()));
//          } else {
            map.setImageUrl(propertyService.getString("img.url").concat(detail.getModUrl()).concat("&fileSn=")
                .concat(item.getFileSn()));
//          }
          map.setFileSn(item.getFileSn());
          
          modList.add(map);
        }
      }
      detail.setModFileList(modList);
      // 9. Assistance Device and Cable(시험기기 전체구성)
      detail.setRawAsstnList(asstnList(detail.getRawSeq()));
      // 10. System Configuration (시스템구성)
      detail.setRawSysList(sysList(detail.getRawSeq()));
      // 11. Type of Cable Used (접속 케이블)
      detail.setRawCableList(cableList(detail.getRawSeq()));
      // 14. Test Set-up Configuraiotn for EUT - 타이틀&파일 리스트
      fileVO = new FileVO();
      fileVO.setAtchFileId(detail.getSetupUrl());
      List<FileVO> setupReulst = fileMngService.selectImageFileList(fileVO);
      List<PicDTO> setupList = new ArrayList<PicDTO>();
      if (setupReulst != null) {
        for (FileVO item : setupReulst) {
          PicDTO map = new PicDTO();
          map.setTitle(item.getFileCn());
          
//          if ("CDN".contentEquals(item.getFileLoc())) {
//            map.setImageUrl(propertyService.getString("cdn.url").concat(item.getFileStreCours()).concat("/")
//                .concat(item.getStreFileNm()).concat(".").concat(item.getFileExtsn()));
//          } else {
            map.setImageUrl(propertyService.getString("img.url").concat(detail.getSetupUrl())
                .concat("&fileSn=").concat(item.getFileSn()));
//          }
          map.setFileSn(item.getFileSn());
          
          setupList.add(map);

        }
      }
      detail.setSetupList(setupList);

    }
  
    return detail;
  }

  @Override
  @Transactional
  public boolean insertRs(RsDTO req) {
    boolean result = true;
    boolean isNew = isNew(TableType.RS, req.getRawSeq());
    
    // 기본로데이터가 없을때 로데이터 먼저 등록
    if (req.getRawSeq() == 0)
      req.setRawSeq(beforeRawInsert(req.getTestSeq(), req.getInsMemId()));

    methodMapper.insertRs(req);

    // 측정설비
    if (!ObjectUtils.isEmpty(req.getMacList())) {
      insertMac(req.getMacList(), req.getRawSeq(), req.getMacType(), req.getMacResetYn());
      
      // 교정일 체크 (신규저장시에만 체크)
      if (isNew)
        valMacReformDate(req.getMsrmnYear(), req.getMsrmnMon(), req.getMsrmnDay(), req.getMacList(), req.getRawSeq(), req.getMacType());
    }

    // 시험결과 > 인가부위
    if (!ObjectUtils.isEmpty(req.getSubList())) {
      
      List<MethodRsSub> cIItems = req.getSubList().stream().filter(t -> "I".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(cIItems))
        methodMapper.insertRsSub(req.getRsSeq(), cIItems);
  
      List<MethodRsSub> cUItems =
          req.getSubList().stream().filter(t -> "U".equals(t.getState())).map(state -> {
            state.setState(state.getNoYn() == 1 ? "D" : "U");
            return state;
          }).collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(cUItems))
        methodMapper.updateRsSub(req.getRsSeq(), cUItems);
  
      List<MethodRsSub> cDItems = req.getSubList().stream().filter(t -> "D".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(cDItems))
        methodMapper.updateRsSub(req.getRsSeq(), cDItems);
  
      // 해당없음일 경우에 기존 데이터 날리기(appl_type이 같은것들)
      for (MethodRsSub nodata : cIItems) {
        if (nodata.getNoYn() == 1) {
          methodMapper.deleteRsSub(req.getRsSeq(), nodata);
        }
      }
    }
    
    return result;
  }
  
  @Override
  public RsDTO rsDetail(int rawSeq) {
    RsDTO detail = methodMapper.rsDetail(rawSeq);
    
    if (detail != null) {
      // 측정설비
      detail.setMacList(macList("RS", rawSeq));
      // 시험결과 > 인가부위
      detail.setSubList(methodMapper.rsSubList(detail.getRsSeq()));
    }
    
    return detail;
  }

  @Override
  @Transactional
  public boolean insertEft(EftDTO req) {
    boolean result = true;
    boolean isNew = isNew(TableType.EFT, req.getRawSeq());
    
    // 기본로데이터가 없을때 로데이터 먼저 등록
    if (req.getRawSeq() == 0)
      req.setRawSeq(beforeRawInsert(req.getTestSeq(), req.getInsMemId()));

    methodMapper.insertEft(req);

    // 측정설비
    if (!ObjectUtils.isEmpty(req.getMacList())) {
      insertMac(req.getMacList(), req.getRawSeq(), req.getMacType(), req.getMacResetYn());
      
      // 교정일 체크 (신규저장시에만 체크)
      if (isNew)
        valMacReformDate(req.getMsrmnYear(), req.getMsrmnMon(), req.getMsrmnDay(), req.getMacList(), req.getRawSeq(), req.getMacType());
    }

    // 시험결과 > 포트
    List<MethodEftSub> cIItems = req.getSubList().stream().filter(t -> "I".equals(t.getState()))
        .collect(Collectors.toList());
    if (!ObjectUtils.isEmpty(cIItems))
      methodMapper.insertEftSub(req.getEftSeq(), cIItems);

    List<MethodEftSub> cUItems =
        req.getSubList().stream().filter(t -> "U".equals(t.getState())).map(state -> {
          state.setState(state.getNoYn() == 1 ? "D" : "U");
          return state;
        }).collect(Collectors.toList());
    if (!ObjectUtils.isEmpty(cUItems))
      methodMapper.updateEftSub(req.getEftSeq(), cUItems);

    List<MethodEftSub> cDItems = req.getSubList().stream().filter(t -> "D".equals(t.getState()))
        .collect(Collectors.toList());
    if (!ObjectUtils.isEmpty(cDItems))
      methodMapper.updateEftSub(req.getEftSeq(), cDItems);

    // 해당없음일 경우에 기존 데이터 날리기(appl_type이 같은것들)
    for (MethodEftSub nodata : cIItems) {
      if (nodata.getNoYn() == 1) {
        methodMapper.deleteEftSub(req.getEftSeq(), nodata);
      }
    }
    return result;
  }
  
  @Override
  public EftDTO eftDetail(int rawSeq) {
    EftDTO detail = methodMapper.eftDetail(rawSeq);
    
    if (detail != null) {
      // 측정설비
      detail.setMacList(macList("ET", rawSeq));
      // 시험결과 > 포트
      detail.setSubList(methodMapper.eftSubList(detail.getEftSeq()));
    }
    return detail;
  }

  public int beforeRawInsert(int testSeq, String insMemId) {

    RawData req = new RawData();
    req.setTestSeq(testSeq);
    req.setInsMemId(insMemId);
    req.setUdtMemId(insMemId);
    req.setSbkId(rawMapper.getSbkId(testSeq));
    rawMapper.insert(req);

    return req.getRawSeq();
  }

  @Override
  @Transactional
  public boolean insertSurge(SurgeDTO req) {

    boolean result = true;
    boolean isNew = isNew(TableType.SURGE, req.getRawSeq());
    
    // 기본로데이터가 없을때 로데이터 먼저 등록
    if (req.getRawSeq() == 0)
      req.setRawSeq(beforeRawInsert(req.getTestSeq(), req.getInsMemId()));

    methodMapper.insertSurge(req);

    // 측정설비
    if (!ObjectUtils.isEmpty(req.getMacList())) {
      insertMac(req.getMacList(), req.getRawSeq(), req.getMacType(), req.getMacResetYn());
      
      // 교정일 체크 (신규저장시에만 체크)
      if (isNew)
        valMacReformDate(req.getMsrmnYear(), req.getMsrmnMon(), req.getMsrmnDay(), req.getMacList(), req.getRawSeq(), req.getMacType());
    }

    // 시험결과 > 포트
    List<MethodSurgeSub> cIItems = req.getSubList().stream().filter(t -> "I".equals(t.getState()))
        .collect(Collectors.toList());
    if (!ObjectUtils.isEmpty(cIItems))
      methodMapper.insertSurgeSub(req.getSurgeSeq(), cIItems);

    List<MethodSurgeSub> cUItems =
        req.getSubList().stream().filter(t -> "U".equals(t.getState())).map(state -> {
          state.setState(state.getNoYn() == 1 ? "D" : "U");
          return state;
        }).collect(Collectors.toList());
    if (!ObjectUtils.isEmpty(cUItems))
      methodMapper.updateSurgeSub(req.getSurgeSeq(), cUItems);

    List<MethodSurgeSub> cDItems = req.getSubList().stream().filter(t -> "D".equals(t.getState()))
        .collect(Collectors.toList());
    if (!ObjectUtils.isEmpty(cDItems))
      methodMapper.updateSurgeSub(req.getSurgeSeq(), cDItems);

    // 해당없음일 경우에 기존 데이터 날리기(appl_type이 같은것들)
    for (MethodSurgeSub nodata : cIItems) {
      if (nodata.getNoYn() == 1) {
        methodMapper.deleteSurgeSub(req.getSurgeSeq(), nodata);
      }
    }

    return result;

  }
  
  @Override
  public SurgeDTO surgeDetail(int rawSeq) {
    SurgeDTO detail = methodMapper.surgeDetail(rawSeq);
    
    if (detail != null) {
      // 측정설비
      detail.setMacList(macList("SG", rawSeq));
      // 시험결과 > 포트
      detail.setSubList(methodMapper.surgeSubList(detail.getSurgeSeq()));
    }
    return detail;
  }

  @Override
  @Transactional
  public boolean insertCs(CsDTO req) {

    boolean result = true;
    boolean isNew = isNew(TableType.CS, req.getRawSeq());
    
    // 기본로데이터가 없을때 로데이터 먼저 등록
    if (req.getRawSeq() == 0)
      req.setRawSeq(beforeRawInsert(req.getTestSeq(), req.getInsMemId()));

    methodMapper.insertCs(req);

    // 측정설비
    if (!ObjectUtils.isEmpty(req.getMacList())) {
      insertMac(req.getMacList(), req.getRawSeq(), req.getMacType(), req.getMacResetYn());
      
      // 교정일 체크 (신규저장시에만 체크)
      if (isNew)
        valMacReformDate(req.getMsrmnYear(), req.getMsrmnMon(), req.getMsrmnDay(), req.getMacList(), req.getRawSeq(), req.getMacType());
    }

    // 시험결과 > 인가부위
    if (!ObjectUtils.isEmpty(req.getSubList())) {
      List<MethodCsSub> cIItems = req.getSubList().stream().filter(t -> "I".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(cIItems))
        methodMapper.insertCsSub(req.getCsSeq(), cIItems);
  
      List<MethodCsSub> cUItems =
          req.getSubList().stream().filter(t -> "U".equals(t.getState())).map(state -> {
            state.setState(state.getNoYn() == 1 ? "D" : "U");
            return state;
          }).collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(cUItems))
        methodMapper.updateCsSub(req.getCsSeq(), cUItems);
  
      List<MethodCsSub> cDItems = req.getSubList().stream().filter(t -> "D".equals(t.getState()))
          .collect(Collectors.toList());
      if (!ObjectUtils.isEmpty(cDItems))
        methodMapper.updateCsSub(req.getCsSeq(), cDItems);
  
  
      // 해당없음일 경우에 기존 데이터 날리기(appl_type이 같은것들)
      for (MethodCsSub nodata : cIItems) {
        if (nodata.getNoYn() == 1) {
          methodMapper.deleteCsSub(req.getCsSeq(), nodata);
        }
      }
    }
    
    return result;

  }
  
  @Override
  public CsDTO csDetail(int rawSeq) {
    CsDTO detail = methodMapper.csDetail(rawSeq);
    
    if (detail != null) {
      // 측정설비
      detail.setMacList(macList("CS", rawSeq));
      // 시험결과 > 인가부위
      detail.setSubList(methodMapper.csSubList(detail.getCsSeq()));
    }
    return detail;
  }
  
  @Override
  @Transactional
  public boolean insertMf(MfDTO req) {
    
    boolean result = true;
    boolean isNew = isNew(TableType.MF, req.getRawSeq());
    
    // 기본로데이터가 없을때 로데이터 먼저 등록
    if (req.getRawSeq() == 0)
      req.setRawSeq(beforeRawInsert(req.getTestSeq(), req.getInsMemId()));
    
    methodMapper.insertMf(req);
    
    // 측정설비
    if (!ObjectUtils.isEmpty(req.getMacList())) {
      insertMac(req.getMacList(), req.getRawSeq(), req.getMacType(), req.getMacResetYn());
      
      // 교정일 체크 (신규저장시에만 체크)
      if (isNew)
        valMacReformDate(req.getMsrmnYear(), req.getMsrmnMon(), req.getMsrmnDay(), req.getMacList(), req.getRawSeq(), req.getMacType());
    }
    
    return result;
    
  }

  @Override
  public MfDTO mfDetail(int rawSeq) {
    MfDTO detail = methodMapper.mfDetail(rawSeq);

    if (detail != null) {
      // 측정설비
      detail.setMacList(macList("MF", rawSeq));
    }

    return detail;
  }

  @Override
  public InfoDTO info(int rawSeq) {
    return rawMapper.info(rawSeq);
  }
  
  @Override
  @Transactional
  public boolean insertVdip(VdipDTO req) {
    
    boolean result = true;
    boolean isNew = isNew(TableType.VDIP, req.getRawSeq());
    
    // 기본로데이터가 없을때 로데이터 먼저 등록
    if (req.getRawSeq() == 0)
      req.setRawSeq(beforeRawInsert(req.getTestSeq(), req.getInsMemId()));
    
    methodMapper.insertVdip(req);
    
    // 측정설비
    if (!ObjectUtils.isEmpty(req.getMacList())) {
      insertMac(req.getMacList(), req.getRawSeq(), req.getMacType(), req.getMacResetYn());
      
      // 교정일 체크 (신규저장시에만 체크)
      if (isNew)
        valMacReformDate(req.getMsrmnYear(), req.getMsrmnMon(), req.getMsrmnDay(), req.getMacList(), req.getRawSeq(), req.getMacType());
    }
    
    return result;
    
  }

  @Override
  public VdipDTO vdipDetail(int rawSeq) {
    VdipDTO detail = methodMapper.vdipDetail(rawSeq);

    if (detail != null) {
      // 측정설비
      detail.setMacList(macList("VD", rawSeq));
    }
    return detail;
  }
//  
//  @Override
//  @Transactional
//  public boolean insertCti(CtiDTO req) {
//    
//    boolean result = true;
//    boolean isNew = isNew(TableType.c, req.getRawSeq());
//    
//    // 기본로데이터가 없을때 로데이터 먼저 등록
//    if (req.getRawSeq() == 0)
//      req.setRawSeq(beforeRawInsert(req.getTestSeq(), req.getInsMemId()));
//    
//    methodMapper.insertCti(req);
//    
//    // 측정설비
//    if (!ObjectUtils.isEmpty(req.getMacList()))
////      insertMac(req.getMacList(), req.getRawSeq(), req.getMacType(), req.getMacResetYn());
//      
//      // 전원 System: DC XX V System
//      if (!ObjectUtils.isEmpty(req.getSubList()))
//        methodMapper.insertCtiSub(req.getCtiSeq(), req.getSubList());
//    
//    return result;
//    
//  }
//
//  @Override
//  public CtiDTO ctiDetail(int rawSeq) {
//    CtiDTO detail = methodMapper.ctiDetail(rawSeq);
//
//    if (detail != null) {
//      // 측정설비
//      detail.setMacList(macList("CT", rawSeq));
//
//      // 시험결과
//      detail.setSubList(ctiSubList(detail.getCtiSeq()));
//    }
//    return detail;
//  }
  
  @Override
  @Transactional
  public boolean insertClk(ClkDTO req) {
    
    boolean result = true;
    boolean isNew = isNew(TableType.CK, req.getRawSeq());
    
    methodMapper.insertClk(req);
    
    // 측정설비
    if (!ObjectUtils.isEmpty(req.getMacList())) {
      insertMac(req.getMacList(), req.getRawSeq(), req.getMacType(), req.getMacResetYn());
      
      // 교정일 체크 (신규저장시에만 체크)
      if (isNew)
        valMacReformDate(req.getMsrmnYear(), req.getMsrmnMon(), req.getMsrmnDay(), req.getMacList(), req.getRawSeq(), req.getMacType());
    }
    
    return result;
    
  }

  @Override
  public ClkDTO clkDetail(int rawSeq) {
    ClkDTO detail = methodMapper.clkDetail(rawSeq);

    if (detail != null) {
      // 측정설비
      detail.setMacList(macList("CK", rawSeq));
    }
    return detail;
  }
  
  @Override
  @Transactional
  public boolean insertDp(DpDTO req) {
    
    boolean result = true;
    boolean isNew = isNew(TableType.DP, req.getRawSeq());
    
    methodMapper.insertDp(req);
    
    // 측정설비
    if (!ObjectUtils.isEmpty(req.getMacList())) {
      insertMac(req.getMacList(), req.getRawSeq(), req.getMacType(), req.getMacResetYn());
      
      // 교정일 체크 (신규저장시에만 체크)
      if (isNew)
        valMacReformDate(req.getMsrmnYear(), req.getMsrmnMon(), req.getMsrmnDay(), req.getMacList(), req.getRawSeq(), req.getMacType());
    }
    
    return result;
    
  }
  
  @Override
  public DpDTO dpDetail(int rawSeq) {
    DpDTO detail = methodMapper.dpDetail(rawSeq);

    if (detail != null) {
      // 측정설비
      detail.setMacList(macList("DP", rawSeq));
    }
    return detail;
  }
 
  @Override
  public TelDTO telDetail(int rawSeq) {
    TelDTO detail = methodMapper.telDetail(rawSeq);

    return detail;
  }

  @Override
  public boolean insertTel(TelDTO req) {

    boolean result = true;

    methodMapper.insertTel(req);

    return result;

  }
  
  @Override
  public ImgDTO imgDetail(ImgDTO req) {
    return methodMapper.imgDetail(req);
  }

  @Override
  public List<ImgDTO> imgList(int rawSeq) {
    return methodMapper.imgList(rawSeq);
  }
  
  @Override
  public boolean insertImg(ImgDTO req) {

    // 기본로데이터가 없을때 로데이터 먼저 등록
    if (req.getRawSeq() == 0)
      req.setRawSeq(beforeRawInsert(req.getTestSeq(), req.getInsMemId()));

    return methodMapper.insertImg(req);
  }

  @Override
  public List<HisDTO> hisList(String rawSeq) {
    return rawMapper.hisList(rawSeq);
  }

  @Override
  public ReportDTO report(int rawSeq) {
    return rawMapper.report(rawSeq);
  }

  @Override
  public boolean insertFile(FileRaw req) {
    return rawMapper.insertFile(req);
  }

  @Override
  public int fileRawListCnt(int testSeq, ComParam param) {
    return rawMapper.fileRawListCnt(testSeq, param);
  }

  @Override
  public List<FileRawDTO> fileRawList(int testSeq, ComParam param) {
    return rawMapper.fileRawList(testSeq, param);
  }

  @Override
  public List<RawTchn> tchnList(int rawSeq) {
    return rawMapper.tchnList(rawSeq);
  }

  @Override
  public List<RawSpec> specList(int rawSeq) {
    return rawMapper.specList(rawSeq);
  }

  @Override
  public List<RawAsstn> asstnList(int rawSeq) {
    return rawMapper.asstnList(rawSeq);
  }

  @Override
  public List<RawSys> sysList(int rawSeq) {
    return rawMapper.sysList(rawSeq);
  }

  @Override
  public List<RawCable> cableList(int rawSeq) {
    return rawMapper.cableList(rawSeq);
  }

  @Override
  public List<RawMet> methodList(int rawSeq) {
    return rawMapper.methodList(rawSeq);
  }

  @Override
  public List<RawMac> macList(String machineType, int rawSeq) {
    return methodMapper.macList(machineType, rawSeq);
  }

  @Override
  public List<RawMac> emptyMacList(String machineType, int rawSeq) {
    return methodMapper.emptyMacList(machineType, rawSeq); 
  }

  @Override
  public List<MethodEsdSub> esdSubList(int esdSeq) {
    return methodMapper.esdSubList(esdSeq);
  }

//  @Override
//  public List<MethodCtiSub> ctiSubList(int ctiSeq) {
//    return methodMapper.ctiSubList(ctiSeq);
//  }

  @Override
  public List<Test> reportDetail(int testSeq) {
    return rawMapper.reportDetail(testSeq);
  }

  private void insertMac(List<RawMac> macList, int rawSeq, String macType, int resetYn) {
    
    // 장비 초기화
    if (resetYn == 1) {
      methodMapper.deleteMac(rawSeq, macType);
    }
    // 측정설비
    if (!ObjectUtils.isEmpty(macList))
      methodMapper.insertMac(rawSeq, macType, macList);

  }
  
  private boolean isNew(TableType type, int rawSeq) {
      return !methodMapper.existsByRawSeq(type.getTableName(), rawSeq);
  }
  
  private void valMacReformDate(int year, int mon, int day, List<RawMac> macList, int rawSeq, String macType) {
    if (ObjectUtils.isEmpty(macList)) return;

    try {
      
      // 측정일 조합
      LocalDate measurementDate = LocalDate.of(year, mon, day);
  
      List<RawMac> insertedMacList = methodMapper.selectReformDate(rawSeq, macType);
  
      for (RawMac mac : insertedMacList) {
          
        if (mac.getReformDt() != null) {
          LocalDate reformDate = LocalDate.parse(mac.getReformDt());
            if (reformDate.isBefore(measurementDate)) {
              String msg = String.format(
                  "장비 %d는 개조일(%s)이 측정일(%s)보다 빠릅니다. 저장 중단.",
                  mac.getMachineSeq(), reformDate, measurementDate
                  );
              System.out.println(msg);
              throw new IllegalArgumentException("시험 장비의 차기 교정일을 확인해 주세요.");
            }
        }
        
      }
      
    } catch (DateTimeException e) {
      log.warn("장비 측정일 조합 중 날짜 오류 발생 - year={}, month={}, day={}", year, mon, day);
      
    }
    
  }
  
  private int[] extractMeasurementDate(ReDTO req) {
    switch (req.getMacType()) {
        case "RE1":
            return new int[]{ req.getHz1MsrmnYear(), req.getHz1MsrmnMon(), req.getHz1MsrmnDay() };
        case "RE2":
            return new int[]{ req.getHz2MsrmnYear(), req.getHz2MsrmnMon(), req.getHz2MsrmnDay() };
        case "RE3":
            return new int[]{ req.getHz3MsrmnYear(), req.getHz3MsrmnMon(), req.getHz3MsrmnDay() };
        default:
            return new int[]{ 0, 0, 0 };
    }
}
}
