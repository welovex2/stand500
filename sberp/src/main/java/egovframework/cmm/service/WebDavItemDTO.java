package egovframework.cmm.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WebDavItemDTO {
  private String davPath; // "/ERP/2025/12/..."
  private String name;
  private boolean directory;
  private Long size;
  private String lastModified;
  private boolean canWrite;
  private String uploadSrc;

  public String getDavPath() {
    return decode(davPath);
  }

  private String decode(String s) {
    if (s == null)
      return null;
    try {
      return URLDecoder.decode(s, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      // UTF-8은 사실상 안 터짐
      return s;
    }
  }

}
