package egovframework.sam.dto;

import java.util.List;
import egovframework.sam.service.ImSubLoc;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel(description = "모바일 시료 단건 상세")
public class ImSubItemDetailDTO {

  @ApiModelProperty(value = "시료 라벨 ID", example = "SB26-G0030-M01")
  private String imSubId;

  @ApiModelProperty(value = "IM_ID", example = "IM26-G0001")
  private String imId;

  @ApiModelProperty(value = "신청서번호", example = "SB26-G0030")
  private String sbkId;

  @ApiModelProperty(value = "시료 순번", example = "1")
  private int imSubSeq;

  @ApiModelProperty(value = "본품/구성품 코드(TI)", example = "1")
  private String divCode;

  @ApiModelProperty(value = "본품/구성품", example = "본품")
  private String divName;

  @ApiModelProperty(value = "시료 모델명", example = "모델명")
  private String memo;

  @ApiModelProperty(value = "반입일", example = "2026-07-20")
  private String carryInDate;

  @ApiModelProperty(value = "반입형태 코드(TC)", example = "2")
  private String carryInCode;

  @ApiModelProperty(value = "반입형태", example = "직접전달")
  private String carryInType;

  @ApiModelProperty(value = "수량", example = "1")
  private int qty;

  @ApiModelProperty(value = "접수자", example = "홍길동")
  private String rcptName;

  @ApiModelProperty(value = "신청서 파일서버 상대경로", example = "2026/07/SB26-G0030")
  private String ncFolderPath;

  @ApiModelProperty(value = "업로드 폴더 선택 목록")
  private List<SamUploadFolderDTO> uploadFolders;

  private Integer currentZoneSeq;
  private Integer strSeq;
  private String whName;
  private String zoneName;
  private String carryOutCode;
  private List<ImSubLoc> locHist;
}
