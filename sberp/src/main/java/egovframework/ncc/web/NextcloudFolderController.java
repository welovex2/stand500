package egovframework.ncc.web;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.text.Collator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import egovframework.cmm.service.LoginVO;
import egovframework.cmm.service.ResponseMessage;
import egovframework.cmm.service.SbkInfoVO;
import egovframework.cmm.util.CountingOutputStream;
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.cmm.util.ErpDavPathUtil;
import egovframework.cmm.util.FilePreviewPolicy;
import egovframework.cmm.util.OnlyOfficeEditorConfig;
import egovframework.cmm.util.OnlyOfficeEditorHtml;
import egovframework.cmm.util.OnlyOfficeIntegration;
import egovframework.cmm.util.OnlyOfficePreviewSupport;
import egovframework.ncc.dto.FileOpLogVO;
import egovframework.ncc.dto.OnlyOfficeOpLogRequest;
import egovframework.ncc.dto.NcCopyRequest;
import egovframework.ncc.dto.NcCreateFolderRequest;
import egovframework.ncc.dto.NcDeleteRequest;
import egovframework.ncc.dto.NcImagesToPdfRequest;
import egovframework.ncc.dto.NcImagesToPdfResult;
import egovframework.ncc.dto.NcMoveRequest;
import egovframework.ncc.dto.NcRenameRequest;
import egovframework.ncc.dto.NcSimpleResult;
import egovframework.ncc.dto.UploadResultDTO;
import egovframework.ncc.dto.WebDavItemDTO;
import egovframework.ncc.dto.WebDavListResponseDTO;
import egovframework.ncc.dto.WebDavNodeDTO;
import egovframework.ncc.service.FileOpLogService;
import egovframework.ncc.service.NcBrowsePathResolver;
import egovframework.ncc.service.NcImagePdfException;
import egovframework.ncc.service.NcImagePdfService;
import egovframework.ncc.service.NextcloudDavService;
import egovframework.ncc.service.NextcloudOnlyofficeConnectorService;
import egovframework.rte.fdl.property.EgovPropertyService;
import egovframework.sbk.service.SbkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags = {"파일서버"})
@RestController
@RequestMapping("/nc")
public class NextcloudFolderController {

  @Resource(name = "NextcloudDavService")
  private NextcloudDavService nextcloudDavService;

  @Resource(name = "NextcloudOnlyofficeConnectorService")
  private NextcloudOnlyofficeConnectorService nextcloudOnlyofficeConnectorService;

  @Resource(name = "SbkService")
  private SbkService sbkService;

  @Resource
  private ServletContext servletContext;

  @Resource(name = "FileOpLogService")
  private FileOpLogService fileOpLogService;

  @Resource(name = "NcBrowsePathResolver")
  private NcBrowsePathResolver ncBrowsePathResolver;

  @Resource(name = "NcImagePdfService")
  private NcImagePdfService ncImagePdfService;

  @Resource(name = "propertiesService")
  private EgovPropertyService propertyService;

  /** 4) 선택 폴더 안의 폴더+파일 목록(Depth=1) */
  @ApiOperation(value = "폴더 목록 조회",
      notes = "path 폴더 경로 예시 /ERP/2026/02/SB26-G0000\n" + "path 비우면 루트 기준으로 조회\n"
          + "sort 정렬 기준: name(기본, 숫자 자연 정렬) | date | size\n" + "order 정렬 방향: asc(기본) | desc\n")
  @GetMapping("/list")
  public Map<String, Object> list(
      @RequestParam(name = "path", required = false, defaultValue = "") String path,
      @RequestParam(name = "sort", required = false, defaultValue = "name") String sort,
      @RequestParam(name = "order", required = false, defaultValue = "asc") String order)
      throws Exception {

    String base = ncBrowsePathResolver.resolveBrowsePath(path);
    String sortKey = normalizeListSort(sort);
    String orderKey = normalizeListOrder(order);

    WebDavListResponseDTO raw = nextcloudDavService.list(base, 1);

    // PROPFIND Depth=1은 보통 첫 항목이 "자기 자신"이라 제거
    List<WebDavItemDTO> items = raw.getItems() == null ? new ArrayList<WebDavItemDTO>()
        : raw.getItems().stream().filter(it -> it != null && it.getDavPath() != null)
            .filter(it -> !ErpDavPathUtil.normalizePath(it.getDavPath()).equals(raw.getDavPath()))
            .collect(Collectors.toList());

    sortWebDavItems(items, sortKey, orderKey);

    Map<String, Object> res = new LinkedHashMap<String, Object>();
    res.put("path", raw.getDavPath());
    res.put("sort", sortKey);
    res.put("order", orderKey);
    res.put("items", items);
    return res;
  }

