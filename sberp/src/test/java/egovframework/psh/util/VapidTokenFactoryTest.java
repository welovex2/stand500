package egovframework.psh.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;
import org.junit.Test;

/** VAPID(RFC 8292) 토큰 검증. */
public class VapidTokenFactoryTest {

  private static final String SUBJECT = "mailto:admin@standardbank.co.kr";

  @Test
  public void audienceOf_keepsOriginOnly() {
    assertEquals("https://fcm.googleapis.com",
        VapidTokenFactory.audienceOf("https://fcm.googleapis.com/fcm/send/eQ8:APA91bH"));
    assertEquals("https://web.push.apple.com",
        VapidTokenFactory.audienceOf("https://web.push.apple.com/QKx/abc"));
    assertEquals("https://updates.push.services.mozilla.com",
        VapidTokenFactory.audienceOf("https://updates.push.services.mozilla.com/wpush/v2/gAA"));
    assertEquals("https://push.example.com:8443",
        VapidTokenFactory.audienceOf("https://push.example.com:8443/p/1"));
  }

  @Test
  public void token_isEs256JwtVerifiableWithPublicKey() throws Exception {
    KeyPair keys = WebPushCrypto.generateKeyPair();
    VapidTokenFactory factory = factoryFor(keys);

    String jwt = factory.tokenFor("https://fcm.googleapis.com/fcm/send/abc");
    String[] parts = jwt.split("\\.");
    assertEquals(3, parts.length);

    String header = new String(WebPushCrypto.fromBase64Url(parts[0]), StandardCharsets.UTF_8);
    String payload = new String(WebPushCrypto.fromBase64Url(parts[1]), StandardCharsets.UTF_8);
    assertTrue(header.contains("\"alg\":\"ES256\""));
    assertTrue(payload.contains("\"aud\":\"https://fcm.googleapis.com\""));
    assertTrue(payload.contains("\"sub\":\"" + SUBJECT + "\""));

    byte[] signature = WebPushCrypto.fromBase64Url(parts[2]);
    assertEquals("JWS ES256 서명은 R(32)|S(32) 고정 길이여야 한다", 64, signature.length);

    Signature verifier = Signature.getInstance("SHA256withECDSA");
    verifier.initVerify(keys.getPublic());
    verifier.update((parts[0] + "." + parts[1]).getBytes(StandardCharsets.US_ASCII));
    assertTrue(verifier.verify(joseToDer(signature)));
  }

  @Test
  public void token_expiresWithin24Hours() throws Exception {
    String payload = new String(
        WebPushCrypto.fromBase64Url(
            factoryFor(WebPushCrypto.generateKeyPair())
                .tokenFor("https://fcm.googleapis.com/fcm/send/abc")
                .split("\\.")[1]),
        StandardCharsets.UTF_8);

    long exp = Long.parseLong(payload.replaceAll(".*\"exp\":(\\d+).*", "$1"));
    long now = System.currentTimeMillis() / 1000L;
    // RFC 8292 는 24시간 이내를 요구한다. 넘으면 푸시 서비스가 401 을 준다.
    assertTrue(exp > now);
    assertTrue(exp - now <= 24 * 60 * 60);
  }

  @Test
  public void token_isCachedPerOriginNotPerEndpoint() throws Exception {
    VapidTokenFactory factory = factoryFor(WebPushCrypto.generateKeyPair());

    String first = factory.tokenFor("https://fcm.googleapis.com/fcm/send/device-1");
    String sameOrigin = factory.tokenFor("https://fcm.googleapis.com/fcm/send/device-2");
    String otherOrigin = factory.tokenFor("https://web.push.apple.com/QKx/abc");

    assertEquals(first, sameOrigin);
    assertNotEquals(first, otherOrigin);
  }

  /**
   * ECDSA 서명의 r·s 는 값에 따라 31바이트로 짧아지거나 부호 바이트가 붙어 33바이트가 된다.
   * 드물게 나오는 경우라 반복 서명으로 훑는다. 여기서 실패하면 특정 서명에서만 401 이 난다.
   */
  @Test
  public void derToJose_handlesShortAndPaddedIntegers() throws Exception {
    KeyPair keys = WebPushCrypto.generateKeyPair();

    for (int i = 0; i < 300; i++) {
      byte[] data = ("message-" + i).getBytes(StandardCharsets.US_ASCII);

      Signature signer = Signature.getInstance("SHA256withECDSA");
      signer.initSign(keys.getPrivate());
      signer.update(data);
      byte[] jose = VapidTokenFactory.derToJose(signer.sign());

      assertEquals(64, jose.length);

      Signature verifier = Signature.getInstance("SHA256withECDSA");
      verifier.initVerify(keys.getPublic());
      verifier.update(data);
      assertTrue("서명 " + i + " 의 DER→JOSE 변환이 깨졌다", verifier.verify(joseToDer(jose)));
    }
  }

  private VapidTokenFactory factoryFor(KeyPair keys) throws Exception {
    byte[] scalar = toFixed32(((ECPrivateKey) keys.getPrivate()).getS().toByteArray());
    // 공개키가 실제로 65바이트로 뽑히는지도 함께 확인해 둔다(헤더 k= 값으로 나간다).
    assertEquals(65, WebPushCrypto.toUncompressedPoint((ECPublicKey) keys.getPublic()).length);
    return new VapidTokenFactory(WebPushCrypto.base64Url(scalar), SUBJECT);
  }

  private static byte[] toFixed32(byte[] raw) {
    byte[] out = new byte[32];
    if (raw.length >= 32) {
      System.arraycopy(raw, raw.length - 32, out, 0, 32);
    } else {
      System.arraycopy(raw, 0, out, 32 - raw.length, raw.length);
    }
    return out;
  }

  /** 검증용 역변환. R(32)|S(32) → DER SEQUENCE. */
  private static byte[] joseToDer(byte[] jose) {
    byte[] r = new BigInteger(1, Arrays.copyOfRange(jose, 0, 32)).toByteArray();
    byte[] s = new BigInteger(1, Arrays.copyOfRange(jose, 32, 64)).toByteArray();

    byte[] der = new byte[6 + r.length + s.length];
    int at = 0;
    der[at++] = 0x30;
    der[at++] = (byte) (4 + r.length + s.length);
    der[at++] = 0x02;
    der[at++] = (byte) r.length;
    System.arraycopy(r, 0, der, at, r.length);
    at += r.length;
    der[at++] = 0x02;
    der[at++] = (byte) s.length;
    System.arraycopy(s, 0, der, at, s.length);
    return der;
  }
}
