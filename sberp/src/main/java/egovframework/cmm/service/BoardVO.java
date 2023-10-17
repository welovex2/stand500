package egovframework.cmm.service;

import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Column;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
@ApiModel(value = "BoardVO", description = "게시판")
public class BoardVO {
  
  @ApiModelProperty(value="게시물 아이디 ", example = "")
  @Column
  private int nttId = 0;
  
  @ApiModelProperty(value="게시판 아이디 ", example = "")
  @Column
  private String bbsId = "";
  
  @ApiModelProperty(value="게시물 번호 ", example = "")
  @Column
  private long nttNo = 0L;
  
  @ApiModelProperty(value="게시물 제목 ", example = "")
  @Column
  private String nttSj = "";
  
  @ApiModelProperty(value="게시물 내용 ", example = "")
  @Column
  private String nttCn = "";
  
  @ApiModelProperty(value="정렬순서 ", example = "")
  @Column
  private int sortOrdr = 0;
 
  @ApiModelProperty(value="조회수 ", example = "")
  @Column
  private int rdcnt = 0;
  
  @ApiModelProperty(value="게시시작일 ", example = "")
  @Column
  private String ntceBgnde = "";

  @ApiModelProperty(value="게시종료일 ", example = "")
  @Column
  private String ntceEndde = "";
  
  @ApiModelProperty(value="패스워드 ", example = "")
  @Column
  private String password = "";
  
  @ApiModelProperty(value="비밀글여부 ", example = "")
  @Column
  private String secretYn = "";
  
  @ApiModelProperty(value="게시물 첨부파일 아이디 ", example = "")
  @Column
  private String atchFileId = "";
  
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
  
  @ApiModelProperty(value = "등록 날짜시간 ", example = "")
  @Column
  private String insDtStr;
  
  // 조회시 파일리스트 확인
  private List<FileVO> fileList;
    
}
