package egovframework.tst.dto;

import javax.persistence.Column;
import egovframework.tst.service.TestItem;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(description = "시험항목 및 비용")
public class TestItemDTO extends TestItem {

  @ApiModelProperty(value = "시험번호", example = "")
  @Column
  private String testId;
  
  @ApiModelProperty(value = "인증종류(국가)", example = "", hidden = true)
  @Column
  private String crtfc1;

  @ApiModelProperty(value = "인증종류1", example = "", hidden = true)
  @Column
  private String crtfc2;

  @ApiModelProperty(value = "인증종류2", example = "", hidden = true)
  @Column
  private String crtfc3;

  @ApiModelProperty(value = "인증종류3", example = "", hidden = true)
  @Column
  private String crtfc4;
  
  @ApiModelProperty(value = "시험항목", example = "", hidden = true)
  @Column
  private String testCate;

  @ApiModelProperty(value = "시험부서", example = "", hidden = true)
  @Column
  private String testType;

  @ApiModelProperty(value = "전송타입", example = "")
  @Column
  private String sendType;

  @ApiModelProperty(value = "인증종류(국가) ", example = "방송통신 기", hidden = true)
  @Column
  private String crtfc1Name;

  @ApiModelProperty(value = "인증종류1 ", example = "적합인증(방", hidden = true)
  @Column
  private String crtfc2Name;

  @ApiModelProperty(value = "인증종류2 ", example = "적합인증(방", hidden = true)
  @Column
  private String crtfc3Name;
  
  @ApiModelProperty(value = "인증종류3 ", example = "적합인증(방", hidden = true)
  @Column
  private String crtfc4Name;
  
  @ApiModelProperty(value = "시험규격 ", example = "603 (전기", hidden = true)
  @Column
  private String testStndr;

  @ApiModelProperty(value = "서명상태 ", example = "서명요청완료")
  @Column
  private String signState;

  @ApiModelProperty(value = "로데이터여부 ", example = "미완료", hidden = true)
  @Column
  private String rawYn;

  @ApiModelProperty(value = "시험번호 ", example = "5")
  @Column
  private int testSeq;

  @ApiModelProperty(value = "시험상태 ", example = "")
  @Column
  private String testState;
  
  @ApiModelProperty(value = "시험신청자 부서 ", example = "", hidden = true)
  @Column
  private String testMemDept;
  
  @ApiModelProperty(value = "시험신청자 ", example = "", hidden = true)
  @Column
  private String testMem;

  @ApiModelProperty(value = "시험신청일 ", example = "", hidden = true)
  @Column
  private String testInDt;
  
  @ApiModelProperty(value = "게시판글수 ", example = "", hidden = true)
  @Column
  private int memoCnt;
}
