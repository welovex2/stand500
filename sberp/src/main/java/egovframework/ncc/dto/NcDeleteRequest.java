package egovframework.ncc.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class NcDeleteRequest {

  private String path;
  private boolean recursive;

}
