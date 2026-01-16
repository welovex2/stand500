package egovframework.cmm.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import egovframework.cmm.service.HttpPropfind;
import egovframework.cmm.service.NcFileDTO;
import egovframework.cmm.service.NextcloudDavService;
import egovframework.cmm.service.NextcloudFolderService;
import egovframework.cmm.service.NextcloudShareService;
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
    private String baseDav;     // .../remote.php/dav/files
    private String erpUser;     // stderp
    private String authHeader;  // Basic erpUser:AppPassword
    private static final String ROOT_FOLDER = "/ERP";
    
    @PostConstruct
    public void init() {
        http = HttpClients.createDefault();
        baseDav = propertyService.getString("Globals.nc.webdav.base"); // 예: http://host/remote.php/dav/files
        erpUser = propertyService.getString("Globals.nc.user");
        String appPassword = propertyService.getString("Globals.nc.appPassword");

        String basic = erpUser + ":" + appPassword;
        authHeader = "Basic " + Base64.getEncoder()
                .encodeToString(basic.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public List<NcFileDTO> listErpFolder(String erpRelativeFolder) throws Exception {
        String folder = normalizeFolder(erpRelativeFolder);
        String url = baseDav + "/" + erpUser + "/ERP" + folder;

        String body =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
          + "<d:propfind xmlns:d=\"DAV:\" xmlns:oc=\"http://owncloud.org/ns\">"
          + "  <d:prop>"
          + "    <d:resourcetype/>"
          + "    <d:getlastmodified/>"
          + "    <d:getcontentlength/>"
          + "    <d:getcontenttype/>"
          + "    <d:getetag/>"
          + "  </d:prop>"
          + "</d:propfind>";

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
      if (start > 0) xml = xml.substring(start);

      DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
      f.setNamespaceAware(true);   // ★ 핵심!!
      Document doc = f.newDocumentBuilder()
              .parse(new java.io.ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

      NodeList responses = doc.getElementsByTagNameNS("DAV:", "response");

      for (int i = 0; i < responses.getLength(); i++) {
          Element resp = (Element) responses.item(i);

          String href = getText(resp, "DAV:", "href"); // URL-encoded href
          if (href == null) continue;

          String decodedHref = java.net.URLDecoder.decode(href, "UTF-8");

          // 1) 우선 target 경로(baseDavPath)가 포함된 응답만 처리
          //    decodedHref 예: /remote.php/dav/files/stderp/ERP/2025/12/RAW/xxx.jpg
          if (!decodedHref.contains(baseDavPath)) {
              continue;
          }

          // 2) "폴더 자기 자신" 항목은 제외
          //    마지막 슬래시 유무 다 고려
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
          if (okPropstat == null) continue;

          Element okProp = (Element) okPropstat.getElementsByTagNameNS("DAV:", "prop").item(0);
          if (okProp == null) continue;

          boolean isDir = hasResourceTypeCollection(okProp); // ★ resp가 아니라 okProp 기준

          String name = extractName(decodedHref);

          String sizeStr = getText(okProp, "DAV:", "getcontentlength"); // ★ okProp 기준
          long size = 0;
          if (sizeStr != null && !sizeStr.isEmpty()) {
              try { size = Long.parseLong(sizeStr); } catch (Exception ignore) {}
          }

          String contentType = getText(okProp, "DAV:", "getcontenttype"); // ★ okProp 기준
          String lastMod = getText(okProp, "DAV:", "getlastmodified");    // ★ okProp 기준

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
        if (f == null || f.trim().isEmpty()) return "/";
        String x = f.trim();
        if (!x.startsWith("/")) x = "/" + x;
        if (x.endsWith("/")) x = x.substring(0, x.length() - 1);
        return x;
    }

    private String getText(Element parent, String ns, String tag) {
        NodeList nl = parent.getElementsByTagNameNS(ns, tag);
        if (nl.getLength() == 0) return null;
        Node n = nl.item(0);
        return n.getTextContent();
    }

    private boolean hasResourceTypeCollection(Element resp) {
        NodeList rt = resp.getElementsByTagNameNS("DAV:", "resourcetype");
        if (rt.getLength() == 0) return false;
        Element r = (Element) rt.item(0);
        NodeList col = r.getElementsByTagNameNS("DAV:", "collection");
        return col.getLength() > 0;
    }

    private String extractName(String decodedHref) {
        int idx = decodedHref.lastIndexOf("/");
        if (idx < 0) return decodedHref;
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
        if (p >= 0) return decodedHref.substring(p);
        if (decodedHref.endsWith("/ERP")) return "/ERP";
        return decodedHref; // fallback
    }
    
    /**
     * 폴더만 생성
     */
    @Override
    public String ensureApplyFolder(String yearMonth, String applyNo) throws Exception {
      String relativePath = yearMonth + "/" + applyNo;

      nextcloudDavService.ensureFolder(relativePath);

      String commonSubFolderName = "00.공통폴더";
      nextcloudDavService.ensureFolder(relativePath + "/" + commonSubFolderName);

      return relativePath;
  }

    /**
     * 폴더 생성 + 공유
     */
    @Override
    public String createApplyFolderAndGrant(
            String yearMonth,
            String applyNo,
            String targetId,
            boolean isGroup
    ) throws Exception {

        // 1) 상대 경로 만들기: "2025/12/SB25-G1845"
        String relativePath = yearMonth + "/" + applyNo;

        // 2) WebDAV로 폴더 재귀 생성 (없으면 자동 MKCOL)
        nextcloudDavService.ensureFolder(relativePath);

        // 2-1) 하위 공통 폴더 생성: "2025/12/SB25-G1845/00.공통폴더"
        String commonSubFolderName = "00.공통폴더";
        String commonRelativePath = relativePath + "/" + commonSubFolderName;
        nextcloudDavService.ensureFolder(commonRelativePath);
        
        // 3) Nextcloud 경로(davPath)
        String davPath = ROOT_FOLDER + "/" + relativePath; 
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
}
