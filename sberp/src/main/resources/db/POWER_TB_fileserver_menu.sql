-- 계정권한 설정(/api/sys/pow/detail.do)에 "파일서버현황" 메뉴 추가 (2026-11-20)
-- 권한: P01, P02 만 읽기·쓰기 허용, 나머지 권한은 0
-- 메뉴 순서: 현재 마지막 메뉴 뒤 (DIS_ORDR 최댓값 + 1)
-- @menuCode 는 프론트 메뉴(menuCode)와 맞춰야 한다. 프론트 담당자와 확인 후 바꿔서 실행.
-- 같은 이름의 메뉴가 이미 있으면 넣지 않는다 (두 번 실행해도 안전).

SET @menuName = '파일서버현황';
SET @menuCode = 'nc/status';

INSERT INTO POWER_TB (
    MENU_NAME, MENU_CODE, DIS_ORDR,
    P01_R_YN, P01_W_YN, P02_R_YN, P02_W_YN,
    P03_R_YN, P03_W_YN, P04_R_YN, P04_W_YN, P05_R_YN, P05_W_YN,
    P06_R_YN, P06_W_YN, P07_R_YN, P07_W_YN, P08_R_YN, P08_W_YN,
    P09_R_YN, P09_W_YN, P10_R_YN, P10_W_YN, P11_R_YN, P11_W_YN,
    P12_R_YN, P12_W_YN, P13_R_YN, P13_W_YN, P14_R_YN, P14_W_YN,
    P15_R_YN, P15_W_YN,
    STATE
)
SELECT
    @menuName, @menuCode, IFNULL(MAX(DIS_ORDR), 0) + 1,
    1, 1, 1, 1,
    0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0,
    0, 0,
    'I'
FROM POWER_TB
WHERE STATE != 'D'
HAVING NOT EXISTS (
    SELECT 1 FROM POWER_TB p WHERE p.MENU_NAME = @menuName AND p.STATE != 'D'
);

-- 확인
SELECT MENU_SEQ, MENU_NAME, MENU_CODE, DIS_ORDR, P01_R_YN, P01_W_YN, P02_R_YN, P02_W_YN, P03_R_YN, STATE
FROM POWER_TB
WHERE MENU_NAME = @menuName;
