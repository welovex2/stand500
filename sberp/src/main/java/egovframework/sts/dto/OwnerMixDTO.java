package egovframework.sts.dto;

import java.util.ArrayList;
import java.util.List;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 담당자별 포트폴리오.
 *
 * <p>같은 회사 안에서 담당자별 건당 단가가 몇 배씩 벌어진다. 이 화면은 그 차이를 시험부 구성과 고객 종류로 갈라 놓는다.
 * 단가가 낮은 쪽을 나무라는 화면이 아니다. 담당 고객군이 다르면 단가도 다르게 나오므로, 직고객 비중을 같은 표에 두어
 * 구조에서 오는 차이와 구성에서 오는 차이를 구분할 수 있게 했다.
 */
@Getter
@Setter
@ApiModel(description = "담당자별 포트폴리오")
public class OwnerMixDTO {

  @ApiModelProperty("기준일")
  private String asOfDate;
  @ApiModelProperty("올해")
  private int year;
  @ApiModelProperty("전년")
  private int prevYear;

  @ApiModelProperty("올해 실적이 있는 담당자 수")
  private int ownerCnt;
  @ApiModelProperty("올해 건수 합")
  private int totalCurCnt;
  @ApiModelProperty("올해 순매출 합(만원)")
  private long totalCurNetMan;
  private String totalCurNetEok;
  @ApiModelProperty("올해 건당 단가(만원)")
  private double totalCurAvgMan;

  @ApiModelProperty("건당 단가가 가장 높은 담당자")
  private String topAvgName;
  private double topAvgMan;
  @ApiModelProperty("건당 단가가 가장 낮은 담당자")
  private String lowAvgName;
  private double lowAvgMan;
  @ApiModelProperty("건당 단가 최고 ÷ 최저")
  private double avgSpreadX;

  @ApiModelProperty("전년동기에는 실적이 있었지만 올해는 없는 담당자 수")
  private int goneCnt;
  @ApiModelProperty("그 담당자들의 전년동기 매출 합(만원)")
  private long goneNetMan;
  private String goneNetEok;

  @ApiModelProperty("올해 실적이 있는 담당자. 매출 큰 순")
  private List<OwnerRow> rows = new ArrayList<OwnerRow>();
  @ApiModelProperty("올해 실적이 없는 담당자. 전년 매출 큰 순")
  private List<OwnerRow> goneRows = new ArrayList<OwnerRow>();
  @ApiModelProperty("표의 세로줄 순서")
  private List<DeptCol> deptCols = new ArrayList<DeptCol>();

  /** 담당자 한 줄. */
  @Getter
  @Setter
  public static class OwnerRow {
    private String mngId;
    private String mngName;

    private int curCnt;
    private long curNetMan;
    private String curNetEok;
    private int curCustCnt;
    private long curDirectMan;
    private long curHiMan;
    private int prevCnt;
    private long prevNetMan;

    /** 건당 단가(만원). */
    private double curAvgMan;
    private double prevAvgMan;
    /** 직접고객에서 나온 매출 비중(%). 낮으면 컨설팅사 물량 위주라는 뜻. */
    private double directPct;
    /** MEDICAL·SAR·RF 에서 나온 매출 비중(%). */
    private double hiPct;
    private double cntYoyPct;
    private double netYoyPct;
    private double avgYoyPct;
    /** 매출이 늘어난 까닭. "단가" / "건수" / "감소" / "신규". */
    private String growthSrc;
    /** 시험부별 비중. deptCols 와 같은 순서로 채운다. */
    private List<DeptShare> shares = new ArrayList<DeptShare>();
  }

  /** 담당자 한 줄 안의 시험부 한 칸. */
  @Getter
  @Setter
  public static class DeptShare {
    private String code;
    private String name;
    private int cnt;
    private long netMan;
    private double sharePct;
  }

  /** 표의 세로줄. */
  @Getter
  @Setter
  public static class DeptCol {
    private String code;
    private String name;
  }

  /** selectOwnerDept 결과 한 줄. */
  @Getter
  @Setter
  public static class OwnerDeptRow {
    private String mngId;
    private String deptCode;
    private int cnt;
    private long netMan;
  }
}
