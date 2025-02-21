package egovframework.sys.service;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.validation.constraints.Null;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel(value = "MacCal", description = "시험장비 교정정보")
public class MacCal {

  @Column(name = "MACHINE_CAL_SEQ")
  private Integer machineCalSeq; // 신규 교정정보 시퀀스 (PK)

  @ApiModelProperty(value = "시험장비 번호", example = "")
  @Column(name = "MACHINE_SEQ", nullable = false)
  private int machineSeq; // 기존 장비번호 사용 (PK)

  @ApiModelProperty(value = "교정정보 대상", example = "")
  @Column(name = "CAL_TGT")
  private String calTgt; // 신규 교정정보(대상)

  @ApiModelProperty(value = "교정일자", example = "2025-02-12")
  @Column(name = "CAL_DT")
  private String calDt; // 신규 교정정보(교정일자)

  @ApiModelProperty(value = "교정기관", example = "")
  @Column(name = "CAL_ORG")
  private String calOrg; // 신규 교정정보(교정기관)

  @ApiModelProperty(value = "교정번호", example = "")
  @Column(name = "CAL_NO")
  private String calNo; // 신규 교정정보(교정번호)

  @ApiModelProperty(value = "교정 성적서 파일명", example = "")
  @Column(name = "CAL_FILE")
  private String calFile; // 신규 교정정보(성적서파일)

  @ApiModelProperty(value = "교정 성적서 파일다운로드 주소", example = "")
  @Column(name = "CAL_FILE")
  private String calFileLink; // 신규 교정정보(성적서파일)
  
  @ApiModelProperty(value = "차기 교정일", example = "2026-02-12")
  @Column(name = "REFORM_DT")
  private String reformDt; // 기존 교정정보(차기교정일)

  @ApiModelProperty(value = "교정 주기(년)", example = "1")
  @Column(name = "REFORM_PERIOD")
  private String reformPeriod; // 기존 교정정보(교정주기(년))
  
  @ApiModelProperty(value = "교정 주기(년)", example = "1")
  private String reformPeriodName;
  
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


  @ApiModelProperty(value = "상태(I,U,D) ", example = "")
  @Column
  private String state;
  
  @Null
  private MultipartFile file;
  
  @ApiModelProperty(value = "파일명 ", example = "")
  private String calFileNm;
  
}
