-- =====================================================================
-- chamber-svc 개발용 시험 데이터 (MariaDB 10.1)
-- !! 로컬·개발 DB 전용. 운영 DB 에서 실행하지 않는다. !!
--
-- 챔버·구역 이름은 화면 확인용으로 지어낸 값이다. 실제 목록은 기획에서 받아 ERP 가 넣는다.
-- 여러 번 실행해도 된다 (IF NOT EXISTS, INSERT IGNORE). 지울 때는 맨 아래 "정리"를 쓴다.
--
-- 실행 순서: ops_schema.sql → 이 파일 → chamber_svc_user.sql
-- 실행 예 (개발서버):
--   docker compose exec -T mariadb mysql -uroot -p < chamber-svc/sql/dev_seed.sql
-- =====================================================================

USE erp;

-- ---------------------------------------------------------------------
-- 1. ERP 챔버 테이블 (임시)
--    정본은 ERP 의 src/main/resources/db/CHAMBER_TB.sql 이다 (docs/design/chamber-erp.md §3.1).
--    ERP 쪽 SQL 이 아직 없어서 같은 정의를 여기에 둔다. 이미 있으면 건너뛴다.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS CHAMBER_TB (
    CHAMBER_SEQ  INT          NOT NULL AUTO_INCREMENT COMMENT '챔버 SEQ',
    NAME         VARCHAR(50)  NOT NULL COMMENT '챔버 이름',
    SORT_NO      SMALLINT     NOT NULL DEFAULT 0 COMMENT '현황판 표시 순서',
    USE_YN       TINYINT      NOT NULL DEFAULT 1 COMMENT '사용 여부',
    INS_MEM_ID   VARCHAR(20)  NOT NULL,
    INS_DT       DATETIME     NOT NULL,
    UDT_MEM_ID   VARCHAR(20)  NULL,
    UDT_DT       DATETIME     NULL,
    PRIMARY KEY (CHAMBER_SEQ)
) COMMENT='챔버';

CREATE TABLE IF NOT EXISTS CHAMBER_ZONE_TB (
    ZONE_SEQ     INT          NOT NULL AUTO_INCREMENT COMMENT '구역 SEQ',
    CHAMBER_SEQ  INT          NOT NULL COMMENT '챔버 SEQ',
    NAME         VARCHAR(50)  NOT NULL COMMENT '구역 이름',
    MULTI_YN     TINYINT      NOT NULL DEFAULT 0 COMMENT '동시 사용 허용',
    SORT_NO      SMALLINT     NOT NULL DEFAULT 0,
    USE_YN       TINYINT      NOT NULL DEFAULT 1,
    INS_MEM_ID   VARCHAR(20)  NOT NULL,
    INS_DT       DATETIME     NOT NULL,
    UDT_MEM_ID   VARCHAR(20)  NULL,
    UDT_DT       DATETIME     NULL,
    PRIMARY KEY (ZONE_SEQ),
    KEY IDX_CHAMBER_ZONE (CHAMBER_SEQ, USE_YN, SORT_NO)
) COMMENT='챔버 구역 (사용 단위)';

CREATE TABLE IF NOT EXISTS CHAMBER_ZONE_MET_TB (
    ZONE_SEQ     INT          NOT NULL COMMENT '구역 SEQ',
    MET_KEY      VARCHAR(10)  NOT NULL COMMENT '시험방법 키',
    PRIMARY KEY (ZONE_SEQ, MET_KEY),
    KEY IDX_CHAMBER_ZONE_MET (MET_KEY)
) COMMENT='구역에서 할 수 있는 시험방법';

-- RAW_ENV_TB 에 구역 컬럼 (이미 있으면 건너뛴다)
ALTER TABLE RAW_ENV_TB
    ADD COLUMN IF NOT EXISTS ZONE_SEQ INT NULL COMMENT '시험 구역 (CHAMBER_ZONE_TB.ZONE_SEQ, 이관 행은 NULL)' AFTER TEST_E_DT,
    ADD INDEX IF NOT EXISTS IDX_RAW_ENV_ZONE (ZONE_SEQ, TEST_S_DT);

