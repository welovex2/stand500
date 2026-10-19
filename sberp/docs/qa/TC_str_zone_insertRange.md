# 구역 범위 추가 `/cnf/str/zone/insertRange.do` 테스트케이스

- 대상: `POST /cnf/str/zone/insertRange.do` (`StrController.insertZoneRange` → `StrServiceImpl.insertZoneRange`)
- 요청 본문: `ZoneRangeReq` — `strSeq`, `prefix`, `fromNo`, `toNo` 필수, `useCode` 선택
- 응답: `ZoneRangeResult` — `addedCnt`, `zones[]`, `skippedNames[]`
- 구역설명은 여기서 넣지 않는다. 만든 뒤 `/zone/update.do` 로 하나씩 적는다

## 동작 순서

```
인증 확인 (URL 에 insert 가 있어 쓰기 권한 검사를 받는다)
 → strSeq <= 0 이면 CHECK_DATA
 → prefix 가 비었거나 따옴표가 있거나, fromNo < 1, toNo < fromNo 면 CHECK_ZONE_RANGE
 → 개수가 50 을 넘으면 ZONE_RANGE_LIMIT
 → useCode 정규화 (없으면 01, 알 수 없는 값이면 CHECK_ZONE_USE)
 → fromNo 부터 toNo 까지 이름을 만들어
      이미 사용 중인 이름이면 skippedNames 에 담고 건너뛴다
      아니면 insertZone 으로 추가 (창고 존재 · 중지 확인은 여기서 일어난다)
 → 하나도 못 만들었으면 ZONE_RANGE_NONE
```

이름 규칙: `prefix + "-" + 번호`, 번호는 10 미만이면 두 자리로 채운다. `가`, `1`, `12` → `가-01` … `가-12`. 100 이상은 그대로 `가-100`.

## 테스트케이스

| TC | 구분 | 입력·조건 | 기대 결과 |
|---|---|---|---|
| TC-01 | 정상 | `prefix="가"`, `fromNo=1`, `toNo=12` | `addedCnt=12`, 이름이 `가-01`~`가-12` |
| TC-02 | 정상·자릿수 | `fromNo=8`, `toNo=11` | `가-08`, `가-09`, `가-10`, `가-11` |
| TC-03 | 정상·세자리 | `fromNo=99`, `toNo=101` | `가-99`, `가-100`, `가-101` |
| TC-04 | 정상·단건 | `fromNo=3`, `toNo=3` | `addedCnt=1`, `가-03` |
| TC-05 | 정상·이어붙이기 | `가-01`~`가-04` 가 이미 있는 창고에 `fromNo=5`, `toNo=7` | `addedCnt=3`, `가-05`~`가-07` |
| TC-06 | 정상·건너뛰기 | `가-02` 만 이미 있는 창고에 `fromNo=1`, `toNo=3` | `addedCnt=2`, `skippedNames=["가-02"]` |
| TC-07 | 정상·용도 | `useCode="pick"` | 만든 구역 전부 `useCode='02'` |
| TC-08 | 정상·용도 | `useCode` 생략 | 전부 `01`(일반) |
| TC-09 | 정상·설명 | 응답의 `zones[].zoneDesc` | 전부 `null` — 범위 추가는 설명을 넣지 않는다 |
| TC-10 | 정상·경계 | `fromNo=1`, `toNo=50` | `addedCnt=50` |
| TC-11 | 정상·재사용 | 사용 중지한 `가-02` 가 있는 창고에 `fromNo=1`, `toNo=3` | `가-02` 가 새로 만들어진다 (중지된 이름은 비교 대상이 아니다) |
| TC-12 | 범위 | `fromNo=12`, `toNo=3` (거꾸로) | `result=false`, `접두어와 시작·끝 번호를 확인하세요` 계열 |
| TC-13 | 범위 | `fromNo=0` | `result=false`, 같은 메시지 |
| TC-14 | 범위 | `fromNo=-1` | `result=false`, 같은 메시지 |
| TC-15 | 범위 | `fromNo` · `toNo` 누락 (둘 다 0 으로 들어옴) | `result=false`, 같은 메시지 |
| TC-16 | 범위 | `fromNo=1`, `toNo=51` (51개) | `result=false`, `한 번에 50개까지만 만들 수 있습니다.` |
| TC-17 | 접두어 | `prefix` 누락 또는 `"   "` | `result=false`, 범위 메시지 |
| TC-18 | 접두어 | `prefix="박형태's"` (따옴표) | `result=false`, 범위 메시지 |
| TC-19 | 접두어 | `prefix` 가 길어 만든 이름이 100자를 넘음 | `result=false`, 구역명 메시지 |
| TC-20 | 용도 | `useCode="zz"` | `result=false`, `구역 용도를 확인하세요…` |
| TC-21 | 창고 | `strSeq` 누락 또는 0 | `result=false`, `필수입력값을 확인하세요` |
| TC-22 | 창고 | 없는 `strSeq` | `result=false`, `데이터가 없습니다` |
| TC-23 | 창고 | 사용 중지된 창고 | `result=false`, `사용 중지된 창고는 볼 수만 있습니다.` |
| TC-24 | 전부중복 | 범위의 이름이 모두 이미 사용 중 | `result=false`, `새로 만든 구역이 없습니다. 모두 이미 쓰고 있는 이름입니다.` |
| TC-25 | 롤백 | 범위 도중 실패 (예: 창고가 중지됨) | 앞서 만든 구역도 남지 않는다 (`@Transactional`) |
| TC-26 | 권한 | 비로그인 | `result=false`, `권한이 없습니다` |
| TC-27 | 권한 | 쓰기권한 없는 계정 | `result=false`, `권한이 없습니다` |
| TC-28 | 타입 | `fromNo="abc"` (숫자 아님) | **Spring 기본 400.** `BasicResponse` 형태가 아니다 — 아래 R1 |
| TC-29 | 타입 | `fromNo=1.5` | Jackson 변환 결과에 따름. 서비스까지 오면 `1` 로 절삭 |
| TC-30 | 응답 | TC-06 응답 본문 | `addedCnt`, `zones[]`(whName·useName 채워짐), `skippedNames[]` |

