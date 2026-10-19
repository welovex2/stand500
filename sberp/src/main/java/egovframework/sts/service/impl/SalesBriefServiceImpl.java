package egovframework.sts.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import egovframework.sts.dto.SalesBriefDTO;
import egovframework.sts.dto.SalesBriefDTO.ArRow;
import egovframework.sts.dto.SalesBriefDTO.GapRow;
import egovframework.sts.dto.SalesBriefDTO.MemberProgress;
import egovframework.sts.dto.SalesBriefDTO.UnbilledRow;
import egovframework.sts.service.SalesBriefMapper;
import egovframework.sts.service.SalesBriefService;
import egovframework.sts.util.SalesBriefSupport;

@Service("SalesBriefService")
public class SalesBriefServiceImpl implements SalesBriefService {

  private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

  @Autowired
  SalesBriefMapper salesBriefMapper;

  @Override
  public SalesBriefDTO getBrief() {
    LocalDate today = LocalDate.now(SEOUL);
    int year = today.getYear();
    int month = today.getMonthValue();
    int dayOfMonth = today.getDayOfMonth();
    int daysInMonth = today.lengthOfMonth();
    int dayOfYear = today.getDayOfYear();
    int daysInYear = today.isLeapYear() ? 366 : 365;

    SalesBriefDTO dto = new SalesBriefDTO();
    dto.setAsOfDate(today.toString());
    dto.setYear(year);
    dto.setMonth(month);
    dto.setWeekStartDate(
        today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString());

    SalesBriefDTO.TargetRow tgt = salesBriefMapper.selectTarget();
    if (tgt == null) {
      tgt = new SalesBriefDTO.TargetRow();
    }
    SalesBriefDTO.NetRow net = salesBriefMapper.selectNet();
    if (net == null) {
      net = new SalesBriefDTO.NetRow();
    }
    SalesBriefDTO.AgingRow aging = salesBriefMapper.selectAging();
    if (aging == null) {
      aging = new SalesBriefDTO.AgingRow();
    }
    SalesBriefDTO.CountAmt unbilled = salesBriefMapper.selectUnbilled();
    if (unbilled == null) {
      unbilled = new SalesBriefDTO.CountAmt();
    }
    Long weekPay = salesBriefMapper.selectWeekPayMan();
    SalesBriefDTO.CountAmt stuck = salesBriefMapper.selectStuck();
    if (stuck == null) {
      stuck = new SalesBriefDTO.CountAmt();
    }

    dto.setMonthTargetMan(tgt.getMonthTargetMan());
    dto.setYearTargetMan(tgt.getYearTargetMan());
    dto.setMonthActualMan(net.getMonthActualMan());
    dto.setYearActualMan(net.getYearActualMan());
    dto.setMonthForecastMan(
        SalesBriefSupport.monthForecast(net.getMonthActualMan(), dayOfMonth, daysInMonth));
    dto.setYearForecastMan(
        SalesBriefSupport.yearForecast(net.getYearActualMan(), dayOfYear, daysInYear));

    dto.setMonthTargetEok(SalesBriefSupport.eok(dto.getMonthTargetMan()));
    dto.setMonthActualEok(SalesBriefSupport.eok(dto.getMonthActualMan()));
    dto.setMonthForecastEok(SalesBriefSupport.eok(dto.getMonthForecastMan()));
    dto.setYearTargetEok(SalesBriefSupport.eok(dto.getYearTargetMan()));
    dto.setYearActualEok(SalesBriefSupport.eok(dto.getYearActualMan()));
    dto.setYearForecastEok(SalesBriefSupport.eok(dto.getYearForecastMan()));

    dto.setArrearsMan(aging.getArrearsMan());
    dto.setArrearsCnt(aging.getArrearsCnt());
    dto.setArrearsEok(SalesBriefSupport.eok(aging.getArrearsMan()));
    dto.setArrearsD30Man(aging.getD30Man());
    dto.setArrearsD30Cnt(aging.getD30Cnt());
    dto.setArrearsD60Man(aging.getD60Man());
    dto.setArrearsD60Cnt(aging.getD60Cnt());
    dto.setArrearsD90Man(aging.getD90Man());
    dto.setArrearsD90Cnt(aging.getD90Cnt());
    dto.setArrearsD90pMan(aging.getD90pMan());
    dto.setArrearsD90pCnt(aging.getD90pCnt());
    dto.setArrearsD90pEok(SalesBriefSupport.eok(aging.getD90pMan()));

    dto.setUnbilledCnt(unbilled.getCnt());
    dto.setUnbilledMan(unbilled.getAmtMan());
    dto.setUnbilledEok(SalesBriefSupport.eok(unbilled.getAmtMan()));

    dto.setWeekPayMan(weekPay == null ? 0L : weekPay.longValue());
    dto.setWeekPayEok(SalesBriefSupport.eok(dto.getWeekPayMan()));

    dto.setStuckCnt(stuck.getCnt());
    dto.setStuckMan(stuck.getAmtMan());
    dto.setStuckEok(SalesBriefSupport.eok(stuck.getAmtMan()));

    List<ArRow> arList = salesBriefMapper.selectArList();
    if (arList == null) {
      arList = new ArrayList<ArRow>();
    }
    for (ArRow row : arList) {
      row.setArrearsEok(SalesBriefSupport.eok(row.getArrearsMan()));
      row.setBucket(bucket(row.getDays()));
    }
    dto.setArList(arList);

    List<UnbilledRow> ubList = salesBriefMapper.selectUnbilledList();
    if (ubList == null) {
      ubList = new ArrayList<UnbilledRow>();
    }
    for (UnbilledRow row : ubList) {
      row.setNetEok(SalesBriefSupport.eok(row.getNetMan()));
    }
    dto.setUnbilledList(ubList);

    dto.setRedList(buildRedList(salesBriefMapper.selectMemberProgress(), month, dayOfMonth,
        daysInMonth));

    return dto;
  }

