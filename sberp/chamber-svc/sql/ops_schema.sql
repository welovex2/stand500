-- =====================================================================
-- chamber-svc 용 ops 스키마 (MariaDB 10.1)
-- ERP 가 도는 모든 DB(로컬·개발·운영)에 실행한다.
-- ERP 의 측정 [시작]이 ops.CHAMBER_ETC_USE_TB 를 읽기 때문이다 (docs/design/chamber-erp.md §2.5).
-- 여러 번 실행해도 된다 (IF NOT EXISTS).
-- =====================================================================

CREATE DATABASE IF NOT EXISTS ops DEFAULT CHARACTER SET utf8mb4;

-- 로데이터 외 사용 (점검·디버깅·입회·예비시험)
-- ERP 가 USE_SEQ, ZONE_SEQ, PURPOSE, MEM_NAME, START_DT, END_DT, DEL_YN 을 읽는다. 이름과 뜻을 바꾸지 않는다.
CREATE TABLE IF NOT EXISTS ops.CHAMBER_ETC_USE_TB (
    USE_SEQ      INT          NOT NULL AUTO_INCREMENT,
    ZONE_SEQ     INT          NOT NULL COMMENT '구역 (erp.CHAMBER_ZONE_TB.ZONE_SEQ)',
    PURPOSE      VARCHAR(10)  NOT NULL COMMENT 'PRE 예비시험 / DEBUG 고객 디버깅 / CHECK 점검 / WITNESS 입회 / ETC 기타',
    TEST_SEQ     INT          NULL     COMMENT '관련 시험 (선택)',
    MEMO         VARCHAR(200) NULL     COMMENT '업체명·내용 등',
    MEM_ID       VARCHAR(20)  NOT NULL COMMENT '[사용] 누른 로그인 사용자',
    MEM_NAME     VARCHAR(50)  NULL,
    START_DT     DATETIME     NOT NULL COMMENT '서버시각',
    END_DT       DATETIME     NULL     COMMENT '서버시각. NULL = 사용 중',
    DEL_YN       TINYINT      NOT NULL DEFAULT 0,
    DEL_REASON   VARCHAR(200) NULL,
    DEL_MEM_ID   VARCHAR(20)  NULL,
    DEL_DT       DATETIME     NULL,
    INS_DT       DATETIME     NOT NULL,
    UDT_MEM_ID   VARCHAR(20)  NULL,
    UDT_DT       DATETIME     NULL,
    PRIMARY KEY (USE_SEQ),
    KEY IDX_CHAMBER_ETC_USE (ZONE_SEQ, START_DT),
    KEY IDX_CHAMBER_ETC_USE_END (END_DT)
) DEFAULT CHARSET=utf8mb4 COMMENT='챔버 사용 (로데이터 외)';

-- 현황판 로그인 실패
CREATE TABLE IF NOT EXISTS ops.LOGIN_FAIL_TB (
    MEM_ID       VARCHAR(20)  NOT NULL COMMENT '아이디',
    FAIL_CNT     TINYINT      NOT NULL DEFAULT 0 COMMENT '연속 실패 횟수',
    BLOCK_UNTIL  DATETIME     NULL     COMMENT '차단 해제 시각',
    UDT_DT       DATETIME     NOT NULL,
    PRIMARY KEY (MEM_ID)
) DEFAULT CHARSET=utf8mb4 COMMENT='현황판 로그인 실패';

-- 온습도 센서 기록
CREATE TABLE IF NOT EXISTS ops.DHT_LOG_TB (
    DHT_SEQ      BIGINT       NOT NULL AUTO_INCREMENT,
    SENSOR_ID    VARCHAR(20)  NOT NULL DEFAULT 'S1' COMMENT '센서 구분 (지금은 1대)',
    ZONE_SEQ     INT          NULL COMMENT '센서가 설치된 구역 (추후 챔버별 센서용)',
    TEMPERATURE  DECIMAL(5,2) NOT NULL,
    HUMIDITY     DECIMAL(5,2) NOT NULL,
    UPLOAD_TIME  DATETIME     NOT NULL,
    PRIMARY KEY (DHT_SEQ),
    KEY IDX_DHT_LOG (SENSOR_ID, UPLOAD_TIME)
) DEFAULT CHARSET=utf8mb4 COMMENT='온습도 센서 기록';

-- 확인
SHOW TABLES FROM ops;
