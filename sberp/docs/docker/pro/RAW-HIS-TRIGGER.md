# 운영 DB 로데이터·성적서 발급일 변경 이력 트리거 반영

작성 2026-10-29. 대상: 운영 DB `172.22.0.44` / schema `erp` (MariaDB 10.1.48)
스크립트: `src/main/resources/db/RAW_TEST_HIS_TG.sql`

## 배경

- 개발 DB(40)에는 변경 이력 트리거 3개가 이미 들어가 있고, 운영(44)에는 예전 `RAW_HIS_TG` 하나만 있다.
- 프로그램 수정 없이 트리거만 반영한다. 반영하지 않아도 #26-8 배포에 오류는 없다. 이력이 안 쌓일 뿐이다.

| 트리거 | 대상 | 개발 40 | 운영 44 | 이번 작업 |
|---|---|---|---|---|
| `RAW_HIS_TG` | `RAW_TB` AFTER UPDATE | 새 버전 (시험시작·완료일 변경 전/후 추가) | 예전 버전 (저장 시점만 기록) | 교체 |
| `RAW_MET_HIS_UDT_TG` | `RAW_MET_TB` AFTER UPDATE | 있음 | 없음 | 신규 |
| `TEST_HIS_TG` | `TEST_TB` AFTER UPDATE | 있음 | 없음 | 신규 |

2026-10-30 **프로젝트완료 후 수정 기록**을 같은 스크립트에 추가했다(아래 "프로젝트완료 후 수정 기록" 절).
`RAW_TB` 쪽은 같은 `RAW_HIS_TG` 안에 넣어야 해서(10.1 은 테이블·시점·이벤트당 트리거 1개) 한 스크립트로 묶었다.

| 추가 객체 | 종류 | 운영 44 |
|---|---|---|
| `RAW_DONE_EDIT_TB` | 기록 테이블 | 신규 |
| `RAW_DONE_EDIT_LOG` | 프로시저 (트리거에서 CALL) | 신규 |
| `METHOD_{CE,RE,ESD,RS,EFT,SURGE,CS,MF,VDIP,CK,DP,TEL,IMG}_DONE_{INS,UDT}_TG` | 트리거 26개 | 신규 |
| `FILE_RAW_DONE_INS_TG` | 트리거 (`FILE_RAW_TB` AFTER INSERT) | 신규 |

개발 DB 의 트리거 3개는 2026-10-29 기준 스크립트와 내용이 같았다(주석·공백 제외 비교).
2026-10-30 `RAW_MET_HIS_UDT_TG` 의 시험방법명을 로데이터 화면 메뉴 이름(CE Main Power, ESD, V-Dip 등)으로 바꿨다.
개발 DB 에도 이 트리거를 다시 만들어야 같아진다. 이미 쌓인 이력 행의 COL 값(예전 한글 이름)은 그대로 남는다.

## 사전 점검 결과 (2026-10-29 운영 실측)

| 항목 | 결과 | 의미 |
|---|---|---|
| 대상 테이블 트리거 | `RAW_HIS_TG`(RAW_TB / AFTER / UPDATE) 1건 | 10.1 은 테이블·시점·이벤트당 1개만 허용. 충돌 없음 |
| `log_bin` | 0 (꺼짐) | SUPER 권한·`log_bin_trust_function_creators` 필요 없음 |
| 접속 계정 권한 | `erp`.* ALL PRIVILEGES | `CREATE TRIGGER` 가능 |
| 필요한 컬럼 | 전부 있음 | `RAW_HIS_TB`(TB·COL varchar(45), BEFORE·AFTER text), `RAW_TB`, `RAW_MET_TB`, `TEST_TB`, `TEST_ITEM_TB`, `JOB_TB`, `SBK_TB` |
| `RAW_HIS_TB` 행 수 | 112,860 (모두 `TB=''`) | 반영 전 변경 이력 행은 없다 |
| 현재 `RAW_HIS_TG` 내용 | 스크립트 9번 롤백 블록과 같음 | 롤백하면 지금 상태로 정확히 돌아간다 |

## 적용 후 달라지는 것

