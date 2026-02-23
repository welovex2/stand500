package egovframework.ncc.service.impl;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import egovframework.cmm.filter.NcBizException;
import egovframework.cmm.service.HttpPropfind;
import egovframework.ncc.dto.NcFileDTO;
import egovframework.ncc.service.NextcloudDavService;
import egovframework.ncc.service.NextcloudFolderService;
import egovframework.ncc.service.NextcloudShareService;
import egovframework.rte.fdl.property.EgovPropertyService;
import lombok.extern.slf4j.Slf4j;

@Service("NextcloudFolderService")
@Slf4j
public class NextcloudFolderServiceImpl implements NextcloudFolderService {

  @Autowired
  private EgovPropertyService propertyService;

  @Autowired
  private NextcloudDavService nextcloudDavService;

  @Autowired
  private NextcloudShareService nextcloudShareService;

  private CloseableHttpClient http;
  private String baseUrl;
  private String user;
  private String appPassword;
  private String rootFolder;
  private String authHeader;

  @PostConstruct
  public void init() {
    http = HttpClients.createDefault();
    baseUrl = propertyService.getString("Globals.nc.webdav.base"); // .../dav/files
    user = propertyService.getString("Globals.nc.user");
    appPassword = propertyService.getString("Globals.nc.appPassword");
    rootFolder = propertyService.getString("Globals.nc.rootFolder"); // ERP

    String basic = user + ":" + appPassword;
    authHeader =
        "Basic " + Base64.getEncoder().encodeToString(basic.getBytes(StandardCharsets.UTF_8));
  }


  @Override
  public List<NcFileDTO> listErpFolder(String erpRelativeFolder) throws Exception {
    String folder = normalizeFolder(erpRelativeFolder);
    String url = baseUrl + "/" + user + "/ERP" + folder;

    String body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<d:propfind xmlns:d=\"DAV:\" xmlns:oc=\"http://owncloud.org/ns\">" + "  <d:prop>"
        + "    <d:resourcetype/>" + "    <d:getlastmodified/>" + "    <d:getcontentlength/>"
        + "    <d:getcontenttype/>" + "    <d:getetag/>" + "  </d:prop>" + "</d:propfind>";

    HttpPropfind propfind = new HttpPropfind(url);
    propfind.setHeader("Authorization", authHeader);
    propfind.setHeader("Depth", "1");
    propfind.setHeader("Content-Type", "text/xml; charset=UTF-8");
    propfind.setEntity(new StringEntity(body, StandardCharsets.UTF_8));

    HttpResponse res = http.execute(propfind);
    int code = res.getStatusLine().getStatusCode();
    String xml = res.getEntity() != null ? EntityUtils.toString(res.getEntity()) : "";

    if (code < 200 || code >= 300) {
      throw new RuntimeException("PROPFIND fail: " + code + " body=" + xml);
    }

    return parsePropfindResponse(xml, "/ERP" + folder);
  }


