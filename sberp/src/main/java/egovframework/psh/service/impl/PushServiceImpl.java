package egovframework.psh.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import egovframework.psh.service.PushDTO;
import egovframework.psh.service.PushMapper;
import egovframework.psh.service.PushService;
import egovframework.psh.service.PushSub;
import egovframework.psh.util.VapidTokenFactory;
import egovframework.psh.util.WebPushCrypto;
import egovframework.rte.fdl.property.EgovPropertyService;
import lombok.extern.slf4j.Slf4j;

/**
 * 웹푸시 발송 구현.
 *
 * <p>푸시 서비스(FCM·Apple·Mozilla)로 보내는 요청은 구독마다 한 번씩 일어난다.
 * 본문은 RFC 8291 로 암호화되어 있어 푸시 서비스는 내용을 볼 수 없고, 브라우저의
 * 서비스워커에서만 복호화된다.
 *
 * <p>실패 처리
 *
 * <ul>
 *   <li>404 / 410 — 구독이 사라진 것이다. 즉시 만료 처리하고 다시 보내지 않는다.
 *   <li>401 / 403 — VAPID 키·subject 설정 오류다. 구독 문제가 아니므로 만료시키지 않는다.
 *   <li>그 외 — FAIL_CNT 를 올린다. 10회 연속 실패하면 구독을 접는다.
 * </ul>
 */
@Slf4j
@Service("PushService")
public class PushServiceImpl implements PushService {

  /** 푸시 서비스가 기기를 못 깨웠을 때 보관할 시간(초). */
  private static final int TTL_SEC = 86_400;

  private static final int HTTP_TIMEOUT_MS = 10_000;

  @Autowired
  private PushMapper pushMapper;

  @Resource(name = "propertiesService")
  private EgovPropertyService prop;

  /** null 필드는 빼서 보낸다. 페이로드가 한 레코드(4079바이트)를 넘지 않게 하기 위함. */
  private final ObjectMapper objectMapper =
      new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

  private final CloseableHttpClient http = HttpClients.createDefault();

  /**
   * 발송 전용 스레드. 업무 트랜잭션을 붙잡지 않기 위해 분리한다.
   * 큐가 가득 차면 호출 스레드가 직접 보낸다(CallerRuns) — 알림을 버리지 않기 위함.
   */
  private final ExecutorService sender = new ThreadPoolExecutor(1, 2, 60L, TimeUnit.SECONDS,
      new LinkedBlockingQueue<Runnable>(500), new SenderThreadFactory(),
      new ThreadPoolExecutor.CallerRunsPolicy());

  /** VAPID 개인키는 한 번만 파싱한다. 키 미설정이면 null 로 두고 발송 시점에 걸러낸다. */
  private volatile VapidTokenFactory vapid;
  private volatile boolean vapidInitialized;

  @Override
  public String getPublicKey() {
    return safeGet("Globals.push.vapid.publicKey");
  }

  @Override
  public boolean isConfigured() {
    return !StringUtils.isEmpty(getPublicKey())
        && !StringUtils.isEmpty(safeGet("Globals.push.vapid.privateKey"));
  }

  @Override
  public int subscribe(PushDTO.Subscribe req, String memId, String userAgent) throws Exception {
    if (req == null || StringUtils.isEmpty(req.getEndpoint()) || req.getKeys() == null
        || StringUtils.isEmpty(req.getKeys().getP256dh())
        || StringUtils.isEmpty(req.getKeys().getAuth())) {
      throw new IllegalArgumentException("구독 정보(endpoint, keys.p256dh, keys.auth)가 필요합니다.");
    }

    // 저장 전에 키를 실제로 파싱해 본다. 잘못된 값이 들어오면 발송 시점이 아니라 지금 걸러진다.
    byte[] p256dh = WebPushCrypto.fromBase64Url(req.getKeys().getP256dh());
    byte[] auth = WebPushCrypto.fromBase64Url(req.getKeys().getAuth());
    if (p256dh.length != 65 || auth.length < 16) {
      throw new IllegalArgumentException("구독 키 형식이 올바르지 않습니다.");
    }

    PushSub sub = new PushSub();
    sub.setMemId(memId);
    sub.setEndpoint(req.getEndpoint());
    sub.setEndpointHash(WebPushCrypto.sha256Hex(req.getEndpoint()));
    sub.setP256dh(req.getKeys().getP256dh());
    sub.setAuth(req.getKeys().getAuth());
    sub.setUserAgent(trim(userAgent, 500));

    int affected = pushMapper.upsertSub(sub);
    log.info("웹푸시 구독 등록: memId={}, endpoint={}", memId, shorten(req.getEndpoint()));
    return affected;
  }

