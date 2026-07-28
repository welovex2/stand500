package egovframework.raw.report.impl;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import egovframework.raw.dto.ReportDTO;
import egovframework.raw.report.ReportHtmlTemplateService;
import egovframework.raw.report.ReportPageModelBuilder;
import egovframework.raw.report.TelReportTemplateHelper;
import egovframework.raw.report.model.ReportDocumentModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 성적서 HTML 생성 — 2단계 파이프라인의 "렌더" 단계.
 *
 * <pre>
 * 1) ReportPageModelBuilder  ReportDTO → ReportDocumentModel
 * 2) Freemarker              report_tel_3078.html + partials → HTML 문자열
 * </pre>
 *
 * <p>템플릿 파일 위치: {@code src/main/resources/egovframework/report/html/}
 */
@Slf4j
@Service("ReportHtmlTemplateService")
public class ReportHtmlTemplateServiceImpl implements ReportHtmlTemplateService {

  private static final Pattern SAFE_TEMPLATE_NAME =
      Pattern.compile("^[a-zA-Z0-9_-]+$");

  private static final String TEMPLATE_DIR = "/egovframework/report/html";

  @Autowired(required = false)
  private List<ReportPageModelBuilder> pageModelBuilderList;

  private Configuration freemarker;
  private Map<String, ReportPageModelBuilder> pageModelBuilders;

  @PostConstruct
  public void init() {
    Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    cfg.setLogTemplateExceptions(false);
    cfg.setWrapUncheckedExceptions(true);
    cfg.setClassForTemplateLoading(ReportHtmlTemplateServiceImpl.class, TEMPLATE_DIR);
    this.freemarker = cfg;

    pageModelBuilders = new HashMap<String, ReportPageModelBuilder>();
    if (!ObjectUtils.isEmpty(pageModelBuilderList)) {
      for (ReportPageModelBuilder builder : pageModelBuilderList) {
        registerPageModelBuilder(builder);
      }
    }
    log.info("report templates registered: {}", pageModelBuilders.keySet());
  }

  @Override
  public String render(String templateName, ReportDTO report, String publicBaseUrl)
      throws Exception {
    String name = normalizeTemplateName(templateName);

    ReportPageModelBuilder builder = pageModelBuilders.get(name);
    if (builder == null) {
      throw new IllegalArgumentException("no page model builder for template: " + name);
    }
    ReportDocumentModel document = builder.build(report, normalizeBaseUrl(publicBaseUrl));

    Map<String, Object> model = new HashMap<String, Object>();
    model.put("document", document);
    model.put("h", new TelReportTemplateHelper());

    try {
      Template tpl = freemarker.getTemplate(name + ".html");
      StringWriter out = new StringWriter();
      tpl.process(model, out);
      return out.toString();
    } catch (Exception e) {
      log.error("freemarker render failed template={} testSeq={}", name,
          report == null ? null : report.getTestSeq(), e);
      throw e;
    }
  }

  @Override
  public boolean templateExists(String templateName) {
    return templateErrorMessage(templateName) == null;
  }

  @Override
  public String templateErrorMessage(String templateName) {
    try {
      String name = normalizeTemplateName(templateName);
      if (!pageModelBuilders.containsKey(name)) {
        return "page model builder not registered: " + name
            + " (registered=" + pageModelBuilders.keySet() + ")";
      }
      freemarker.getTemplate(name + ".html");
      return null;
    } catch (Exception e) {
      log.warn("report template load failed: {}", templateName, e);
      return e.getMessage();
    }
  }

  private void registerPageModelBuilder(ReportPageModelBuilder builder) {
    pageModelBuilders.put(builder.templateName(), builder);
  }

  private String normalizeTemplateName(String templateName) {
    String name = StringUtils.isEmpty(templateName) ? TEL_TEMPLATE : templateName.trim();
    if (!SAFE_TEMPLATE_NAME.matcher(name).matches()) {
      throw new IllegalArgumentException("invalid template name: " + name);
    }
    return name;
  }

  private String normalizeBaseUrl(String publicBaseUrl) {
    if (StringUtils.isEmpty(publicBaseUrl)) {
      return "http://localhost:8080/api";
    }
    String base = publicBaseUrl.trim();
    while (base.endsWith("/")) {
      base = base.substring(0, base.length() - 1);
    }
    return base;
  }
}
