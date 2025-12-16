package egovframework.cmm.service.impl;

import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.annotation.PostConstruct;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import egovframework.cmm.service.NextcloudDavService;
import egovframework.rte.fdl.property.EgovPropertyService;

@Service("NextcloudDavService")
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

  private String buildFolderUrl(String relativeFolderPath) {
      // .../dav/files/{user}/{rootFolder}/{relativeFolderPath}
      String p = relativeFolderPath != null && relativeFolderPath.length() > 0
              ? "/" + relativeFolderPath
              : "";
      return baseUrl + "/" + user + "/" + rootFolder + p;
  }

  private String buildFileUrl(String relativePath) {
      // relativePath 전체를 encode하면 "/"도 깨져서 폴더 경로가 망가짐
      // 마지막 파일명만 encode
      int lastSlash = relativePath.lastIndexOf("/");
      String folder = lastSlash >= 0 ? relativePath.substring(0, lastSlash) : "";
      String name   = lastSlash >= 0 ? relativePath.substring(lastSlash + 1) : relativePath;
  
      try {
          name = java.net.URLEncoder.encode(name, "UTF-8").replaceAll("\\+", "%20");
      } catch (Exception ignore) {}
  
      String p = folder.isEmpty() ? name : folder + "/" + name;
      return baseUrl + "/" + user + "/" + rootFolder + "/" + p;
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
  

  
}
