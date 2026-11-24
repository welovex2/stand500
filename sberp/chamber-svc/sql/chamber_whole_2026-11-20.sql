-- 현황판: 시험장 전체 점유 · 사유 변경 · CTI/4-39 · 기록 수정 이력 (기획 v7_1 SC-08 · SC-09 · SC-16 · X-5), 2026-11-20
-- 개발 DB 에 한 번 실행한다. 이미 실행했으면 다시 실행해도 된다 (IF NOT EXISTS).
-- 실행 (서버 호스트에서):
--   docker cp chamber_whole_2026-11-20.sql mariadb:/tmp/chamber_whole.sql
--   docker exec -it mariadb mysql --default-character-set=utf8 -u root -p erp -e "source /tmp/chamber_whole.sql"

ALTER TABLE CHAMBER_ETC_USE_TB
    ADD COLUMN IF NOT EXISTS WHOLE_YN TINYINT NOT NULL DEFAULT 0
        COMMENT '시험장 전체 점유 (시험번호 없이 사용 + 항목 선택 안 함). 1이면 ZONE_SEQ 는 그 챔버의 첫 구역이고 챔버의 모든 구역을 점유'
        AFTER WITNESS_NAME;

ALTER TABLE CHAMBER_ETC_USE_TB
    MODIFY PURPOSE VARCHAR(10) NOT NULL COMMENT 'TEST 시험(시험번호 있음) / NORD 로데이터 연동 없는 시험(CTI·4-39) / 시험번호 없이 사용 사유: PRE 사전 점검 / CHECK 장비 점검 / DEBUG 디버깅 / ETC 기타 (WITNESS 입회는 예전 값)';

-- 사용 이력 [수정] (기록 수정) 이력
CREATE TABLE IF NOT EXISTS CHAMBER_ETC_USE_HIS_TB (
    HIS_SEQ      INT           NOT NULL AUTO_INCREMENT COMMENT '수정 이력 SEQ',
    USE_SEQ      INT           NOT NULL COMMENT '사용 기록 (CHAMBER_ETC_USE_TB.USE_SEQ)',
    BEFORE_TXT   VARCHAR(1000) NOT NULL COMMENT '바꾸기 전 (시험번호 · 시험원 · 시작 ~ 종료)',
    AFTER_TXT    VARCHAR(1000) NOT NULL COMMENT '바꾼 뒤',
    REASON       VARCHAR(200)  NOT NULL COMMENT '수정 사유',
    MEM_ID       VARCHAR(20)   NOT NULL COMMENT '수정한 사람',
    INS_DT       DATETIME      NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (HIS_SEQ),
    KEY IDX_CHAMBER_ETC_USE_HIS (USE_SEQ)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='현황판 사용 기록 수정 이력 (기록 수정은 관리자 P01·P02, 기획 SC-16)';
GRANT SELECT, INSERT ON erp.CHAMBER_ETC_USE_HIS_TB TO 'chamber_svc'@'%';

-- 확인
SHOW COLUMNS FROM CHAMBER_ETC_USE_TB LIKE 'WHOLE_YN';
SHOW TABLES LIKE 'CHAMBER_ETC_USE_HIS_TB';
