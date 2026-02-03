package egovframework.ncc.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class NcRenameRequest {

  private String path;
  private String newName;
  private boolean overwrite;

}
