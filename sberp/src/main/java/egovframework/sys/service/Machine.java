package egovframework.sys.service;

import java.time.LocalDateTime;
import javax.persistence.Column;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel(value = "Machine", description = "시험장비")
public class Machine {

  @ApiModelProperty(value = "장비번호", example = "1001")
  @Column(name = "MACHINE_SEQ")
  private int machineSeq; // 장비번호 (PK)

  @ApiModelProperty(value = "관리부서 코드", example = "D001")
  @Column(name = "MGMT_DEPT", length = 10)
  private String mgmtDept; // 관리부서 (신규)

  @ApiModelProperty(value = "관리번호 (부서별 카운팅)", example = "12345")
  @Column(name = "MGMT_NO", length = 20)
  private int mgmtNo; // 관리번호 (부서별 카운팅) (신규)

  @ApiModelProperty(value = "장비명", example = "측정기 A")
  @Column(name = "NAME", length = 100)
  private String name; // 장비명

  @ApiModelProperty(value = "한글 태그명", example = "측정기")
  @Column(name = "TAG_NAME_KOR", length = 100)
  private String tagNameKor; // 한글태그명 (신규)

  @ApiModelProperty(value = "모델명", example = "M-500")
  @Column(name = "MODEL", length = 45)
  private String model; // 모델명

  @ApiModelProperty(value = "제조사", example = "ABC Company")
  @Column(name = "MNFCT_CMPNY", length = 45)
  private String mnfctCmpny; // 제조사

  @ApiModelProperty(value = "제조번호", example = "SN-001")
  @Column(name = "MNFCT_SERIAL", length = 45)
  private String mnfctSerial; // 제조번호

  @ApiModelProperty(value = "규격", example = "")
  @Column(name = "MNFCT_SERIAL", length = 45)
  private String stndr; // 제조번호

  @ApiModelProperty(value = "입수일", example = "2025-02-12")
  @Column(name = "RCV_DT")
  private String rcvDt; // 입수일

  @ApiModelProperty(value = "사용일", example = "2025-02-12")
  @Column(name = "USE_DT")
  private String useDt; // 사용일
  
  @ApiModelProperty(value = "국내 여부", example = "true")
  @Column(name = "CLSF_DOM_YN")
  private Boolean clsfDomYn; // 분류(국내여부)

  @ApiModelProperty(value = "도입 여부", example = "true")
  @Column(name = "CLSF_IMP_YN")
  private Boolean clsfImpYn; // 분류(도입여부)

  @ApiModelProperty(value = "신규 여부", example = "true")
  @Column(name = "CLSF_NEW_YN")
  private Boolean clsfNewYn; // 분류(신규여부)

  @ApiModelProperty(value = "중고 여부", example = "false")
  @Column(name = "CLSF_USD_YN")
  private Boolean clsfUsdYn; // 분류(중고여부)

  @ApiModelProperty(value = "장비 가격", example = "500000")
  @Column(name = "PRICE")
  private int price; // 금액 (신규)

  @ApiModelProperty(value = "장비 위치", example = "B동 2층")
  @Column(name = "LOCATION", length = 100)
  private String loc; // 장비위치 (신규)

  @ApiModelProperty(value = "장비 사진", example = "photo1.jpg")
  @Column(name = "PHOTO", length = 255)
  private String photo; // 장비사진 (신규)

  @ApiModelProperty(value = "사양", example = "고정밀 측정기")
  @Column(name = "SPEC", length = 255)
  private String spec; // 사양

  @ApiModelProperty(value = "측정 범위", example = "0-100mm")
  @Column(name = "CAL_RANGE", length = 100)
  private String measRange; // 측정범위 (신규)

  @ApiModelProperty(value = "실제 교정 범위", example = "0-100mm")
  @Column(name = "MEAS_RANGE", length = 100)
  private String calRange;
  
  @ApiModelProperty(value = "교정 대상", example = "Y")
  @Column(name = "CAL_TARGET", length = 1)
  private String calTgt; // 교정정보(대상) (신규)

  @ApiModelProperty(value = "교정일자", example = "2025-02-12")
  @Column(name = "CAL_DATE")
  private String calDt; // 교정정보(교정일자) (신규)

  @ApiModelProperty(value = "교정 주기 (년)", example = "1")
  @Column(name = "REFORM_PERIOD", length = 20)
  private String reformPeriod; // 교정정보(교정주기(년))

  @ApiModelProperty(value = "차기 교정일", example = "2026-02-12")
  @Column(name = "REFORM_DT")
  private String reformDt; // 교정정보(차기교정일)

  @ApiModelProperty(value = "교정 기관", example = "KTL")
  @Column(name = "CAL_AGENCY", length = 100)
  private String calOrg; // 교정정보(교정기관) (신규)

  @ApiModelProperty(value = "교정 번호", example = "CAL-2025-001")
  @Column(name = "CAL_NO", length = 50)
  private String calNo; // 교정정보(교정번호) (신규)

  @ApiModelProperty(value = "교정 성적서 파일명", example = "calib_report.pdf")
  @Column(name = "CAL_FILE", length = 255)
  private String calFile; // 교정정보(성적서파일) (신규)

  @ApiModelProperty(value = "비고", example = "정기 점검 필요")
  @Column(name = "MEMO", length = 200)
  private String memo; // 비고
  

  @ApiModelProperty(value = "CEA_YN ", example = "")
  @Column
  private int ceaYn;


  @ApiModelProperty(value = "CEB_YN ", example = "")
  @Column
  private int cebYn;


  @ApiModelProperty(value = "RE_YN ", example = "")
  @Column
  private int reYn;


  @ApiModelProperty(value = "RE_YN ", example = "")
  @Column
  private int reaYn;
  
  
  @ApiModelProperty(value = "RE_YN ", example = "")
  @Column
  private int rebYn;
  
    
  @ApiModelProperty(value = "ESD_YN ", example = "")
  @Column
  private int esdYn;


  @ApiModelProperty(value = "RS_YN ", example = "")
  @Column
  private int rsYn;


  @ApiModelProperty(value = "EFT_YN ", example = "")
  @Column
  private int eftYn;


  @ApiModelProperty(value = "SURGE_YN ", example = "")
  @Column
  private int surgeYn;


  @ApiModelProperty(value = "CS_YN ", example = "")
  @Column
  private int csYn;


  @ApiModelProperty(value = "MFLD_YN ", example = "")
  @Column
  private int mfldYn;


  @ApiModelProperty(value = "VDIP_YN ", example = "")
  @Column
  private int vdipYn;

  @ApiModelProperty(value = "Click_YN ", example = "")
  @Column
  private int clkYn;
  
  
  @ApiModelProperty(value = "DP_YN ", example = "")
  @Column
  private int dpYn;
  
  
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


}
