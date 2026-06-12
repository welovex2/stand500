package egovframework.ncc.service.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
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
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import egovframework.cmm.filter.NcBizException;
import egovframework.cmm.service.EgovFileMngService;
import egovframework.cmm.service.FileVO;
import egovframework.cmm.service.SbkInfoVO;
import egovframework.cmm.util.ErpDavPathUtil;
import egovframework.cmm.util.FilePreviewPolicy;
import egovframework.cmm.util.OnlyOfficeIntegration;
import egovframework.ncc.dto.FileDetailUpdateVO;
import egovframework.ncc.dto.FolderMetaVO;
import egovframework.ncc.dto.UploadResultDTO;
import egovframework.ncc.dto.WebDavItemDTO;
import egovframework.ncc.dto.WebDavListResponseDTO;
import egovframework.ncc.service.NextcloudDavService;
import egovframework.ncc.service.NextcloudFolderService;
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
  NextcloudFolderService nextcloudFolderService;

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


  private void createNewFolderOrThrowDuplicate(String relativeFolderPath) throws Exception {
    // 0) rootFolder(예: ERP) 먼저 보장
    nextcloudFolderService.ensureRootFolder();

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

    // ---------------------------------------------------------
    // [추가] 20 Depth 제한 로직
    // ---------------------------------------------------------
    String[] parts = normalized.split("/");

    // 신청서 번호 패턴 (예: SB25-G1583) 위치 찾기
    int sbkIndex = -1;
    java.util.regex.Pattern sbkPattern = java.util.regex.Pattern.compile("SB\\d{2}-[GM]\\d{4}");

    for (int i = 0; i < parts.length; i++) {
      if (sbkPattern.matcher(parts[i]).matches()) {
        sbkIndex = i;
        break;
      }
    }

    // 신청서 번호를 찾았다면, 그 뒤로 오는 폴더 개수 체크
    if (sbkIndex != -1) {
      int depthAfterSbk = (parts.length - 1) - sbkIndex;
      if (depthAfterSbk > 20) {
        throw new NcBizException("폴더 생성 깊이 제한(20단계)을 초과하였습니다.");
      }
    }
    // ---------------------------------------------------------

    // 5) 누적하면서 폴더 생성
    String current = "";

    boolean duplicated = false;
    String duplicatedFolderName = null;
    for (int i = 0; i < parts.length; i++) {
      String part = parts[i];
      if (part == null)
        continue;
      part = part.trim();
      if (part.isEmpty())
        continue;

      current = current.isEmpty() ? part : (current + "/" + part);

      // buildFileUri는 baseUrl/{user}/{rootFolder}/{current} 로 세그먼트 인코딩까지 처리
      URI folderUri = buildFileUri(current);

      int code = nextcloudFolderService.mkcol(folderUri);

      log.debug("[MKCOL] code={} uri={} (part={})", code, folderUri, part);

      boolean isLast = (i == parts.length - 1);

      if (code == 201) {
        continue;
      }

      if (code == 405) {
        // 마지막 폴더에서 405면 "중복"
        if (isLast) {
          duplicated = true;
          duplicatedFolderName = part;
          break;
        }
        // 중간 폴더면 그냥 통과
        continue;
      }

      if (code == 207) {
        continue;
      }

      throw new RuntimeException("MKCOL fail code=" + code + " uri=" + folderUri);

    }

    if (duplicated) {
      throw new RuntimeException("해당 이름의 폴더가 이미 존재합니다. 다시 입력해 주세요.");
    }

  }



  /**
   * 동일 경로에 파일이 존재하면(412) 업로드를 실패시키기 위한 예외.
   */
  public static class DavAlreadyExistsException extends RuntimeException {
    private final int statusCode;

    public DavAlreadyExistsException(String message, int statusCode) {
      super(message);
      this.statusCode = statusCode;
    }

    public int getStatusCode() {
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

    // 핵심: 이미 존재하면 실패(412)하도록
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
  public String resolveFileUrl(FileVO file) {
    if (file == null)
      return "";

    String streCours = file.getFileStreCours();

    try {
      // Nextcloud
      if ("NEXTCLOUD_DAV".equals(streCours)) {
        return buildPublicRawFileUrl(file.getStreFileNm());
      }

      // Legacy
      return propertyService.getString("img.url").concat(file.getAtchFileId()).concat("&fileSn=")
          .concat(file.getFileSn());
    } catch (Exception e) {
      // 여기서만 로그 남기고 예외는 밖으로 안 던짐
      log.warn("resolveFileUrl 실패. atchFileId={}, fileSn={}", file.getAtchFileId(),
          file.getFileSn(), e);

      return null;
    }
  }

  /**
   * ERP에서 파일 삭제할때 물리적 삭제. deleteWithDbSync 메소드는 파일서버 모달에서 삭제+DB처리
   */
  @Override
  public void deleteByDavPath(String davPath) throws Exception {
    if (davPath == null || davPath.trim().isEmpty())
      return;

    // 신청서 폴더 또는 그 하위만 삭제 가능하도록 강제
    validateDeletablePathOnlyApplicationFolder(davPath);

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
      throw new NcBizException("davPath is empty");
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
    return uploadToFolder(folderDavPath, file, null, null);
  }

  @Override
  public UploadResultDTO uploadToFolder(String folderDavPath, MultipartFile file,
      String relativePath, String creatId) throws Exception {
    // folderDavPath: "/ERP/2025/12/SB25-G1583/00.공통폴더" (화면에서 선택한 현재 폴더)
    if (file == null || file.getOriginalFilename() == null) {
      return UploadResultDTO.fail("파일이 없습니다.");
    }

    /*
     * relativePath 해석
     * ─────────────────────────────────────────────────────────
     * [없음/null/blank]  → folderDavPath 바로 아래에 multipart originalFilename 업로드 (기존 동작)
     * [있음]             → folderDavPath + relativePath 위치에 업로드
     *                      예) path=/ERP/.../00.공통, relativePath=내폴더/01.서류/a.pdf
     *                      → /ERP/.../00.공통/내폴더/01.서류/a.pdf
     *
     * 프론트는 드래그앤드롭 시 file.webkitRelativePath || file.name 을 relativePath로 전달합니다.
     */
    String safeRelative = ErpDavPathUtil.resolveUploadRelativePath(relativePath,
        file.getOriginalFilename());
    String uploadFileName;
    String targetFolderDavPath;
    String parentRelForMeta = "";

    if (safeRelative == null || safeRelative.isEmpty()) {
      // ── 케이스 A: relativePath 미전달 (구 클라이언트 / relativePath 생략)
      targetFolderDavPath = folderDavPath;
      uploadFileName = file.getOriginalFilename().replaceAll("[\\\\/:*?\"<>|]", "_");
    } else {
      // ── 케이스 B: relativePath 전달 (일반 파일 name 또는 폴더 드롭 webkitRelativePath)
      uploadFileName = ErpDavPathUtil.fileNameFromRelativePath(safeRelative);
      if (uploadFileName == null || uploadFileName.isEmpty()) {
        return UploadResultDTO.fail("relativePath에 파일명이 없습니다.");
      }

      String parentRel = ErpDavPathUtil.parentRelativePath(safeRelative);
      parentRelForMeta = parentRel;
      if (parentRel.isEmpty()) {
        // relativePath = "report.pdf" → 현재 폴더 바로 아래
        targetFolderDavPath = folderDavPath;
      } else {
        // relativePath = "내폴더/01.서류/report.pdf" → 중간 폴더 포함
        targetFolderDavPath = ErpDavPathUtil.joinUnderBase(folderDavPath, parentRel);
      }
    }

    log.info(
        "[NC-UPLOAD] folderDavPath={}, relativePath(param)={}, originalFilename={}, safeRelative={}, targetFolder={}, uploadFileName={}",
        folderDavPath, relativePath, file.getOriginalFilename(), safeRelative,
        targetFolderDavPath, uploadFileName);

    /*
     * WebDAV MKCOL: 업로드 대상 폴더(및 중간 하위 폴더) 존재 보장
     * ensureFolder는 rootFolder(ERP) 하위 relative 경로 기준이므로 변환 후 호출
     */
    String relFolder = toRelativePathUnderRoot(targetFolderDavPath);
    nextcloudFolderService.ensureFolder(relFolder);

    // 폴더 드롭: path(base) 아래 새로 생긴 중간 폴더만 FOLDER_META_TB uploadSrc='A'(수동) 등록
    registerManualFolderMetaForUpload(folderDavPath, parentRelForMeta, creatId);

    // Windows 스타일: 동일 폴더에 동일 파일명이 있으면 " (1)", " (2)" ... 를 붙여서 업로드
    String uploadedDavPath = null;

    for (int i = 0; i <= 999; i++) {
      String candidateName = (i == 0) ? uploadFileName
          : ErpDavPathUtil.withWindowsCopySuffix(uploadFileName, i);
      String candidateRelativePath =
          (relFolder.isEmpty() ? "" : relFolder + "/") + candidateName;

      try {
        uploadedDavPath = uploadIfNotExists(file, candidateRelativePath);
        break;
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

    String publicUrl = buildPublicRawFileUrl(uploadedDavPath);
    return UploadResultDTO.ok(uploadedDavPath, publicUrl, uploadFileName);
  }

  /**
   * 폴더 업로드(relativePath에 하위 폴더 포함) 시 FOLDER_META_TB에 uploadSrc='A' 등록.
   * ERP 상위·업로드 base 폴더는 제외하고 base 아래 중간 세그먼트만 대상.
   */
  private void registerManualFolderMetaForUpload(String folderDavPath, String parentRel,
      String creatId) {
    if (parentRel == null || parentRel.isEmpty()) {
      return;
    }
    if (creatId == null || creatId.trim().isEmpty()) {
      return;
    }
    for (String folderPath : ErpDavPathUtil.expandFolderDavPathsUnderBase(folderDavPath,
        parentRel)) {
      FolderMetaVO meta = new FolderMetaVO();
      meta.setFolderPath(folderPath);
      meta.setPathHash(DigestUtils.sha256Hex(folderPath));
      meta.setUploadSrc("A");
      meta.setCreatId(creatId);
      try {
        fileMngService.insertFolderMeta(meta);
      } catch (Exception e) {
        log.warn("folder upload manual meta upsert fail. path={}", folderPath, e);
      }
    }
  }

  @Override
  public WebDavListResponseDTO list(String davPath, int depth) throws Exception {

    // 기존 "폴더 탐색" 대신 DB 기반으로 최종 경로 결정
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

    // 응답 DTO 기본 골격 먼저 만들기
    WebDavListResponseDTO result = new WebDavListResponseDTO();
    result.setDavPath(finalDavPath);

    // 폴더가 없으면(404) / 메서드 허용 안 함(405) => 빈 리스트
    if (code == 404 || code == 405) {
      String errBody =
          (res.getEntity() != null) ? EntityUtils.toString(res.getEntity(), "UTF-8") : "";
      propfind.releaseConnection();

      log.warn("[NC-PROPFIND] missing/unsupported path. return empty. status={}, url={}, body={}",
          code, url, errBody);

      result.setItems(Collections.<WebDavItemDTO>emptyList());
      return result;
    }

    if (code != 207) { // WebDAV Multi-Status
      String err = (res.getEntity() != null) ? EntityUtils.toString(res.getEntity(), "UTF-8") : "";
      propfind.releaseConnection();
      throw new RuntimeException("NC PROPFIND fail: " + code + " url=" + url + " body=" + err);
    }

    String body = EntityUtils.toString(res.getEntity(), "UTF-8");
    propfind.releaseConnection();

    log.info("[NC-PROPFIND] status={}", code);

    int responseCnt = body.split("<d:response>").length - 1;
    log.info("[NC-PROPFIND] responseCnt={}", responseCnt);

    List<WebDavItemDTO> items = parseMultiStatus(body, davPath);

    // 파일만 골라서 DB로 출처 조회 후 붙이기 (+ 폴더도 붙이려면 enrich 확장)
    enrichUploadSource(items);

    result.setItems(items);
    return result;
  }

  private void enrichUploadSource(List<WebDavItemDTO> items) {
    if (items == null || items.isEmpty())
      return;

    // 1) 파일명만 모아서 IN 조회
    List<String> filePaths = new java.util.ArrayList<>();
    List<String> folderPaths = new java.util.ArrayList<>();

    for (WebDavItemDTO it : items) {
      if (it == null)
        continue;

      String p = it.getDavPath();
      if (p == null || p.trim().isEmpty())
        continue;

      if (it.isDirectory()) {
        // ✅ 폴더는 trailing slash 제거 후 DB와 매칭
        String norm = p.replaceAll("/+$", "");
        if (!folderPaths.contains(norm))
          folderPaths.add(norm);
      } else {
        // ✅ 파일은 기존 로직 유지 (davPath 그대로)
        if (!filePaths.contains(p))
          filePaths.add(p);
      }
    }

    // 1) 파일 uploadSrc 맵
    java.util.Map<String, String> fileSrcMap = new java.util.HashMap<>();
    if (!filePaths.isEmpty()) {
      List<FileVO> rows = fileMngService.selectUploadSrcByPaths(filePaths);
      if (rows != null) {
        for (FileVO vo : rows) {
          if (vo == null || vo.getStreFileNm() == null)
            continue;
          if (!fileSrcMap.containsKey(vo.getStreFileNm())) {
            fileSrcMap.put(vo.getStreFileNm(), vo.getUploadSrc());
          }
        }
      }
    }

    // 2) 폴더 uploadSrc 맵 (FOLDER_META_TB 조회)
    java.util.Map<String, String> folderSrcMap = new java.util.HashMap<>();
    if (!folderPaths.isEmpty()) {
      List<FolderMetaVO> frows = fileMngService.selectFolderUploadSrcByPaths(folderPaths);
      if (frows != null) {
        for (FolderMetaVO vo : frows) {
          if (vo == null || vo.getFolderPath() == null)
            continue;
          if (!folderSrcMap.containsKey(vo.getFolderPath())) {
            folderSrcMap.put(vo.getFolderPath(), vo.getUploadSrc());
          }
        }
      }
    }

    boolean onlyofficeEnabled = OnlyOfficeIntegration.isEnabled(propertyService);

    // 4) DTO에 uploadSrc · previewType · mimeType 매칭
    for (WebDavItemDTO it : items) {
      if (it == null)
        continue;

      if (it.isDirectory()) {
        String norm = (it.getDavPath() == null) ? null : it.getDavPath().replaceAll("/+$", "");
        it.setUploadSrc(norm == null ? "E" : folderSrcMap.getOrDefault(norm, "E"));
        it.setMimeType(null);
        it.setPreviewable(false);
        it.setPreviewType(FilePreviewPolicy.TYPE_NONE);
      } else {
        it.setUploadSrc(fileSrcMap.get(it.getDavPath()));
        String fileName = it.getName();
        if (fileName == null || fileName.trim().isEmpty()) {
          fileName = nameOfDavPath(it.getDavPath());
        }
        FilePreviewPolicy.Info preview = FilePreviewPolicy.classify(fileName, onlyofficeEnabled);
        it.setMimeType(preview.getMimeType());
        it.setPreviewable(preview.isPreviewable());
        it.setPreviewType(preview.getPreviewType());
      }
    }

  }


  private String resolveListPathUsingDb(String inputDavPath) throws Exception {
    String p = ErpDavPathUtil.normalizePathOrRoot(inputDavPath); // 기존 함수 사용
    log.info("[resolveListPath] inputDavPath={}, normalized={}", inputDavPath, p);

    // 1) 신청서번호 추출
    String sbkNo = ErpDavPathUtil.extractSbkNo(p);
    log.info("[resolveListPath] sbkNo={}", sbkNo);

    // 2) DB 조회 (여기서 provisionIfMissing(result) 호출은 "list"에서는 비추)
    // list는 조회니까 폴더 생성까지 하면, 탐색만 해도 폴더가 생길 수 있음.
    SbkInfoVO sbk = sbkService.findBySbkNoReadonly(sbkNo);
    log.info("[resolveListPath] sbk={}", sbk);
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
      item.setLastModified(formatLastModified(lastModified));
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

  private String formatLastModified(String webDavDate) {
    if (webDavDate == null || webDavDate.isEmpty()) {
      return null;
    }

    try {
      java.time.ZonedDateTime zdt = java.time.ZonedDateTime.parse(webDavDate,
          java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME);

      // 한국 시간으로 변환
      java.time.ZonedDateTime kst = zdt.withZoneSameInstant(java.time.ZoneId.of("Asia/Seoul"));

      int hour = kst.getHour();
      String ampm = hour < 12 ? "오전" : "오후";

      java.time.format.DateTimeFormatter fmt =
          java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd");

      return kst.format(fmt) + " " + ampm + " "
          + String.format("%02d:%02d", hour % 12 == 0 ? 12 : hour % 12, kst.getMinute());

    } catch (Exception e) {
      return webDavDate;
    }
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

  /** href에서 "/ERP/..." 형태로 복원. WebDAV href 는 세그먼트별 URL 인코딩되어 있으므로 디코딩한다. */
  private String toErpDavPathFromHref(String href) {
    if (href == null)
      return "/" + rootFolder;

    // href 예: "/remote.php/dav/files/user/ERP/%EC%8B%A0%EC%B2%AD%EC%84%9C/..."
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
      h = "/" + h;
    return decodeDavPathFromHref(h);
  }

  /** PROPFIND href 경로 세그먼트를 UTF-8 디코딩 후 NFC 정규화 (DB FOLDER_PATH 와 매칭용). */
  private static String decodeDavPathFromHref(String path) {
    if (path == null || path.isEmpty()) {
      return path;
    }
    String p = path.replace("\\", "/").replaceAll("/{2,}", "/");
    if (!p.startsWith("/")) {
      p = "/" + p;
    }
    String[] parts = p.split("/");
    StringBuilder sb = new StringBuilder();
    for (String part : parts) {
      if (part == null || part.isEmpty()) {
        continue;
      }
      sb.append('/');
      try {
        sb.append(URLDecoder.decode(part, "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        sb.append(part);
      }
    }
    String decoded = sb.length() == 0 ? "/" : sb.toString();
    return Normalizer.normalize(decoded, Normalizer.Form.NFC);
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

    String rel = toRelativeUnderRoot(newDavPath); // 너희 impl에 맞게 구현
    createNewFolderOrThrowDuplicate(rel);

    // DB 기록 (폴더 메타)
    FolderMetaVO meta = new FolderMetaVO();
    meta.setFolderPath(newDavPath); // decoded
    meta.setPathHash(DigestUtils.sha256Hex(newDavPath));
    meta.setUploadSrc("A"); // API
    meta.setCreatId(userId);

    fileMngService.insertFolderMeta(meta);

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
      throw new NcBizException("atchFileId가 없습니다.");
    }
    if (StringUtils.isEmpty(davPath)) {
      throw new NcBizException("davPath가 없습니다.");
    }
    if (StringUtils.isEmpty(originalFileName)) {
      throw new NcBizException("originalFileName이 없습니다.");
    }
    if (StringUtils.isEmpty(creatId)) {
      throw new NcBizException("creatId(로그인ID)가 없습니다.");
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

  @Override
  public boolean existsDirectory(String davPath) throws Exception {
    String finalDavPath = ErpDavPathUtil.normalizePathOrRoot(davPath);
    String url = buildDavUrlFromDavPath(finalDavPath);

    // 폴더 existence 체크는 trailing slash 권장
    if (!url.endsWith("/"))
      url += "/";

    String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" + "<d:propfind xmlns:d=\"DAV:\">\n"
        + "  <d:prop>\n" + "    <d:resourcetype />\n" + "  </d:prop>\n" + "</d:propfind>";

    HttpEntityEnclosingRequestBase propfind = new HttpEntityEnclosingRequestBase() {
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
    int code = res.getStatusLine().getStatusCode();
    String body = res.getEntity() != null ? EntityUtils.toString(res.getEntity(), "UTF-8") : "";
    propfind.releaseConnection();

    // 404/405면 없음
    if (code == 404 || code == 405)
      return false;

    // Nextcloud DAV는 보통 207
    if (code != 207)
      return false;

    // ✅ 핵심: 207이어도 내부에 404가 있으면 없음
    // (간단판정)
    if (body.contains(" 404 "))
      return false;

    // ✅ 폴더(컬렉션) 표시가 있으면 존재
    return body.contains("<d:collection") || body.contains("<d:collection/>");
  }

  // NextcloudDavServiceImpl 클래스 내부에 추가

  @Override
  public String rename(String targetDavPath, String newName, boolean overwrite, String userId)
      throws Exception {

    String src = ErpDavPathUtil.normalizePathOrRoot(targetDavPath);
    if (!src.startsWith("/")) {
      src = "/" + src;
    }

    if (newName == null) {
      throw new NcBizException("newName이 비어있습니다.");
    }

    String nn = newName.trim();
    if (nn.isEmpty()) {
      throw new NcBizException("newName이 비어있습니다.");
    }

    String parent = parentOfDavPath(src);
    if (parent == null || parent.trim().isEmpty()) {
      throw new NcBizException("부모 경로를 계산할 수 없습니다.");
    }

    String dest = ErpDavPathUtil.normalizePath(parent + "/" + nn);

    if (src.equals(dest)) {
      return dest;
    }

    move(src, dest, overwrite, userId);

    WebDavItemDTO dstItem = stat(dest);

    if (dstItem != null && dstItem.isDirectory()) {
      FolderMetaVO meta = new FolderMetaVO();
      meta.setFolderPath(dest);
      meta.setPathHash(DigestUtils.sha256Hex(dest));
      meta.setUploadSrc("A");
      meta.setCreatId(userId);

      try {
        fileMngService.updateFolderMetaByPathHash(targetDavPath, meta);
      } catch (Exception e) {
        // 메타 저장 실패는 rename 자체를 실패로 보지 않도록 방어
        log.warn("rename meta upsert fail. dest={}", dest, e);
      }
    } else {
      updateFileDetail(src, dest, newName, userId);
    }

    return dest;
  }

  private WebDavItemDTO stat(String davPath) throws Exception {
    String path = ErpDavPathUtil.normalizePathOrRoot(davPath);
    if (!path.startsWith("/")) {
      path = "/" + path;
    }

    String url = buildDavUrlFromDavPath(path);

    HttpRequestBase propfind = new HttpEntityEnclosingRequestBase() {
      @Override
      public String getMethod() {
        return "PROPFIND";
      }
    };

    propfind.setURI(URI.create(url));
    propfind.setHeader("Authorization", authHeader);
    propfind.setHeader("Depth", "0");
    propfind.setHeader("Content-Type", "application/xml; charset=UTF-8");

    // 최소 속성만 요청
    String body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<d:propfind xmlns:d=\"DAV:\">"
        + "  <d:prop>" + "    <d:resourcetype/>" + "    <d:getcontentlength/>"
        + "    <d:getlastmodified/>" + "  </d:prop>" + "</d:propfind>";

    ((HttpEntityEnclosingRequestBase) propfind).setEntity(new StringEntity(body, "UTF-8"));

    HttpResponse res = null;
    try {
      res = http.execute(propfind);
      int code = res.getStatusLine().getStatusCode();

      // 존재하지 않음
      if (code == 404) {
        return null;
      }

      // 정상 응답은 207 Multi-Status
      if (code != 207) {
        String respBody =
            res.getEntity() != null ? EntityUtils.toString(res.getEntity(), "UTF-8") : "";
        throw new RuntimeException("PROPFIND fail: " + code + " body=" + respBody);
      }

      String xml = EntityUtils.toString(res.getEntity(), "UTF-8");
      return parseStatFromPropfind(xml, path);

    } finally {
      propfind.releaseConnection();
    }
  }

  private WebDavItemDTO parseStatFromPropfind(String xml, String davPath) throws Exception {

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);

    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(new InputSource(new StringReader(xml)));

    NodeList responses = doc.getElementsByTagNameNS("DAV:", "response");
    if (responses == null || responses.getLength() == 0) {
      return null;
    }

    Element response = (Element) responses.item(0);

    boolean isDir = false;
    Long size = null;
    String lastModified = null;

    NodeList propstats = response.getElementsByTagNameNS("DAV:", "propstat");
    for (int i = 0; i < propstats.getLength(); i++) {
      Element propstat = (Element) propstats.item(i);
      NodeList props = propstat.getElementsByTagNameNS("DAV:", "prop");
      if (props.getLength() == 0) {
        continue;
      }

      Element prop = (Element) props.item(0);

      // 폴더 여부
      NodeList resType = prop.getElementsByTagNameNS("DAV:", "resourcetype");
      if (resType.getLength() > 0) {
        Element rt = (Element) resType.item(0);
        NodeList collection = rt.getElementsByTagNameNS("DAV:", "collection");
        isDir = (collection != null && collection.getLength() > 0);
      }

      // 파일 크기
      NodeList len = prop.getElementsByTagNameNS("DAV:", "getcontentlength");
      if (len.getLength() > 0) {
        String v = len.item(0).getTextContent();
        if (v != null && !v.isEmpty()) {
          size = Long.parseLong(v);
        }
      }

      // 수정일
      NodeList lm = prop.getElementsByTagNameNS("DAV:", "getlastmodified");
      if (lm.getLength() > 0) {
        lastModified = lm.item(0).getTextContent();
      }
    }

    WebDavItemDTO dto = new WebDavItemDTO();
    dto.setDavPath(davPath);
    dto.setDirectory(isDir);
    dto.setSize(size);
    dto.setLastModified(lastModified);

    return dto;
  }


  @Transactional
  public int updateFileDetail(String oldDavPath, String newDavPath, String newOriginalFileName,
      String updtId) throws Exception {

    if (StringUtils.isEmpty(oldDavPath)) {
      throw new NcBizException("oldDavPath가 없습니다.");
    }
    if (StringUtils.isEmpty(newDavPath)) {
      throw new NcBizException("newDavPath가 없습니다.");
    }
    if (StringUtils.isEmpty(newOriginalFileName)) {
      throw new NcBizException("newOriginalFileName이 없습니다.");
    }
    if (StringUtils.isEmpty(updtId)) {
      throw new NcBizException("updtId(로그인ID)가 없습니다.");
    }

    FileDetailUpdateVO vo = new FileDetailUpdateVO();
    vo.setOldDavPath(oldDavPath);
    vo.setNewDavPath(newDavPath);
    vo.setNewOriginalFileName(newOriginalFileName);
    vo.setFileExtsn(extractExt(newOriginalFileName));
    vo.setUpdtId(updtId);

    int updated = fileMngService.updateFileDetailByStreFileNm(vo);

    if (updated <= 0) {
      throw new NcBizException("파일 상세 정보가 존재하지 않습니다.");
    }

    return updated;
  }

  @Override
  public String move(String sourceDavPath, String destDavPath, boolean overwrite, String userId)
      throws Exception {

    String src = ErpDavPathUtil.normalizePathOrRoot(sourceDavPath);
    String dst = ErpDavPathUtil.normalizePathOrRoot(destDavPath);

    if (!src.startsWith("/")) {
      src = "/" + src;
    }
    if (!dst.startsWith("/")) {
      dst = "/" + dst;
    }

    String srcUrl = buildDavUrlFromDavPath(src);
    String dstUrl = buildDavUrlFromDavPath(dst);

    HttpRequestBase moveReq = new HttpEntityEnclosingRequestBase() {
      @Override
      public String getMethod() {
        return "MOVE";
      }
    };

    moveReq.setURI(URI.create(srcUrl));
    moveReq.setHeader("Authorization", authHeader);

    // Destination은 절대경로 URL로 넣는게 안전
    moveReq.setHeader("Destination", dstUrl);

    // 덮어쓰기 여부
    // T: 대상이 있으면 덮어씀
    // F: 대상이 있으면 실패
    moveReq.setHeader("Overwrite", overwrite ? "T" : "F");

    HttpResponse res = null;
    try {
      res = http.execute(moveReq);
      int code = res.getStatusLine().getStatusCode();

      // MOVE 성공 케이스
      // 201 Created, 204 No Content 가 흔함
      if (code == 201 || code == 204) {
        EntityUtils.consumeQuietly(res.getEntity());
        return dst;
      }

      // 대상이 이미 존재하고 Overwrite=F 인 경우 412가 흔함
      if (code == 412) {
        String body = res.getEntity() != null ? EntityUtils.toString(res.getEntity(), "UTF-8") : "";
        throw new NcBizException("대상 이름이 이미 존재합니다. 다른 이름을 사용해 주세요.");
      }

      // 소스가 없는 경우
      if (code == 404) {
        String body = res.getEntity() != null ? EntityUtils.toString(res.getEntity(), "UTF-8") : "";
        throw new IllegalArgumentException("원본이 존재하지 않습니다.");
      }

      String body = res.getEntity() != null ? EntityUtils.toString(res.getEntity(), "UTF-8") : "";
      throw new RuntimeException(
          "MOVE fail: " + code + " src=" + srcUrl + " dst=" + dstUrl + " body=" + body);

    } finally {
      moveReq.releaseConnection();
    }
  }

  @Override
  @Transactional
  public void deleteWithDbSync(String davPath, boolean recursive, String userId) throws Exception {

    String path = ErpDavPathUtil.normalizePathOrRoot(davPath);
    if (!path.startsWith("/")) {
      path = "/" + path;
    }

    // 신청서 폴더 또는 그 하위만 삭제 가능하도록 강제
    validateDeletablePathOnlyApplicationFolder(path);

    WebDavItemDTO before = stat(path);
    if (before == null) {
      throw new IllegalArgumentException("삭제 대상이 존재하지 않습니다.");
    }

    boolean isDir = before.isDirectory();

    if (isDir) {
      if (!recursive) {
        boolean hasChildren = hasChildren(path);
        if (hasChildren) {
          throw new IllegalArgumentException("폴더 하위 항목이 존재합니다. recursive=true로 요청해 주세요.");
        }
      }
      deleteDav(path);
      fileMngService.markFileDetailDeletedByPathPrefix(path, userId);
      fileMngService.deleteFolderMetaByPathPrefix(path, userId);
      return;
    }

    deleteDav(path);
    fileMngService.markFileDetailDeletedByExactPath(path, userId);
  }

  private void deleteDav(String davPath) throws Exception {

    String path = ErpDavPathUtil.normalizePathOrRoot(davPath);
    if (!path.startsWith("/")) {
      path = "/" + path;
    }

    String url = buildDavUrlFromDavPath(path);

    HttpRequestBase del = new HttpRequestBase() {
      @Override
      public String getMethod() {
        return "DELETE";
      }
    };

    del.setURI(URI.create(url));
    del.setHeader("Authorization", authHeader);

    HttpResponse res = null;
    try {
      res = http.execute(del);
      int code = res.getStatusLine().getStatusCode();

      if (code == 204 || code == 200 || code == 201) {
        EntityUtils.consumeQuietly(res.getEntity());
        return;
      }

      if (code == 404) {
        EntityUtils.consumeQuietly(res.getEntity());
        return;
      }

      String body = res.getEntity() != null ? EntityUtils.toString(res.getEntity(), "UTF-8") : "";
      throw new RuntimeException("DELETE fail: " + code + " url=" + url + " body=" + body);

    } finally {
      del.releaseConnection();
    }
  }

  private void validateDeletablePathOnlyApplicationFolder(String normalizedPath) {
    if (normalizedPath == null) {
      throw new IllegalArgumentException("삭제 경로가 비어있습니다.");
    }

    String p = ErpDavPathUtil.normalizePathOrRoot(normalizedPath);
    if (!p.startsWith("/")) {
      p = "/" + p;
    }

    // /ERP 전체 또는 상위 폴더 삭제 방지
    if ("/ERP".equals(p) || "/ERP/".equals(p)) {
      throw new IllegalArgumentException("ERP 루트는 삭제할 수 없습니다.");
    }

    // 경로 토큰 분해
    // 기대 형태: /ERP/YYYY/MM/SB26-G0000 또는 /ERP/YYYY/MM/SB26-G0000/하위...
    String[] parts = p.split("/");
    // split 결과는 첫 요소가 빈 문자열이 될 수 있음
    // 예: ["", "ERP", "2026", "02", "SB26-G0000", "00.공통폴더"]
    if (parts.length < 5) {
      throw new IllegalArgumentException("삭제는 신청서 폴더에서만 가능합니다.");
    }

    if (!"ERP".equals(parts[1])) {
      throw new IllegalArgumentException("삭제는 ERP 경로에서만 가능합니다.");
    }

    String yyyy = parts.length > 2 ? parts[2] : "";
    String mm = parts.length > 3 ? parts[3] : "";
    String appNo = parts.length > 4 ? parts[4] : "";

    // 연도 월 기본 검증
    if (!yyyy.matches("\\d{4}")) {
      throw new IllegalArgumentException("삭제는 신청서 경로에서만 가능합니다.");
    }
    if (!mm.matches("\\d{2}")) {
      throw new IllegalArgumentException("삭제는 신청서 경로에서만 가능합니다.");
    }

    // 신청서번호 패턴 제한
    // 예: SB26-G0000
    if (!appNo.matches("SB\\d{2}-[A-Z]\\d{4}")) {
      throw new IllegalArgumentException("삭제는 신청서 폴더에서만 가능합니다.");
    }

    // 상위 폴더 차단
    // /ERP/YYYY, /ERP/YYYY/MM 자체 삭제 방지
    if (parts.length == 4) {
      throw new IllegalArgumentException("월 폴더는 삭제할 수 없습니다.");
    }
    if (parts.length == 3) {
      throw new IllegalArgumentException("연도 폴더는 삭제할 수 없습니다.");
    }
  }


  private boolean hasChildren(String davPath) throws Exception {

    String path = ErpDavPathUtil.normalizePathOrRoot(davPath);
    if (!path.startsWith("/")) {
      path = "/" + path;
    }

    String url = buildDavUrlFromDavPath(path);

    HttpRequestBase propfind = new HttpEntityEnclosingRequestBase() {
      @Override
      public String getMethod() {
        return "PROPFIND";
      }
    };

    propfind.setURI(URI.create(url));
    propfind.setHeader("Authorization", authHeader);
    propfind.setHeader("Depth", "1");
    propfind.setHeader("Content-Type", "application/xml; charset=UTF-8");

    String body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<d:propfind xmlns:d=\"DAV:\">"
        + "  <d:prop>" + "    <d:resourcetype/>" + "  </d:prop>" + "</d:propfind>";

    ((HttpEntityEnclosingRequestBase) propfind).setEntity(new StringEntity(body, "UTF-8"));

    HttpResponse res = null;
    try {
      res = http.execute(propfind);
      int code = res.getStatusLine().getStatusCode();

      if (code == 404) {
        return false;
      }

      if (code != 207) {
        String respBody =
            res.getEntity() != null ? EntityUtils.toString(res.getEntity(), "UTF-8") : "";
        throw new RuntimeException("PROPFIND fail: " + code + " body=" + respBody);
      }

      String xml = EntityUtils.toString(res.getEntity(), "UTF-8");
      int responseCount = countDavResponses(xml);

      // Depth 1이면 자기 자신 1개는 항상 포함
      return responseCount > 1;

    } finally {
      propfind.releaseConnection();
    }
  }

  private int countDavResponses(String xml) throws Exception {

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);

    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(new InputSource(new StringReader(xml)));

    NodeList responses = doc.getElementsByTagNameNS("DAV:", "response");
    if (responses == null) {
      return 0;
    }
    return responses.getLength();
  }


  @Override
  @Transactional
  public String moveWithDbSync(String sourceDavPath, String destDavPath, boolean overwrite,
      String userId) throws Exception {

    String src = ErpDavPathUtil.normalizePathOrRoot(sourceDavPath);
    String dst = ErpDavPathUtil.normalizePathOrRoot(destDavPath);

    if (!src.startsWith("/")) {
      src = "/" + src;
    }
    if (!dst.startsWith("/")) {
      dst = "/" + dst;
    }

    WebDavItemDTO before = stat(src);
    if (before == null) {
      throw new IllegalArgumentException("원본이 존재하지 않습니다.");
    }

    boolean isDir = before.isDirectory();

    String moved = move(src, dst, overwrite, userId);

    if (isDir) {
      try {
        // 이동된 폴더 자신 + 하위 폴더 메타 + 하위 파일상세 경로를 모두 src->dst 로 갱신한다.
        // updateFolderMetaPathPrefix 는 LIKE 'src/%' (하위)만 갱신해 이동 폴더 자신이 누락되므로
        // rename 과 동일하게 updateFolderMetaByPathHash 를 사용한다.
        FolderMetaVO meta = new FolderMetaVO();
        meta.setFolderPath(dst);
        meta.setPathHash(DigestUtils.sha256Hex(dst));
        meta.setUploadSrc("A");
        meta.setCreatId(userId);
        fileMngService.updateFolderMetaByPathHash(src, meta);
      } catch (Exception e) {
        log.warn("move folder meta sync fail. src={} dst={}", src, dst, e);
      }
    } else {
      String newName = nameOfDavPath(dst);
      try {
        updateFileDetail(src, dst, newName, userId);
      } catch (Exception e) {
        log.warn("move file detail sync fail. src={} dst={}", src, dst, e);
      }
    }

    return moved;
  }

  @Override
  @Transactional
  public String copyWithDbSync(String sourceDavPath, String destDavPath, boolean overwrite,
      String userId, String metaMode) throws Exception {

    String src = ErpDavPathUtil.normalizePathOrRoot(sourceDavPath);
    String dst = ErpDavPathUtil.normalizePathOrRoot(destDavPath);

    if (!src.startsWith("/")) {
      src = "/" + src;
    }
    if (!dst.startsWith("/")) {
      dst = "/" + dst;
    }

    WebDavItemDTO before = stat(src);
    if (before == null) {
      throw new IllegalArgumentException("원본이 존재하지 않습니다.");
    }

    boolean isDir = before.isDirectory();

    // src와 dst가 같으면 자동으로 새 이름으로 목적지 생성
    if (sameDavPath(src, dst)) {
      String parent = parentOfDavPath(src);
      String baseName = nameOfDavPath(src);

      int i = 1;
      while (true) {
        String newName = buildCopyName(baseName, i);
        String cand = parent.endsWith("/") ? parent + newName : parent + "/" + newName;

        WebDavItemDTO exists = stat(cand);
        if (exists == null) {
          dst = cand;
          break;
        }
        i++;

        if (i > 999) {
          throw new IllegalArgumentException("복사 대상 이름을 자동 생성할 수 없습니다.");
        }
      }
    }

    String copied;
    if (isDir) {
      copied = copyDav(src, dst, overwrite, 1);
    } else {
      copied = copyDav(src, dst, overwrite, -1);
    }

    String mode = metaMode == null ? "TOP_ONLY" : metaMode.trim();
    if (mode.isEmpty()) {
      mode = "TOP_ONLY";
    }

    if (isDir) {
      if ("FULL".equalsIgnoreCase(mode)) {
        try {
          fileMngService.copyFolderMetaByPathPrefix(src, dst, userId);
        } catch (Exception e) {
          log.warn("copy folder meta full sync fail. src={} dst={}", src, dst, e);
        }
      } else {
        try {
          FolderMetaVO meta = new FolderMetaVO();
          meta.setFolderPath(dst);
          meta.setPathHash(DigestUtils.sha256Hex(dst));
          meta.setUploadSrc("A");
          meta.setCreatId(userId);
          fileMngService.insertFolderMeta(meta);
        } catch (Exception e) {
          log.warn("copy folder meta top sync fail. dst={}", dst, e);
        }
      }
    } else {
      // 파일 복사 시 FILE_DETAIL_TB 메타 저장
      try {
        fileMngService.copyFileDetailByPathPrefix(src, dst, userId);
      } catch (Exception e) {
        log.warn("copy file meta sync fail. src={} dst={}", src, dst, e);
      }
    }

    return copied;
  }

  private boolean sameDavPath(String a, String b) {
    String pa = ErpDavPathUtil.normalizePathOrRoot(a);
    String pb = ErpDavPathUtil.normalizePathOrRoot(b);
    if (!pa.startsWith("/"))
      pa = "/" + pa;
    if (!pb.startsWith("/"))
      pb = "/" + pb;
    return pa.equals(pb);
  }

  private String buildCopyName(String name, int idx) {
    if (name == null) {
      return "copy_" + idx;
    }
    int dot = name.lastIndexOf(".");
    if (dot > 0 && dot < name.length() - 1) {
      String base = name.substring(0, dot);
      String ext = name.substring(dot);
      return base + " (" + idx + ")" + ext;
    }
    return name + " (" + idx + ")";
  }

  private String copyDav(String sourceDavPath, String destDavPath, boolean overwrite, int depth)
      throws Exception {

    String src = ErpDavPathUtil.normalizePathOrRoot(sourceDavPath);
    String dst = ErpDavPathUtil.normalizePathOrRoot(destDavPath);

    if (!src.startsWith("/")) {
      src = "/" + src;
    }
    if (!dst.startsWith("/")) {
      dst = "/" + dst;
    }

    String srcUrl = buildDavUrlFromDavPath(src);
    String dstUrl = buildDavUrlFromDavPath(dst);

    HttpRequestBase copyReq = new HttpEntityEnclosingRequestBase() {
      @Override
      public String getMethod() {
        return "COPY";
      }
    };

    copyReq.setURI(URI.create(srcUrl));
    copyReq.setHeader("Authorization", authHeader);
    copyReq.setHeader("Destination", dstUrl);
    copyReq.setHeader("Overwrite", overwrite ? "T" : "F");

    if (depth >= 0) {
      copyReq.setHeader("Depth", String.valueOf(depth));
    }

    HttpResponse res = null;
    try {
      res = http.execute(copyReq);
      int code = res.getStatusLine().getStatusCode();

      if (code == 201 || code == 204) {
        EntityUtils.consumeQuietly(res.getEntity());
        return dst;
      }

      if (code == 412) {
        EntityUtils.consumeQuietly(res.getEntity());
        throw new NcBizException("대상 이름이 이미 존재합니다. 다른 이름을 사용해 주세요.");
      }

      if (code == 404) {
        EntityUtils.consumeQuietly(res.getEntity());
        throw new IllegalArgumentException("원본이 존재하지 않습니다.");
      }

      String body = res.getEntity() != null ? EntityUtils.toString(res.getEntity(), "UTF-8") : "";
      throw new RuntimeException(
          "COPY fail: " + code + " src=" + srcUrl + " dst=" + dstUrl + " body=" + body);

    } finally {
      copyReq.releaseConnection();
    }
  }

  /**
   * 전달된 dav 경로에서 부모 폴더 경로를 반환한다 예시 /ERP/2026/02/SB26-G0000/00.공통폴더/IMG.jpg ->
   * /ERP/2026/02/SB26-G0000/00.공통폴더 /ERP/2026/02/SB26-G0000 -> /ERP/2026/02 /ERP -> / 처리 순서 1. 경로를
   * 정규화하여 슬래시 형식을 통일한다 2. 항상 절대경로 형태로 맞춘다 3. 마지막 슬래시 기준으로 상위 경로를 계산한다
   */
  private String parentOfDavPath(String davPath) {
    if (davPath == null) {
      return null;
    }

    // 입력 경로를 표준 형식으로 정규화
    String p = ErpDavPathUtil.normalizePath(davPath);
    if (p.length() <= 1) {
      return "/";
    }

    // 마지막 슬래시 위치를 찾는다
    int idx = p.lastIndexOf("/");

    // 루트 수준이거나 더 이상 부모가 없으면 루트를 반환한다
    if (idx <= 0) {
      return "/";
    }

    // 마지막 슬래시 이전까지를 부모 경로로 반환한다
    return p.substring(0, idx);
  }

  /**
   * 전달된 dav 경로에서 파일명 또는 폴더명을 반환한다 예시 /ERP/2026/02/SB26-G0000/00.공통폴더/IMG.jpg -> IMG.jpg
   * /ERP/2026/02/SB26-G0000/00.공통폴더 -> 00.공통폴더 IMG.jpg -> IMG.jpg 처리 순서 1. 경로를 정규화하여 슬래시 형식을 통일한다
   * 2. 마지막 슬래시 이후의 문자열을 이름으로 추출한다
   */
  private String nameOfDavPath(String davPath) {
    if (davPath == null) {
      return null;
    }
    // 입력 경로를 표준 형식으로 정규화
    String p = ErpDavPathUtil.normalizePathOrRoot(davPath);

    if ("/".equals(p)) {
      return "";
    }

    // 마지막 슬래시 위치를 찾는다
    int idx = p.lastIndexOf("/");

    // 슬래시가 없으면 전체를 이름으로 간주한다
    if (idx < 0) {
      return p;
    }

    // 마지막 슬래시 이후 문자열을 이름으로 반환한다
    return p.substring(idx + 1);
  }

  @Override
  public boolean existsFile(String davPath) throws Exception {
    WebDavItemDTO item = stat(ErpDavPathUtil.normalizePath(davPath));
    return item != null && !item.isDirectory();
  }

  @Override
  public String uploadBytes(byte[] content, String davPath, String contentType,
      boolean overwrite) throws Exception {
    if (content == null || content.length == 0) {
      throw new IllegalArgumentException("업로드할 내용이 비어 있습니다.");
    }

    String normalized = ErpDavPathUtil.normalizePath(davPath);
    String relativePath = toRelativePathUnderRoot(normalized);
    URI uri = buildFileUri(relativePath);

    HttpPut put = new HttpPut(uri);
    put.setHeader("Authorization", authHeader);
    put.setHeader("Content-Type",
        contentType == null || contentType.trim().isEmpty() ? "application/octet-stream"
            : contentType);
    if (!overwrite) {
      put.setHeader("If-None-Match", "*");
    }

    try {
      put.setEntity(new ByteArrayEntity(content));
      HttpResponse res = http.execute(put);
      int code = res.getStatusLine().getStatusCode();

      if (!overwrite && code == 412) {
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


}
