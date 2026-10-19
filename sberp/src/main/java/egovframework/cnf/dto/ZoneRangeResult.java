package egovframework.cnf.dto;

import java.util.List;
import egovframework.cnf.service.Zone;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel(description = "구역 범위등록 결과")
public class ZoneRangeResult {

  @ApiModelProperty(value = "등록 건수", example = "11")
  private int addedCnt;

  @ApiModelProperty(value = "등록된 구역")
  private List<Zone> zones;

  @ApiModelProperty(value = "이미 사용중이라 제외한 구역명")
  private List<String> skippedNames;
}
