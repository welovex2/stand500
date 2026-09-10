package egovframework.psh.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.junit.Test;

/**
 * RFC 8291 암호화 검증.
 *
 * <p>핵심 테스트는 라운드트립이다. 브라우저(수신자) 입장의 복호화를 여기서 다시 구현해
 * 원문이 복원되는지 본다. 수신측 유도식을 {@link WebPushCrypto} 를 호출하지 않고 따로 적었으므로,
 * 송신측 구현이 틀리면 복호화가 깨진다.
 */
public class WebPushCryptoTest {

  @Test
  public void encrypt_isDecryptableByReceiver() throws Exception {
    KeyPair ua = WebPushCrypto.generateKeyPair();
    byte[] uaPublic = WebPushCrypto.toUncompressedPoint((ECPublicKey) ua.getPublic());
    byte[] authSecret = new byte[16];
    new SecureRandom().nextBytes(authSecret);

    String plain = "{\"title\":\"제품사진 업로드 요청\",\"body\":\"SB2609-B0012\",\"url\":\"/mErp/\"}";

    byte[] body =
        WebPushCrypto.encrypt(plain.getBytes(StandardCharsets.UTF_8), uaPublic, authSecret);

    assertEquals(plain, decryptAsReceiver(body, ua, uaPublic, authSecret));
  }

  @Test
  public void encrypt_producesDifferentBytesEachCall() throws Exception {
    KeyPair ua = WebPushCrypto.generateKeyPair();
    byte[] uaPublic = WebPushCrypto.toUncompressedPoint((ECPublicKey) ua.getPublic());
    byte[] authSecret = new byte[16];
    new SecureRandom().nextBytes(authSecret);
    byte[] payload = "same payload".getBytes(StandardCharsets.UTF_8);

    // salt 와 서버 임시키가 매번 새로 생성되므로 같은 평문이라도 본문이 달라야 한다.
    byte[] first = WebPushCrypto.encrypt(payload, uaPublic, authSecret);
    byte[] second = WebPushCrypto.encrypt(payload, uaPublic, authSecret);
    assertTrue(!Arrays.equals(first, second));
  }

  @Test
  public void encrypt_headerLayoutMatchesRfc8188() throws Exception {
    KeyPair ua = WebPushCrypto.generateKeyPair();
    byte[] uaPublic = WebPushCrypto.toUncompressedPoint((ECPublicKey) ua.getPublic());
    byte[] authSecret = new byte[16];
    new SecureRandom().nextBytes(authSecret);

    byte[] body = WebPushCrypto.encrypt(new byte[] {1, 2, 3}, uaPublic, authSecret);

    ByteBuffer buf = ByteBuffer.wrap(body);
    buf.position(16);
    assertEquals(WebPushCrypto.RECORD_SIZE, buf.getInt());
    assertEquals(65, buf.get() & 0xFF);
    assertEquals(0x04, body[21]);
    // salt(16) + rs(4) + idlen(1) + 키(65) + 평문(3) + 구분자(1) + GCM 태그(16)
    assertEquals(16 + 4 + 1 + 65 + 3 + 1 + 16, body.length);
  }

  @Test
  public void encrypt_rejectsPayloadLargerThanOneRecord() throws Exception {
    KeyPair ua = WebPushCrypto.generateKeyPair();
    byte[] uaPublic = WebPushCrypto.toUncompressedPoint((ECPublicKey) ua.getPublic());
    byte[] authSecret = new byte[16];

    // 한 레코드에 담을 수 있는 최대치는 RECORD_SIZE - 17 이다.
    WebPushCrypto.encrypt(new byte[WebPushCrypto.RECORD_SIZE - 17], uaPublic, authSecret);
    try {
      WebPushCrypto.encrypt(new byte[WebPushCrypto.RECORD_SIZE - 16], uaPublic, authSecret);
      fail("한 레코드를 넘는 페이로드는 거부되어야 한다");
    } catch (IllegalArgumentException expected) {
      assertTrue(expected.getMessage().contains("너무 큼"));
    }
  }

