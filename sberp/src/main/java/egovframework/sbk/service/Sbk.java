package egovframework.sbk.service;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel(description = "신청서")
@Getter
@Setter
@ToString
public class Sbk {
  @ApiModelProperty(value = "SBK_YM ", example = "", hidden = true)
  @Column
  private String sbkYm;


  @ApiModelProperty(value = "SBK_SEQ ", example = "", hidden = true)
  @Column
  private int sbkSeq;

  @ApiModelProperty(value = "재발행 번호 ", example = "", hidden = true)
  @Column
  private String revision;
  
  @ApiModelProperty(value = "신청서타입(G:일반,M:의료) ", example = "G", hidden = true)
  @Column
  private String type = "G";


  @ApiModelProperty(value = "예상완료일 ", example = "2023-05-21")
  @Column
  private String estCmpDt;


  @ApiModelProperty(value = "사업자등록번호 ", example = "사업자등록번호")
  @Column
  private String bsnsRgnmb;


  @ApiModelProperty(value = "대표자 ", example = "대표자")
  @Column
  private String rprsn;


  @ApiModelProperty(value = "법인등록번호 ", example = "법인등록번호")
  @Column
  private String crprtRgnmb;


  @ApiModelProperty(value = "주소 ", example = "주소")
  @Column
  private String address;


  @ApiModelProperty(value = "주민등록번호 ", example = "주민등록번호")
  @Column
  private String rsdntRgnmb;


  @ApiModelProperty(value = "제조자 (Manufacturer)", example = "1")
  @Column
  private String indstMfYn;

  @ApiModelProperty(value = "판매자 (Seller)", example = "1")
  @Column
  private String indstSlYn;

  @ApiModelProperty(value = "수입자 (Importer)", example = "1")
  @Column
  private String indstIpYn;

//  @Pattern(regexp = "^[0-9-]*$", message = "연락처는 10 ~ 11 자리의 숫자만 입력 가능합니다.")
//  @Size(min = 0, max = 13, message = "연락처는 10 ~ 11 자리의 숫자만 입력 가능합니다.")
  @ApiModelProperty(value = "담당자전화 ", example = "02-222-1111")
  @Column
  private String mngTel;

  @ApiModelProperty(value = "담당자이메일 ", example = "info@standardbank.co.kr")
  @Column
  // @Pattern(regexp = "^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$", message = "이메일을
  // 확인하세요.")
  private String mngEmail;


  @ApiModelProperty(value = "파생모델명 ", example = "파생모델명")
  @Column
  private String extendModel;


  @ApiModelProperty(value = "회사식별부호 ", example = "회사식별부호")
  @Column
  private String cmpnyIdntf;


  @ApiModelProperty(value = "인증번호 ", example = "인증번호")
  @Column
  private String athntNmbr;


  @ApiModelProperty(value = "신규회사식별부호1 ", example = "신규회사식별부호1")
  @Column
  private String newCmpnyIdntf1;


  @ApiModelProperty(value = "신규회사식별부호2 ", example = "신규회사식별부호2")
  @Column
  private String newCmpnyIdntf2;


  @ApiModelProperty(value = "신규회사식별부호3 ", example = "신규회사식별부호3")
  @Column
  private String newCmpnyIdntf3;


  @ApiModelProperty(value = "전기정격W ", example = "전기정격W")
  @Column
  private String elctrRtngW;


  @ApiModelProperty(value = "클럭주파수 ", example = "클럭주파수")
  @Column
  private String clockFrqnc;


  @ApiModelProperty(value = "모듈식별부호 ", example = "모듈식별부호")
  @Column
  private String mdlIdntf;


  @ApiModelProperty(value = "추가기기부호 ", example = "추가기기부호")
  @Column
  private String addDev;


  @ApiModelProperty(value = "제조사회사명 ", example = "제조사회사명")
  @Column
  private String mnfctCmpny;


