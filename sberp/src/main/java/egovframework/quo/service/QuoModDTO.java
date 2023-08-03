package egovframework.quo.service;

import javax.persistence.Column;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

public class QuoModDTO {
  @Getter
  @Setter
  @ApiModel(value = "QuoModDTO.Req", description = "견적서 수정 요청 등록")
  public static class Req extends QuoMod {
    @ApiModelProperty(value = "견적서번호", example = "Q2303-G0032")
    @Column
    private String quoId;
    
    @ApiModelProperty(value = "세금계산서 신청금액", example = "1000000")
    @Column
    private int bill;
  }

  @Getter
  @Setter
  @ApiModel(value = "QuoModDTO.Res", description = "견적서 수정 허용 조회")
  public static class Res extends QuoMod {

    @ApiModelProperty(value = "견적서번호", example = "Q2303-G0032")
    @Column
    private String quoId;

    @ApiModelProperty(value = "등록 날짜", example = "2023-03-15")
    @Column
    private String insDtStr;

    @ApiModelProperty(value = "작성자", example = "고객지원부 김정미")
    @Column
    private String insName;

    @ApiModelProperty(value = "상태", example = "수정요청")
    @Column
    private String stateType;
  }
}
