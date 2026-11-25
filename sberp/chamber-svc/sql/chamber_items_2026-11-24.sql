-- =====================================================================
-- 시험장 · 시험 항목을 기획 v8 "시험장 · 시험 항목 · 장비 이관" 표에 맞춘다 (MariaDB 10.1, erp) — 2026-11-24
--
-- 기획 v8 표: 모든 시험장에 시험 항목이 있다. 지금 DB 는 항목이 하나뿐인 시험장의 구역 이름이 빈 값('')이라
--             현황판 · 모니터 화면에서 항목 칩이 나오지 않았다 (RE (10m) 은 나오고 DP / LLA 는 안 나옴).
--
-- 바꾸는 것 (구역 이름이 빈 값인 것만 바꾼다. 이미 바꾼 DB 에 다시 실행해도 그대로)
--   RS #3        : '' → RS
--   RS #2        : '' → RS
--   RS #4        : '' → RS
--   RE (Above)   : '' → RE (Above)
--   3F_RE (3m)   : '' → RE (Below)
--   DP / LLA     : '' → DP/LLA, H/F 추가. 둘 중 1개 선택 (SOLO_YN=1). DP 시험방법 매핑은 DP/LLA 에 그대로
--   3F_RE (10m)  : '' → RE (Below), RE (Above) 추가. 한 번에 1건 (SOLO_YN=1). 장비 이관 없음
-- 그대로인 것
--   EMS #1 · EMS #2 · CE · 3F_CE (항목별 구역), RE (3m) · RE (10m) (chamber_link_2026-11-19.sql),
--   장비 이관: RE (Above) → RE (3m) · RE (10m), RS #3 → RE (10m) (RS (#3실))
--
-- 실행 전 : 아래 @MEM 을 실행하는 사람의 ERP 아이디로 바꾼다.
-- 실행 뒤  : 새로 생긴 구역(H/F, 3F_RE (10m) RE (Above))은 ERP 시험방법 매핑이 없다.
--            필요하면 챔버 관리 화면에서 매핑한다 (매핑이 없어도 현황판 [사용 시작]은 된다).
-- 실행     : docker cp chamber_items_2026-11-24.sql mariadb:/tmp/chamber_items.sql
--            docker exec -it mariadb mysql --default-character-set=utf8 -u root -p erp -e "source /tmp/chamber_items.sql"
-- =====================================================================

USE erp;

SET @MEM = 'system';   -- 실행자 아이디 (실행 전에 바꾼다)

-- 0. 확인 — 바꾸기 전 구역
SELECT c.NAME AS CHAMBER, c.SOLO_YN, z.ZONE_SEQ, z.NAME AS ZONE, z.USE_YN
  FROM CHAMBER_TB c INNER JOIN CHAMBER_ZONE_TB z ON z.CHAMBER_SEQ = c.CHAMBER_SEQ
 WHERE c.NAME IN ('RS #3', 'RS #2', 'RS #4', 'RE (Above)', '3F_RE (3m)', 'DP / LLA', '3F_RE (10m)') AND c.USE_YN = 1
 ORDER BY c.FLOOR, c.SORT_NO, z.SORT_NO;

START TRANSACTION;

-- 1. 항목이 하나뿐인 시험장: 빈 구역 이름 → 항목 이름
UPDATE CHAMBER_ZONE_TB z INNER JOIN CHAMBER_TB c ON c.CHAMBER_SEQ = z.CHAMBER_SEQ
   SET z.NAME = CASE c.NAME WHEN 'RS #3' THEN 'RS' WHEN 'RS #2' THEN 'RS' WHEN 'RS #4' THEN 'RS'
                            WHEN 'RE (Above)' THEN 'RE (Above)' WHEN '3F_RE (3m)' THEN 'RE (Below)' END,
       z.UDT_MEM_ID = @MEM, z.UDT_DT = NOW()
 WHERE c.NAME IN ('RS #3', 'RS #2', 'RS #4', 'RE (Above)', '3F_RE (3m)') AND c.USE_YN = 1 AND z.NAME = '';

