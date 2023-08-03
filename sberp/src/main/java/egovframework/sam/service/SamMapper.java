package egovframework.sam.service;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import egovframework.cmm.service.ComParam;
import egovframework.rte.psl.dataaccess.mapper.Mapper;
import egovframework.sam.dto.ImDTO;
import egovframework.sam.dto.ImSubDTO;

@Mapper("SamMapper")
public interface SamMapper {

  public boolean insert(ImDTO req);
  
  public boolean update(ImDTO req);
  
  public ImDTO detail(String sbkId);
  
  public void insertSub(@Param("insMemId") String insMemId, @Param("imId") String imId, @Param("item") ImSub item);
  
  public void updateSub(@Param("insMemId") String insMemId, @Param("imId") String imId, @Param("itemList") List<ImSub> itemList);
  
  public void deleteSub(@Param("insMemId") String insMemId, @Param("imId") String imId, @Param("itemList") List<ImSub> itemList);
  
  public List<ImSub> subList(String imId);

  public int selectListCnt(ComParam param);

  public List<ImSubDTO> selectList(ComParam param);
  
}
