package egovframework.psh.util;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 웹푸시 페이로드 암호화 — RFC 8291 (Message Encryption for Web Push) + RFC 8188 (aes128gcm).
 *
 * <p>외부 라이브러리를 쓰지 않는다. Java 8 표준 JCE(SunEC + AES-GCM + HmacSHA256) 만으로 충분하다.
 * eGov 3.10 / Spring 4.3 스택에 BouncyCastle·Netty 계열 의존성을 새로 끌어오지 않기 위함이다.
 *
 * <p>암호화 흐름
 *
 * <pre>
 * 서버 임시키(as) 생성  →  ECDH(as_private, 구독 공개키)  →  ecdh_secret
 * PRK_key = HMAC(auth_secret, ecdh_secret)
 * IKM     = HKDF-Expand(PRK_key, "WebPush: info" 0x00 ua_public as_public, 32)
 * PRK     = HMAC(salt, IKM)
 * CEK     = HKDF-Expand(PRK, "Content-Encoding: aes128gcm" 0x00, 16)
 * NONCE   = HKDF-Expand(PRK, "Content-Encoding: nonce"     0x00, 12)
 * body    = salt(16) | rs(4) | idlen(1)=65 | as_public(65) | AES-128-GCM(평문 | 0x02)
 * </pre>
 */
public final class WebPushCrypto {

  /** 레코드 크기. 페이로드가 한 레코드에 들어가야 한다(평문 + 패딩1 + GCM태그16 &lt;= RECORD_SIZE). */
  public static final int RECORD_SIZE = 4096;

  private static final int MAX_PLAINTEXT = RECORD_SIZE - 17;
  private static final String CURVE = "secp256r1";
  private static final SecureRandom RANDOM = new SecureRandom();

  private static volatile ECParameterSpec p256;

  private WebPushCrypto() {}

  /** P-256 곡선 파라미터. KeyFactory 로 raw 좌표를 키 객체로 만들 때 필요하다. */
  public static ECParameterSpec p256() throws Exception {
    ECParameterSpec cached = p256;
    if (cached == null) {
      synchronized (WebPushCrypto.class) {
        cached = p256;
        if (cached == null) {
          AlgorithmParameters params = AlgorithmParameters.getInstance("EC");
          params.init(new ECGenParameterSpec(CURVE));
          cached = params.getParameterSpec(ECParameterSpec.class);
          p256 = cached;
        }
      }
    }
    return cached;
  }

  public static KeyPair generateKeyPair() throws Exception {
    KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
    gen.initialize(new ECGenParameterSpec(CURVE), RANDOM);
    return gen.generateKeyPair();
  }

  /** 공개키 → 비압축 포인트 65바이트 (0x04 | X(32) | Y(32)). 브라우저·VAPID 가 쓰는 형식이다. */
  public static byte[] toUncompressedPoint(ECPublicKey key) {
    byte[] x = toFixedLength(key.getW().getAffineX(), 32);
    byte[] y = toFixedLength(key.getW().getAffineY(), 32);
    byte[] out = new byte[65];
    out[0] = 0x04;
    System.arraycopy(x, 0, out, 1, 32);
    System.arraycopy(y, 0, out, 33, 32);
    return out;
  }

  /** 비압축 포인트 65바이트 → 공개키. */
  public static PublicKey toPublicKey(byte[] uncompressed) throws Exception {
    if (uncompressed == null || uncompressed.length != 65 || uncompressed[0] != 0x04) {
      throw new IllegalArgumentException("P-256 비압축 공개키(65바이트)가 아님");
    }
    BigInteger x = new BigInteger(1, Arrays.copyOfRange(uncompressed, 1, 33));
    BigInteger y = new BigInteger(1, Arrays.copyOfRange(uncompressed, 33, 65));
    return KeyFactory.getInstance("EC")
        .generatePublic(new ECPublicKeySpec(new ECPoint(x, y), p256()));
  }

  /** 스칼라 32바이트 → 개인키. VAPID 개인키를 properties 에서 읽을 때 쓴다. */
  public static PrivateKey toPrivateKey(byte[] scalar) throws Exception {
    if (scalar == null || scalar.length == 0 || scalar.length > 32) {
      throw new IllegalArgumentException(
          "P-256 개인키 스칼라 길이 오류: " + (scalar == null ? 0 : scalar.length));
    }
    return KeyFactory.getInstance("EC")
        .generatePrivate(new ECPrivateKeySpec(new BigInteger(1, scalar), p256()));
  }

