package egovframework.ppc.service;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Pp {
  @ApiModelProperty(value="PP_YM ", example = "")
  @Column
  private int ppYm;


  @ApiModelProperty(value="PP_SEQ ", example = "")
  @Column
  private int ppSeq;


  @ApiModelProperty(value="해당견적서번호 ", example = "")
  @Column
  private String quoId;


  @ApiModelProperty(value="CMPY_SEQ ", example = "")
  @Column
  private int cmpySeq;


  @ApiModelProperty(value="CMPY_MNG_SEQ ", example = "")
  @Column
  private int cmpyMngSeq;


  @ApiModelProperty(value="업체명 ", example = "")
  @Column
  private String cmpnyName;


  @ApiModelProperty(value="대표자이름 ", example = "")
  @Column
  private String rprsn;


  @ApiModelProperty(value="대표자연락처 ", example = "")
//  @Pattern(regexp = "^[0-9-]*$", message = "연락처는 10 ~ 11 자리의 숫자만 입력 가능합니다.") 
  @Column
  private String rprsnPhone;


  @ApiModelProperty(value="대표자이메일 ", example = "")
  @Column
  private String rprsnEmail;


  @ApiModelProperty(value="담당자이름 ", example = "")
  @Column
  private String contact;


  @ApiModelProperty(value="담당자연락처 ", example = "")
//  @Pattern(regexp = "^[0-9-]*$", message = "연락처는 10 ~ 11 자리의 숫자만 입력 가능합니다.") 
  @Column
  private String contactPhone;


  @ApiModelProperty(value="담당자이메일 ", example = "")
  @Column
  private String contactEmail;


  @ApiModelProperty(value="제조사 ", example = "", required = true)
  @Column
  private String mnfctCmpny;


  @ApiModelProperty(value="제조국 ", example = "")
  @Column
  private String mnfctCntry;


  @ApiModelProperty(value="제조자주소 ", example = "")
  @Column
  private String mnfctAdres;


  @ApiModelProperty(value="제품명 ", example = "", required = true)
  @Column
  private String prdctName;


  @ApiModelProperty(value="모델명 ", example = "", required = true)
  @Column
  private String modelName;


  @ApiModelProperty(value="BL ", example = "", required = true)
  @Column
  private String bl;


  @ApiModelProperty(value="HS_CODE ", example = "", required = true)
  @Column
  private String hsCd;


  @ApiModelProperty(value="시료갯수 ", example = "", required = true)
  @Column
  private int imQty;


  @ApiModelProperty(value="유니패스 ", example = "")
  @Column
  private String unipass;


  @ApiModelProperty(value="상담내용 ", example = "")
  @Column
  private String memo;

  
  @ApiModelProperty(value="수입 요건번호 ", example = "")
  @Column
  private String reqNumber;
  
  
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
