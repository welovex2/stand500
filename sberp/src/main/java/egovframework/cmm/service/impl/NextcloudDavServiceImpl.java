package egovframework.cmm.service.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import egovframework.cmm.service.FileVO;
import egovframework.cmm.service.NextcloudDavService;
import egovframework.cmm.service.UploadResultDTO;
import egovframework.cmm.service.WebDavItemDTO;
import egovframework.rte.fdl.property.EgovPropertyService;
import lombok.extern.slf4j.Slf4j;

@Service("NextcloudDavService")
@Slf4j
public class NextcloudDavServiceImpl implements NextcloudDavService {

  @Autowired
  private EgovPropertyService propertyService;

  private CloseableHttpClient http;
  private String baseUrl;
  private String user;
  private String appPassword;
  private String rootFolder;
  private String authHeader;

  @PostConstruct
  public void init() {
      http = HttpClients.createDefault();
      baseUrl      = propertyService.getString("Globals.nc.webdav.base");  // .../dav/files
      user         = propertyService.getString("Globals.nc.user");
      appPassword  = propertyService.getString("Globals.nc.appPassword");
      rootFolder   = propertyService.getString("Globals.nc.rootFolder");  // ERP

      String basic = user + ":" + appPassword;
      authHeader = "Basic " + Base64.getEncoder().encodeToString(basic.getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public void ensureFolder(String relativeFolderPath) throws Exception {
      // 0) rootFolder(예: ERP) 먼저 보장
      ensureRootFolder();

      // relativeFolderPath 예: "2025/12/RAW"
      if (relativeFolderPath == null || relativeFolderPath.trim().isEmpty()) {
          return; // ERP 루트만 쓰는 경우
      }

      // 경로 정리 (앞/뒤 슬래시 제거)
      String normalized = relativeFolderPath;
      if (normalized.startsWith("/")) normalized = normalized.substring(1);
      if (normalized.endsWith("/")) normalized = normalized.substring(0, normalized.length() - 1);

      String[] parts = normalized.split("/");
      String current = "";

      for (String part : parts) {
          if (part == null || part.trim().isEmpty()) continue;

          current = current.isEmpty() ? part : current + "/" + part;

          String url = buildFolderUrl(current); 
          // => .../dav/files/{user}/{rootFolder}/{current}

          int code = mkcol(url);

          // 201 Created: 생성됨
          // 405 Method Not Allowed: 이미 존재
          // 207 Multi-Status: 서버/프록시 변형
          if (!(code == 201 || code == 405 || code == 207)) {
              throw new RuntimeException("MKCOL fail: " + code + " url=" + url);
          }
      }
  }

  /** rootFolder 자체를 생성 (없으면 MKCOL) */
  private void ensureRootFolder() throws Exception {
      String rootUrl = buildRootFolderUrl(); 
      // => .../dav/files/{user}/{rootFolder}

      int code = mkcol(rootUrl);

      if (!(code == 201 || code == 405 || code == 207)) {
          throw new RuntimeException("MKCOL root fail: " + code + " url=" + rootUrl);
      }
  }

  /** MKCOL 공통 실행 */
  private int mkcol(String url) throws Exception {
      HttpRequestBase mkcol = new HttpRequestBase() {
          @Override public String getMethod() { return "MKCOL"; }
      };
      mkcol.setURI(URI.create(url));
      mkcol.setHeader("Authorization", authHeader);

      try {
          HttpResponse res = http.execute(mkcol);
          // 바디는 디버그할 때만 필요하면 읽어도 됨
          return res.getStatusLine().getStatusCode();
      } finally {
          mkcol.releaseConnection();
      }
  }

  /** rootFolder URL */
  private String buildRootFolderUrl() {
      return baseUrl + "/" + user + "/" + rootFolder;
  }


  @Override
  public String upload(MultipartFile file, String relativePath) throws Exception {
      // relativePath 예: 2025/12/RAW/xxx.jpg
      String url = buildFileUrl(relativePath);

      HttpPut put = new HttpPut(url);
      put.setHeader("Authorization", authHeader);
      put.setHeader("Content-Type", file.getContentType());

      try (InputStream is = file.getInputStream()) {
          put.setEntity(new InputStreamEntity(is, file.getSize()));
          HttpResponse res = http.execute(put);
          int code = res.getStatusLine().getStatusCode();

          if (!(code == 201 || code == 204)) {
              String body = res.getEntity() != null ? EntityUtils.toString(res.getEntity()) : "";
              throw new RuntimeException("PUT fail: " + code + " " + body);
          }
      } finally {
          put.releaseConnection();
      }

      // DB에 저장할 objectKey 역할: Nextcloud 내부 DAV 경로
      return "/" + rootFolder + "/" + relativePath;
  }

  private String buildFolderUrl(String relativeFolderPath) throws Exception {
      String p = (relativeFolderPath != null && relativeFolderPath.length() > 0)
              ? encodePathSegments("/" + relativeFolderPath)
              : "";
      return baseUrl + "/" + user + "/" + rootFolder + p;
  }


  private String buildFileUrl(String relativePath) throws Exception {
      // relativePath 예: 2025/12/RAW/고객사/김정미 테스트/a b.jpg
      String normalized = relativePath == null ? "" : relativePath.trim();
      if (!normalized.startsWith("/")) normalized = "/" + normalized;
  
      String encoded = encodePathSegments(normalized); // ✅ 폴더+파일 전체 세그먼트 인코딩 (슬래시 유지)
      return baseUrl + "/" + user + "/" + rootFolder + encoded;
  }

  
  @Override
  public String buildPublicRawFileUrl(String davPath) throws Exception {
      if (davPath == null || davPath.trim().isEmpty()) return null;

      String base  = propertyService.getString("Globals.nc.base");          // http://221...
      String token = propertyService.getString("Globals.nc.shareToken"); // ERP 폴더 공유 토큰
      String root  = propertyService.getString("Globals.nc.rootFolder");    // ERP

      // 1) davPath 정리
      String normalized = davPath.trim();
      if (!normalized.startsWith("/")) normalized = "/" + normalized;

      // 2) 공유 폴더(/ERP) 기준 상대경로로 변환
      //    "/ERP/2025/12/RAW/a.jpg" -> "/2025/12/RAW/a.jpg"
      String relative = normalized;
      String rootPrefix = "/" + root;
      if (relative.startsWith(rootPrefix)) {
          relative = relative.substring(rootPrefix.length());
          if (relative.isEmpty()) relative = "/";
      }

      // 3) 상대경로를 세그먼트 단위로 인코딩 (슬래시는 유지)
      String encodedRelative = encodePathSegments(relative);

      // 4) Public DAV raw URL 조립
      //    예) http://host/public.php/dav/files/{token}/2025/12/RAW/a.jpg
      return base + "/public.php/dav/files/" + token + encodedRelative;
  }

  /** "/2025/12/RAW/IMG_한글.jpg" -> "/2025/12/RAW/IMG_%ED%95%9C%EA%B8%80.jpg" */
  private String encodePathSegments(String path) throws Exception {
      if (path == null || path.isEmpty()) return "/";

      String[] parts = path.split("/");
      StringBuilder sb = new StringBuilder();

      for (String p : parts) {
          if (p.isEmpty()) continue;
          sb.append("/");
          sb.append(URLEncoder.encode(p, StandardCharsets.UTF_8.name())
                  .replace("+", "%20"));
      }
      return sb.length() == 0 ? "/" : sb.toString();
  }
  
  @Override
  public String resolveFileUrl(FileVO file) throws Exception {
      if (file == null) return "";

      String streCours = file.getFileStreCours();

      // Nextcloud
      if ("NEXTCLOUD_DAV".equals(streCours)) {
          return buildPublicRawFileUrl(file.getStreFileNm());
      }

      // Legacy
      return propertyService.getString("img.url")
              .concat(file.getAtchFileId())
              .concat("&fileSn=")
              .concat(file.getFileSn());
  }
  
  @Override
  public void deleteByDavPath(String davPath) throws Exception {
      if (davPath == null || davPath.trim().isEmpty()) return;

      // davPath 예: "/ERP/2025/12/RAW/a.jpg"
      String normalized = davPath.trim();
      if (!normalized.startsWith("/")) normalized = "/" + normalized;

      // "/ERP" 제거하고 rootFolder 기준 상대경로로 변환
      String rootPrefix = "/" + rootFolder;
      String relative = normalized.startsWith(rootPrefix)
              ? normalized.substring(rootPrefix.length())
              : normalized; // 혹시 이미 상대경로면 그대로

      if (!relative.startsWith("/")) relative = "/" + relative;

      // ✅ 세그먼트 단위 인코딩(한글/공백 대응) — 너희 파일에 이미 있음 :contentReference[oaicite:3]{index=3}
      String encoded = encodePathSegments(relative);

      String url = baseUrl + "/" + user + "/" + rootFolder + encoded;

      HttpRequestBase del = new HttpRequestBase() {
          @Override public String getMethod() { return "DELETE"; }
      };
      del.setURI(URI.create(url));
      del.setHeader("Authorization", authHeader);

      try {
          HttpResponse res = http.execute(del);
          int code = res.getStatusLine().getStatusCode();

          // 204 No Content: 삭제됨
          // 404 Not Found: 이미 없음(삭제된 상태)
          if (!(code == 204 || code == 404)) {
              String body = res.getEntity() != null ? EntityUtils.toString(res.getEntity()) : "";
              throw new RuntimeException("DELETE fail: " + code + " url=" + url + " " + body);
          }
      } finally {
          del.releaseConnection();
      }
  }

  @Override
  public InputStream downloadStreamByDavPath(String davPath) throws Exception {
      if (davPath == null || davPath.trim().isEmpty()) {
          throw new IllegalArgumentException("davPath is empty");
      }

      String normalized = davPath.trim();
      if (!normalized.startsWith("/")) normalized = "/" + normalized;

      // "/ERP/..." 형태면 rootFolder 중복 제거
      String rootPrefix = "/" + rootFolder;
      if (normalized.startsWith(rootPrefix + "/")) {
          normalized = normalized.substring(rootPrefix.length()); // "/board/..."
      }

      String encoded = encodePathSegments(normalized);
      String url = baseUrl + "/" + user + "/" + rootFolder + encoded;

      HttpGet get = new HttpGet(URI.create(url));
      get.setHeader("Authorization", authHeader);

      HttpResponse res = http.execute(get);
      int code = res.getStatusLine().getStatusCode();

      if (code != 200) {
          String body = res.getEntity() != null ? EntityUtils.toString(res.getEntity(), "UTF-8") : "";
          get.releaseConnection();
          throw new RuntimeException("Nextcloud GET failed: " + code + " url=" + url + " body=" + body);
      }

      // 성공 시: 컨트롤러에서 반드시 InputStream close 해야 연결이 반환됨
      return res.getEntity().getContent();
  }

  @Override
  public UploadResultDTO uploadToFolder(String folderDavPath, MultipartFile file) throws Exception {
      // folderDavPath: "/ERP/신청서/2026-01" 같은 값
      if (file == null || file.getOriginalFilename() == null) {
          return UploadResultDTO.fail("파일이 없습니다.");
      }

      // ✅ 업로드 전에 폴더 존재 보장: ensureFolder는 "relativeFolderPath" 기준 (rootFolder 하위) :contentReference[oaicite:5]{index=5}
      String relFolder = toRelativePathUnderRoot(folderDavPath); // "/ERP/신청서" -> "신청서"
      ensureFolder(relFolder);

      // 기존 naming 규칙(원본명_타임스탬프)은 MinIoFileMngUtil에서 만들고 있음.
      // 화면 업로드에서도 동일하게 하고 싶으면 여기에서 newName을 만들어 relativePath로 넘기면 됨.
      String safeOriginal = file.getOriginalFilename().replaceAll("[\\\\/:*?\"<>|]", "_");
      String newName = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
      String relativePath = (relFolder.isEmpty() ? "" : relFolder + "/") + safeOriginal + "_" + newName;

      String davPath = upload(file, relativePath); // 기존 PUT 업로드 사용 :contentReference[oaicite:6]{index=6}
      String publicUrl = buildPublicRawFileUrl(davPath); // 공유 raw url :contentReference[oaicite:7]{index=7}
      return UploadResultDTO.ok(davPath, publicUrl, file.getOriginalFilename());
  }

  @Override
  public WebDavItemDTO stat(String davPath) throws Exception {
      List<WebDavItemDTO> items = list(davPath, 0);
      // Depth 0이면 본인만 내려오는게 정상(멀티스테이터스 1개)
      return (items != null && !items.isEmpty()) ? items.get(0) : null;
  }

  @Override
  public List<WebDavItemDTO> list(String davPath, int depth) throws Exception {
    
      String url = buildDavUrlFromDavPath(davPath);
      
      // Depth > 0 일 때 폴더 목록을 확실히 받기 위해 trailing slash 보정
      if (depth > 0 && !url.endsWith("/")) {
          url = url + "/";
      }
      
      log.info("[NC-PROPFIND] url={}, depth={}", url, depth);

      // PROPFIND body: 필요한 속성만 요청 (Nextcloud는 oc:permissions가 유용)
      String xml =
          "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
          "<d:propfind xmlns:d=\"DAV:\" xmlns:oc=\"http://owncloud.org/ns\">\n" +
          "  <d:prop>\n" +
          "    <d:displayname />\n" +
          "    <d:resourcetype />\n" +
          "    <d:getcontentlength />\n" +
          "    <d:getlastmodified />\n" +
          "    <oc:permissions />\n" +
          "  </d:prop>\n" +
          "</d:propfind>";

      HttpEntityEnclosingRequestBase propfind = new HttpEntityEnclosingRequestBase() {
          @Override public String getMethod() { return "PROPFIND"; }
      };
      propfind.setURI(URI.create(url));
      propfind.setHeader("Authorization", authHeader);
      propfind.setHeader("Depth", String.valueOf(depth));
      propfind.setHeader("Content-Type", "application/xml; charset=utf-8");
      propfind.setEntity(new StringEntity(xml, StandardCharsets.UTF_8));

      HttpResponse res = http.execute(propfind);
      int code = res.getStatusLine().getStatusCode();
      if (code != 207) { // WebDAV Multi-Status
          String body = res.getEntity() != null ? EntityUtils.toString(res.getEntity(), "UTF-8") : "";
          propfind.releaseConnection();
          throw new RuntimeException("NC PROPFIND fail: " + code + " url=" + url + " body=" + body);
      }

      String body = EntityUtils.toString(res.getEntity(), "UTF-8");
      propfind.releaseConnection();
      
      log.info("[NC-PROPFIND] status={}", code);
      log.info("[NC-PROPFIND] bodyHead={}", body.length() > 300 ? body.substring(0, 300) : body);

      int responseCnt = body.split("<d:response>").length - 1;
      log.info("[NC-PROPFIND] responseCnt={}", responseCnt);
      
      return parseMultiStatus(body, davPath);
  }

  /** "/ERP/..." -> ".../dav/files/{user}/{rootFolder}/..." (세그먼트 인코딩) */
  private String buildDavUrlFromDavPath(String davPath) throws Exception {
      if (davPath == null || davPath.trim().isEmpty()) {
          // rootFolder 자체
          return baseUrl + "/" + user + "/" + rootFolder;
      }

      String normalized = davPath.trim();
      if (!normalized.startsWith("/")) normalized = "/" + normalized;

      // davPath가 "/ERP/..." 형태면 rootFolder prefix 제거해서 상대경로로 만든다.
      String rootPrefix = "/" + rootFolder;
      String relative = normalized;
      if (relative.startsWith(rootPrefix)) {
          relative = relative.substring(rootPrefix.length()); // "/신청서/..."
          if (relative.isEmpty()) relative = "/";
      }

      // 상대경로 세그먼트 인코딩(슬래시는 유지) - 기존 함수 사용 :contentReference[oaicite:8]{index=8}
      String encodedRelative = encodePathSegments(relative);

      return baseUrl + "/" + user + "/" + rootFolder + encodedRelative;
  }

  /** "/ERP/신청서" -> "신청서" (ensureFolder가 rootFolder 하위 relativeFolderPath를 받기 때문) :contentReference[oaicite:9]{index=9} */
  private String toRelativePathUnderRoot(String folderDavPath) {
      if (folderDavPath == null) return "";
      String p = folderDavPath.trim();
      if (!p.startsWith("/")) p = "/" + p;
      String rootPrefix = "/" + rootFolder;
      if (p.startsWith(rootPrefix + "/")) {
          p = p.substring(rootPrefix.length() + 1); // "신청서/..."
      } else if (p.equals(rootPrefix)) {
          p = "";
      } else if (p.startsWith("/")) {
          // 혹시 "/신청서" 식으로 들어오면 그냥 앞 "/" 제거
          p = p.substring(1);
      }
      // 앞/뒤 슬래시 제거
      while (p.startsWith("/")) p = p.substring(1);
      while (p.endsWith("/")) p = p.substring(0, p.length() - 1);
      return p;
  }

  /** 207 Multi-Status XML 파싱 -> WebDavItem 리스트 */
  private List<WebDavItemDTO> parseMultiStatus(String xml, String requestedDavPath) throws Exception {
    
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);   // 필수
      Document doc = factory.newDocumentBuilder()
          .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

      NodeList responses = doc.getElementsByTagNameNS("DAV:", "response");
      List<WebDavItemDTO> result = new ArrayList<>();

      for (int i = 0; i < responses.getLength(); i++) {
          org.w3c.dom.Element r = (org.w3c.dom.Element) responses.item(i);

          String href = textOf(r, "DAV:", "href"); // URL-encoded path가 나올 수 있음(표시용이 아니라 식별용)
          String displayName = textOf(r, "DAV:", "displayname");
          String lastModified = textOf(r, "DAV:", "getlastmodified");
          String lengthStr = textOf(r, "DAV:", "getcontentlength");
          String ocPerm = textOf(r, "http://owncloud.org/ns", "permissions");

          boolean isDir = hasCollection(r);
          Long size = null;
          if (!isDir && lengthStr != null && lengthStr.matches("\\d+")) {
              size = Long.valueOf(lengthStr);
          }

          // ✅ 쓰기 가능 여부: Nextcloud oc:permissions에 "W"가 들어가면 write 가능으로 표시(관례)
          boolean canWrite = ocPerm != null && ocPerm.contains("W");

          WebDavItemDTO item = new WebDavItemDTO();
          item.setName((displayName == null || displayName.isEmpty()) ? fallbackNameFromHref(href) : displayName);
          item.setDirectory(isDir);
          item.setSize(size);
          item.setLastModified(lastModified);
          item.setCanWrite(canWrite);

          // davPath는 UI에서 다시 요청하기 쉽게 "/ERP/..." 형태로 맞춰서 내려주기
          item.setDavPath(toErpDavPathFromHref(href));

          result.add(item);
      }
      
      log.info("[NC-PROPFIND] parsedItems={}", result.size());
      for (int i=0; i<Math.min(result.size(), 10); i++) {
          WebDavItemDTO it = result.get(i);
          log.info("[NC-PROPFIND] item[{}] name={}, dir={}, path={}, canWrite={}",
              i, it.getName(), it.isDirectory(), it.getDavPath(), it.isCanWrite());
      }

      // Depth=1이면 첫번째 response가 "자기 자신"인 경우가 많아서,
      // UI 목록에는 자식만 보여주려면 controller에서 첫 요소를 제거해도 됨.
      return result;
  }

  private boolean hasCollection(org.w3c.dom.Element responseEl) {
      NodeList rt = responseEl.getElementsByTagNameNS("DAV:", "resourcetype");
      if (rt == null || rt.getLength() == 0) return false;
      org.w3c.dom.Element resType = (org.w3c.dom.Element) rt.item(0);
      NodeList col = resType.getElementsByTagNameNS("DAV:", "collection");
      return col != null && col.getLength() > 0;
  }

  private String textOf(org.w3c.dom.Element root, String ns, String local) {
      NodeList list = root.getElementsByTagNameNS(ns, local);
      if (list == null || list.getLength() == 0) return null;
      String v = list.item(0).getTextContent();
      return v == null ? null : v.trim();
  }

  private String fallbackNameFromHref(String href) {
      if (href == null) return "";
      String s = href;
      if (s.endsWith("/")) s = s.substring(0, s.length() - 1);
      int idx = s.lastIndexOf("/");
      return idx >= 0 ? s.substring(idx + 1) : s;
  }

  /** href에서 "/ERP/..." 형태로 복원(대략) */
  private String toErpDavPathFromHref(String href) {
      if (href == null) return "/" + rootFolder;

      // href 예: "/remote.php/dav/files/user/ERP/신청서/..."
      // 또는 full URL이 올 수도 있음 -> "dav/files/" 이후만 잘라냄
      String h = href;
      int pos = h.indexOf("/dav/files/");
      if (pos >= 0) {
          h = h.substring(pos + "/dav/files/".length()); // "user/ERP/..."
      }
      // 앞에 user가 붙어있으면 제거
      String prefix = user + "/";
      if (h.startsWith(prefix)) {
          h = h.substring(prefix.length());
      }
      if (!h.startsWith("/")) h = "/" + h; // "/ERP/..."
      return h;
  }
  
  @Override
  public String createFolder(String parentDavPath, String folderName) throws Exception {
      // parentDavPath: "/ERP/2025/12"
      // folderName: "새폴더"

      String parent = parentDavPath == null ? "/ERP" : parentDavPath.trim();
      if (!parent.startsWith("/")) parent = "/" + parent;
      parent = parent.replaceAll("/+$", "");

      String newDavPath = parent + "/" + folderName; // "/ERP/2025/12/새폴더"

      // ✅ ensureFolder는 rootFolder 하위 relative path를 받음: "/ERP/..." -> "2025/12/새폴더"
      String rel = toRelativeUnderRoot(newDavPath); // 너희 impl에 맞게 구현
      ensureFolder(rel);

      return newDavPath;
  }

  private String toRelativeUnderRoot(String davPath) {
      // rootFolder가 "ERP"라면 "/ERP/..." -> "..." 로 변환
      String p = davPath == null ? "" : davPath.trim();
      if (!p.startsWith("/")) p = "/" + p;

      String rootPrefix = "/" + rootFolder; // "/ERP"
      if (p.equals(rootPrefix)) return "";
      if (p.startsWith(rootPrefix + "/")) {
          p = p.substring(rootPrefix.length() + 1); // "2025/12/..."
      } else if (p.startsWith("/")) {
          p = p.substring(1); // fallback
      }

      // 앞/뒤 슬래시 정리
      p = p.replaceAll("^/+", "").replaceAll("/+$", "");
      return p;
  }

  
}
