package egovframework.wrp.service;

import java.time.LocalDateTime;
import java.util.List;
import egovframework.cmm.service.FileVO;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel(description = "주간보고")
public class WeekRep {
  private int no;                   // 순번
  private int wrSeq;                // 보고서 SEQ
  private int wrCnt;                // 최종보고 회차
  private String testTypeCode;      // 부서코드
  private String testType;          // 부서
  private String nttSj;             // 제목
  private String nttCn;             // 내용
  private String atchFileId;        // 파일첨부
  private String memo;              // 피드백
  private String insMemId;          // 보고자 ID
  private String insName;           // 보고자 ID
  private String insPos;            // 보고자 직급
  private LocalDateTime insDt;      // 최초 저장일
  private String insDtStr;          // 최초 저장일
  private String udtMemId;          // 업데이트 ID
  private LocalDateTime udtDt;      // 최종 저장일
  private String udtDtStr;          // 최종 저장일
  private char state;               // 상태
  private boolean attchYn;          // 첨부파일 유무
  
  // 조회시 파일리스트 확인
  private List<FileVO> fileList;
}