  /** 1) 좌측 폴더 트리 (폴더만 권장) */
  @GetMapping("/tree")
  public Map<String, Object> tree(
      @RequestParam(name = "path", required = false, defaultValue = "") String path,
      @ApiParam(value = "조회 깊이", example = "3") @RequestParam(name = "depth", required = false,
          defaultValue = "3") int depth,
      @RequestParam(name = "onlyDir", required = false, defaultValue = "true") boolean onlyDir)
      throws Exception {

    String base = ncBrowsePathResolver.resolveBrowsePath(path);

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
  @ApiOperation(value = "폴더 생성",
      notes = "davPath 생성할 부모 경로 예시 /ERP/2026/02/SB26-G0000\n" + "folderName 생성할 폴더명 예시 00.공통폴더\n")
  @PostMapping(value = "/folder", consumes = MediaType.APPLICATION_JSON_VALUE)
  public NcSimpleResult createFolder(@RequestBody NcCreateFolderRequest req,
      HttpServletRequest request) throws Exception {

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

    Long logId =
        startFileOpLog("MKDIR", parent + "/" + name, null, null, true, null, null, 0L, request);
    try {
      String created = nextcloudDavService.createFolder(parent, name, user.getId());
      markFileOpLogSuccess(logId, null, null);
      return NcSimpleResult.ok(created);
    } catch (RuntimeException e) {
      markFileOpLogFail(logId, e, null, null);
      return NcSimpleResult.fail(e.getMessage());
    }

  }

  /**
   * 5) 파일 업로드 (완료 신호는 JSON 응답으로)
   *
   * <p>단일 파일·다중 파일·폴더 드롭을 모두 이 API 하나로 처리합니다.
   * <ul>
   *   <li>{@code path} — 화면에서 사용자가 보고 있는 현재 폴더 DAV 경로</li>
   *   <li>{@code file} — multipart 파일 1개 (폴더 드롭 시 프론트가 파일마다 1회씩 호출)</li>
   *   <li>{@code relativePath} — (선택) base(path) 기준 상대 경로. 프론트는 {@code file.webkitRelativePath || file.name} 전달</li>
   * </ul>
   *
   * <p>relativePath 예:
   * <ul>
   *   <li>일반 파일 드롭: {@code report.pdf}</li>
   *   <li>폴더 드롭: {@code 내폴더/01.서류/report.pdf}</li>
   * </ul>
   */
  @ApiOperation(value = "파일 업로드",
      notes = "path=업로드 기준 폴더 DAV 경로 (예: /ERP/2026/02/SB26-G0000/00.공통폴더)\n"
          + "file=Multipart 파일\n"
          + "relativePath=(선택) base 아래 상대 경로. 드래그앤드롭 시 webkitRelativePath || file.name\n"
          + "  - 일반 파일: report.pdf\n"
          + "  - 폴더 드롭: 내폴더/01.서류/report.pdf\n")
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public UploadResultDTO upload(@RequestParam("path") String folderPath,
      @RequestPart("file") MultipartFile file,
      @RequestParam(value = "relativePath", required = false) String relativePath,
      HttpServletRequest request) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    // path: 화면 현재 폴더 (업로드 기준점). 재발행 번호는 원본 폴더 기준으로 해석
    String reqPath = ncBrowsePathResolver.resolveBrowsePath(folderPath);

    /*
     * relativePath가 있으면 최종 업로드 대상 "파일" DAV 경로를 미리 계산합니다.
     * 권한 검증(validateUnderBase)은 폴더뿐 아니라 하위 경로까지 포함한 최종 파일 경로 기준으로 수행합니다.
     *
     * 예) path=/ERP/.../00.공통, relativePath=내폴더/a.pdf
     *     → resolvedFilePath=/ERP/.../00.공통/내폴더/a.pdf
     */
    String resolvedFilePath = reqPath;
    String safeRelative = ErpDavPathUtil.resolveUploadRelativePath(relativePath,
        file == null ? null : file.getOriginalFilename());
    if (safeRelative != null && !safeRelative.isEmpty()) {
      resolvedFilePath = ErpDavPathUtil.joinUnderBase(reqPath, safeRelative);
    }

    // 1) 신청서번호 추출 (resolvedFilePath 기준 — SBxx-Gxxxx가 경로에 포함되어야 함)
    String sbkNo = ErpDavPathUtil.extractSbkNo(resolvedFilePath);

    // 2) 신청서 조회 (SBK_TB)
    SbkInfoVO sbk = sbkService.findBySbkNoAndProvision(sbkNo);
    if (sbk == null)
      return UploadResultDTO.fail("신청서를 찾을 수 없습니다: " + sbkNo);

    String baseFolder = ErpDavPathUtil.normalizePath(sbk.getNcFolderPath()); // /ERP/2025/12/SB25-G1583
    String atchFileId = sbk.getAtchFileId();

    // 3) 최종 파일 경로가 신청서 baseFolder 하위인지 검증 (폴더 드롭 시 하위 폴더까지 포함)
    validateUnderBase(baseFolder, resolvedFilePath);

    // 로그/DB에 남길 표시용 파일명 (relativePath 우선, 없으면 multipart originalFilename)
    String logFileName = safeRelative != null && !safeRelative.isEmpty()
        ? ErpDavPathUtil.fileNameFromRelativePath(safeRelative)
        : (file == null ? null : file.getOriginalFilename());

    // 4) Nextcloud WebDAV 업로드 (relativePath 있으면 하위 폴더 MKCOL + PUT)
    UploadResultDTO up = nextcloudDavService.uploadToFolder(reqPath, file, relativePath,
        user.getId());
    if (!up.isOk()) {
      Long failLogId = startFileOpLog("UPLOAD", resolvedFilePath, relativePath, null, false,
          logFileName, file == null ? null : file.getContentType(),
          file == null ? null : file.getSize(), request);
      markFileOpLogFail(failLogId, new RuntimeException(up.getMessage()), file == null ? null : file.getSize(), null);
      return up;
    }

    String fileDavPath = ErpDavPathUtil.normalizePath(up.getDavPath());
    Long logId = startFileOpLog("UPLOAD", fileDavPath, relativePath, null, false, logFileName,
        file == null ? null : file.getContentType(), file == null ? null : file.getSize(), request);
    try {
      // 5) 메타 저장 (FILE_DETAIL_TB) — originalName은 uploadToFolder가 확정한 파일명 사용
      String creatId = user.getId();
      String storedName =
          up.getOriginalName() != null ? up.getOriginalName() : logFileName;
      nextcloudDavService.insertFileDetail(atchFileId, reqPath, up.getDavPath(), storedName,
          file == null ? null : file.getSize(), creatId);

      markFileOpLogSuccess(logId, file == null ? null : file.getSize(), null);
      return up;
    } catch (Exception e) {
      markFileOpLogFail(logId, e, file == null ? null : file.getSize(), null);
      throw e;
    }

    // 필요하면 FILE_TB에 ATCH_FILE_ID가 없을 때 생성/USE_AT 갱신 등도 여기서 처리

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

    sortWebDavNodes(node.getChildren(), "name", "asc");

    for (WebDavNodeDTO c : node.getChildren()) {
      sortTree(c);
    }
  }

