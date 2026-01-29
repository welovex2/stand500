package egovframework.cmm.web;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
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
import egovframework.cmm.service.NcCreateFolderRequest;
import egovframework.cmm.service.NcSimpleResult;
import egovframework.cmm.service.NextcloudDavService;
import egovframework.cmm.service.SbkInfoVO;
import egovframework.cmm.service.UploadResultDTO;
import egovframework.cmm.service.WebDavItemDTO;
import egovframework.cmm.service.WebDavNodeDTO;
import egovframework.cmm.service.WebDavPathResolver;
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.cmm.util.ErpDavPathUtil;
import egovframework.sbk.service.SbkService;
import io.swagger.annotations.Api;

@Api(tags = {"파일서버"})
@RestController
@RequestMapping("/nc")
public class NextcloudFolderController {

  @Resource(name = "NextcloudDavService")
  private NextcloudDavService nextcloudDavService;

  @Resource(name = "WebDavPathResolver")
  private WebDavPathResolver webDavPathResolver;

  @Resource(name = "SbkService")
  private SbkService sbkService;


  /** 4) 선택 폴더 안의 폴더+파일 목록(Depth=1) */
  @GetMapping("/list")
  public Map<String, Object> list(
      @RequestParam(name = "path", required = false, defaultValue = "") String path)
      throws Exception {

    String base = ErpDavPathUtil.normalizePathOrRoot(path);

    // 2) path가 "코드(SB..-...)" 형태면 fullPath로 바꿔치기
    base = webDavPathResolver.resolveIfCode(base);
    // 람다에서 사용할 final 변수
    String basePath = base;

    List<WebDavItemDTO> raw = nextcloudDavService.list(base, 1);

    // PROPFIND Depth=1은 보통 첫 항목이 "자기 자신"이라 제거
    List<WebDavItemDTO> items = raw == null ? new ArrayList<WebDavItemDTO>()
        : raw.stream().filter(it -> it != null && it.getDavPath() != null)
            .filter(it -> !ErpDavPathUtil.normalizePath(it.getDavPath()).equals(basePath))
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
    res.put("path", base);
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

    // 2) path가 "코드(SB..-...)" 형태면 fullPath로 바꿔치기
    base = webDavPathResolver.resolveIfCode(base);

    List<WebDavItemDTO> raw = nextcloudDavService.list(base, depth);

    WebDavNodeDTO tree = buildTree(base, raw, onlyDir);

    Map<String, Object> res = new LinkedHashMap<String, Object>();
    res.put("path", base);
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

    String created = nextcloudDavService.createFolder(parent, name, user.getId());
    return NcSimpleResult.ok(created);
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

  // ----------------- helpers -----------------


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

  private void validateUnderBase(String baseFromDb, String requestPath) {
    String base = ErpDavPathUtil.normalizePath(ensureRootPrefix(baseFromDb));
    String req = ErpDavPathUtil.normalizePath(requestPath);

    String basePrefix = base.endsWith("/") ? base : base + "/";

    if (req.equals(base) || req.startsWith(basePrefix))
      return;

    throw new IllegalArgumentException(
        "요청 경로가 신청서 기본 폴더 하위가 아닙니다. base=" + base + ", request=" + req);
  }

  private String ensureRootPrefix(String p) {
    String x = ErpDavPathUtil.normalizePath(p);
    // DB에 /ERP 없이 /2025/12/... 처럼 저장된 경우 보정
    if (!x.startsWith(ROOT + "/") && !x.equals(ROOT)) {
      x = ErpDavPathUtil.normalizePath(ROOT + x);
    }
    return x;
  }

}
