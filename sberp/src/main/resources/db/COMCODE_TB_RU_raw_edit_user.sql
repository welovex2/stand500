-- 공통코드 RU : 로데이터 잠금 예외 사용자 (2026-11-27, #26-8)
--
-- 프로젝트완료(TEST_TB.RAW_EDIT_YN = 0)로 잠긴 로데이터도 저장할 수 있는 로그인 아이디.
-- 예전에는 rawMapper.xml selectEditYn 에 아이디 12개가 하드코딩(RAW_EDIT_USER_HOTFIX)돼 있었다.
--
--   TOP_CD    = 'RU'
--   TYPE_CD   = 순번 (01, 02, ...)
--   TYPE_NAME = 로그인 아이디 (MEMBER_TB.ID). 서버는 이 값으로 비교한다
--   USE_YN    = 1 이면 예외 적용, 0 이면 해제
--
-- 쓰는 곳: rawMapper.xml selectEditYn (저장 허용 판단), detail (기본정보 editYn)
--
-- ※ 반영 순서: 이 SQL 을 먼저 실행하고 서버를 배포한다.
--    서버를 먼저 올리면 그사이 12명이 잠긴 로데이터를 저장하지 못한다.
-- ※ 사람 추가·해제는 배포 없이 이 테이블만 고치면 된다 (아래 "추가 / 해제" 참고).

-- 0) 실행 전 확인: 컬럼 구성 (INSERT 컬럼이 다르면 맞춰서 고친다), RU 가 비어 있는지
SHOW COLUMNS FROM COMCODE_TB;
SELECT * FROM COMCODE_TB WHERE TOP_CD = 'RU' OR (TOP_CD = '00' AND TYPE_CD = 'RU');

-- 1) 상위코드 (최상위 '00' 아래에 RU 등록)
INSERT INTO COMCODE_TB (TOP_CD, TYPE_CD, TYPE_NAME, MEMO, USE_YN, DIS_ORDR, INS_MEM_ID, INS_DT)
SELECT '00', 'RU', '로데이터 잠금 예외 사용자', 'TYPE_NAME = 로그인 아이디. 프로젝트완료 후에도 로데이터 저장 허용', 1, 0, 'system', NOW()
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM COMCODE_TB WHERE TOP_CD = '00' AND TYPE_CD = 'RU');

-- 2) 예외 사용자 12명 (기존 하드코딩 목록 그대로)
INSERT INTO COMCODE_TB (TOP_CD, TYPE_CD, TYPE_NAME, MEMO, USE_YN, DIS_ORDR, INS_MEM_ID, INS_DT)
SELECT 'RU', v.TYPE_CD, v.ID, NULL, 1, v.ORDR, 'system', NOW()
  FROM (
        SELECT '01' TYPE_CD, 'justin'  ID,  1 ORDR UNION ALL
        SELECT '02',         'pgm',         2      UNION ALL
        SELECT '03',         'htpark',      3      UNION ALL
        SELECT '04',         'phj',         4      UNION ALL
        SELECT '05',         'jcbae',       5      UNION ALL
        SELECT '06',         'jcba',        6      UNION ALL
        SELECT '07',         'sgh',         7      UNION ALL
        SELECT '08',         'red100y',     8      UNION ALL
        SELECT '09',         'dw.lee',      9      UNION ALL
        SELECT '10',         'sangjun',    10      UNION ALL
        SELECT '11',         'jyh',        11      UNION ALL
        SELECT '12',         'jjh',        12
       ) v
 WHERE NOT EXISTS (SELECT 1 FROM COMCODE_TB c WHERE c.TOP_CD = 'RU' AND c.TYPE_NAME = v.ID);

-- 3) 확인 (12건, 이름이 MEMBER_TB 에 있는지)
SELECT c.TYPE_CD, c.TYPE_NAME AS ID, m.MEM_NAME, c.USE_YN
  FROM COMCODE_TB c
       LEFT JOIN MEMBER_TB m ON m.ID = c.TYPE_NAME
 WHERE c.TOP_CD = 'RU'
 ORDER BY c.DIS_ORDR;

-- ---------------------------------------------------------------------
-- 추가 / 해제 (예시)
-- INSERT INTO COMCODE_TB (TOP_CD, TYPE_CD, TYPE_NAME, USE_YN, DIS_ORDR, INS_MEM_ID, INS_DT)
-- VALUES ('RU', '13', '새아이디', 1, 13, '작업자아이디', NOW());
-- UPDATE COMCODE_TB SET USE_YN = 0 WHERE TOP_CD = 'RU' AND TYPE_NAME = '해제할아이디';

-- 되돌리기
-- DELETE FROM COMCODE_TB WHERE TOP_CD = 'RU' OR (TOP_CD = '00' AND TYPE_CD = 'RU');
