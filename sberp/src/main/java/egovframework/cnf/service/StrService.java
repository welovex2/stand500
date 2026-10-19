package egovframework.cnf.service;

import java.util.List;
import egovframework.cnf.dto.StoreDetailDTO;
import egovframework.cnf.dto.ZoneRangeReq;
import egovframework.cnf.dto.ZoneRangeResult;

public interface StrService {

  List<Warehouse> selectList(String searchWord, boolean includeStopped);

  StoreDetailDTO detail(int strSeq);

  boolean insert(Warehouse req);

  boolean update(Warehouse req);

  boolean stop(Warehouse req);

  boolean insertZone(Zone req);

  ZoneRangeResult insertZoneRange(ZoneRangeReq req);

  boolean updateZone(Zone req);

  boolean stopZone(Zone req);
}
