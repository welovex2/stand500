-- 창고 · 구역
-- IM_SUB_TB.CURRENT_ZONE_SEQ 가 이미 있으면 해당 ALTER 만 건너뛴다.

DROP TABLE IF EXISTS `ZONE_TB`;
DROP TABLE IF EXISTS `WH_TB`;
DROP TABLE IF EXISTS `STR_TB`;

CREATE TABLE `STR_TB` (
  `STR_SEQ`    int          NOT NULL AUTO_INCREMENT,
  `STR_NAME`   varchar(100) NOT NULL                COMMENT '창고명',
  `STR_LOC`    varchar(200) DEFAULT NULL            COMMENT '위치. 자유텍스트',
  `TYPE_CODE`  varchar(10)  NOT NULL DEFAULT '01'   COMMENT '형태(공통코드:WH) 01 방 02 복도 영역 03 기타',
  `MNG_ID`     varchar(20)  DEFAULT NULL            COMMENT '창고 관리자. MEMBER_TB.ID. 선택',
  `DEPT_SEQ`   int          DEFAULT NULL            COMMENT '담당 부서. DEPT_TB.DEPT_SEQ. 선택',
  `INS_MEM_ID` varchar(20)  DEFAULT NULL,
  `INS_DT`     datetime     DEFAULT CURRENT_TIMESTAMP,
  `UDT_MEM_ID` varchar(20)  DEFAULT NULL,
  `UDT_DT`     datetime     DEFAULT CURRENT_TIMESTAMP,
  `STATE`      varchar(1)   NOT NULL DEFAULT 'I',
  PRIMARY KEY (`STR_SEQ`),
  KEY `IDX_STR_MNG` (`MNG_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='창고';

CREATE TABLE `ZONE_TB` (
  `ZONE_SEQ`   int          NOT NULL AUTO_INCREMENT,
  `STR_SEQ`    int          NOT NULL                COMMENT 'STR_TB.STR_SEQ',
  `ZONE_NAME`  varchar(100) NOT NULL                COMMENT '구역명. 사용 중인 구역끼리 한 창고 안에서 중복 불가',
  `ZONE_DESC`  varchar(200) DEFAULT NULL            COMMENT '구역설명. 선택',
  `USE_CODE`   varchar(2)   NOT NULL DEFAULT '01'   COMMENT '용도 01 일반 02 인수요청 03 장기',
  `INS_MEM_ID` varchar(20)  DEFAULT NULL,
  `INS_DT`     datetime     DEFAULT CURRENT_TIMESTAMP,
  `UDT_MEM_ID` varchar(20)  DEFAULT NULL,
  `UDT_DT`     datetime     DEFAULT CURRENT_TIMESTAMP,
  `STATE`      varchar(1)   NOT NULL DEFAULT 'I',
  PRIMARY KEY (`ZONE_SEQ`),
  KEY `IDX_ZONE_STR` (`STR_SEQ`,`ZONE_SEQ`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='창고 구역';

-- 구역명 중복은 UNIQUE 를 걸지 않고 앱에서 검사한다.
-- 소프트 삭제(STATE='D')를 쓰기 때문에, 사용 중지한 구역명은 다시 만들 수 있다.

-- 시료가 놓인 구역. 값 채우기는 위치 스캔에서 한다.
ALTER TABLE `IM_SUB_TB`
  ADD COLUMN `CURRENT_ZONE_SEQ` int DEFAULT NULL COMMENT '현재 구역. ZONE_TB.ZONE_SEQ';

ALTER TABLE `IM_SUB_TB`
  ADD KEY `IDX_IM_SUB_ZONE` (`CURRENT_ZONE_SEQ`);

-- ===== 공통코드: 창고형태 (TOP_CD = 'WH') =====
INSERT INTO `COMCODE_TB` (`TOP_CD`,`TYPE_CD`,`TYPE_NAME`,`USE_YN`,`DIS_ORDR`,`INS_MEM_ID`,`UDT_MEM_ID`,`STATE`)
SELECT '00','WH','창고형태',1,0,'system','system','I'
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `COMCODE_TB` WHERE `TOP_CD` = '00' AND `TYPE_CD` = 'WH');

INSERT INTO `COMCODE_TB` (`TOP_CD`,`TYPE_CD`,`TYPE_NAME`,`USE_YN`,`DIS_ORDR`,`INS_MEM_ID`,`UDT_MEM_ID`,`STATE`)
SELECT 'WH','01','방',1,1,'system','system','I'
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `COMCODE_TB` WHERE `TOP_CD` = 'WH' AND `TYPE_CD` = '01');

INSERT INTO `COMCODE_TB` (`TOP_CD`,`TYPE_CD`,`TYPE_NAME`,`USE_YN`,`DIS_ORDR`,`INS_MEM_ID`,`UDT_MEM_ID`,`STATE`)
SELECT 'WH','02','복도 영역',1,2,'system','system','I'
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `COMCODE_TB` WHERE `TOP_CD` = 'WH' AND `TYPE_CD` = '02');

INSERT INTO `COMCODE_TB` (`TOP_CD`,`TYPE_CD`,`TYPE_NAME`,`USE_YN`,`DIS_ORDR`,`INS_MEM_ID`,`UDT_MEM_ID`,`STATE`)
SELECT 'WH','03','기타',1,3,'system','system','I'
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `COMCODE_TB` WHERE `TOP_CD` = 'WH' AND `TYPE_CD` = '03');

-- ===== 이미 만든 DB 에 적용할 변경분 (2026-10-16) =====
-- 창고 관리자를 선택으로 바꾸고 담당 부서를 더한다.
ALTER TABLE `STR_TB` MODIFY COLUMN `MNG_ID` varchar(20) DEFAULT NULL COMMENT '창고 관리자. MEMBER_TB.ID. 선택';
ALTER TABLE `STR_TB` ADD COLUMN `DEPT_SEQ` int DEFAULT NULL COMMENT '담당 부서. DEPT_TB.DEPT_SEQ. 선택';

-- 구역 정렬 기능이 없어 DIS_ORDR 를 걷어낸다. 목록은 ZONE_SEQ 순(추가 순)이다.
ALTER TABLE `ZONE_TB` DROP INDEX `IDX_ZONE_STR`;
ALTER TABLE `ZONE_TB` ADD KEY `IDX_ZONE_STR` (`STR_SEQ`,`ZONE_SEQ`);
ALTER TABLE `ZONE_TB` DROP COLUMN `DIS_ORDR`;

-- 창고 메모는 쓰는 화면이 없어 걷어낸다.
ALTER TABLE `STR_TB` DROP COLUMN `MEMO`;
