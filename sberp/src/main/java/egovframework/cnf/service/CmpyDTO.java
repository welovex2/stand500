package egovframework.cnf.service;

import java.util.List;
import egovframework.cmm.service.FileVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class CmpyDTO extends Cmpy {

  @ApiModelProperty(value = "게시글번호", example = "1")
  int no;
  
  String partnerType;
  
  String directType;
  
  String prntCmpyName;
  
  String prntType; 
  
  String insDtStr;
  
  String cntry;
  
  String insName;
    
  // 연결된 협력사 리스트
  List<Integer> prntCmpySeqList;
  List<CmpyRelationDTO> prntCmpyList;
  List<CmpyRelationDTO> childCmpyList;
  
  List<CmpyMng> mngList;
  
  // 조회시 파일리스트 확인
  List<FileVO> fileList;
}
