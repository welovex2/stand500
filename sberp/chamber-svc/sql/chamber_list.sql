-- =====================================================================
-- 챔버(시험장)·구역·시험방법 목록 (MariaDB 10.1, erp)
-- 대상 : CHAMBER_TB, CHAMBER_ZONE_TB, CHAMBER_ZONE_MET_TB
--
-- 출처 : 노션 "[RD&현황판] 시험실,시험항목 명칭 재정비 및 매칭표" 의 "현황판 챔버별 시험항목 정리" 표
--        (변경될 시험장별 명칭 = 확인 완, 2026-11-13 1차 픽스). 2026-11-16 에 표와 대조했다.
--        시안(시험장_현황판_v5_1.html)의 시험장·항목 목록과 같다.
--
-- 표에는 있지만 테이블에 담지 못한 것 (칸이 없다)
--   · 동시 진행 수 : EMS #1·EMS #2 는 한 번에 3개, CE 는 한 번에 2개까지. 구역은 각각 따로 쓸 수 있어 막지 않는다.
--   · 연계 사용    : RE (3m)·RE (10m) 에서 RE (Above) 장비를, RE (10m) 에서 RS #3 장비를 가져와 쓰면
--                    그 시험장도 함께 [사용 중] 이어야 한다.
--                    → 2026-11-19: 이 파일을 넣은 뒤 chamber_link_2026-11-19.sql 로 RE (3m)·RE (10m) 를
--                      항목 구역으로 나누고 연계 구역(LINK_ZONE_SEQ)·단독 사용(SOLO_YN)을 넣는다.
--
-- 모양 : 사용 단위는 구역이다.
--        동시에 여러 시험을 하는 시험장(EMS #1, EMS #2, CE, 3F_CE)은 시험 항목마다 구역 1개.
--        한 번에 하나만 쓰는 시험장은 구역 1개(이름은 빈 값).
--        층은 FLOOR 칸 (1층 10개, 3층 3개). 1층 평면도 자리는 index.html 의 PLAN (이름으로 찾음).
--
-- 실행 전 : 0) CHAMBER_TB 에 FLOOR 칸이 있어야 한다 (없으면 CHAMBER_TB.sql 의 ALTER 를 먼저 실행).
--           1) 아래 @MEM 을 등록자 ERP 아이디로 바꾼다.
--           2) 개발 DB 에 dev_seed.sql 의 시험용 목록(9001~)이 있으면 그 파일 맨 아래 "정리"로 먼저 지운다.
--           3) 번호(SEQ)를 직접 넣는다. 같은 번호가 이미 있으면 오류로 멈춘다 (덮어쓰지 않는다).
-- =====================================================================

USE erp;

SET @MEM = 'system';   -- 등록자 아이디 (실행 전에 바꾼다)

-- 0. 실행 전 확인 — 세 줄 모두 0 이어야 한다
SELECT 'CHAMBER_TB' AS T, COUNT(*) AS CNT FROM CHAMBER_TB WHERE CHAMBER_SEQ BETWEEN 1 AND 13
UNION ALL SELECT 'CHAMBER_ZONE_TB', COUNT(*) FROM CHAMBER_ZONE_TB WHERE ZONE_SEQ BETWEEN 1 AND 24
UNION ALL SELECT 'CHAMBER_ZONE_MET_TB', COUNT(*) FROM CHAMBER_ZONE_MET_TB WHERE ZONE_SEQ BETWEEN 1 AND 24;

START TRANSACTION;

-- ---------------------------------------------------------------------
-- 1. 챔버 (시험장) 13개 — 1층 10, 3층 3
-- ---------------------------------------------------------------------
INSERT INTO CHAMBER_TB (CHAMBER_SEQ, NAME, FLOOR, SORT_NO, USE_YN, INS_MEM_ID, INS_DT) VALUES
    ( 1, 'RS #3', 1,        1, 1, @MEM, NOW()),
    ( 2, 'EMS #1', 1,       2, 1, @MEM, NOW()),
    ( 3, 'EMS #2', 1,       3, 1, @MEM, NOW()),
    ( 4, 'RS #2', 1,        4, 1, @MEM, NOW()),
    ( 5, 'CE', 1,           5, 1, @MEM, NOW()),
    ( 6, 'RS #4', 1,        6, 1, @MEM, NOW()),
    ( 7, 'DP / LLA', 1,     7, 1, @MEM, NOW()),
    ( 8, 'RE (Above)', 1,   8, 1, @MEM, NOW()),
    ( 9, 'RE (3m)', 1,      9, 1, @MEM, NOW()),
    (10, 'RE (10m)', 1,    10, 1, @MEM, NOW()),
    (11, '3F_RE (3m)', 3,  11, 1, @MEM, NOW()),
    (12, '3F_RE (10m)', 3, 12, 1, @MEM, NOW()),
    (13, '3F_CE', 3,       13, 1, @MEM, NOW());

