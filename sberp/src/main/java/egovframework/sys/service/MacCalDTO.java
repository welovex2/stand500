package egovframework.sys.service;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class MacCalDTO {

  MacCal macCal;
  List<MacCal> uptFileList;
}
