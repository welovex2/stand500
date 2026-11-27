package egovframework.raw.dto;

import java.util.List;

import javax.persistence.Column;

import com.fasterxml.jackson.annotation.JsonInclude;

import egovframework.raw.service.MethodImg;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class ImgDTO extends MethodImg {

  @ApiModelProperty(value = "시험고유번호", example = "15", hidden = true)
  @Column
  int testSeq;

  @ApiModelProperty(value = "시험사진 등록갯수", example = "1", hidden = true)
  @Column
  int regCnt;

  @ApiModelProperty(value = "기본정보 체크여부", example = "1", hidden = true)
  @Column
  int checkYn;
  
  @ApiModelProperty(value = "이미지 리스트", example = "")
  @Column
  List<PicDTO> imgList;

  @ApiModelProperty(value = "로데이터 수정 가능 여부 (1 가능, 0 프로젝트완료 등으로 잠김. 잠금 예외 사용자는 1). 상세 조회(pic/{picId}/detail.do) 응답에만 채움, 그 외 응답에는 없음", example = "1")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  Integer editYn;
}
