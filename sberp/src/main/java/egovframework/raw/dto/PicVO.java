package egovframework.raw.dto;

import java.util.List;

import javax.persistence.Column;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PicVO {

	@ApiModelProperty(value="시험장면사진", example = "")
	@Column
	private List<PicDTO> picList;
	
}
