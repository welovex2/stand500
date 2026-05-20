package egovframework.ncc.service.impl;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import egovframework.cmm.util.ErpDavPathUtil;
import egovframework.ncc.service.NextcloudOnlyofficeConnectorService;
import egovframework.rte.fdl.property.EgovPropertyService;
import lombok.extern.slf4j.Slf4j;

@Service("NextcloudOnlyofficeConnectorService")
@Slf4j
public class NextcloudOnlyofficeConnectorServiceImpl implements NextcloudOnlyofficeConnectorService {

  private static final ObjectMapper JSON = new ObjectMapper();

  @Autowired
  private EgovPropertyService propertyService;

  private CloseableHttpClient http;
  private String ncBase;
  private String webdavBase;
  private String ncUser;
  private String authHeader;
  private String rootFolder;

  @PostConstruct
  public void init() {
    http = HttpClients.createDefault();
    ncBase = trimTrailingSlash(propertyService.getString("Globals.nc.base"));
    webdavBase = propertyService.getString("Globals.nc.webdav.base");
    ncUser = propertyService.getString("Globals.nc.user");
    rootFolder = propertyService.getString("Globals.nc.rootFolder");
    String appPassword = propertyService.getString("Globals.nc.appPassword");
    String basic = ncUser + ":" + appPassword;
    authHeader =
        "Basic " + Base64.getEncoder().encodeToString(basic.getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public long resolveFileId(String davPath) throws Exception {
    String url = buildDavUrlFromDavPath(davPath);
    String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n"
        + "<d:propfind xmlns:d=\"DAV:\" xmlns:oc=\"http://owncloud.org/ns\">\n"
        + "  <d:prop><oc:fileid /></d:prop>\n" + "</d:propfind>";

    org.apache.http.client.methods.HttpEntityEnclosingRequestBase propfind =
        new org.apache.http.client.methods.HttpEntityEnclosingRequestBase() {
          @Override
          public String getMethod() {
            return "PROPFIND";
          }
        };
    propfind.setURI(URI.create(url));
    propfind.setHeader("Authorization", authHeader);
    propfind.setHeader("Depth", "0");
    propfind.setHeader("Content-Type", "application/xml; charset=utf-8");
    propfind.setEntity(new StringEntity(xml, StandardCharsets.UTF_8));

    HttpResponse res = http.execute(propfind);
    try {
      int code = res.getStatusLine().getStatusCode();
      String body = res.getEntity() != null ? EntityUtils.toString(res.getEntity(), "UTF-8") : "";
      if (code != 207) {
        throw new RuntimeException("PROPFIND fileid HTTP " + code + " url=" + url + " body=" + body);
      }
      String fileIdStr = firstTagText(body, "fileid");
      if (fileIdStr == null || fileIdStr.trim().isEmpty()) {
        throw new RuntimeException("oc:fileid not found for path: " + davPath);
      }
      return Long.parseLong(fileIdStr.trim());
    } finally {
      EntityUtils.consumeQuietly(res.getEntity());
      propfind.releaseConnection();
    }
  }

  @Override
  public Map<String, Object> fetchEditorConfig(String davPath, boolean viewOnly) throws Exception {
    long fileId = resolveFileId(davPath);
    String ncFilePath = toNextcloudFilesRelativePath(davPath);

    URIBuilder ub = new URIBuilder(ncBase + "/ocs/v2.php/apps/onlyoffice/api/v1/config/" + fileId);
    ub.addParameter("filePath", ncFilePath);
    ub.addParameter("inframe", "true");

    HttpGet get = new HttpGet(ub.build());
    get.setHeader("Authorization", authHeader);
    get.setHeader("OCS-APIRequest", "true");
    get.setHeader("Accept", "application/json");

    HttpResponse res = http.execute(get);
    try {
      String body = res.getEntity() != null ? EntityUtils.toString(res.getEntity(), "UTF-8") : "";
      int code = res.getStatusLine().getStatusCode();
      if (code != 200) {
        throw new RuntimeException("ONLYOFFICE OCS config HTTP " + code + " body=" + body);
      }

      JsonNode root = JSON.readTree(body);
      assertOcsSuccess(root, body);
      JsonNode data = unwrapOcsData(root);
      if (data == null) {
        throw new RuntimeException("ONLYOFFICE OCS: empty data " + body);
      }
      if (data.has("error")) {
        throw new RuntimeException("ONLYOFFICE OCS: " + data.get("error").asText());
      }
      if (data.has("redirectUrl")) {
        throw new RuntimeException(
            "ONLYOFFICE OCS redirect (로그인 필요): " + data.get("redirectUrl").asText());
      }

      @SuppressWarnings("unchecked")
      Map<String, Object> cfg = JSON.convertValue(data, Map.class);
      cfg = stripInternalKeys(cfg);

      if (viewOnly) {
        applyViewOnly(cfg);
      }

      return cfg;
    } finally {
      EntityUtils.consumeQuietly(res.getEntity());
      get.releaseConnection();
    }
  }

  private static void assertOcsSuccess(JsonNode root, String rawBody) {
    if (root == null || !root.has("ocs")) {
      return;
    }
    JsonNode meta = root.get("ocs").get("meta");
    if (meta == null || !meta.has("statuscode")) {
      return;
    }
    int statusCode = meta.get("statuscode").asInt();
    if (statusCode == 100) {
      return;
    }
    String msg = meta.has("message") ? meta.get("message").asText() : "";
    throw new RuntimeException(
        "ONLYOFFICE OCS meta.statuscode=" + statusCode + " " + msg + " body=" + rawBody);
  }

  private static JsonNode unwrapOcsData(JsonNode root) {
    if (root == null) {
      return null;
    }
    if (root.has("ocs") && root.get("ocs").has("data")) {
      return root.get("ocs").get("data");
    }
    return root;
  }

  private static Map<String, Object> stripInternalKeys(Map<String, Object> cfg) {
    Map<String, Object> out = new LinkedHashMap<String, Object>();
    for (Map.Entry<String, Object> e : cfg.entrySet()) {
      String k = e.getKey();
      if (k != null && k.startsWith("_")) {
        continue;
      }
      out.put(k, e.getValue());
    }
    return out;
  }

  @SuppressWarnings("unchecked")
  private static void applyViewOnly(Map<String, Object> cfg) {
    Object docObj = cfg.get("document");
    if (docObj instanceof Map) {
      Map<String, Object> doc = (Map<String, Object>) docObj;
      Object permObj = doc.get("permissions");
      Map<String, Object> perms;
      if (permObj instanceof Map) {
        perms = new LinkedHashMap<String, Object>((Map<String, Object>) permObj);
      } else {
        perms = new LinkedHashMap<String, Object>();
      }
      perms.put("edit", Boolean.FALSE);
      perms.put("comment", Boolean.FALSE);
      perms.put("fillForms", Boolean.FALSE);
      perms.put("modifyFilter", Boolean.FALSE);
      perms.put("modifyContentControl", Boolean.FALSE);
      perms.put("review", Boolean.FALSE);
      doc.put("permissions", perms);
      cfg.put("document", doc);
    }
    Object ecObj = cfg.get("editorConfig");
    Map<String, Object> ec;
    if (ecObj instanceof Map) {
      ec = new LinkedHashMap<String, Object>((Map<String, Object>) ecObj);
    } else {
      ec = new LinkedHashMap<String, Object>();
    }
    ec.put("mode", "view");
    ec.remove("callbackUrl");
    cfg.put("editorConfig", ec);
  }

  /** {@code /ERP/a/b.xlsx} → NC filePath 쿼리용 {@code /ERP/a/b.xlsx} */
  private String toNextcloudFilesRelativePath(String davPath) {
    String p = ErpDavPathUtil.normalizePath(davPath);
    if (p.isEmpty()) {
      return "/" + rootFolder;
    }
    return p.startsWith("/") ? p : ("/" + p);
  }

  private String buildDavUrlFromDavPath(String davPath) throws Exception {
    String normalized = ErpDavPathUtil.normalizePath(davPath);
    if (normalized.startsWith("/")) {
      normalized = normalized.substring(1);
    }
    String encoded = encodePathSegments("/" + normalized);
    return webdavBase + "/" + ncUser + encoded;
  }

  private static String encodePathSegments(String path) throws Exception {
    if (path == null || path.isEmpty()) {
      return "";
    }
    String p = path.replace("\\", "/");
    if (!p.startsWith("/")) {
      p = "/" + p;
    }
    String[] parts = p.split("/");
    StringBuilder sb = new StringBuilder();
    for (String part : parts) {
      if (part == null || part.isEmpty()) {
        continue;
      }
      sb.append('/').append(URLEncoder.encode(part, "UTF-8").replace("+", "%20"));
    }
    return sb.toString();
  }

  private static String firstTagText(String xml, String localName) throws Exception {
    javax.xml.parsers.DocumentBuilderFactory factory =
        javax.xml.parsers.DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    Document doc = factory.newDocumentBuilder()
        .parse(new java.io.ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    NodeList nodes = doc.getElementsByTagNameNS("http://owncloud.org/ns", localName);
    if (nodes.getLength() == 0) {
      nodes = doc.getElementsByTagName(localName);
    }
    if (nodes.getLength() == 0) {
      return null;
    }
    return nodes.item(0).getTextContent();
  }

  private static String trimTrailingSlash(String u) {
    if (u == null) {
      return "";
    }
    String s = u.trim();
    while (s.endsWith("/")) {
      s = s.substring(0, s.length() - 1);
    }
    return s;
  }
}