  private static String normalizeListSort(String sort) {
    if (sort == null) {
      return "name";
    }
    String key = sort.trim().toLowerCase(Locale.ROOT);
    if ("date".equals(key) || "size".equals(key)) {
      return key;
    }
    return "name";
  }

  private static String normalizeListOrder(String order) {
    if (order != null && "desc".equalsIgnoreCase(order.trim())) {
      return "desc";
    }
    return "asc";
  }

  /** 폴더 우선, 동일 유형 내 sort/order 적용 */
  private void sortWebDavItems(List<WebDavItemDTO> items, String sort, String order) {
    if (items == null || items.isEmpty()) {
      return;
    }
    Comparator<WebDavItemDTO> bySort = buildWebDavItemComparator(sort);
    if ("desc".equals(order)) {
      bySort = bySort.reversed();
    }
    final Comparator<WebDavItemDTO> itemComparator = bySort;
    items.sort((a, b) -> {
      if (a.isDirectory() != b.isDirectory()) {
        return a.isDirectory() ? -1 : 1;
      }
      return itemComparator.compare(a, b);
    });
  }

  private void sortWebDavNodes(List<WebDavNodeDTO> nodes, String sort, String order) {
    if (nodes == null || nodes.isEmpty()) {
      return;
    }
    Comparator<WebDavNodeDTO> bySort = buildWebDavNodeComparator(sort);
    if ("desc".equals(order)) {
      bySort = bySort.reversed();
    }
    final Comparator<WebDavNodeDTO> nodeComparator = bySort;
    nodes.sort((a, b) -> {
      if (a.isDirectory() != b.isDirectory()) {
        return a.isDirectory() ? -1 : 1;
      }
      return nodeComparator.compare(a, b);
    });
  }

  private static final Pattern NATURAL_NAME_PARTS = Pattern.compile("\\d+|\\D+");

  private static final Collator KOREAN_NAME_COLLATOR;

  static {
    KOREAN_NAME_COLLATOR = Collator.getInstance(Locale.KOREAN);
    KOREAN_NAME_COLLATOR.setStrength(Collator.PRIMARY);
  }