-- 2. DP / LLA: DP/LLA · H/F 중 1개 선택
SET @DP = (SELECT CHAMBER_SEQ FROM CHAMBER_TB WHERE NAME = 'DP / LLA' AND USE_YN = 1 LIMIT 1);
UPDATE CHAMBER_TB SET SOLO_YN = 1, UDT_MEM_ID = @MEM, UDT_DT = NOW() WHERE CHAMBER_SEQ = @DP;
UPDATE CHAMBER_ZONE_TB SET NAME = 'DP/LLA', SORT_NO = 1, UDT_MEM_ID = @MEM, UDT_DT = NOW()
 WHERE CHAMBER_SEQ = @DP AND NAME = '';
INSERT INTO CHAMBER_ZONE_TB (CHAMBER_SEQ, NAME, MULTI_YN, LINK_ZONE_SEQ, SORT_NO, USE_YN, INS_MEM_ID, INS_DT)
SELECT @DP, 'H/F', 0, NULL, 2, 1, @MEM, NOW() FROM DUAL
 WHERE @DP IS NOT NULL AND NOT EXISTS (SELECT 1 FROM CHAMBER_ZONE_TB WHERE CHAMBER_SEQ = @DP AND NAME = 'H/F');

-- 3. 3F_RE (10m): RE (Below) · RE (Above), 한 번에 1건
SET @RE10_3F = (SELECT CHAMBER_SEQ FROM CHAMBER_TB WHERE NAME = '3F_RE (10m)' AND USE_YN = 1 LIMIT 1);
UPDATE CHAMBER_TB SET SOLO_YN = 1, UDT_MEM_ID = @MEM, UDT_DT = NOW() WHERE CHAMBER_SEQ = @RE10_3F;
UPDATE CHAMBER_ZONE_TB SET NAME = 'RE (Below)', SORT_NO = 1, UDT_MEM_ID = @MEM, UDT_DT = NOW()
 WHERE CHAMBER_SEQ = @RE10_3F AND NAME = '';
INSERT INTO CHAMBER_ZONE_TB (CHAMBER_SEQ, NAME, MULTI_YN, LINK_ZONE_SEQ, SORT_NO, USE_YN, INS_MEM_ID, INS_DT)
SELECT @RE10_3F, 'RE (Above)', 0, NULL, 2, 1, @MEM, NOW() FROM DUAL
 WHERE @RE10_3F IS NOT NULL AND NOT EXISTS (SELECT 1 FROM CHAMBER_ZONE_TB WHERE CHAMBER_SEQ = @RE10_3F AND NAME = 'RE (Above)');

COMMIT;

-- 4. 확인 — 이름이 빈 구역이 없어야 한다 (첫 줄 0), 그 아래는 전체 목록
SELECT COUNT(*) AS EMPTY_ZONE_NAMES
  FROM CHAMBER_ZONE_TB z INNER JOIN CHAMBER_TB c ON c.CHAMBER_SEQ = z.CHAMBER_SEQ
 WHERE c.USE_YN = 1 AND z.USE_YN = 1 AND z.NAME = '';
SELECT c.FLOOR, c.NAME AS CHAMBER, c.SOLO_YN, z.ZONE_SEQ, z.NAME AS ZONE,
       (SELECT CONCAT(lc.NAME, IF(lz.NAME = '', '', CONCAT(' / ', lz.NAME)))
          FROM CHAMBER_ZONE_TB lz INNER JOIN CHAMBER_TB lc ON lc.CHAMBER_SEQ = lz.CHAMBER_SEQ
         WHERE lz.ZONE_SEQ = z.LINK_ZONE_SEQ) AS LINK_TO,
       (SELECT GROUP_CONCAT(m.MET_KEY ORDER BY m.MET_KEY) FROM CHAMBER_ZONE_MET_TB m WHERE m.ZONE_SEQ = z.ZONE_SEQ) AS MET_KEYS
  FROM CHAMBER_TB c INNER JOIN CHAMBER_ZONE_TB z ON z.CHAMBER_SEQ = c.CHAMBER_SEQ
 WHERE c.USE_YN = 1 AND z.USE_YN = 1
 ORDER BY c.FLOOR, c.SORT_NO, z.SORT_NO;
