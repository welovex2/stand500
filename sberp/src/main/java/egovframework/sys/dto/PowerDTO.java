package egovframework.sys.dto;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class PowerDTO {

  @ApiModelProperty(value="MENU_SEQ ", example = "", required = true)
  @Column
  @NotNull
  private int menuSeq;
  
  @ApiModelProperty(value="MENU_NAME ", example = "", required = true)
  @Column
  @NotNull
  private String menuName;
  
  
  @ApiModelProperty(value="MENU_CODE ", example = "", required = true)
  @Column
  @NotNull
  private String menuCode;
  
  
  @ApiModelProperty(value="MENU_CODE ", example = "", required = true)
  @Column
  @NotNull
  private int disOrdr;
  
  
  @ApiModelProperty(value="읽기권한 ", example = "", required = true)
  @Column
  private boolean rYn;


  @ApiModelProperty(value="쓰기권한 ", example = "", required = true)
  @Column
  private boolean wYn;
  
}
