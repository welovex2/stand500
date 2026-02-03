package egovframework.ncc.web;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import egovframework.cmm.service.LoginVO;
import egovframework.cmm.service.SbkInfoVO;
import egovframework.cmm.util.CountingOutputStream;
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.cmm.util.ErpDavPathUtil;
import egovframework.ncc.dto.DownloadLogVO;
import egovframework.ncc.dto.NcCreateFolderRequest;
import egovframework.ncc.dto.NcDeleteRequest;
import egovframework.ncc.dto.NcRenameRequest;
import egovframework.ncc.dto.NcSimpleResult;
import egovframework.ncc.dto.UploadResultDTO;
import egovframework.ncc.dto.WebDavItemDTO;
import egovframework.ncc.dto.WebDavListResponseDTO;
import egovframework.ncc.dto.WebDavNodeDTO;
import egovframework.ncc.service.DownloadLogService;
import egovframework.ncc.service.NextcloudDavService;
import egovframework.sbk.service.SbkService;
import io.swagger.annotations.Api;

@Api(tags = {"파일서버"})
@RestController
@RequestMapping("/nc")
public class NextcloudFolderController {

  @Resource(name = "NextcloudDavService")
  private NextcloudDavService nextcloudDavService;

  @Resource(name = "SbkService")
  private SbkService sbkService;

  @Resource
  private ServletContext servletContext;

  @Resource(name = "DownloadLogService")
  private DownloadLogService downloadLogService;

  /** 4) 선택 폴더 안의 폴더+파일 목록(Depth=1) */
  @GetMapping("/list")
  public Map<String, Object> list(
      @RequestParam(name = "path", required = false, defaultValue = "") String path)
      throws Exception {

    String base = ErpDavPathUtil.normalizePathOrRoot(path);

    WebDavListResponseDTO raw = nextcloudDavService.list(base, 1);

    // PROPFIND Depth=1은 보통 첫 항목이 "자기 자신"이라 제거
    List<WebDavItemDTO> items = raw.getItems() == null ? new ArrayList<WebDavItemDTO>()
        : raw.getItems().stream().filter(it -> it != null && it.getDavPath() != null)
            .filter(it -> !ErpDavPathUtil.normalizePath(it.getDavPath()).equals(raw.getDavPath()))
            .collect(Collectors.toList());

    // 폴더 먼저, 파일 나중(정렬)
    items.sort((a, b) -> {
      if (a.isDirectory() != b.isDirectory())
        return a.isDirectory() ? -1 : 1;
      String an = a.getName() == null ? "" : a.getName();
      String bn = b.getName() == null ? "" : b.getName();
      return an.compareToIgnoreCase(bn);
    });

    Map<String, Object> res = new LinkedHashMap<String, Object>();
    res.put("path", raw.getDavPath());
    res.put("items", items);
    return res;
  }

  /** 1) 좌측 폴더 트리 (폴더만 권장) */
  @GetMapping("/tree")
  public Map<String, Object> tree(
      @RequestParam(name = "path", required = false, defaultValue = "") String path,
      @RequestParam(name = "depth", required = false, defaultValue = "3") int depth,
      @RequestParam(name = "onlyDir", required = false, defaultValue = "true") boolean onlyDir)
      throws Exception {

    String base = ErpDavPathUtil.normalizePathOrRoot(path);

    WebDavListResponseDTO raw = nextcloudDavService.list(base, depth);

    WebDavNodeDTO tree = buildTree(raw.getDavPath(), raw.getItems(), onlyDir);

    Map<String, Object> res = new LinkedHashMap<String, Object>();
    res.put("path", raw.getDavPath());
    res.put("depth", depth);
    res.put("onlyDir", onlyDir);
    res.put("tree", tree);
    return res;
  }

