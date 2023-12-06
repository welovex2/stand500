package egovframework.raw.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import egovframework.cmm.service.BasicResponse;
import egovframework.cmm.service.EgovFileMngService;
import egovframework.cmm.service.FileVO;
import egovframework.cmm.service.ResponseMessage;
import egovframework.raw.dto.CeDTO;
import egovframework.raw.dto.CsDTO;
import egovframework.raw.dto.EftDTO;
import egovframework.raw.dto.EsdDTO;
import egovframework.raw.dto.ImgDTO;
import egovframework.raw.dto.MfDTO;
import egovframework.raw.dto.PicDTO;
import egovframework.raw.dto.ReDTO;
import egovframework.raw.dto.ReportDTO;
import egovframework.raw.dto.RsDTO;
import egovframework.raw.dto.SurgeDTO;
import egovframework.raw.dto.VdipDTO;
import egovframework.raw.service.RawMet;
import egovframework.raw.service.RawService;
import egovframework.rte.fdl.property.EgovPropertyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags = {"성적서"})
@RestController
public class RepController {

  @Resource(name = "RawService")
  private RawService rawService;

  @Resource(name = "EgovFileMngService")
  private EgovFileMngService fileMngService;

  @Resource(name = "propertiesService")
  protected EgovPropertyService propertyService;
  
