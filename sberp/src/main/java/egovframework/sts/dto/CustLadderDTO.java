package egovframework.sts.dto;

import java.util.ArrayList;
import java.util.List;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 고객 성장 단계.
 *
 * <p>이 화면이 답하는 것은 세 가지다. 한 번만 오고 안 온 고객을 두 번째로 데려올 수 있는가, 한 시험부만 쓰는 단골에게 다른
 * 시험부를 붙일 수 있는가, 평소보다 조용해진 고객이 누구인가.
 */
@Getter
@Setter
@ApiModel(description = "고객 성장 단계")
public class CustLadderDTO {

  @ApiModelProperty("기준일")
  private String asOfDate;
  @ApiModelProperty("올해")
  private int year;

  @ApiModelProperty("거래 이력이 있는 고객 수(같은 회사는 하나로 합친 뒤)")
  private int custCnt;
  @ApiModelProperty("합치기 전 업체 등록 줄 수")
  private int rawCnt;
  @ApiModelProperty("여러 줄로 등록돼 있어 하나로 합친 고객 수")
  private int mergedCnt;

  @ApiModelProperty("올해 매출(만원)")
  private long netCurMan;
  private String netCurEok;
  @ApiModelProperty("올해 매출을 낸 기존 고객 수")
  private int existCnt;
  @ApiModelProperty("기존 고객 올해 매출(만원)")
  private long existNetMan;
  private String existNetEok;
  @ApiModelProperty("올해 처음 거래한 고객 수")
  private int newCnt;
  @ApiModelProperty("신규 고객 올해 매출(만원)")
  private long newNetMan;
  private String newNetEok;
  @ApiModelProperty("올해 매출 중 기존 고객 비중(%)")
  private double existSharePct;

  @ApiModelProperty("의뢰 횟수 구간. 1회만 → 51회 이상")
  private List<StepRow> ladder = new ArrayList<StepRow>();
  @ApiModelProperty("이용 시험부 개수 구간. 1개 → 5개 이상")
  private List<StepRow> breadth = new ArrayList<StepRow>();

  @ApiModelProperty("한 번만 의뢰하고 안 온 고객")
  private List<CustRow> onceList = new ArrayList<CustRow>();
  @ApiModelProperty("한 시험부만 쓰는 단골")
  private List<CustRow> crossList = new ArrayList<CustRow>();
  @ApiModelProperty("평소보다 조용해진 고객")
  private List<CustRow> quietList = new ArrayList<CustRow>();

  @ApiModelProperty("목록에 걸린 전체 건수·금액(상위 30건만 표에 보인다)")
  private int onceTotalCnt;
  private long onceTotalMan;
  private String onceTotalEok;
  private int crossTotalCnt;
  private long crossTotalMan;
  private String crossTotalEok;
  private int quietTotalCnt;
  private long quietTotalMan;
  private String quietTotalEok;

  /** 구간 한 줄. 의뢰 횟수 구간과 시험부 개수 구간이 같은 모양이라 함께 쓴다. */
  @Getter
  @Setter
  public static class StepRow {
    /** 1~5. 이름은 화면에서 붙인다. */
    private int step;
    private String label;
    private int custCnt;
    private long netAllMan;
    private String netAllEok;
    private long netCurMan;
    /** 고객 한 곳당 누적 매출(만원). 이 화면에서 단계를 가르는 숫자. */
    private long perCustMan;
    /** 한 단계 아래 대비 몇 배인가. */
    private double stepUpX;
  }

  /** 고객 한 줄. */
  @Getter
  @Setter
  public static class CustRow {
    private int cmpySeq;
    private String cmpyName;
    private String bsnsRgnmb;
    private String mngName;
    /** 여러 줄로 등록돼 있어 합친 고객이면 그 줄 수. 1이면 합친 것 없음. */
    private int mergedSeqCnt;
    private int orders;
    private int depts;
    /** 이미 쓴 시험부 이름. */
    private String usedDepts;
    /** 아직 안 쓴 시험부 이름. */
    private String unusedDepts;
    private long netAllMan;
    private String netAllEok;
    private String firstDt;
    private String lastDt;
    /** 마지막 의뢰 후 지난 날수. */
    private int days;
    /** 지금까지 의뢰 간격 평균(일). */
    private int avgGap;
    /** 경과일 ÷ 평소 주기. 2.0 이면 평소의 두 배로 늦었다는 뜻. */
    private double overdue;
  }

  /** selectCustBase 결과 한 줄. 합치기 전 업체 단위. */
  @Getter
  @Setter
  public static class BaseRow {
    private int cmpySeq;
    private String cmpyName;
    private String bsnsRgnmb;
    private String lastMngName;
    private int orders;
    private String deptCodes;
    private long netAll;
    private long netCur;
    private String firstDt;
    private String lastDt;
  }

  /** 시험부 코드와 이름. */
  @Getter
  @Setter
  public static class DeptCode {
    private String code;
    private String name;
  }
}
