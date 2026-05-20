package egovframework.ncc.dto;

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

  /** 파일의 MIME 타입. 예: "application/pdf", "image/jpeg". 폴더면 null. */
  private String mimeType;

  /** 프론트에서 "보기" 버튼을 노출할지 여부. 폴더는 false. */
  private boolean previewable;

  /** 미리보기 분류. "IMAGE" | "PDF" | "ONLYOFFICE" | "NONE". 폴더는 "NONE". */
  private String previewType;

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
