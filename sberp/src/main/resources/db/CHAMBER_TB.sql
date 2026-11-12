-- =====================================================================
-- 챔버 사용 관리 (#26-8) — 챔버·구역·시험방법 매핑, 로데이터 외 사용
-- 대상 DB : MariaDB 10.1 (erp)
-- 설계    : docs/design/chamber-erp.md (ERP), chamber-svc.md (현황판)
--
-- 이 파일 : CHAMBER_TB, CHAMBER_ZONE_TB, CHAMBER_ZONE_MET_TB, CHAMBER_ETC_USE_TB (모두 신규)
--           RAW_ENV_TB.ZONE_SEQ 는 RAW_ENV_TB.sql 에 있다.
-- 순서    : 이 파일 → RAW_ENV_TB.sql → chamber-svc/sql/chamber_svc_user.sql (GRANT)
--
-- 테이블별 쓰는 쪽
--   CHAMBER_TB, CHAMBER_ZONE_TB, CHAMBER_ZONE_MET_TB : chamber-svc 챔버 관리 화면 (ERP 는 읽기)
--   CHAMBER_ETC_USE_TB                              : chamber-svc 현황판 (ERP 는 측정 [시작] 때 읽기)
--   erp 스키마 DDL 은 ERP 가 맡는다. 컬럼을 바꿀 때는 chamber-svc 와 먼저 맞춘다.
--
-- 사용 단위는 구역(ZONE). 구역마다 동시에 1건 (MULTI_YN=1 구역은 제한 없음 — 일반 시험실)
-- 구역을 나누지 않는 챔버도 구역 1개를 만든다. 챔버·구역은 지우지 않고 USE_YN=0 으로 숨긴다.
--
-- "진행 중" 정의 (ERP·chamber-svc 공통)
--   RAW_ENV_TB         : DEL_YN = 0 AND MIG_YN = 0 AND TEST_S_DT IS NOT NULL AND TEST_E_DT IS NULL
--   CHAMBER_ETC_USE_TB : DEL_YN = 0 AND END_DT IS NULL
-- 잠금 : 구역을 쓰기 시작하는 쪽은 사용 중 확인 전에 CHAMBER_ZONE_TB 행을 FOR UPDATE 로 잠근다
--        (ERP 는 RAW_TB → CHAMBER_ZONE_TB 순서)
-- =====================================================================


-- ---------------------------------------------------------------------
-- 0. 실행 전 확인 — 결과 0 이어야 한다 (개발 DB 는 chamber-svc dev_seed.sql 로 이미 있을 수 있다)
-- ---------------------------------------------------------------------
SELECT TABLE_NAME
  FROM information_schema.TABLES
 WHERE TABLE_SCHEMA = DATABASE()
   AND TABLE_NAME IN ('CHAMBER_TB', 'CHAMBER_ZONE_TB', 'CHAMBER_ZONE_MET_TB', 'CHAMBER_ETC_USE_TB');


-- ---------------------------------------------------------------------
-- 1. 챔버·구역·시험방법 매핑
-- ---------------------------------------------------------------------
CREATE TABLE CHAMBER_TB (
    CHAMBER_SEQ  INT          NOT NULL AUTO_INCREMENT COMMENT '챔버 SEQ',
    NAME         VARCHAR(50)  NOT NULL COMMENT '챔버 이름 (예: 10m 챔버, 차폐실 1)',
    SORT_NO      SMALLINT     NOT NULL DEFAULT 0 COMMENT '표시 순서',
    USE_YN       TINYINT      NOT NULL DEFAULT 1 COMMENT '사용 여부 (0이면 선택 목록·현황판에서 숨김)',
    INS_MEM_ID   VARCHAR(20)  NOT NULL COMMENT '등록자',
    INS_DT       DATETIME     NOT NULL COMMENT '등록 일시',
    UDT_MEM_ID   VARCHAR(20)  NULL     COMMENT '수정자',
    UDT_DT       DATETIME     NULL     COMMENT '수정 일시',
    PRIMARY KEY (CHAMBER_SEQ)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='챔버';

CREATE TABLE CHAMBER_ZONE_TB (
    ZONE_SEQ     INT          NOT NULL AUTO_INCREMENT COMMENT '구역 SEQ',
    CHAMBER_SEQ  INT          NOT NULL COMMENT '챔버 SEQ (CHAMBER_TB)',
    NAME         VARCHAR(50)  NOT NULL COMMENT '구역 이름 (구역이 하나뿐인 챔버는 빈 값)',
    MULTI_YN     TINYINT      NOT NULL DEFAULT 0 COMMENT '동시 사용 허용 (1이면 사용 중 확인 안 함. 일반 시험실용)',
    SORT_NO      SMALLINT     NOT NULL DEFAULT 0 COMMENT '표시 순서',
    USE_YN       TINYINT      NOT NULL DEFAULT 1 COMMENT '사용 여부',
    INS_MEM_ID   VARCHAR(20)  NOT NULL COMMENT '등록자',
    INS_DT       DATETIME     NOT NULL COMMENT '등록 일시',
    UDT_MEM_ID   VARCHAR(20)  NULL     COMMENT '수정자',
    UDT_DT       DATETIME     NULL     COMMENT '수정 일시',
    PRIMARY KEY (ZONE_SEQ),
    KEY IDX_CHAMBER_ZONE (CHAMBER_SEQ, USE_YN, SORT_NO)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='챔버 구역 (사용 단위)';

CREATE TABLE CHAMBER_ZONE_MET_TB (
    ZONE_SEQ     INT          NOT NULL COMMENT '구역 SEQ (CHAMBER_ZONE_TB)',
    MET_KEY      VARCHAR(10)  NOT NULL COMMENT '시험방법 키 (RAW_ENV_TB.MET_KEY 와 같은 값: CE, RE1, RE2, RE3, ESD, RS, EFT, SURGE, CS, MF, VDIP, CLK, DP, TEL)',
    PRIMARY KEY (ZONE_SEQ, MET_KEY),
    KEY IDX_CHAMBER_ZONE_MET (MET_KEY)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='구역에서 할 수 있는 시험방법';


-- ---------------------------------------------------------------------
-- 2. 로데이터 외 사용 (점검·디버깅·입회·예비시험) — chamber-svc 현황판이 기록
--    ERP 는 USE_SEQ, ZONE_SEQ, PURPOSE, MEM_NAME, START_DT, END_DT, DEL_YN 을 읽는다
-- ---------------------------------------------------------------------
CREATE TABLE CHAMBER_ETC_USE_TB (
    USE_SEQ      INT          NOT NULL AUTO_INCREMENT COMMENT '사용 SEQ',
    ZONE_SEQ     INT          NOT NULL COMMENT '구역 (CHAMBER_ZONE_TB.ZONE_SEQ)',
    PURPOSE      VARCHAR(10)  NOT NULL COMMENT 'PRE 예비시험 / DEBUG 고객 디버깅 / CHECK 점검 / WITNESS 입회 / ETC 기타',
    TEST_SEQ     INT          NULL     COMMENT '관련 시험 (선택)',
    MEMO         VARCHAR(200) NULL     COMMENT '업체명·내용 등',
    MEM_ID       VARCHAR(20)  NOT NULL COMMENT '[사용] 누른 로그인 사용자',
    MEM_NAME     VARCHAR(50)  NULL     COMMENT '사용자 이름',
    START_DT     DATETIME     NOT NULL COMMENT '시작 (서버시각)',
    END_DT       DATETIME     NULL     COMMENT '종료 (서버시각). NULL = 사용 중',
    DEL_YN       TINYINT      NOT NULL DEFAULT 0 COMMENT '삭제 여부',
    DEL_REASON   VARCHAR(200) NULL     COMMENT '삭제 사유',
    DEL_MEM_ID   VARCHAR(20)  NULL     COMMENT '삭제자',
    DEL_DT       DATETIME     NULL     COMMENT '삭제 일시',
    INS_DT       DATETIME     NOT NULL COMMENT '등록 일시',
    UDT_MEM_ID   VARCHAR(20)  NULL     COMMENT '수정자',
    UDT_DT       DATETIME     NULL     COMMENT '수정 일시',
    PRIMARY KEY (USE_SEQ),
    KEY IDX_CHAMBER_ETC_USE (ZONE_SEQ, START_DT)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='챔버 사용 (로데이터 외)';


-- ---------------------------------------------------------------------
-- 3. 챔버 목록 — 기획 목록 확정 후 chamber-svc 관리 화면 또는 INSERT 로 넣는다 (미결)
--    "일반 시험실" 구역(MULTI_YN=1) 포함
-- ---------------------------------------------------------------------


-- ---------------------------------------------------------------------
-- 4. 확인
-- ---------------------------------------------------------------------
SELECT TABLE_NAME, TABLE_COMMENT
  FROM information_schema.TABLES
 WHERE TABLE_SCHEMA = DATABASE()
   AND TABLE_NAME LIKE 'CHAMBER%';


-- ---------------------------------------------------------------------
-- 되돌리기 (RAW_ENV_TB.ZONE_SEQ 를 쓰는 프로그램을 내린 뒤에만)
-- ---------------------------------------------------------------------
-- DROP TABLE IF EXISTS CHAMBER_ETC_USE_TB;
-- DROP TABLE IF EXISTS CHAMBER_ZONE_MET_TB;
-- DROP TABLE IF EXISTS CHAMBER_ZONE_TB;
-- DROP TABLE IF EXISTS CHAMBER_TB;