-- ---------------------------------------------------------------------
-- 2. 구역 24개
--    오른쪽 주석 = 시안의 시험 항목 (구역이 하나인 시험장은 그 구역에서 하는 항목 전부)
-- ---------------------------------------------------------------------
INSERT INTO CHAMBER_ZONE_TB (ZONE_SEQ, CHAMBER_SEQ, NAME, MULTI_YN, SORT_NO, USE_YN, INS_MEM_ID, INS_DT) VALUES
    ( 1,  1, '',        0, 1, 1, @MEM, NOW()),   -- RS #3       : RS
    ( 2,  2, 'BSD #1',  0, 1, 1, @MEM, NOW()),   -- EMS #1 (동시 4)
    ( 3,  2, 'CS #1',   0, 2, 1, @MEM, NOW()),
    ( 4,  2, 'CS #2',   0, 3, 1, @MEM, NOW()),
    ( 5,  2, 'CTI',     0, 4, 1, @MEM, NOW()),
    ( 6,  3, 'ESD #1',  0, 1, 1, @MEM, NOW()),   -- EMS #2 (동시 5)
    ( 7,  3, 'ESD #2',  0, 2, 1, @MEM, NOW()),
    ( 8,  3, 'BSD #2',  0, 3, 1, @MEM, NOW()),
    ( 9,  3, 'M-Field', 0, 4, 1, @MEM, NOW()),
    (10,  3, '4-39',    0, 5, 1, @MEM, NOW()),
    (11,  4, '',        0, 1, 1, @MEM, NOW()),   -- RS #2       : RS
    (12,  5, 'CE #1',   0, 1, 1, @MEM, NOW()),   -- CE (동시 3)
    (13,  5, 'CE #2',   0, 2, 1, @MEM, NOW()),
    (14,  5, 'CLICK',   0, 3, 1, @MEM, NOW()),
    (15,  6, '',        0, 1, 1, @MEM, NOW()),   -- RS #4       : RS
    (16,  7, '',        0, 1, 1, @MEM, NOW()),   -- DP / LLA    : DP/LLA, H/F
    (17,  8, '',        0, 1, 1, @MEM, NOW()),   -- RE (Above)  : RE (Above)
    (18,  9, '',        0, 1, 1, @MEM, NOW()),   -- RE (3m)     : RE (Below), RE (Above → RE (Above) 시험장 연계)
    (19, 10, '',        0, 1, 1, @MEM, NOW()),   -- RE (10m)    : RE (Below), RE (Above → RE (Above) 연계), RS (#3실 → RS #3 연계)
    (20, 11, '',        0, 1, 1, @MEM, NOW()),   -- 3F_RE (3m)  : RE (Below)
    (21, 12, '',        0, 1, 1, @MEM, NOW()),   -- 3F_RE (10m) : RE (Below/Above)
    (22, 13, 'CE #1',   0, 1, 1, @MEM, NOW()),   -- 3F_CE (동시 3)
    (23, 13, 'CE #2',   0, 2, 1, @MEM, NOW()),
    (24, 13, 'CLICK',   0, 3, 1, @MEM, NOW());

-- ---------------------------------------------------------------------
-- 3. 구역별 시험방법 (ERP EnvMetKey: CE, RE1, RE2, RE3, ESD, RS, EFT, SURGE, CS, MF, VDIP, CLK, DP, TEL)
--    3-1. 표의 항목이 ERP 시험방법과 바로 이어지는 것
--         BSD = Burst / Surge / Dip (표의 설명) → EFT, SURGE, VDIP
-- ---------------------------------------------------------------------
INSERT INTO CHAMBER_ZONE_MET_TB (ZONE_SEQ, MET_KEY) VALUES
    ( 1, 'RS'),                                   -- RS #3
    ( 2, 'EFT'), ( 2, 'SURGE'), ( 2, 'VDIP'),     -- EMS #1 / BSD #1
    ( 3, 'CS'), ( 4, 'CS'),                       -- EMS #1 / CS #1, CS #2
    ( 6, 'ESD'), ( 7, 'ESD'),                     -- EMS #2 / ESD #1, ESD #2
    ( 8, 'EFT'), ( 8, 'SURGE'), ( 8, 'VDIP'),     -- EMS #2 / BSD #2
    ( 9, 'MF'),                                   -- EMS #2 / M-Field
    (11, 'RS'),                                   -- RS #2
    (12, 'CE'), (13, 'CE'),                       -- CE / CE #1, CE #2
    (14, 'CLK'),                                  -- CE / CLICK
    (15, 'RS'),                                   -- RS #4
    (16, 'DP'),                                   -- DP / LLA
    (19, 'RS'),                                   -- RE (10m) : RS (#3실)
    (22, 'CE'), (23, 'CE'),                       -- 3F_CE / CE #1, CE #2
    (24, 'CLK');                                  -- 3F_CE / CLICK

--    3-2. 미정 — 표만으로는 ERP 시험방법 키를 정할 수 없다. 추측으로 넣지 않았다.
--         매핑이 없는 구역은 ERP 측정 [시작]의 구역 목록에 나오지 않는다 (현황판 [사용]은 된다).
--      구역 17~21 (RE 5곳) : 표는 Below 1GHz / Above 1GHz 로 나누는데, ERP 의 RE1·RE2·RE3 은 규격마다
--                            대역이 달라 (RE1 이 9kHz~30MHz 인 규격도, 1GHz 이하인 규격도 있다) 1:1 로 맞지 않는다.
--                            RE 구역 전부에 RE1·RE2·RE3 을 다 넣는 안:
-- INSERT INTO CHAMBER_ZONE_MET_TB (ZONE_SEQ, MET_KEY) VALUES
--     (17, 'RE1'), (17, 'RE2'), (17, 'RE3'), (18, 'RE1'), (18, 'RE2'), (18, 'RE3'), (19, 'RE1'), (19, 'RE2'), (19, 'RE3'),
--     (20, 'RE1'), (20, 'RE2'), (20, 'RE3'), (21, 'RE1'), (21, 'RE2'), (21, 'RE3');
--      구역  5 (EMS #1 / CTI)  : CTI 에 해당하는 ERP 키 확인 필요
--      구역 10 (EMS #2 / 4-39) : ERP 시험방법 키에 없음 (표에서 "추가"된 항목)
--      구역 16 의 H/F          : ERP 시험방법 키에 없음. DP 만 넣었다
--      EMS #1 의 ESD           : 표의 "진행되는 시험"에는 ESD 가 있는데 EMS #1 구역(BSD #1, CS #1, CS #2, CTI)에는
--                                ESD 자리가 없다. ESD 는 EMS #2 의 ESD #1·#2 에만 넣었다

COMMIT;

-- 4. 확인
SELECT c.CHAMBER_SEQ, c.FLOOR, c.NAME AS CHAMBER, z.ZONE_SEQ, z.NAME AS ZONE,
       (SELECT GROUP_CONCAT(m.MET_KEY ORDER BY m.MET_KEY) FROM CHAMBER_ZONE_MET_TB m WHERE m.ZONE_SEQ = z.ZONE_SEQ) AS MET_KEYS
  FROM CHAMBER_TB c
       INNER JOIN CHAMBER_ZONE_TB z ON z.CHAMBER_SEQ = c.CHAMBER_SEQ
 WHERE c.CHAMBER_SEQ BETWEEN 1 AND 13
 ORDER BY c.FLOOR, c.SORT_NO, z.SORT_NO;

-- 번호를 직접 넣었으므로 자동 증가 값이 뒤따라오는지 확인한다 (dev_seed 를 지운 DB 는 9010 에서 이어질 수 있다)
-- ALTER TABLE CHAMBER_TB AUTO_INCREMENT = 14;
-- ALTER TABLE CHAMBER_ZONE_TB AUTO_INCREMENT = 25;

-- 되돌리기 (이 구역을 가리키는 사용 기록이 없을 때만)
-- DELETE FROM CHAMBER_ZONE_MET_TB WHERE ZONE_SEQ BETWEEN 1 AND 24;
-- DELETE FROM CHAMBER_ZONE_TB     WHERE ZONE_SEQ BETWEEN 1 AND 24;
-- DELETE FROM CHAMBER_TB          WHERE CHAMBER_SEQ BETWEEN 1 AND 13;
