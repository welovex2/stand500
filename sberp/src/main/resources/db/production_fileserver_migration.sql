-- =============================================================================
-- 파일서버(Nextcloud) 연동 - 운영 DB 반영 스크립트
-- 대상: 운영 DB (172.22.0.44 / schema: erp)
-- 작성: 운영 전환 준비
--
-- 특징
--  - 재실행해도 안전(idempotent)하도록 작성
--  - 신규 테이블: FILE_OP_LOG_TB, FOLDER_META_TB
--  - 기존 테이블 컬럼 보강: FILE_DETAIL_TB (파일서버 연동용 컬럼)
--  - FILE_TB 는 변경 없음
--
-- 실행 전 반드시 운영 DB 백업!
--   mysqldump -h 172.22.0.44 -u erp_user -p erp > erp_backup_YYYYMMDD.sql
-- =============================================================================

-- 안전: 사용할 스키마 명시 (필요 시 주석 해제)
-- USE erp;

-- -----------------------------------------------------------------------------
-- 0) (사전 점검용) 개발 DB와 운영 DB의 컬럼 차이 확인 쿼리
--    개발(40)·운영(44) 각각에서 실행해 결과를 비교하면 누락 컬럼을 정확히 알 수 있다.
-- -----------------------------------------------------------------------------
-- SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT
--   FROM INFORMATION_SCHEMA.COLUMNS
--  WHERE TABLE_SCHEMA = DATABASE()
--    AND TABLE_NAME IN ('FILE_TB','FILE_DETAIL_TB','FOLDER_META_TB','FILE_OP_LOG_TB')
--  ORDER BY TABLE_NAME, ORDINAL_POSITION;


