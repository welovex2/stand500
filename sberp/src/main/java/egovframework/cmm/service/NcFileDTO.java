package egovframework.cmm.service;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class NcFileDTO {

  private String name;        // 파일명
  private String davPath;     // "/ERP/2025/12/RAW/a.jpg" 형태
  private boolean directory;  // 폴더 여부
  private long size;          // 파일 크기
  private String contentType; // mime
  private String lastModified;
  private String downloadUrl; // public raw url (파일만)
  
  
}
