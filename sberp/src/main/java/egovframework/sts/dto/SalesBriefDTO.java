package egovframework.sts.dto;

import java.util.ArrayList;
import java.util.List;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(description = "경영진 영업 주간 한 장")
public class SalesBriefDTO {

  @ApiModelProperty("기준일")
  private String asOfDate;
  @ApiModelProperty("연")
  private int year;
  @ApiModelProperty("월")
  private int month;

  private String monthTargetEok;
  private String monthActualEok;
  private String monthForecastEok;
  private String yearTargetEok;
  private String yearActualEok;
  private String yearForecastEok;

  private long monthTargetMan;
  private long monthActualMan;
  private long monthForecastMan;
  private long yearTargetMan;
  private long yearActualMan;
  private long yearForecastMan;

  private long arrearsMan;
  private int arrearsCnt;
  private String arrearsEok;
  private long arrearsD30Man;
  private int arrearsD30Cnt;
  private long arrearsD60Man;
  private int arrearsD60Cnt;
  private long arrearsD90Man;
  private int arrearsD90Cnt;
  private long arrearsD90pMan;
  private int arrearsD90pCnt;
  private String arrearsD90pEok;

  private long unbilledMan;
  private int unbilledCnt;
  private String unbilledEok;

  private long weekPayMan;
  private String weekPayEok;
  private String weekStartDate;

  private long stuckMan;
  private int stuckCnt;
  private String stuckEok;

  private List<ArRow> arList = new ArrayList<ArRow>();
  private List<UnbilledRow> unbilledList = new ArrayList<UnbilledRow>();
  private List<GapRow> redList = new ArrayList<GapRow>();

  @Getter
  @Setter
  public static class ArRow {
    private String slsId;
    private String cmpyName;
    private String mngName;
    private long arrearsMan;
    private String arrearsEok;
    private int days;
    private String bucket;
  }

  @Getter
  @Setter
  public static class UnbilledRow {
    private String testId;
    private String quoId;
    private String cmpyName;
    private String mngName;
    private long netMan;
    private String netEok;
    private int days;
    private String endDt;
  }

  @Getter
  @Setter
  public static class GapRow {
    private String memName;
    private long targetMan;
    private long actualMan;
    private long gapMan;
    private String targetEok;
    private String actualEok;
    private String gapEok;
  }

  @Getter
  @Setter
  public static class TargetRow {
    private long yearTargetMan;
    private long monthTargetMan;
    private long m1;
    private long m2;
    private long m3;
    private long m4;
    private long m5;
    private long m6;
    private long m7;
    private long m8;
    private long m9;
    private long m10;
    private long m11;
    private long m12;
  }

  @Getter
  @Setter
  public static class NetRow {
    private long monthActualMan;
    private long yearActualMan;
  }

  @Getter
  @Setter
  public static class AgingRow {
    private long arrearsMan;
    private int arrearsCnt;
    private long d30Man;
    private int d30Cnt;
    private long d60Man;
    private int d60Cnt;
    private long d90Man;
    private int d90Cnt;
    private long d90pMan;
    private int d90pCnt;
  }

  @Getter
  @Setter
  public static class CountAmt {
    private int cnt;
    private long amtMan;
  }

  @Getter
  @Setter
  public static class MemberProgress {
    private String memId;
    private String memName;
    private long m1;
    private long m2;
    private long m3;
    private long m4;
    private long m5;
    private long m6;
    private long m7;
    private long m8;
    private long m9;
    private long m10;
    private long m11;
    private long m12;
    private long yearActualMan;
  }
}