TC-02·03·10·11·19·25~30 은 DB·HTTP 계층이라 Swagger 수동 확인이 필요하다.

## 남은 항목

### R1 — 숫자가 아닌 번호는 어느 필드가 문제인지 알 수 없다

`fromNo` · `toNo` 가 `int` 라 JSON 에 `"abc"` 가 오면 Jackson 이 변환에 실패하고, 서비스까지 오지 않는다. 서비스에 「숫자가 아님」 검사를 넣을 필요는 없다 — 타입이 막아 준다.

**응답 형태는 정상이다.** `ErrorController` 가 `BasicResponse` 로 바꿔 준다. 2026-10-16 로컬 서버에서 확인했다.

```
POST /api/cnf/str/zone/insertRange.do   {"fromNo":"abc", ...}
HTTP 400  {"result":false,"message":"잘못된 요청입니다.", ...}
```

남는 문제는 **어느 필드가 잘못됐는지 알 수 없다**는 것뿐이다. 번호를 생략하면 `int` 기본값 `0` 이 되어 `CHECK_ZONE_RANGE` 가 정상적으로 나간다.

### R2 — 중복 검사를 두 번 한다

루프에서 `countZoneName` 으로 이미 쓰는 이름을 걸러내고, 통과한 이름은 `insertZone()` 을 호출하는데 그 안의 `applyZone()` 이 같은 `countZoneName` 을 다시 돈다. 50개를 만들면 중복 검사 쿼리가 100번 나간다.

동작은 맞다. 창고 조회(`selectBySeq`)도 구역마다 한 번씩 돌아서, 50개면 창고 조회 50번 + 중복 검사 100번이다.

### R3 — 창고 확인이 범위 검사보다 늦다

`insertZoneRange` 는 `strSeq > 0` 만 보고 범위 · 용도를 먼저 검사한다. 창고가 있는지, 중지됐는지는 **첫 구역을 만들 때** `insertZone` 안에서 확인한다.

메시지는 정확히 나오므로 사용자가 보기에는 차이가 없다. 다만 없는 창고에 잘못된 범위를 보내면 범위 오류가 먼저 나와, 진짜 원인(창고 없음)이 뒤에 드러난다.

### R4 — 만들 수 있는 구역 수에 전체 상한이 없다

한 번에 50개로 막지만 반복 호출에는 제한이 없다. `zone/insert.do` 의 Z3 과 같다.
