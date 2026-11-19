-- =====================================================================
-- RE (3m)·RE (10m) 항목 구역 + 연계 구역(장비 이관) + 단독 사용 (MariaDB 10.1, erp) — 2026-11-19
--
-- 바꾸는 것
--   RE (3m)  : 구역 1개(빈 이름) → RE (Below), RE (Above). 한 번에 하나만 (SOLO_YN=1)
--              RE (Above) 구역을 쓰면 RE (Above) 챔버도 함께 점유
--   RE (10m) : 구역 1개(빈 이름) → RE (Below), RE (Above), RS (#3실). 한 번에 하나만 (SOLO_YN=1)
--              RE (Above) → RE (Above) 챔버, RS (#3실) → RS #3 챔버도 함께 점유
--   RS 시험방법 매핑을 RE (10m) 의 RS (#3실) 구역으로 옮긴다 (로데이터 RS 측정 → RS #3 함께 점유)
--
-- 실행 전 : 1) src/main/resources/db/CHAMBER_TB.sql 의 "2026-11-19 추가" ALTER 를 먼저 실행한다.
--           2) 아래 @MEM 을 실행하는 사람의 ERP 아이디로 바꾼다.
--           3) "0. 확인" 결과: 네 값이 모두 있어야 하고, 진행 중 사용이 0 건이어야 한다.
--              (진행 중 측정 행이 RE (10m) 에 있으면 RS 매핑을 옮겨도 그 행은 RE (Below) 구역에 남는다)
-- 실행 뒤  : RE (Below)·RE (Above) 구역에 ERP 시험방법(RE1·RE2·RE3)을 챔버 관리 화면에서 매핑한다.
--           매핑이 없는 구역은 ERP 로데이터 [시작]의 구역 목록에 나오지 않는다 (현황판 [사용 시작]은 된다).
-- 이름으로 찾으므로 chamber_list.sql 로 넣은 DB 에도, 관리 화면으로 넣은 DB 에도 쓸 수 있다.
-- =====================================================================

USE erp;

SET @MEM = 'system';   -- 실행자 아이디 (실행 전에 바꾼다)

SET @RE3   = (SELECT CHAMBER_SEQ FROM CHAMBER_TB WHERE NAME = 'RE (3m)'    AND USE_YN = 1 LIMIT 1);
SET @RE10  = (SELECT CHAMBER_SEQ FROM CHAMBER_TB WHERE NAME = 'RE (10m)'   AND USE_YN = 1 LIMIT 1);
SET @REA_Z = (SELECT z.ZONE_SEQ FROM CHAMBER_ZONE_TB z INNER JOIN CHAMBER_TB c ON c.CHAMBER_SEQ = z.CHAMBER_SEQ
               WHERE c.NAME = 'RE (Above)' AND c.USE_YN = 1 AND z.USE_YN = 1 ORDER BY z.SORT_NO, z.ZONE_SEQ LIMIT 1);
SET @RS3_Z = (SELECT z.ZONE_SEQ FROM CHAMBER_ZONE_TB z INNER JOIN CHAMBER_TB c ON c.CHAMBER_SEQ = z.CHAMBER_SEQ
               WHERE c.NAME = 'RS #3' AND c.USE_YN = 1 AND z.USE_YN = 1 ORDER BY z.SORT_NO, z.ZONE_SEQ LIMIT 1);

-- 0. 확인 — 네 값이 모두 있어야 한다
SELECT @RE3 AS RE3_CHAMBER, @RE10 AS RE10_CHAMBER, @REA_Z AS RE_ABOVE_ZONE, @RS3_Z AS RS3_ZONE;
-- 0. 확인 — 두 시험장의 진행 중 사용 (0 건이어야 한다)
SELECT 'RAW' AS SRC, e.ENV_SEQ AS SEQ, e.ZONE_SEQ, e.MET_KEY, e.MEM_NAME, e.TEST_S_DT
  FROM RAW_ENV_TB e INNER JOIN CHAMBER_ZONE_TB z ON z.ZONE_SEQ = e.ZONE_SEQ
 WHERE z.CHAMBER_SEQ IN (@RE3, @RE10)
   AND e.DEL_YN = 0 AND e.MIG_YN = 0 AND e.TEST_S_DT IS NOT NULL AND e.TEST_E_DT IS NULL
