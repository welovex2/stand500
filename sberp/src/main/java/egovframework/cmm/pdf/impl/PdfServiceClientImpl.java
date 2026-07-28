package egovframework.cmm.pdf.impl;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Resource;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import egovframework.cmm.pdf.PdfServiceClient;
import egovframework.rte.fdl.property.EgovPropertyService;
import lombok.extern.slf4j.Slf4j;

/**
 * pdf-svc REST 클라이언트 구현.
 *
 * <p>요청 JSON 예:
 * <pre>{@code
 * {
 *   "html": "<!DOCTYPE html>...",
 *   "print_background": true,
 *   "prefer_css_page_size": true,
 *   "base_url": "http://host.docker.internal:8080/html/"  // 선택
 * }
 * }</pre>
 *
 * <p>로컬 개발: {@code GLOBALS_PDF_BASE_URL=http://localhost:8002} (Docker pdf-svc 포트 매핑)
 * <br>Docker ERP: {@code GLOBALS_PDF_BASE_URL=http://pdf-svc:8000}
 */
@Slf4j
@Service("PdfServiceClient")
public class PdfServiceClientImpl implements PdfServiceClient {

  /** pdf-svc FastAPI 엔드포인트 (main.py) */
  private static final String RENDER_PATH = "/v1/render/pdf";

  @Resource(name = "propertiesService")
  private EgovPropertyService prop;

  private final CloseableHttpClient http = HttpClients.createDefault();
  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public byte[] renderPdf(String html, String baseUrl) throws Exception {
    if (StringUtils.isEmpty(html)) {
      throw new IllegalArgumentException("html required");
    }

    // --- pdf-svc 접속 정보 (context-properties.xml) ---
    String base = safeGet("Globals.pdf.baseUrl");
    if (StringUtils.isEmpty(base)) {
      throw new IllegalStateException(
          "GLOBALS_PDF_BASE_URL 미설정. Eclipse: erp-local.properties → http://localhost:8002, "
              + "Docker: http://pdf-svc:8000");
    }
    String token = safeGet("Globals.pdf.token");

    int timeoutMs = 120_000;
    try {
      timeoutMs = prop.getInt("Globals.pdf.timeoutMs");
    } catch (Exception ignore) {
      // 미설정 시 120초 (긴 표·이미지 로딩 대비)
    }

    // --- pdf-svc RenderPdfRequest (schemas.py) 와 동일 필드 ---
    Map<String, Object> body = new LinkedHashMap<String, Object>();
    body.put("html", html);
    // 배경색·@page CSS 반영 (성적서 테두리·회색 영역)
    body.put("print_background", Boolean.TRUE);
    // @page { size: A4 } 등 CSS 페이지 크기 우선
    body.put("prefer_css_page_size", Boolean.TRUE);
    // load + fonts.ready (pdf-svc). networkidle 은 유휴 대기로 5~15초 추가될 수 있음.
    body.put("wait_until", "load");
    // 프론트 export HTML 이 ../css 상대경로일 때만 전달
    if (!StringUtils.isEmpty(baseUrl)) {
      body.put("base_url", baseUrl.trim());
    }

    String jsonBody = mapper.writeValueAsString(body);
    String url = trimTrailingSlash(base) + RENDER_PATH;

    RequestConfig rc = RequestConfig.custom()
        .setConnectTimeout(timeoutMs)
        .setSocketTimeout(timeoutMs)
        .setConnectionRequestTimeout(timeoutMs)
        .build();

    HttpPost post = new HttpPost(url);
    post.setConfig(rc);
    post.setHeader("Content-Type", "application/json");
    post.setHeader("Accept", "application/pdf");
    // PDF_INTERNAL_TOKEN 과 쌍 — 운영에서 내부망 호출 보호용
    if (!StringUtils.isEmpty(token)) {
      post.setHeader("X-Internal-Token", token);
    }
    post.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));

    long start = System.currentTimeMillis();
    try (CloseableHttpResponse res = http.execute(post)) {
      int sc = res.getStatusLine().getStatusCode();
      byte[] pdfBytes = res.getEntity() == null ? new byte[0]
          : EntityUtils.toByteArray(res.getEntity());

      // pdf-svc 오류 시 JSON/HTML 본문을 메시지에 포함
      if (sc < 200 || sc >= 300) {
        String errText = new String(pdfBytes, StandardCharsets.UTF_8);
        throw new RuntimeException("pdf-svc error: HTTP " + sc + " - " + errText);
      }

      if (pdfBytes.length == 0) {
        throw new RuntimeException("pdf-svc returned empty pdf");
      }

      log.info("pdf-svc render: {} bytes latency={}ms", pdfBytes.length,
          System.currentTimeMillis() - start);
      return pdfBytes;
    }
  }

  private String trimTrailingSlash(String s) {
    if (s == null) {
      return "";
    }
    return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
  }

  private String safeGet(String key) {
    try {
      return prop.getString(key);
    } catch (Exception e) {
      return "";
    }
  }
}
