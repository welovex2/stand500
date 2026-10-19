package egovframework.cnf.service;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import egovframework.cnf.dto.StoreDetailDTO;
import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("StrMapper")
public interface StrMapper {

  /* ===== 창고 (STR_TB) ===== */

  List<Warehouse> selectList(@Param("searchWord") String searchWord,
      @Param("includeStopped") String includeStopped);

  Warehouse selectBySeq(@Param("strSeq") int strSeq);

  /** 상세. 창고 항목을 StoreDetailDTO 에 바로 담는다. 구역 목록은 selectZoneList 로 따로 채운다. */
  StoreDetailDTO selectDetailBySeq(@Param("strSeq") int strSeq);

  int insert(Warehouse req);

  int update(Warehouse req);

  int stop(Warehouse req);

  int countTypeCode(@Param("typeCode") String typeCode);

  int countWhName(@Param("whName") String whName, @Param("strSeq") int strSeq);

  /* ===== 구역 ===== */

  List<Zone> selectZoneList(@Param("strSeq") int strSeq);

  Zone selectZoneBySeq(@Param("zoneSeq") int zoneSeq);

  int countZoneName(@Param("strSeq") int strSeq, @Param("zoneSeq") Integer zoneSeq,
      @Param("zoneName") String zoneName);

  int insertZone(Zone req);

  int updateZoneLabel(Zone req);

  int updateZoneUse(Zone req);

  int stopZone(Zone req);

  int countItemsByWh(@Param("strSeq") int strSeq);

  int countItemsByZone(@Param("zoneSeq") int zoneSeq);

  /** 그 창고에서 사용 중인 구역 수. 마지막 구역 중지를 막는 데 쓴다. */
  int countActiveZones(@Param("strSeq") int strSeq);
}
