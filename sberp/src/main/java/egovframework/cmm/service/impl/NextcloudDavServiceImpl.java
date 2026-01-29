package egovframework.cmm.service.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.codec.digest.DigestUtils;
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
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import egovframework.cmm.service.EgovFileMngService;
import egovframework.cmm.service.FileVO;
import egovframework.cmm.service.FolderMetaVO;
import egovframework.cmm.service.NextcloudDavService;
import egovframework.cmm.service.SbkInfoVO;
import egovframework.cmm.service.UploadResultDTO;
import egovframework.cmm.service.WebDavItemDTO;
import egovframework.cmm.util.ErpDavPathUtil;
import egovframework.rte.fdl.property.EgovPropertyService;
import egovframework.sbk.service.SbkService;
import lombok.extern.slf4j.Slf4j;

@Service("NextcloudDavService")
@Slf4j
public class NextcloudDavServiceImpl implements NextcloudDavService {

  @Autowired
  private EgovPropertyService propertyService;

  @Autowired
  private EgovFileMngService fileMngService;

  @Autowired
  private SbkService sbkService;

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
      throw new IllegalArgumentException("허용되지 않는 경로입니다: " + relativeFolderPath);
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

      // 201 Created: 생성됨
      // 405 Method Not Allowed: 이미 존재
      // 207 Multi-Status: 서버/프록시 변형 (환경에 따라 나올 수 있어 허용)
      if (!(code == 201 || code == 405 || code == 207)) {
        throw new RuntimeException("MKCOL fail: " + code + " uri=" + folderUri);
      }
    }

    log.debug("[MKCOL] ensureFolder done. input={} -> normalized={}", relativeFolderPath,
        normalized);
  }


  /** rootFolder 자체를 생성 (없으면 MKCOL) */
  private void ensureRootFolder() throws Exception {
    URI rootUri = buildRootFolderUri();
    // => .../dav/files/{user}/{rootFolder}

    int code = mkcol(rootUri);

    if (!(code == 201 || code == 405 || code == 207)) {
      throw new RuntimeException("MKCOL root fail: " + code + " uri=" + rootUri);
    }

    log.debug("[MKCOL] root code={} uri={}", code, rootUri);
  }

  /** MKCOL 공통 실행 */
  private int mkcol(URI uri) throws Exception {
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


  /**
   * 동일 경로에 파일이 존재하면(412) 업로드를 실패시키기 위한 예외.
   */
  private static class DavAlreadyExistsException extends RuntimeException {
    private final int statusCode;

    DavAlreadyExistsException(String message, int statusCode) {
      super(message);
      this.statusCode = statusCode;
    }

    int getStatusCode() {
      return statusCode;
    }
  }

  /**
   * 덮어쓰기 방지 업로드: 동일 경로에 파일이 이미 있으면 412(Precondition Failed) 발생. - Windows 스타일 이름 증가 "(1)(2)..."
   * 재시도를 위해 사용합니다.
   */
  @Override
  public String uploadIfNotExists(MultipartFile file, String relativePath) throws Exception {
    URI uri = buildFileUri(relativePath);

    HttpPut put = new HttpPut(uri);
    put.setHeader("Authorization", authHeader);
    put.setHeader("Content-Type", file.getContentType());

    // ✅ 핵심: 이미 존재하면 실패(412)하도록
    put.setHeader("If-None-Match", "*");

    try (InputStream is = file.getInputStream()) {
      put.setEntity(new InputStreamEntity(is, file.getSize()));
      HttpResponse res = http.execute(put);
      int code = res.getStatusLine().getStatusCode();

      if (code == 412) {
        String body = res.getEntity() != null ? EntityUtils.toString(res.getEntity()) : "";
        throw new DavAlreadyExistsException("PUT already exists: " + body, 412);
      }

      if (!(code == 201 || code == 204)) {
        String body = res.getEntity() != null ? EntityUtils.toString(res.getEntity()) : "";
        throw new RuntimeException("PUT fail: " + code + " " + body);
      }
    } finally {
      put.releaseConnection();
    }

    return "/" + rootFolder + "/" + relativePath;
  }

  public String upload(MultipartFile file, String relativePath) throws Exception {
    // relativePath 예: 2025/12/RAW/xxx.jpg
    URI uri = buildFileUri(relativePath);

    HttpPut put = new HttpPut(uri);
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


  private URI buildFileUri(String relativePath) {
    // relativePath 예: 2025/12/RAW/고객사/김정미 테스트/a b.jpg

    String normalized = (relativePath == null) ? "" : relativePath.trim();
    normalized = normalized.replace("\\", "/").replaceAll("/{2,}", "/");
    if (normalized.startsWith("/"))
      normalized = normalized.substring(1);

    // 최소 방어
    if (normalized.indexOf("..") >= 0) {
      throw new IllegalArgumentException("허용되지 않는 경로입니다: " + relativePath);
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

  @Override
  public String buildPublicRawFileUrl(String davPath) throws Exception {
    if (davPath == null || davPath.trim().isEmpty())
      return null;

    String base = propertyService.getString("Globals.nc.base"); // http://221...
    String token = propertyService.getString("Globals.nc.shareToken"); // ERP 폴더 공유 토큰
    String root = propertyService.getString("Globals.nc.rootFolder"); // ERP

    // 1) davPath 정리
    String normalized = davPath.trim();
    if (!normalized.startsWith("/"))
      normalized = "/" + normalized;

    // 2) 공유 폴더(/ERP) 기준 상대경로로 변환
    // "/ERP/2025/12/RAW/a.jpg" -> "/2025/12/RAW/a.jpg"
    String relative = normalized;
    String rootPrefix = "/" + root;
    if (relative.startsWith(rootPrefix)) {
      relative = relative.substring(rootPrefix.length());
      if (relative.isEmpty())
        relative = "/";
    }

    // 3) 상대경로를 세그먼트 단위로 인코딩 (슬래시는 유지)
    String encodedRelative = encodePathSegments(relative);

    // 4) Public DAV raw URL 조립
    // 예) http://host/public.php/dav/files/{token}/2025/12/RAW/a.jpg
    return base + "/public.php/dav/files/" + token + encodedRelative;
  }

  /** "/2025/12/RAW/IMG_한글.jpg" -> "/2025/12/RAW/IMG_%ED%95%9C%EA%B8%80.jpg" */
  private String encodePathSegments(String rawPath) {
    if (rawPath == null || rawPath.trim().isEmpty())
      return "/";

    String p = rawPath.trim().replace("\\", "/").replaceAll("/{2,}", "/");
    if (!p.startsWith("/"))
      p = "/" + p;

    String[] parts = p.split("/");
    StringBuilder sb = new StringBuilder();

    for (String part : parts) {
      if (part == null || part.isEmpty())
        continue;

      sb.append("/");
      sb.append(encodeSegmentUtf8(part));
    }

    return sb.length() == 0 ? "/" : sb.toString();
  }

  private String encodeSegmentUtf8(String segment) {
    try {
      // ⭐ 이미 퍼센트 인코딩이 섞여있다면(=이미 인코딩된 입력) 절대 재인코딩하지 않도록 방어
      // 예: "%EC%A0%95%EB%AF%B8%EA%BA%BC" 같은 값
      if (segment.contains("%") && segment.matches(".*%[0-9a-fA-F]{2}.*")) {
        return segment;
      }

      // URLEncoder는 공백을 + 로 바꿈 -> DAV 경로에는 %20이 안전
      return URLEncoder.encode(segment, "UTF-8").replace("+", "%20");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String resolveFileUrl(FileVO file) throws Exception {
    if (file == null)
      return "";

    String streCours = file.getFileStreCours();

    // Nextcloud
    if ("NEXTCLOUD_DAV".equals(streCours)) {
      return buildPublicRawFileUrl(file.getStreFileNm());
    }

    // Legacy
    return propertyService.getString("img.url").concat(file.getAtchFileId()).concat("&fileSn=")
        .concat(file.getFileSn());
  }

  @Override
  public void deleteByDavPath(String davPath) throws Exception {
    if (davPath == null || davPath.trim().isEmpty())
      return;

    // davPath 예: "/ERP/2025/12/RAW/a.jpg"
    String normalized = davPath.trim();
    if (!normalized.startsWith("/"))
      normalized = "/" + normalized;

    // "/ERP" 제거하고 rootFolder 기준 상대경로로 변환
    String rootPrefix = "/" + rootFolder;
    String relative =
        normalized.startsWith(rootPrefix) ? normalized.substring(rootPrefix.length()) : normalized;

    if (!relative.startsWith("/"))
      relative = "/" + relative;

    // ✅ 세그먼트 단위 인코딩(한글/공백 대응) — 너희 파일에 이미 있음 :contentReference[oaicite:3]{index=3}
    String encoded = encodePathSegments(relative);

    String url = baseUrl + "/" + user + "/" + rootFolder + encoded;

    HttpRequestBase del = new HttpRequestBase() {
      @Override
      public String getMethod() {
        return "DELETE";
      }
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
    if (!normalized.startsWith("/"))
      normalized = "/" + normalized;

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
    // folderDavPath: "/ERP/2025/12/SB25-G1583/00.공통폴더" 또는 "2025/12/SB25-G1583/00.공통폴더"
    if (file == null || file.getOriginalFilename() == null) {
      return UploadResultDTO.fail("파일이 없습니다.");
    }

    // ✅ 업로드 전에 폴더 존재 보장: ensureFolder는 "relativeFolderPath" 기준 (rootFolder 하위)
    String relFolder = toRelativePathUnderRoot(folderDavPath); // "/ERP/..." -> "2025/12/..."
    ensureFolder(relFolder);

    String safeOriginal = file.getOriginalFilename().replaceAll("[\\\\/:*?\"<>|]", "_");

    // ✅ Windows 스타일: 동일 폴더에 동일 파일명이 있으면 " (1)", " (2)" ... 를 붙여서 업로드
    String uploadedDavPath = null;

    for (int i = 0; i <= 999; i++) {
      String candidateName =
          (i == 0) ? safeOriginal : ErpDavPathUtil.withWindowsCopySuffix(safeOriginal, i);
      String candidateRelativePath = (relFolder.isEmpty() ? "" : relFolder + "/") + candidateName;

      try {
        uploadedDavPath = uploadIfNotExists(file, candidateRelativePath);
        break; // 성공
      } catch (DavAlreadyExistsException ex) {
        // 412 Precondition Failed: 동일 경로에 파일이 이미 존재함
        if (ex.getStatusCode() == 412) {
          continue;
        }
        throw ex;
      }
    }

    if (uploadedDavPath == null) {
      return UploadResultDTO.fail("동일 파일명이 너무 많아 업로드할 수 없습니다.");
    }

    String publicUrl = buildPublicRawFileUrl(uploadedDavPath); // 공유 raw url
    return UploadResultDTO.ok(uploadedDavPath, publicUrl, file.getOriginalFilename());
  }

  @Override
  public WebDavItemDTO stat(String davPath) throws Exception {
    List<WebDavItemDTO> items = list(davPath, 0);
    // Depth 0이면 본인만 내려오는게 정상(멀티스테이터스 1개)
    return (items != null && !items.isEmpty()) ? items.get(0) : null;
  }

  @Override
  public List<WebDavItemDTO> list(String davPath, int depth) throws Exception {

    // ✅ 기존 "폴더 탐색" 대신 DB 기반으로 최종 경로 결정
    String finalDavPath = resolveListPathUsingDb(davPath);

    String url = buildDavUrlFromDavPath(finalDavPath);

    // Depth > 0 일 때 폴더 목록을 확실히 받기 위해 trailing slash 보정
    if (depth > 0 && !url.endsWith("/")) {
      url = url + "/";
    }

    log.info("[NC-PROPFIND] url={}, depth={}", url, depth);

    // PROPFIND body: 필요한 속성만 요청 (Nextcloud는 oc:permissions가 유용)
    String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n"
        + "<d:propfind xmlns:d=\"DAV:\" xmlns:oc=\"http://owncloud.org/ns\">\n" + "  <d:prop>\n"
        + "    <d:displayname />\n" + "    <d:resourcetype />\n" + "    <d:getcontentlength />\n"
        + "    <d:getlastmodified />\n" + "    <oc:permissions />\n" + "  </d:prop>\n"
        + "</d:propfind>";

    HttpEntityEnclosingRequestBase propfind = new HttpEntityEnclosingRequestBase() {
      @Override
      public String getMethod() {
        return "PROPFIND";
      }
    };
    propfind.setURI(URI.create(url));
    propfind.setHeader("Authorization", authHeader);
    propfind.setHeader("Depth", String.valueOf(depth));
    propfind.setHeader("Content-Type", "application/xml; charset=utf-8");
    propfind.setEntity(new StringEntity(xml, StandardCharsets.UTF_8));

    HttpResponse res = http.execute(propfind);
    int code = res.getStatusLine().getStatusCode();

    // ✅ 폴더가 없으면(404) / 메서드 허용 안 함(405) => 빈 리스트 반환
    if (code == 404 || code == 405) {
      String errBody =
          res.getEntity() != null ? EntityUtils.toString(res.getEntity(), "UTF-8") : "";
      propfind.releaseConnection();

      log.warn("[NC-PROPFIND] missing/unsupported path. return empty. status={}, url={}, body={}",
          code, url, errBody);

      return Collections.emptyList();
    }

    if (code != 207) { // WebDAV Multi-Status
      String body = res.getEntity() != null ? EntityUtils.toString(res.getEntity(), "UTF-8") : "";
      propfind.releaseConnection();
      throw new RuntimeException("NC PROPFIND fail: " + code + " url=" + url + " body=" + body);
    }

    String body = EntityUtils.toString(res.getEntity(), "UTF-8");
    propfind.releaseConnection();

    log.info("[NC-PROPFIND] status={}", code);

    int responseCnt = body.split("<d:response>").length - 1;
    log.info("[NC-PROPFIND] responseCnt={}", responseCnt);

    List<WebDavItemDTO> items = parseMultiStatus(body, davPath);

    // 파일만 골라서 DB로 출처 조회 후 붙이기
    enrichUploadSource(items);

    return items;
  }

  private void enrichUploadSource(List<WebDavItemDTO> items) {
    if (items == null || items.isEmpty())
      return;

    // 1) 파일명만 모아서 IN 조회
    List<String> names = new java.util.ArrayList<>();
    for (WebDavItemDTO it : items) {
      if (it == null || it.isDirectory())
        continue;
      if (it.getName() != null && !it.getName().isEmpty()) {
        if (!names.contains(it.getDavPath()))
          names.add(it.getDavPath());
      }
    }
    if (names.isEmpty())
      return;

    // 2) DB에서 (STRE_FILE_NM, UPLOAD_SRC) 조회
    List<FileVO> rows = fileMngService.selectUploadSrcByPaths(names);
    if (rows == null || rows.isEmpty())
      return;

    // 3) STRE_FILE_NM -> UPLOAD_SRC 맵
    java.util.Map<String, String> srcMap = new java.util.HashMap<>();
    for (FileVO vo : rows) {
      if (vo == null || vo.getStreFileNm() == null)
        continue;
      if (!srcMap.containsKey(vo.getStreFileNm())) {
        srcMap.put(vo.getStreFileNm(), vo.getUploadSrc());
      }
    }

    // 4) DTO에 매칭해서 세팅
    for (WebDavItemDTO it : items) {
      if (it == null || it.isDirectory())
        continue;
      it.setUploadSrc(srcMap.get(it.getDavPath()));
    }
  }


  private String resolveListPathUsingDb(String inputDavPath) throws Exception {
    String p = ErpDavPathUtil.normalizePathOrRoot(inputDavPath); // 기존 함수 사용

    // 1) 신청서번호 추출
    String sbkNo = ErpDavPathUtil.extractSbkNo(p);

    // 2) DB 조회 (여기서 provisionIfMissing(result) 호출은 "list"에서는 비추)
    // list는 조회니까 폴더 생성까지 하면, 탐색만 해도 폴더가 생길 수 있음.
    SbkInfoVO sbk = sbkService.findBySbkNoReadonly(sbkNo);
    if (sbk == null || sbk.getInsDt() == null) {
      // DB도 없으면 그냥 입력 그대로(혹은 빈 리스트 유도용으로 그대로)
      return p;
    }

    String yearMonth = ErpDavPathUtil.formatYearMonthFromInsDt(sbk.getInsDt()); // "2025/01"
    String baseFolder = "/ERP/" + yearMonth + "/" + sbkNo; // "/ERP/2025/01/SB25-G1574"

    // 3) 사용자가 더 깊은 하위 폴더를 요청한 경우만 suffix 유지
    // 예: input "/ERP/xxxx/SB25-G1574/00.공통" 이면 suffix "/00.공통" 유지
    String suffix = extractSuffixAfterSbkNo(p, sbkNo); // 없으면 ""
    return ErpDavPathUtil.normalizePath(baseFolder + suffix);
  }

  private String extractSuffixAfterSbkNo(String p, String sbkNo) {
    int idx = p.toUpperCase().indexOf(sbkNo.toUpperCase());
    if (idx < 0)
      return "";
    int after = idx + sbkNo.length();
    if (after >= p.length())
      return "";
    return p.substring(after); // "/00.공통폴더/..."
  }


  /** "/ERP/..." -> ".../dav/files/{user}/{rootFolder}/..." (세그먼트 인코딩) */
  private String buildDavUrlFromDavPath(String davPath) throws Exception {
    if (davPath == null || davPath.trim().isEmpty()) {
      // rootFolder 자체
      return baseUrl + "/" + user + "/" + rootFolder;
    }

    String normalized = davPath.trim();
    if (!normalized.startsWith("/"))
      normalized = "/" + normalized;

    // davPath가 "/ERP/..." 형태면 rootFolder prefix 제거해서 상대경로로 만든다.
    String rootPrefix = "/" + rootFolder;
    String relative = normalized;
    if (relative.startsWith(rootPrefix)) {
      relative = relative.substring(rootPrefix.length()); // "/신청서/..."
      if (relative.isEmpty())
        relative = "/";
    }

    // 상대경로 세그먼트 인코딩(슬래시는 유지) - 기존 함수 사용 :contentReference[oaicite:8]{index=8}
    String encodedRelative = encodePathSegments(relative);

    return baseUrl + "/" + user + "/" + rootFolder + encodedRelative;
  }

  /**
   * "/ERP/신청서" -> "신청서" (ensureFolder가 rootFolder 하위 relativeFolderPath를 받기 때문)
   * :contentReference[oaicite:9]{index=9}
   */
  private String toRelativePathUnderRoot(String folderDavPath) {
    if (folderDavPath == null)
      return "";
    String p = folderDavPath.trim();
    if (!p.startsWith("/"))
      p = "/" + p;
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
    while (p.startsWith("/"))
      p = p.substring(1);
    while (p.endsWith("/"))
      p = p.substring(0, p.length() - 1);
    return p;
  }

  /** 207 Multi-Status XML 파싱 -> WebDavItem 리스트 */
  private List<WebDavItemDTO> parseMultiStatus(String xml, String requestedDavPath)
      throws Exception {

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true); // 필수
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

      // 쓰기 가능 여부: Nextcloud oc:permissions에 "W"가 들어가면 write 가능으로 표시(관례)
      boolean canWrite = ocPerm != null && ocPerm.contains("W");

      WebDavItemDTO item = new WebDavItemDTO();
      item.setName((displayName == null || displayName.isEmpty()) ? fallbackNameFromHref(href)
          : displayName);
      item.setDirectory(isDir);
      item.setSize(size);
      item.setLastModified(lastModified);
      item.setCanWrite(canWrite);

      // davPath는 UI에서 다시 요청하기 쉽게 "/ERP/..." 형태로 맞춰서 내려주기
      item.setDavPath(toErpDavPathFromHref(href));

      result.add(item);
    }

    log.info("[NC-PROPFIND] parsedItems={}", result.size());
    for (int i = 0; i < Math.min(result.size(), 10); i++) {
      WebDavItemDTO it = result.get(i);
      log.info("[NC-PROPFIND] item[{}] name={}, dir={}, path={}, canWrite={}", i, it.getName(),
          it.isDirectory(), it.getDavPath(), it.isCanWrite());
    }

    // Depth=1이면 첫번째 response가 "자기 자신"인 경우가 많아서,
    // UI 목록에는 자식만 보여주려면 controller에서 첫 요소를 제거해도 됨.
    return result;
  }

  private boolean hasCollection(org.w3c.dom.Element responseEl) {
    NodeList rt = responseEl.getElementsByTagNameNS("DAV:", "resourcetype");
    if (rt == null || rt.getLength() == 0)
      return false;
    org.w3c.dom.Element resType = (org.w3c.dom.Element) rt.item(0);
    NodeList col = resType.getElementsByTagNameNS("DAV:", "collection");
    return col != null && col.getLength() > 0;
  }

  private String textOf(org.w3c.dom.Element root, String ns, String local) {
    NodeList list = root.getElementsByTagNameNS(ns, local);
    if (list == null || list.getLength() == 0)
      return null;
    String v = list.item(0).getTextContent();
    return v == null ? null : v.trim();
  }

  private String fallbackNameFromHref(String href) {
    if (href == null)
      return "";
    String s = href;
    if (s.endsWith("/"))
      s = s.substring(0, s.length() - 1);
    int idx = s.lastIndexOf("/");
    return idx >= 0 ? s.substring(idx + 1) : s;
  }

  /** href에서 "/ERP/..." 형태로 복원(대략) */
  private String toErpDavPathFromHref(String href) {
    if (href == null)
      return "/" + rootFolder;

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
    if (!h.startsWith("/"))
      h = "/" + h; // "/ERP/..."
    return h;
  }

  @Override
  public String createFolder(String parentDavPath, String folderName, String userId)
      throws Exception {
    // parentDavPath: "/ERP/2025/12"
    // folderName: "새폴더"

    String parent = parentDavPath == null ? "/ERP" : parentDavPath.trim();
    if (!parent.startsWith("/"))
      parent = "/" + parent;
    parent = parent.replaceAll("/+$", "");

    String newDavPath = parent + "/" + folderName; // "/ERP/2025/12/새폴더"

    // ✅ ensureFolder는 rootFolder 하위 relative path를 받음: "/ERP/..." -> "2025/12/새폴더"
    String rel = toRelativeUnderRoot(newDavPath); // 너희 impl에 맞게 구현
    ensureFolder(rel);

    // DB 기록 (폴더 메타)
    FolderMetaVO meta = new FolderMetaVO();
    meta.setFolderPath(newDavPath); // decoded
    meta.setPathHash(DigestUtils.sha256Hex(newDavPath));
    meta.setUploadSrc("A"); // API
    meta.setCreatId(userId);

    fileMngService.upsertFolderMeta(meta);

    return newDavPath;
  }

  private String toRelativeUnderRoot(String davPath) {
    // rootFolder가 "ERP"라면 "/ERP/..." -> "..." 로 변환
    String p = davPath == null ? "" : davPath.trim();
    if (!p.startsWith("/"))
      p = "/" + p;

    String rootPrefix = "/" + rootFolder; // "/ERP"
    if (p.equals(rootPrefix))
      return "";
    if (p.startsWith(rootPrefix + "/")) {
      p = p.substring(rootPrefix.length() + 1); // "2025/12/..."
    } else if (p.startsWith("/")) {
      p = p.substring(1); // fallback
    }

    // 앞/뒤 슬래시 정리
    p = p.replaceAll("^/+", "").replaceAll("/+$", "");
    return p;
  }

  @Override
  @Transactional
  public int insertFileDetail(String atchFileId, String reqPath, String davPath,
      String originalFileName, long fileSize, String creatId) throws Exception {

    if (StringUtils.isEmpty(atchFileId)) {
      throw new IllegalArgumentException("atchFileId가 없습니다.");
    }
    if (StringUtils.isEmpty(davPath)) {
      throw new IllegalArgumentException("davPath가 없습니다.");
    }
    if (StringUtils.isEmpty(originalFileName)) {
      throw new IllegalArgumentException("originalFileName이 없습니다.");
    }
    if (StringUtils.isEmpty(creatId)) {
      throw new IllegalArgumentException("creatId(로그인ID)가 없습니다.");
    }

    // 2) 다음 FILE_SN
    FileVO key = new FileVO();
    key.setAtchFileId(atchFileId);
    int nextSn = fileMngService.getMaxFileSN(key);

    // 3) 상세 insert 값 구성
    List<FileVO> _result = new ArrayList<FileVO>();

    FileVO fvo = new FileVO();
    fvo.setFileExtsn(extractExt(originalFileName));
    fvo.setFileStreCours("NEXTCLOUD_DAV");
    fvo.setFileMg(Long.toString(fileSize));
    fvo.setOrignlFileNm(originalFileName);
    fvo.setStreFileNm(davPath);
    fvo.setAtchFileId(atchFileId);
    fvo.setFileSn(String.valueOf(nextSn));
    fvo.setCreatId(creatId);
    fvo.setAtchFileId(atchFileId);
    fvo.setUploadSrc("A"); // api에서 업로드
    _result.add(fvo);

    fileMngService.updateFileInfs(_result);

    return nextSn;
  }

  private String extractExt(String fileName) {
    // "a.tar.gz"면 "gz"로 저장(원하면 tar.gz로 바꿔도 됨)
    int idx = fileName.lastIndexOf('.');
    if (idx < 0 || idx == fileName.length() - 1)
      return "";
    return fileName.substring(idx + 1).toLowerCase();
  }

}
