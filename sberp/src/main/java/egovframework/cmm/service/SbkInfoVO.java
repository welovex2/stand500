package egovframework.cmm.service;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SbkInfoVO {

  private String sbkId; // 신청서번호
  private String testNo; // 시험번호
  private String ncFolderPath; // 파일서버 신청서 기본 폴더
  private String atchFileId; // 파일 그룹 ID
  private Date insDt;
  private String cmpyName; // 회사명 (JOB_TB.CMPY_NAME)
  private String modelName; // 모델명 (JOB_TB.MODEL_NAME)
  private String prdctName; // 기기명칭/제품명 (JOB_TB.PRDCT_NAME)
  private String mnfctCmpny; // 제조자 (SBK_TB.MNFCT_CMPNY)
  private String mnfctCntry; // 제조국가명
  private String athntNmbr; // 인증번호
  private String mdlIdntf; // 모델식별부호

}
