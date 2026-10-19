package egovframework.wrp.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel(description = "막힌 시험 명단")
public class StuckItem {

  @ApiModelProperty(value = "시험부 코드")
  private String testTypeCode;

  @ApiModelProperty(value = "시험부명")
  private String testType;

  @ApiModelProperty(value = "시험번호")
  private String testId;

  @ApiModelProperty(value = "시험 SEQ")
  private int testSeq;

  @ApiModelProperty(value = "고객명")
  private String cmpyName;

  @ApiModelProperty(value = "모델명")
  private String model;

  @ApiModelProperty(value = "시험상태")
  private String testState;

  @ApiModelProperty(value = "담당자")
  private String mngName;

  @ApiModelProperty(value = "현재 상태 경과일")
  private int stuckDays;

  @ApiModelProperty(value = "상태 변경일")
  private String stateDt;

  @ApiModelProperty(value = "게시판 최신글")
  private String stateMemo;

  @ApiModelProperty(value = "게시판 최신글 작성 정보")
  private String boardHead;

  @ApiModelProperty(value = "홀딩 원인 구분(업체사정/확인필요)")
  private String causeType;

  @ApiModelProperty(value = "홀딩 사유 짧은 표시")
  private String causeMemo;

  public String getBoardTitle() {
    if (stateMemo == null || stateMemo.isEmpty()) {
      return "";
    }
    if (boardHead == null || boardHead.isEmpty()) {
      return stateMemo;
    }
    return boardHead + "\n" + stateMemo;
  }
}
