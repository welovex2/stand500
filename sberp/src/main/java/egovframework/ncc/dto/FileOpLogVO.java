package egovframework.ncc.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FileOpLogVO {

  private Long logId;

  // who
  private String userId;
  private String dept; // optional (join해서 채워도 됨)

  // what
  private String sbkNo;
  private String opType; // UPLOAD / DOWNLOAD / PREVIEW / DELETE / RENAME / MOVE / COPY / MKDIR ...
  private String uploadSrc; // E(ERP) / A(모달) 등
  private String davPath; // 대상 경로
  private String srcPath; // MOVE/COPY 원본
  private String dstPath; // MOVE/COPY 목적지
  private String isFolder; // Y/N
  private String fileName;
  private String contentType;
  private Long fileSize; // upload size
  private Long bytesSent; // download/preview bytes

  // client
  private String clientIp;
  private String userAgent;

  // result
  private String resultCd; // START / SUCCESS / FAIL
  private String errMsg;
}

