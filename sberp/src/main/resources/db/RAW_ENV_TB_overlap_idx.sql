-- #26-8 측정일·환경조건 : [종료] 시 같은 시험원(서명) 시간 겹침 확인용 인덱스
-- 이미 RAW_ENV_TB 를 만든 DB(개발·운영)에 한 번 실행. 신규 생성은 RAW_ENV_TB.sql 에 포함됨.
-- (MEMBER_SIGN_TB 는 100건 미만이라 인덱스 추가 안 함)
ALTER TABLE RAW_ENV_TB
    ADD KEY IDX_RAW_ENV_SIGN (SIGN_URL, TEST_S_DT);
