# 창고 상세 `/cnf/str/{strSeq}/detail.do` 테스트케이스

- 대상: `GET /cnf/str/{strSeq}/detail.do` (`StrController.detail` → `StrServiceImpl.detail` → `selectBySeq` + `selectZoneList`)
- 응답: `StoreDetailDTO` (= `Warehouse` + `zones[]`)
- 사전조건: 로그인 세션, `STR_TB`·`ZONE_TB` 데이터, 공통코드 `WH`

## 동작 요약

```
인증 확인 → selectBySeq(strSeq)  // STATE 필터 없음. 중지된 창고도 연다
         → null 이면 NO_DATA
         → copy(wh, dto)
         → selectZoneList(strSeq) // STATE 필터 없음. 중지된 구역도 담는다
         → 구역마다 whName·useName 채움
```

창고도 구역도 **중지된 것까지 보여주는 것이 의도된 동작**이다. 구분은 `state` 필드로만 한다.

## 테스트케이스

| TC | 구분 | 입력·조건 | 기대 결과 |
|---|---|---|---|
| TC-01 | 정상 | 사용 중인 창고, 구역 3개 | `result=true`, `data.strSeq` 일치, `zones` 3건 |
| TC-02 | 정상 | 구역 0개인 창고 | `zones: []` (null 아님) |
| TC-03 | 정상·필드 | 모든 항목이 채워진 창고 | `whName`·`whLoc`·`typeCode`·`typeName`·`mngId`·`mngName`·`mngDeptName`·`deptSeq`·`deptName` 반환 |
| TC-04 | 정상·집계 | 사용 구역 5, 중지 구역 1, 시료 45 | `zoneCnt=5`, `stopZoneCnt=1`, `itemCnt=45` |
| TC-05 | 정상·정렬 | `DIS_ORDR` 가 섞인 구역들 | `DIS_ORDR` → `ZONE_SEQ` 순으로 정렬 |
| TC-06 | 정상·구역 | 구역 목록 | 구역마다 `whName` 이 창고명으로 채워짐 |
| TC-07 | 정상·구역 | `useCode` 01 / 02 / 03 | `useName` 일반 / 인수요청 / 장기 |
| TC-08 | 정상·구역 | `zoneDesc` 없는 구역 | `zoneDesc: null` |
| TC-09 | 정상·일관성 | 사용 중인 구역들의 `itemCnt` 합 | 창고 `itemCnt` 와 같다 |
| TC-10 | 중지 | 사용 중지된 창고 | `result=true`, `state='D'` 로 열린다 |
| TC-11 | 중지 | 사용 중지된 창고 | `zoneCnt`·`stopZoneCnt` 가 중지 전과 같다 — 창고 중지는 구역을 건드리지 않는다 (2026-10-15 변경) |
| TC-12 | 중지 | 구역 하나를 중지한 창고 | 그 구역이 목록에 남고 `state='D'` |
| TC-13 | 중지 | 중지된 구역 | `itemCnt` 가 0 이다 (중지 가드가 0건만 허용) |
| TC-14 | 없음 | 없는 `strSeq` | `result=false`, `데이터가 없습니다` |
| TC-15 | 경계 | `strSeq=0` | `result=false`, `데이터가 없습니다` |
| TC-16 | 경계 | `strSeq=-1` | `result=false`, `데이터가 없습니다` |
| TC-17 | 경계 | `/cnf/str/abc/detail.do` | **Spring 기본 400**. `BasicResponse` 형태가 아니다 |
| TC-18 | 경계 | `/cnf/str/99999999999/detail.do` (int 초과) | **Spring 기본 400** |
| TC-19 | 권한 | 비로그인 | `result=false`, `권한이 없습니다` |
| TC-20 | 응답계약 | 응답 본문 확인 | `insDt`·`udtDt`·`udtMemName` 이 `list.do` 와 똑같이 내려온다 (2026-10-15 수정됨) |
| TC-21 | 응답계약 | `state` 값 확인 | `I`/`U`/`D`. 사용중 판별은 `!= 'D'` (수정하면 `U` 가 된다) |
| TC-22 | 응답계약 | `zones[].state` | 중지 구역 구분용. Swagger 에 설명과 함께 노출됨 (2026-10-15 수정됨) |

## 남은 항목

### F5 — 시료 건수 집계 기준이 창고와 구역에서 다르다

- `whItemCnt` : `INNER JOIN ZONE_TB z ... AND z.STATE != 'D'` → 중지된 구역의 시료 **제외**
- `zoneItemCnt` : 구역 상태를 보지 않음 → 중지된 구역도 **집계**

중지된 구역에 시료가 남으면 구역 목록의 합과 창고 `itemCnt` 가 어긋난다.

다만 정상 경로로는 생기지 않는다. 구역 중지는 시료 0건일 때만 되고(`stopZone` 가드), 스캔도 중지된 구역을 거부한다(`SamServiceImpl.java:237`, `ZONE_LABEL_OFF`). DB 를 직접 손대는 경우에만 드러난다. **우선순위 낮음.**

### F6 — 경로변수 형식 오류 메시지에 Java 예외 원문이 나온다

`@PathVariable(name = "strSeq") int` 라 `abc` 가 오면 변환에 실패한다.

**응답 형태는 정상이다.** `ErrorController` 가 컨테이너 오류를 `BasicResponse` 로 바꿔 주므로 `result`/`message` 모양은 다른 API 와 같다. 2026-10-16 로컬 서버에서 확인했다.

```
GET /api/cnf/str/abc/detail.do
HTTP 400  {"result":false,"message":"For input string: \"abc\"", ...}
```

문제는 **메시지가 Java 예외 원문**이라는 것이다. 사용자에게 그대로 띄울 수 없고, 내부 구현이 드러난다. `GlobalExceptionHandler` 에 타입 변환 예외를 잡아 「요청 형식을 확인하세요」 같은 문구로 바꾸면 된다.
