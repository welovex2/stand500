package egovframework.raw.service;

import java.time.LocalDateTime;

import javax.persistence.Column;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel(value = "MethodImg", description = "시험장면사진 저장")
@Getter
@Setter
@ToString
public class MethodImg {

  @ApiModelProperty(value = "IMG_SEQ ", example = "")
  @Column
  private int imgSeq;


  @ApiModelProperty(value = "RAW_SEQ ", example = "")
  @Column
  private int rawSeq;


  @ApiModelProperty(value = "시험장면사진 순번 : 공통코드 RP ", example = "")
  @Column
  private String picId;


  @ApiModelProperty(value = "해당됨/해당없음 ", example = "")
  @Column
  private int picYn;


  @ApiModelProperty(value = "파일저정되어있는 ID ", example = "")
  @Column
  private String atchFileId;


  @ApiModelProperty(value = "등록자 아이디 ", example = "", hidden = true)
  @Column
  private String insMemId;


  @ApiModelProperty(value = "등록 날짜시간 ", example = "", hidden = true)
  @Column
  private LocalDateTime insDt;


  @ApiModelProperty(value = "수정자 아이디 ", example = "", hidden = true)
  @Column
  private String udtMemId;


  @ApiModelProperty(value = "수정 날짜시간 ", example = "", hidden = true)
  @Column
  private LocalDateTime udtDt;


  @ApiModelProperty(value = "상태(I,U,D) ", example = "", hidden = true)
  @Column
  private String state;


}
