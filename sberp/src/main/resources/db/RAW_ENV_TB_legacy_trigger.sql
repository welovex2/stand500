-- =====================================================================
-- RAW_ENV_TB : 기존 화면 입력 행(MIG_YN=2) 등록 이력 기록 (#26-8, 2026-11-18)
--
-- 이미 RAW_ENV_TB.sql 을 반영한 DB(개발)에만 실행한다. 새로 만드는 DB는 RAW_ENV_TB.sql 에 포함됨.
--   MIG_YN 0 일반 / 1 이관 / 2 기존 화면(시작·종료 없음)에서 측정일을 처음 입력한 행
--   등록 트리거 조건 MIG_YN = 0 → MIG_YN <> 1 (이관 행만 제외)
--   컬럼 주석도 같이 바꾼다
-- =====================================================================

ALTER TABLE RAW_ENV_TB
    MODIFY MIG_YN TINYINT NOT NULL DEFAULT 0
    COMMENT '기존 방식 행 (0 일반, 1 이관, 2 기존 화면 입력). 0이 아니면 시작=종료, 소요시간 표시 안 함';

DROP TRIGGER IF EXISTS RAW_ENV_INS_TG;

DELIMITER ;;

CREATE TRIGGER RAW_ENV_INS_TG AFTER INSERT ON RAW_ENV_TB FOR EACH ROW
BEGIN
    IF NEW.MIG_YN <> 1 THEN
        CALL RAW_DONE_EDIT_LOG(NEW.RAW_SEQ, NULL, 'RAW_ENV_TB', 'INSERT', NEW.INS_MEM_ID);
    END IF;
END;;

DELIMITER ;

-- 확인 : ACTION_STATEMENT 에 'MIG_YN <> 1' 이 보여야 한다
SELECT TRIGGER_NAME, ACTION_STATEMENT
  FROM information_schema.TRIGGERS
 WHERE TRIGGER_SCHEMA = DATABASE()
   AND TRIGGER_NAME = 'RAW_ENV_INS_TG';
