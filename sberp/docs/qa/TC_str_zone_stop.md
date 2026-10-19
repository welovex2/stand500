# 구역 사용 중지 `/cnf/str/zone/stop.do` 테스트케이스

- 대상: `POST /cnf/str/zone/stop.do` (`StrController.stopZone` → `StrServiceImpl.stopZone`)
- 요청 본문: `ZoneStopReq` — `zoneSeq` 하나
- 성격: **되살리지 않는 작업.** 복구 API 가 없다
- 검증 보조: `GET /cnf/str/{strSeq}/detail.do`

## 동작 순서

```
인증 확인 (쓰기 권한 검사를 받는다)
 → zoneSeq <= 0 이면 CHECK_DATA
 → selectZoneBySeq : 없으면 NO_DATA
 → 이미 STATE='D' 면 ZONE_ALREADY_STOPPED
 → 보관 시료가 있으면 구역명과 건수를 담은 메시지로 거절
 → 소속 창고가 중지됐으면 ZONE_WH_STOPPED
 → 그 창고의 사용 중인 구역이 하나뿐이면 ZONE_LAST_ONE
 → UPDATE ZONE_TB (STATE='D', UDT_DT=NOW()). 0행이면 ZONE_ALREADY_STOPPED
```

소속 창고는 요청에서 받지 않고 저장된 값에서 가져온다. 중지 후 구역은 `detail.do` 의 목록에 `state='D'` 로 남고, 그 구역 QR 로 위치 스캔하면 거절된다.

## 테스트케이스

| TC | 구분 | 입력·조건 | 기대 결과 |
|---|---|---|---|
| TC-01 | 정상 | 시료 0건인 구역 | `result=true`. `state='D'`, `udtDt`=오늘 |
| TC-02 | 정상·이력 | 중지 수행자 확인 | `udtMemId` 가 로그인 사용자 |
| TC-03 | 정상·목록 | 중지 직후 `detail.do` | 그 구역이 목록에 남고 `state='D'` |
| TC-04 | 정상·집계 | 중지 직후 `detail.do` | `zoneCnt` 가 1 줄고 `stopZoneCnt` 가 1 는다 |
| TC-05 | 정상·이름재사용 | 중지한 구역명으로 `zone/insert.do` | `result=true` — 중지된 이름은 다시 쓸 수 있다 |
| TC-06 | 거절 | 보관 시료 1건 이상 | `result=false`, `가-01 에 보관 중인 시료가 3건 있습니다.` + 줄바꿈 + `시료를 모두 꺼내거나 다른 구역으로 옮긴 뒤에 중지할 수 있습니다.` |
| TC-07 | 거절 | 이미 중지된 구역 | `result=false`, `이미 사용 중지된 구역입니다.` |
| TC-08 | 거절 | 없는 `zoneSeq` | `result=false`, `데이터가 없습니다` |
| TC-09 | 거절 | `zoneSeq=0` 또는 누락 | `result=false`, `필수입력값을 확인하세요` |
| TC-10 | 권한 | 비로그인 | `result=false`, `권한이 없습니다` |
| TC-11 | 권한 | 쓰기권한 없는 계정 | `result=false`, `권한이 없습니다` |
| TC-12 | 후속 | 중지된 구역 QR 로 위치 스캔 | `사용 중지된 구역입니다. 라벨을 떼 주세요.` |
| TC-13 | 후속 | 중지된 구역을 `zone/update.do` 로 수정 | `result=false`, `사용 중지된 구역은 고칠 수 없습니다.` |
| TC-14 | 후속 | 중지된 구역이 QR 보드 출력 대상에서 빠지는지 | 기획: 빠진다 (프런트 처리) |
| TC-15 | 가드 | 창고에 남은 마지막 사용 구역 | `result=false`, `이 창고에 남은 마지막 사용 구역이라 중지할 수 없습니다…` |
| TC-16 | 가드 | 사용 중지된 창고의 구역 | `result=false`, `사용 중지된 창고입니다. 구역을 따로 중지할 필요가 없습니다.` |
| TC-17 | 동시성 | 두 세션이 동시에 중지 | 나중 요청이 `result=false`, `이미 사용 중지된 구역입니다.` |
| TC-18 | 타입 | `zoneSeq="abc"` | Spring 기본 400 (`TC_str_zone_insertRange.md` R1 과 같은 뿌리) |

TC-01~05·10~14·17·18 은 DB·HTTP 계층이라 Swagger 수동 확인이 필요하다.

## 남은 항목

### ZS4 — 거절 문구를 창고 쪽과 맞췄는지 전체 점검

구역 중지의 시료 거절 문구는 창고 중지와 같은 두 줄 형식으로 맞췄다. 다른 엔드포인트의 거절 문구는 아직 제각각이라, 한 번 훑어 정리할 필요가 있다.