  @Override
  public int unsubscribe(String endpoint, String memId) throws Exception {
    if (StringUtils.isEmpty(endpoint)) {
      throw new IllegalArgumentException("endpoint 가 필요합니다.");
    }
    return pushMapper.deleteSub(WebPushCrypto.sha256Hex(endpoint), memId);
  }

  @Override
  public boolean isSubscribed(String memId) {
    return pushMapper.countByMemId(memId) > 0;
  }

  @Override
  public int sendToMember(String memId, PushDTO.Message message) {
    if (StringUtils.isEmpty(memId)) {
      return 0;
    }
    return send(pushMapper.selectByMemId(memId), message);
  }

  @Override
  public int sendToMembers(List<String> memIds, PushDTO.Message message) {
    List<String> targets = distinctNonEmpty(memIds);
    if (targets.isEmpty()) {
      return 0;
    }
    return send(pushMapper.selectByMemIds(targets), message);
  }

  @Override
  public int sendToAll(PushDTO.Message message, String exceptMemId) {
    return send(pushMapper.selectAllActive(exceptMemId), message);
  }

  @Override
  public void sendToMemberAsync(final String memId, final PushDTO.Message message) {
    submit(new Runnable() {
      @Override
      public void run() {
        sendToMember(memId, message);
      }
    });
  }

  @Override
  public void sendToMembersAsync(final List<String> memIds, final PushDTO.Message message) {
    submit(new Runnable() {
      @Override
      public void run() {
        sendToMembers(memIds, message);
      }
    });
  }

  @Override
  public void sendToAllAsync(final PushDTO.Message message, final String exceptMemId) {
    submit(new Runnable() {
      @Override
      public void run() {
        sendToAll(message, exceptMemId);
      }
    });
  }

  private void submit(Runnable task) {
    try {
      sender.execute(task);
    } catch (Exception e) {
      // 알림 실패가 업무 처리를 막아서는 안 된다.
      log.warn("웹푸시 비동기 발송 등록 실패", e);
    }
  }

  private int send(List<PushSub> subs, PushDTO.Message message) {
    if (subs == null || subs.isEmpty()) {
      return 0;
    }
    VapidTokenFactory tokenFactory = vapid();
    if (tokenFactory == null) {
      log.warn("VAPID 키 미설정 — 웹푸시를 보내지 않는다. GLOBALS_PUSH_VAPID_PUBLIC/PRIVATE 확인");
      return 0;
    }

    byte[] payload;
    try {
      payload = objectMapper.writeValueAsBytes(message);
    } catch (Exception e) {
      log.error("웹푸시 페이로드 직렬화 실패", e);
      return 0;
    }

    String publicKey = getPublicKey();
    int success = 0;
    for (PushSub sub : subs) {
      if (sendOne(sub, payload, tokenFactory, publicKey)) {
        success++;
      }
    }
    log.info("웹푸시 발송: 대상 {}건, 성공 {}건, title={}", subs.size(), success, message.getTitle());
    return success;
  }

