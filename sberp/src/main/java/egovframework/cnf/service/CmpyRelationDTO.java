package egovframework.cnf.service;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class CmpyRelationDTO {

  private int no;
  private int childCmpySeq;
  private int parentCmpySeq;
  private String prntCmpyName;
  private String childCmpyName;
  private String insMemId;
  private String udtMemId;
  
}
