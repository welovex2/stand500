package egovframework.sys.service;

import java.util.List;

import egovframework.cmm.service.ComParam;

public interface PowService {

  List<Power> selectDetail();

  boolean insert(List<Power> list);

}