- `RAW_HIS_TB` 에 `TB` 가 `RAW_TB` / `RAW_MET_TB` / `TEST_TB` 인 변경 전/후 행이 쌓인다.
- **로데이터 이력 화면(`rawMapper.hisList`)에 행이 늘어난다.** 이 쿼리는 `TB` 를 구분하지 않아서, 변경 이력 행도 저장 이력처럼(같은 시각·같은 사람) 함께 보인다. 원하지 않으면 `hisList` 에 `AND RAW_HIS_TB.TB = ''` 을 넣는 코드 수정이 따로 필요하다.
- 변경자 기록의 한계는 스크립트 머리말 "알려진 한계" 1~3 을 따른다.
- 프로젝트완료(18) 시험의 로데이터가 저장되면 `RAW_DONE_EDIT_TB` 에 저장 1회당 1행이 쌓인다. `RAW_HIS_TB`·이력 화면과는 별개라 화면에 영향이 없다.

## 프로젝트완료 후 수정 기록

저장 시점에 그 로데이터의 시험(`RAW_TB.TEST_SEQ`)이 프로젝트완료(18)일 때만 남는다. 진행 중 시험의 저장은 남지 않는다.

| 저장 화면 | 잡는 테이블 | 비고 |
|---|---|---|
| 로데이터 기본정보 | `RAW_TB` (`RAW_HIS_TG` 안, `UDT_DT` 가 바뀔 때만) | 측정일 재계산(`updateTestDtRange`)은 제외 |
| 시험방법 탭 12개 | `METHOD_*_TB` AFTER INSERT/UPDATE | 탭 첫 저장=INSERT, 재저장=UPDATE. 하위 표(`*_SUB_TB`)는 본 테이블 저장과 함께라 따로 안 잡는다 |
| 사진 | `METHOD_IMG_TB` AFTER INSERT/UPDATE | |
| 첨부 로데이터 | `FILE_RAW_TB` AFTER INSERT | `RAW_SEQ` 는 NULL. 삭제는 못 잡는다 |

- 값의 변경 전/후는 남기지 않는다(누가·언제·어느 탭). 같은 시각 `RAW_HIS_TB`(`TB<>''`)에 일부 항목의 전/후가 있다.
- 프로시저는 예외를 무시한다. 기록이 실패해도 로데이터 저장은 성공한다(로컬 MariaDB 10.6 에서 기록 테이블을 치운 상태로 저장 성공 확인).
- 사진(`/raw/pic/insert.do`)·첨부 로데이터 등록(`/raw/file/insert.do`)은 서버에서 `RAW_EDIT_YN` 잠금을 확인하지 않는다. 화면만 막혀 있다면 완료 후에도 저장될 수 있고, 그 경우도 이 기록에 남는다.

조회 예시:

```sql
SELECT d.INS_DT, FN_MEM_NAME(d.MEM_ID) memName, d.MEM_ID, d.TEST_SEQ, d.RAW_SEQ, d.TB, d.ACT, d.DONE_DT
  FROM RAW_DONE_EDIT_TB d
 ORDER BY d.DONE_EDIT_SEQ DESC;
```

## 1. 작업 시간

`DROP TRIGGER RAW_HIS_TG` 와 `CREATE TRIGGER RAW_HIS_TG` 사이에 저장된 로데이터는 저장 이력이 남지 않는다. 몇 초지만 **업무 시간 외**에 한다.

## 2. 반영 전 백업

트리거 정의만 백업하면 된다(데이터는 바꾸지 않는다). 로컬 PC 의 MariaDB 클라이언트로 실행한다.

```bash
"/c/Program Files/MariaDB 10.6/bin/mysql.exe" -h 172.22.0.44 -u erp_user -p erp -e "SHOW CREATE TRIGGER RAW_HIS_TG\G" > D:/work/STD/RAW_HIS_TG_backup_20261029.txt
```

## 3. 사전 확인 (스크립트 0번)

결과가 `RAW_HIS_TG / RAW_TB / AFTER / UPDATE` 1건이 아니면 **중단**한다. `METHOD_*_TB`·`FILE_RAW_TB` 에 이미 AFTER INSERT/UPDATE 트리거가 있으면 새 트리거와 겹쳐 만들어지지 않는다.

```bash
"/c/Program Files/MariaDB 10.6/bin/mysql.exe" -h 172.22.0.44 -u erp_user -p erp -e "SELECT TRIGGER_NAME, EVENT_OBJECT_TABLE, ACTION_TIMING, EVENT_MANIPULATION FROM information_schema.TRIGGERS WHERE EVENT_OBJECT_SCHEMA = DATABASE() AND (EVENT_OBJECT_TABLE IN ('RAW_TB','RAW_MET_TB','TEST_TB','FILE_RAW_TB') OR EVENT_OBJECT_TABLE LIKE 'METHOD\_%')"
```

