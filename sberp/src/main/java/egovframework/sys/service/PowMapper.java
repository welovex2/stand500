package egovframework.sys.service;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("PowMapper")
public interface PowMapper {

  List<Power> selectDetail();

  void insert(@Param("req") List<Power> req);

}
