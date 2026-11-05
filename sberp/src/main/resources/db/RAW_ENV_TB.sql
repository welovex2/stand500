-- =====================================================================
-- 로데이터 측정일·환경조건 다건 입력 (#26-8) — 테이블·이력 트리거
-- 대상 DB : MariaDB 10.1 (erp)
-- 설계    : docs/design/raw-env-msrmn-multi.md
--
-- 이 파일 : RAW_ENV_TB(신규) + RAW_ENV_INS_TG / RAW_ENV_UDT_TG(신규)
-- 이관    : RAW_ENV_TB_migration.sql (프로그램 배포 시점에 별도 실행)
-- 순서    : 이 파일 → (배포 직전) 이관 파일. 이 파일은 미리 반영해도 기존 동작에 영향 없음.
--
-- 행 하나 = 시험방법(MET_KEY) 표의 측정 1회 (시작~종료, 온습도·기압, 시험원 1명 서명)
--   MET_KEY : CE, RE1, RE2, RE3, ESD, RS, EFT, SURGE, CS, MF, VDIP, CLK, DP, TEL
--             RE 는 대역(HZ1/HZ2/HZ3)별로 RE1/RE2/RE3
--   표당 살아있는 행(DEL_YN=0) 최대 10개 — 프로그램에서 검증
--   삭제·중단은 DEL_YN=1 (소프트 삭제). 사유는 DEL_REASON + TEST_MEMO_TB(작성내역)
--   MEM_ID 는 [시작] 누른 로그인 사용자를 자동 저장 (화면 입력 없음). 이름·서명(MEM_NAME, SIGN)은 화면 입력
--
-- 추후 시험원 동시간 중복 확인·일정 달력 (IDX_RAW_ENV_MEM 사용)
--   같은 MEM_ID 의 [TEST_S_DT, IFNULL(TEST_E_DT, NOW())) 구간이 겹치면 중복
--   DEL_YN=0, MIG_YN=0 행만 비교 (이관 행은 시작=종료라 구간이 없음)
--
-- 이력 (기존 RAW_TEST_HIS_TG.sql 과 같은 방식)
--   RAW_DONE_EDIT_TB : 프로젝트완료 후 저장 기록 (TB='RAW_ENV_TB', INSERT/UPDATE)
--   RAW_HIS_TB       : TB='RAW_ENV_TB' 값 변경 전/후
--                      COL = {컬럼}:{ENV_SEQ} {MET_KEY}-{ROW_NO}   예) TEST_S_DT:501 ESD-2
--                      대상 컬럼 : TEST_S_DT, TEST_E_DT, TEMP, TEMP_PLUS, HMDT, HMDT_PLUS, BRMT, BRMT_PLUS, DEL_YN
--                      변경 전 값이 없는 최초 입력(NULL·빈 값 → 값)은 남기지 않는다
--                      → [종료] 클릭(TEST_E_DT NULL → 값)은 남지 않고, 기술책임자의 일시 수정은 남는다
--   이관 행(MIG_YN=1) 의 INSERT 는 기록하지 않는다
-- =====================================================================


-- ---------------------------------------------------------------------
-- 0. 실행 전 확인 — 결과 0 이어야 한다 (이미 반영된 DB 면 중단)
-- ---------------------------------------------------------------------
SELECT COUNT(*)
  FROM information_schema.TABLES
 WHERE TABLE_SCHEMA = DATABASE()
   AND TABLE_NAME = 'RAW_ENV_TB';

-- 이력 기록 프로시저가 있어야 한다 (RAW_TEST_HIS_TG.sql 반영 여부, 결과 1)
SELECT COUNT(*)
  FROM information_schema.ROUTINES
 WHERE ROUTINE_SCHEMA = DATABASE()
   AND ROUTINE_NAME = 'RAW_DONE_EDIT_LOG';


