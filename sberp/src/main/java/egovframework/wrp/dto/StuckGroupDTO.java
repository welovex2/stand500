package egovframework.wrp.dto;

import java.util.ArrayList;
import java.util.List;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel(description = "부서(또는 출장)별 막힘 현황")
public class StuckGroupDTO {

  @ApiModelProperty(value = "주간 3숫자")
  private StuckSummary summary;

  @ApiModelProperty(value = "홀딩 7일 이상 상위 5건")
  private List<StuckItem> holdingList = new ArrayList<StuckItem>();

  @ApiModelProperty(value = "RD지연 7일 이상 상위 5건")
  private List<StuckItem> rdDelayList = new ArrayList<StuckItem>();
}
