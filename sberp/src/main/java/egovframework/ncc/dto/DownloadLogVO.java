package egovframework.ncc.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DownloadLogVO {

  private Long logId;
  private String userId;
  private String sbkNo;
  private String davPath;
  private String isFolder;
  private String mode;
  private String fileName;
  private String zipName;
  private String contentType;
  private String clientIp;
  private String userAgent;
  private String resultCd;
  private String errMsg;
  private Long bytesSent;

}
