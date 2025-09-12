package egovframework.raw.dto;

import java.util.List;
import javax.persistence.Column;
import egovframework.raw.service.RawAsstn;
import egovframework.raw.service.RawCable;
import egovframework.raw.service.RawMet;
import egovframework.raw.service.RawSpec;
import egovframework.raw.service.RawSys;
import egovframework.tst.service.Test;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel(description = "성적서")
@Getter
@Setter
@ToString
public class ReportDTO {
  
  @ApiModelProperty(value = "0 로데이터번호  ", example = "")
  @Column
  private int rawSeq;

  @ApiModelProperty(value = "0 하단성적서번호  ", example = "")
  @Column
  private String sbkId;
  
  @ApiModelProperty(value = "0 하단성적서번호  ", example = "")
  @Column
  private String testId;
  
  @ApiModelProperty(value = "0 하단성적서번호 - 리비젼  ", example = "")
  @Column
  private int revision;
  
  @ApiModelProperty(value = "0 하단성적서번호 - 시험번호  ", example = "")
  @Column
  private int testSeq;
  
  @ApiModelProperty(value = "0 하단성적서번호 - 전버젼의 발급번호  ", example = "")
  @Column
  private String revReportNo;
  
  @ApiModelProperty(value = "1	발급번호  ", example = "")
  @Column
  private String reportNo;


  @ApiModelProperty(value = "2	접수일  ", example = "")
  @Column
  private String rcptDt;


  @ApiModelProperty(value = "3	시험기간  ", example = "")
  @Column
  private String testSDt;


  @ApiModelProperty(value = "3	시험기간  ", example = "")
  @Column
  private String testEDt;


  @ApiModelProperty(value = "4	신청인(상호명)  ", example = "")
  @Column
  private String aplcn;


  @ApiModelProperty(value = "4-1	사업자등록번호  ", example = "")
  @Column
  private String bsnsRgnmb;


  @ApiModelProperty(value = "4-2	대표자 성명  ", example = "")
  @Column
  private String rprsn;


  @ApiModelProperty(value = "4-3	주소  ", example = "")
  @Column
  private String address;


  @ApiModelProperty(value = "5	기자재명칭 / 모델명\n1-1. 시험기자재 (기자재명칭)  ", example = "")
  @Column
  private String eqpmn;


  @ApiModelProperty(value = "5	기자재명칭 / 모델명\n1-1. 시험기자재 (모델명)  ", example = "")
  @Column
  private String model;

  
  @ApiModelProperty(value = "(로데이터 출력용) 제조번호  ", example = "")
  @Column
  private String mnfctSerial;
  
  
  @ApiModelProperty(value = "6	제조자 / 제조국가\n1-1. 시험기자재 (제조자)  ", example = "")
  @Column
  private String mnfctCmpny;


  @ApiModelProperty(value = "6	제조자 / 제조국가  ", example = "")
  @Column
  private String mnfctCntry;
  
  @ApiModelProperty(value = "신청서_제조국 (#63)  ", example = "")
  @Column
  private String addMnfctCntry;  


  @ApiModelProperty(value = "신청기기 인증번호 ", example = "")
  @Column
  private String athntNmbr;
  

  @ApiModelProperty(value = "구성품(모듈) 식별부호 ", example = "")
  @Column
  private String mdlIdntf;
  

  @ApiModelProperty(value = "성적서 발급일 ", example = "2023-05-30")
  @Column
  private String reportDt;


  @ApiModelProperty(value = "성적서 발급사유 ", example = "최초발급")
  @Column
  private String reportMemo;


  @ApiModelProperty(value = "1-1. 시험기자재 (제품구분)  A", example = "")
  @Column
  private int classAYn;


  @ApiModelProperty(value = "1-1. 시험기자재 (제품구분)  B", example = "")
  @Column
  private int classBYn;
  
  private int class1Yn;
  private int class2Yn;
  private int class3Yn;
  private int class4Yn;
  private int class5Yn;
  private int classUbdYn;

  // @ApiModelProperty(value="1-2. 시험기준 >> 고정 ", example = "")
  @ApiModelProperty(value="시험요구_KC_EMC ", example = "1")
  @Column
  private int tbrKcEmcYn;


  @ApiModelProperty(value="시험요구_KC_RF_EMC ", example = "0")
  @Column
  private int tbrKcRfEmcYn;


  @ApiModelProperty(value="시험요구_EMCD ", example = "0")
  @Column
  private int tbrEmcdYn;


  @ApiModelProperty(value="시험요구_FCC ", example = "0")
  @Column
  private int tbrFccYn;


  @ApiModelProperty(value="시험요구_PSE ", example = "0")
  @Column
  private int tbrPseYn;


  @ApiModelProperty(value="시험요구_KT ", example = "0")
  @Column
  private int tbrKtYn;


