# 로데이터 측정일·환경조건 다건 입력 설계 (#26-8)

- 근거: 노션 「[RD 및 성적서] 측정일, 환경조건 추가 건 (#26-8)」 (2026.10.29 EMC 정지훈 과장 요청)
- 범위: 백엔드(DB·API·검증·성적서 데이터). 프론트 화면은 별도 담당자에게 §8로 넘긴다.
- 상태: 설계 확정 (2026-11-04 미결 정리 완료, §9)

---

## 1. 요구사항 요약

| 구분 | 내용 |
|---|---|
| 표 구성 | 시험방법마다 표 하나. 행 = `측정일시(시작/종료) · 소요시간 · 온도 · 습도 · (기압) · 이름/서명 · 동작` |
| 행 개수 | 1 ~ 10행. 우측 상단 [추가] 버튼, 10행이면 비활성 |
| 시험시작 | [시작] 버튼 → 서버시각 자동 기록. 시험원이 일시 직접 입력 불가 |
| 시험종료 | [종료] 버튼 → 서버시각 자동 기록. **종료 전에는 다음 행 추가 불가** |
| 중단 | 시작 일시 옆 [중단] → 사유 입력 모달(필수) → 저장 시 해당 행 제거(첫 행이면 리셋). 사유는 시험리스트 View 모달의 ‘작성내역’에 남김 |
| 권한 | 생성된 행 수정·삭제, 일시 수정은 **기술책임자**만. 일시 수정은 [종료] 이후에만 |
| 환경조건 형식 | ① 온도/습도: CE, RE, CLK, DP, TEL ② 온도/습도/기압: ESD, RS, EFT, SURGE, CS, MF, VDIP (CLK·DP·TEL은 노션 목록 밖이나 같이 적용하기로 결정) |
| 이름·서명 | 행마다 시험원 1명 (행이 여러 개라 결과적으로 여러 시험원). 서명 후 [취소하기] 유지 |
| 성적서 | ‘환경조건 및 측정일자’ 표로 통일: `[일자/온도/습도(/기압)]` 행 단위 출력. 일자는 **종료일 기준**, 같은 날짜 여러 행도 모두 출력. 목차 수정 |
| 성적서 시험일 | 시험결과 ‘시험일’ = 날짜 중복 제거 후 `,` 로 나열 (예: `2026년 11월 03일 , 2026년 11월 04일`) |

---

## 2. 현재 구조와 문제

- 시험방법마다 `METHOD_*_TB` 1행 / `RAW_SEQ`. 환경조건·측정일·서명이 그 행의 컬럼 1세트.
  - 온습도: `TEMP, TEMP_PLUS, HMDT, HMDT_PLUS` / 기압: `BRMT, BRMT_PLUS` (ESD·RS·EFT·SURGE·CS·MF·VDIP)
  - 측정일: `MSRMN_YEAR/MON/DAY/HOUR/MIN` (시험원이 직접 입력)
  - 서명: `SIGN, SIGN_URL`
- RE만 `HZ1_/HZ2_/HZ3_` 접두어로 3세트 고정 (대역별 측정일·서명·결과).
- `RAW_TB.TEST_S_DT/TEST_E_DT`는 방법 저장 때마다 측정일 MIN/MAX로 재계산 (`RawServiceImpl.recalcAndUpdateTestDtRange`).
- 측정일을 모으는 `rawMapper.selectAllMsrmnDates`는 방법별 UNION ALL + MET_SEQ 하드코딩.

컬럼을 10세트로 늘리는 방식은 테이블 13개 × 컬럼 수십 개라 불가. **공통 하위 테이블로 분리**한다.

---

## 3. DB 설계

### 3.1 신규 테이블 `RAW_ENV_TB` (환경조건 및 측정일 행)

> 정본 SQL: `src/main/resources/db/RAW_ENV_TB.sql` (테이블·이력 트리거), `RAW_ENV_TB_migration.sql` (이관).
> 아래는 개요이며 컬럼 길이 등은 정본을 따른다 (온습도 VARCHAR(50), 아이디 VARCHAR(20)).
> 정본에는 시험원 일정용 인덱스 `IDX_RAW_ENV_MEM (MEM_ID, TEST_S_DT)`가 추가돼 있다 (§11).

```sql
CREATE TABLE RAW_ENV_TB (
  ENV_SEQ       INT          NOT NULL AUTO_INCREMENT COMMENT '환경조건 행 SEQ',
  RAW_SEQ       INT          NOT NULL COMMENT '로데이터 SEQ',
  MET_KEY       VARCHAR(10)  NOT NULL COMMENT '시험방법 키 (CE, RE1, RE2, RE3, ESD, RS, EFT, SURGE, CS, MF, VDIP, CLK, DP, TEL)',
  ROW_NO        TINYINT      NOT NULL COMMENT '표시 순번 1~10',

  TEST_S_DT     DATETIME     NULL     COMMENT '시험시작일시 (서버시각)',
  TEST_E_DT     DATETIME     NULL     COMMENT '시험종료일시 (서버시각)',

  TEMP          VARCHAR(20)  NULL COMMENT '온도',
  TEMP_PLUS     VARCHAR(20)  NULL COMMENT '온도 ±',
  HMDT          VARCHAR(20)  NULL COMMENT '습도',
  HMDT_PLUS     VARCHAR(20)  NULL COMMENT '습도 ±',
  BRMT          VARCHAR(20)  NULL COMMENT '기압 (형식② 방법만)',
  BRMT_PLUS     VARCHAR(20)  NULL COMMENT '기압 ±',

  MEM_ID        VARCHAR(50)  NULL COMMENT '시험원 아이디 ([시작] 누른 로그인 사용자 자동 저장)',
  MEM_NAME      VARCHAR(50)  NULL COMMENT '시험원 이름 (표기용)',
  SIGN          MEDIUMTEXT   NULL COMMENT '서명 (기존 METHOD_*.SIGN 과 같은 형식)',
  SIGN_URL      VARCHAR(500) NULL,

  MIG_YN        TINYINT      NOT NULL DEFAULT 0 COMMENT '기존 데이터 이관 행 여부 (1이면 소요시간 표시 안 함)',

  DEL_YN        TINYINT      NOT NULL DEFAULT 0 COMMENT '삭제(중단·기술책임자 삭제) 여부',
  DEL_REASON    VARCHAR(200) NULL COMMENT '중단/삭제 사유 (최대 200자)',
  DEL_MEM_ID    VARCHAR(50)  NULL,
  DEL_DT        DATETIME     NULL,

  INS_MEM_ID    VARCHAR(50)  NOT NULL,
  INS_DT        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UDT_MEM_ID    VARCHAR(50)  NULL,
  UDT_DT        DATETIME     NULL,
  PRIMARY KEY (ENV_SEQ),
  KEY IX_RAW_ENV_RAW (RAW_SEQ, MET_KEY, DEL_YN, ROW_NO)
) COMMENT='로데이터 환경조건 및 측정일';
```

이름·서명은 행마다 1명으로 확정되어 별도 서명 테이블 없이 `RAW_ENV_TB`에 둔다.

### 3.2 설계 포인트

- **MET_KEY**: RE는 대역(HZ1~3)마다 측정일·서명·결과가 따로라 `RE1/RE2/RE3`로 표를 나눈다 (대역별 최대 10행). 기존 `macType`(RE1/RE2/RE3) 표기와 맞춘다. 대역↔MET_SEQ 매핑(규격 7/686/774/775 분기)은 기존 로직을 그대로 쓴다.
- **환경조건 형식은 테이블에 저장하지 않고** MET_KEY로 결정한다 (`EnvFormat` enum: CE·RE*·CLK·DP·TEL = TH, ESD·RS·EFT·SURGE·CS·MF·VDIP = THP). 기압 컬럼은 형식 ①이면 무시.
- **삭제는 소프트 삭제** (`DEL_YN=1` + 사유). 중단도 같은 처리. 첫 행 “리셋”은 화면에서 행이 0건이면 빈 행 하나를 그려 주는 것으로 통일한다(서버는 첫 행/나머지 행 구분 없이 같은 처리).
- **ROW_NO**: 살아있는 행 기준으로 재정렬하지 않고 시작 시 `MAX(ROW_NO)+1`. 화면 표시는 `ROW_NO` 순.
- **기존 `METHOD_*_TB`의 환경·측정일·서명 컬럼은 바로 지우지 않는다.** 이관 후 쓰기만 중단하고, 운영 안정화 뒤 별도 작업으로 DROP.

### 3.3 기존 데이터 이관 (1회성 SQL)

| 원본 | 이관 결과 |
|---|---|
| 각 `METHOD_*_TB` 1행 (측정일 입력된 행만) | `RAW_ENV_TB` ROW_NO=1, `TEST_S_DT = TEST_E_DT = MSRMN_*` 조합 일시, 온습도·기압 복사, `MIG_YN=1` |
| RE `HZ1_/HZ2_/HZ3_` | `RE1/RE2/RE3` 각 ROW_NO=1 |
| `SIGN, SIGN_URL` | 같은 행의 `SIGN, SIGN_URL`. MEM_NAME은 RAW_TB의 시험자명(없으면 공란) |

- 이관 행은 시작=종료라 소요시간이 0이 되므로, `MIG_YN=1`이면 `elapsedMin`을 null로 내려 화면에서 빈칸 처리한다. 성적서 날짜는 정상 출력.

- 측정일이 0/NULL인 행은 이관하지 않는다 (미작성).
- CTI는 미사용이라 제외.
- 이관 스크립트: `src/main/resources/db/RAW_ENV_TB_migration.sql`. 0번(이관 제외 행 미리보기)·2번(건수 대조) 쿼리 포함. 재실행 안전.
- 날짜로 만들 수 없는 측정일(2월 31일, 연도 2000~2099 밖 등)은 이관하지 않는다. 시·분이 범위 밖이면 23시/59분으로 자른다.
- 이관 행(`MIG_YN=1`) INSERT는 이력 트리거가 기록하지 않는다.

---

## 4. API 설계

**[시작]·[종료]·[저장]은 방법별 `insert.do` 하나로** 방법 데이터(시험조건·측정설비·결과)와 같은 트랜잭션에서 처리한다 (2026-11-04 변경, §9-14). 화면 버튼이 저장 기능을 같이 갖고, 프론트가 API를 두 번 부르다 반만 저장되는 일을 막는다. 어느 하나라도 실패하면 방법 데이터까지 롤백된다.
행 단위 API는 조회·[중단]·기술책임자 수정·삭제만 남긴다.

공통: `@RequestMapping("/raw")`, 응답은 기존 `ResponseMessage` 체계. 모든 쓰기는 `assertRawEditable` 선행.

> 구현: `RawController`(방법별 insert/detail) → `RawServiceImpl.insertXx` → `RawEnvService.applyFromMethod`.
> 행 단위: `RawEnvController`(Swagger 태그 ‘로데이터 측정일·환경조건’) → `RawEnvService/Impl` → `RawEnvMapper` + `rawEnvMapper.xml`.
> 편집 가능·측정일·교정일·시험기간 검증은 `RawSupport` 공통. 시험방법 키·형식·측정설비 구분은 `EnvMetKey` enum. `ROW_NO`는 삭제 행 포함 MAX+1.

| # | 메서드·URL | 설명 | 권한 |
|---|---|---|---|
| 1 | `POST /raw/{방법}/insert.do` + `envAction` | [시작]·[종료]·[저장] (아래 4.1) | 로그인 |
| 2 | `GET /raw/{rawSeq}/env/list.do?metKey=CE` | 표 조회. `metKey` 생략 시 행이 있는 표 전체. 방법별 `detail.do`의 `envList`도 같은 형식 | 로그인 |
| 3 | `POST /raw/env/{envSeq}/stop.do` | [중단]: 사유 필수, 소프트 삭제 + 작성내역 기록 | 누구나 (진행 중 행) |
| 4 | `POST /raw/env/{envSeq}/update.do` | 종료된 행 수정: 일시·온습도·기압·이름·서명 | 기술책임자 (종료된 행) |
| 5 | `POST /raw/env/{envSeq}/delete.do` | 행 삭제 (사유 필수) | 기술책임자 |

기존 `start.do`, `end.do`, `date.do`는 삭제했다 (1번과 4번으로 통합).

### 4.1 방법별 insert.do 에 추가된 필드

| 필드 | 값 | 설명 |
|---|---|---|
| `envAction` | `START` / `END` / 빈 값 | 빈 값이면 [저장] |
| `envMetKey` | RE1~RE3 | [시작]·[종료] 대상 표. **RE만 필수**, 표가 하나인 화면은 생략 가능 (2026-11-05) |
| `envRows` | `RowReq` 목록 | 화면의 **진행 중 행** 값. [시작]으로 새로 만들 행은 `envSeq: 0` |

```json
"envAction": "START",
"envMetKey": "",
"envRows": [
  { "envSeq": 0, "temp": "23.1", "tempPlus": "0.5", "hmdt": "45.2", "hmdtPlus": "0.5",
    "brmt": "99.8", "brmtPlus": "0.1", "memName": "김시험", "atchFileId": "" }
]
```

처리 순서 (`applyFromMethod`, 방법 데이터·측정설비 저장 뒤)
1. `envRows` 중 `envSeq > 0`: 이 로데이터·이 방법 표의 행이 아니면 거부. **진행 중 행만 값 저장**, 종료·삭제된 행은 무시 (과거 측정 행은 건드리지 않음).
2. `START`: `envMetKey` 표에 새 행 + `TEST_S_DT=NOW()`, `MEM_ID`=로그인 사용자. `envSeq: 0` 행 값을 새 행에 저장.
3. `END`: `envMetKey` 표의 진행 중 행에 `TEST_E_DT=NOW()`. 1번에서 저장된 값 기준으로 **온도·습도·시험자명(THP는 기압도) 필수**.
4. 시험기간 재계산.

- 로데이터가 없으면(`rawSeq=0`) 방법 저장이 로데이터를 먼저 만든다 (CLK·DP·TEL도 이번에 같은 처리 추가). 응답 `data` = rawSeq — **화면은 이 값으로 수정 모드로 바뀌어야** 같은 시험에 로데이터가 또 생기지 않는다.
- 방법 저장의 기존 환경·측정일·서명 필드(`temp`, `msrmnYear`, `sign` …)는 무시된다 (§6).
- 서명은 첨부파일 ID(`atchFileId` → `SIGN_URL`)만 받는다. `SIGN`(서명자 이름, 예전 직접 업로드 때 쓰던 필드)은 이관 행에만 남고 API로는 쓰거나 돌려주지 않는다 (2026-11-05).
- 응답의 표 `label`·`envFormat`은 뺐다. 화면이 자기 제목과 기압 칸 유무를 이미 알고, 기압 저장 여부는 서버가 `EnvMetKey`로 판단한다 (2026-11-05).

`update.do` 요청 (기술책임자, 종료된 행)
```json
{ "testSeq": 123, "testSDt": "2026-11-04 09:10:00", "testEDt": "2026-11-04 11:40:00",
  "temp": "23.1", "tempPlus": "0.5", "hmdt": "45.2", "hmdtPlus": "0.5",
  "brmt": "99.8", "brmtPlus": "0.1", "memName": "김시험", "atchFileId": "FILE_000000000000862" }
```
- 값은 보낸 그대로 덮어쓴다 (빈 값 = 지움). 서명 [취소하기]는 `atchFileId` 빈 값.
- 일시는 둘 다 주면 수정, 둘 다 비우면 그대로. 하나만 주면 오류. 초까지 같으면 변경 없음으로 본다 → 화면은 조회 값(`yyyy-MM-dd HH:mm:ss`)을 그대로 돌려보낸다.

`stop.do` / `delete.do` 요청
```json
{ "testSeq": 123, "reason": "장비 이상으로 재시험" }
```

`list.do` 응답 (행)
```json
{
  "envSeq": 501, "metKey": "ESD", "rowNo": 2,
  "testSDt": "2026-11-04 09:10:00", "testEDt": "2026-11-04 11:40:00", "elapsedMin": 150,
  "temp": "23.1", "tempPlus": "0.5", "hmdt": "45.2", "hmdtPlus": "0.5", "brmt": "99.8", "brmtPlus": "0.1",
  "memId": "kim", "memName": "김시험", "atchFileId": "...", "signUrl": "...", "migYn": 0,
  "canEdit": false, "canEnd": false, "canStop": false, "canRevEdit": true, "canDelete": true
}
```
- 표 단위: `metKey`, `canAdd`([시작] 가능 = 살아있는 행 < 10 + 진행 중 행 없음 + 로데이터 수정 가능), `rows`.
- `elapsedMin`: 서버 계산. 이관 행(`MIG_YN=1`)과 종료 전 행은 null.
- `canEdit`: 진행 중 행 입력칸 활성 (값은 [저장]·[종료] 때 `envRows`로). `canRevEdit`: 기술책임자 + 종료된 행 → `update.do`.

### 4.2 서버 검증 규칙

| 규칙 | 시점 | 실패 메시지 |
|---|---|---|
| `envAction`은 START / END / 빈 값 | insert | `CHECK_ENV_ACTION` |
| `envMetKey`가 그 방법 화면의 표가 아님 | insert START·END | `CHECK_ENV_MET_KEY` |
| `envRows`에 다른 로데이터·다른 방법 행 | insert | `CHECK_ENV_STATE` |
| 살아있는 행 10개 이상이면 추가 불가 | START | `CHECK_ENV_MAX` |
| 종료 안 된 행이 있으면 추가 불가 | START | `CHECK_ENV_NOT_ENDED` |
| 진행 중 행이 없으면 종료 불가 | END | `CHECK_ENV_STATE` |
| 온도·습도·시험자명(·기압) 없으면 종료 불가 | END | `CHECK_ENV_REQUIRED` |
| 중단은 진행 중 행만 / 수정은 종료된 행만 | stop / update | `CHECK_ENV_STATE` |
| 사유 공란(공백만 포함) 불가, 최대 200자 | stop, delete | `CHECK_ENV_REASON` |
| 일시: 둘 다 입력, 시작 ≤ 종료, 미래 불가 | update | `CHECK_ENV_DATE` |
| 시작일·종료일 ≤ 성적서 발급일 | START, END, update | 기존 `CHECK_MSRMN_DT` |
| 측정설비 교정일 (그 표의 측정설비, 종료일 기준) | END, update | 기존 `CHECK_MAC_DATE` |
| 기술책임자 권한 | update, delete | `CHECK_ENV_AUTH` |

- 동시성: 측정 행 쓰기는 `SELECT ... FROM RAW_TB WHERE RAW_SEQ=? FOR UPDATE`로 같은 로데이터 요청을 직렬화 (더블클릭으로 행 2개 생기는 것 방지).
- 시각은 **DB `NOW()`** 사용 (클라이언트 시각 받지 않음).

---

## 5. 권한 규칙

| 행 상태 | 시험원 | 기술책임자 |
|---|---|---|
| 진행 중 (시작O·종료X) | 온습도·이름·서명 입력([저장]·[종료] 때 저장), [종료], [중단] | 동일 + 삭제 |
| 종료 후 | 조회만 | `update.do`로 일시·온습도·이름·서명 수정, 삭제 |

- 노션의 “생성된 행의 수정·삭제는 기술책임자만”은 **종료된 행에만** 적용한다 (확정). 진행 중 행은 시험원이 온습도·이름·서명을 입력한다.
- 기술책임자 판단: 로그인 사용자의 `MEMBER_TB.REV_YN = 1` (기술책임자 전원, 해당 시험 지정 여부와 무관. 확정).

---

## 6. 기존 로직 변경

| 대상 | 변경 |
|---|---|
| 방법별 `insertXx` (12개, `RawServiceImpl`) | `validateMsrmnDt` 호출 제거 (반영). `methodMapper.xml` insert 12개: VALUES의 `TEMP*, HMDT*, BRMT*, SIGN, SIGN_URL`은 `NULL`, `MSRMN_*`은 `0` (미입력과 같은 값, 컬럼 위치 유지), `ON DUPLICATE KEY UPDATE`에서 해당 대입 삭제 → 기존 행의 값은 그대로 남는다. RE HZ3 `<if>` 환경 컬럼 삭제. DTO 필드는 남겨 두고 무시 |
| `RawController` RE `macType` 판정 | `HZn_RESULT_CODE` 기준 그대로 유지 (측정일 무관) |
| 방법별 `detail` 조회 | `RawController`의 12개 `detail.do` 응답에 `envList` 추가 (반영). 형식은 `list.do`의 표(`RawEnvDTO.Table`) 목록, 행이 없어도 빈 표 포함. RE는 `RE1/RE2/RE3` 3표, 나머지는 1표. 기존 환경 필드는 이관 기간 동안 그대로 내려간다(사용 중단). `RepController` 등 서비스 detail 호출에는 붙이지 않는다 |
| `rawMapper.selectAllMsrmnDates` | 삭제 → `rawEnvMapper.selectDtRange` (반영). 종료된 행만, `RAW_MET_TB.CHECK_YN=1` 방법만 (MET_KEY↔MET_SEQ는 SQL 안에 기존 값 그대로: CE 0·1, ESD 6, RS 7, EFT 8, SURGE 9, CS 10, MF 11, VDIP 12, CLK 13, DP 14, RE 규격 분기, TEL 체크 없음. MET_SEQ 2(CTI)는 제외) |
| `recalcAndUpdateTestDtRange` (`RawSupport`) | `RAW_TB.TEST_S_DT = MIN(DATE(TEST_S_DT))`, `TEST_E_DT = MAX(DATE(TEST_E_DT))` (종료된 행). 종료된 행이 없으면 둘 다 NULL (반영, §9-12) |
| `validateReportDt` | `selectDtRange` 결과 사용. 로직 동일 (반영) |
| `valMacReformDate` | 측정일 = 그 측정설비 구분(`EnvMetKey.ofMacType`)의 표에서 종료된 행 중 가장 늦은 종료일. 종료된 행이 없으면 체크 안 함(행 [종료]·일시 수정 때 체크). “신규 저장 시에만” 조건은 기존 그대로 (반영) |
| `TableType` enum / `existsByRawSeq` | 영향 없음 (방법 행 존재 판단은 그대로) |
| DB 이력 트리거, `his-svc` | `RAW_ENV_TB.sql`에 `RAW_ENV_INS_TG/UDT_TG` (완료 후 저장 기록 + 일시·온습도·기압·삭제 변경 전/후 → `RAW_HIS_TB` TB='RAW_ENV_TB'). his-svc `labels.py`·`db.py`에 ‘측정일·환경조건’ 대상 추가 (반영 완료) |
| 작성내역 | 중단·삭제 시 `TEST_MEMO_TB`에 `[측정 중단] ESD 2회차 (시작 2026-11-04 09:10) / 사유: …` 형식으로 insert (기존 `testBoardInsert` 재사용) |

“측정일” 정의: 행의 **표시 일자 = `DATE(TEST_E_DT)`** (노션: 시작·종료일이 다르면 종료일 기준). 종료 전 행은 성적서·측정일 집계에서 제외.

---

## 7. 성적서

| 대상 | 변경 |
|---|---|
| `ReportDTO` | 방법별 `List<ReportEnvRow> xxEnvList` 추가 (`dateStr`, `temp`, `hmdt`, `brmt` 포맷 문자열). 방법별 `xxTestDays` (중복 제거·정렬 후 `yyyy년 MM월 dd일 , …`) 추가 |
| `RepController.getDetail` | 방법 루프에서 `RAW_ENV_TB` 조회해 위 필드 채움. 같은 날짜 여러 행은 표에서 모두 출력, 시험일 문자열에서는 한 번만 |
| DOCX `9832_report.docx` (report-service, 저장소 밖) | ‘환경조건 및 측정일자’ 표를 반복 행으로 변경, 목차 수정. **템플릿 담당 쪽 작업** |
| TEL HTML (`partials/tel/page_sec1.ftl`, `TelReportTemplateHelper.telEnvTemp/telEnvHmdt`) | ‘6. 시험환경’ 온도/습도 1줄 → `[일자/온도/습도]` 반복 표로 변경. 표지(`page_cover.ftl`) 시험기간은 기존 `RAW_TB.TEST_S_DT~TEST_E_DT` 그대로 |
| 시험일 길이 | 줄바꿈은 템플릿에서 처리 (데이터는 한 문자열) |

---

## 8. 프론트 전달 사항 (별도 담당)

- 화면: `raw_create_*/raw_update_*` 각 방법의 환경조건·측정일·서명 영역을 표 컴포넌트로 교체. 노션 RD 작성 화면 이미지 기준.
- **[시작]·[종료]·[저장] 모두 방법별 `insert.do` 한 번 호출** (§4.1). 화면 전체 데이터 + `envAction`(START/END/빈 값) + `envMetKey` + `envRows`(진행 중 행 값, [시작]할 새 행은 `envSeq: 0`). 성공 후 `detail.do` 재조회.
- `insert.do` 응답 `data`(rawSeq)로 화면을 수정 모드로 바꿀 것. 신규 화면에서 [시작]을 누르면 로데이터가 이때 생긴다. 바꾸지 않으면 다음 저장 때 로데이터가 또 생긴다.
- 표는 `detail.do`의 `envList`로 그린다 (방법 데이터가 없어 NO_DATA여도 빈 표가 온다). RE는 `RE1/RE2/RE3` 3표. 따로 필요하면 `list.do`.
- 방법 저장의 기존 환경·측정일·서명 필드(`temp`, `msrmnYear` … `sign`)는 서버가 무시한다. 보내지 않아도 된다.
- [시작] 버튼 활성 = 표의 `canAdd` (10행이거나 진행 중 행이 있으면 비활성). 0행이면 빈 행 하나를 그려 입력칸 + [시작].
- [종료] 전에 온도·습도·시험자명(기압 표는 기압도) 필수. 서버도 막는다 (`CHECK_ENV_REQUIRED`).
- 행 버튼: [종료] `canEnd`, [중단] `canStop`(사유 모달 → `stop.do`), 입력칸 `canEdit`(진행 중 행), 기술책임자 [수정] `canRevEdit`(수정 모달 → `update.do`, 일시 포함), [삭제] `canDelete`(사유 모달 → `delete.do`).
- `update.do`의 일시는 조회 값(`yyyy-MM-dd HH:mm:ss`)을 그대로 보내거나 둘 다 비운다 (분까지만 보내면 초가 바뀐 것으로 본다).
- 소요시간은 `elapsedMin`이 null이면 빈칸.
- 중단·삭제 사유 입력은 최대 200자, 공란 저장 불가.
- 대상 방법: CE, RE(대역별 3표), ESD, RS, EFT, SURGE, CS, MF, VDIP, CLK, DP, TEL. 기압 칸은 ESD·RS·EFT·SURGE·CS·MF·VDIP만 (`EnvMetKey` 형식 THP, 서버가 판단).
- 출력 화면(`raw_total_print*.js`, `report*.js`)의 온습도·측정일 표시를 `envList` 기반 표로 교체.

---

## 9. 결정 사항 (2026-11-04)

| # | 항목 | 결정 |
|---|---|---|
| 1 | 이름·서명 인원 | 행마다 1명 → `RAW_ENV_TB`에 컬럼으로 (서명 테이블 없음) |
| 2 | 기술책임자 전용 수정·삭제 범위 | 종료된 행만. 진행 중 행은 시험원 입력 |
| 3 | 중단·삭제 사유 길이 | 최대 200자, 공란 불가 |
| 4 | 기술책임자 기준 | `MEMBER_TB.REV_YN = 1` 전원 |
| 5 | CLK, DP, TEL | 모두 적용 (온도/습도 형식). TEL HTML 성적서도 변경 |
| 6 | RE 표 | 대역별(RE1/RE2/RE3) 표, 각 최대 10행 |
| 7 | 기존 데이터 이관 | 시작=종료 동일 시각, `MIG_YN=1`로 소요시간 빈칸 |
| 8 | 작성내역 위치 | `TEST_MEMO_TB` (`testBoardList`)에 중단·삭제 사유 기록 |
| 9 | 시험원 동시간 중복 비교 | 이번 범위 제외 (노션 보류, 조사 중). 데이터는 이번에 준비 (§11) |
| 10 | 시험원 아이디 | [시작] 누른 로그인 사용자를 `MEM_ID`에 자동 저장. 화면 입력·변경 없음 |
| 11 | 시험기간 재계산 기준 | 종료된 행만. `RAW_TB.TEST_S_DT = MIN(DATE(TEST_S_DT))`, `TEST_E_DT = MAX(DATE(TEST_E_DT))` |
| 12 | 측정일이 없을 때 시험기간 | 종료된 행이 0건이면 `RAW_TB.TEST_S_DT/TEST_E_DT`를 NULL로 비움 (기존: 값 유지) |
| 13 | 기존 컬럼 쓰기 중단 시점 | 3단계에서 바로 제거. 개발 DB는 이관 SQL을 먼저 실행해 둔다 (프론트 교체 전까지 기존 화면의 측정일·온습도 입력은 저장 안 됨) |
| 14 | [시작]·[종료]·[저장] | 버튼마다 방법 데이터 저장을 같이 한다. 방법별 `insert.do`의 `envAction` 하나로 처리 (행 API `start.do`·`end.do` 삭제). [저장]·[종료]는 진행 중 행 값만 저장, 과거 행은 건드리지 않음 |
| 15 | 종료된 행 수정 | 기술책임자 전용 행 단위 `update.do` 유지 (일시·온습도·이름·서명 한 번에, `date.do` 통합) |
| 16 | [종료] 필수값 | 온도·습도·시험자명, THP 방법은 기압도. 서명은 선택 |

---

## 10. 작업 순서

1. DDL (`RAW_ENV_TB`) + 이관 SQL + 이력 트리거 — 개발 DB 적용 완료 (11-04, 이관 포함)
2. VO/DTO/Mapper/Service/Controller: §4 API + 검증 (11-04 버튼 통합 구조로 개편, 11-05 로컬 RE 화면 콘솔 테스트 완료)
3. 기존 로직 교체: `selectAllMsrmnDates`, `recalcAndUpdateTestDtRange`, 방법별 insert에서 환경 컬럼 제거, detail에 `envList` (구현 완료, 11-05 로컬 테스트 완료)
4. 성적서 데이터 (`ReportDTO`, `RepController`) + 템플릿 담당 전달
5. 프론트 전달 (§8) → 연동 테스트
6. 운영 반영: DDL → 이관 → 배포 순. 기존 컬럼 DROP은 안정화 후 별도

---

## 11. 추후: 시험원 동시간 중복 확인·일정 달력

한 시험원은 같은 시간에 두 측정을 할 수 없다. `RAW_ENV_TB` 행 = 시험원 1명 × [시작, 종료) 구간이라 이 테이블로 바로 확인할 수 있다.

- 시험원: `MEM_ID` (시작 누른 로그인 사용자, 자동 저장)
- 구간: `[TEST_S_DT, IFNULL(TEST_E_DT, NOW()))` — 진행 중 행은 지금까지 진행 중으로 본다
- 제외: `DEL_YN=1`(삭제·중단), `MIG_YN=1`(이관 행, 시작=종료라 구간 없음), `MEM_ID IS NULL`
- 인덱스: `IDX_RAW_ENV_MEM (MEM_ID, TEST_S_DT)`
- 같은 시험 안의 다른 시험방법끼리 겹쳐도 중복으로 본다

```sql
SELECT a.MEM_ID, a.ENV_SEQ, a.RAW_SEQ, a.MET_KEY, b.ENV_SEQ, b.RAW_SEQ, b.MET_KEY
  FROM RAW_ENV_TB a
  JOIN RAW_ENV_TB b
    ON b.MEM_ID = a.MEM_ID
   AND b.ENV_SEQ > a.ENV_SEQ
   AND b.TEST_S_DT < IFNULL(a.TEST_E_DT, NOW())
   AND a.TEST_S_DT < IFNULL(b.TEST_E_DT, NOW())
 WHERE a.DEL_YN = 0 AND b.DEL_YN = 0
   AND a.MIG_YN = 0 AND b.MIG_YN = 0;
```

- [시작] 시점 차단도 가능: 로그인 사용자에게 종료 안 된 다른 행이 있으면 거부 (이번 범위 아님).
- 달력: 행을 그대로 일정으로 사용. `RAW_SEQ → RAW_TB → TEST_TB`로 시험번호·제품명을 붙인다.
