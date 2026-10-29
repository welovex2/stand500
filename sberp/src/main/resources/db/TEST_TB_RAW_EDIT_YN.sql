-- 로데이터 수정여부 (1=수정가능, 0=불가)
-- 시험상태를 프로젝트완료(18)로 변경 시 RAW_EDIT_YN = 0 으로 설정

ALTER TABLE TEST_TB
  ADD COLUMN RAW_EDIT_YN TINYINT NOT NULL DEFAULT 1 COMMENT '로데이터 수정여부 (1=수정가능, 0=불가)' AFTER TEST_STATE_DT;

-- 이미 프로젝트완료인 시험은 수정 불가로 맞춤
UPDATE TEST_TB
SET RAW_EDIT_YN = 0
WHERE TEST_STATE_CODE = '18';
