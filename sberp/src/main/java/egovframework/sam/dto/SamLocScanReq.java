package egovframework.sam.dto;

import java.util.List;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel(description = "위치 스캔(구역 먼저 + 시료 N건)")
public class SamLocScanReq {

  @ApiModelProperty(value = "구역 SEQ. 프론트 QR 에서 읽은 값", example = "3", required = true)
  private int zoneSeq;

  @ApiModelProperty(value = "시료 라벨 ID 목록", required = true)
  private List<String> imSubIds;

  @ApiModelProperty(value = "메모", example = "")
  private String memo;
}
