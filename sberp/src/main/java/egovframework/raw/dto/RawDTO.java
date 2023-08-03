package egovframework.raw.dto;

import java.util.List;

import javax.persistence.Column;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel(description = "로데이터 사진파일들")
@Getter @Setter @ToString(callSuper = true)
public class RawDTO {

	@ApiModelProperty(value="시험자 서명", example = "")
	@Column
	private MultipartFile testSign;

	@ApiModelProperty(value="기술책임자 서명", example = "")
	@Column
	private MultipartFile revSign;
	
	@ApiModelProperty(value="EUT Modifications (보완사항)", example = "")
	@Column
	private List<MultipartFile> modFileList;
	
	@ApiModelProperty(value="Test Set-up Configuraiotn for EUT", example = "")
	@Column
	private List<PicDTO> setupList;

} 
