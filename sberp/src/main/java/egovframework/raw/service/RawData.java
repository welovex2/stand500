package egovframework.raw.service;

import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Column;
import egovframework.raw.dto.PicDTO;
import egovframework.tst.service.Test;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel(description = "로데이터")
@Getter
@Setter
@ToString
public class RawData {

  @ApiModelProperty(value = "시험번호+RD+001 ", example = "")
  @Column
  private int rawSeq;


  @ApiModelProperty(value = "시험번호+RD+001 ", example = "")
  @Column
  private String rawId;


  @ApiModelProperty(value = "신청서번호 ", example = "")
  @Column
  private String sbkId;

  @ApiModelProperty(value = "신청서별 로데이터 번호 ", example = "")
  @Column
  private int sbkNo;

  @ApiModelProperty(value = "시험번호 ", example = "")
  @Column
  private int testSeq;


  @ApiModelProperty(value = "신청인 ", example = "드림에스앤아이")
  @Column
  private String aplcn;


  @ApiModelProperty(value = "기자재명칭 ", example = "네트워크 고해상도 카메라(영상감시장치)")
  @Column
  private String eqpmn;


  @ApiModelProperty(value = "모델명 ", example = "XNB-52812R")
  @Column
  private String model;


  @ApiModelProperty(value = "제조번호 ", example = "N/A")
  @Column
  private String mnfctSerial;


  @ApiModelProperty(value = "제조사 ", example = "드림에스앤아이")
  @Column
  private String mnfctCmpny;


  @ApiModelProperty(value = "제조국가 ", example = "한국")
  @Column
  private String mnfctCntry;


  @ApiModelProperty(value = "입수일 ", example = "2023-01-10")
  @Column
  private String rcptDt;


  @ApiModelProperty(value = "시험시작 ", example = "2023-01-23")
  @Column
  private String testSDt;


  @ApiModelProperty(value = "시험완료 ", example = "2023-01-24")
  @Column
  private String testEDt;


  @ApiModelProperty(value = "시험자 ", example = "김재희")
  @Column
  private String testBy;


  @ApiModelProperty(value = "시험자서명url ", example = "")
  @Column
  private String testSignUrl;


  @ApiModelProperty(value = "기술책임자 ", example = "나승주")
  @Column
  private String revBy;


  @ApiModelProperty(value = "기술책임자서서명url ", example = "")
  @Column
  private String revSignUrl;


  @ApiModelProperty(value = "시험요구_KC_EMC ", example = "1")
  @Column
  private int tbrKcEmcYn;


  @ApiModelProperty(value = "시험요구_KC_RF_EMC ", example = "0")
  @Column
  private int tbrKcRfEmcYn;


  @ApiModelProperty(value = "시험요구_EMCD ", example = "0")
  @Column
  private int tbrEmcdYn;


  @ApiModelProperty(value = "시험요구_FCC ", example = "0")
  @Column
  private int tbrFccYn;


  @ApiModelProperty(value = "시험요구_PSE ", example = "0")
  @Column
  private int tbrPseYn;


  @ApiModelProperty(value = "시험요구_KT ", example = "0")
  @Column
  private int tbrKtYn;


  @ApiModelProperty(value = "시험요구_ETC ", example = "시험요구_ETC")
  @Column
  private String tbrEtc;


  @ApiModelProperty(value = "시험요구_ETC ", example = "0")
  @Column
  private int tbrEtcYn;


  @ApiModelProperty(value = "CLASSA ", example = "1")
  @Column
  private int classAYn;


  @ApiModelProperty(value = "CLASSB ", example = "0")
  @Column
  private int classBYn;


  @ApiModelProperty(value = "CLASS_ETC_MEMO ", example = "CLASS_ETC_MEMO")
  @Column
  private String classEtc;


  @ApiModelProperty(value = "CLASS_ETC_MEMO ", example = "0")
  @Column
  private int classEtcYn;
  
  /**
   * 9814 추가
   */
  @ApiModelProperty(value = "제품군 1 ", example = "0")
  @Column
  private int class1Yn;  
  
  @ApiModelProperty(value = "제품군 2 ", example = "0")
  @Column
  private int class2Yn;
  
  @ApiModelProperty(value = "제품군 3 ", example = "0")
  @Column
  private int class3Yn;
  
  @ApiModelProperty(value = "제품군 4 ", example = "0")
  @Column
  private int class4Yn;
  
  @ApiModelProperty(value = "제품군 5 ", example = "0")
  @Column
  private int class5Yn;
  
  @ApiModelProperty(value = "제품군 UBD ", example = "0")
  @Column
  private int classUbdYn;
  /**
   * --END 9814 추가
   */

  @ApiModelProperty(value = "4-2. method (시험방법)", example = "")
  @Column
  private List<RawMet> methodList;


  @ApiModelProperty(value = "제품설명 ", example = "감시를 위해 배치되는 디지털 비디오 카메라")
  @Column
  private String prdExpln;


  @ApiModelProperty(value = "제품용도 ", example = "보안용 카메라")
  @Column
  private String prdUses;


  @ApiModelProperty(value = "클럭수파수 ", example = "148MHz")
  @Column
  private String clockFrqnc;

  @ApiModelProperty(value = "클럭수파수 ", example = "RC")
  @Column
  private String clockFrqncCode;

