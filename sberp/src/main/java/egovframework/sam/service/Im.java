package egovframework.sam.service;

import java.time.LocalDateTime;
import javax.persistence.Column;
import org.springmodules.validation.bean.conf.loader.annotation.handler.NotNull;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class Im {
  
  @ApiModelProperty(value="IM_YM ", example = "")
  @Column
  @NotNull
  private String imYm;


  @ApiModelProperty(value="IM_SEQ ", example = "")
  @Column
  @NotNull
  private int imSeq;


  @ApiModelProperty(value="SBK_ID ", example = "")
  @Column
  @NotNull
  private String sbkId;


  @ApiModelProperty(value="시료특이사항 ", example = "")
  @Column
  private String memo;


  @ApiModelProperty(value="담당자 ", example = "")
  @Column
  private String mngId;


  @ApiModelProperty(value="사진파일ID ", example = "")
  @Column
  private String picUrl;


  @ApiModelProperty(value="등록자 아이디 ", example = "", hidden = true)
  @Column
  private String insMemId;


  @ApiModelProperty(value="등록 날짜시간 ", example = "", hidden = true)
  @Column
  private LocalDateTime insDt;


  @ApiModelProperty(value="수정자 아이디 ", example = "", hidden = true)
  @Column
  private String udtMemId;


  @ApiModelProperty(value="수정 날짜시간 ", example = "", hidden = true)
  @Column
  private LocalDateTime udtDt;


  @ApiModelProperty(value="상태(I,U,D) ", example = "", hidden = true)
  @Column
  private String state;


}
