package egovframework.sbk.service;

import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel(description = "신청서 민원서류")
@Getter
@Setter
@ToString
public class Sbd {

  private String sbkId;
  private String reportDt;
  private String rprsnSign;
  private String picFront;
  private String picBack;
  private String reportNo;
  private String revBy;
  private String prdIdntf;
  private String devCode;
  private String scrtYn;
  private String typeCode;
  private String testStndrSeq;
  
  // 신청서관련
  private String address;
  private String bsnsRgnmb;
  private String mngName;
  private String mngTel;
  private String mngEmail;
  private String mngFax;
  private String cfType;
  private String athntNmbr;
  private String mdlIdntf;
  private String prdctName;
  private String modelName;
  private String cmpyName;
  private String cfDate;
  private String mnfctCmpny;
  private String mnfctCntry;
  private String extendModel;
  private String extendModelMemo;
  private String addMnfctCntry;
  private String rprsn;
  private String mngPhone;
  private String addDev;
  
  private String insMemId;
  private LocalDateTime insDt;
  private String udtMemId;
  private LocalDateTime udtDt;
  private String state;
  
}