  /** 파일명 숫자 구간은 수치로, 나머지는 한글 locale 로 비교 (1 → 2 → 10 순) */
  private static int compareNaturalFileName(String a, String b) {
    if (a == null) {
      a = "";
    }
    if (b == null) {
      b = "";
    }
    if (a.equals(b)) {
      return 0;
    }

    Matcher ma = NATURAL_NAME_PARTS.matcher(a);
    Matcher mb = NATURAL_NAME_PARTS.matcher(b);

    while (ma.find() && mb.find()) {
      String pa = ma.group();
      String pb = mb.group();
      boolean da = Character.isDigit(pa.charAt(0));
      boolean db = Character.isDigit(pb.charAt(0));
      int cmp;
      if (da && db) {
        cmp = compareNumericToken(pa, pb);
      } else {
        cmp = KOREAN_NAME_COLLATOR.compare(pa, pb);
      }
      if (cmp != 0) {
        return cmp;
      }
    }
    if (ma.hitEnd() && mb.hitEnd()) {
      return 0;
    }
    return ma.hitEnd() ? -1 : 1;
  }

  private static int compareNumericToken(String a, String b) {
    if (a.length() != b.length()) {
      return a.length() - b.length();
    }
    return a.compareTo(b);
  }

  private Comparator<WebDavItemDTO> buildWebDavItemComparator(String sort) {
    if ("date".equals(sort)) {
      return Comparator.comparing(
          it -> it.getLastModified() == null ? "" : it.getLastModified(),
          String.CASE_INSENSITIVE_ORDER);
    }
    if ("size".equals(sort)) {
      return Comparator.comparing(it -> it.getSize() == null ? 0L : it.getSize());
    }
    return (a, b) -> compareNaturalFileName(
        a.getName() == null ? "" : a.getName(),
        b.getName() == null ? "" : b.getName());
  }