-- -----------------------------------------------------------------------------
-- 1) FILE_OP_LOG_TB : 파일서버 작업 이력(통합). 기존 DOWNLOAD_LOG_TB 대체.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `FILE_OP_LOG_TB` (
  `LOG_ID`       bigint       NOT NULL AUTO_INCREMENT,
  `USER_ID`      varchar(45)  DEFAULT NULL,
  `DEPT`         varchar(100) DEFAULT NULL,
  `SBK_NO`       varchar(30)  DEFAULT NULL,
  `OP_TYPE`      varchar(30)  NOT NULL COMMENT 'UPLOAD/DOWNLOAD/PREVIEW/ONLYOFFICE_OPEN/ONLYOFFICE_SAVE/DELETE/RENAME/MOVE/COPY/MKDIR/CONVERT_TO_PDF 등',
  `UPLOAD_SRC`   char(1)      DEFAULT NULL COMMENT 'E=ERP직접, A=파일모달/NC API',
  `DAV_PATH`     varchar(2000) DEFAULT NULL COMMENT '대상 경로',
  `SRC_PATH`     varchar(2000) DEFAULT NULL COMMENT 'MOVE/COPY 원본',
  `DST_PATH`     varchar(2000) DEFAULT NULL COMMENT 'MOVE/COPY 목적지',
  `IS_FOLDER`    char(1)      DEFAULT 'N',
  `FILE_NAME`    varchar(255) DEFAULT NULL,
  `CONTENT_TYPE` varchar(200) DEFAULT NULL,
  `FILE_SIZE`    bigint       DEFAULT 0,
  `BYTES_SENT`   bigint       DEFAULT 0,
  `CLIENT_IP`    varchar(100) DEFAULT NULL,
  `USER_AGENT`   varchar(500) DEFAULT NULL,
  `RESULT_CD`    varchar(10)  NOT NULL DEFAULT 'START' COMMENT 'START/SUCCESS/FAIL',
  `ERR_MSG`      varchar(1000) DEFAULT NULL,
  `CREAT_DT`     datetime     DEFAULT CURRENT_TIMESTAMP,
  `UPDT_DT`      datetime     DEFAULT NULL,
  PRIMARY KEY (`LOG_ID`),
  KEY `IDX_FILE_OP_LOG_01` (`SBK_NO`,`CREAT_DT`),
  KEY `IDX_FILE_OP_LOG_02` (`USER_ID`,`CREAT_DT`),
  KEY `IDX_FILE_OP_LOG_03` (`OP_TYPE`,`CREAT_DT`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- -----------------------------------------------------------------------------
-- 2) FOLDER_META_TB : 폴더 메타(업로드 출처 E/A 추적). PATH_HASH 기준 upsert.
--    insertFolderMeta / copyFolderMetaByPathPrefix 등에서 사용.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `FOLDER_META_TB` (
  `FOLDER_META_ID` bigint        NOT NULL AUTO_INCREMENT,
  `FOLDER_PATH`    varchar(1024) NOT NULL COMMENT '디코딩된 폴더 경로 (예: /ERP/2026/05/SB26-G0000)',
  `PATH_HASH`      char(64)      NOT NULL COMMENT 'SHA2(FOLDER_PATH, 256)',
  `UPLOAD_SRC`     char(1)       DEFAULT 'A' COMMENT 'E=ERP직접, A=연동',
  `STATE`          char(1)       DEFAULT 'I' COMMENT 'I/A/U/C/D',
  `CREAT_ID`       varchar(45)   DEFAULT NULL,
  `CREATED_AT`     datetime      DEFAULT CURRENT_TIMESTAMP,
  `UPDATE_ID`      varchar(45)   DEFAULT NULL,
  `UPDATED_AT`     datetime      DEFAULT NULL,
  PRIMARY KEY (`FOLDER_META_ID`),
  UNIQUE KEY `UK_FOLDER_META_PATH_HASH` (`PATH_HASH`),
  KEY `IDX_FOLDER_META_PATH` (`FOLDER_PATH`(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- -----------------------------------------------------------------------------
-- 3) FILE_DETAIL_TB : 파일서버 연동용 컬럼 보강 (없을 때만 추가)
--
--    MySQL은 ADD COLUMN IF NOT EXISTS 를 지원하지 않으므로,
--    INFORMATION_SCHEMA 로 존재 여부를 확인 후 ADD 하는 프로시저를 임시로 만든다.
--    (스크립트 마지막에서 DROP)
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS `__add_col_if_missing`;
DELIMITER $$
CREATE PROCEDURE `__add_col_if_missing`(
  IN p_table  VARCHAR(64),
  IN p_column VARCHAR(64),
  IN p_ddl    VARCHAR(1000)
)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME   = p_table
       AND COLUMN_NAME  = p_column
  ) THEN
    SET @sql = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN ', p_ddl);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$
DELIMITER ;

-- 파일서버 연동 코드(fileMapper.xml)에서 사용하는 컬럼들 — 없으면 추가
CALL `__add_col_if_missing`('FILE_DETAIL_TB','UPLOAD_SRC', "`UPLOAD_SRC` char(1) DEFAULT 'E' COMMENT 'E=ERP직접, A=연동'");
CALL `__add_col_if_missing`('FILE_DETAIL_TB','STATE',      "`STATE` char(1) DEFAULT NULL COMMENT 'I/A/U/C/D (D=삭제)'");
CALL `__add_col_if_missing`('FILE_DETAIL_TB','FILE_ORDR',  "`FILE_ORDR` int DEFAULT NULL");
CALL `__add_col_if_missing`('FILE_DETAIL_TB','FILE_MEMO',  "`FILE_MEMO` varchar(500) DEFAULT NULL");
CALL `__add_col_if_missing`('FILE_DETAIL_TB','FILE_CN',    "`FILE_CN` varchar(500) DEFAULT NULL");
CALL `__add_col_if_missing`('FILE_DETAIL_TB','CREAT_ID',   "`CREAT_ID` varchar(45) DEFAULT NULL");
CALL `__add_col_if_missing`('FILE_DETAIL_TB','CREAT_DT',   "`CREAT_DT` datetime DEFAULT NULL");
CALL `__add_col_if_missing`('FILE_DETAIL_TB','UPDATE_ID',  "`UPDATE_ID` varchar(45) DEFAULT NULL");
CALL `__add_col_if_missing`('FILE_DETAIL_TB','UPDATE_DT',  "`UPDATE_DT` datetime DEFAULT NULL");

DROP PROCEDURE IF EXISTS `__add_col_if_missing`;


-- -----------------------------------------------------------------------------
-- 4) 반영 후 확인
-- -----------------------------------------------------------------------------
-- SHOW TABLES LIKE 'FILE_OP_LOG_TB';
-- SHOW TABLES LIKE 'FOLDER_META_TB';
-- SHOW COLUMNS FROM FILE_DETAIL_TB;
