package egovframework.raw.service;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import egovframework.cmm.service.ComParam;
import egovframework.cmm.service.HisDTO;
import egovframework.raw.dto.FileRawDTO;
import egovframework.raw.dto.InfoDTO;
import egovframework.raw.dto.ReportDTO;
import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("RawMapper")
public interface RawMapper {

	public String getSbkId(int testSeq);
	
	public boolean insert(RawData req);
	
	public void insertSpec(@Param("rawSeq") int rawSeq, @Param("rawSpecList") List<RawSpec> rawSpecList);
	
	public void updateSpec(@Param("rawSeq") int rawSeq, @Param("rawSpecList") List<RawSpec> rawSpecList);
	
	public void deleteSpec(@Param("rawSeq") int rawSeq, @Param("rawSpecList") List<RawSpec> rawSpecList);
	
	public void insertTchn(@Param("rawSeq") int rawSeq, @Param("rawTchnList") List<RawTchn> rawTchnList);
	
	public void updateTchn(@Param("rawSeq") int rawSeq, @Param("rawTchnList") List<RawTchn> rawTchnList);
	
	public void deleteTchn(@Param("rawSeq") int rawSeq, @Param("rawTchnList") List<RawTchn> rawTchnList);

	public void insertAsstn(@Param("rawSeq") int rawSeq, @Param("rawAsstnList") List<RawAsstn> rawAsstnList);
	
	public void updateAsstn(@Param("rawSeq") int rawSeq, @Param("rawAsstnList") List<RawAsstn> rawAsstnList);
	
	public void deleteAsstn(@Param("rawSeq") int rawSeq, @Param("rawAsstnList") List<RawAsstn> rawAsstnList);

	public void insertSys(@Param("rawSeq") int rawSeq, @Param("rawSysList") List<RawSys> rawSysList);
	
	public void updateSys(@Param("rawSeq") int rawSeq, @Param("rawSysList") List<RawSys> rawSysList);
	
	public void deleteSys(@Param("rawSeq") int rawSeq, @Param("rawSysList") List<RawSys> rawSysList);

	public void insertCable(@Param("rawSeq") int rawSeq, @Param("rawCableList") List<RawCable> rawCableList);
	
	public void updateCable(@Param("rawSeq") int rawSeq, @Param("rawCableList") List<RawCable> rawCableList);
	
	public void deleteCable(@Param("rawSeq") int rawSeq, @Param("rawCableList") List<RawCable> rawCableList);
	
	public boolean update(RawData req);

	public RawData detail(int testSeq);

	public List<RawTchn> tchnList(int rawSeq);

	public List<RawSpec> specList(int rawSeq);

	public List<RawAsstn> asstnList(int rawSeq);

	public List<RawSys> sysList(int rawSeq);

	public List<RawCable> cableList(int rawSeq);

	public void insertMethod(@Param("rawSeq") int rawSeq, @Param("methodList") List<RawMet> methodList);

	public List<RawMet> methodList(int rawSeq);

	public InfoDTO info(int rawSeq);
	
	public List<HisDTO> hisList(String rawSeq);
	
	ReportDTO report(int rawSeq);
	
	public boolean insertFile(FileRaw req);
	
	int fileRawListCnt(@Param("testSeq") int testSeq, @Param("comParam") ComParam param);

	List<FileRawDTO> fileRawList(@Param("testSeq") int testSeq, @Param("comParam") ComParam param);
}
