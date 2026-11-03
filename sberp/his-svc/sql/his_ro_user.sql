-- =====================================================================
-- his-svc 읽기 전용 계정 (MariaDB 10.1, schema erp)
-- 이력 조회에 필요한 테이블만 SELECT. MEMBER_TB 는 아이디·이름 컬럼만 (비밀번호 컬럼 제외).
-- RAW_DONE_EDIT_TB 는 RAW_TEST_HIS_TG.sql 반영 후에 생기므로, 그 GRANT 는 트리거 반영 뒤에 실행한다.
-- '<비밀번호>' 를 바꿔서 실행하고, 같은 값을 .env 의 HIS_DB_PASSWORD 에 넣는다.
-- =====================================================================

CREATE USER IF NOT EXISTS 'his_ro'@'%' IDENTIFIED BY '<비밀번호>';

GRANT SELECT ON erp.RAW_HIS_TB   TO 'his_ro'@'%';
GRANT SELECT ON erp.RAW_TB       TO 'his_ro'@'%';
GRANT SELECT ON erp.TEST_TB      TO 'his_ro'@'%';
GRANT SELECT ON erp.TEST_ITEM_TB TO 'his_ro'@'%';
GRANT SELECT ON erp.JOB_TB       TO 'his_ro'@'%';
GRANT SELECT (ID, MEM_NAME) ON erp.MEMBER_TB TO 'his_ro'@'%';

-- 트리거 반영(RAW_DONE_EDIT_TB 생성) 후
GRANT SELECT ON erp.RAW_DONE_EDIT_TB TO 'his_ro'@'%';

FLUSH PRIVILEGES;

-- 확인
SHOW GRANTS FOR 'his_ro'@'%';

-- 회수가 필요할 때
-- DROP USER 'his_ro'@'%';
