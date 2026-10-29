-- =====================================================================
-- 로데이터 시험기간·시험방법, 시험 성적서발급일 변경 이력 (변경 전/후) — 운영 반영용
-- 대상 DB : MariaDB 10.1 (erp)
-- 대상    : RAW_HIS_TG(교체), RAW_MET_HIS_UDT_TG(신규), TEST_HIS_TG(신규)
-- 프로그램 수정 없음 — 트리거만 반영한다.
--
-- 기록 위치 : 모두 RAW_HIS_TB
--   TB=''           : 기존 저장 시점 행 (기존 동작 그대로)
--   TB='RAW_TB'     : COL = TEST_S_DT / TEST_E_DT
--   TB='RAW_MET_TB' : COL = CHECK_YN:{MET_SEQ} {시험방법명} / TYPE:{MET_SEQ} {시험방법명}
--                     예) CHECK_YN:6 정전기 방전
--   TB='TEST_TB'    : COL = REPORT_DT:{TEST_SEQ} / TEST_REPORT_DT:{TEST_SEQ}
--                     RAW_ID 는 그 시험의 로데이터 RAW_SEQ (재발행 시험은 원본 시험의 로데이터, 앱 selectRawSeqByTestSeq 와 동일).
--                     재발행 시험은 원본과 RAW_ID 를 공유하므로 COL 에 TEST_SEQ 를 붙여 구분한다.
--                     로데이터가 아직 없는 시험은 RAW_ID 가 NULL 로 남는다.
--
-- 변경자 (MEM_ID)
--   RAW_TB     : NEW.UDT_MEM_ID
--   RAW_MET_TB : 수정자 컬럼이 없어 RAW_TB.UDT_MEM_ID (로데이터 저장 시 RAW_TB 가 먼저 갱신됨)
--   TEST_TB    : 앱이 UDT_DT 를 갱신한 수정(/tst/insertReportDt.do)이면 NEW.UDT_MEM_ID,
--                갱신하지 않은 수정(로데이터 기본정보 저장 → updateReport)이면 RAW_TB.UDT_MEM_ID
--
-- 알려진 한계 (프로그램 수정 없이 반영하므로)
--   1) 기존 로데이터 이력 화면(hisList)은 RAW_HIS_TB 를 TB 구분 없이 조회한다.
--      반영 후에는 TB<>'' 인 변경 이력 행도 화면에 저장 이력처럼 함께 나온다.
--   2) 측정일 저장으로 시험기간이 재계산(updateTestDtRange)될 때는 앱이 UDT_MEM_ID 를 갱신하지 않아
--      변경자가 직전 로데이터 기본정보 저장자로 기록된다.
--   3) RAW_TB·RAW_MET_TB 최초 등록(INSERT) 시 값은 기록하지 않는다. 이후 수정부터 변경 전/후가 남는다.
--
-- 시험방법명 출처 : RepController.java 성적서 출력 switch(met.getMetSeq()) 주석
--   (COMCODE_TB 'RP' 그룹은 시험장면사진용 영문 코드라 사용하지 않음)
-- =====================================================================


-- ---------------------------------------------------------------------
-- 0. 실행 전 확인
--    MariaDB 10.1 은 테이블당 같은 시점·이벤트 트리거를 1개만 허용한다.
--    아래 결과가 RAW_HIS_TG(RAW_TB / AFTER / UPDATE) 1건이어야 한다. 그 외 행이 있으면 반영 중단 후 확인.
-- ---------------------------------------------------------------------
SELECT TRIGGER_NAME, EVENT_OBJECT_TABLE, ACTION_TIMING, EVENT_MANIPULATION
  FROM information_schema.TRIGGERS
 WHERE EVENT_OBJECT_SCHEMA = DATABASE()
   AND EVENT_OBJECT_TABLE IN ('RAW_TB', 'RAW_MET_TB', 'TEST_TB');


-- ---------------------------------------------------------------------
-- 1. 트리거
-- ---------------------------------------------------------------------
DROP TRIGGER IF EXISTS RAW_HIS_TG;
DROP TRIGGER IF EXISTS RAW_MET_HIS_UDT_TG;
DROP TRIGGER IF EXISTS TEST_HIS_TG;

DELIMITER ;;

