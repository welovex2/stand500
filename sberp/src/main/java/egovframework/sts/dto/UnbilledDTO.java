package egovframework.sts.dto;

import java.util.ArrayList;
import java.util.List;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 완료 후 매출 미확정 상세.
 *
 * <p>시험이 다 끝났는데 매출확정(SLS_TB)조차 안 된 건을 견적 단위로 모은다.
 * 매출확정까지 간 건은 매출 리스트 화면에서 조회되므로 여기서 뺀다.
 *
 * <p>영업 주간 화면의 미청구 카드와 판정 기준이 세 군데 다르다. 올해 완료분만 보지 않고,
 * 견적에 딸린 시험이 전부 끝난 것만 세며, 매출확정된 건을 뺀다.
 */
@Getter
@Setter
@ApiModel(description = "완료 후 미청구 상세")
public class UnbilledDTO {

  @ApiModelProperty("기준일")
  private String asOfDate;
  @ApiModelProperty("명단에 실을 최소 경과일")
  private int minDays;

  @ApiModelProperty("미청구 견적 수")
  private int quoCnt;
  @ApiModelProperty("미청구 시험 건수")
  private int testCnt;
  @ApiModelProperty("순매출 합(만원)")
  private long netMan;
  private String netEok;
  @ApiModelProperty("청구액 합(부가세 포함, 만원)")
  private long vatMan;
  private String vatEok;

  @ApiModelProperty("매출확정까지 가서 이 표에서 뺀 건. 매출 리스트에서 조회된다")
  private int excludedCnt;
  private long excludedMan;
  private String excludedEok;
  @ApiModelProperty("완료 연도가 오래돼서 뺀 건")
  private int oldCnt;
  private long oldMan;
  private String oldEok;
  @ApiModelProperty("집계에 넣는 가장 이른 완료 연도")
  private int fromYear;
  @ApiModelProperty("시험 상태가 매끈하지 않아 확인이 필요한 견적 수")
  private int checkCnt;
  private long checkMan;
  private String checkEok;

  @ApiModelProperty("완료 후 90일 이상")
  private int d90Cnt;
  private long d90Man;
  private String d90Eok;
  @ApiModelProperty("완료 후 365일 이상")
  private int d365Cnt;
  private long d365Man;
  private String d365Eok;

  @ApiModelProperty("경과일 구간별")
  private List<BucketRow> buckets = new ArrayList<BucketRow>();
  @ApiModelProperty("완료 연도별")
  private List<BucketRow> years = new ArrayList<BucketRow>();
  @ApiModelProperty("시험부별")
  private List<GroupRow> depts = new ArrayList<GroupRow>();
  @ApiModelProperty("담당자별")
  private List<GroupRow> owners = new ArrayList<GroupRow>();
  @ApiModelProperty("고객별 상위")
  private List<GroupRow> custs = new ArrayList<GroupRow>();

  @ApiModelProperty("명단. minDays 이상 경과한 견적, 경과일 긴 순")
  private List<QuoRow> list = new ArrayList<QuoRow>();
  private int listQuoCnt;
  private long listNetMan;
  private String listNetEok;

  /** 구간 한 줄. 경과일 구간과 완료 연도가 같은 모양이라 함께 쓴다. */
  @Getter
  @Setter
  public static class BucketRow {
    private String label;
    private int quoCnt;
    private int testCnt;
    private long netMan;
    private String netEok;
    private long vatMan;
    /** 전체 순매출에서 차지하는 비중(%). */
    private double sharePct;
  }

  /** 시험부·담당자·고객 집계 한 줄. */
  @Getter
  @Setter
  public static class GroupRow {
    private String name;
    private int quoCnt;
    private int testCnt;
    private long netMan;
    private String netEok;
    private double sharePct;
    /** 그 묶음에서 가장 오래 밀린 건의 경과일. */
    private int maxDays;
    /** 담당자 묶음일 때, 그 담당자가 퇴사했으면 true. */
    private boolean quit;
  }

  /** 명단 한 줄. 견적 하나. */
  @Getter
  @Setter
  public static class QuoRow {
    private String quoId;
    /** 견적 리스트 화면에서 이 견적 한 건만 걸러 보여주는 주소. */
    private String listUrl;
    /** 취합견적이면 true. 묶어서 청구하므로 따로 확인해야 한다. */
    private boolean chq;
    private int cmpySeq;
    private String cmpyName;
    private String mngName;
    /** 담당자가 퇴사했으면 true. 챙길 사람이 없는 건이다. */
    private boolean mngQuit;
    private String prdctName;
    /** 그 견적에 걸린 시험부 이름들. */
    private String deptNames;
    private int testCnt;
    /** 그 견적의 시험 상태 구성. 예: "완료 2", "완료 1 · 취소 3". */
    private String testStateText;
    /** 완료라는데 성적서 발급 흔적이 없는 시험 수. */
    private int noReportCnt;
    /** 취소된 형제 시험 수. */
    private int cancelCnt;
    /** 상태 구성이 매끈하지 않아 눈으로 확인해야 하는 줄. */
    private boolean stateWarn;
    private long netMan;
    private long vatMan;
    private String issueDt;
    /** 마지막 시험이 끝난 날. */
    private String doneDt;
    private int days;
  }

  /** selectUnbilled 결과 한 줄. 시험 한 건. */
  @Getter
  @Setter
  public static class ItemRow {
    private String quoYm;
    private int quoSeq;
    private String quoId;
    private int chqSeq;
    private String issueDt;
    private long quoTotalVat;
    private int cmpySeq;
    private String cmpyName;
    private String mngName;
    private String mngWorkState;
    private String prdctName;
    private String testId;
    private String deptCode;
    private String deptName;
    private long netSales;
    private String doneDt;
    private int quoDoneCnt;
    private int quoCancelCnt;
    private int quoSkipCnt;
    private int quoNoReportCnt;
    private int hasSls;
  }
}
