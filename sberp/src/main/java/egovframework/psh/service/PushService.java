package egovframework.psh.service;

import java.util.List;

/**
 * 웹푸시 발송·구독 관리.
 *
 * <p>업무 코드에서는 {@link #sendToMemberAsync(String, PushDTO.Message)} 만 쓰면 된다.
 * 푸시 서비스 호출은 외부 HTTP 라 느리고 실패할 수 있으므로, 업무 트랜잭션을 붙잡지 않도록
 * 비동기 메서드를 기본으로 쓴다.
 */
public interface PushService {

  /** 프론트가 {@code PushManager.subscribe()} 에 넘길 VAPID 공개키(base64url). */
  String getPublicKey();

  /** 서버에 VAPID 키가 설정되어 있는지. 미설정이면 프론트는 알림 UI 를 감춘다. */
  boolean isConfigured();

  int subscribe(PushDTO.Subscribe req, String memId, String userAgent) throws Exception;

  int unsubscribe(String endpoint, String memId) throws Exception;

  /** 이 사용자에게 살아 있는 구독이 하나라도 있는지. */
  boolean isSubscribed(String memId);

  /**
   * 한 사용자의 모든 기기로 발송한다(동기).
   *
   * @return 발송에 성공한 기기 수
   */
  int sendToMember(String memId, PushDTO.Message message);

  /** 여러 사용자에게 발송한다(동기). */
  int sendToMembers(List<String> memIds, PushDTO.Message message);

  /**
   * 구독 중인 전 사용자에게 발송한다(동기). 공지사항처럼 수신자를 특정하지 않는 알림용.
   *
   * @param exceptMemId 제외할 사용자(보통 작성자 본인). 없으면 null
   */
  int sendToAll(PushDTO.Message message, String exceptMemId);

  /** 업무 로직에서 쓰는 발송. 별도 스레드로 넘기고 즉시 반환한다. */
  void sendToMemberAsync(String memId, PushDTO.Message message);

  void sendToMembersAsync(List<String> memIds, PushDTO.Message message);

  void sendToAllAsync(PushDTO.Message message, String exceptMemId);
}
