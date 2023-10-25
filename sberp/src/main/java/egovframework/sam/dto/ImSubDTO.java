package egovframework.sam.dto;

import javax.persistence.Column;
import org.springmodules.validation.bean.conf.loader.annotation.handler.NotNull;
import egovframework.sam.service.ImSub;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString(callSuper = true)
public class ImSubDTO extends ImSub {
  
  @ApiModelProperty(value="번호", example = "")
  @Column
  @NotNull
  private int no;
  
  private String sbkId;
  private String imSubId;
  private String rcptDt;
  private String carryInName;
  private String carryInType;
  private String carryOutName;
  private String carryOutType;
  private String cmpyName;
  private String prdctName;
  private String modelName;
  private String cmpyTitle;
  private String mngName;
}
