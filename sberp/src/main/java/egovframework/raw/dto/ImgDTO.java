package egovframework.raw.dto;

import java.util.List;

import javax.persistence.Column;

import egovframework.raw.service.MethodImg;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString(callSuper = true)
public class ImgDTO extends MethodImg {

	@ApiModelProperty(value="시험고유번호", example = "15", hidden = true)
	@Column
	int testSeq;

	@ApiModelProperty(value="이미지 리스트", example = "")
	@Column
	List<PicDTO> imgList;
}