  private boolean sendOne(PushSub sub, byte[] payload, VapidTokenFactory tokenFactory,
      String publicKey) {
    try {
      byte[] body = WebPushCrypto.encrypt(payload,
          WebPushCrypto.fromBase64Url(sub.getP256dh()),
          WebPushCrypto.fromBase64Url(sub.getAuth()));

      HttpPost post = new HttpPost(sub.getEndpoint());
      post.setConfig(RequestConfig.custom()
          .setConnectTimeout(HTTP_TIMEOUT_MS)
          .setSocketTimeout(HTTP_TIMEOUT_MS)
          .setConnectionRequestTimeout(HTTP_TIMEOUT_MS)
          .build());
      post.setHeader("TTL", String.valueOf(TTL_SEC));
      post.setHeader("Urgency", "normal");
      post.setHeader("Content-Encoding", "aes128gcm");
      post.setHeader("Content-Type", "application/octet-stream");
      post.setHeader("Authorization",
          "vapid t=" + tokenFactory.tokenFor(sub.getEndpoint()) + ", k=" + publicKey);
      post.setEntity(new ByteArrayEntity(body));

      try (CloseableHttpResponse res = http.execute(post)) {
        int sc = res.getStatusLine().getStatusCode();
        if (sc >= 200 && sc < 300) {
          pushMapper.updateSendSuccess(sub.getPushSeq());
          return true;
        }

        String detail = "HTTP " + sc + " " + shorten(readBody(res));
        if (sc == 404 || sc == 410) {
          // 구독이 사라졌다(앱 삭제·브라우저 데이터 삭제·구독 갱신). 재시도 대상이 아니다.
          pushMapper.expireSub(sub.getEndpointHash(), detail);
          log.info("웹푸시 구독 만료 처리: memId={}, {}", sub.getMemId(), detail);
        } else if (sc == 401 || sc == 403) {
          // 서버 설정 문제다. 구독을 접으면 사용자가 재구독해도 계속 실패한다.
          log.error("웹푸시 VAPID 인증 거부 — 키·subject 설정 확인 필요: {}", detail);
        } else {
          pushMapper.updateSendFail(sub.getPushSeq(), trim(detail, 500));
          log.warn("웹푸시 발송 실패: memId={}, {}", sub.getMemId(), detail);
        }
        return false;
      }
    } catch (Exception e) {
      pushMapper.updateSendFail(sub.getPushSeq(), trim(String.valueOf(e.getMessage()), 500));
      log.warn("웹푸시 발송 오류: memId={}, endpoint={}", sub.getMemId(), shorten(sub.getEndpoint()), e);
      return false;
    }
  }

  /** VAPID 키는 서버 기동 후 바뀌지 않는다. 최초 1회만 파싱하고 실패해도 재시도하지 않는다. */
  private VapidTokenFactory vapid() {
    if (vapidInitialized) {
      return vapid;
    }
    synchronized (this) {
      if (vapidInitialized) {
        return vapid;
      }
      vapidInitialized = true;
      String privateKey = safeGet("Globals.push.vapid.privateKey");
      String subject = safeGet("Globals.push.vapid.subject");
      if (StringUtils.isEmpty(privateKey) || StringUtils.isEmpty(getPublicKey())) {
        return null;
      }
      try {
        vapid = new VapidTokenFactory(privateKey,
            StringUtils.isEmpty(subject) ? "mailto:admin@standardbank.co.kr" : subject);
      } catch (Exception e) {
        log.error("VAPID 개인키 파싱 실패 — GLOBALS_PUSH_VAPID_PRIVATE 확인", e);
      }
      return vapid;
    }
  }

  @PreDestroy
  public void shutdown() {
    sender.shutdown();
    try {
      http.close();
    } catch (Exception ignore) {
      // 종료 중 오류는 남길 의미가 없다.
    }
  }

  private String readBody(CloseableHttpResponse res) {
    try {
      return res.getEntity() == null ? "" : EntityUtils.toString(res.getEntity(), "UTF-8");
    } catch (Exception e) {
      return "";
    }
  }

  private List<String> distinctNonEmpty(List<String> memIds) {
    List<String> out = new ArrayList<>();
    if (memIds == null) {
      return out;
    }
    for (String memId : memIds) {
      if (!StringUtils.isEmpty(memId) && !out.contains(memId)) {
        out.add(memId);
      }
    }
    return out;
  }

  private String safeGet(String key) {
    try {
      return prop.getString(key);
    } catch (Exception e) {
      return "";
    }
  }

  private String trim(String text, int max) {
    if (text == null) {
      return null;
    }
    return text.length() <= max ? text : text.substring(0, max);
  }

  private String shorten(String text) {
    return trim(text, 120);
  }

  private static final class SenderThreadFactory implements ThreadFactory {
    @Override
    public Thread newThread(Runnable r) {
      Thread t = Executors.defaultThreadFactory().newThread(r);
      t.setName("web-push-sender");
      t.setDaemon(true);
      return t;
    }
  }
}