  /**
   * 푸시 본문을 암호화한다.
   *
   * @param plaintext 구독자에게 보낼 평문(JSON)
   * @param uaPublicKey 구독의 p256dh — 비압축 포인트 65바이트
   * @param authSecret 구독의 auth — 16바이트
   * @return HTTP 본문에 그대로 실을 aes128gcm 바이트열
   */
  public static byte[] encrypt(byte[] plaintext, byte[] uaPublicKey, byte[] authSecret)
      throws Exception {
    if (plaintext.length > MAX_PLAINTEXT) {
      throw new IllegalArgumentException(
          "푸시 페이로드가 너무 큼: " + plaintext.length + " > " + MAX_PLAINTEXT);
    }
    if (authSecret == null || authSecret.length < 16) {
      throw new IllegalArgumentException("구독 auth 시크릿은 16바이트여야 함");
    }

    KeyPair server = generateKeyPair();
    byte[] asPublic = toUncompressedPoint((ECPublicKey) server.getPublic());

    byte[] salt = new byte[16];
    RANDOM.nextBytes(salt);

    KeyAgreement ka = KeyAgreement.getInstance("ECDH");
    ka.init(server.getPrivate());
    ka.doPhase(toPublicKey(uaPublicKey), true);
    byte[] ecdhSecret = ka.generateSecret();

    byte[] prkKey = hmac(authSecret, ecdhSecret);
    byte[] keyInfo = concat(ascii("WebPush: info"), new byte[] {0}, uaPublicKey, asPublic);
    byte[] ikm = hkdfExpand(prkKey, keyInfo, 32);

    byte[] prk = hmac(salt, ikm);
    byte[] cek = hkdfExpand(prk, concat(ascii("Content-Encoding: aes128gcm"), new byte[] {0}), 16);
    byte[] nonce = hkdfExpand(prk, concat(ascii("Content-Encoding: nonce"), new byte[] {0}), 12);

    // 단일 레코드이므로 마지막 레코드 구분자 0x02 를 평문 뒤에 붙인다.
    byte[] padded = concat(plaintext, new byte[] {0x02});

    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    cipher.init(
        Cipher.ENCRYPT_MODE, new SecretKeySpec(cek, "AES"), new GCMParameterSpec(128, nonce));
    byte[] ciphertext = cipher.doFinal(padded);

    ByteBuffer body = ByteBuffer.allocate(16 + 4 + 1 + asPublic.length + ciphertext.length);
    body.put(salt);
    body.putInt(RECORD_SIZE);
    body.put((byte) asPublic.length);
    body.put(asPublic);
    body.put(ciphertext);
    return body.array();
  }

  public static byte[] hmac(byte[] key, byte[] data) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(key, "HmacSHA256"));
    return mac.doFinal(data);
  }

  /** HKDF-Expand. 필요한 길이가 32바이트 이하라 T(1) 한 번이면 충분하다. */
  public static byte[] hkdfExpand(byte[] prk, byte[] info, int length) throws Exception {
    if (length > 32) {
      throw new IllegalArgumentException("HKDF 출력은 32바이트 이하만 지원");
    }
    byte[] t = hmac(prk, concat(info, new byte[] {0x01}));
    return Arrays.copyOf(t, length);
  }

  public static String sha256Hex(String text) throws Exception {
    byte[] digest =
        MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
    StringBuilder sb = new StringBuilder(digest.length * 2);
    for (byte b : digest) {
      sb.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
    }
    return sb.toString();
  }

  /** 패딩 없는 base64url. 브라우저 PushSubscription 과 VAPID 헤더가 쓰는 형식이다. */
  public static String base64Url(byte[] raw) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
  }

  /** base64url·base64 어느 쪽으로 와도 받아준다. 브라우저마다 표기가 다르다. */
  public static byte[] fromBase64Url(String text) {
    if (text == null) {
      throw new IllegalArgumentException("base64 값이 비어 있음");
    }
    String normalized = text.trim().replace('+', '-').replace('/', '_');
    int pad = normalized.indexOf('=');
    if (pad >= 0) {
      normalized = normalized.substring(0, pad);
    }
    return Base64.getUrlDecoder().decode(normalized);
  }

  private static byte[] ascii(String text) {
    return text.getBytes(StandardCharsets.US_ASCII);
  }

  private static byte[] concat(byte[]... parts) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    for (byte[] part : parts) {
      out.write(part, 0, part.length);
    }
    return out.toByteArray();
  }

  /** BigInteger 를 부호비트 없이 고정 길이로 만든다. 앞에 0x00 이 붙거나 짧게 나오는 경우를 보정. */
  private static byte[] toFixedLength(BigInteger value, int length) {
    byte[] raw = value.toByteArray();
    if (raw.length == length) {
      return raw;
    }
    byte[] out = new byte[length];
    if (raw.length > length) {
      System.arraycopy(raw, raw.length - length, out, 0, length);
    } else {
      System.arraycopy(raw, 0, out, length - raw.length, raw.length);
    }
    return out;
  }
}
