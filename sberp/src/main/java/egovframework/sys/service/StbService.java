package egovframework.sys.service;

import java.util.List;

import egovframework.cmm.service.ComParam;
import egovframework.tst.dto.TestCateDTO;

public interface StbService {

  List<TestStndrDTO> selectList(ComParam param);

//  int selectListCnt(ComParam param);

  boolean insert(List<TestStndrDTO> req);

  boolean update(TestStndrDTO req);

  boolean insertCate(TestCateDTO req);

  List<TestCateDTO> selectCateList(ComParam param);

//  int selectCateListCnt(ComParam param);
}