UNION ALL
SELECT 'ETC', x.USE_SEQ, x.ZONE_SEQ, x.PURPOSE, x.MEM_NAME, x.START_DT
  FROM CHAMBER_ETC_USE_TB x INNER JOIN CHAMBER_ZONE_TB z ON z.ZONE_SEQ = x.ZONE_SEQ
 WHERE z.CHAMBER_SEQ IN (@RE3, @RE10) AND x.DEL_YN = 0 AND x.END_DT IS NULL;

START TRANSACTION;

-- 1. 단독 사용
UPDATE CHAMBER_TB SET SOLO_YN = 1, UDT_MEM_ID = @MEM, UDT_DT = NOW() WHERE CHAMBER_SEQ IN (@RE3, @RE10);

-- 2. RE (3m): 기존 구역(빈 이름) → RE (Below), RE (Above) 추가 (연계: RE (Above) 챔버)
UPDATE CHAMBER_ZONE_TB SET NAME = 'RE (Below)', SORT_NO = 1, UDT_MEM_ID = @MEM, UDT_DT = NOW()
 WHERE CHAMBER_SEQ = @RE3 AND NAME = '';
INSERT INTO CHAMBER_ZONE_TB (CHAMBER_SEQ, NAME, MULTI_YN, LINK_ZONE_SEQ, SORT_NO, USE_YN, INS_MEM_ID, INS_DT)
VALUES (@RE3, 'RE (Above)', 0, @REA_Z, 2, 1, @MEM, NOW());

-- 3. RE (10m): 기존 구역(빈 이름) → RE (Below), RE (Above)·RS (#3실) 추가
UPDATE CHAMBER_ZONE_TB SET NAME = 'RE (Below)', SORT_NO = 1, UDT_MEM_ID = @MEM, UDT_DT = NOW()
 WHERE CHAMBER_SEQ = @RE10 AND NAME = '';
SET @RE10_BELOW = (SELECT ZONE_SEQ FROM CHAMBER_ZONE_TB WHERE CHAMBER_SEQ = @RE10 AND NAME = 'RE (Below)' LIMIT 1);
INSERT INTO CHAMBER_ZONE_TB (CHAMBER_SEQ, NAME, MULTI_YN, LINK_ZONE_SEQ, SORT_NO, USE_YN, INS_MEM_ID, INS_DT)
VALUES (@RE10, 'RE (Above)', 0, @REA_Z, 2, 1, @MEM, NOW());
INSERT INTO CHAMBER_ZONE_TB (CHAMBER_SEQ, NAME, MULTI_YN, LINK_ZONE_SEQ, SORT_NO, USE_YN, INS_MEM_ID, INS_DT)
VALUES (@RE10, 'RS (#3실)', 0, @RS3_Z, 3, 1, @MEM, NOW());
SET @RE10_RS = LAST_INSERT_ID();

-- 4. RS 시험방법 매핑: RE (10m) RE (Below) → RS (#3실)
UPDATE CHAMBER_ZONE_MET_TB SET ZONE_SEQ = @RE10_RS WHERE ZONE_SEQ = @RE10_BELOW AND MET_KEY = 'RS';

COMMIT;

-- 5. 확인
SELECT c.NAME AS CHAMBER, c.SOLO_YN, z.ZONE_SEQ, z.NAME AS ZONE, z.LINK_ZONE_SEQ,
       (SELECT CONCAT(lc.NAME, IF(lz.NAME = '', '', CONCAT(' / ', lz.NAME)))
          FROM CHAMBER_ZONE_TB lz INNER JOIN CHAMBER_TB lc ON lc.CHAMBER_SEQ = lz.CHAMBER_SEQ
         WHERE lz.ZONE_SEQ = z.LINK_ZONE_SEQ) AS LINK_TO,
       (SELECT GROUP_CONCAT(m.MET_KEY ORDER BY m.MET_KEY) FROM CHAMBER_ZONE_MET_TB m WHERE m.ZONE_SEQ = z.ZONE_SEQ) AS MET_KEYS
  FROM CHAMBER_TB c INNER JOIN CHAMBER_ZONE_TB z ON z.CHAMBER_SEQ = c.CHAMBER_SEQ
 WHERE c.CHAMBER_SEQ IN (@RE3, @RE10)
 ORDER BY c.SORT_NO, z.SORT_NO;