  private Comparator<WebDavNodeDTO> buildWebDavNodeComparator(String sort) {
    if ("date".equals(sort)) {
      return Comparator.comparing(
          it -> it.getLastModified() == null ? "" : it.getLastModified(),
          String.CASE_INSENSITIVE_ORDER);
    }
    if ("size".equals(sort)) {
      return Comparator.comparing(it -> it.getSize() == null ? 0L : it.getSize());
    }
    return (a, b) -> compareNaturalFileName(
        a.getName() == null ? "" : a.getName(),
        b.getName() == null ? "" : b.getName());
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

  @ApiOperation(value = "이름 변경", notes = "targetDavPath 대상 경로 예시 /ERP/2026/02/SB26-G0000/00.공통폴더\n"
      + "newName 새 이름 예시 00.공통폴더_수정\n" + "overwrite 대상 이름이 이미 존재할 때 덮어쓰기 여부\n")
  @PostMapping(value = "/rename", consumes = MediaType.APPLICATION_JSON_VALUE)
  public NcSimpleResult rename(@RequestBody NcRenameRequest req, HttpServletRequest request)
      throws Exception {

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

    // src/dst 를 분리해서 이력에 남김
    String dstPath = ErpDavPathUtil.normalizePath(parent + "/" + newName);
    Long logId = startFileOpLog("RENAME", dstPath, path, dstPath, false, newName, null, null,
        request);
    try {
      String changed = nextcloudDavService.rename(path, newName, req.isOverwrite(), user.getId());
      markFileOpLogSuccess(logId, null, null);
      return NcSimpleResult.ok(changed);
    } catch (Exception e) {
      markFileOpLogFail(logId, e, null, null);
      throw e;
    }
  }

  @ApiOperation(value = "삭제", notes = "davPath 삭제 대상 경로 예시 /ERP/2026/02/SB26-G0000/00.공통폴더\n"
      + "recursive 폴더일 때 하위까지 삭제 여부\n")
  @PostMapping(value = "/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
  public NcSimpleResult delete(@RequestBody NcDeleteRequest req, HttpServletRequest request)
      throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    String path = ErpDavPathUtil.normalizePathOrRoot(req.getPath());

    boolean recursive = req.isRecursive();

    Long logId = startFileOpLog("DELETE", path, null, null, recursive, extractNameFromPath(path),
        null, null, request);
    try {
      nextcloudDavService.deleteWithDbSync(path, recursive, user.getId());
      markFileOpLogSuccess(logId, null, null);
      return NcSimpleResult.ok("OK");
    } catch (Exception e) {
      markFileOpLogFail(logId, e, null, null);
      throw e;
    }
  }

  // -----------------------------
  // 다운로드/미리보기/폴더 ZIP
  // -----------------------------

  @GetMapping("/download")
  public void downloadFile(@RequestParam("path") String path, HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    String davPath = ncBrowsePathResolver.resolveBrowsePath(path);

    String pathErr = validateDownloadPathStrong(davPath);
    if (pathErr != null) {
      response.setStatus(400);
      response.setContentType("application/json;charset=UTF-8");
      response.getWriter().write("{\"ok\":false,\"message\":\"" + escapeJson(pathErr) + "\"}");
      return;
    }

    String fileName = extractNameFromPath(davPath);
    String contentType = detectContentType(fileName);

    Long logId = startFileOpLog("DOWNLOAD", davPath, null, null, false, fileName, contentType, null,
        request);
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
      markFileOpLogSuccess(logId, null, bytesSent);

    } catch (Exception e) {
      markFileOpLogFail(logId, e, null, bytesSent);
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

    String davPath = ncBrowsePathResolver.resolveBrowsePath(path);

    String pathErr = validateDownloadPathStrong(davPath);
    if (pathErr != null) {
      response.setStatus(400);
      response.setContentType("application/json;charset=UTF-8");
      response.getWriter().write("{\"ok\":false,\"message\":\"" + escapeJson(pathErr) + "\"}");
      return;
    }

    String fileName = extractNameFromPath(davPath);
    String contentType = detectContentType(fileName);

    // PREVIEW는 이력에서 제외
    Long logId = null;

    long bytesSent = 0L;

    response.setStatus(200);
    // 바이너리 미리보기(PDF/이미지 등)에 charset 이 끼면 일부 환경에서 인라인 렌더 실패.
    // setContentType 은 필터(CharacterEncodingFilter 등)에 의해 charset 이 강제 부착될 수 있어
    // 헤더를 직접 set 하고, characterEncoding 도 비워둔다.
    response.setCharacterEncoding(null);
    response.setHeader("Content-Type", contentType);
    response.setHeader("X-Content-Type-Options", "nosniff");

    // inline 미리보기
    response.setHeader("Content-Disposition",
        buildContentDispositionByUa("inline", fileName, request));

    InputStream in = null;
    BufferedInputStream bin = null;
    BufferedOutputStream bout = null;
    try {
      bytesSent = streamFileToResponse(davPath, response);
      markFileOpLogSuccess(logId, null, bytesSent);
    } catch (Exception e) {
      markFileOpLogFail(logId, e, null, bytesSent);
      throw e;
    } finally {
      safeClose(bout);
      safeClose(bin);
      safeClose(in);
    }
  }

  /**
   * PDF 미리보기 전용 — Nextcloud 의 files_pdfviewer 와 동일한 방식.
   * 브라우저 내장 PDF 뷰어가 비활성화/다운로드 처리되는 환경에서도 미리보기가 보장되도록
   * Mozilla PDF.js prebuilt 의 viewer.html 로 리다이렉트한다.
   *
   * 사용 전 준비: sberp/src/main/webapp/pdfjs/web/viewer.html 가 배포되어 있어야 한다.
   */
  @ApiOperation(value = "PDF 미리보기 뷰어",
      notes = "PDF 전용 PDF.js 뷰어로 리다이렉트한다. 사용 예: /api/nc/preview-viewer?path=/ERP/2026/05/.../A.pdf")
  @GetMapping("/preview-viewer")
  public void previewViewer(@RequestParam("path") String path, HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    String davPath = ncBrowsePathResolver.resolveBrowsePath(path);

    String pathErr = validateDownloadPathStrong(davPath);
    if (pathErr != null) {
      response.setStatus(400);
      response.setContentType("application/json;charset=UTF-8");
      response.getWriter().write("{\"ok\":false,\"message\":\"" + escapeJson(pathErr) + "\"}");
      return;
    }

    String fileName = extractNameFromPath(davPath);
    String lower = fileName == null ? "" : fileName.toLowerCase();
    if (!lower.endsWith(".pdf")) {
      response.setStatus(400);
      response.setContentType("application/json;charset=UTF-8");
      response.getWriter().write("{\"ok\":false,\"message\":\"PDF 파일만 지원합니다.\"}");
      return;
    }

    // contextPath 가 이미 "/api" 이므로, 컨트롤러 기준 경로(/nc/preview, /pdfjs/...) 만 더한다.
    String ctx = request.getContextPath() == null ? "" : request.getContextPath();
    String pdfUrl = ctx + "/nc/preview?path=" + urlEncodeUtf8(davPath);
    String viewerUrl =
        ctx + "/pdfjs/web/viewer.html?file=" + urlEncodeUtf8(pdfUrl);

    response.sendRedirect(viewerUrl);
  }

  /**
   * ONLYOFFICE 뷰어 HTML. Nextcloud ONLYOFFICE 커넥터 OCS 로 설정을 받고, 저장은 Nextcloud 가 처리한다.
   */
  @ApiOperation(value = "ONLYOFFICE 미리보기/편집(HTML)",
      notes = "mode=view(기본) | edit. 예: /api/nc/preview-onlyoffice?path=/ERP/.../a.xlsx&amp;mode=edit")
  @GetMapping(value = "/preview-onlyoffice", produces = "text/html;charset=UTF-8")
  public void previewOnlyoffice(@RequestParam("path") String path,
      @RequestParam(value = "mode", required = false, defaultValue = "view") String mode,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    if (!OnlyOfficeIntegration.isEnabled(propertyService)) {
      response.setStatus(503);
      response.setContentType("text/plain;charset=UTF-8");
      response.getWriter().write("ONLYOFFICE 연동이 비활성입니다. Globals.nc.* 및 NC ONLYOFFICE 앱을 확인하세요.");
      return;
    }

    String davPath = ncBrowsePathResolver.resolveBrowsePath(path);
    String pathErr = validateDownloadPathStrong(davPath);
    if (pathErr != null) {
      response.setStatus(400);
      response.setContentType("text/plain;charset=UTF-8");
      response.getWriter().write(pathErr);
      return;
    }

    String fileName = extractNameFromPath(davPath);
    if (!OnlyOfficePreviewSupport.isPreviewableExtension(fileName)) {
      response.setStatus(400);
      response.setContentType("text/plain;charset=UTF-8");
      response.getWriter().write("이 확장자는 ONLYOFFICE 미리보기 대상이 아닙니다.");
      return;
    }

    boolean editMode = "edit".equalsIgnoreCase(mode == null ? "" : mode.trim());
    String contentType = detectContentType(fileName);
    Long openLogId = null;
    if (editMode) {
      openLogId = startFileOpLog("ONLYOFFICE_OPEN", davPath, null, null, false, fileName,
          contentType, null, request);
    }
    try {
      Map<String, Object> cfg =
          nextcloudOnlyofficeConnectorService.fetchEditorConfig(davPath, !editMode);
      if (editMode && OnlyOfficeEditorConfig.isSpreadsheetFile(fileName)) {
        OnlyOfficeEditorConfig.applyStrictCoEditing(cfg);
      }
      Object dsUrl = cfg.get("documentServerUrl");
      if (dsUrl == null) {
        throw new IllegalStateException("documentServerUrl missing in NC ONLYOFFICE config");
      }
      String dsJs = OnlyOfficeEditorHtml.normalizeDocumentServerApiJs(dsUrl.toString());
      OnlyOfficeEditorHtml.write(response, fileName, cfg, dsJs, davPath, editMode);
      if (openLogId != null) {
        markFileOpLogSuccess(openLogId, null, null);
      }
    } catch (Exception e) {
      if (openLogId != null) {
        markFileOpLogFail(openLogId, e, null, null);
      }
      response.setStatus(502);
      response.setContentType("text/plain;charset=UTF-8");
      response.getWriter().write("Nextcloud ONLYOFFICE 연동 오류: " + e.getMessage());
    }
  }

  /**
   * ONLYOFFICE 편집기(브라우저)에서 저장 완료 시 호출. NC 가 실제 파일을 저장한다.
   */
  @ApiOperation(value = "ONLYOFFICE 저장 이력", notes = "편집기 iframe 내부에서 ONLYOFFICE_SAVE 로 호출")
  @PostMapping("/onlyoffice-op-log")
  public Map<String, Object> onlyofficeOpLog(@RequestBody OnlyOfficeOpLogRequest body,
      HttpServletRequest request) {
    Map<String, Object> res = new LinkedHashMap<String, Object>();
    if (body == null || body.getPath() == null || body.getOpType() == null) {
      res.put("ok", false);
      res.put("message", "path, opType required");
      return res;
    }
    String opType = body.getOpType().trim().toUpperCase();
    if (!"ONLYOFFICE_SAVE".equals(opType)) {
      res.put("ok", false);
      res.put("message", "unsupported opType");
      return res;
    }
    try {
      String davPath = ncBrowsePathResolver.resolveBrowsePath(body.getPath());
      String pathErr = validateDownloadPathStrong(davPath);
      if (pathErr != null) {
        res.put("ok", false);
        res.put("message", pathErr);
        return res;
      }
      String fileName = extractNameFromPath(davPath);
      Long logId = startFileOpLog(opType, davPath, null, null, false, fileName,
          detectContentType(fileName), null, request);
      markFileOpLogSuccess(logId, null, null);
      res.put("ok", true);
      return res;
    } catch (Exception e) {
      res.put("ok", false);
      res.put("message", e.getMessage());
      return res;
    }
  }


  @GetMapping("/download-folder")
  public void downloadFolderAsZip(@RequestParam("path") String path, HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    String folderPath = ncBrowsePathResolver.resolveBrowsePath(path);

    String pathErr = validateDownloadPathStrong(folderPath);
    if (pathErr != null) {
      response.setStatus(400);
      response.setContentType("application/json;charset=UTF-8");
      response.getWriter().write("{\"ok\":false,\"message\":\"" + escapeJson(pathErr) + "\"}");
      return;
    }

    String zipName = buildZipDownloadName(folderPath);

    Long logId = startFileOpLog("DOWNLOAD_FOLDER", folderPath, null, null, true, null,
        "application/zip", null, request);

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
      markFileOpLogSuccess(logId, null, bytesSent);

    } catch (Exception e) {
      long bytesSentSoFar = (cos != null) ? cos.getBytesWritten() : 0L;
      markFileOpLogFail(logId, e, null, bytesSentSoFar);
      throw e;
    } finally {
      safeClose(zos);
      safeClose(bout);
      safeClose(cos);
    }
  }