완료 후 수정 기록 대상 테이블 14개가 모두 있는지 본다(결과 14).

```bash
"/c/Program Files/MariaDB 10.6/bin/mysql.exe" -h 172.22.0.44 -u erp_user -p erp -e "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME IN ('METHOD_CE_TB','METHOD_RE_TB','METHOD_ESD_TB','METHOD_RS_TB','METHOD_EFT_TB','METHOD_SURGE_TB','METHOD_CS_TB','METHOD_MF_TB','METHOD_VDIP_TB','METHOD_CK_TB','METHOD_DP_TB','METHOD_TEL_TB','METHOD_IMG_TB','FILE_RAW_TB')"
```

## 4. 반영

스크립트에 `DELIMITER ;;` 가 있어서 **mysql 커맨드라인 클라이언트**(또는 HeidiSQL 의 "SQL 파일 실행")로 실행한다. JDBC·일부 GUI 의 "쿼리 실행" 창은 `DELIMITER` 를 모른다.

```bash
"/c/Program Files/MariaDB 10.6/bin/mysql.exe" -h 172.22.0.44 -u erp_user -p --default-character-set=utf8 erp < D:/work/STD/workspace/sberp/src/main/resources/db/RAW_TEST_HIS_TG.sql
```

- `--default-character-set=utf8` 을 빼면 트리거 안의 한글 시험방법명이 깨져 저장될 수 있다.
- 스크립트 끝의 9번 롤백은 주석이라 실행되지 않는다.

## 5. 반영 후 확인

트리거 3건이 나와야 한다.

```bash
"/c/Program Files/MariaDB 10.6/bin/mysql.exe" -h 172.22.0.44 -u erp_user -p --default-character-set=utf8 erp -e "SELECT TRIGGER_NAME, EVENT_OBJECT_TABLE, DEFINER FROM information_schema.TRIGGERS WHERE TRIGGER_SCHEMA = DATABASE() AND TRIGGER_NAME IN ('RAW_HIS_TG','RAW_MET_HIS_UDT_TG','TEST_HIS_TG')"
```

완료 후 수정 기록 트리거 27건과 프로시저 1건이 나와야 한다.

```bash
"/c/Program Files/MariaDB 10.6/bin/mysql.exe" -h 172.22.0.44 -u erp_user -p erp -e "SELECT COUNT(*) FROM information_schema.TRIGGERS WHERE TRIGGER_SCHEMA = DATABASE() AND (TRIGGER_NAME LIKE 'METHOD\_%\_DONE\_%\_TG' OR TRIGGER_NAME = 'FILE_RAW_DONE_INS_TG'); SELECT ROUTINE_NAME FROM information_schema.ROUTINES WHERE ROUTINE_SCHEMA = DATABASE() AND ROUTINE_NAME = 'RAW_DONE_EDIT_LOG'"
```

한글이 깨지지 않았는지 확인한다(`ok` 가 1 이어야 한다).

```bash
"/c/Program Files/MariaDB 10.6/bin/mysql.exe" -h 172.22.0.44 -u erp_user -p --default-character-set=utf8 erp -e "SELECT INSTR(ACTION_STATEMENT, 'B급/방송수신기/튜너') > 0 AS ok FROM information_schema.TRIGGERS WHERE TRIGGER_SCHEMA = DATABASE() AND TRIGGER_NAME = 'RAW_MET_HIS_UDT_TG'"
```

다음 업무일에 실제 수정이 일어난 뒤 이력이 쌓였는지 본다.

```sql
SELECT TB, COUNT(*), MAX(INS_DT) FROM RAW_HIS_TB WHERE TB <> '' GROUP BY TB;
```

## 9. 롤백

스크립트 9번 블록의 주석을 풀어 실행한다. 운영의 현재 `RAW_HIS_TG` 와 같은 내용으로 되돌아간다.
완료 후 수정 기록 트리거 27개와 프로시저도 함께 지운다(로컬 MariaDB 10.6 에서 롤백 후 `RAW_HIS_TG` 1건만 남는 것 확인).
쌓인 변경 이력 행(`TB<>''`)은 남는다. 지우려면 `DELETE FROM RAW_HIS_TB WHERE TB <> '';`
`RAW_DONE_EDIT_TB` 도 남는다. 지우려면 `DROP TABLE RAW_DONE_EDIT_TB;`
