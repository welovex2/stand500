-- =====================================================================
-- chamber-svc 전용 계정 (MariaDB 10.1). erp 는 읽기, ops 는 쓰기.
-- 먼저 실행돼 있어야 하는 것:
--   1) ERP 의 CHAMBER_TB.sql (CHAMBER_TB, CHAMBER_ZONE_TB, CHAMBER_ZONE_MET_TB) 과 RAW_ENV_TB.ZONE_SEQ
--   2) ops_schema.sql
-- '<비밀번호>' 를 바꿔서 실행하고, 같은 값을 compose .env 의 CHAMBER_DB_PASSWORD 에 넣는다.
-- =====================================================================

-- 동시 접속 5개로 제한 — ERP 가 쓸 DB 접속을 차지하지 않게 한다
CREATE USER IF NOT EXISTS 'chamber_svc'@'%' IDENTIFIED BY '<비밀번호>';
GRANT USAGE ON *.* TO 'chamber_svc'@'%' WITH MAX_USER_CONNECTIONS 5;

-- erp: 읽기
GRANT SELECT ON erp.RAW_ENV_TB          TO 'chamber_svc'@'%';
GRANT SELECT ON erp.RAW_TB              TO 'chamber_svc'@'%';
GRANT SELECT ON erp.TEST_TB             TO 'chamber_svc'@'%';
GRANT SELECT ON erp.TEST_ITEM_TB        TO 'chamber_svc'@'%';
GRANT SELECT ON erp.JOB_TB              TO 'chamber_svc'@'%';
GRANT SELECT ON erp.CHAMBER_TB          TO 'chamber_svc'@'%';
GRANT SELECT ON erp.CHAMBER_ZONE_TB     TO 'chamber_svc'@'%';
GRANT SELECT ON erp.CHAMBER_ZONE_MET_TB TO 'chamber_svc'@'%';
-- 로그인 확인에 쓰는 컬럼만
GRANT SELECT (ID, PASSWORD, MEM_NAME, LOCK_YN, REV_YN, QUIT_DT) ON erp.MEMBER_TB TO 'chamber_svc'@'%';

-- 구역 행 잠금용. SELECT ... FOR UPDATE 는 SELECT 권한만으로는 거부될 수 있어 UDT_DT 컬럼에만 UPDATE 를 준다.
-- chamber-svc 는 이 컬럼을 실제로 고치지 않는다. (SELECT 만으로 잠금이 되는 것을 확인하면 이 줄은 뺀다)
GRANT UPDATE (UDT_DT) ON erp.CHAMBER_ZONE_TB TO 'chamber_svc'@'%';

-- ops: 쓰기 (DELETE 없음 — 삭제는 DEL_YN)
GRANT SELECT, INSERT, UPDATE ON ops.CHAMBER_ETC_USE_TB TO 'chamber_svc'@'%';
GRANT SELECT, INSERT, UPDATE ON ops.LOGIN_FAIL_TB      TO 'chamber_svc'@'%';
GRANT SELECT, INSERT         ON ops.DHT_LOG_TB         TO 'chamber_svc'@'%';

FLUSH PRIVILEGES;

-- 확인
SHOW GRANTS FOR 'chamber_svc'@'%';

-- 회수가 필요할 때
-- DROP USER 'chamber_svc'@'%';