-- RAW_TB 수정: 기존 저장 시점 기록 + 시험시작·완료일 변경 전/후
CREATE TRIGGER RAW_HIS_TG AFTER UPDATE ON RAW_TB FOR EACH ROW
BEGIN

    IF NEW.UDT_DT != OLD.UDT_DT THEN
		INSERT INTO RAW_HIS_TB
			VALUES (NEW.RAW_SEQ, '', '', '', '', NEW.UDT_MEM_ID, NOW() );
	END IF;

    IF NOT (NEW.TEST_S_DT <=> OLD.TEST_S_DT) THEN
        INSERT INTO RAW_HIS_TB (RAW_ID, TB, COL, `BEFORE`, `AFTER`, MEM_ID, INS_DT)
            VALUES (NEW.RAW_SEQ, 'RAW_TB', 'TEST_S_DT', OLD.TEST_S_DT, NEW.TEST_S_DT, NEW.UDT_MEM_ID, NOW());
    END IF;

    IF NOT (NEW.TEST_E_DT <=> OLD.TEST_E_DT) THEN
        INSERT INTO RAW_HIS_TB (RAW_ID, TB, COL, `BEFORE`, `AFTER`, MEM_ID, INS_DT)
            VALUES (NEW.RAW_SEQ, 'RAW_TB', 'TEST_E_DT', OLD.TEST_E_DT, NEW.TEST_E_DT, NEW.UDT_MEM_ID, NOW());
    END IF;

END;;

-- RAW_MET_TB 수정: 시험방법 체크 여부·시험규격 변경 전/후
-- (insertMethod 는 매 저장마다 ON DUPLICATE KEY UPDATE 로 전체를 덮어쓰지만, 값이 같으면 기록하지 않는다)
CREATE TRIGGER RAW_MET_HIS_UDT_TG AFTER UPDATE ON RAW_MET_TB FOR EACH ROW
BEGIN
    DECLARE v_met VARCHAR(40);
    DECLARE v_mem VARCHAR(10);

    IF NOT (NEW.CHECK_YN <=> OLD.CHECK_YN) OR NOT (NEW.TYPE <=> OLD.TYPE) THEN

        -- MET_SEQ + 시험방법명 (예: '6 정전기 방전')
        SET v_met = CONCAT(NEW.MET_SEQ, ' ', CASE NEW.MET_SEQ
            WHEN 0  THEN '교류 주전원 포트 전도성 방해'
            WHEN 1  THEN '비대칭모드 전도성 방해'
            WHEN 2  THEN '방송수신기 튜너포트 차동전압 전도성 방해'
            WHEN 3  THEN 'RF변조기 출력포트 차동전압 전도성 방해'
            WHEN 4  THEN '방사성 방해(1GHz 이하)'
            WHEN 5  THEN '방사성 방해(1GHz 초과)'
            WHEN 6  THEN '정전기 방전'
            WHEN 7  THEN '방사성 RF 전자기장'
            WHEN 8  THEN '전기적 빠른 과도현상'
            WHEN 9  THEN '서지'
            WHEN 10 THEN '전도성 RF 전자기장'
            WHEN 11 THEN '전원 주파수 자기장'
            WHEN 12 THEN '전압 강하 및 순간 정전'
            WHEN 13 THEN 'Click'
            WHEN 14 THEN 'DP'
            WHEN 15 THEN '방사성 방해(9kHz~30MHz)'
            ELSE '' END);

        SET v_mem = (SELECT UDT_MEM_ID FROM RAW_TB WHERE RAW_SEQ = NEW.RAW_SEQ LIMIT 1);

        IF NOT (NEW.CHECK_YN <=> OLD.CHECK_YN) THEN
            INSERT INTO RAW_HIS_TB (RAW_ID, TB, COL, `BEFORE`, `AFTER`, MEM_ID, INS_DT)
                VALUES (NEW.RAW_SEQ, 'RAW_MET_TB', LEFT(CONCAT('CHECK_YN:', v_met), 45), OLD.CHECK_YN, NEW.CHECK_YN, v_mem, NOW());
        END IF;

        IF NOT (NEW.TYPE <=> OLD.TYPE) THEN
            INSERT INTO RAW_HIS_TB (RAW_ID, TB, COL, `BEFORE`, `AFTER`, MEM_ID, INS_DT)
                VALUES (NEW.RAW_SEQ, 'RAW_MET_TB', LEFT(CONCAT('TYPE:', v_met), 45), OLD.TYPE, NEW.TYPE, v_mem, NOW());
        END IF;

    END IF;

END;;

