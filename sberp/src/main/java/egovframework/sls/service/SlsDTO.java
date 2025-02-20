package egovframework.sls.service;

import java.util.List;
import javax.persistence.Column;
import org.springmodules.validation.bean.conf.loader.annotation.handler.NotNull;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public class SlsDTO {

  @Getter
  @Setter
  @ToString
  @ApiModel(value = "SlsDTO.Req", description = "매출 등록")
  public static class Req extends Sls {

    @ApiModelProperty(value = "견적서번호", example = "Q2303-G0018")
    @Column
    private String quoId;

    @ApiModelProperty(value = "견적서번호", example = "Q2303-G0018", hidden = true)
    @Column
    private List<String> quoIds;
    
    @ApiModelProperty(value = "취합견적서번호", example = "CH2305-001", hidden = true)
    @Column
    private String chqId;

    @ApiModelProperty(value = "견적서상태 :1 매출확정, 2 수정요청, 3 수정허가, 4 수정완료 ", example = "1", hidden = true)
    @Column
    private String quoStateCode;
    
    @ApiModelProperty(value = "매출번호", example = "M2303-0002")
    @Column
    private String slsId;

    @ApiModelProperty(value="BILL_SEQ ", example = "")
    @Column
    @NotNull
    private int billSeq;
 
    @ApiModelProperty(value = "매출확정 신청금액", example = "1000000")
    @Column
    private int bill;
    
    @ApiModelProperty(value="계산서발행여부 ", example = "", hidden = true)
    @Column
    private int billYn;
    
    @ApiModelProperty(value="납부상태(공통토드:MP) 01 납부완료(계좌이체) 02 납부완료(가상계좌) 03 납부완료(신용카드) 04 납부완료(기타입력)  ", example = "", hidden = true)
    @Column
    private String payCode;
    
    @ApiModelProperty(value="OTHER_BILL_DT ", example = "", hidden = true)
    @Column
    private String otherBillDt;
    
    @ApiModelProperty(value="납부완료일 ", example = "", hidden = true)
    @Column
    private String payDt;
    
    @ApiModelProperty(value="입금자명 ", example = "", hidden = true)
    @Column
    private String payer;
    
  }

  @Getter
  @Setter
  @ApiModel(value = "SlsDTO.Res", description = "매출 조회")
  public static class Res extends Sls {

    @ApiModelProperty(value = "게시글번호", example = "1")
    @Column
    private int no;

    @ApiModelProperty(value = "매출번호", example = "M2303-0002")
    @Column
    private String slsId;

    @ApiModelProperty(value = "취합번호", example = "CH2303-001")
    @Column
    private String chqId;

    @ApiModelProperty(value = "견적서번호", example = "Q2303-G0018")
    @Column
    private String quoId;

    @ApiModelProperty(value = "매출확정일", example = "2023-03-04")
    @Column
    private String cnfrmDtStr;

    @ApiModelProperty(value = "매출확정자", example = "김정미")
    @Column
    private String cnfrmName;

    @ApiModelProperty(value = "고객유형", example = "컨설팅 or 직고객")
    @Column
    private String cmpyType;

    @ApiModelProperty(value = "컨설팅명 ", example = "")
    @Column
    private String cmpyTitle;

    @ApiModelProperty(value = "컨설팅 회사명", example = "")
    private String prtnName;
    
    @ApiModelProperty(value = "컨설팅 회사명", example = "")
    private String prtnSeq;
    
    @ApiModelProperty(value = "신청사 회사명", example = "")
    private String dirtName;
    
    @ApiModelProperty(value = "업체명 ", example = "")
    @Column
    private String cmpyName;

    @ApiModelProperty(value = "제품명 ", example = "")
    @Column
    private String prdctName;

    @ApiModelProperty(value = "모델명 ", example = "")
    @Column
    private String modelName;
    
    @ApiModelProperty(value = "청구액 ", example = "0")
    @Column
    private int chrgs;

    @ApiModelProperty(value = "순매출 ", example = "0")
    @Column
    private int netSales;
    
    @ApiModelProperty(value = "시험비 ", example = "0")
    @Column
    private int testFee;

    @ApiModelProperty(value = "정상총합 ", example = "3000000")
    @Column
    private String CostTotal;
    
    @ApiModelProperty(value = "계산서발행액총합 ", example = "3300000")
    @Column
    private String TotalVat;

    @ApiModelProperty(value = "시험항목 ", example = "")
    @Column
    private String testCate;
    
    @ApiModelProperty(value = "시험부 ", example = "")
    @Column
    private String testType;

    @ApiModelProperty(value = "계산서 상태", example = "-")
    @Column
    private String billState;

    @ApiModelProperty(value = "납부 상태", example = "-")
    @Column
    private String payState;
    
//    @ApiModelProperty(value = "계산서발행내역 ")
//    private List<Bill> billList;
//    
//    @ApiModelProperty(value = "수정요청상태 ", example = "")
//    @Column
//    private String quoState;
//
//    @ApiModelProperty(value = "수정요청상태(일) ", example = "")
//    @Column
//    private String prmsDtStr;
//
//    @ApiModelProperty(value = "수정요청상태(이름) ", example = "")
//    @Column
//    private String prmsName;

    @ApiModelProperty(value = "최종상태 ", example = "")
    @Column
    private String lastState;

    @ApiModelProperty(value = "납입횟수 ", example = "")
    @Column
    private int cnt;
    
    @ApiModelProperty(value = "삭제가능여부", example = "Y")
    @Column
    private String canDelete;
    
    @ApiModelProperty(value = "보안견적서 여부", example = "")
    @Column
    private int secretYn;
    
    @ApiModelProperty(value = "VERSION ", example = "1")
    @Column
    private String version;
  }
}
