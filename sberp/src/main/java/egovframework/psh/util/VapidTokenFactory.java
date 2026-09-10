package egovframework.psh.util;

import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * VAPID(RFC 8292) 인증 토큰 생성기.
 *
 * <p>푸시 서비스(FCM·Apple·Mozilla)는 "누가 보낸 푸시인가"를 ES256 서명된 JWT 로 확인한다.
 * 요청 헤더는 아래 한 줄이다.
 *
 * <pre>
 * Authorization: vapid t=&lt;JWT&gt;, k=&lt;VAPID 공개키 base64url&gt;
 * </pre>
 *
 * <p>JWT 는 엔드포인트의 오리진(aud) 단위로만 달라지므로 오리진별로 캐시한다.
 * 수백 건을 한 번에 보낼 때 매 건 서명하는 비용을 없애기 위함이다.
 */
public class VapidTokenFactory {

  /** 토큰 유효기간. RFC 8292 는 24시간 이내를 요구한다. 여유를 두고 12시간. */
  private static final long TTL_SEC = 12 * 60 * 60L;

  /** 만료 1시간 전에 미리 새로 만든다. 경계에서 401 이 나지 않게 하기 위함. */
  private static final long RENEW_BEFORE_SEC = 60 * 60L;

  private static final String HEADER_B64 =
      WebPushCrypto.base64Url("{\"typ\":\"JWT\",\"alg\":\"ES256\"}".getBytes(StandardCharsets.UTF_8));

  private final PrivateKey privateKey;
  private final String subject;
  private final ConcurrentMap<String, CachedToken> cache = new ConcurrentHashMap<>();

  /**
   * @param privateKeyBase64Url VAPID 개인키(P-256 스칼라 32바이트, base64url)
   * @param subject 연락처. {@code mailto:} 또는 {@code https:} URL 이어야 한다
   */
  public VapidTokenFactory(String privateKeyBase64Url, String subject) throws Exception {
    this.privateKey = WebPushCrypto.toPrivateKey(WebPushCrypto.fromBase64Url(privateKeyBase64Url));
    this.subject = subject;
  }

  /** 엔드포인트 URL 에 맞는 JWT 를 돌려준다. 캐시가 살아 있으면 재사용한다. */
  public String tokenFor(String endpoint) throws Exception {
    String audience = audienceOf(endpoint);
    long now = System.currentTimeMillis() / 1000L;

    CachedToken cached = cache.get(audience);
    if (cached != null && cached.expiresAt - RENEW_BEFORE_SEC > now) {
      return cached.token;
    }

    long exp = now + TTL_SEC;
    String token = sign(audience, exp);
    cache.put(audience, new CachedToken(token, exp));
    return token;
  }

  /** 푸시 엔드포인트의 오리진. JWT 의 aud 는 경로를 제외한 scheme://host[:port] 다. */
  public static String audienceOf(String endpoint) {
    URI uri = URI.create(endpoint);
    StringBuilder sb = new StringBuilder(uri.getScheme()).append("://").append(uri.getHost());
    if (uri.getPort() > 0) {
      sb.append(':').append(uri.getPort());
    }
    return sb.toString();
  }

  private String sign(String audience, long exp) throws Exception {
    String payload =
        "{\"aud\":\"" + audience + "\",\"exp\":" + exp + ",\"sub\":\"" + subject + "\"}";
    String signingInput =
        HEADER_B64 + "." + WebPushCrypto.base64Url(payload.getBytes(StandardCharsets.UTF_8));

    Signature signature = Signature.getInstance("SHA256withECDSA");
    signature.initSign(privateKey);
    signature.update(signingInput.getBytes(StandardCharsets.US_ASCII));

    return signingInput + "." + WebPushCrypto.base64Url(derToJose(signature.sign()));
  }

  /**
   * JCA 의 ECDSA 서명은 DER {@code SEQUENCE { INTEGER r, INTEGER s }} 인데,
   * JWS ES256 은 고정 길이 {@code R(32) || S(32)} 를 요구한다.
   */
  static byte[] derToJose(byte[] der) {
    int offset = 0;
    if (der[offset++] != 0x30) {
      throw new IllegalArgumentException("ECDSA DER 서명이 아님");
    }
    int seqLen = der[offset++] & 0xFF;
    if (seqLen == 0x81) {
      // 길이가 128바이트를 넘으면 한 바이트가 더 붙는다. P-256 에서는 사실상 나오지 않지만 방어.
      offset++;
    }
    BigInteger r = readInteger(der, offset);
    offset += 2 + (der[offset + 1] & 0xFF);
    BigInteger s = readInteger(der, offset);

    byte[] out = new byte[64];
    copyFixed(r, out, 0);
    copyFixed(s, out, 32);
    return out;
  }

  private static BigInteger readInteger(byte[] der, int offset) {
    if (der[offset] != 0x02) {
      throw new IllegalArgumentException("ECDSA DER INTEGER 태그 오류");
    }
    int len = der[offset + 1] & 0xFF;
    return new BigInteger(Arrays.copyOfRange(der, offset + 2, offset + 2 + len));
  }

  private static void copyFixed(BigInteger value, byte[] out, int at) {
    byte[] raw = value.toByteArray();
    if (raw.length > 32) {
      // toByteArray 앞에 붙는 부호 바이트 0x00 제거
      System.arraycopy(raw, raw.length - 32, out, at, 32);
    } else {
      System.arraycopy(raw, 0, out, at + 32 - raw.length, raw.length);
    }
  }

  private static final class CachedToken {
    final String token;
    final long expiresAt;

    CachedToken(String token, long expiresAt) {
      this.token = token;
      this.expiresAt = expiresAt;
    }
  }
}
