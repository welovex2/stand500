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
-- 함께 점유 (2026-11-19): 구역 Z 를 쓰면 Z, Z 의 연계 구역(LINK_ZONE_SEQ), 단독 사용 챔버(SOLO_YN=1)의
--   다른 구역이 모두 사용 중이 된다. 두 사용이 점유하는 구역이 하나라도 겹치면 함께 쓸 수 없다.
--   잠금은 점유하는 구역 행을 모두 ZONE_SEQ 오름차순으로 (ERP·chamber-svc 같은 순서).
-- 1인 1시험: 같은 시험원(MEM_ID)의 진행 중 측정 행·현황판 사용이 있으면 새로 시작할 수 없다.
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
    FLOOR        TINYINT      NOT NULL DEFAULT 1 COMMENT '층 (현황판 층 구분)',
    SOLO_YN      TINYINT      NOT NULL DEFAULT 0 COMMENT '구역 단독 사용 (1이면 구역이 여러 개여도 한 번에 하나만. 예: RE (3m)의 RE (Below)·RE (Above))',
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
    LINK_ZONE_SEQ INT         NULL     COMMENT '연계 구역 (장비 이관): 이 구역을 쓰면 함께 점유되는 다른 챔버의 구역. 예: RE (3m)/RE (Above) → RE (Above)',
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
    MET_KEY      VARCHAR(10)  NOT NULL COMMENT '시험방법 키 (RAW_ENV_TB.MET_KEY 와 같은 값: CE, ESD, RS, EFT, SURGE, CS, MF, VDIP, CLK, DP, TEL. RE 는 대역 RA=RE (Below), RB=RE (Above))',
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
    PURPOSE      VARCHAR(10)  NOT NULL COMMENT 'TEST 시험(시험번호 있음) / NORD 로데이터 연동 없는 시험(CTI·4-39) / 시험번호 없이 사용 사유: PRE 사전 점검 / CHECK 장비 점검 / DEBUG 디버깅 / ETC 기타',
    TEST_SEQ     INT          NULL     COMMENT '시험 (PURPOSE=TEST 일 때. 자동화 로데이터 규격이 아닌 시험)',
    MEMO         VARCHAR(200) NULL     COMMENT '업체명·내용 등',
    MEM_ID       VARCHAR(20)  NULL     COMMENT '시험원 (1인 1시험 확인 기준). 고객 입회 시험은 NULL (시험원 미지정)',
    MEM_NAME     VARCHAR(50)  NULL     COMMENT '시험원 이름',
    REG_MEM_ID   VARCHAR(20)  NULL     COMMENT '[사용 시작]을 누른 사람 (기술책임자·관리자가 시험원을 골라 시작하면 MEM_ID 와 다름)',
    WITNESS_NAME VARCHAR(100) NULL     COMMENT '고객 입회자 이름 (소속). 값이 있으면 입회 시험 (시험원 미지정)',
    WHOLE_YN     TINYINT      NOT NULL DEFAULT 0 COMMENT '시험장 전체 점유 (시험번호 없이 사용 + 항목 선택 안 함). 1이면 ZONE_SEQ 는 그 챔버의 첫 구역이고 챔버의 모든 구역을 점유',
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
    KEY IDX_CHAMBER_ETC_USE (ZONE_SEQ, START_DT),
    KEY IDX_CHAMBER_ETC_USE_MEM (MEM_ID, END_DT)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='챔버 사용 (현황판 기록: 자동화 로데이터가 아닌 시험 + 시험번호 없이 사용)';

-- 2-1. 현황판 사용 기록 수정 이력 (사용 이력 [수정], 2026-11-20 추가)
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


-- 이미 테이블을 만든 DB (개발 DB) 에 층 칸만 더할 때 (2026-11-16 추가)
-- ALTER TABLE CHAMBER_TB ADD COLUMN FLOOR TINYINT NOT NULL DEFAULT 1 COMMENT '층 (현황판 층 구분)' AFTER NAME;

-- 이미 테이블을 만든 DB 에 단독 사용·연계 구역·등록자를 더할 때 (2026-11-19 추가)
-- 구역 재구성(RE (3m)·RE (10m) 항목 구역, 연계)은 chamber-svc/sql/chamber_link_2026-11-19.sql
-- ALTER TABLE CHAMBER_TB ADD COLUMN SOLO_YN TINYINT NOT NULL DEFAULT 0 COMMENT '구역 단독 사용 (1이면 구역이 여러 개여도 한 번에 하나만. 예: RE (3m)의 RE (Below)·RE (Above))' AFTER FLOOR;
-- ALTER TABLE CHAMBER_ZONE_TB ADD COLUMN LINK_ZONE_SEQ INT NULL COMMENT '연계 구역 (장비 이관): 이 구역을 쓰면 함께 점유되는 다른 챔버의 구역. 예: RE (3m)/RE (Above) → RE (Above)' AFTER MULTI_YN;
-- ALTER TABLE CHAMBER_ETC_USE_TB ADD COLUMN REG_MEM_ID VARCHAR(20) NULL COMMENT '[사용 시작]을 누른 사람 (기술책임자·관리자가 시험원을 골라 시작하면 MEM_ID 와 다름)' AFTER MEM_NAME;
-- ALTER TABLE CHAMBER_ETC_USE_TB MODIFY PURPOSE VARCHAR(10) NOT NULL COMMENT 'TEST 시험(시험번호 있음) / 시험번호 없이 사용 사유: PRE 예비시험 / DEBUG 고객 디버깅 / CHECK 점검 / WITNESS 입회 / ETC 기타';
-- ALTER TABLE CHAMBER_ETC_USE_TB ADD KEY IDX_CHAMBER_ETC_USE_MEM (MEM_ID, END_DT);   -- 1인 1시험 확인

