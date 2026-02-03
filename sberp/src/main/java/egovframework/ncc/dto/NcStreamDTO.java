package egovframework.ncc.dto;

import java.io.InputStream;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class NcStreamDTO {

  private int statusCode;
  private InputStream inputStream;
  private String contentType;
  private Long contentLength;
  private String contentRange;
  private String acceptRanges;

}
