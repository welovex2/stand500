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

## 1. 작업 시간

`DROP TRIGGER RAW_HIS_TG` 와 `CREATE TRIGGER RAW_HIS_TG` 사이에 저장된 로데이터는 저장 이력이 남지 않는다. 몇 초지만 **업무 시간 외**에 한다.

## 2. 반영 전 백업

트리거 정의만 백업하면 된다(데이터는 바꾸지 않는다). 로컬 PC 의 MariaDB 클라이언트로 실행한다.

```bash
"/c/Program Files/MariaDB 10.6/bin/mysql.exe" -h 172.22.0.44 -u erp_user -p erp -e "SHOW CREATE TRIGGER RAW_HIS_TG\G" > D:/work/STD/RAW_HIS_TG_backup_20261029.txt
```

## 3. 사전 확인 (스크립트 0번)

결과가 `RAW_HIS_TG / RAW_TB / AFTER / UPDATE` 1건이 아니면 **중단**한다.

```bash
"/c/Program Files/MariaDB 10.6/bin/mysql.exe" -h 172.22.0.44 -u erp_user -p erp -e "SELECT TRIGGER_NAME, EVENT_OBJECT_TABLE, ACTION_TIMING, EVENT_MANIPULATION FROM information_schema.TRIGGERS WHERE EVENT_OBJECT_SCHEMA = DATABASE() AND EVENT_OBJECT_TABLE IN ('RAW_TB','RAW_MET_TB','TEST_TB')"
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
쌓인 변경 이력 행(`TB<>''`)은 남는다. 지우려면 `DELETE FROM RAW_HIS_TB WHERE TB <> '';`
