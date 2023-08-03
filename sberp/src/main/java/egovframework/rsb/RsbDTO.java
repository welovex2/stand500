package egovframework.rsb;

import javax.persistence.Column;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel(description = "재발행 신청서")
public class RsbDTO {

  @Getter
  @Setter
  @ToString
  @ApiModel(value = "RsbDTO.Req", description = "재발행 신청서 등록")
  public static class Req {
    
    private String sbkId;
    private int quoSeq;
    private int revision;
    private String insMemId;
    @Column
    private String udtMemId;
    
  }
  
  @Getter
  @Setter
  @ApiModel(value = "RsbDTO.Res", description = "재발행 신청서 조회")
  public static class Res {
    
  }


  
}
