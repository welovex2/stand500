package egovframework.raw.service;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel(description = "Type of Cable Used (접속 케이블)")
@Getter @Setter @ToString
public class RawCable {
	
	@ApiModelProperty(value="CABLE_SEQ ", example = "")
	@Column
	@NotNull
	private int cableSeq;


	@ApiModelProperty(value="RAW_SEQ ", example = "")
	@Column
	@NotNull
	private String rawSeq;


	@ApiModelProperty(value="접속 시작 장치_명칭 ", example = "")
	@Column
	private String dfName;


	@ApiModelProperty(value="접속 시작 장치_MODE ", example = "")
	@Column
	private String dfMode;


	@ApiModelProperty(value="접속 시작 장치_PORT ", example = "")
	@Column
	private String dfPort;


	@ApiModelProperty(value="접속 끝 장치_명칭 ", example = "")
	@Column
	private String dtName;


	@ApiModelProperty(value="접속 끝 장치_PORT ", example = "")
	@Column
	private String dtPort;


	@ApiModelProperty(value="케이블규격_길이 ", example = "")
	@Column
	private String csM;


	@ApiModelProperty(value="케이블규격_차폐여부 (1:Shi, 0:UnShi) ", example = "")
	@Column
	private String csYn;

	@ApiModelProperty(value="상태", notes="I:신규등록, U:수정, D:삭제한 항목", example = "")
	@Column
	private String state;

}
