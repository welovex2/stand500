# 창고 사용 중지 `/cnf/str/stop.do` 테스트케이스

- 대상: `POST /cnf/str/stop.do` (`StrController.stop` → `StrServiceImpl.stop`)
- 요청 본문: `WhStopReq` — `{ "strSeq": 1 }` 하나뿐이다
- 성격: **되살리지 않는 작업.** 복구 API 가 없다
- 검증 보조: `GET /cnf/str/{strSeq}/detail.do`, `GET /cnf/str/list.do?includeStopped=Y`

## 동작 순서

```
인증 확인
 → strSeq <= 0 이면 CHECK_DATA
 → selectBySeq : 없으면 NO_DATA
 → 이미 STATE='D' 면 WH_ALREADY_STOPPED
 → countItemsByWh > 0 이면 건수를 담은 메시지로 거절
 → 창고만 중지 (STATE='D', UDT_DT=NOW())  // 구역은 건드리지 않는다
```

중지 후 `state='D'` 이고 그때의 `udtDt` 가 곧 중지일이다.

## 테스트케이스

| TC | 구분 | 입력·조건 | 기대 결과 |
|---|---|---|---|
| TC-01 | 정상 | 시료 0건, 구역 3개(모두 사용 중) | `result=true`. 창고만 `state='D'`. **구역은 그대로** |
| TC-02 | 정상 | 구역 0개인 창고 | `result=true` |
| TC-03 | 정상·구역 | 구역 중 하나가 이미 중지됨 | 구역의 `state`·`udtDt` 가 전혀 바뀌지 않는다 |
| TC-04 | 정상·이력 | 중지 수행자 확인 | 창고의 `udtMemId` 가 로그인 사용자 |
| TC-05 | 정상·날짜 | 중지 직후 `detail.do` | `state='D'`, `udtDt` = 오늘 |
| TC-06 | 정상·집계 | 중지 직후 `detail.do` | `zoneCnt`·`stopZoneCnt` 가 중지 전과 같다 (구역을 건드리지 않으므로) |
| TC-07 | 정상·목록 | 중지 직후 `list.do` | 기본 목록에서 사라짐 |
| TC-08 | 정상·목록 | `list.do?includeStopped=Y` | 다시 보이고 `state='D'` |
| TC-09 | 거절 | 보관 시료 1건 이상 | `result=false`, `2창고 에 보관 중인 시료가 3건 있습니다.` + 줄바꿈 + `시료를 모두 꺼내거나…` |
| TC-10 | 거절 | 이미 중지된 창고 | `result=false`, `이미 사용 중지된 창고입니다.` |
| TC-11 | 거절 | 없는 `strSeq` | `result=false`, `데이터가 없습니다` |
| TC-12 | 거절 | `strSeq=0` | `result=false`, `필수입력값을 확인하세요` |
| TC-13 | 거절 | `strSeq` 누락(본문 `{}`) | `result=false`, `필수입력값을 확인하세요` |
| TC-14 | 권한 | 비로그인 | `result=false`, `권한이 없습니다`. 데이터 무변화 |
| TC-14b | 권한 | 로그인했지만 창고 메뉴 쓰기권한 없음 | `result=false`, `권한이 없습니다` (2026-10-15 추가) |
| TC-15 | 후속 | 중지된 창고에 `update.do` | `result=false`, `사용 중지된 창고는 볼 수만 있습니다.` |
| TC-16 | 후속 | 중지된 창고에 `zone/insert.do` | 거절 |
| TC-17 | 후속 | 중지된 구역 QR 로 위치 스캔 | `사용 중지된 구역입니다. 라벨을 떼 주세요.` |
| TC-17b | 후속 | **중지된 창고의 살아 있는 구역** QR 로 위치 스캔 | `사용 중지된 창고입니다. 라벨을 떼 주세요.` (2026-10-15 추가) |
| TC-18 | 후속 | 중지한 창고명으로 새 창고 등록 | `result=true` — 중지된 이름은 재사용 가능 |
| TC-19 | 거절·전이 | 시료를 모두 뺀 뒤 재시도 | `result=true` |
| TC-20 | 동시성 | 두 세션이 동시에 중지 | 나중 요청이 `result=false`, `이미 사용 중지된 창고입니다.` 전부 롤백 (2026-10-15 수정됨) |

## 남은 항목

### S3 — 중지된 구역의 시료는 가드가 세지 않는다

`countItemsByWh` 의 `INNER JOIN ZONE_TB z ON ... AND z.STATE != 'D'` 때문에, 중지된 구역에 놓인 시료는 집계에서 빠진다. 그런 시료가 있어도 0건으로 보여 중지가 통과한다.

정상 경로로는 생기지 않는다 — 구역 중지는 시료 0건일 때만 되고, 위치 스캔도 중지된 구역을 거부한다(`SamServiceImpl.java:237`). DB 를 직접 손댈 때만 드러난다. **우선순위 낮음.**

