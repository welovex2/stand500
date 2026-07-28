package egovframework.raw.report.model;

import org.springframework.util.ObjectUtils;
import egovframework.raw.dto.ReportDTO;
import egovframework.tst.service.Test;
import lombok.Getter;
import lombok.Setter;

/** 푸터·문서 제목 등 공통 메타. */
@Getter
@Setter
public class ReportMeta {

  private String reportNo;
  private String sbkId;
  private String testId;

  public static ReportMeta from(ReportDTO report) {
    ReportMeta m = new ReportMeta();
    if (report == null) {
      return m;
    }
    m.setReportNo(resolveReportNo(report));
    m.setSbkId(report.getSbkId());
    m.setTestId(report.getTestId());
    return m;
  }

  /**
   * {@code report.reportNo} 가 비어 있으면 {@code reportList} 에서 현재 testSeq 매칭 후 fallback.
   */
  public static String resolveReportNo(ReportDTO report) {
    if (report == null) {
      return "";
    }
    if (!ObjectUtils.isEmpty(report.getReportNo())) {
      return report.getReportNo();
    }
    if (!ObjectUtils.isEmpty(report.getReportList())) {
      for (Test t : report.getReportList()) {
        if (t.getTestSeq() == report.getTestSeq() && !ObjectUtils.isEmpty(t.getReportNo())) {
          return t.getReportNo();
        }
      }
      for (int i = report.getReportList().size() - 1; i >= 0; i--) {
        String no = report.getReportList().get(i).getReportNo();
        if (!ObjectUtils.isEmpty(no)) {
          return no;
        }
      }
    }
    return "";
  }
}
