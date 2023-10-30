package egovframework.cnf.service;

import java.util.List;
import egovframework.cmm.service.FileVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class CmpyDTO extends Cmpy {

  String partnerType;
  
  String directType;
  
  List<CmpyMng> mngList;
  
  // 조회시 파일리스트 확인
  List<FileVO> fileList;
}
