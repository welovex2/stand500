package egovframework.sys.service;

import java.util.List;

import egovframework.cmm.service.ComParam;
import egovframework.rte.psl.dataaccess.mapper.Mapper;
import egovframework.tst.dto.TestCateDTO;

@Mapper("StbMapper")
public interface StbMapper {

  List<TestStndrDTO> selectList(ComParam param);

  int selectListCnt(ComParam param);

  void insert(TestStndrDTO req);

  void insertCate(TestCateDTO req);

  List<TestCateDTO> selectCateList(ComParam param);

  int selectCateListCnt(ComParam param);
}
