package egovframework.ncc.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WebDavListResponseDTO {

  private String davPath;
  private List<WebDavItemDTO> items;

}
