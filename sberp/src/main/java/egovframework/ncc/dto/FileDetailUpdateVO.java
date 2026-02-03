package egovframework.ncc.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FileDetailUpdateVO {

  private String oldDavPath;
  private String newDavPath;
  private String newOriginalFileName;
  private String fileExtsn;
  private String updtId;

}
