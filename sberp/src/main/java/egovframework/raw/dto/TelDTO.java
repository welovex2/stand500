package egovframework.raw.dto;

import java.util.List;
import javax.persistence.Column;
import egovframework.raw.service.MethodTel;
import egovframework.raw.service.RawMac;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class TelDTO extends MethodTel {

  @ApiModelProperty(value = "시험고유번호", example = "15", hidden = true)
  @Column
  int testSeq;

}
