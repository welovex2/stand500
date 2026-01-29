package egovframework.cmm.service;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SbkInfoVO {

  private String sbkId; // 신청서번호
  private String ncFolderPath; // 파일서버 신청서 기본 폴더
  private String atchFileId; // 파일 그룹 ID
  private Date insDt;

}
