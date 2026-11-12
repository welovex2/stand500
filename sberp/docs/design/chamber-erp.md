# 챔버 사용 연동: ERP(sberp) 작업 문서

- 변경(2026-11-12 3차): `ops` 스키마를 쓰지 않는다. `CHAMBER_ETC_USE_TB`는 `erp` 스키마에 둔다. 로그인 실패 횟수는 테이블 없이 메모리에만 센다. 온습도 센서 수신은 현황판에서 뺀다
- 변경(2026-11-12 4차): 챔버 관리 화면은 chamber-svc가 만든다 (§8)
- 구현(2026-11-12): §3 정본 SQL, §4 API, §5 검증, §6 기술책임자 종료, §9 프론트 문서. 구역 변경 이력(트리거·his-svc 라벨)만 보류
- 작성: 2026-11-12
- 대상 프로젝트: `D:\work\STD\workspace\sberp`
- 범위: 챔버 테이블 DDL, 측정 행에 구역 기록, 구역 사용 중 확인, 기술책임자 종료
- 짝 문서: `chamber-svc.md` (현황판 서비스 작업), `chamber-usage-design.md` (전체 설계와 결정 배경)
- 관련: `sberp/docs/design/raw-env-msrmn-multi.md` (측정 행 설계, #26-8)

---

## 1. 이 프로젝트가 맡는 일

| 구분 | 내용 |
|---|---|
| 맡는 일 | 챔버 테이블 DDL(`CHAMBER_TB.sql`), 측정 행 [시작] 때 구역 선택·저장, 구역 사용 중 확인, [종료] 누락 행 닫기(기술책임자) |
| 맡지 않는 일 | 현황판 화면, 사용 이력 화면, 로데이터 외 사용(점검·디버깅·입회·예비시험) 기록, **챔버·구역 관리 화면**. 모두 chamber-svc가 맡는다 |
| 바꾸지 않는 것 | 시험장소 체크박스(`ROOM*_YN`)와 성적서 출력. 지금 그대로 둔다 |

### 관련 결정

| 항목 | 결정 |
|---|---|
| 사용 단위 | 구역. 구역마다 동시에 1건 |
| 선택 | 시험방법별로 고를 수 있는 구역 제한, 모든 시험방법에서 [시작] 때 필수 |
| 사용 중 막기 | 다른 측정 행이 진행 중이거나 **로데이터 외 사용이 진행 중**인 구역은 [시작] 거부 |
| 챔버 밖 시험 | `MULTI_YN=1` 구역("일반 시험실")은 사용 중 확인을 하지 않는다 |
| [종료] 누락 | 측정 행은 ERP에서만 닫는다. 기술책임자가 종료 시각을 넣어 닫는다 |
| 성적서 | 구역은 성적서에 쓰지 않는다 |
| 챔버 목록 | 기획 목록으로 새로 입력한다 |
| 챔버 관리 화면 | chamber-svc가 만든다. ERP(`sys`)에는 만들지 않는다. 테이블과 DDL은 그대로 `erp`·ERP |
| 반영 시점 | `RAW_ENV_TB` 운영 반영(#26-8)과 같이 한다. 운영 DDL에 `ZONE_SEQ`를 처음부터 넣는다 |

---

## 2. chamber-svc와의 약속

두 프로젝트는 같은 MariaDB를 쓰고, 코드로는 서로 호출하지 않는다. 아래 약속만 지키면 따로 개발할 수 있다.

### 2.1 테이블별 쓰는 쪽

| 테이블 | 쓰는 쪽 | 읽는 쪽 |
|---|---|---|
| `erp.RAW_ENV_TB` | **ERP** | chamber-svc |
| `erp.CHAMBER_TB`, `CHAMBER_ZONE_TB`, `CHAMBER_ZONE_MET_TB` | chamber-svc (관리 화면). DDL은 ERP | **ERP** |
| `erp.MEMBER_TB` | **ERP** | chamber-svc (로그인 확인) |
| `erp.CHAMBER_ETC_USE_TB` (로데이터 외 사용) | chamber-svc | **ERP** (측정 [시작] 사용 중 확인) |

### 2.2 "진행 중"의 정의 (양쪽이 같은 조건을 쓴다)

```sql
-- 진행 중인 측정 행
erp.RAW_ENV_TB           WHERE DEL_YN = 0 AND MIG_YN = 0 AND TEST_S_DT IS NOT NULL AND TEST_E_DT IS NULL
-- 진행 중인 로데이터 외 사용
erp.CHAMBER_ETC_USE_TB   WHERE DEL_YN = 0 AND END_DT IS NULL
```

### 2.3 잠금

- 구역을 쓰기 시작하는 쪽은 사용 중 확인 **전에** 구역 행을 잠근다: `SELECT ZONE_SEQ FROM erp.CHAMBER_ZONE_TB WHERE ZONE_SEQ = ? FOR UPDATE`
- ERP의 잠금 순서는 `RAW_TB` → `CHAMBER_ZONE_TB`로 고정한다. chamber-svc는 `CHAMBER_ZONE_TB`만 잠근다.
- `MULTI_YN=1` 구역은 잠금과 사용 중 확인을 건너뛴다.

### 2.4 ERP가 읽는 `erp.CHAMBER_ETC_USE_TB` 컬럼

ERP는 아래 컬럼만 쓴다. chamber-svc는 이 컬럼의 이름과 뜻을 바꾸지 않는다.

| 컬럼 | 뜻 |
|---|---|
| `USE_SEQ` | 기본 키 |
| `ZONE_SEQ` | 구역 |
| `PURPOSE` | 용도 (PRE / DEBUG / CHECK / WITNESS / ETC) |
| `MEM_NAME` | 기록한 사람 이름 |
| `START_DT`, `END_DT` | 시작·종료 (서버 시각). `END_DT` NULL = 사용 중 |
| `DEL_YN` | 삭제 여부 |

### 2.5 환경 준비 (중요)

**ERP가 도는 모든 DB(로컬·개발·운영)에 `CHAMBER_ETC_USE_TB`가 있어야 한다.** 없으면 측정 [시작]의 사용 중 확인 쿼리가 오류를 낸다. chamber-svc를 띄우지 않는 로컬 DB에도 `chamber-svc/sql/CHAMBER_ETC_USE_TB.sql`은 실행한다. ERP와 같은 `erp` 스키마라서 스키마를 따로 만들 필요는 없다.

---

## 3. DB

대상: ERP MariaDB 10.1, `erp` 스키마. 정본 SQL은 `sberp/src/main/resources/db/CHAMBER_TB.sql`로 만든다.

### 3.1 챔버·구역 목록 (신규)

```sql
CREATE TABLE CHAMBER_TB (
    CHAMBER_SEQ  INT          NOT NULL AUTO_INCREMENT COMMENT '챔버 SEQ',
    NAME         VARCHAR(50)  NOT NULL COMMENT '챔버 이름 (예: 10m 챔버, 차폐실 1)',
    SORT_NO      SMALLINT     NOT NULL DEFAULT 0 COMMENT '현황판 표시 순서',
    USE_YN       TINYINT      NOT NULL DEFAULT 1 COMMENT '사용 여부 (0이면 선택 목록·현황판에서 숨김)',
    INS_MEM_ID   VARCHAR(20)  NOT NULL,
    INS_DT       DATETIME     NOT NULL,
    UDT_MEM_ID   VARCHAR(20)  NULL,
    UDT_DT       DATETIME     NULL,
    PRIMARY KEY (CHAMBER_SEQ)
) COMMENT='챔버';

CREATE TABLE CHAMBER_ZONE_TB (
    ZONE_SEQ     INT          NOT NULL AUTO_INCREMENT COMMENT '구역 SEQ',
    CHAMBER_SEQ  INT          NOT NULL COMMENT '챔버 SEQ',
    NAME         VARCHAR(50)  NOT NULL COMMENT '구역 이름 (구역이 하나뿐인 챔버는 빈 값)',
    MULTI_YN     TINYINT      NOT NULL DEFAULT 0 COMMENT '동시 사용 허용 (1이면 사용 중 확인 안 함. 일반 시험실용)',
    SORT_NO      SMALLINT     NOT NULL DEFAULT 0,
    USE_YN       TINYINT      NOT NULL DEFAULT 1,
    INS_MEM_ID   VARCHAR(20)  NOT NULL,
    INS_DT       DATETIME     NOT NULL,
    UDT_MEM_ID   VARCHAR(20)  NULL,
    UDT_DT       DATETIME     NULL,
    PRIMARY KEY (ZONE_SEQ),
    KEY IDX_CHAMBER_ZONE (CHAMBER_SEQ, USE_YN, SORT_NO)
) COMMENT='챔버 구역 (사용 단위)';

CREATE TABLE CHAMBER_ZONE_MET_TB (
    ZONE_SEQ     INT          NOT NULL COMMENT '구역 SEQ',
    MET_KEY      VARCHAR(10)  NOT NULL COMMENT '시험방법 키 (RAW_ENV_TB.MET_KEY 와 같은 값)',
    PRIMARY KEY (ZONE_SEQ, MET_KEY),
    KEY IDX_CHAMBER_ZONE_MET (MET_KEY)
) COMMENT='구역에서 할 수 있는 시험방법';
```

- 사용 단위는 항상 구역이다. 구역을 나누지 않는 챔버도 구역 1개를 만든다.
- `MET_KEY`는 `EnvMetKey` 값을 그대로 쓴다 (CE, RE1, RE2, RE3, ESD, RS, EFT, SURGE, CS, MF, VDIP, CLK, DP, TEL).
- 챔버·구역은 지우지 않고 `USE_YN=0`으로 숨긴다.
- 데이터는 chamber-svc 챔버 관리 화면에서 입력·수정한다. 기획 최초 목록은 화면으로 넣을지 INSERT SQL로 넣을지 미결 (§11). "일반 시험실" 구역(`MULTI_YN=1`)을 포함한다.
- 개발 DB에는 chamber-svc `dev_seed.sql`로 세 테이블과 `RAW_ENV_TB.ZONE_SEQ`가 이미 만들어져 있다 (시험용 목록, 2026-11-12 확인).

### 3.2 `RAW_ENV_TB` 변경

```sql
-- 개발 DB (이미 테이블이 있는 곳)
ALTER TABLE RAW_ENV_TB
    ADD COLUMN ZONE_SEQ INT NULL COMMENT '시험 구역 (CHAMBER_ZONE_TB.ZONE_SEQ, 이관 행은 NULL)' AFTER TEST_E_DT,
    ADD KEY IDX_RAW_ENV_ZONE (ZONE_SEQ, TEST_S_DT);
```

- **운영용 정본 `RAW_ENV_TB.sql`의 CREATE 문에 컬럼과 인덱스를 넣는다.** 운영은 ALTER 없이 한 번에 만든다.
- 이관 행(`MIG_YN=1`)은 NULL.
- 이력 트리거 `RAW_ENV_UDT_TG`에 `ZONE_SEQ`를 넣을지(구역 변경 이력)는 측정 행 구역 처리를 다시 정할 때 같이 정한다 (보류).

### 3.3 권한

ERP 계정에 따로 줄 권한은 없다. `CHAMBER_ETC_USE_TB`가 ERP와 같은 `erp` 스키마에 있어서 지금 계정으로 읽을 수 있다.

chamber-svc 계정(`chamber_svc`)의 권한은 `chamber-svc.md` §3.2에 있다. ERP의 챔버 테이블이 먼저 있어야 그 GRANT를 실행할 수 있다.

---

## 4. API

기존 측정 행 API에 구역을 얹는다. 새 API는 구역 목록 조회 하나다.

| # | 메서드·URL | 변경 | 권한 |
|---|---|---|---|
| 1 | `GET /raw/env/zones.do?metKey=ESD` | **신규.** 그 시험방법에서 고를 수 있는 구역 목록 + 사용 중 여부 | 로그인 |
| 2 | `POST /raw/{방법}/insert.do` | `envRows[].zoneSeq` 추가. `envAction=START`면 필수 | 로그인 |
| 3 | `GET /raw/{testSeq}/{방법}/detail.do` | `envList` 행에 `zoneSeq`, `chamberName`, `zoneName` 추가 | 로그인 |
| 4 | `POST /raw/env/{envSeq}/update.do` | `zoneSeq` 추가. **진행 중 행도 허용** (§6) | 기술책임자 |

`zones.do` 응답 (행)
```json
{ "zoneSeq": 12, "chamberName": "10m 챔버", "zoneName": "A", "multiYn": 0, "inUse": true,
  "useSrc": "RAW",
  "useTestId": "SB2611-G0012-EM0034", "useMetKey": "RE1", "usePurpose": null,
  "useMemName": "김시험", "useStartDt": "2026-11-12 09:10:00" }
```
- `useSrc`: `RAW`(측정 행) / `ETC`(로데이터 외 사용). `ETC`면 `usePurpose`에 용도가 오고 `useTestId`·`useMetKey`는 비어 있을 수 있다.
- 사용 중인 구역도 목록에 내려 준다 (화면이 비활성으로 표시).
- `MULTI_YN=1` 구역은 항상 `inUse: false`.
- `USE_YN=1`이고 `CHAMBER_ZONE_MET_TB`에 그 `metKey`가 매핑된 구역만 내려 준다.

---

## 5. 서버 검증

| 시점 | 규칙 | 실패 메시지 |
|---|---|---|
| `START` | `zoneSeq` 없음 | `CHECK_ENV_ZONE_REQUIRED` |
| `START`, [저장]의 구역 변경, `update.do` | 그 시험방법에 매핑되지 않았거나 `USE_YN=0`인 구역 | `CHECK_ENV_ZONE` |
| `START`, [저장]의 구역 변경 | 그 구역에 진행 중인 다른 측정 행 또는 진행 중인 로데이터 외 사용이 있음 | `CHECK_ENV_ZONE_BUSY` (접수번호 또는 용도·시험방법·시험원·시작 시각 표시) |
| `update.do` (일시·구역 변경) | 같은 구역의 다른 측정 행·로데이터 외 사용과 시간이 겹침 | `CHECK_ENV_ZONE_BUSY` |

- "진행 중"의 조건은 §2.2.
- 시간 겹침 조건은 기존 시험원 겹침 확인(`raw-env-msrmn-multi.md` §11.1)과 같다: `상대 시작 < 이 행 종료 AND (상대 진행 중 OR 상대 종료 > 이 행 시작)`. 경계(10:00 종료·10:00 시작)는 통과. 자기 자신·삭제·이관 행 제외.
- `MULTI_YN=1` 구역은 사용 중·겹침 확인을 건너뛴다.
- 잠금은 §2.3. 기존 `RAW_TB ... FOR UPDATE` 뒤에 `CHAMBER_ZONE_TB ... FOR UPDATE`.
- `START`에서 거부되면 방법 저장까지 롤백된다 (기존 `applyFromMethod` 트랜잭션).
- 진행 중 행의 구역은 시험원이 [저장] 때 바꿀 수 있다. 종료된 행의 구역은 기술책임자만 `update.do`로 바꾼다.
- 구현은 "사용 중"과 "시간 겹침"을 같은 쿼리로 본다: 진행 중 행은 끝이 없는 구간으로 보고 겹침을 확인한다 (`rawEnvMapper.selectZoneConflict`).
- `CHAMBER_ETC_USE_TB`의 `START_DT < 이 행 종료` 조건도 같이 본다 (현황판 기록과 측정 행의 시간 겹침).
- [종료] 때는 구역 사용 중 확인을 하지 않는다 ([시작] 때 이미 확인했다).

---

## 6. 기술책임자 종료 ([종료] 누락 처리)

시험원이 [종료]를 누르지 않으면 그 측정 행은 계속 진행 중으로 남는다. 현황판에도 그 구역이 계속 사용 중으로 보이고, 다른 사람은 그 구역으로 [시작]할 수 없다.

측정 행은 ERP에서만 닫는다. 현황판은 오래된 건을 강조하고 "ERP에서 종료하세요"로 안내만 한다.

지금 `update.do`는 종료된 행만 고칠 수 있고, `stop.do`는 행을 삭제 처리해 기록이 사라진다. 기록을 남긴 채 닫을 수 있게 `update.do`를 넓힌다.

- 기술책임자는 **진행 중 행에도** `update.do`를 부를 수 있다. 이때 `testSDt`, `testEDt` 둘 다 필수이고, 저장되면 그 행은 종료된 행이 된다.
- 검증은 기존 `update.do`와 같다: 시작 ≤ 종료, 미래 불가, 시험원 겹침, 교정일, 성적서 발급일. 구역 처리가 확정되면 구역 겹침이 추가된다.
- 온도·습도·서명이 비어 있어도 닫을 수 있다. 빈 값은 기술책임자가 같은 화면에서 채운다. 성적서 발급 전 필수값 확인은 기존 흐름을 따른다.
- 작성내역(`TEST_MEMO_TB`)에 `[측정 종료 처리] ESD 2행 (시작 2026-11-12 09:10 → 종료 2026-11-12 18:00)` 형식으로 남긴다 (기존 [측정 중단]·[측정일 삭제]와 같은 "행" 표기).
- 응답 `envList`에 진행 중 행의 `canRevEdit`를 기술책임자에게 true로 내려 준다.

---

## 7. 변경 대상 코드

| 대상 | 변경 |
|---|---|
| `CHAMBER_TB.sql` (정본, 신규) | 챔버 3개 테이블 |
| `RAW_ENV_TB.sql` (정본) | `ZONE_SEQ` 컬럼·인덱스 |
| `RawEnvServiceImpl.update` | 진행 중 행 `update` 허용 (§6), 구역 변경 |
| `RawEnvDTO.RowReq`, `Row`, `UpdateReq`, `Zone`(신규) | `zoneSeq` (+ 응답 `chamberName`, `zoneName`), 구역 목록 |
| `RawEnv`, `RawEnvZone`(신규) | `zoneSeq`·이름, 구역 + 사용 중인 건 |
| `RawEnvServiceImpl.startRow / applyFromMethod / zones` | 구역 필수·허용·사용 중·겹침 검사, 구역 잠금, 구역 목록 |
| `rawEnvMapper.xml` | `ZONE_SEQ` 저장·조회, 구역 목록(`selectZones`), 잠금(`lockZone`), 허용 확인(`selectZoneForMet`), 겹침(`selectZoneConflict`) |
| `RawEnvController` | `zones.do` 추가 |
| `ResponseMessage` | `CHECK_ENV_ZONE_REQUIRED`, `CHECK_ENV_ZONE`, `CHECK_ENV_ZONE_BUSY` |
| 이력 트리거, his-svc `labels.py` | 구역 변경 이력 — 보류 |
| 챔버 관리 화면 | **ERP에서는 만들지 않는다** (chamber-svc, §8) |
| 방법별 `ROOM*_YN`, 성적서 | **변경 없음** |

추후 과제: 고른 구역에 맞춰 시험장소 체크박스를 자동으로 체크하기. 이번 범위에는 넣지 않는다.

---

## 8. 챔버 관리 화면 (chamber-svc로 이관)

챔버·구역·시험방법 매핑 등록·수정 화면은 chamber-svc가 만든다 (2026-11-12 결정). ERP는 테이블 DDL만 맡고, 화면·API는 만들지 않는다. 상세는 `chamber-svc.md`.

- 챔버: 이름, 표시 순서, 사용 여부
- 구역: 이름, 동시 사용 허용(`MULTI_YN`), 표시 순서, 사용 여부
- 구역별 시험방법 매핑: `MET_KEY` 체크박스
- 삭제는 없다. 사용 여부를 끈다.
- 매핑에서 시험방법을 빼면 ERP 측정 [시작]의 구역 목록에서 바로 빠진다.
- chamber-svc 계정에 챔버 3개 테이블 쓰기 권한이 필요하다 (`chamber-svc.md` §3.2).

---

## 9. 프론트 전달 사항

`raw-env-frontend.md`(프론트 담당자 연동 문서)에 반영했다 (2026-11-12).

- 측정 표의 진행 전 행(빈 행)에 구역 선택 칸을 추가한다. 목록은 `zones.do?metKey=`로 받는다 (RE는 `RE1`~`RE3` 표마다).
- 사용 중(`inUse`)인 구역은 비활성으로 표시하고 누가 쓰는지 보여 준다. `useSrc`가 `ETC`면 용도를 표시한다.
- [시작] 때 `envRows[0].zoneSeq`를 보낸다. 없으면 서버가 거부한다.
- 진행 중·종료된 행은 `chamberName` + `zoneName`을 표시한다. 진행 중 행은 구역을 바꿀 수 있다.
- 기술책임자 수정 모달에 구역 칸을 추가하고, 진행 중 행에도 [수정] 버튼을 보인다 (`canRevEdit`). 진행 중 행을 수정할 때는 시작·종료 일시가 둘 다 필수다.
- 서버 거부 메시지(`CHECK_ENV_ZONE_BUSY` 등)는 그대로 표시한다.
- 시험장소 체크박스는 건드리지 않는다.

---

## 10. 작업 순서

| # | 작업 | 선행 조건 |
|---|---|---|
| 1 | `CHAMBER_TB.sql` 작성, `RAW_ENV_TB.sql` 정본에 `ZONE_SEQ` 추가 (개발 DB에는 `dev_seed.sql`로 이미 있음) | - |
| 2 | 개발 DB에 `CHAMBER_ETC_USE_TB` 생성 | chamber-svc의 `CHAMBER_ETC_USE_TB.sql` (§2.5) |
| 3 | 챔버 목록 입력 | 기획 목록 확정, 입력 방식 결정. 그 전에는 시험용 목록으로 개발 |
| 4 | 백엔드: `zones.do`, `insert.do`·`detail.do`·`update.do` 구역 처리, 검증, 잠금 | 1, 2 |
| 5 | 기술책임자 종료 (`update.do` 확장), 작성내역 기록 | - |
| 6 | 프론트 전달 문서 작성 (§9) | 4, 5 |
| 7 | 모니터 전용 계정 생성 (chamber-svc 현황판용) | - |
| 8 | chamber-svc와 연동 테스트 (`chamber-svc.md` §9) | chamber-svc 개발서버 배포 |
| 9 | 운영 반영: #26-8 운영 반영 절차(`raw-env-msrmn-multi.md` §10.1)에 `CHAMBER_TB.sql`, `CHAMBER_ETC_USE_TB.sql`, chamber-svc 계정 GRANT, 챔버 목록 입력을 추가 | 전부 |

챔버 관리 화면은 chamber-svc 작업 순서에서 한다 (§8).

ERP 단독 테스트 (chamber-svc 없이 가능)
- [시작] 때 구역 없으면 거부, 매핑되지 않은 구역 거부
- 같은 구역에 측정 [시작] 두 번째는 거부, [종료] 후에는 통과
- `erp.CHAMBER_ETC_USE_TB`에 진행 중 행을 SQL로 직접 넣고 [시작]이 거부되는지
- `MULTI_YN=1` 구역은 동시에 여러 건 시작
- 기술책임자가 진행 중 행을 `update.do`로 닫기, 작성내역 기록
- 구역 변경이 `RAW_HIS_TB`에 남는지 (이력 보류가 풀리면)

---

## 11. 미결

| # | 내용 | 담당 |
|---|---|---|
| 1 | 챔버 목록: 챔버·구역·시험방법 매핑, "일반 시험실" 구역 | 기획 (작성 중) |
| 2 | 모니터 전용 계정 아이디 | ERP |
| 3 | ~~운영 ERP DB 계정 이름 (GRANT 대상)~~ | 해결: 같은 스키마라 GRANT가 필요 없다 |
| 4 | 챔버 목록 최초 입력 방식: chamber-svc 관리 화면 / INSERT SQL | ERP·chamber-svc |
| 5 | 구역 변경 이력(트리거·his-svc 라벨) | 보류 |
| 6 | 챔버 관리 화면 수정 권한 (기술책임자만?) | chamber-svc |
