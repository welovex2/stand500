package egovframework.sts.dto;

import java.util.ArrayList;
import java.util.List;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 시험부별 손익 한 장.
 *
 * <p>건수 비중과 매출 비중의 격차(shareGapPct)가 이 화면의 핵심이다. 격차가 음수인 시험부는 캐파를 쓰는 만큼 매출을 못
 * 내고 있다는 뜻이므로, 그 물량을 격차가 양수인 시험부로 옮기는 것이 설비 증설 없이 매출을 올리는 길이다.
 */
@Getter
@Setter
@ApiModel(description = "시험부별 손익 한 장")
public class DeptPnlDTO {

  @ApiModelProperty("기준일")
  private String asOfDate;
  @ApiModelProperty("올해")
  private int year;
  @ApiModelProperty("전년")
  private int prevYear;

  @ApiModelProperty("올해 수주 건수 합")
  private int totalCurCnt;
  @ApiModelProperty("올해 수주 순매출 합(만원)")
  private long totalCurNetMan;
  private String totalCurNetEok;
  @ApiModelProperty("전년동기 수주 순매출 합(만원)")
  private long totalPrevNetMan;
  private String totalPrevNetEok;
  @ApiModelProperty("전년동기 수주 건수 합")
  private int totalPrevCnt;
  @ApiModelProperty("순매출 전년동기 대비 증감률(%)")
  private double totalNetYoyPct;
  @ApiModelProperty("건수 전년동기 대비 증감률(%)")
  private double totalCntYoyPct;
  @ApiModelProperty("올해 건당 단가(만원)")
  private double totalCurAvgMan;
  @ApiModelProperty("전년동기 건당 단가(만원)")
  private double totalPrevAvgMan;

  @ApiModelProperty("미완료 건수 합")
  private int totalWipCnt;
  @ApiModelProperty("미완료 금액 합(만원)")
  private long totalWipMan;
  private String totalWipEok;
  @ApiModelProperty("막힌 건수 합")
  private int totalStuckCnt;
  @ApiModelProperty("막힌 금액 합(만원)")
  private long totalStuckMan;
  private String totalStuckEok;

  @ApiModelProperty("시험부별 행. 올해 순매출 큰 순")
  private List<DeptRow> rows = new ArrayList<DeptRow>();

  @Getter
  @Setter
  public static class DeptRow {

    private String deptCode;
    private String deptName;

    /* 수주(견적 발행) 기준 */
    private int curCnt;
    private long curNetMan;
    private long curChrgsMan;
    private int prevCnt;
    private long prevNetMan;
    private String curNetEok;

    /** 건당 단가(만원). 이 화면에서 시험부를 가르는 첫 번째 숫자. */
    private double curAvgMan;
    private double prevAvgMan;
    private double cntYoyPct;
    private double netYoyPct;
    private double avgYoyPct;

    /** 순매출 ÷ 청구액. 낮으면 외주·대납이 많이 끼어 있다는 뜻. */
    private double netRatePct;

    private double cntSharePct;
    private double netSharePct;
    /** 매출 비중 − 건수 비중. 음수면 캐파를 쓴 만큼 매출을 못 내는 시험부. */
    private double shareGapPct;

    /* 처리량·리드타임 */
    private int curDoneCnt;
    private double curLeadDays;
    private int curOver60Cnt;
    private int prevDoneCnt;
    private double prevLeadDays;
    /** 리드타임 올해 − 전년동기. 양수면 느려졌다. */
    private double leadDeltaDays;
    private double over60Pct;

    /* 미완료(WIP) */
    private int wipCnt;
    private long wipMan;
    private String wipEok;
    private int wipAvgAge;
    private int stuckCnt;
    private long stuckMan;
    private String stuckEok;
    private int aged90Cnt;
  }

  /** selectBooking 결과 한 줄. */
  @Getter
  @Setter
  public static class BookingRow {
    private String deptCode;
    private String deptName;
    private int curCnt;
    private long curNetMan;
    private long curChrgsMan;
    private int prevCnt;
    private long prevNetMan;
  }

  /** selectThroughput 결과 한 줄. */
  @Getter
  @Setter
  public static class ThroughputRow {
    private String deptCode;
    private int curDoneCnt;
    private double curLeadDays;
    private int curOver60Cnt;
    private int prevDoneCnt;
    private double prevLeadDays;
  }

  /** selectWip 결과 한 줄. */
  @Getter
  @Setter
  public static class WipRow {
    private String deptCode;
    private int wipCnt;
    private long wipMan;
    private int wipAvgAge;
    private int stuckCnt;
    private long stuckMan;
    private int aged90Cnt;
  }
}
