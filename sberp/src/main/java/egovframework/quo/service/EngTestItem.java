package egovframework.quo.service;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class EngTestItem {

  @ApiModelProperty(value = "ENG_TEST_ITEM_SEQ ", example = "")
  @Column
  @NotNull
  private int engTestItemSeq;


  @ApiModelProperty(value = "QUO_ID ", example = "")
  @Column
  private String quoId;


  @ApiModelProperty(value = "ITEM ", example = "")
  @Column
  private String item;


  @ApiModelProperty(value = "DESCRIPTION ", example = "")
  @Column
  private String description;


  @ApiModelProperty(value = "MODEL ", example = "")
  @Column
  private String model;


  @ApiModelProperty(value = "AMOUNT ", example = "")
  @Column
  private String amount;


  @ApiModelProperty(value = "등록자 아이디 ", example = "", hidden = true)
  @Column
  private String insMemId;


  @ApiModelProperty(value = "등록 날짜시간 ", example = "", hidden = true)
  @Column
  private LocalDateTime insDt;


  @ApiModelProperty(value = "수정자 아이디 ", example = "", hidden = true)
  @Column
  private String udtMemId;


  @ApiModelProperty(value = "수정 날짜시간 ", example = "", hidden = true)
  @Column
  private LocalDateTime udtDt;


  @ApiModelProperty(value = "상태(I,U,D) ", example = "", hidden = true)
  @Column
  private String state;


}