  @ApiModelProperty(value = "제조국 (공통코드 : SN)", example = "1")
  @Column
  private String mnfctCntryCode;


  @ApiModelProperty(value = "주소 ", example = "주소")
  @Column
  private String mnfctAdres;


  @ApiModelProperty(value = "추가제조사 ", example = "추가제조사")
  @Column
  private String addMnfctCmpny;


  @ApiModelProperty(value = "추가제조국 ", example = "추가제조국")
  @Column
  private String addMnfctCntry;


  @ApiModelProperty(value = "시험장소:시험기관내 ", example = "1")
  @Column
  private int testPlaceInYn;


  @ApiModelProperty(value = "시험장소:현장시험 ", example = "1")
  @Column
  private int testPlaceOutYn;


  @ApiModelProperty(value = "시험장소주소 ", example = "시험장소주소")
  @Column
  private String testAdres;


  @ApiModelProperty(value = "전기시험여부:안전인증 ", example = "1")
  @Column
  private int elctrTestCYn;


  @ApiModelProperty(value = "전기시험여부:안전확인 ", example = "1")
  @Column
  private int elctrTestKYn;


  @ApiModelProperty(value = "전기시험여부:해당없음 ", example = "1")
  @Column
  private int elctrTestNYn;


  @ApiModelProperty(value = "시험성적서구분:KOLAS 성적서 ", example = "1")
  @Column
  private int testRprtKYn;


  @ApiModelProperty(value = "시험성적서구분:일반 성적서 ", example = "1")
  @Column
  private int testRprtNYn;


  @ApiModelProperty(value = "시험성적서용도조달청Yn ", example = "1")
  @Column
  private int testRprtJodalYn;


  @ApiModelProperty(value = "시험성적서용도지원사업yn ", example = "1")
  @Column
  private int testRprtSprtYn;


  @ApiModelProperty(value = "시험성적서용도지원사업Text ", example = "시험성적서용도지원사업Text")
  @Column
  private String testRprtSprt;


  @ApiModelProperty(value = "시험성적서용도기타Yn ", example = "1")
  @Column
  private int testRprtEtcYn;

  @ApiModelProperty(value = "시험성적서용도기타Text ", example = "시험성적서용도기타Text")
  @Column
  private String testRprtEtc;

  @ApiModelProperty(value = "적합성결정규칙Yn ", example = "1")
  @Column
  private int cnfrmYn;


  @ApiModelProperty(value = "시료처리_EUT_yn ", example = "1")
  @Column
  private int imEutYn;


  @ApiModelProperty(value = "시료처리_의뢰자_yn ", example = "1")
  @Column
  private int imClntYn;


  @ApiModelProperty(value = "시료처리_택배우송_yn ", example = "1")
  @Column
  private int imDlvryYn;


  @ApiModelProperty(value = "시료처리_폐기_yn ", example = "1")
  @Column
  private int imDspslYn;


  @ApiModelProperty(value = "시료처리_기타_yn ", example = "1")
  @Column
  private int imEtcYn;


  @ApiModelProperty(value = "고객정보동의_yn ", example = "1")
  @Column
  private int cusInfoAgreeYn;


  @ApiModelProperty(value = "예상소요시간 ", example = "40")
  @Column
  private int estCmpTime;


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


  @ApiModelProperty(value = "적합등록 ", example = "1")
  @Column
  private int cfJdYn;


  @ApiModelProperty(value = "적합인증 ", example = "1")
  @Column
  private int cfJiYn;


  @ApiModelProperty(value = "자기적합 ", example = "1")
  @Column
  private int cfJgYn;


  @ApiModelProperty(value = "잠정인증 ", example = "1")
  @Column
  private int cfJjYn;


  @ApiModelProperty(value = "기타 ", example = "1")
  @Column
  private int cfEtc1Yn;


  @ApiModelProperty(value = "기타 ", example = "1")
  @Column
  private String cfEtc1;