  @ApiModelProperty(value = "정격전원 ", example = "-DC 12 V (직류전원장치)")
  @Column
  private String ratedPower;


  @ApiModelProperty(value = "시험전원 ", example = "- AC 220 V, 60 Hz (직류전원장치)")
  @Column
  private String testPower;


  @ApiModelProperty(value = "사용자포트 ", example = "LAN (RJ-45)_PoE, DC In")
  @Column
  private String userPort;


  @ApiModelProperty(value = "관리자포트 ", example = "해당 없음")
  @Column
  private String adminPort;


  @ApiModelProperty(value = "제품기능 ", example = "감시를 위해 배치되는 디저털 비디오 카메라")
  @Column
  private String prdFnc;


  @ApiModelProperty(value = "무선기능 ", example = "해당 없음")
  @Column
  private String wrlFnc;


  @ApiModelProperty(value = "구성품 ", example = "네트워크 고해상도 카메라(영상감시장치)")
  @Column
  private String cmp;


  @ApiModelProperty(value = "기타 ", example = "해당 없음")
  @Column
  private String etc;


  @ApiModelProperty(value = "인증사용여부 ", example = "0")
  @Column
  private int whtUseCrtYn;


  @ApiModelProperty(value = "인증번호 ", example = "인증번호")
  @Column
  private String crtNbr;


  @ApiModelProperty(value = "인증특이사항 ", example = "해당 없음")
  @Column
  private String crtMemo;


  @ApiModelProperty(value = "보완사항 ", example = "해당 없음")
  @Column
  private String modMemo;


  @ApiModelProperty(value = "보완사항_URL ", example = "")
  @Column
  private String modUrl;


  @ApiModelProperty(value = "보완확인1 ", example = "1")
  @Column
  private int modCheck1Yn;


  @ApiModelProperty(value = "보완확인2 ", example = "1")
  @Column
  private int modCheck2Yn;


  @ApiModelProperty(value = "10. System Configuration (시스템구성) 제조번호에 해당하는 헤드 이름 ", example = "")
  @Column
  private String sysHead;


  @ApiModelProperty(value = "시험배치_FLOOR ", example = "0")
  @Column
  private int testFloorYn;


  @ApiModelProperty(value = "시험배치_TABLE ", example = "1")
  @Column
  private int testTableYn;


  @ApiModelProperty(value = "시험배치_FSTT ", example = "0")
  @Column
  private int testFsttYn;


  @ApiModelProperty(value = "시험배치_ETC ", example = "0")
  @Column
  private int testEtcYn;


  @ApiModelProperty(value = "시험기자재의동작상태 ", example = "Adapter Mode")
  @Column
  private String oprCnd;


  @ApiModelProperty(value = "Test Set-up Configuraiotn for EUT ", example = "")
  @Column
  private String setupUrl;


  @ApiModelProperty(value = "규격적용 특이사항 ", example = "추가시험 요건")
  @Column
  private String stdMemo;

  
  @ApiModelProperty(value = "규격적용 특이사항 YN ", example = "0")
  @Column
  private int stdYn;
  
  
  @ApiModelProperty(value = "규격적용 특이사항 기타 YN ", example = "0")
  @Column
  private int stdEtcYn;

  
  @ApiModelProperty(value = "디스플레이 관찰 거리 ", example = "해당없다")
  @Column
  private String addTestDis;

  @ApiModelProperty(value = "네트워킹 기능 시험 시 사용한 케이블 유형 ", example = "해당없다")
  @Column
  private String addTestCable;

  @ApiModelProperty(value = "네트워킹 기능 시험 시 데이터 속도 ", example = "해당없다")
  @Column
  private String addTestData;

  @ApiModelProperty(value = "오디오 출력 기능 시험 시 선정된 레벨 ", example = "해당없다")
  @Column
  private String addTestAudio;

  
  @ApiModelProperty(value="성적서발급번호 ", example = "STB23-0158")
  @Column
  private String reportNo;
  
  
  @ApiModelProperty(value="성적서 발급일 ", example = "2023-05-30")
  @Column    
  private String reportDt;
  
  
  @ApiModelProperty(value="성적서 발급사유 ", example = "최초발급")
  @Column    
  private String reportMemo;
  
  
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

  @ApiModelProperty(value = "Technical specifications (기술제원) ", example = "")
  @Column
  private List<RawSpec> rawSpecList;

  @ApiModelProperty(value = "Technical Requirements (기술적 요구항목) ", example = "")
  @Column
  private List<RawTchn> rawTchnList;

  @ApiModelProperty(value = "Assistance Device and Cable(시험기기 전체구성) ", example = "")
  @Column
  private List<RawAsstn> rawAsstnList;

  @ApiModelProperty(value = "System Configuration (시스템구성) ", example = "")
  @Column
  private List<RawSys> rawSysList;

  @ApiModelProperty(value = "Type of Cable Used (접속 케이블) ", example = "")
  @Column
  private List<RawCable> rawCableList;

  @ApiModelProperty(value = "EUT Modifications (보완사항)", example = "")
  @Column
  private List<PicDTO> modFileList;

  @ApiModelProperty(value = "Test Set-up Configuraiotn for EUT", example = "")
  @Column
  private List<PicDTO> setupList;

  @ApiModelProperty(value = "성적서 발급내역", example = "")
  @Column
  private List<Test> reportList;
}
