package egovframework.raw.service;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel(description = "로데이터 > 측정설비")
@Getter @Setter @ToString
public class RawMac {
	
	@ApiModelProperty(value="RAW_MAC_SEQ ", example = "", hidden = true)
	@Column
	@NotNull
	private int rawMacSeq;


	@ApiModelProperty(value="MACHINE_TYPE(공통코드 TM)", example = "CE", hidden = true)
	@Column
	@NotNull
	private String machineType;


	@ApiModelProperty(value="RAW_SEQ ", example = "", hidden = true)
	@Column
	@NotNull
	private int rawSeq;


	@ApiModelProperty(value="MACHINE_SEQ ", example = "")
	@Column
	@NotNull
	private int machineSeq;


	@ApiModelProperty(value="USE_YN ", example = "")
	@Column
	private int useYn;


}