  @ApiModelProperty(value="시험요구_ETC ", example = "시험요구_ETC")
  @Column
  private String tbrEtc;

  
  @ApiModelProperty(value="시험요구_ETC ", example = "0")
  @Column
  private int tbrEtcYn;


  @ApiModelProperty(value="CLASSA ", example = "1")
  @Column
  private int rawClassAYn;


  @ApiModelProperty(value="CLASSB ", example = "0")
  @Column
  private int rawClassBYn;


  @ApiModelProperty(value="CLASS_ETC_MEMO ", example = "CLASS_ETC_MEMO")
  @Column
  private String rawClassEtc;


  @ApiModelProperty(value="CLASS_ETC_MEMO ", example = "0")
  @Column
  private int rawClassEtcYn;
  
  // @ApiModelProperty(value="1-3. 시험방법 >> 고정 ", example = "")


  @ApiModelProperty(value = "1-4. 인증받은 모듈 사용 유무  ", example = "")
  @Column
  private String whtUseCrtYn;


  @ApiModelProperty(value = "1-4. 인증받은 모듈 사용 유무  > 인증번호  ", example = "")
  @Column
  private String crtNbr;


  @ApiModelProperty(value = "1-4. 특기사항  ", example = "")
  @Column
  private String crtMemo;
  
  @ApiModelProperty(value = "7. 기타 사항  ", example = "")
  @Column
  private int stdYn;
  
  @ApiModelProperty(value = "7. 기타 사항  ", example = "")
  @Column
  private int stdEtcYn;
  
  @ApiModelProperty(value = "1-5. 특기사항  ", example = "")
  @Column
  private String stdMemo;

  @ApiModelProperty(value = "1. 시험원  ", example = "")
  @Column
  private String testBy;


  @ApiModelProperty(value = "1. 시험원  ", example = "")
  @Column
  private String testSignUrl;


  @ApiModelProperty(value = "1. 시험원  ", example = "")
  @Column
  private String revBy;


  @ApiModelProperty(value = "1. 시험원  ", example = "")
  @Column
  private String revSignUrl;


  @ApiModelProperty(value = "3.2 시험항목 >> methodList  ", example = "")
  private List<RawMet> methodList;


  @ApiModelProperty(value = "3.3 피시험기기의 보완내용 >> modList", example = "")
  private List<String> modFileList;


  @ApiModelProperty(value = "3.3 피시험기기의 보완내용", example = "", hidden = true)
  @Column
  private String modUrl;


  @ApiModelProperty(value = "보완확인1 ", example = "1")
  @Column
  private int modCheck1Yn;


  @ApiModelProperty(value = "보완확인2 ", example = "1")
  @Column
  private int modCheck2Yn;

  
  @ApiModelProperty(value="10. System Configuration (시스템구성) 제조번호에 해당하는 헤드 이름 ", example = "")
  @Column
  private String sysHead;
  
  
  @ApiModelProperty(value = "3.3 피시험기기의 보완내용  ", example = "")
  @Column
  private String modMemo;


  @ApiModelProperty(value = "4.1 제품 개요  ", example = "")
  @Column
  private String prdExpln;


  @ApiModelProperty(value = "4.1 제품 용도  ", example = "")
  @Column
  private String prdUses;


  @ApiModelProperty(value = "4.2 기술 제원  > 주파수", example = "")
  @Column
  private String clockFrqncCode;


  @ApiModelProperty(value = "4.2 기술 제원  > 주요사항", example = "")
  @Column
  private String clockFrqnc;


  @ApiModelProperty(value = "4.2 기술 제원  > 정격전원 ", example = "-DC 12 V (직류전원장치)")
  @Column
  private String ratedPower;


  @ApiModelProperty(value = "4.2 기술 제원  > 시험전원 ", example = "- AC 220 V, 60 Hz (직류전원장치)")
  @Column
  private String testPower;


  @ApiModelProperty(value = "4.2 기술 제원  > 사용자포트 ", example = "LAN (RJ-45)_PoE, DC In")
  @Column
  private String userPort;


  @ApiModelProperty(value = "4.2 기술 제원  > 관리자포트 ", example = "해당 없음")
  @Column
  private String adminPort;


  @ApiModelProperty(value = "4.2 기술 제원  > 제품기능 ", example = "감시를 위해 배치되는 디저털 비디오 카메라")
  @Column
  private String prdFnc;


  @ApiModelProperty(value = "4.2 기술 제원  > 무선기능 ", example = "해당 없음")
  @Column
  private String wrlFnc;


  @ApiModelProperty(value = "4.2 기술 제원  > 구성품 ", example = "네트워크 고해상도 카메라(영상감시장치)")
  @Column
  private String cmp;


  @ApiModelProperty(value = "4.2 기술 제원  > 기타 ", example = "해당 없음")
  @Column
  private String etc;


