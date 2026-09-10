-- 웹푸시(Web Push) 구독 정보
--
-- 브라우저의 PushSubscription 을 사용자별로 저장한다.
-- 한 사람이 폰·PC·태블릿을 각각 홈화면에 추가하면 기기 수만큼 행이 생긴다.
--
-- ENDPOINT 는 푸시 서비스(FCM / Apple / Mozilla) 가 발급한 URL 이며 500자를 넘길 수 있다.
-- utf8 인덱스는 767바이트 제한이 있어 ENDPOINT 자체에는 UNIQUE 를 걸지 못한다.
-- 대신 SHA-256 해시(ENDPOINT_HASH)에 UNIQUE 를 걸어 같은 구독의 중복 등록을 막는다.
--
-- 구독 만료(HTTP 404/410)는 소프트 삭제(STATE='D') 로 처리한다.
-- 같은 기기가 재구독하면 같은 ENDPOINT_HASH 로 되살아난다(ON DUPLICATE KEY UPDATE).

CREATE TABLE IF NOT EXISTS `PUSH_SUB_TB` (
  `PUSH_SEQ`      bigint       NOT NULL AUTO_INCREMENT,
  `MEM_ID`        varchar(20)  NOT NULL              COMMENT '구독자. MEMBER_TB.ID',
  `ENDPOINT`      varchar(1000) NOT NULL             COMMENT '푸시 서비스 엔드포인트 URL',
  `ENDPOINT_HASH` char(64)     NOT NULL              COMMENT 'ENDPOINT 의 SHA-256 hex. 중복 구독 방지용',
  `P256DH`        varchar(255) NOT NULL              COMMENT '구독 공개키(base64url). 65바이트 비압축 P-256',
  `AUTH`          varchar(64)  NOT NULL              COMMENT '구독 인증 시크릿(base64url). 16바이트',
  `USER_AGENT`    varchar(500) DEFAULT NULL          COMMENT '등록 시점 UA. 기기 구분용',
  `FAIL_CNT`      int          NOT NULL DEFAULT 0    COMMENT '연속 발송 실패 횟수',
  `LAST_ERR`      varchar(500) DEFAULT NULL          COMMENT '마지막 실패 사유',
  `LAST_SENT_DT`  datetime     DEFAULT NULL          COMMENT '마지막 발송 성공 시각',
  `INS_DT`        datetime     DEFAULT CURRENT_TIMESTAMP,
  `UDT_DT`        datetime     DEFAULT CURRENT_TIMESTAMP,
  `STATE`         varchar(1)   NOT NULL DEFAULT 'I'  COMMENT 'I=사용, D=만료·해지',
  PRIMARY KEY (`PUSH_SEQ`),
  UNIQUE KEY `UK_PUSH_SUB_ENDPOINT` (`ENDPOINT_HASH`),
  KEY `IDX_PUSH_SUB_MEM` (`MEM_ID`,`STATE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='웹푸시 구독';