  private List<NcFileDTO> parsePropfindResponse(String xml, String baseDavPath) throws Exception {
    List<NcFileDTO> list = new ArrayList<>();

    // 혹시 xml 앞에 쓰레기("{}" 같은)가 붙는 케이스 방어
    int start = xml.indexOf("<?xml");
    if (start > 0)
      xml = xml.substring(start);

    DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
    f.setNamespaceAware(true); // ★ 핵심!!
    Document doc = f.newDocumentBuilder()
        .parse(new java.io.ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

    NodeList responses = doc.getElementsByTagNameNS("DAV:", "response");

    for (int i = 0; i < responses.getLength(); i++) {
      Element resp = (Element) responses.item(i);

      String href = getText(resp, "DAV:", "href"); // URL-encoded href
      if (href == null)
        continue;

      String decodedHref = java.net.URLDecoder.decode(href, "UTF-8");

      // 1) 우선 target 경로(baseDavPath)가 포함된 응답만 처리
      // decodedHref 예: /remote.php/dav/files/stderp/ERP/2025/12/RAW/xxx.jpg
      if (!decodedHref.contains(baseDavPath)) {
        continue;
      }

      // 2) "폴더 자기 자신" 항목은 제외
      // 마지막 슬래시 유무 다 고려
      if (decodedHref.endsWith(baseDavPath) || decodedHref.endsWith(baseDavPath + "/")) {
        continue;
      }

      // 3) propstat 중 200 OK 인 것만 골라서 prop 읽기
      Element okPropstat = null;
      NodeList propstats = resp.getElementsByTagNameNS("DAV:", "propstat");
      for (int p = 0; p < propstats.getLength(); p++) {
        Element ps = (Element) propstats.item(p);
        String status = getText(ps, "DAV:", "status");
        if (status != null && status.contains("200")) {
          okPropstat = ps;
          break;
        }
      }
      if (okPropstat == null)
        continue;

      Element okProp = (Element) okPropstat.getElementsByTagNameNS("DAV:", "prop").item(0);
      if (okProp == null)
        continue;

      boolean isDir = hasResourceTypeCollection(okProp); // ★ resp가 아니라 okProp 기준

      String name = extractName(decodedHref);

      String sizeStr = getText(okProp, "DAV:", "getcontentlength"); // ★ okProp 기준
      long size = 0;
      if (sizeStr != null && !sizeStr.isEmpty()) {
        try {
          size = Long.parseLong(sizeStr);
        } catch (Exception ignore) {
        }
      }

      String contentType = getText(okProp, "DAV:", "getcontenttype"); // ★ okProp 기준
      String lastMod = getText(okProp, "DAV:", "getlastmodified"); // ★ okProp 기준

      // davPath로 변환: "/ERP/..."만 남기기
      String davPath = toDavPath(decodedHref);

      NcFileDTO dto = new NcFileDTO();
      dto.setName(name);
      dto.setDavPath(davPath);
      dto.setDirectory(isDir);
      dto.setSize(size);
      dto.setContentType(contentType);
      dto.setLastModified(lastMod);

      if (!isDir) {
        dto.setDownloadUrl(nextcloudDavService.buildPublicRawFileUrl(davPath));
      }

      list.add(dto);
    }

    return list;
  }


  private String normalizeFolder(String f) {
    if (f == null || f.trim().isEmpty())
      return "/";
    String x = f.trim();
    if (!x.startsWith("/"))
      x = "/" + x;
    if (x.endsWith("/"))
      x = x.substring(0, x.length() - 1);
    return x;
  }

  private String getText(Element parent, String ns, String tag) {
    NodeList nl = parent.getElementsByTagNameNS(ns, tag);
    if (nl.getLength() == 0)
      return null;
    Node n = nl.item(0);
    return n.getTextContent();
  }

  private boolean hasResourceTypeCollection(Element resp) {
    NodeList rt = resp.getElementsByTagNameNS("DAV:", "resourcetype");
    if (rt.getLength() == 0)
      return false;
    Element r = (Element) rt.item(0);
    NodeList col = r.getElementsByTagNameNS("DAV:", "collection");
    return col.getLength() > 0;
  }

  private String extractName(String decodedHref) {
    int idx = decodedHref.lastIndexOf("/");
    if (idx < 0)
      return decodedHref;
    String name = decodedHref.substring(idx + 1);
    if (name.isEmpty()) {
      // trailing slash 폴더
      String tmp = decodedHref.substring(0, idx);
      int idx2 = tmp.lastIndexOf("/");
      return idx2 >= 0 ? tmp.substring(idx2 + 1) : tmp;
    }
    return name;
  }

  private String toDavPath(String decodedHref) {
    // /remote.php/dav/files/stderp/ERP/... -> /ERP/...
    int p = decodedHref.indexOf("/ERP/");
    if (p >= 0)
      return decodedHref.substring(p);
    if (decodedHref.endsWith("/ERP"))
      return "/ERP";
    return decodedHref; // fallback
  }

  /**
   * 폴더만 생성
   */
  @Override
  public String ensureApplyFolder(String yearMonth, String applyNo) throws Exception {
    String relativePath = yearMonth + "/" + applyNo;

    ensureFolder(relativePath);

    String commonSubFolderName1 = "00.신청서 및 공통";
    ensureFolder(relativePath + "/" + commonSubFolderName1);

    String commonSubFolderName2 = "00.신청관련서류";
    ensureFolder(relativePath + "/" + commonSubFolderName1 + "/" + commonSubFolderName2);

    String commonSubFolderName3 = "시료 반입반출";
    ensureFolder(relativePath + "/" + commonSubFolderName1 + "/" + commonSubFolderName2 + "/"
        + commonSubFolderName3);

    commonSubFolderName2 = "01.제품사진";
    ensureFolder(relativePath + "/" + commonSubFolderName1 + "/" + commonSubFolderName2);

    commonSubFolderName2 = "02.접수(완료자료)";
    ensureFolder(relativePath + "/" + commonSubFolderName1 + "/" + commonSubFolderName2);

    return relativePath;
  }

  /**
   * 폴더 생성 + 공유
   */
  @Override
  public String createApplyFolderAndGrant(String yearMonth, String applyNo, String targetId,
      boolean isGroup) throws Exception {

    // 1) 상대 경로 만들기: "2025/12/SB25-G1845"
    String relativePath = yearMonth + "/" + applyNo;

    // 2) WebDAV로 폴더 재귀 생성 (없으면 자동 MKCOL)
    ensureFolder(relativePath);

    // 2-1) 하위 공통 폴더 생성: "2025/12/SB25-G1845/00.신청서 및 공통"
    String commonSubFolderName = "00.신청서 및 공통";
    String commonRelativePath = relativePath + "/" + commonSubFolderName;
    ensureFolder(commonRelativePath);

    // 3) Nextcloud 경로(davPath)
    String davPath = "/" + rootFolder + "/" + relativePath;
    // => "/ERP/2025/12/SB25-G1845"

    // 4) 권한 주기 (기본 15 = read+create+update+delete)
    int permissions = 15;

    if (isGroup) {
      nextcloudShareService.shareToGroup(davPath, targetId, permissions);
    } else {
      nextcloudShareService.shareToUser(davPath, targetId, permissions);
    }

    return davPath;
  }

  @Override
  public void ensureFolder(String relativeFolderPath) throws Exception {
    // 0) rootFolder(예: ERP) 먼저 보장
    ensureRootFolder();

    // 1) 비어있으면 rootFolder만 사용
    if (relativeFolderPath == null || relativeFolderPath.trim().isEmpty()) {
      log.debug("[MKCOL] ensureFolder: empty -> root only");
      return;
    }

    // 2) 정규화: 슬래시 통일 + 앞뒤 슬래시 제거
    String normalized = relativeFolderPath.trim().replace("\\", "/").replaceAll("/{2,}", "/");

    if (normalized.startsWith("/"))
      normalized = normalized.substring(1);
    if (normalized.endsWith("/"))
      normalized = normalized.substring(0, normalized.length() - 1);

    // 3) traversal 방지
    if (normalized.indexOf("..") >= 0) {
      throw new NcBizException("허용되지 않는 경로입니다: " + relativeFolderPath);
    }

    // 4) 혹시 relativeFolderPath에 rootFolder(ERP)가 포함되어 들어오는 경우 제거
    // 예: "ERP/2025/12/SB..." 또는 "/ERP/2025/..."
    String rf = (rootFolder == null) ? "" : rootFolder.trim().replace("\\", "/");
    if (!rf.isEmpty()) {
      if (normalized.equals(rf)) {
        log.debug("[MKCOL] ensureFolder: normalized equals rootFolder -> root only");
        return;
      }
      if (normalized.startsWith(rf + "/")) {
        normalized = normalized.substring((rf + "/").length());
      }
    }

    if (normalized.isEmpty()) {
      log.debug("[MKCOL] ensureFolder: normalized empty after strip root -> root only");
      return;
    }

    // 5) 누적하면서 폴더 생성
    String[] parts = normalized.split("/");
    String current = "";

    for (int i = 0; i < parts.length; i++) {
      String part = parts[i];
      if (part == null)
        continue;
      part = part.trim();
      if (part.isEmpty())
        continue;

      current = current.isEmpty() ? part : (current + "/" + part);

      // ✅ buildFileUri는 baseUrl/{user}/{rootFolder}/{current} 로 세그먼트 인코딩까지 처리
      URI folderUri = buildFileUri(current);

      int code = mkcol(folderUri);

      log.debug("[MKCOL] code={} uri={} (part={})", code, folderUri, part);


      if (code == 201) {
        continue;
      }

      if (code == 405) {
        continue;
      }

      if (code == 207) {
        continue;
      }

      throw new RuntimeException("MKCOL fail code=" + code + " uri=" + folderUri);

    }

  }

  /** rootFolder 자체를 생성 (없으면 MKCOL) */
  public void ensureRootFolder() throws Exception {
    URI rootUri = buildRootFolderUri();
    // => .../dav/files/{user}/{rootFolder}

    int code = mkcol(rootUri);

    if (!(code == 201 || code == 405 || code == 207)) {
      throw new RuntimeException("MKCOL root fail: " + code + " uri=" + rootUri);
    }

    log.debug("[MKCOL] root code={} uri={}", code, rootUri);
  }


  /** MKCOL 공통 실행 */
  public int mkcol(URI uri) throws Exception {
    HttpRequestBase req = new HttpEntityEnclosingRequestBase() {
      @Override
      public String getMethod() {
        return "MKCOL";
      }
    };
    req.setURI(uri);
    req.setHeader("Authorization", authHeader);

    HttpResponse res = http.execute(req);
    EntityUtils.consumeQuietly(res.getEntity());

    return res.getStatusLine().getStatusCode();
  }

  /** rootFolder URL */
  private URI buildRootFolderUri() {
    // baseUrl: http://172.22.0.41:8090/remote.php/dav/files (files까지)
    // 최종: {baseUrl}/{user}/{rootFolder...}

    List<String> segments = new ArrayList<String>();

    String u = (user == null) ? null : user.trim();
    if (!StringUtils.isEmpty(u))
      segments.add(u);

    String rf = (rootFolder == null) ? null : rootFolder.trim();
    if (!StringUtils.isEmpty(rf)) {
      rf = rf.replace("\\", "/").replaceAll("/{2,}", "/");
      if (rf.startsWith("/"))
        rf = rf.substring(1);
      if (rf.endsWith("/"))
        rf = rf.substring(0, rf.length() - 1);

      String[] rfParts = rf.split("/");
      for (int i = 0; i < rfParts.length; i++) {
        String s = rfParts[i];
        if (s != null)
          s = s.trim();
        if (!StringUtils.isEmpty(s))
          segments.add(s);
      }
    }

    return UriComponentsBuilder.fromHttpUrl(baseUrl).pathSegment(segments.toArray(new String[0]))
        .build().toUri();
  }

  private URI buildFileUri(String relativePath) {
    // relativePath 예: 2025/12/RAW/고객사/김정미 테스트/a b.jpg

    String normalized = (relativePath == null) ? "" : relativePath.trim();
    normalized = normalized.replace("\\", "/").replaceAll("/{2,}", "/");
    if (normalized.startsWith("/"))
      normalized = normalized.substring(1);

    // 최소 방어
    if (normalized.indexOf("..") >= 0) {
      throw new NcBizException("허용되지 않는 경로입니다: " + relativePath);
    }

    List<String> segments = new ArrayList<String>();

    // ⚠️ baseUrl은 files까지 포함된 형태여야 안전합니다.
    // 예: http://172.22.0.41:8090/remote.php/dav/files
    // 최종: {baseUrl}/{user}/{rootFolder}/{relativePath...}

    String u = (user == null) ? null : user.trim();
    if (!StringUtils.isEmpty(u))
      segments.add(u);

    String rf = (rootFolder == null) ? null : rootFolder.trim();
    if (!StringUtils.isEmpty(rf)) {
      rf = rf.replace("\\", "/").replaceAll("/{2,}", "/");
      String[] rfParts = rf.split("/");
      for (int i = 0; i < rfParts.length; i++) {
        String s = rfParts[i];
        if (s != null)
          s = s.trim();
        if (!StringUtils.isEmpty(s))
          segments.add(s);
      }
    }

    if (!StringUtils.isEmpty(normalized)) {
      String[] parts = normalized.split("/");
      for (int i = 0; i < parts.length; i++) {
        String s = parts[i];
        if (s != null)
          s = s.trim();
        if (!StringUtils.isEmpty(s))
          segments.add(s);
      }
    }

    return UriComponentsBuilder.fromHttpUrl(baseUrl).pathSegment(segments.toArray(new String[0]))
        .build().toUri();
  }


}