-- ---------------------------------------------------------------------
-- 1. 테이블
-- ---------------------------------------------------------------------
CREATE TABLE RAW_ENV_TB (
    ENV_SEQ     INT           NOT NULL AUTO_INCREMENT COMMENT '환경조건 행 SEQ',
    RAW_SEQ     INT           NOT NULL COMMENT '로데이터 SEQ (RAW_TB.RAW_SEQ)',
    MET_KEY     VARCHAR(10)   NOT NULL COMMENT '시험방법 키 (CE, RE1, RE2, RE3, ESD, RS, EFT, SURGE, CS, MF, VDIP, CLK, DP, TEL)',
    ROW_NO      SMALLINT      NOT NULL COMMENT '표시 순번 (시작 시 그 표의 MAX+1, 삭제된 행 포함)',

    TEST_S_DT   DATETIME      NULL     COMMENT '시험시작일시 (서버시각)',
    TEST_E_DT   DATETIME      NULL     COMMENT '시험종료일시 (서버시각)',

    TEMP        VARCHAR(50)   NULL     COMMENT '온도',
    TEMP_PLUS   VARCHAR(50)   NULL     COMMENT '온도 ±',
    HMDT        VARCHAR(50)   NULL     COMMENT '습도',
    HMDT_PLUS   VARCHAR(50)   NULL     COMMENT '습도 ±',
    BRMT        VARCHAR(50)   NULL     COMMENT '기압 (ESD·RS·EFT·SURGE·CS·MF·VDIP)',
    BRMT_PLUS   VARCHAR(50)   NULL     COMMENT '기압 ±',

    MEM_ID      VARCHAR(20)   NULL     COMMENT '시험원 아이디 ([시작] 누른 로그인 사용자 자동 저장, 이관 행은 NULL)',
    MEM_NAME    VARCHAR(50)   NULL     COMMENT '시험원 이름 (표기용)',
    SIGN        MEDIUMTEXT    NULL     COMMENT '서명 (기존 METHOD_*.SIGN 과 같은 형식)',
    SIGN_URL    VARCHAR(500)  NULL     COMMENT '서명 URL',

    MIG_YN      TINYINT       NOT NULL DEFAULT 0 COMMENT '기존 데이터 이관 행 (1이면 소요시간 표시 안 함)',

    DEL_YN      TINYINT       NOT NULL DEFAULT 0 COMMENT '삭제 여부 (중단·기술책임자 삭제)',
    DEL_REASON  VARCHAR(200)  NULL     COMMENT '중단/삭제 사유',
    DEL_MEM_ID  VARCHAR(20)   NULL     COMMENT '삭제자',
    DEL_DT      DATETIME      NULL     COMMENT '삭제 일시',

    INS_MEM_ID  VARCHAR(20)   NOT NULL COMMENT '등록자',
    INS_DT      DATETIME      NOT NULL COMMENT '등록 일시',
    UDT_MEM_ID  VARCHAR(20)   NULL     COMMENT '수정자',
    UDT_DT      DATETIME      NULL     COMMENT '수정 일시',

    PRIMARY KEY (ENV_SEQ),
    KEY IDX_RAW_ENV_RAW (RAW_SEQ, MET_KEY, DEL_YN, ROW_NO),
    KEY IDX_RAW_ENV_MEM (MEM_ID, TEST_S_DT)   -- 시험원 동시간 중복 확인·일정 달력용 (추후)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='로데이터 측정일·환경조건 (시험방법별 다건)';


-- ---------------------------------------------------------------------
-- 2. 이력 트리거
-- ---------------------------------------------------------------------
DROP TRIGGER IF EXISTS RAW_ENV_INS_TG;
DROP TRIGGER IF EXISTS RAW_ENV_UDT_TG;

DELIMITER ;;

-- 등록: 프로젝트완료 후 저장 기록 (이관 행 제외)
CREATE TRIGGER RAW_ENV_INS_TG AFTER INSERT ON RAW_ENV_TB FOR EACH ROW
BEGIN
    IF NEW.MIG_YN = 0 THEN
        CALL RAW_DONE_EDIT_LOG(NEW.RAW_SEQ, NULL, 'RAW_ENV_TB', 'INSERT', NEW.INS_MEM_ID);
    END IF;
END;;

-- 수정: 프로젝트완료 후 저장 기록 + 값 변경 전/후
CREATE TRIGGER RAW_ENV_UDT_TG AFTER UPDATE ON RAW_ENV_TB FOR EACH ROW
BEGIN
    DECLARE v_key VARCHAR(30);
    DECLARE v_mem VARCHAR(20);

    -- 예) '501 ESD-2'
    SET v_key = CONCAT(NEW.ENV_SEQ, ' ', NEW.MET_KEY, '-', NEW.ROW_NO);
    -- 변경자: 삭제(DEL_YN 변경)면 삭제자, 아니면 수정자. 둘 다 없으면 등록자
    SET v_mem = IFNULL(IF(NEW.DEL_YN <> OLD.DEL_YN, NEW.DEL_MEM_ID, NEW.UDT_MEM_ID), NEW.INS_MEM_ID);

    CALL RAW_DONE_EDIT_LOG(NEW.RAW_SEQ, NULL, 'RAW_ENV_TB', 'UPDATE', v_mem);

    IF NOT (NEW.TEST_S_DT <=> OLD.TEST_S_DT) AND OLD.TEST_S_DT IS NOT NULL THEN
        INSERT INTO RAW_HIS_TB (RAW_ID, TB, COL, `BEFORE`, `AFTER`, MEM_ID, INS_DT)
            VALUES (NEW.RAW_SEQ, 'RAW_ENV_TB', CONCAT('TEST_S_DT:', v_key), OLD.TEST_S_DT, NEW.TEST_S_DT, v_mem, NOW());
    END IF;

    IF NOT (NEW.TEST_E_DT <=> OLD.TEST_E_DT) AND OLD.TEST_E_DT IS NOT NULL THEN
        INSERT INTO RAW_HIS_TB (RAW_ID, TB, COL, `BEFORE`, `AFTER`, MEM_ID, INS_DT)
            VALUES (NEW.RAW_SEQ, 'RAW_ENV_TB', CONCAT('TEST_E_DT:', v_key), OLD.TEST_E_DT, NEW.TEST_E_DT, v_mem, NOW());
    END IF;

    IF NOT (NEW.TEMP <=> OLD.TEMP) AND IFNULL(OLD.TEMP, '') <> '' THEN
        INSERT INTO RAW_HIS_TB (RAW_ID, TB, COL, `BEFORE`, `AFTER`, MEM_ID, INS_DT)
            VALUES (NEW.RAW_SEQ, 'RAW_ENV_TB', CONCAT('TEMP:', v_key), OLD.TEMP, NEW.TEMP, v_mem, NOW());
    END IF;

    IF NOT (NEW.TEMP_PLUS <=> OLD.TEMP_PLUS) AND IFNULL(OLD.TEMP_PLUS, '') <> '' THEN
        INSERT INTO RAW_HIS_TB (RAW_ID, TB, COL, `BEFORE`, `AFTER`, MEM_ID, INS_DT)
            VALUES (NEW.RAW_SEQ, 'RAW_ENV_TB', CONCAT('TEMP_PLUS:', v_key), OLD.TEMP_PLUS, NEW.TEMP_PLUS, v_mem, NOW());
    END IF;

    IF NOT (NEW.HMDT <=> OLD.HMDT) AND IFNULL(OLD.HMDT, '') <> '' THEN
        INSERT INTO RAW_HIS_TB (RAW_ID, TB, COL, `BEFORE`, `AFTER`, MEM_ID, INS_DT)
            VALUES (NEW.RAW_SEQ, 'RAW_ENV_TB', CONCAT('HMDT:', v_key), OLD.HMDT, NEW.HMDT, v_mem, NOW());
    END IF;

    IF NOT (NEW.HMDT_PLUS <=> OLD.HMDT_PLUS) AND IFNULL(OLD.HMDT_PLUS, '') <> '' THEN
        INSERT INTO RAW_HIS_TB (RAW_ID, TB, COL, `BEFORE`, `AFTER`, MEM_ID, INS_DT)
            VALUES (NEW.RAW_SEQ, 'RAW_ENV_TB', CONCAT('HMDT_PLUS:', v_key), OLD.HMDT_PLUS, NEW.HMDT_PLUS, v_mem, NOW());
    END IF;

    IF NOT (NEW.BRMT <=> OLD.BRMT) AND IFNULL(OLD.BRMT, '') <> '' THEN
        INSERT INTO RAW_HIS_TB (RAW_ID, TB, COL, `BEFORE`, `AFTER`, MEM_ID, INS_DT)
            VALUES (NEW.RAW_SEQ, 'RAW_ENV_TB', CONCAT('BRMT:', v_key), OLD.BRMT, NEW.BRMT, v_mem, NOW());
    END IF;

    IF NOT (NEW.BRMT_PLUS <=> OLD.BRMT_PLUS) AND IFNULL(OLD.BRMT_PLUS, '') <> '' THEN
        INSERT INTO RAW_HIS_TB (RAW_ID, TB, COL, `BEFORE`, `AFTER`, MEM_ID, INS_DT)
            VALUES (NEW.RAW_SEQ, 'RAW_ENV_TB', CONCAT('BRMT_PLUS:', v_key), OLD.BRMT_PLUS, NEW.BRMT_PLUS, v_mem, NOW());
    END IF;

    -- 중단·삭제 (DEL_YN 0 → 1). 사유는 DEL_REASON / TEST_MEMO_TB 에 있다
    IF NOT (NEW.DEL_YN <=> OLD.DEL_YN) THEN
        INSERT INTO RAW_HIS_TB (RAW_ID, TB, COL, `BEFORE`, `AFTER`, MEM_ID, INS_DT)
            VALUES (NEW.RAW_SEQ, 'RAW_ENV_TB', CONCAT('DEL_YN:', v_key), OLD.DEL_YN, NEW.DEL_YN, v_mem, NOW());
    END IF;