  @ApiModelProperty(value = "안전인증 ", example = "1")
  @Column
  private int cfAiYn;


  @ApiModelProperty(value = "안전확인 ", example = "1")
  @Column
  private int cfAhYn;


  @ApiModelProperty(value = "공급자적합성 ", example = "1")
  @Column
  private int cfGjYn;


  @ApiModelProperty(value = "대기전력 ", example = "1")
  @Column
  private int cfDjYn;


  @ApiModelProperty(value = "에너지효율 ", example = "1")
  @Column
  private int cfEhYn;


  @ApiModelProperty(value = "기타2 ", example = "1")
  @Column
  private int cfEtc2Yn;


  @ApiModelProperty(value = "기타2 ", example = "1")
  @Column
  private String cfEtc2;


  @ApiModelProperty(value = "EMC ", example = "1")
  @Column
  private int testEmcYn;


  @ApiModelProperty(value = "EMC_Medical ", example = "1")
  @Column
  private int testEmYn;


  @ApiModelProperty(value = "EMF ", example = "1")
  @Column
  private int testEmfYn;


  @ApiModelProperty(value = "Telecom ", example = "1")
  @Column
  private int testTelYn;


  @ApiModelProperty(value = "RoHS ", example = "1")
  @Column
  private int testRohsYn;


  @ApiModelProperty(value = "신뢰성 ", example = "1")
  @Column
  private int testSsYn;


  @ApiModelProperty(value = "Safety ", example = "1")
  @Column
  private int testSafYn;


  @ApiModelProperty(value = "TEST_SM_YN ", example = "1")
  @Column
  private int testSmYn;


  @ApiModelProperty(value = "대기전력 ", example = "1")
  @Column
  private int testDjYn;


  @ApiModelProperty(value = "에너지효율 ", example = "1")
  @Column
  private int testEhYn;


  @ApiModelProperty(value = "RF ", example = "1")
  @Column
  private int testRfYn;


  @ApiModelProperty(value = "SAR ", example = "1")
  @Column
  private int testSarYn;


  @ApiModelProperty(value = "승인대행(해외) ", example = "1")
  @Column
  private int testOutYn;


  @ApiModelProperty(value = "승인대행(국내) ", example = "1")
  @Column
  private int testInYn;


  @ApiModelProperty(value = "Class A ", example = "1")
  @Column
  private int classAYn;


  @ApiModelProperty(value = "Class B ", example = "1")
  @Column
  private int classBYn;


  @ApiModelProperty(value = "사전통관 시험신청YN ", example = "1")
  @Column
  private int ppYn;


  @ApiModelProperty(value = "시험기관명 ", example = "1")
  @Column
  private String ppName;


  @ApiModelProperty(value = "시험접수번호 ", example = "1")
  @Column
  private String ppNum;


  @ApiModelProperty(value = "B/L No ", example = "1")
  @Column
  private String ppBl;


  @ApiModelProperty(value = "선적수량 ", example = "1")
  @Column
  private String ppCnt;

  @ApiModelProperty(value = "제출서류파일ID ", example = "1")
  @Column
  private String docUrl;

  @ApiModelProperty(value = "인증(시험)신청서  ", example = "1")
  @Column
  private int docIsYn;


  @ApiModelProperty(value = "사용설명서 ", example = "1")
  @Column
  private int docSsYn;


  @ApiModelProperty(value = "회로도 ", example = "1")
  @Column
  private int docHdYn;


  @ApiModelProperty(value = "부품배치도 ", example = "1")
  @Column
  private int docBbYn;


  @ApiModelProperty(value = "패턴도 ", example = "1")
  @Column
  private int docPdYn;


  @ApiModelProperty(value = "부품리스트 ", example = "1")
  @Column
  private int docBlYn;


  @ApiModelProperty(value = "주요부품 ", example = "1")
  @Column
  private int docJbYn;


  @ApiModelProperty(value = "주요부품 승인서 ", example = "1")
  @Column
  private int docJsYn;