  @ApiOperation(value = "이미지 PDF 변환 다운로드 (한 페이지 2장)",
      notes = "선택한 Nextcloud 이미지를 A4 세로 PDF로 합쳐 다운로드합니다.\n"
          + "- 페이지당 2장(위·아래), paths 배열 순서 유지\n"
          + "- PDF 파일명: 사진이 있는 폴더명.pdf (자동)\n"
          + "- paths 예: [\"/ERP/2026/05/SB26-G0000/00.시험사진/a.jpg\"]")
  @PostMapping(value = "/images-to-pdf", consumes = MediaType.APPLICATION_JSON_VALUE)
  public void convertImagesToPdf(@RequestBody NcImagesToPdfRequest req, HttpServletRequest request,
      HttpServletResponse response) throws Exception {

    if (!EgovUserDetailsHelper.isAuthenticated()) {
      writePdfError(response, 401, ResponseMessage.UNAUTHORIZED);
      return;
    }

    try {
      ncImagePdfService.download(req, request, response);
    } catch (NcImagePdfException e) {
      writePdfError(response, e.getStatusCode(), e.getMessage());
    }
  }

  @ApiOperation(value = "이미지 PDF 변환 저장 (한 페이지 2장)",
      notes = "선택한 이미지를 PDF로 변환해 원본과 같은 폴더에 저장합니다.\n"
          + "- PDF 파일명: 사진이 있는 폴더명.pdf (자동)\n"
          + "- overwrite=true 이면 동일 파일명 PDF 덮어쓰기")
  @PostMapping(value = "/images-to-pdf/save", consumes = MediaType.APPLICATION_JSON_VALUE)
  public NcImagesToPdfResult saveImagesAsPdf(@RequestBody NcImagesToPdfRequest req,
      HttpServletRequest request, HttpServletResponse response) {

    if (!EgovUserDetailsHelper.isAuthenticated()) {
      response.setStatus(401);
      return NcImagesToPdfResult.fail(ResponseMessage.UNAUTHORIZED);
    }

    try {
      NcImagesToPdfResult result = ncImagePdfService.save(req, request);
      response.setStatus(200);
      return result;
    } catch (NcImagePdfException e) {
      response.setStatus(e.getStatusCode());
      return NcImagesToPdfResult.fail(e.getMessage());
    } catch (Exception e) {
      response.setStatus(502);
      return NcImagesToPdfResult.fail(
          e.getMessage() == null ? "PDF 저장에 실패했습니다." : e.getMessage());
    }
  }

