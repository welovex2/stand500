package egovframework.raw.service;

import java.util.List;
import egovframework.cmm.service.ComParam;
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
import egovframework.raw.dto.RawSearchDTO;
import egovframework.raw.dto.ReDTO;
import egovframework.raw.dto.ReportDTO;
import egovframework.raw.dto.RsDTO;
import egovframework.raw.dto.SurgeDTO;
import egovframework.raw.dto.TelDTO;
import egovframework.raw.dto.VdipDTO;
import egovframework.tst.service.Test;

public interface RawService {

  boolean insert(RawData req);

  boolean update(RawData req);

  CeDTO ceDetail(int rawSeq);

  boolean insertCe(CeDTO req);

  ReDTO reDetail(int rawSeq);

  boolean insertRe(ReDTO req);

  boolean insertEsd(EsdDTO req);

  EsdDTO esdDetail(int rawSeq) throws Exception;

  int getTestSeq(String testId);

  RawData detail(RawSearchDTO req) throws Exception;

  RawData basicDetail(RawSearchDTO req) throws Exception;
  
  RsDTO rsDetail(int rawSeq);

  boolean insertRs(RsDTO req);

  EftDTO eftDetail(int rawSeq);

  boolean insertEft(EftDTO req);

  List<RawTchn> tchnList(int rawSeq);

  List<RawSpec> specList(int rawSeq);

  List<RawAsstn> asstnList(int rawSeq);

  List<RawSys> sysList(int rawSeq);

  List<RawCable> cableList(int rawSeq);

  List<RawMet> methodList(int rawSeq);

  List<RawMac> macList(String machineType, int rawSeq);

  List<RawMac> emptyMacList(String machineType, int rawSeq);

  List<MethodEsdSub> esdSubList(int esdSeq);

  List<MethodCtiSub> ctiSubList(int ctiSeq);

  SurgeDTO surgeDetail(int rawSeq);

  boolean insertSurge(SurgeDTO req);

  CsDTO csDetail(int rawSeq);

  boolean insertCs(CsDTO req);

  MfDTO mfDetail(int rawSeq);

  boolean insertMf(MfDTO req);

  InfoDTO info(int testSeq);

  VdipDTO vdipDetail(int rawSeq);

  boolean insertVdip(VdipDTO req);

  CtiDTO ctiDetail(int rawSeq);

  boolean insertCti(CtiDTO req);
  
  ClkDTO clkDetail(int rawSeq);

  boolean insertClk(ClkDTO req);
  
  DpDTO dpDetail(int rawSeq);

  boolean insertDp(DpDTO req);
  
  TelDTO telDetail(int rawSeq);

  boolean insertTel(TelDTO req);
  
  ImgDTO imgDetail(ImgDTO req);
  
  List<ImgDTO> imgList(int rawSeq);

  boolean insertImg(ImgDTO req);

  List<HisDTO> hisList(String rawSeq);

  ReportDTO report(int rawSeq);

  boolean insertFile(FileRaw req);

  int fileRawListCnt(int testSeq, ComParam param);

  List<FileRawDTO> fileRawList(int testSeq, ComParam param);

  List<Test> reportDetail(int testSeq);
}
