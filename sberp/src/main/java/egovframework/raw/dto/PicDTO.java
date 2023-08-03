package egovframework.raw.dto;

import javax.validation.constraints.Null;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
@ApiModel(value="PicDTO", description = "14. Test Set-up Configuraiotn for EUT 외 사진과타이틀")
public class PicDTO {

	@Null
	private MultipartFile image;
	private String title;
	private String mode;
	private String imageUrl;
	private String picId;
	private String fileSn;
	// 성적서에만 사용
	private int picYn;
}
