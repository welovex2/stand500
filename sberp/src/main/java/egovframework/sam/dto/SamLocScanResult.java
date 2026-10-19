package egovframework.sam.dto;

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
@ApiModel(description = "위치 스캔 결과")
public class SamLocScanResult {

  @ApiModelProperty(value = "처리 건수")
  private int savedCnt;

  @ApiModelProperty(value = "같은 신청서 시료가 다른 위치에 있을 때 경고")
  private List<String> warnings = new ArrayList<String>();
}
