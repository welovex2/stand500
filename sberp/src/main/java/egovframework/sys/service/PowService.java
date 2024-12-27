package egovframework.sys.service;

import java.util.List;

import egovframework.cmm.service.ComParam;
import egovframework.sys.service.Power.AuthCode;

public interface PowService {

  List<Power> selectDetail();

  boolean insert(List<Power> list);

  AuthCode selectMemDetail();

}
