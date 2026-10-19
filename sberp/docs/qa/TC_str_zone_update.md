# 구역 수정 `/cnf/str/zone/update.do` 테스트케이스

- 대상: `POST /cnf/str/zone/update.do` (`StrController.updateZone` → `StrServiceImpl.updateZone`)
- 요청 본문: `Zone` — `zoneSeq` 필수, 나머지는 고칠 것만
- 화면에서 구역 목록의 **용도 칩을 누르면** 이 API 로 `{zoneSeq, useCode}` 만 보낸다

## 동작 순서

```
인증 확인 (쓰기 권한 검사를 받는다)
 → zoneSeq <= 0 이면 CHECK_DATA
 → selectZoneBySeq : 없으면 NO_DATA
 → 구역이 STATE='D' 면 ZONE_READONLY
 → 소속 창고 확인 : 없으면 NO_DATA, 중지됐으면 WH_READONLY
 → 보낸 항목 판정 : zoneName(값 있음) · zoneDesc(null 아님) · useCode(값 있음)
 → 셋 다 안 보냈으면 CHECK_DATA
 → 이름·설명을 보냈으면 : 이름 검증 · 설명 검증 · 같은 창고 중복(자기 제외) → updateZoneLabel
 → 용도를 보냈으면 : 정규화 후 기존과 다를 때만 → updateZoneUse
```

이름과 설명은 한 묶음이다. 설명만 보내도 이름 검증이 함께 돈다. 용도는 따로 처리하고, 값이 그대로면 UPDATE 를 돌리지 않는다.

## 테스트케이스

| TC | 구분 | 입력·조건 | 기대 결과 |
|---|---|---|---|
| TC-01 | 정상·이름 | `zoneSeq`, `zoneName="가-05"` | `result=true`. 설명·용도는 그대로 |
| TC-02 | 정상·설명 | `zoneSeq`, `zoneDesc="창가쪽 선반"` | 설명만 바뀌고 이름·용도는 그대로 |
| TC-03 | 정상·설명지움 | `zoneDesc=""` | 설명이 비워진다 |
| TC-04 | 정상·설명유지 | `zoneDesc` 를 본문에서 뺌 | 기존 설명 유지 |
| TC-05 | 정상·용도 | `zoneSeq`, `useCode="02"` | 용도만 바뀌고 이름·설명은 그대로 |
| TC-06 | 정상·용도 | `useCode="pick"` / `"인수요청"` | `02` 로 변환 |
| TC-07 | 정상·동시 | 이름·설명·용도를 함께 전송 | 전부 반영 |
| TC-08 | 정상·무변경 | 기존과 같은 `useCode` | `result=true`. 용도 UPDATE 는 돌지 않는다 |
| TC-09 | 정상·공백 | `zoneName="  가-05  "` | `가-05` 로 정리 |
| TC-10 | 정상·응답 | 응답 본문 | `whName`·`useName` 채워짐 |
| TC-11 | 필수값 | `zoneSeq` 누락 또는 0 | `result=false`, `필수입력값을 확인하세요` |
| TC-12 | 필수값 | `zoneSeq` 만 전송 (고칠 것 없음) | `result=false`, `필수입력값을 확인하세요` |
| TC-13 | 이름 | `zoneName="   "` | 「보내지 않음」으로 보고 기존 이름 유지 |
| TC-14 | 이름 | `zoneName` 101자 | `result=false`, 구역명 메시지 |
| TC-15 | 이름 | `zoneName` 에 따옴표 | `result=false`, 구역명 메시지 |
| TC-16 | 설명 | `zoneDesc` 201자 | `result=false`, `구역설명은 200자까지입니다.` |
| TC-17 | 용도 | `useCode="zz"` | `result=false`, `구역 용도를 확인하세요…` |
| TC-18 | 중복 | 같은 창고의 다른 사용 구역과 같은 이름 | `result=false`, `이 창고에 이미 있는 구역명입니다.` |
| TC-19 | 중복 | 자기 이름 그대로 | `result=true` |
| TC-20 | 중복 | 사용 중지된 구역과 같은 이름 | `result=true` |
| TC-21 | 상태 | 없는 `zoneSeq` | `result=false`, `데이터가 없습니다` |
| TC-22 | 상태 | 사용 중지된 구역 | `result=false`, `사용 중지된 구역은 고칠 수 없습니다.` |
| TC-23 | 상태 | 사용 중지된 창고의 구역 | `result=false`, `사용 중지된 창고는 볼 수만 있습니다.` |
| TC-24 | 권한 | 비로그인 | `result=false`, `권한이 없습니다` |
| TC-25 | 권한 | 쓰기권한 없는 계정 | `result=false`, `권한이 없습니다` |
| TC-26 | 동시성 | 수정 중 다른 세션이 구역을 중지 | `result=false`, `사용 중지된 구역은 고칠 수 없습니다.` 이름 변경도 롤백 |
| TC-27 | 연계 | 용도를 바꿨을 때 그 구역 시료의 상태 | 기획: 함께 바뀐다. **현재는 바뀌지 않는다** — 아래 ZU2 |

TC-01~10·14·16·18~26 은 DB·HTTP 계층이라 Swagger 수동 확인이 필요하다.

## 남은 항목

### ZU2 — 용도를 바꿔도 그 구역 시료의 상태가 따라가지 않는다 (보류)

> **2026-10-16 — 창고를 먼저 끝내고 시료에서 다룬다.** 설계는 `docs/design/시료상태_설계.md` 에 있다.

기획서는 용도 칩을 누르면 구역 용도와 함께 그 구역에 있는 시료의 상태를 일괄 변경하고, 시료마다 「용도 변경」 이력 줄을 남긴다(업무규칙 63~65). 현재 `updateZoneUse` 는 `ZONE_TB.USE_CODE` 한 칸만 바꾼다.

시료상태 칼럼(`IM_SUB_TB.SST_CODE` 등)과 이력 테이블(`IM_SUB_HIS_TB`)이 생겨야 붙일 수 있다. **창고 쪽 작업으로는 더 할 것이 없다.**

### ZU4 — 용도를 바꾸면 라벨을 다시 뽑아야 한다는 신호가 없다

기획서는 라벨에 용도 글자를 인쇄하므로, 용도가 바뀌면 QR 보드를 다시 출력해 붙여야 한다(QR 값 자체는 그대로). 응답에 그런 표시가 없어 프런트가 스스로 판단해야 한다. 프런트가 바꾼 값을 알고 있으므로 막히지는 않는다.
