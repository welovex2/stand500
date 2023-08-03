package egovframework.tst.service;

import java.time.LocalDateTime;

import javax.persistence.Column;

import org.springmodules.validation.bean.conf.loader.annotation.handler.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel(description = "시험")
@Getter
@Setter
@ToString
public class Test {


  @ApiModelProperty(value = "시험 고유번호 ", example = "")
  @Column
  private int testSeq;


  @ApiModelProperty(value = "시험부서별 서브 시험번호 ", example = "")
  @Column
  private int testNo;


  @ApiModelProperty(value = "시험항목 고유번호 ", example = "")
  @Column
  private int testItemSeq;


  @ApiModelProperty(value = "시험부서코드 ", example = "EM")
  @Column
  private String testTypeCode;


  @ApiModelProperty(value = "성적서 발급일 ", example = "2023-05-30")
  @Column
  private String reportDt;


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
