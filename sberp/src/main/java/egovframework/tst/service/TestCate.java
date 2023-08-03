package egovframework.tst.service;

import java.time.LocalDateTime;

import javax.persistence.Column;

import org.springmodules.validation.bean.conf.loader.annotation.handler.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TestCate {

  @ApiModelProperty(value = "TEST_CATE_SEQ ", example = "", hidden = true)
  @Column
  @NotNull
  private int testCateSeq;

  @ApiModelProperty(value = "DEPTH ", example = "", hidden = true)
  @Column
  private int depth;


  @ApiModelProperty(value = "NAME ", example = "", hidden = true)
  @Column
  private String name;


  @ApiModelProperty(value = "상위카테고리 SEQ ", example = "", hidden = true)
  @Column
  private int topDepthSeq;

  @ApiModelProperty(value = "MEMO ", example = "", hidden = true)
  @Column
  private String memo;

  @ApiModelProperty(value = "INS_MEM_ID ", example = "", hidden = true)
  @Column
  private String insMemId;


  @ApiModelProperty(value = "INS_DT ", example = "", hidden = true)
  @Column
  private LocalDateTime insDt;


  @ApiModelProperty(value = "UDT_MEM_ID ", example = "", hidden = true)
  @Column
  private String udtMemId;


  @ApiModelProperty(value = "UDT_DT ", example = "", hidden = true)
  @Column
  private LocalDateTime udtDt;


  @ApiModelProperty(value = "상태(I,U,D) ", example = "", hidden = true)
  @Column
  private String state;


}
