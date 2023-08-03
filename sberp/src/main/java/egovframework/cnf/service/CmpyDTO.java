package egovframework.cnf.service;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class CmpyDTO extends Cmpy {

  String partnerType;
  
  String directType;
  
  List<CmpyMng> mngList;
}
