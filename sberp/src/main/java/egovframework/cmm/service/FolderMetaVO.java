package egovframework.cmm.service;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FolderMetaVO {

  private String folderPath; // decoded path
  private String pathHash; // SHA-256
  private String uploadSrc; // 'E' / 'A'
  private String creatId; // 생성자

}
