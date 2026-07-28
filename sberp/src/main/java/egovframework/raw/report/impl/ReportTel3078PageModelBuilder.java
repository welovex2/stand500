package egovframework.raw.report.impl;

import org.springframework.stereotype.Service;
import egovframework.raw.dto.ReportDTO;
import egovframework.raw.report.ReportCommonPaths;
import egovframework.raw.report.ReportHtmlTemplateService;
import egovframework.raw.report.ReportPageModelBuilder;
import egovframework.raw.report.model.ReportDocumentModel;
import egovframework.raw.report.model.ReportMeta;

@Service("ReportTel3078PageModelBuilder")
public class ReportTel3078PageModelBuilder implements ReportPageModelBuilder {

  @Override
  public String templateName() {
    return ReportHtmlTemplateService.TEL_TEMPLATE;
  }

  @Override
  public ReportDocumentModel build(ReportDTO report, String publicBaseUrl) {
    ReportDocumentModel doc = new ReportDocumentModel();
    ReportCommonPaths.applyTo(doc, publicBaseUrl);
    doc.setMeta(ReportMeta.from(report));
    doc.setTel(ReportTel3078DataBuilder.build(report, publicBaseUrl));
    return doc;
  }
}