  static String bucket(int days) {
    if (days >= 90) {
      return "90일+";
    }
    if (days >= 60) {
      return "60–89";
    }
    if (days >= 30) {
      return "30–59";
    }
    return "30일 미만";
  }

  static List<GapRow> buildRedList(List<MemberProgress> members, int month, int dayOfMonth,
      int daysInMonth) {
    List<GapRow> gaps = new ArrayList<GapRow>();
    if (ObjectUtils.isEmpty(members)) {
      return gaps;
    }
    for (MemberProgress m : members) {
      long[] months = new long[] {m.getM1(), m.getM2(), m.getM3(), m.getM4(), m.getM5(), m.getM6(),
          m.getM7(), m.getM8(), m.getM9(), m.getM10(), m.getM11(), m.getM12()};
      long target = SalesBriefSupport.prorateYearTarget(months, month, dayOfMonth, daysInMonth);
      long actual = m.getYearActualMan();
      long gap = target - actual;
      if (gap <= 0) {
        continue;
      }
      GapRow row = new GapRow();
      row.setMemName(m.getMemName());
      row.setTargetMan(target);
      row.setActualMan(actual);
      row.setGapMan(gap);
      row.setTargetEok(SalesBriefSupport.eok(target));
      row.setActualEok(SalesBriefSupport.eok(actual));
      row.setGapEok(SalesBriefSupport.eok(gap));
      gaps.add(row);
    }
    Collections.sort(gaps, new Comparator<GapRow>() {
      @Override
      public int compare(GapRow a, GapRow b) {
        return Long.compare(b.getGapMan(), a.getGapMan());
      }
    });
    if (gaps.size() > 3) {
      return new ArrayList<GapRow>(gaps.subList(0, 3));
    }
    return gaps;
  }
}
