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
import egovframework.cmm.service.NcCreateFolderRequest;
import egovframework.cmm.service.NcSimpleResult;
import egovframework.cmm.service.NextcloudDavService;
import egovframework.cmm.service.UploadResultDTO;
import egovframework.cmm.service.WebDavItemDTO;
import egovframework.cmm.service.WebDavNodeDTO;
import io.swagger.annotations.Api;

@Api(tags = {"파일서버"})
@RestController
@RequestMapping("/nc")
public class NextcloudFolderController {

    @Resource(name = "NextcloudDavService")
    private NextcloudDavService nextcloudDavService;

    /** 4) 선택 폴더 안의 폴더+파일 목록(Depth=1) */
    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(name = "path", required = false, defaultValue = "") String path
    ) throws Exception {

        String base = normalizePathOrRoot(path);

        List<WebDavItemDTO> raw = nextcloudDavService.list(base, 1);

        // PROPFIND Depth=1은 보통 첫 항목이 "자기 자신"이라 제거
        List<WebDavItemDTO> items = raw == null ? new ArrayList<WebDavItemDTO>()
                : raw.stream()
                     .filter(it -> it != null && it.getDavPath() != null)
                     .filter(it -> !normalizePath(it.getDavPath()).equals(base))
                     .collect(Collectors.toList());

        // 폴더 먼저, 파일 나중(정렬)
        items.sort((a, b) -> {
            if (a.isDirectory() != b.isDirectory()) return a.isDirectory() ? -1 : 1;
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
            @RequestParam(name = "onlyDir", required = false, defaultValue = "true") boolean onlyDir
    ) throws Exception {

        String base = normalizePathOrRoot(path);
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
        String parent = normalizePathOrRoot(req.getParentPath());
        String name = req.getName() == null ? "" : req.getName().trim();

        if (name.isEmpty()) return NcSimpleResult.fail("폴더명이 비어있습니다.");
        if (name.contains("/")) return NcSimpleResult.fail("폴더명에 '/'는 사용할 수 없습니다.");

        String created = nextcloudDavService.createFolder(parent, name);
        return NcSimpleResult.ok(created);
    }

    /** 5) 파일 업로드 (완료 신호는 JSON 응답으로) */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResultDTO upload(@RequestParam("path") String folderPath,
                               @RequestPart("file") MultipartFile file) throws Exception {
        String folder = normalizePathOrRoot(folderPath);
        return nextcloudDavService.uploadToFolder(folder, file);
    }

    // ----------------- helpers -----------------

    private String normalizePathOrRoot(String path) {
        if (path == null) return "/ERP";
        String p = path.trim();
        if (p.isEmpty()) return "/ERP";
        if (!p.startsWith("/")) p = "/" + p;
        p = p.replaceAll("/+$", "");
        if (p.isEmpty()) return "/ERP";
        if (!p.startsWith("/ERP")) {
            // 실수 방지: ERP 아래만 허용(원하면 제거 가능)
            p = "/ERP" + (p.startsWith("/") ? "" : "/") + p;
            p = p.replaceAll("//+", "/");
        }
        return p;
    }

    private String normalizePath(String path) {
        if (path == null) return "";
        String p = path.trim();
        if (!p.startsWith("/")) p = "/" + p;
        return p.replaceAll("/+$", "");
    }

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
                if (it == null || it.getDavPath() == null) continue;

                String p = normalizePath(it.getDavPath());
                if (p.equals(base)) {
                    // 자기 자신(메타 갱신)
                    root.setName(it.getName());
                    root.setDirectory(it.isDirectory());
                    root.setCanWrite(it.isCanWrite());
                    root.setLastModified(it.getLastModified());
                    root.setSize(it.getSize());
                    continue;
                }

                if (onlyDir && !it.isDirectory()) continue;

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
            if (node == root) continue;

            String parent = parentOf(node.getDavPath());
            WebDavNodeDTO parentNode = map.get(parent);
            if (parentNode == null) parentNode = root; // 안전

            parentNode.getChildren().add(node);
        }

        // sort folders first
        sortTree(root);
        return root;
    }

    private String parentOf(String path) {
        String p = normalizePath(path);
        int idx = p.lastIndexOf("/");
        if (idx <= 0) return "/ERP";
        return p.substring(0, idx);
    }

    private void sortTree(WebDavNodeDTO node) {
        if (node.getChildren() == null) return;

        node.getChildren().sort((a, b) -> {
            if (a.isDirectory() != b.isDirectory()) return a.isDirectory() ? -1 : 1;
            String an = a.getName() == null ? "" : a.getName();
            String bn = b.getName() == null ? "" : b.getName();
            return an.compareToIgnoreCase(bn);
        });

        for (WebDavNodeDTO c : node.getChildren()) {
            sortTree(c);
        }
    }
    
}
