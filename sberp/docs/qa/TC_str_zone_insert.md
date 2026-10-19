# 구역 추가 `/cnf/str/zone/insert.do` 테스트케이스

- 대상: `POST /cnf/str/zone/insert.do` (`StrController.insertZone` → `StrServiceImpl.insertZone`)
- 요청 본문: `Zone` — `strSeq`, `zoneName` 필수
- 여러 개를 한 번에 만들 때는 `/cnf/str/zone/insertRange.do`
- 검증 보조: `GET /cnf/str/{strSeq}/detail.do`

## 동작 순서

```
인증 확인 (URL 에 insert 가 있어 쓰기 권한 검사를 받는다)
 → strSeq <= 0 이면 CHECK_DATA
 → selectBySeq : 없으면 NO_DATA
 → 창고가 STATE='D' 면 WH_READONLY
 → zoneName 정규화 · 검증 (비울 수 없음 · 100자 · 따옴표 금지)
 → zoneDesc 검증 (200자)
 → useCode 정규화 (없으면 01, 알 수 없는 값이면 CHECK_ZONE_USE)
 → 같은 창고의 사용 중인 구역명과 중복이면 DUPLICATE_ZONE
 → INSERT (STATE='I')
 → 응답에 zoneSeq · whName · useName 을 채워 돌려준다
```

`useCode` 는 코드 · 화면값 · 한글을 모두 받는다.

| 저장값 | 받는 입력 |
|---|---|
| `01` 일반 | `01`, `norm`, `일반`, 빈 값 |
| `02` 인수요청 | `02`, `pick`, `인수요청` |
| `03` 장기 | `03`, `long`, `장기` |

## 테스트케이스

| TC | 구분 | 입력·조건 | 기대 결과 |
|---|---|---|---|
| TC-01 | 정상 | `strSeq`, `zoneName="가-01"` | `result=true`, `data.zoneSeq>0`, `useCode='01'`, `useName='일반'` |
| TC-02 | 정상 | `zoneDesc="창가쪽 선반"` 포함 | 설명이 그대로 저장 |
| TC-03 | 정상 | `zoneDesc` 생략 | `zoneDesc=null` |
| TC-04 | 정상·용도 | `useCode` 를 `01`/`02`/`03` 으로 각각 | 그대로 저장, `useName` 이 일반/인수요청/장기 |
| TC-05 | 정상·용도 | `useCode` 를 `norm`/`pick`/`long` 으로 | `01`/`02`/`03` 으로 변환 |
| TC-06 | 정상·용도 | `useCode` 를 `일반`/`인수요청`/`장기` 로 | `01`/`02`/`03` 으로 변환 |
| TC-07 | 정상·용도 | `useCode=""` | `01`(일반) |
| TC-08 | 정상·공백 | `zoneName="  가-01  "` | `가-01` 로 정리 |
| TC-09 | 정상·응답 | TC-01 응답 본문 | `whName`·`useName`·`zoneSeq`·`strSeq` 가 채워짐. `insDt`·`udtDt` 는 비어 있음 |
| TC-10 | 정상·정렬 | 구역 3개를 순서대로 추가 후 `detail.do` | 추가한 순서대로 나온다 (`DIS_ORDR` 동률 → `ZONE_SEQ` 순) |
| TC-11 | 필수값 | `strSeq` 누락 또는 0 | `result=false`, `필수입력값을 확인하세요` |
| TC-12 | 필수값 | `zoneName` 누락 | `result=false`, `구역명을 확인하세요. 비울 수 없고, 따옴표는 쓸 수 없습니다.` |
| TC-13 | 필수값 | `zoneName="   "` | `result=false`, 같은 메시지 |
| TC-14 | 경계 | `zoneName` 100자 | `result=true` |
| TC-15 | 경계 | `zoneName` 101자 | `result=false`, 구역명 메시지 |
| TC-16 | 경계 | `zoneName="박형태's 선반"` (작은따옴표) | `result=false`, 구역명 메시지 |
| TC-17 | 경계 | `zoneName` 에 큰따옴표 | `result=false`, 구역명 메시지 |
| TC-18 | 경계 | `zoneDesc` 200자 | `result=true` |
| TC-19 | 경계 | `zoneDesc` 201자 | `result=false`, `구역설명은 200자까지입니다.` |
| TC-20 | 용도 | `useCode="zz"` | `result=false`, `구역 용도를 확인하세요. 일반, 인수요청, 장기 중 하나입니다.` |
| TC-21 | 창고상태 | 없는 `strSeq` | `result=false`, `데이터가 없습니다` |
| TC-22 | 창고상태 | 사용 중지된 창고 | `result=false`, `사용 중지된 창고는 볼 수만 있습니다.` |
| TC-23 | 중복 | 같은 창고에 같은 구역명 | `result=false`, `이 창고에 이미 있는 구역명입니다.` |
| TC-24 | 중복 | 같은 창고, 앞뒤 공백만 다른 이름 | `result=false` — 정리 후 비교 |
| TC-25 | 중복 | **다른 창고**에 같은 구역명 | `result=true` — 창고 안에서만 겹치지 않으면 된다 |
| TC-26 | 중복 | 사용 중지한 구역과 같은 이름 | `result=true` — 중지된 이름은 재사용 가능 |
| TC-27 | 권한 | 비로그인 | `result=false`, `권한이 없습니다` |
| TC-28 | 권한 | 로그인했지만 창고 메뉴 쓰기권한 없음 | `result=false`, `권한이 없습니다` |
| TC-29 | 보안 | 본문에 `insMemId=admin` | 저장값은 로그인 ID |
| TC-30 | 보안 | 본문에 `state='D'` | 무시된다. INSERT 가 `'I'` 로 고정 |
| TC-31 | 정렬 | 구역 3개를 순서대로 추가 | 목록이 `ZONE_SEQ` 순(추가 순)으로 나온다 |

TC-10·14~19·24·27~31 은 DB·HTTP 계층이라 Swagger 수동 확인이 필요하다.

## 남은 항목

### Z2 — 구역명은 따옴표를 막는데 창고명은 막지 않는다

`isValidZoneName()` 이 `'` 와 `"` 를 거절한다. 반면 창고명에는 같은 검사가 없다. 구역명은 QR 라벨에 인쇄되니 제약이 있을 수 있으나, 두 이름의 규칙이 다른 이유가 코드에 남아 있지 않다.

`박형태's 선반` 같은 이름을 못 쓴다.

### Z3 — 창고당 구역 수에 상한이 없다

`insertRange.do` 는 한 번에 50개까지로 막는데, 단건 추가는 제한이 없다. 반복 호출로 얼마든지 만들 수 있다.

### Z4 — INSERT 가 0행이면 메시지 없이 실패한다

`strMapper.insertZone(req) > 0` 을 그대로 돌려준다. 0행이면 `result=false` · `message=null` 이 나간다. INSERT 는 실패하면 보통 예외가 나므로 실제로 걸릴 일은 드물다. `update.do` · `stop.do` 에서 고친 것과 같은 모양이다.
