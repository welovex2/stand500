package egovframework.cmm.service;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ApiLogVO {
  private String userId;
  private String requestUri;
  private String method;
  private String remoteIp;
}