-- TEST_TB 수정: 성적서 발급일 변경 전/후 → RAW_HIS_TB
-- REPORT_DT 가 앱에서 실제로 쓰는 발급일, TEST_REPORT_DT 는 현재 코드 미사용(첨부파일 로데이터용)이나 함께 추적
CREATE TRIGGER TEST_HIS_TG AFTER UPDATE ON TEST_TB FOR EACH ROW
BEGIN
    DECLARE v_raw_seq INT;
    DECLARE v_mem VARCHAR(10);

    IF NOT (NEW.REPORT_DT <=> OLD.REPORT_DT) OR NOT (NEW.TEST_REPORT_DT <=> OLD.TEST_REPORT_DT) THEN

        -- 시험 → 로데이터 RAW_SEQ (재발행은 원본 시험의 로데이터) : rawMapper.selectRawSeqByTestSeq 와 동일
        SET v_raw_seq = (
            SELECT RAW_TB.RAW_SEQ
              FROM TEST_ITEM_TB
                   LEFT JOIN TEST_TB orgTest ON orgTest.TEST_ITEM_SEQ = TEST_ITEM_TB.ORG_TEST_ITEM_SEQ
                   INNER JOIN JOB_TB ON TEST_ITEM_TB.QUO_YM = JOB_TB.QUO_YM
                          AND TEST_ITEM_TB.QUO_SEQ = JOB_TB.QUO_SEQ
                   INNER JOIN SBK_TB ON JOB_TB.SBK_YM = SBK_TB.SBK_YM
                          AND SBK_TB.TYPE = JOB_TB.SBK_TYPE
                          AND JOB_TB.SBK_SEQ = SBK_TB.SBK_SEQ
                          AND JOB_TB.SBK_REVISION = SBK_TB.REVISION
                   INNER JOIN RAW_TB ON RAW_TB.TEST_SEQ = IF(SBK_TB.REVISION = 0, NEW.TEST_SEQ, orgTest.TEST_SEQ)
             WHERE TEST_ITEM_TB.TEST_ITEM_SEQ = NEW.TEST_ITEM_SEQ
             LIMIT 1);

        -- 변경자: UDT_DT 를 같이 갱신한 수정이면 그 수정자, 아니면(로데이터 저장 경로) 로데이터 저장자
        IF NEW.UDT_DT <=> OLD.UDT_DT THEN
            SET v_mem = (SELECT UDT_MEM_ID FROM RAW_TB WHERE RAW_SEQ = v_raw_seq LIMIT 1);
        END IF;
        SET v_mem = IFNULL(v_mem, NEW.UDT_MEM_ID);

        IF NOT (NEW.REPORT_DT <=> OLD.REPORT_DT) THEN
            INSERT INTO RAW_HIS_TB (RAW_ID, TB, COL, `BEFORE`, `AFTER`, MEM_ID, INS_DT)
                VALUES (v_raw_seq, 'TEST_TB', CONCAT('REPORT_DT:', NEW.TEST_SEQ), OLD.REPORT_DT, NEW.REPORT_DT, v_mem, NOW());
        END IF;

        IF NOT (NEW.TEST_REPORT_DT <=> OLD.TEST_REPORT_DT) THEN
            INSERT INTO RAW_HIS_TB (RAW_ID, TB, COL, `BEFORE`, `AFTER`, MEM_ID, INS_DT)
                VALUES (v_raw_seq, 'TEST_TB', CONCAT('TEST_REPORT_DT:', NEW.TEST_SEQ), OLD.TEST_REPORT_DT, NEW.TEST_REPORT_DT, v_mem, NOW());
        END IF;

    END IF;

END;;

DELIMITER ;


-- ---------------------------------------------------------------------
-- 2. 반영 후 확인
-- ---------------------------------------------------------------------
-- 트리거 3건 (RAW_HIS_TG / RAW_MET_HIS_UDT_TG / TEST_HIS_TG)
SELECT TRIGGER_NAME, EVENT_OBJECT_TABLE, ACTION_TIMING, EVENT_MANIPULATION
  FROM information_schema.TRIGGERS
 WHERE EVENT_OBJECT_SCHEMA = DATABASE()
   AND TRIGGER_NAME IN ('RAW_HIS_TG', 'RAW_MET_HIS_UDT_TG', 'TEST_HIS_TG');

-- 화면에서 측정일·시험방법 체크·발급일을 한 번씩 바꾼 뒤
-- SELECT * FROM RAW_HIS_TB WHERE TB <> '' ORDER BY INS_DT DESC LIMIT 20;


-- ---------------------------------------------------------------------
-- 9. 롤백 (필요할 때만 주석 해제 후 실행) — RAW_HIS_TG 를 반영 전 내용으로 되돌린다
--    쌓인 변경 이력 행(TB<>'')은 남겨 둔다. 지우려면 DELETE FROM RAW_HIS_TB WHERE TB <> '';
-- ---------------------------------------------------------------------
-- DROP TRIGGER IF EXISTS RAW_MET_HIS_UDT_TG;
-- DROP TRIGGER IF EXISTS TEST_HIS_TG;
-- DROP TRIGGER IF EXISTS RAW_HIS_TG;
-- DELIMITER ;;
-- CREATE TRIGGER RAW_HIS_TG AFTER UPDATE ON RAW_TB FOR EACH ROW
-- BEGIN
--     IF NEW.UDT_DT != OLD.UDT_DT THEN
--         INSERT INTO RAW_HIS_TB
--             VALUES (NEW.RAW_SEQ, '', '', '', '', NEW.UDT_MEM_ID, NOW() );
--     END IF;
-- END;;
-- DELIMITER ;
