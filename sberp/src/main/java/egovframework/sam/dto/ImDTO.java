package egovframework.sam.dto;

import java.util.List;
import javax.persistence.Column;
import egovframework.cmm.service.FileVO;
import egovframework.sam.service.Im;
import egovframework.sam.service.ImSub;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString(callSuper = true)
public class ImDTO extends Im {

  String imId;
  
  String mngName;
  
  String deptSeq;
  
  String deptName;
  
  List<ImSub> itemList;
  
  @ApiModelProperty(value="시료사진 리스트", example = "")
  @Column
  private List<FileVO> picList;
}