  @ApiModelProperty(value = "CB Report ", example = "1")
  @Column
  private int docCrYn;


  @ApiModelProperty(value = "승인기관 인증서 ", example = "1")
  @Column
  private int docSiYn;

  @ApiModelProperty(value = "승인기관 성적서 ", example = "1")
  @Column
  private int docSgYn;

  @ApiModelProperty(value = "신청인보유 ", example = "1")
  @Column
  private int docSbYn;

  @ApiModelProperty(value = "신청년 ", example = "2023")
  @Column
  @Pattern(regexp = "^[0-9-]*$", message = "신청년은 4 자리의 숫자만 입력 가능합니다.")
  @Size(min = 0, max = 4, message = "신청년은 4 자리의 숫자만 입력 가능합니다.")
  private String appYear;

  @ApiModelProperty(value = "신청월 ", example = "03")
  @Column
  @Pattern(regexp = "^[0-9-]*$", message = "신청월은 2 자리의 숫자만 입력 가능합니다.")
  @Size(min = 0, max = 2, message = "신청월은 2 자리의 숫자만 입력 가능합니다.")
  private String appMon;

  @ApiModelProperty(value = "신청일 ", example = "24")
  @Column
  @Pattern(regexp = "^[0-9-]*$", message = "신청일은 2 자리의 숫자만 입력 가능합니다.")
  @Size(min = 0, max = 2, message = "신청일은 2 자리의 숫자만 입력 가능합니다.")
  private String appDay;

  @ApiModelProperty(value = "신청인 ", example = "신청인")
  @Column
  private String appName;

  @ApiModelProperty(value = "신청인 서명 ", example = "")
  @Column
  private String appSignUrl;

  @ApiModelProperty(value = "동의 신청인 ", example = "동의 신청인")
  @Column
  private String appAgreeName;

  @ApiModelProperty(value = "동의 신청인 서명 ", example = "")
  @Column
  private String appAgreeSignUrl;

  @ApiModelProperty(value = "시험결과이상없음 ", example = "1")
  @Column
  private int workChkYn;

  @ApiModelProperty(value = "업무담당자 ", example = "업무담당자")
  @Column
  private String workName;

  @ApiModelProperty(value = "업무담당자 서명 ", example = "")
  @Column
  private String workSignUrl;

  @ApiModelProperty(value = "신청구분_신규 ", example = "1")
  @Column
  private int sgNewYn;

  @ApiModelProperty(value = "신청구분_기술기준변경 ", example = "1")
  @Column
  private int sgGbYn;

  @ApiModelProperty(value = "신청구분_동일기자재 ", example = "1")
  @Column
  private int sgDgYn;

  @ApiModelProperty(value = "신청구분_기술기준외변경 ", example = "1")
  @Column
  private int sgEtcYn;

  @ApiModelProperty(value = "인증분야-CE_DoC ", example = "1")
  @Column
  private int cfCdYn;

  @ApiModelProperty(value = "인증분야-CE_CoC ", example = "1")
  @Column
  private int cfCcYn;

  @ApiModelProperty(value = "인증분야-FCC ", example = "1")
  @Column
  private int cfFccYn;

  @ApiModelProperty(value = "인증분야-VCCI ", example = "1")
  @Column
  private int cfVcciYn;

  @ApiModelProperty(value = "인증분야-PSE ", example = "1")
  @Column
  private int cfPseYn;

  @ApiModelProperty(value = "보완확인1 ", example = "1")
  @Column
  private int modCheck1Yn;

  @ApiModelProperty(value = "보완확인2 ", example = "1")
  @Column
  private int modCheck2Yn;
  
  @ApiModelProperty(value = "업체요청(메모) ", example = "")
  @Column
  private String cmpnyMemo;
  
  @ApiModelProperty(value = "재발행 사유 ", example = "")
  @Column
  private String revisionMemo;
}
