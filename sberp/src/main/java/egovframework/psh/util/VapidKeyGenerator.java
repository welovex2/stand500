package egovframework.psh.util;

import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

/**
 * VAPID 키쌍 생성기 — 한 번만 실행해서 나온 값을 properties 에 넣는다.
 *
 * <p>실행: Eclipse 에서 이 클래스를 Java Application 으로 Run.
 *
 * <pre>
 * GLOBALS_PUSH_VAPID_PUBLIC=BN...    ← 프론트에도 그대로 전달된다(공개해도 되는 값)
 * GLOBALS_PUSH_VAPID_PRIVATE=...     ← 서버에만 둔다. Git 에 올리지 않는다
 * </pre>
 *
 * <p>키를 바꾸면 기존 구독은 전부 무효가 된다(푸시 서비스가 401 을 준다).
 * 교체 시에는 PUSH_SUB_TB 를 비우고 사용자에게 재구독을 받아야 한다.
 */
public final class VapidKeyGenerator {

  private VapidKeyGenerator() {}

  public static void main(String[] args) throws Exception {
    KeyPair pair = WebPushCrypto.generateKeyPair();

    byte[] publicPoint = WebPushCrypto.toUncompressedPoint((ECPublicKey) pair.getPublic());
    byte[] privateScalar = toFixed32(((ECPrivateKey) pair.getPrivate()).getS().toByteArray());

    System.out.println("GLOBALS_PUSH_VAPID_PUBLIC=" + WebPushCrypto.base64Url(publicPoint));
    System.out.println("GLOBALS_PUSH_VAPID_PRIVATE=" + WebPushCrypto.base64Url(privateScalar));
  }

  /** BigInteger.toByteArray 는 부호 바이트 때문에 33바이트가 되기도 한다. 32바이트로 맞춘다. */
  private static byte[] toFixed32(byte[] raw) {
    byte[] out = new byte[32];
    if (raw.length >= 32) {
      System.arraycopy(raw, raw.length - 32, out, 0, 32);
    } else {
      System.arraycopy(raw, 0, out, 32 - raw.length, raw.length);
    }
    return out;
  }
}