END;;

DELIMITER ;


-- ---------------------------------------------------------------------
-- 3. 반영 후 확인
-- ---------------------------------------------------------------------
-- 테이블 컬럼 25개
SELECT COUNT(*)
  FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = DATABASE()
   AND TABLE_NAME = 'RAW_ENV_TB';

-- 트리거 2건 (RAW_ENV_INS_TG / RAW_ENV_UDT_TG)
SELECT TRIGGER_NAME, EVENT_OBJECT_TABLE, ACTION_TIMING, EVENT_MANIPULATION
  FROM information_schema.TRIGGERS
 WHERE EVENT_OBJECT_SCHEMA = DATABASE()
   AND EVENT_OBJECT_TABLE = 'RAW_ENV_TB';


-- ---------------------------------------------------------------------
-- 9. 롤백 (필요할 때만 주석 해제 후 실행)
--    RAW_HIS_TB 의 TB='RAW_ENV_TB' 행, RAW_DONE_EDIT_TB 의 TB='RAW_ENV_TB' 행은 남겨 둔다.
-- ---------------------------------------------------------------------
-- DROP TRIGGER IF EXISTS RAW_ENV_INS_TG;
-- DROP TRIGGER IF EXISTS RAW_ENV_UDT_TG;
-- DROP TABLE IF EXISTS RAW_ENV_TB;