  private void writePdfError(HttpServletResponse response, int status, String message)
      throws Exception {
    if (response.isCommitted()) {
      return;
    }
    response.resetBuffer();
    response.setStatus(status);
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write("{\"ok\":false,\"message\":\"" + escapeJson(message) + "\"}");
  }

  @ApiOperation(value = "이동 MOVE",
      notes = "sourceDavPath 원본 경로 예시 /ERP/2026/02/SB26-G0000/00.공통폴더\n"
          + "destDavPath 목적지 경로 예시 /ERP/2026/02/SB26-G0000/01.신청서\n"
          + "overwrite 목적지에 동일 이름이 있을 때 덮어쓰기 여부\n")
  @PostMapping("/move")
  public NcSimpleResult moveWithSync(@RequestBody NcMoveRequest req, HttpServletRequest request)
      throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String userId = user.getId();

    String src = req == null ? "" : req.getSourceDavPath();
    String dst = req == null ? "" : req.getDestDavPath();
    boolean overwrite = req != null && req.isOverwrite();

    Long logId = startFileOpLog("MOVE", dst, src, dst, true, null, null, null, request);
    try {
      String moved = nextcloudDavService.moveWithDbSync(src, dst, overwrite, userId);
      markFileOpLogSuccess(logId, null, null);
      return NcSimpleResult.ok(moved);
    } catch (Exception e) {
      markFileOpLogFail(logId, e, null, null);
      throw e;
    }
  }

  @ApiOperation(value = "복사 COPY함",
      notes = "sourceDavPath 원본 경로 예시 /ERP/2026/02/SB26-G0000/00.공통폴더\n"
          + "destDavPath 목적지 경로 예시 /ERP/2026/02/SB26-G0000/00.공통폴더_복사\n"
          + "overwrite 목적지에 동일 이름이 있을 때 덮어쓰기 여부\n" + "metaMode 메타 반영 방식 TOP_ONLY 또는 FULL(기본)\n")
  @PostMapping("/copy")
  public NcSimpleResult copyWithSync(@RequestBody NcCopyRequest req, HttpServletRequest request)
      throws Exception {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    String userId = user.getId();

    String src = req == null ? "" : req.getSourceDavPath();
    String dst = req == null ? "" : req.getDestDavPath();
    boolean overwrite = req != null && req.isOverwrite();
    String metaMode = req == null ? null : req.getMetaMode();

    Long logId = startFileOpLog("COPY", dst, src, dst, true, null, null, null, request);
    try {
      String copied = nextcloudDavService.copyWithDbSync(src, dst, overwrite, userId, metaMode);
      markFileOpLogSuccess(logId, null, null);
      return NcSimpleResult.ok(copied);
    } catch (Exception e) {
      markFileOpLogFail(logId, e, null, null);
      throw e;
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
      ct = detectContentTypeByExt(fileName);
    }
    return ct;
  }

  private String detectContentTypeByExt(String fileName) {
    return FilePreviewPolicy.detectMimeByExt(fileName);
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
    // filename 은 ASCII 만 안전하므로 한글/특수문자는 '_'로 대체한 ASCII fallback 을 함께 내려보낸다.
    // 실제 표시용 한글 이름은 RFC 5987 의 filename* 로 전달.
    String asciiFallback = toAsciiFallbackName(original);
    return type + "; filename=\"" + asciiFallback + "\"; filename*=UTF-8''" + encodedName;
  }

  private String toAsciiFallbackName(String original) {
    if (original == null || original.isEmpty()) return "file";
    StringBuilder sb = new StringBuilder(original.length());
    for (int i = 0; i < original.length(); i++) {
      char c = original.charAt(i);
      // ASCII 가시문자만 허용. 따옴표/역슬래시/세미콜론은 헤더 파싱 안전을 위해 제거.
      if (c >= 0x20 && c < 0x7F && c != '"' && c != '\\' && c != ';') {
        sb.append(c);
      } else {
        sb.append('_');
      }
    }
    String s = sb.toString().trim();
    return s.isEmpty() ? "file" : s;
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

  private FileOpLogVO buildFileOpLog(String opType, String davPath, String srcPath, String dstPath,
      boolean isFolder, String fileName, String contentType, Long fileSize,
      javax.servlet.http.HttpServletRequest request) {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    FileOpLogVO log = new FileOpLogVO();
    log.setUserId(user == null ? null : user.getId());
    log.setDept(user == null ? null : user.getDeptName());

    // sbkNo는 경로에서 최대한 뽑아냄
    String sbkNo = ErpDavPathUtil.extractSbkNo(davPath);
    if ((sbkNo == null || sbkNo.trim().isEmpty()) && srcPath != null) {
      sbkNo = ErpDavPathUtil.extractSbkNo(srcPath);
    }
    if ((sbkNo == null || sbkNo.trim().isEmpty()) && dstPath != null) {
      sbkNo = ErpDavPathUtil.extractSbkNo(dstPath);
    }

    log.setSbkNo(sbkNo);
    log.setOpType(opType);
    log.setUploadSrc("A"); // Nextcloud API(/nc)는 파일 모달/연동 경로로 취급
    log.setDavPath(davPath);
    log.setSrcPath(srcPath);
    log.setDstPath(dstPath);
    log.setIsFolder(isFolder ? "Y" : "N");
    log.setFileName(fileName);
    log.setContentType(contentType);
    log.setFileSize(fileSize == null ? 0L : fileSize);
    log.setBytesSent(0L);
    log.setClientIp(resolveClientIp(request));
    log.setUserAgent(request == null ? null : request.getHeader("User-Agent"));
    log.setResultCd("START");
    log.setErrMsg(null);
    return log;
  }

  private Long startFileOpLog(String opType, String davPath, String srcPath, String dstPath,
      boolean isFolder, String fileName, String contentType, Long fileSize,
      javax.servlet.http.HttpServletRequest request) {

    FileOpLogVO log = buildFileOpLog(opType, davPath, srcPath, dstPath, isFolder, fileName,
        contentType, fileSize, request);
    return fileOpLogService.start(log);
  }

  private void markFileOpLogSuccess(Long logId, Long fileSize, Long bytesSent) {
    fileOpLogService.success(logId, fileSize, bytesSent);
  }

  private void markFileOpLogFail(Long logId, Exception e, Long fileSize, Long bytesSent) {
    String msg = e == null ? null : e.getMessage();
    fileOpLogService.fail(logId, msg, fileSize, bytesSent);
  }


}
