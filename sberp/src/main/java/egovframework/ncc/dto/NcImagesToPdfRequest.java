package egovframework.ncc.dto;

import java.util.List;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel(value = "NcImagesToPdfRequest", description = "이미지 PDF 변환 요청")
@Getter
@Setter
@ToString
public class NcImagesToPdfRequest {

  @ApiModelProperty(
      value = "변환할 이미지 davPath 목록 (사용자 선택 순서 유지, 동일 폴더만)",
      required = true,
      example = "[\"/ERP/2026/05/SB26-G0000/00.시험사진/a.jpg\", \"/ERP/2026/05/SB26-G0000/00.시험사진/b.png\"]")
  private List<String> paths;

  @ApiModelProperty(
      value = "저장 API 전용. 동일 파일명 PDF가 이미 있을 때 덮어쓰기 여부 (기본 false)",
      required = false)
  private boolean overwrite;

}