  /** 2) 새폴더 만들기 */
  @PostMapping(value = "/folder", consumes = MediaType.APPLICATION_JSON_VALUE)
  public NcSimpleResult createFolder(@RequestBody NcCreateFolderRequest req) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    String parent = ErpDavPathUtil.normalizePathOrRoot(req.getParentPath());
    String name = req.getName() == null ? "" : req.getName().trim();

    // 1) 폴더명 강력 검증
    String nameErr = validateFolderNameStrong(name);
    if (nameErr != null)
      return NcSimpleResult.fail(nameErr);

    // 2) 부모 경로 강력 검증 + 신청서 폴더 이하(하위/하위하위 포함)만 허용
    String parentErr = validateParentPathStrong(parent);
    if (parentErr != null)
      return NcSimpleResult.fail(parentErr);

    try {
      String created = nextcloudDavService.createFolder(parent, name, user.getId());
      return NcSimpleResult.ok(created);
    } catch (RuntimeException e) {
      return NcSimpleResult.fail(e.getMessage());
    }

  }



  /** 5) 파일 업로드 (완료 신호는 JSON 응답으로) */
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public UploadResultDTO upload(@RequestParam("path") String folderPath,
      @RequestPart("file") MultipartFile file) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    String reqPath = ErpDavPathUtil.normalizePathOrRoot(folderPath);

    // 1) 신청서번호 추출
    String sbkNo = ErpDavPathUtil.extractSbkNo(reqPath);

    // 2) 신청서 조회 (SBK_TB)
    SbkInfoVO sbk = sbkService.findBySbkNoAndProvision(sbkNo);
    if (sbk == null)
      return UploadResultDTO.fail("신청서를 찾을 수 없습니다: " + sbkNo);

    String baseFolder = ErpDavPathUtil.normalizePath(sbk.getNcFolderPath()); // /ERP/2025/12/SB25-G1583
    String atchFileId = sbk.getAtchFileId();

    // 3) 요청 path가 baseFolder 하위인지 검증 (핵심)
    validateUnderBase(baseFolder, reqPath);

    // 4) Nextcloud 업로드
    UploadResultDTO up = nextcloudDavService.uploadToFolder(reqPath, file);
    if (!up.isOk())
      return up;

    // 5) 메타 저장 (FILE_DETAIL_TB)
    // creatId는 ERP 로그인 사용자
    String creatId = user.getId();

    nextcloudDavService.insertFileDetail(atchFileId, reqPath, // FILE_STRE_COURS 후보 (혹은 상대경로)
        up.getDavPath(), // 실제 저장 경로/파일명
        file.getOriginalFilename(), file.getSize(), creatId);

    // 필요하면 FILE_TB에 ATCH_FILE_ID가 없을 때 생성/USE_AT 갱신 등도 여기서 처리

    return up;

  }

  private String parentOf(String path) {
    String p = ErpDavPathUtil.normalizePath(path);
    int idx = p.lastIndexOf("/");
    if (idx <= 0)
      return "/ERP";
    return p.substring(0, idx);
  }

  private void sortTree(WebDavNodeDTO node) {
    if (node.getChildren() == null)
      return;

    node.getChildren().sort((a, b) -> {
      if (a.isDirectory() != b.isDirectory())
        return a.isDirectory() ? -1 : 1;
      String an = a.getName() == null ? "" : a.getName();
      String bn = b.getName() == null ? "" : b.getName();
      return an.compareToIgnoreCase(bn);
    });

    for (WebDavNodeDTO c : node.getChildren()) {
      sortTree(c);
    }
  }

  private static final String ROOT = "/ERP"; // 설정값으로 빼는 걸 추천



  private String ensureRootPrefix(String p) {
    String x = ErpDavPathUtil.normalizePath(p);
    // DB에 /ERP 없이 /2025/12/... 처럼 저장된 경우 보정
    if (!x.startsWith(ROOT + "/") && !x.equals(ROOT)) {
      x = ErpDavPathUtil.normalizePath(ROOT + x);
    }
    return x;
  }

  @PostMapping(value = "/rename", consumes = MediaType.APPLICATION_JSON_VALUE)
  public NcSimpleResult rename(@RequestBody NcRenameRequest req) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    String path = ErpDavPathUtil.normalizePathOrRoot(req.getPath());
    String newName = req.getNewName() == null ? "" : req.getNewName().trim();

    // 이름 검증은 폴더 생성 검증과 거의 동일하게 강하게 적용
    String nameErr = validateFolderNameStrong(newName);
    if (nameErr != null) {
      return NcSimpleResult.fail(nameErr);
    }

    // 경로 검증은 createFolder와 동일한 정책 적용
    String parent = parentOf(path);
    String parentErr = validateParentPathStrong(parent);
    if (parentErr != null) {
      return NcSimpleResult.fail(parentErr);
    }

    // 원본 path 자체도 허용 범위인지 한번 더 확인
    String srcErr = validateParentPathStrong(path);
    if (srcErr != null) {
      return NcSimpleResult.fail("허용되지 않은 경로입니다.");
    }

    String changed = nextcloudDavService.rename(path, newName, req.isOverwrite(), user.getId());
    return NcSimpleResult.ok(changed);
  }

  @PostMapping(value = "/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
  public NcSimpleResult delete(@RequestBody NcDeleteRequest req) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    String path = ErpDavPathUtil.normalizePathOrRoot(req.getPath());

    boolean recursive = req.isRecursive();

    nextcloudDavService.deleteWithDbSync(path, recursive, user.getId());

    return NcSimpleResult.ok("OK");
  }

  // -----------------------------
  // 다운로드/미리보기/폴더 ZIP
  // -----------------------------

  @GetMapping("/download")
  public void downloadFile(@RequestParam("path") String path, HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    String davPath = ErpDavPathUtil.normalizePathOrRoot(path);

    String pathErr = validateDownloadPathStrong(davPath);
    if (pathErr != null) {
      response.setStatus(400);
      response.setContentType("application/json;charset=UTF-8");
      response.getWriter().write("{\"ok\":false,\"message\":\"" + escapeJson(pathErr) + "\"}");
      return;
    }

    String fileName = extractNameFromPath(davPath);
    String contentType = detectContentType(fileName);

    Long logId = startDownloadLog("download", davPath, false, fileName, null, contentType, request);
    long bytesSent = 0L;

    response.setStatus(200);
    response.setContentType(contentType);
    response.setHeader("Content-Disposition",
        buildContentDispositionByUa("attachment", fileName, request));

    InputStream in = null;
    BufferedInputStream bin = null;
    BufferedOutputStream bout = null;
    try {

      bytesSent = streamFileToResponse(davPath, response);
      markDownloadLogSuccess(logId, bytesSent);

    } catch (Exception e) {
      markDownloadLogFail(logId, e, bytesSent);
      throw e;
    } finally {
      safeClose(bout);
      safeClose(bin);
      safeClose(in);
    }
  }

  @GetMapping("/preview")
  public void previewFile(@RequestParam("path") String path, HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    String davPath = ErpDavPathUtil.normalizePathOrRoot(path);

    String pathErr = validateDownloadPathStrong(davPath);
    if (pathErr != null) {
      response.setStatus(400);
      response.setContentType("application/json;charset=UTF-8");
      response.getWriter().write("{\"ok\":false,\"message\":\"" + escapeJson(pathErr) + "\"}");
      return;
    }

    String fileName = extractNameFromPath(davPath);
    String contentType = detectContentType(fileName);

    Long logId = startDownloadLog("preview", davPath, false, fileName, null, contentType, request);

    long bytesSent = 0L;

    response.setStatus(200);
    response.setContentType(contentType);

    // inline 미리보기
    response.setHeader("Content-Disposition",
        buildContentDispositionByUa("inline", fileName, request));

    InputStream in = null;
    BufferedInputStream bin = null;
    BufferedOutputStream bout = null;
    try {
      bytesSent = streamFileToResponse(davPath, response);
      markDownloadLogSuccess(logId, bytesSent);
    } catch (Exception e) {
      markDownloadLogFail(logId, e, bytesSent);
      throw e;
    } finally {
      safeClose(bout);
      safeClose(bin);
      safeClose(in);
    }
  }

  @GetMapping("/download-folder")
  public void downloadFolderAsZip(@RequestParam("path") String path, HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    String folderPath = ErpDavPathUtil.normalizePathOrRoot(path);

    String pathErr = validateDownloadPathStrong(folderPath);
    if (pathErr != null) {
      response.setStatus(400);
      response.setContentType("application/json;charset=UTF-8");
      response.getWriter().write("{\"ok\":false,\"message\":\"" + escapeJson(pathErr) + "\"}");
      return;
    }

    String zipName = buildZipDownloadName(folderPath);

    Long logId = startDownloadLog("download_folder", folderPath, true, null, zipName,
        "application/zip", request);

    response.setStatus(200);
    response.setContentType("application/zip");
    response.setHeader("Content-Disposition",
        buildContentDispositionByUa("attachment", zipName, request));

    ZipOutputStream zos = null;
    BufferedOutputStream bout = null;
    CountingOutputStream cos = null; // 바이트 추적용

    try {
      // 순서: Response -> Counting -> Buffered -> Zip
      cos = new CountingOutputStream(response.getOutputStream());
      bout = new BufferedOutputStream(cos);
      zos = new ZipOutputStream(bout, StandardCharsets.UTF_8);

      zipFolderRecursive(folderPath, folderPath, zos);

      zos.finish();
      zos.flush();
      bout.flush(); // 중요: 모든 데이터를 cos로 밀어넣어야 함

      // 최종적으로 전송된 바이트 수 획득
      long bytesSent = cos.getBytesWritten();
      markDownloadLogSuccess(logId, bytesSent);

    } catch (Exception e) {
      long bytesSentSoFar = (cos != null) ? cos.getBytesWritten() : 0L;
      markDownloadLogFail(logId, e, bytesSentSoFar);
      throw e;
    } finally {
      safeClose(zos);
      safeClose(bout);
      safeClose(cos);
    }
  }

  private long streamFileToResponse(String davPath, HttpServletResponse response) throws Exception {
    InputStream in = null;
    BufferedInputStream bin = null;
    CountingOutputStream cos = null;
    BufferedOutputStream bout = null;

    try {
      in = nextcloudDavService.downloadStreamByDavPath(davPath);
      bin = new BufferedInputStream(in);

      cos = new CountingOutputStream(response.getOutputStream());
      bout = new BufferedOutputStream(cos);

      byte[] buf = new byte[8192];
      int n;
      while ((n = bin.read(buf)) >= 0) {
        bout.write(buf, 0, n);
      }
      bout.flush();

      return cos.getBytesWritten();
    } finally {
      safeClose(bout);
      safeClose(cos);
      safeClose(bin);
      safeClose(in);
    }
  }


  private String buildZipDownloadName(String folderPath) {
    String base = extractNameFromPath(folderPath);
    if (base == null || base.trim().isEmpty() || base.equals("/")) {
      base = "folder";
    }
    // 점(.)이나 공백으로 끝나는 경우 윈도우 호환성을 위해 제거하되, 원본 명칭 유지
    base = trimTrailingDotOrSpace(base);
    return base + ".zip";
  }

  private String trimTrailingDotOrSpace(String s) {
    if (s == null)
      return null;
    String v = s;
    while (v.endsWith(".") || v.endsWith(" ")) {
      v = v.substring(0, v.length() - 1);
    }
    return v.isEmpty() ? "folder" : v;
  }

  private void zipFolderRecursive(String rootFolder, String currentFolder, ZipOutputStream zos)
      throws Exception {

    WebDavListResponseDTO raw = nextcloudDavService.list(currentFolder, 1);
    List<WebDavItemDTO> items = raw.getItems() == null ? new ArrayList<WebDavItemDTO>()
        : raw.getItems().stream().filter(it -> it != null && it.getDavPath() != null)
            .filter(it -> !ErpDavPathUtil.normalizePath(it.getDavPath()).equals(raw.getDavPath()))
            .collect(Collectors.toList());

    // 1. 현재 폴더가 루트가 아니라면, '폴더 엔트리' 자체를 먼저 추가 (비어있는 폴더 포함의 핵심)
    if (!rootFolder.equals(currentFolder)) {
      String folderRelPath = buildZipRelativePath(rootFolder, currentFolder);
      if (!folderRelPath.endsWith("/")) {
        folderRelPath += "/"; // 폴더임을 명시하기 위해 끝에 / 추가
      }
      if (folderRelPath.startsWith("/")) {
        folderRelPath = folderRelPath.substring(1);
      }

      ZipEntry folderEntry = new ZipEntry(folderRelPath);
      zos.putNextEntry(folderEntry);
      zos.closeEntry();
    }

    // 2. 하위 항목 순회
    for (WebDavItemDTO it : items) {
      if (it.isDirectory()) {
        // 폴더면 재귀 호출
        String dirPath = ErpDavPathUtil.normalizePath(it.getDavPath());
        zipFolderRecursive(rootFolder, dirPath, zos);
      } else {
        // 파일이면 기존 로직대로 추가
        String fileDavPath = ErpDavPathUtil.normalizePath(it.getDavPath());
        String rel = buildZipRelativePath(rootFolder, fileDavPath);

        if (rel.startsWith("/"))
          rel = rel.substring(1);

        ZipEntry entry = new ZipEntry(rel);
        zos.putNextEntry(entry);

        InputStream in = null;
        BufferedInputStream bin = null;
        try {
          in = nextcloudDavService.downloadStreamByDavPath(fileDavPath);
          bin = new BufferedInputStream(in);

          byte[] buf = new byte[8192];
          int n;
          while ((n = bin.read(buf)) >= 0) {
            zos.write(buf, 0, n);
          }
        } finally {
          safeClose(bin);
          safeClose(in);
          zos.closeEntry();
        }
      }
    }
  }

  private String buildZipRelativePath(String rootFolder, String fileDavPath) {
    String root = ErpDavPathUtil.normalizePath(rootFolder);
    String file = ErpDavPathUtil.normalizePath(fileDavPath);

    if (!root.endsWith("/"))
      root = root + "/";

    if (file.startsWith(root)) {
      return file.substring(root.length());
    }
    return extractNameFromPath(file);
  }

  // -----------------------------
  // 기존 helper + 검증 로직
  // -----------------------------

  private WebDavNodeDTO buildTree(String base, List<WebDavItemDTO> raw, boolean onlyDir) {
    // base root node
    WebDavNodeDTO root = new WebDavNodeDTO();
    root.setDavPath(base);
    root.setName(base.substring(base.lastIndexOf("/") + 1));
    root.setDirectory(true);
    root.setCanWrite(true);

    Map<String, WebDavNodeDTO> map = new LinkedHashMap<String, WebDavNodeDTO>();
    map.put(base, root);

    if (raw != null) {
      for (WebDavItemDTO it : raw) {
        if (it == null || it.getDavPath() == null)
          continue;

        String p = ErpDavPathUtil.normalizePath(it.getDavPath());
        if (p.equals(base)) {
          // 자기 자신(메타 갱신)
          root.setName(it.getName());
          root.setDirectory(it.isDirectory());
          root.setCanWrite(it.isCanWrite());
          root.setLastModified(it.getLastModified());
          root.setSize(it.getSize());
          continue;
        }

        if (onlyDir && !it.isDirectory())
          continue;

        WebDavNodeDTO node = new WebDavNodeDTO();
        node.setDavPath(p);
        node.setName(it.getName());
        node.setDirectory(it.isDirectory());
        node.setSize(it.getSize());
        node.setLastModified(it.getLastModified());
        node.setCanWrite(it.isCanWrite());

        map.put(p, node);
      }
    }

    // parent-child link
    for (WebDavNodeDTO node : map.values()) {
      if (node == root)
        continue;

      String parent = parentOf(node.getDavPath());
      WebDavNodeDTO parentNode = map.get(parent);
      if (parentNode == null)
        parentNode = root; // 안전

      parentNode.getChildren().add(node);
    }

    // sort folders first
    sortTree(root);
    return root;
  }

  private void validateUnderBase(String baseFromDb, String requestPath) {
    String base = ErpDavPathUtil.normalizePath(ensureRootPrefix(baseFromDb));
    String req = ErpDavPathUtil.normalizePath(requestPath);

    String basePrefix = base.endsWith("/") ? base : base + "/";

    if (req.equals(base) || req.startsWith(basePrefix))
      return;

    throw new IllegalArgumentException(
        "요청 경로가 신청서 기본 폴더 하위가 아닙니다. base=" + base + ", request=" + req);
  }

  // ===== 강화 검증 로직 =====

  private static final java.util.regex.Pattern BASE_APP_PATH = java.util.regex.Pattern
      .compile("^/ERP/(\\d{4})/(0[1-9]|1[0-2])/(SB\\d{2}-[GM]\\d{4})(?:/.*)?$");

  private String validateParentPathStrong(String parent) {
    if (parent == null)
      return "부모 경로가 비어있습니다.";

    String p = parent.trim();
    if (p.isEmpty())
      return "부모 경로가 비어있습니다.";
    if (p.length() > 1 && p.endsWith("/"))
      p = p.substring(0, p.length() - 1);

    // 역슬래시 차단 (윈도우 경로 우회 방지)
    if (p.indexOf('\\') >= 0)
      return "부모 경로에 '\\'는 사용할 수 없습니다.";

    // 인코딩 우회 보수 차단 (%2f, %5c 등)
    String lower = p.toLowerCase();
    if (lower.contains("%2f") || lower.contains("%5c")) {
      return "부모 경로에 인코딩된 경로 구분자는 사용할 수 없습니다.";
    }

    // 경로 세그먼트 검사: '.' '..' 금지 + 빈 세그먼트 방지
    String segErr = validatePathSegmentsNoTraversal(p);
    if (segErr != null)
      return segErr;

    // 신청서 폴더 이하(하위/하위하위 포함)만 허용
    java.util.regex.Matcher m = BASE_APP_PATH.matcher(p);
    if (!m.matches()) {
      return "허용되지 않은 경로입니다. /ERP/년도/월/신청서번호 아래에서만 폴더를 생성할 수 있습니다.";
    }

    return null;
  }

  private String validateFolderNameStrong(String name) {
    if (name == null || name.trim().isEmpty())
      return "폴더명이 비어있습니다.";

    String n = name.trim();

    // 슬래시 금지(기존)
    if (n.contains("/"))
      return "폴더명에 '/'는 사용할 수 없습니다.";
    // 역슬래시 금지
    if (n.contains("\\"))
      return "폴더명에 '\\'는 사용할 수 없습니다.";

    // 경로탈출 이름 금지
    if (".".equals(n) || "..".equals(n))
      return "폴더명으로 '.' 또는 '..'는 사용할 수 없습니다.";

    // 제어문자 차단
    for (int i = 0; i < n.length(); i++) {
      if (Character.isISOControl(n.charAt(i)))
        return "폴더명에 제어문자는 사용할 수 없습니다.";
    }

    // Windows/범용 예약문자 보수 차단 (환경 혼합 시 사고 예방)
    // 필요 없으면 줄여도 됨
    if (n.matches(".*[<>:\"|?*].*"))
      return "폴더명에 사용할 수 없는 문자가 포함되어 있습니다. (< > : \" | ? *)";

    // 뒤/앞 공백, 점(.)으로 끝나는 이름은 파일시스템 호환성 이슈가 많아 차단 권장
    if (!n.equals(name))
      return "폴더명 앞뒤 공백은 허용되지 않습니다.";
    if (n.endsWith("."))
      return "폴더명은 '.'로 끝날 수 없습니다.";

    // 길이 제한(보수적으로)
    if (n.length() > 80)
      return "폴더명은 최대 80자까지 가능합니다.";

    return null;
  }

  private String validatePathSegmentsNoTraversal(String path) {
    // "/a//b" 같은 중복 슬래시를 normalize가 처리해도, 보수적으로 검사
    String[] parts = path.split("/");
    for (String part : parts) {
      if (part.isEmpty())
        continue; // leading '/'
      if (".".equals(part) || "..".equals(part)) {
        return "부모 경로에 '.' 또는 '..' 세그먼트는 사용할 수 없습니다.";
      }
      // 세그먼트에 제어문자 차단
      for (int i = 0; i < part.length(); i++) {
        if (Character.isISOControl(part.charAt(i))) {
          return "부모 경로에 제어문자는 사용할 수 없습니다.";
        }
      }
    }
    return null;
  }

  // 다운로드/미리보기에서도 동일 기준으로 제한
  private String validateDownloadPathStrong(String path) {
    if (path == null)
      return "경로가 비어있습니다.";

    String p = path.trim();
    if (p.isEmpty())
      return "경로가 비어있습니다.";

    if (p.indexOf('\\') >= 0)
      return "경로에 '\\'는 사용할 수 없습니다.";

    String lower = p.toLowerCase();
    if (lower.contains("%2f") || lower.contains("%5c")) {
      return "경로에 인코딩된 경로 구분자는 사용할 수 없습니다.";
    }

    String segErr = validatePathSegmentsNoTraversal(p);
    if (segErr != null)
      return segErr;

    java.util.regex.Matcher m = BASE_APP_PATH.matcher(p);
    if (!m.matches()) {
      return "허용되지 않은 경로입니다. /ERP/년도/월/신청서번호 아래에서만 허용됩니다.";
    }
    return null;
  }

  private String extractNameFromPath(String davPath) {
    if (davPath == null)
      return "file";
    String p = davPath;
    if (p.endsWith("/"))
      p = p.substring(0, p.length() - 1);
    int idx = p.lastIndexOf("/");
    if (idx < 0)
      return p;
    String name = p.substring(idx + 1);
    return name == null || name.trim().isEmpty() ? "file" : name;
  }

  private String detectContentType(String fileName) {
    String ct = null;
    if (servletContext != null && fileName != null) {
      ct = servletContext.getMimeType(fileName);
    }
    if (ct == null || ct.trim().isEmpty()) {
      // 기본값
      ct = "application/octet-stream";
    }
    return ct;
  }

  private String buildContentDispositionByUa(String type, String originalFileName,
      HttpServletRequest request) {
    String ua = request == null ? "" : nvl(request.getHeader("User-Agent"));
    String original =
        (originalFileName == null || originalFileName.isEmpty()) ? "file" : originalFileName;

    // 1. RFC 5987 인코딩 (한글 깨짐 방지의 핵심)
    String encodedName = urlEncodeUtf8(original);

    // 2. 레거시 브라우저 (IE, 구형 Edge) 대응
    if (isLegacyIeFamily(ua)) {
      return type + "; filename=\"" + encodedName + "\"";
    }

    // 3. 최신 브라우저 (Chrome, Safari, Firefox 등) 대응
    // filename은 ASCII만 지원하므로 비워두거나 간단한 대체값 사용, filename*에 실제 한글 이름 저장
    return type + "; filename=\"" + "download.zip" + "\"; filename*=UTF-8''" + encodedName;
  }

  private boolean isLegacyIeFamily(String ua) {
    String u = ua.toLowerCase();

    // MSIE, Trident 는 IE
    if (u.contains("msie"))
      return true;
    if (u.contains("trident"))
      return true;

    // EdgeHTML 기반 구형 Edge 는 Edge/ 포함, 크로미움 Edge 는 Edg/ 포함
    // Edge/ 는 레거시 취급
    if (u.contains("edge/"))
      return true;

    return false;
  }

  private String urlEncodeUtf8(String s) {
    try {
      return URLEncoder.encode(s, "UTF-8").replace("+", "%20");
    } catch (Exception e) {
      return s;
    }
  }

  private String rfc5987Encode(String s) {
    // URLEncoder 는 RFC 5987 완전 구현이 아니라서 보정
    // 공백은 %20로 맞추고, 일부 문자는 그대로 두는 보정이 필요할 수 있음
    String v = urlEncodeUtf8(s);

    // 추가 보정이 필요하면 여기에서 처리
    // return v.replace("%7E", "~");
    return v;
  }

  private String toAsciiFallbackFileName(String original) {
    String s = original == null ? "file" : original;

    s = s.replace("\"", "");

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);

      boolean ok = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
          || c == '.' || c == '_' || c == '-' || c == ' ';

      sb.append(ok ? c : '_');
    }

    String out = sb.toString().trim();
    out = trimTrailingDotOrSpace(out);

    if (out.isEmpty())
      out = "file";
    return out;
  }

  private String nvl(String s) {
    return s == null ? "" : s;
  }

  private void safeClose(java.io.Closeable c) {
    try {
      if (c != null)
        c.close();
    } catch (Exception e) {
      // ignore
    }
  }

  private String escapeJson(String s) {
    if (s == null)
      return "";
    return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
  }

  private String resolveClientIp(javax.servlet.http.HttpServletRequest request) {
    if (request == null)
      return null;

    String xff = request.getHeader("X-Forwarded-For");
    if (xff != null && xff.trim().length() > 0) {
      String[] parts = xff.split(",");
      if (parts.length > 0)
        return parts[0].trim();
      return xff.trim();
    }

    String xrip = request.getHeader("X-Real-IP");
    if (xrip != null && xrip.trim().length() > 0)
      return xrip.trim();

    return request.getRemoteAddr();
  }

  private DownloadLogVO buildDownloadLog(String mode, String davPath, boolean isFolder,
      String fileName, String zipName, String contentType,
      javax.servlet.http.HttpServletRequest request) {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    DownloadLogVO log = new DownloadLogVO();
    log.setUserId(user == null ? null : user.getId());
    log.setSbkNo(ErpDavPathUtil.extractSbkNo(davPath));
    log.setDavPath(davPath);
    log.setIsFolder(isFolder ? "Y" : "N");
    log.setMode(mode);
    log.setFileName(fileName);
    log.setZipName(zipName);
    log.setContentType(contentType);
    log.setClientIp(resolveClientIp(request));
    log.setUserAgent(request == null ? null : request.getHeader("User-Agent"));
    log.setResultCd("START");
    log.setErrMsg(null);
    log.setBytesSent(0L);
    return log;
  }

  private Long startDownloadLog(String mode, String davPath, boolean isFolder, String fileName,
      String zipName, String contentType, javax.servlet.http.HttpServletRequest request) {

    DownloadLogVO log =
        buildDownloadLog(mode, davPath, isFolder, fileName, zipName, contentType, request);

    return downloadLogService.startLog(log);
  }

  private void markDownloadLogSuccess(Long logId, long bytesSent) {
    downloadLogService.success(logId, bytesSent);
  }

  private void markDownloadLogFail(Long logId, Exception e, long bytesSent) {
    String msg = e == null ? null : e.getMessage();
    downloadLogService.fail(logId, msg, bytesSent);
  }


}
