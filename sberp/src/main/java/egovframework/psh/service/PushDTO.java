package egovframework.psh.service;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public class PushDTO {

  /**
   * 구독 등록·해지 요청.
   *
   * <p>브라우저의 {@code PushSubscription.toJSON()} 결과를 그대로 받는 형태다.
   * 프론트는 가공 없이 {@code JSON.stringify(subscription)} 만 보내면 된다.
   */
  @Getter
  @Setter
  @ToString(exclude = "keys")
  @ApiModel(value = "PushDTO.Subscribe", description = "웹푸시 구독 등록")
  public static class Subscribe {

    @ApiModelProperty(value = "푸시 서비스 엔드포인트", required = true,
        example = "https://fcm.googleapis.com/fcm/send/eQ8...")
    private String endpoint;

    @ApiModelProperty(value = "구독 키", required = true)
    private Keys keys;
  }

  @Getter
  @Setter
  @ToString
  @ApiModel(value = "PushDTO.Unsubscribe", description = "웹푸시 구독 해지")
  public static class Unsubscribe {

    @ApiModelProperty(value = "해지할 엔드포인트", required = true,
        example = "https://fcm.googleapis.com/fcm/send/eQ8...")
    private String endpoint;
  }

  /** 프론트가 알림 UI 를 그리기 전에 받아가는 설정. */
  @Getter
  @Setter
  @ToString
  @ApiModel(value = "PushDTO.Config", description = "웹푸시 설정")
  public static class Config {

    @ApiModelProperty(value = "서버에 VAPID 키가 설정되어 있는지", example = "true")
    private boolean configured;

    @ApiModelProperty(value = "VAPID 공개키(base64url). applicationServerKey 로 쓴다", example = "BN...")
    private String publicKey;

    @ApiModelProperty(value = "이 사용자에게 살아 있는 구독이 있는지", example = "false")
    private boolean subscribed;
  }

  @Getter
  @Setter
  @ApiModel(value = "PushDTO.Keys", description = "구독 키")
  public static class Keys {

    @ApiModelProperty(value = "구독 공개키(base64url, 65바이트)", required = true, example = "BEl...")
    private String p256dh;

    @ApiModelProperty(value = "구독 인증 시크릿(base64url, 16바이트)", required = true, example = "tBH...")
    private String auth;
  }

  /**
   * 발송할 알림 내용.
   *
   * <p>이 객체가 JSON 으로 직렬화되어 암호화된 뒤 서비스워커의 {@code push} 이벤트로 전달된다.
   * 서비스워커는 이 필드들을 그대로 {@code showNotification} 에 넘긴다.
   */
  @Getter
  @Setter
  @ToString
  @ApiModel(value = "PushDTO.Message", description = "알림 내용")
  public static class Message {

    @ApiModelProperty(value = "알림 제목", required = true, example = "제품사진 업로드 요청")
    private String title;

    @ApiModelProperty(value = "알림 본문", example = "SB2609-B0012 신청서의 제품사진을 등록해 주세요.")
    private String body;

    @ApiModelProperty(value = "알림 클릭 시 열 경로. 모바일 SPA 딥링크",
        example = "/mErp/#/sbk/SB2609-B0012/photo")
    private String url;

    @ApiModelProperty(value = "묶음 키. 같은 tag 는 덮어써서 알림이 쌓이지 않는다", example = "sbk-SB2609-B0012")
    private String tag;
  }
}
