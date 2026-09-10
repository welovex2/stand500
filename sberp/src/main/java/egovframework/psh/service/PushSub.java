package egovframework.psh.service;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** PUSH_SUB_TB — 웹푸시 구독 한 건(= 사용자의 기기 한 대). */
@Getter
@Setter
@ToString(exclude = {"p256dh", "auth"})
@ApiModel(value = "PushSub", description = "웹푸시 구독")
public class PushSub {

  @ApiModelProperty(value = "구독 고유번호", example = "1")
  private Long pushSeq;

  @ApiModelProperty(value = "구독자 아이디", example = "system")
  private String memId;

  @ApiModelProperty(value = "푸시 서비스 엔드포인트", example = "https://fcm.googleapis.com/fcm/send/...")
  private String endpoint;

  @ApiModelProperty(value = "엔드포인트 SHA-256 hex", hidden = true)
  private String endpointHash;

  @ApiModelProperty(value = "구독 공개키(base64url)", hidden = true)
  private String p256dh;

  @ApiModelProperty(value = "구독 인증 시크릿(base64url)", hidden = true)
  private String auth;

  @ApiModelProperty(value = "등록 시점 User-Agent", example = "Mozilla/5.0 (iPhone; ...)")
  private String userAgent;

  @ApiModelProperty(value = "연속 발송 실패 횟수", example = "0")
  private int failCnt;

  @ApiModelProperty(value = "마지막 실패 사유", example = "")
  private String lastErr;

  @ApiModelProperty(value = "마지막 발송 성공 시각", example = "2026-09-10 09:00:00")
  private String lastSentDt;

  @ApiModelProperty(value = "등록일시", example = "2026-09-10 09:00:00")
  private String insDt;

  @ApiModelProperty(value = "수정일시", example = "2026-09-10 09:00:00")
  private String udtDt;

  @ApiModelProperty(value = "상태. I=사용, D=만료·해지", example = "I")
  private String state;
}