-- ---------------------------------------------------------------------
-- 2. 시험용 챔버·구역 (번호 9001~ 로 넣어 실제 데이터와 구분한다)
-- ---------------------------------------------------------------------
INSERT IGNORE INTO CHAMBER_TB (CHAMBER_SEQ, NAME, SORT_NO, INS_MEM_ID, INS_DT) VALUES
    (9001, '[시험용] 10m 챔버',   1, 'seed', NOW()),
    (9002, '[시험용] 3m 챔버',    2, 'seed', NOW()),
    (9003, '[시험용] 차폐실',     3, 'seed', NOW()),
    (9004, '[시험용] 일반 시험실', 4, 'seed', NOW()),
    (9005, '[시험용] 숨긴 챔버',   5, 'seed', NOW());
UPDATE CHAMBER_TB SET USE_YN = 0 WHERE CHAMBER_SEQ = 9005;   -- 현황판에 안 보여야 한다

INSERT IGNORE INTO CHAMBER_ZONE_TB (ZONE_SEQ, CHAMBER_SEQ, NAME, MULTI_YN, SORT_NO, INS_MEM_ID, INS_DT) VALUES
    (9001, 9001, 'RE',       0, 1, 'seed', NOW()),   -- 구역이 하나인 챔버
    (9002, 9002, 'RE',       0, 1, 'seed', NOW()),
    (9003, 9002, 'RS',       0, 2, 'seed', NOW()),
    (9004, 9003, 'CE #1',    0, 1, 'seed', NOW()),
    (9005, 9003, 'CE #2',    0, 2, 'seed', NOW()),
    (9006, 9003, 'CS #1',    0, 3, 'seed', NOW()),
    (9007, 9003, '숨긴 구역', 0, 4, 'seed', NOW()),
    (9008, 9004, 'ESD·EFT',  1, 1, 'seed', NOW()),   -- 동시 사용 허용
    (9009, 9005, '구역',     0, 1, 'seed', NOW());
UPDATE CHAMBER_ZONE_TB SET USE_YN = 0 WHERE ZONE_SEQ = 9007;  -- 현황판에 안 보여야 한다

INSERT IGNORE INTO CHAMBER_ZONE_MET_TB (ZONE_SEQ, MET_KEY) VALUES
    (9001, 'RE1'), (9001, 'RE2'), (9001, 'RE3'),
    (9002, 'RE1'), (9002, 'RE2'),
    (9003, 'RS'),
    (9004, 'CE'), (9005, 'CE'),
    (9006, 'CS'),
    (9008, 'ESD'), (9008, 'EFT'), (9008, 'SURGE'), (9008, 'MF'), (9008, 'VDIP');

-- ---------------------------------------------------------------------
-- 3. 시험용 로데이터 외 사용 (번호 9001~)
--    현황판에서 볼 수 있는 상태를 하나씩 만든다.
-- ---------------------------------------------------------------------
INSERT IGNORE INTO ops.CHAMBER_ETC_USE_TB
       (USE_SEQ, ZONE_SEQ, PURPOSE, MEMO, MEM_ID, MEM_NAME, START_DT, END_DT, INS_DT) VALUES
    -- 사용 중 (방금 시작)
    (9001, 9002, 'CHECK',   '분기 점검',        'seed', '시험용', DATE_SUB(NOW(), INTERVAL 20 MINUTE), NULL, NOW()),
    -- 사용 중 (12시간 넘김 → 강조돼야 한다)
    (9002, 9004, 'DEBUG',   '(주)가나다 디버깅', 'seed', '시험용', DATE_SUB(NOW(), INTERVAL 15 HOUR),   NULL, NOW()),
    -- 동시 사용 구역에 두 건
    (9003, 9008, 'PRE',     '예비시험 A',       'seed', '시험용', DATE_SUB(NOW(), INTERVAL 2 HOUR),    NULL, NOW()),
    (9004, 9008, 'WITNESS', '입회 B',           'seed', '시험용', DATE_SUB(NOW(), INTERVAL 1 HOUR),    NULL, NOW()),
    -- 종료된 기록 (이력 화면)
    (9005, 9001, 'CHECK',   '안테나 교정',      'seed', '시험용', DATE_SUB(NOW(), INTERVAL 27 HOUR), DATE_SUB(NOW(), INTERVAL 24 HOUR), NOW()),
    (9006, 9006, 'ETC',     NULL,               'seed', '시험용', DATE_SUB(NOW(), INTERVAL 50 HOUR), DATE_SUB(NOW(), INTERVAL 49 HOUR), NOW());