  @ApiModelProperty(value = "4.2 기술 제원  >> SpecList ", example = "해당 없음")
  private List<RawSpec> rawSpecList;


  @ApiModelProperty(value = "5.1 전체구성 >> AsstnList  ", example = "")
  private List<RawAsstn> rawAsstnList;


  @ApiModelProperty(value = "5.2 시스템구성 (시험기자재가 컴퓨터 및 시스템인 경우) >> sysList  ", example = "")
  private List<RawSys> rawSysList;


  @ApiModelProperty(value = "5.3 접속 케이블 >> cableList  ", example = "")
  private List<RawCable> rawCableList;


  @ApiModelProperty(value="시험배치_FLOOR ", example = "0")
  @Column
  private int testFloorYn;


  @ApiModelProperty(value="시험배치_TABLE ", example = "1")
  @Column
  private int testTableYn;


  @ApiModelProperty(value="시험배치_FSTT ", example = "0")
  @Column
  private int testFsttYn;


  @ApiModelProperty(value="시험배치_ETC ", example = "0")
  @Column
  private int testEtcYn;
  
  @ApiModelProperty(value = "5.4 시험기자재의 동작상태  ", example = "")
  @Column
  private String oprCnd;


  @ApiModelProperty(value = "5.5 배치도 >> setupList  ", example = "")
  private List<PicDTO> setupList;


  @ApiModelProperty(value = "5.5 배치도 ", example = "", hidden = true)
  @Column
  private String setupUrl;

  @ApiModelProperty(value = "8. 추가시험 요건 > 디스플레이 관찰 거리 ", example = "해당없다")
  @Column
  private String addTestDis;

  @ApiModelProperty(value = "8. 추가시험 요건 > 네트워킹 기능 시험 시 사용한 케이블 유형 ", example = "해당없다")
  @Column
  private String addTestCable;

  @ApiModelProperty(value = "8. 추가시험 요건 > 네트워킹 기능 시험 시 데이터 속도 ", example = "해당없다")
  @Column
  private String addTestData;

  @ApiModelProperty(value = "8. 추가시험 요건 > 오디오 출력 기능 시험 시 선정된 레벨 ", example = "해당없다")
  @Column
  private String addTestAudio;

  @ApiModelProperty(value = "9.1 교류 주전원 포트에서의 전도성 방해 시험", example = "")
  private CeDTO ce1;

  @ApiModelProperty(value = "9.2 비대칭모드 전도성 방해 시험", example = "")
  private CeDTO ce2;

  @ApiModelProperty(value = "9.3 B급 기기의 방송수신기 튜너포트 차동전압 전도성 방해 시험", example = "")
  private CeDTO ce3;

  @ApiModelProperty(value = "9.4 B급 기기의 RF변조기 출력포트에서의 차동전압 전도성 방해 시험", example = "")
  private CeDTO ce4;

  @ApiModelProperty(value = "RE (9 ㎑ ~ 30 ㎒) ", example = "")
  private ReDTO re0;
  
  @ApiModelProperty(value = "9.5 방사성 방해 시험 (1GHz 이하 대역)", example = "")
  private ReDTO re1;

  @ApiModelProperty(value = "9.6 방사성 방해 시험 (1GHz 초과 대역)", example = "")
  private ReDTO re2;

  @ApiModelProperty(value = "9.7 정전기 방전 시험", example = "")
  private EsdDTO esd;

  @ApiModelProperty(value = "9.8 방사성 RF 전자기장 시험", example = "")
  private RsDTO rs;

  @ApiModelProperty(value = "9.9 전기적 빠른 과도현상 시험", example = "")
  private EftDTO eft;

  @ApiModelProperty(value = "9.10 서지 시험", example = "")
  private SurgeDTO surge;

  @ApiModelProperty(value = "9.11 전도성 RF 전자기장 시험", example = "")
  private CsDTO cs;

  @ApiModelProperty(value = "9.12 전원 주파수 자기장 시험", example = "")
  private MfDTO mf;

  @ApiModelProperty(value = "9.13 전압 강하 및 순간 정전 시험", example = "")
  private VdipDTO vdip;

  @ApiModelProperty(value = "Click", example = "")
  private ClkDTO clk;

  @ApiModelProperty(value = "DP", example = "")
  private DpDTO dp;
  
  @ApiModelProperty(value = "TEL", example = "")
  private TelDTO tel;
  
  @ApiModelProperty(value = "이미지 리스트", example = "")
  private List<PicDTO> imgList;

  @ApiModelProperty(value = "성적서 발급내역", example = "")
  private List<Test> reportList;
  
  @ApiModelProperty(value = "시험규격", example = "")
  private int testStndrSeq;
  
  @ApiModelProperty(value = "적합/부적합", example = "")
  private boolean result;

}
