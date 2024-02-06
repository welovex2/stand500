package egovframework.raw.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RegStateDTO {

  private int rawSeq;
  private int rawYn;
  private int ceYn;
  private int reYn;
  private int EdYn;
  private int rsYn;
  private int etYn;
  private int sgYn;
  private int csYn;
  private int mfYn;
  private int vdYn;
  private int ctYn;
  private int pcYn;

  /**
   * 9814 추가
   */
  private int ckYn;
  private int dpYn;
  private int re1Yn;
  private int re2Yn;
  private int re3Yn;
  private int ceCheckYn;
  private int ckCheckYn;
  private int dpCheckYn;
  private int re1CheckYn;
  private int re2CheckYn;
  private int re3CheckYn;
  private int edCheckYn;
  private int rsCheckYn;
  private int etCheckYn;
  private int sgCheckYn;
  private int csCheckYn;
  private int vdCheckYn;
  /**
   * --END 9814 추가
   */
  
  /**
   * Tel 추가
   */
  private int telYn;
  /**
   * --END Tel 추가
   */
}