  @ApiOperation(value = "성적서 상세보기")
  @GetMapping(value = "/raw/{testSeq}/report.do")
  public BasicResponse rawReport(@ApiParam(value = "시험 고유번호", required = true,
      example = "22") @PathVariable(name = "testSeq") int testSeq) throws Exception {
    boolean result = true;
    String msg = "";
    ReportDTO detail = new ReportDTO();

    detail = rawService.report(testSeq);
    if (detail == null) {
      result = false;
      msg = ResponseMessage.NO_DATA;
    } else {
      /* 세부데이터 추가로 가지고 오기 */
      int rawSeq = detail.getRawSeq();

      // 성적서 발급내역 리스트 가져오기
      detail.setReportList(rawService.reportDetail(testSeq));
      
      // 3.2 시험항목 >> methodList
      detail.setMethodList(rawService.methodList(rawSeq));
      // 3.3 피시험기기의 보완내용 >> modList
      FileVO fileVO = new FileVO();
      fileVO.setAtchFileId(detail.getModUrl());
      List<FileVO> modResult = fileMngService.selectImageFileList(fileVO);
      List<String> modList = new ArrayList<String>();
      if (modResult != null) {
        for (FileVO item : modResult) {
          if ("CDN".contentEquals(item.getFileLoc())) {
            modList.add(propertyService.getString("cdn.url").concat(item.getFileStreCours()).concat("/")
                .concat(item.getStreFileNm()).concat(".").concat(item.getFileExtsn()));
          } else {
            modList.add(propertyService.getString("img.url").concat(detail.getModUrl()).concat("&fileSn=")
                .concat(item.getFileSn()));
          }
        }
      }
      detail.setModFileList(modList);
      // 4.2 기술 제원 >> SpecList
      detail.setRawSpecList(rawService.specList(rawSeq));
      // 5.1 전체구성 >> AsstnList
      detail.setRawAsstnList(rawService.asstnList(rawSeq));
      // 5.2 시스템구성 (시험기자재가 컴퓨터 및 시스템인 경우) >> sysList
      detail.setRawSysList(rawService.sysList(rawSeq));
      // 5.3 접속 케이블 >> cableList
      detail.setRawCableList(rawService.cableList(rawSeq));
      // 5.5 배치도 >> setupList
      fileVO = new FileVO();
      fileVO.setAtchFileId(detail.getSetupUrl());
      List<FileVO> setupReulst = fileMngService.selectImageFileList(fileVO);
      List<PicDTO> setupList = new ArrayList<PicDTO>();
      if (setupReulst != null) {
        for (FileVO item : setupReulst) {
          PicDTO map = new PicDTO();
          map.setTitle(item.getFileCn());
          
          if ("CDN".contentEquals(item.getFileLoc())) {
            map.setImageUrl(propertyService.getString("cdn.url").concat(item.getFileStreCours()).concat("/")
                .concat(item.getStreFileNm()).concat(".").concat(item.getFileExtsn()));
          } else {
            map.setImageUrl(propertyService.getString("img.url").concat(detail.getSetupUrl())
                .concat("&fileSn=").concat(item.getFileSn()));
          }
          
          setupList.add(map);

        }
      }
      detail.setSetupList(setupList);

      CeDTO ce = null;
      ReDTO re = null;
      for (int i = 0; i < detail.getMethodList().size(); i++) {

        RawMet met = (RawMet) detail.getMethodList().get(i);

        /**
         * 시험 해당 됨 처리
         */
        if (met.getCheckYn() == 1) {
          switch (met.getMetSeq()) {
            //   9.1 교류 주전원 포트에서의 전도성 방해 시험
            case 0:
              if (ObjectUtils.isEmpty(ce))
                ce = rawService.ceDetail(rawSeq);
              if (ce != null) {
                ce.setMacList(rawService.macList("CA", rawSeq));
              }
              detail.setCe1(ce);
              break;
            //   9.2 비대칭모드 전도성 방해 시험
            case 1:
              ce = null;
              if (ObjectUtils.isEmpty(ce))
                ce = rawService.ceDetail(rawSeq);
              if (ce != null) {
                ce.setMacList(rawService.macList("CA", rawSeq));
              }
              detail.setCe2(ce);
              break;
            //   9.3 B급 기기의 방송수신기 튜너포트 차동전압 전도성 방해 시험
            case 2:
              ce = null;
              if (ObjectUtils.isEmpty(ce))
                ce = rawService.ceDetail(rawSeq);
              if (ce != null) {
                ce.setMacList(rawService.macList("CB", rawSeq));
              }
              detail.setCe3(ce);
              break;
            //   9.4 B급 기기의 RF변조기 출력포트에서의 차동전압 전도성 방해 시험
            case 3:
              ce = null;
              if (ObjectUtils.isEmpty(ce))
                ce = rawService.ceDetail(rawSeq);
              if (ce != null) {
                ce.setMacList(rawService.macList("CB", rawSeq));
              }
              detail.setCe4(ce);
              break;
            //   9.5 방사성 방해 시험 (1GHz 이하 대역)
            case 4:
              if (ObjectUtils.isEmpty(re))
                re = rawService.reDetail(rawSeq);
              if (re != null) {
                re.setMacList(rawService.macList("RA", rawSeq));
              }
              detail.setRe1(re);
              break;
            case 5:
              re = null;
              //   9.6 방사성 방해 시험 (1GHz 초과 대역)
              if (ObjectUtils.isEmpty(re))
                re = rawService.reDetail(rawSeq);
              if (re != null) {
                re.setMacList(rawService.macList("RB", rawSeq));
              }
              detail.setRe2(re);
              break;
            //   9.7 정전기 방전 시험
            case 6:
              detail.setEsd(rawService.esdDetail(rawSeq));
              break;
            //   9.8 방사성 RF 전자기장 시험
            case 7:
              detail.setRs(rawService.rsDetail(rawSeq));
              break;
            //   9.9 전기적 빠른 과도현상 시험
            case 8:
              detail.setEft(rawService.eftDetail(rawSeq));
              break;
            //   9.10 서지 시험
            case 9:
              detail.setSurge(rawService.surgeDetail(rawSeq));
              break;
            //   9.11 전도성 RF 전자기장 시험
            case 10:
              detail.setCs(rawService.csDetail(rawSeq));
              break;
            //   9.12 전원 주파수 자기장 시험
            case 11:
              detail.setMf(rawService.mfDetail(rawSeq));
              break;
            //   9.13 전압 강하 및 순간 정전 시험
            case 12:
              detail.setVdip(rawService.vdipDetail(rawSeq));
              break;

          } // -- END switch
        } // -- END if
        /**
         * 시험항목 없을 경우 macList만 셋팅
         */
        else if (met.getCheckYn() == 0) {

          switch (met.getMetSeq()) {
            //   9.1 교류 주전원 포트에서의 전도성 방해 시험
            case 0:
              ce = new CeDTO();
              ce.setResultCode("-1");
              ce.setMacList(rawService.emptyMacList("CA", rawSeq));
              detail.setCe1(ce);
              break;
            //   9.2 비대칭모드 전도성 방해 시험
            case 1:
              ce = new CeDTO();
              ce.setResultCode("-1");
              ce.setMacList(rawService.emptyMacList("CA", rawSeq));
              detail.setCe2(ce);
              break;
            //   9.3 B급 기기의 방송수신기 튜너포트 차동전압 전도성 방해 시험
            case 2:
              ce = new CeDTO();
              ce.setResultCode("-1");
              ce.setMacList(rawService.emptyMacList("CB", rawSeq));
              detail.setCe3(ce);
              break;
            //   9.4 B급 기기의 RF변조기 출력포트에서의 차동전압 전도성 방해 시험
            case 3:
              ce = new CeDTO();
              ce.setResultCode("-1");
              ce.setMacList(rawService.emptyMacList("CB", rawSeq));
              detail.setCe4(ce);
              break;
            //   9.5 방사성 방해 시험 (1GHz 이하 대역)
            case 4:
              re = new ReDTO();
              re.setHz1ResultCode("-1");
              re.setMacList(rawService.emptyMacList("RA", rawSeq));
              detail.setRe1(re);
              break;
            case 5:
              //   9.6 방사성 방해 시험 (1GHz 초과 대역)
              re = new ReDTO();
              re.setHz2ResultCode("-1");
              re.setMacList(rawService.emptyMacList("RB", rawSeq));
              detail.setRe2(re);
              break;
            //   9.7 정전기 방전 시험
            case 6:
              EsdDTO esd = new EsdDTO();
              esd.setResultCode("-1");
              esd.setMacList(rawService.emptyMacList("ED", rawSeq));
              detail.setEsd(esd);
              break;
            //   9.8 방사성 RF 전자기장 시험
            case 7:
              RsDTO rs = new RsDTO();
              rs.setResultCode("-1");
              rs.setMacList(rawService.emptyMacList("RS", rawSeq));
              detail.setRs(rs);
              break;
            //   9.9 전기적 빠른 과도현상 시험
            case 8:
              EftDTO eft = new EftDTO();
              eft.setResultCode("-1");
              eft.setMacList(rawService.emptyMacList("ET", rawSeq));
              detail.setEft(eft);
              break;
            //   9.10 서지 시험
            case 9:
              SurgeDTO su = new SurgeDTO();
              su.setResultCode("-1");
              su.setMacList(rawService.emptyMacList("SG", rawSeq));
              detail.setSurge(su);
              break;
            //   9.11 전도성 RF 전자기장 시험
            case 10:
              CsDTO cs = new CsDTO();
              cs.setResultCode("-1");
              cs.setMacList(rawService.emptyMacList("CS", rawSeq));
              detail.setCs(cs);
              break;
            //   9.12 전원 주파수 자기장 시험
            case 11:
              MfDTO mf = new MfDTO();
              mf.setResultCode("-1");
              mf.setMacList(rawService.emptyMacList("MF", rawSeq));
              detail.setMf(mf);
              break;
            //   9.13 전압 강하 및 순간 정전 시험
            case 12:
              VdipDTO vp = new VdipDTO();
              vp.setResultCode("-1");
              vp.setMacList(rawService.emptyMacList("VD", rawSeq));
              detail.setVdip(vp);
              break;

          } // -- END switch
        } // -- END elseif
      } // -- END for


      // 시험장면 사진
      List<PicDTO> resultList = new ArrayList<PicDTO>();

      for (int i = 1; i < 15; i++) {

        ImgDTO img = new ImgDTO();
        img.setRawSeq(rawSeq);
        img.setPicId(Integer.toString(i));
        img = rawService.imgDetail(img);

        if (img != null) {
          fileVO = new FileVO();
          fileVO.setAtchFileId(img.getAtchFileId());
          // 해당없음이 있기 때문에 fileInf로 확인
          List<FileVO> fileReulst = fileMngService.selectFileInfs(fileVO);

          // 리스트 정렬 (모드순으로)
          Collections.sort(fileReulst, new Comparator<FileVO>() {
            @Override
            public int compare(FileVO p1, FileVO p2) {
              return p1.getFileMemo().compareTo(p2.getFileMemo());
            }
          });

          if (fileReulst != null) {
            for (FileVO item : fileReulst) {
              PicDTO pic = new PicDTO();
              pic.setPicId(Integer.toString(i));
              
              if ("CDN".contentEquals(item.getFileLoc())) {
                pic.setImageUrl(propertyService.getString("cdn.url").concat(item.getFileStreCours()).concat("/")
                    .concat(item.getStreFileNm()).concat(".").concat(item.getFileExtsn()));
              } else {
                pic.setImageUrl(propertyService.getString("img.url").concat(img.getAtchFileId())
                    .concat("&fileSn=").concat(item.getFileSn()));
              }
              pic.setTitle(item.getFileCn());
              pic.setMode(item.getFileMemo());
              // 성적서에서만 사용
              pic.setPicYn(img.getPicYn());
              resultList.add(pic);

            }
          }

          int picYn = img.getPicYn();
          // 시험결과 해당없음 처리
          switch (i) {
            case 1:
              detail.getCe1().setPicYn(picYn);
              break;
            case 2:
              detail.getCe2().setPicYn(picYn);
              break;
            case 3:
              detail.getCe3().setPicYn(picYn);
              break;
            case 4:
              detail.getCe4().setPicYn(picYn);
              break;
            case 5:
              detail.getRe1().setPicYn(picYn);
              break;
            case 6:
              detail.getRe2().setPicYn(picYn);
              break;
            case 7:
              detail.getEsd().setPicYn(picYn);
              break;
            case 8:
              detail.getRs().setPicYn(picYn);
              break;
            case 9:
              detail.getEft().setPicYn(picYn);
              break;
            case 10:
              detail.getSurge().setPicYn(picYn);
              break;
            case 11:
              detail.getCs().setPicYn(picYn);
              break;
            case 12:
              detail.getMf().setPicYn(picYn);
              break;
            case 13:
              detail.getVdip().setPicYn(picYn);
              break;

          }
          // -- END 시험결과 해당없음 처리


        }

      }
      detail.setImgList(resultList);

    }



    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(detail).build();

    return res;
  }

}

