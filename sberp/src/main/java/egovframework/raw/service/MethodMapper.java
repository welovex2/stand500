package egovframework.raw.service;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import egovframework.raw.dto.CeDTO;
import egovframework.raw.dto.ClkDTO;
import egovframework.raw.dto.CsDTO;
import egovframework.raw.dto.CtiDTO;
import egovframework.raw.dto.DpDTO;
import egovframework.raw.dto.EftDTO;
import egovframework.raw.dto.EsdDTO;
import egovframework.raw.dto.ImgDTO;
import egovframework.raw.dto.MfDTO;
import egovframework.raw.dto.ReDTO;
import egovframework.raw.dto.RsDTO;
import egovframework.raw.dto.SurgeDTO;
import egovframework.raw.dto.TelDTO;
import egovframework.raw.dto.VdipDTO;
import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("MethodMapper")
public interface MethodMapper {

  public List<RawMac> macList(@Param("machineType") String machineType,
      @Param("rawSeq") int rawSeq);

  public List<RawMac> emptyMacList(@Param("machineType") String machineType,
      @Param("rawSeq") int rawSeq);

  public CeDTO ceDetail(int rawSeq);

  public void insertCe(CeDTO req);

  public void insertMac(@Param("rawSeq") int rawSeq, @Param("machineType") String machineType,
      @Param("macList") List<RawMac> macList);

  public void updateCe(CeDTO req);

  public ReDTO reDetail(int rawSeq);

  public void insertRe(ReDTO req);

  public void updateRe(ReDTO req);

  public void insertEsd(EsdDTO req);

  public EsdDTO esdDetail(int rawSeq);

  public List<MethodEsdSub> esdSubList(int esdSeq);

  public void insertEsdSub(@Param("esdSeq") int esdSeq,
      @Param("MethodEsdSubList") List<MethodEsdSub> sIItems);

  public void updateEsdSub(@Param("esdSeq") int esdSeq,
      @Param("MethodEsdSubList") List<MethodEsdSub> sUItems);

  public void deleteEsdSub(@Param("esdSeq") int esdSeq,
      @Param("MethodEsdSubList") List<MethodEsdSub> sDItems);

  public RsDTO rsDetail(int rawSeq);

  public void insertRs(RsDTO req);

  public void insertRsSub(@Param("rsSeq") int rsSeq,
      @Param("MethodRsSubList") List<MethodRsSub> sIItems);

  public void updateRsSub(@Param("rsSeq") int rsSeq,
      @Param("MethodRsSubList") List<MethodRsSub> sUItems);

  public void deleteRsSub(@Param("rsSeq") int rsSeq, @Param("MethodSub") MethodRsSub sDItems);

  public List<MethodRsSub> rsSubList(int rsSeq);

  EftDTO eftDetail(int rawSeq);

  public List<MethodEftSub> eftSubList(int eftSeq);

  boolean insertEft(EftDTO req);

  public void insertEftSub(@Param("eftSeq") int eftSeq,
      @Param("MethodEftSubList") List<MethodEftSub> sIItems);

  public void updateEftSub(@Param("eftSeq") int eftSeq,
      @Param("MethodEftSubList") List<MethodEftSub> sUItems);

  public void deleteEftSub(@Param("eftSeq") int eftSeq, @Param("MethodSub") MethodEftSub dItems);

  public SurgeDTO surgeDetail(int rawSeq);

  public List<MethodSurgeSub> surgeSubList(int surgeSeq);

  public boolean insertSurge(SurgeDTO req);

  public void insertSurgeSub(@Param("surgeSeq") int surgeSeq,
      @Param("MethodSurgeSubList") List<MethodSurgeSub> sIItems);

  public void updateSurgeSub(@Param("surgeSeq") int surgeSeq,
      @Param("MethodSurgeSubList") List<MethodSurgeSub> sUItems);

  public void deleteSurgeSub(@Param("surgeSeq") int surgeSeq,
      @Param("MethodSub") MethodSurgeSub sDItems);

  public CsDTO csDetail(int rawSeq);

  public boolean insertCs(CsDTO req);

  public void insertCsSub(@Param("csSeq") int csSeq,
      @Param("MethodCsSubList") List<MethodCsSub> sIItems);

  public void updateCsSub(@Param("csSeq") int csSeq,
      @Param("MethodCsSubList") List<MethodCsSub> sUItems);

  public void deleteCsSub(@Param("csSeq") int csSeq,
      @Param("MethodSub") MethodCsSub sDItems);

  public List<MethodCsSub> csSubList(int esdSeq);

  public MfDTO mfDetail(int rawSeq);

  public boolean insertMf(MfDTO req);

  public VdipDTO vdipDetail(int rawSeq);

  public boolean insertVdip(VdipDTO req);

  public CtiDTO ctiDetail(int rawSeq);

  public boolean insertCti(CtiDTO req);

  public ClkDTO clkDetail(int rawSeq);

  public boolean insertClk(ClkDTO req);
  
  public DpDTO dpDetail(int rawSeq);

  public boolean insertDp(DpDTO req);
  
  public TelDTO telDetail(int rawSeq);

  public boolean insertTel(TelDTO req);
  
  public void insertCtiSub(@Param("ctiSeq") int ctiSeq,
      @Param("ctiSubList") List<MethodCtiSub> sIItems);

  public List<MethodCtiSub> ctiSubList(int ctiSeq);

  public ImgDTO imgDetail(ImgDTO req);
  
  public List<ImgDTO> imgList(int rawSeq);

  public boolean insertImg(ImgDTO req);
}