  @Test
  public void publicKey_roundTripsThroughUncompressedPoint() throws Exception {
    for (int i = 0; i < 50; i++) {
      KeyPair pair = WebPushCrypto.generateKeyPair();
      byte[] point = WebPushCrypto.toUncompressedPoint((ECPublicKey) pair.getPublic());

      assertEquals(65, point.length);
      assertEquals(0x04, point[0]);
      // 좌표에 선행 0 이 있어도 32바이트로 채워져야 한다(짧게 나오면 복원이 깨진다).
      assertArrayEquals(point,
          WebPushCrypto.toUncompressedPoint((ECPublicKey) WebPushCrypto.toPublicKey(point)));
    }
  }

  @Test
  public void base64Url_acceptsBothAlphabetsAndMissingPadding() {
    byte[] raw = new byte[] {0, 1, 2, (byte) 250, (byte) 251, (byte) 252, (byte) 253};
    String encoded = WebPushCrypto.base64Url(raw);

    assertTrue(encoded.indexOf('=') < 0);
    assertArrayEquals(raw, WebPushCrypto.fromBase64Url(encoded));
    // 표준 base64(+/) 로 보내는 브라우저·패딩을 붙여 보내는 클라이언트도 받아준다.
    assertArrayEquals(raw, WebPushCrypto.fromBase64Url(encoded.replace('-', '+').replace('_', '/')));
    assertArrayEquals(raw, WebPushCrypto.fromBase64Url(encoded + "=="));
  }

  @Test
  public void sha256Hex_is64LowercaseHexChars() throws Exception {
    String hash = WebPushCrypto.sha256Hex("https://fcm.googleapis.com/fcm/send/abc");

    assertEquals(64, hash.length());
    assertTrue(hash.matches("[0-9a-f]{64}"));
    // 같은 엔드포인트는 항상 같은 해시여야 UNIQUE 키가 중복 구독을 막는다.
    assertEquals(hash, WebPushCrypto.sha256Hex("https://fcm.googleapis.com/fcm/send/abc"));
  }

  /** 브라우저가 하는 일을 그대로 재현한다. RFC 8291 3.4 수신자 유도. */
  private String decryptAsReceiver(byte[] body, KeyPair ua, byte[] uaPublic, byte[] authSecret)
      throws Exception {
    ByteBuffer buf = ByteBuffer.wrap(body);
    byte[] salt = new byte[16];
    buf.get(salt);
    buf.getInt();
    byte[] asPublic = new byte[buf.get() & 0xFF];
    buf.get(asPublic);
    byte[] ciphertext = new byte[buf.remaining()];
    buf.get(ciphertext);

    KeyAgreement ka = KeyAgreement.getInstance("ECDH");
    ka.init(ua.getPrivate());
    ka.doPhase(WebPushCrypto.toPublicKey(asPublic), true);

    byte[] prkKey = WebPushCrypto.hmac(authSecret, ka.generateSecret());
    byte[] ikm = WebPushCrypto.hkdfExpand(prkKey,
        concat(ascii("WebPush: info"), new byte[] {0}, uaPublic, asPublic), 32);
    byte[] prk = WebPushCrypto.hmac(salt, ikm);
    byte[] cek = WebPushCrypto.hkdfExpand(prk,
        concat(ascii("Content-Encoding: aes128gcm"), new byte[] {0}), 16);
    byte[] nonce = WebPushCrypto.hkdfExpand(prk,
        concat(ascii("Content-Encoding: nonce"), new byte[] {0}), 12);

    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(cek, "AES"),
        new GCMParameterSpec(128, nonce));
    byte[] decrypted = cipher.doFinal(ciphertext);

    assertEquals(0x02, decrypted[decrypted.length - 1]);
    return new String(Arrays.copyOf(decrypted, decrypted.length - 1), StandardCharsets.UTF_8);
  }

  private static byte[] ascii(String text) {
    return text.getBytes(StandardCharsets.US_ASCII);
  }

  private static byte[] concat(byte[]... parts) {
    int length = 0;
    for (byte[] part : parts) {
      length += part.length;
    }
    byte[] out = new byte[length];
    int at = 0;
    for (byte[] part : parts) {
      System.arraycopy(part, 0, out, at, part.length);
      at += part.length;
    }
    return out;
  }
}
