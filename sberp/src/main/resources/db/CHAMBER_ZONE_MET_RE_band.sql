-- =====================================================================
-- RE 챔버 매핑을 시험방법 키(RE1·RE2·RE3)에서 대역(RA·RB)으로 바꾼다 (2026-11-25, #26-8)
-- 대상 DB : MariaDB 10.1 (erp) — 로컬·개발·운영
--
-- 이유 : RE1~RE3 의 Below·Above 가 규격마다 다르다
--          9832·9835·3124·3126 : RE1(hz1) = RA = RE (Below), RE2(hz2) = RB = RE (Above)
--          9814               : RE1(hz1)·RE2(hz2) = RA = RE (Below), RE3(hz3) = RB = RE (Above)
--        → 챔버 매핑은 대역으로 하고, ERP 가 로데이터 규격 + RE1~RE3 으로 대역을 정해 확인한다
--          (RawEnvServiceImpl.zoneMetKey). 챔버 목록은 zones.do?metKey=RA / RB.
--
-- 반영 : ERP 코드(zoneMetKey)와 같이 반영한다. 이 SQL 만 먼저 돌리면 RE [시작]이 전부 막힌다.
-- 이름 : chamber_list.sql + chamber_link_2026-11-19.sql 의 시험장·구역 이름으로 찾는다 (번호는 DB 마다 다를 수 있음)
-- =====================================================================

USE erp;

-- 0. 실행 전 — RE 구역과 지금 매핑
SELECT c.NAME AS CHAMBER, z.ZONE_SEQ, z.NAME AS ZONE,
       (SELECT GROUP_CONCAT(m.MET_KEY ORDER BY m.MET_KEY) FROM CHAMBER_ZONE_MET_TB m WHERE m.ZONE_SEQ = z.ZONE_SEQ) AS MET_KEYS
  FROM CHAMBER_TB c
 INNER JOIN CHAMBER_ZONE_TB z ON z.CHAMBER_SEQ = c.CHAMBER_SEQ
 WHERE c.NAME IN ('RE (Above)', 'RE (3m)', 'RE (10m)', '3F_RE (3m)', '3F_RE (10m)')
 ORDER BY c.FLOOR, c.SORT_NO, z.SORT_NO;

START TRANSACTION;

-- 1. RE1·RE2·RE3 매핑 삭제 (RE 대역은 RA·RB 로만)
DELETE FROM CHAMBER_ZONE_MET_TB WHERE MET_KEY IN ('RE1', 'RE2', 'RE3');

-- 2. RA = 구역 이름 'RE (Below)'
INSERT IGNORE INTO CHAMBER_ZONE_MET_TB (ZONE_SEQ, MET_KEY)
SELECT z.ZONE_SEQ, 'RA'
  FROM CHAMBER_TB c
 INNER JOIN CHAMBER_ZONE_TB z ON z.CHAMBER_SEQ = c.CHAMBER_SEQ
 WHERE c.NAME IN ('RE (3m)', 'RE (10m)', '3F_RE (3m)', '3F_RE (10m)')
   AND z.NAME = 'RE (Below)';

-- 3. RB = 구역 이름 'RE (Above)' (RE (3m)·RE (10m) 의 것은 장비 이관 LINK_ZONE_SEQ 구역) + RE (Above) 시험장
INSERT IGNORE INTO CHAMBER_ZONE_MET_TB (ZONE_SEQ, MET_KEY)
SELECT z.ZONE_SEQ, 'RB'
  FROM CHAMBER_TB c
 INNER JOIN CHAMBER_ZONE_TB z ON z.CHAMBER_SEQ = c.CHAMBER_SEQ
 WHERE c.NAME = 'RE (Above)'
    OR (c.NAME IN ('RE (3m)', 'RE (10m)', '3F_RE (3m)', '3F_RE (10m)') AND z.NAME = 'RE (Above)');

COMMIT;

-- 4. 확인 — 기대 : 구역 이름 RE (Below) = RA, RE (Above) = RB, RE (Above) 시험장 = RB
--               RE1·RE2·RE3 매핑 0건
SELECT c.NAME AS CHAMBER, z.ZONE_SEQ, z.NAME AS ZONE,
       (SELECT GROUP_CONCAT(m.MET_KEY ORDER BY m.MET_KEY) FROM CHAMBER_ZONE_MET_TB m WHERE m.ZONE_SEQ = z.ZONE_SEQ) AS MET_KEYS
  FROM CHAMBER_TB c
 INNER JOIN CHAMBER_ZONE_TB z ON z.CHAMBER_SEQ = c.CHAMBER_SEQ
 WHERE c.NAME IN ('RE (Above)', 'RE (3m)', 'RE (10m)', '3F_RE (3m)', '3F_RE (10m)')
 ORDER BY c.FLOOR, c.SORT_NO, z.SORT_NO;

SELECT COUNT(*) AS OLD_RE_KEYS FROM CHAMBER_ZONE_MET_TB WHERE MET_KEY IN ('RE1', 'RE2', 'RE3');
