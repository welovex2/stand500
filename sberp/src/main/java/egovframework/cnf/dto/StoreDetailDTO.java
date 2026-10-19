package egovframework.cnf.dto;

import java.util.List;
import egovframework.cnf.service.Warehouse;
import egovframework.cnf.service.Zone;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@ApiModel(description = "창고 상세")
public class StoreDetailDTO extends Warehouse {

  @ApiModelProperty(value = "구역리스트", notes = "사용중지 구역도 포함")
  private List<Zone> zones;
}
