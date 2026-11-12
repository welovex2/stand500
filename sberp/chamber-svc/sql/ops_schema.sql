-- =====================================================================
-- 온습도 센서 기록 (MariaDB 10.1, schema ops)
-- !! 이번 현황판에서는 쓰지 않는다. 이번 배포에서는 실행하지 않아도 된다. !!
-- 센서(ESP32) 수신을 다시 붙일 때를 위해 정의만 남겨 둔다.
-- chamber-svc 는 이번 범위에서 ops 스키마를 쓰지 않는다. chamber_svc 계정에도 ops 권한이 없다.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS ops DEFAULT CHARACTER SET utf8mb4;

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