-- 삭제된 기록 (현황판·이력 어디에도 안 보여야 한다)
INSERT IGNORE INTO ops.CHAMBER_ETC_USE_TB
       (USE_SEQ, ZONE_SEQ, PURPOSE, MEMO, MEM_ID, MEM_NAME, START_DT, INS_DT, DEL_YN, DEL_REASON, DEL_MEM_ID, DEL_DT) VALUES
    (9007, 9001, 'ETC', '잘못 누름', 'seed', '시험용', DATE_SUB(NOW(), INTERVAL 5 MINUTE), NOW(), 1, '잘못 등록', 'seed', NOW());

-- ---------------------------------------------------------------------
-- 4. (선택) 진행 중인 측정 행 — 현황판에 "시험"으로 보이는지, 그 구역 [사용]이 거부되는지 확인용
--    실제 로데이터 하나에 측정 행을 붙이므로 필요할 때만 주석을 풀어 실행한다.
--    INSERT 트리거(RAW_ENV_INS_TG)가 이력을 남길 수 있다. 확인이 끝나면 "정리"로 지운다.
-- ---------------------------------------------------------------------
-- INSERT INTO RAW_ENV_TB (RAW_SEQ, MET_KEY, ROW_NO, TEST_S_DT, ZONE_SEQ, MEM_ID, MEM_NAME, INS_MEM_ID, INS_DT)
-- SELECT r.RAW_SEQ, 'RE1', 99, DATE_SUB(NOW(), INTERVAL 40 MINUTE), 9001, 'seed', '시험용', 'seed', NOW()
--   FROM RAW_TB r
--  ORDER BY r.RAW_SEQ DESC
--  LIMIT 1;

-- ---------------------------------------------------------------------
-- 확인: 현황판이 보여 줄 내용
--   기대: 10m RE = 비어 있음 / 3m RE = 점검 / 3m RS = 비어 있음 / 차폐실 CE #1 = 디버깅(강조)
--         차폐실 CE #2, CS #1 = 비어 있음 / 일반 시험실 = 2건 / 숨긴 챔버·숨긴 구역은 없음
-- ---------------------------------------------------------------------
SELECT c.NAME AS CHAMBER, z.NAME AS ZONE, z.MULTI_YN, x.PURPOSE, x.MEMO, x.START_DT,
       TIMESTAMPDIFF(MINUTE, x.START_DT, NOW()) AS ELAPSED_MIN
  FROM CHAMBER_TB c
       INNER JOIN CHAMBER_ZONE_TB z ON z.CHAMBER_SEQ = c.CHAMBER_SEQ AND z.USE_YN = 1
       LEFT JOIN ops.CHAMBER_ETC_USE_TB x ON x.ZONE_SEQ = z.ZONE_SEQ AND x.DEL_YN = 0 AND x.END_DT IS NULL
 WHERE c.USE_YN = 1
 ORDER BY c.SORT_NO, z.SORT_NO, x.START_DT;

-- ---------------------------------------------------------------------
-- 정리 (시험 데이터만 지운다. 필요할 때 주석을 풀어 실행)
-- ---------------------------------------------------------------------
-- DELETE FROM ops.CHAMBER_ETC_USE_TB WHERE ZONE_SEQ BETWEEN 9001 AND 9099;
-- DELETE FROM erp.RAW_ENV_TB          WHERE ZONE_SEQ BETWEEN 9001 AND 9099 AND INS_MEM_ID = 'seed';
-- DELETE FROM erp.CHAMBER_ZONE_MET_TB WHERE ZONE_SEQ BETWEEN 9001 AND 9099;
-- DELETE FROM erp.CHAMBER_ZONE_TB     WHERE ZONE_SEQ BETWEEN 9001 AND 9099;
-- DELETE FROM erp.CHAMBER_TB          WHERE CHAMBER_SEQ BETWEEN 9001 AND 9099;
-- 실제 목록을 넣기 전에 번호를 1부터 다시 시작하려면:
-- ALTER TABLE erp.CHAMBER_TB AUTO_INCREMENT = 1;
-- ALTER TABLE erp.CHAMBER_ZONE_TB AUTO_INCREMENT = 1;
-- ALTER TABLE ops.CHAMBER_ETC_USE_TB AUTO_INCREMENT = 1;