-- 고객 입회 시험 (2026-11-19 추가): 시험원 미지정 + 입회자
-- ALTER TABLE CHAMBER_ETC_USE_TB MODIFY MEM_ID VARCHAR(20) NULL COMMENT '시험원 (1인 1시험 확인 기준). 고객 입회 시험은 NULL (시험원 미지정)';
-- ALTER TABLE CHAMBER_ETC_USE_TB ADD COLUMN WITNESS_NAME VARCHAR(100) NULL COMMENT '고객 입회자 이름 (소속). 값이 있으면 입회 시험 (시험원 미지정)' AFTER REG_MEM_ID;

-- 시험장 전체 점유 · 사유 변경 · CTI/4-39 (2026-11-20 추가). chamber-svc/sql/chamber_whole_2026-11-20.sql
-- ALTER TABLE CHAMBER_ETC_USE_TB ADD COLUMN WHOLE_YN TINYINT NOT NULL DEFAULT 0 COMMENT '시험장 전체 점유 (시험번호 없이 사용 + 항목 선택 안 함). 1이면 ZONE_SEQ 는 그 챔버의 첫 구역이고 챔버의 모든 구역을 점유' AFTER WITNESS_NAME;
-- ALTER TABLE CHAMBER_ETC_USE_TB MODIFY PURPOSE VARCHAR(10) NOT NULL COMMENT 'TEST 시험(시험번호 있음) / NORD 로데이터 연동 없는 시험(CTI·4-39) / 시험번호 없이 사용 사유: PRE 사전 점검 / CHECK 장비 점검 / DEBUG 디버깅 / ETC 기타 (WITNESS 입회는 예전 값)';


-- ---------------------------------------------------------------------
-- 3. 챔버 목록 — chamber-svc/sql/chamber_list.sql (+ chamber_link_2026-11-19.sql)
-- ---------------------------------------------------------------------


-- ---------------------------------------------------------------------
-- 3-1. TEL 전용 숨김 챔버 (2026-11-19)
--    TEL(KS X 3078) 측정은 챔버를 고르지 않고 항상 이 구역으로 시작한다 (ERP RawEnvServiceImpl.usesFixedZone).
--    ERP 는 "숨김 챔버(USE_YN=0) + MET_KEY='TEL' 매핑" 으로 찾는다 → 챔버 USE_YN 은 0 으로 둔다
--    (1 로 바꾸면 현황판에 보이고 ERP TEL [시작]이 "선택할 수 없는 챔버" 로 막힌다).
--    MULTI_YN=1 : 여러 TEL 시험이 동시에 진행돼도 막지 않는다 (1인 1시험은 확인).
--    로컬·개발·운영 DB 모두 한 번 실행한다. 이미 있으면(아래 확인 결과 1) 실행하지 않는다.
-- ---------------------------------------------------------------------
SELECT COUNT(*) AS TEL_ZONE_CNT
  FROM CHAMBER_ZONE_MET_TB m
 INNER JOIN CHAMBER_ZONE_TB z ON z.ZONE_SEQ = m.ZONE_SEQ
 INNER JOIN CHAMBER_TB c ON c.CHAMBER_SEQ = z.CHAMBER_SEQ AND c.USE_YN = 0
 WHERE m.MET_KEY = 'TEL';

INSERT INTO CHAMBER_TB (NAME, FLOOR, SOLO_YN, SORT_NO, USE_YN, INS_MEM_ID, INS_DT)
VALUES ('TEL', 1, 0, 999, 0, 'system', NOW());
SET @TEL_CHAMBER = LAST_INSERT_ID();

INSERT INTO CHAMBER_ZONE_TB (CHAMBER_SEQ, NAME, MULTI_YN, LINK_ZONE_SEQ, SORT_NO, USE_YN, INS_MEM_ID, INS_DT)
VALUES (@TEL_CHAMBER, '', 1, NULL, 1, 1, 'system', NOW());
SET @TEL_ZONE = LAST_INSERT_ID();

INSERT INTO CHAMBER_ZONE_MET_TB (ZONE_SEQ, MET_KEY) VALUES (@TEL_ZONE, 'TEL');


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
