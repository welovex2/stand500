-- =====================================================================
-- chamber-svc 전용 계정 (MariaDB 10.1, schema erp)
-- 쓰는 곳: 로데이터 외 사용, 챔버 관리 테이블 3개, MEMBER_TB 의 로그인 실패 컬럼 2개. 나머지는 읽기만.
-- 먼저 있어야 하는 테이블 (ERP 의 CHAMBER_TB.sql. 개발 DB 는 dev_seed.sql 이 임시로 만든다):
--   CHAMBER_TB, CHAMBER_ZONE_TB, CHAMBER_ZONE_MET_TB, CHAMBER_ETC_USE_TB, RAW_ENV_TB.ZONE_SEQ
-- '<비밀번호>' 를 바꿔서 실행하고, 같은 값을 compose .env 의 CHAMBER_DB_PASSWORD 에 넣는다.
-- 계정이 이미 있으면 CREATE USER 는 건너뛰고(비밀번호 그대로) GRANT 만 다시 적용된다.
-- =====================================================================

-- 동시 접속 5개로 제한 — ERP 가 쓸 DB 접속을 차지하지 않게 한다
CREATE USER IF NOT EXISTS 'chamber_svc'@'%' IDENTIFIED BY '<비밀번호>';
GRANT USAGE ON *.* TO 'chamber_svc'@'%' WITH MAX_USER_CONNECTIONS 5;

-- 읽기 (현황·이력)
GRANT SELECT ON erp.RAW_ENV_TB   TO 'chamber_svc'@'%';
GRANT SELECT ON erp.RAW_TB       TO 'chamber_svc'@'%';
GRANT SELECT ON erp.TEST_TB      TO 'chamber_svc'@'%';
GRANT SELECT ON erp.TEST_ITEM_TB TO 'chamber_svc'@'%';
GRANT SELECT ON erp.JOB_TB       TO 'chamber_svc'@'%';

-- 로데이터 외 사용 (DELETE 없음 — 삭제는 DEL_YN)
GRANT SELECT, INSERT, UPDATE ON erp.CHAMBER_ETC_USE_TB TO 'chamber_svc'@'%';

-- 챔버 관리 화면. 챔버·구역은 지우지 않고 USE_YN 으로 숨긴다. 매핑만 행을 넣고 지운다.
-- CHAMBER_ZONE_TB 의 UPDATE 권한으로 구역 행 잠금(SELECT ... FOR UPDATE)도 된다.
GRANT SELECT, INSERT, UPDATE ON erp.CHAMBER_TB          TO 'chamber_svc'@'%';
GRANT SELECT, INSERT, UPDATE ON erp.CHAMBER_ZONE_TB     TO 'chamber_svc'@'%';
GRANT SELECT, INSERT, DELETE ON erp.CHAMBER_ZONE_MET_TB TO 'chamber_svc'@'%';

-- 로그인: 확인에 쓰는 컬럼만 읽고, 실패 횟수·잠금 컬럼만 고친다 (ERP 와 같은 컬럼을 함께 쓴다)
GRANT SELECT (ID, PASSWORD, MEM_NAME, LOCK_YN, REV_YN, QUIT_DT, FAIL_PASS_CNT) ON erp.MEMBER_TB TO 'chamber_svc'@'%';
GRANT UPDATE (FAIL_PASS_CNT, LOCK_YN) ON erp.MEMBER_TB TO 'chamber_svc'@'%';

FLUSH PRIVILEGES;

-- 확인
SHOW GRANTS FOR 'chamber_svc'@'%';

-- 회수가 필요할 때
-- DROP USER 'chamber_svc'@'%';
