package egovframework.cmm.service.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.annotation.PostConstruct;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import egovframework.cmm.service.NextcloudShareService;
import egovframework.rte.fdl.property.EgovPropertyService;
import lombok.extern.slf4j.Slf4j;

@Service("NextcloudShareService")
@Slf4j
public class NextcloudShareServiceImpl implements NextcloudShareService {

    @Autowired
    private EgovPropertyService propertyService;

    private CloseableHttpClient http;
    private String base;
    private String user;
    private String appPassword;
    private String authHeader;

    @PostConstruct
    public void init() {
        http = HttpClients.createDefault();
        base        = propertyService.getString("Globals.nc.base");
        user        = propertyService.getString("Globals.nc.user");
        appPassword = propertyService.getString("Globals.nc.appPassword");

        String basic = user + ":" + appPassword;
        authHeader = "Basic " + Base64.getEncoder()
                .encodeToString(basic.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String getOrCreateErpFolderShareToken() throws Exception {
        // 1) 기존 공유 조회
        String token = findExistingShareToken("/ERP");
        if (token != null) return token;

        // 2) 없으면 생성
        return createPublicShareToken("/ERP");
    }

    private String findExistingShareToken(String path) throws Exception {
        String url = base + "/ocs/v2.php/apps/files_sharing/api/v1/shares"
                + "?path=" + encode(path)
                + "&reshares=true&subfiles=true";

        HttpGet get = new HttpGet(url);
        get.setHeader("Authorization", authHeader);
        get.setHeader("OCS-APIRequest", "true");
        get.setHeader("Accept", "application/json");

        HttpResponse res = http.execute(get);
        String body = res.getEntity() != null ? EntityUtils.toString(res.getEntity()) : "";

        // data 배열에서 첫 token 추출 (단순 파싱)
        // {"ocs":{"data":[{"token":"xxxx"}]}}
        int idx = body.indexOf("\"token\":\"");
        if (idx < 0) return null;
        int start = idx + 9;
        int end = body.indexOf("\"", start);
        if (end < 0) return null;

        return body.substring(start, end);
    }

    private String createPublicShareToken(String path) throws Exception {
        String url = base + "/ocs/v2.php/apps/files_sharing/api/v1/shares";

        HttpPost post = new HttpPost(url);
        post.setHeader("Authorization", authHeader);
        post.setHeader("OCS-APIRequest", "true");
        post.setHeader("Accept", "application/json");

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("path", path));
        params.add(new BasicNameValuePair("shareType", "3"));   // public link
        params.add(new BasicNameValuePair("permissions", "1")); // read-only
        post.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

        HttpResponse res = http.execute(post);
        int code = res.getStatusLine().getStatusCode();
        String body = res.getEntity() != null ? EntityUtils.toString(res.getEntity()) : "";

        if (code < 200 || code >= 300) {
            throw new RuntimeException("Create share fail: " + code + " body=" + body);
        }

        // token 추출
        int idx = body.indexOf("\"token\":\"");
        if (idx < 0) {
            throw new RuntimeException("Token not found in body=" + body);
        }
        int start = idx + 9;
        int end = body.indexOf("\"", start);

        return body.substring(start, end);
    }

    private String encode(String s) throws Exception {
        return java.net.URLEncoder.encode(s, "UTF-8").replace("+", "%20");
    }
    
    @Override
    public void shareToUser(String path, String targetUserId, int permissions) throws Exception {
        createShare(path, "0", targetUserId, permissions);
    }

    @Override
    public void shareToGroup(String path, String targetGroupId, int permissions) throws Exception {
        createShare(path, "1", targetGroupId, permissions);
    }

    private void createShare(String path, String shareType, String shareWith, int permissions) throws Exception {
        String url = base + "/ocs/v2.php/apps/files_sharing/api/v1/shares";

        HttpPost post = new HttpPost(url);
        post.setHeader("Authorization", authHeader);
        post.setHeader("OCS-APIRequest", "true");
        post.setHeader("Accept", "application/json");

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("path", path));
        params.add(new BasicNameValuePair("shareType", shareType));
        params.add(new BasicNameValuePair("shareWith", shareWith));
        params.add(new BasicNameValuePair("permissions", String.valueOf(permissions)));

        post.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

        HttpResponse res = http.execute(post);
        int code = res.getStatusLine().getStatusCode();
        String body = res.getEntity() != null ? EntityUtils.toString(res.getEntity()) : "";

        if (code < 200 || code >= 300) {
            throw new RuntimeException("Share fail: " + code + " body=" + body);
        }
    }
    
    /**
     * 정책 B:
     * - 사용자 공유(share_type=0)만 삭제
     * - 그룹공유(1), 공개링크(3), 이메일(4) 등은 유지
     */
    public void revokeUserSharesByPath(String davPath) throws Exception {
        List<Long> userShareIds = findShareIdsByPathUserOnly(davPath);

        for (Long id : userShareIds) {
            deleteShareById(id);
        }

        log.info("revokeUserSharesByPath done path={} userShareIds={}", davPath, userShareIds);
    }
    
    private List<Long> findShareIdsByPathUserOnly(String davPath) throws Exception {
        String url = base
                + "/ocs/v2.php/apps/files_sharing/api/v1/shares"
                + "?path=" + URLEncoder.encode(davPath, "UTF-8")
                + "&format=json";
  
        HttpGet get = new HttpGet(url);
        get.setHeader("Authorization", authHeader); // stderp App Password
        get.setHeader("OCS-APIRequest", "true");
  
        HttpResponse res = http.execute(get);
        int code = res.getStatusLine().getStatusCode();
        String body = EntityUtils.toString(res.getEntity(), StandardCharsets.UTF_8);
  
        if (code < 200 || code >= 300) {
            throw new RuntimeException("Share list fail: " + code + " body=" + body);
        }
  
        JsonNode data = new ObjectMapper()
                .readTree(body)
                .path("ocs")
                .path("data");
  
        List<Long> ids = new ArrayList<>();
        if (data.isArray()) {
            for (JsonNode s : data) {
                int shareType = s.path("share_type").asInt();
  
                // ✅ 사용자 공유만
                if (shareType == 0) {
                    ids.add(s.path("id").asLong());
                }
            }
        }
  
        return ids;
    }

    
    private void deleteShareById(Long id) throws Exception {
      String url = base
              + "/ocs/v2.php/apps/files_sharing/api/v1/shares/" + id;

      HttpDelete del = new HttpDelete(url);
      del.setHeader("Authorization", authHeader);
      del.setHeader("OCS-APIRequest", "true");

      HttpResponse res = http.execute(del);
      int code = res.getStatusLine().getStatusCode();
      String body = res.getEntity() != null
              ? EntityUtils.toString(res.getEntity(), StandardCharsets.UTF_8)
              : "";

      if (code < 200 || code >= 300) {
          throw new RuntimeException("Share delete fail: " + code + " id=" + id + " body=" + body);
      }
  }




}
