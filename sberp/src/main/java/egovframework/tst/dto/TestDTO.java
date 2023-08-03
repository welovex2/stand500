package egovframework.tst.dto;

import javax.persistence.Column;
import egovframework.tst.service.Test;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public class TestDTO {

  @Getter
  @Setter
  @ToString(callSuper = true)
  @ApiModel(value = "TestDTO.Req", description = "시험 등록")
  public static class Req extends Test {
    @ApiModelProperty(value = "메모 ", example = "바빠서..")
    @Column
    private String memo;

    @ApiModelProperty(value = "시험상태 번호 ", example = "1")
    @Column
    private int testStateSeq;

    @ApiModelProperty(value = "시험상태 ", example = "1")
    @Column
    private String stateCode;

    @ApiModelProperty(value = "시험담당자 ID ", example = "c")
    @Column
    private String testMngId;
  }

  @Getter
  @Setter
  @ApiModel(value = "TestDTO.Res", description = "시험 조회")
  public static class Res extends Test {

  }
}
