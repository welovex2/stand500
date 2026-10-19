# 창고 수정 `/cnf/str/update.do` 테스트케이스

- 대상: `POST /cnf/str/update.do` (`StrController.update` → `StrServiceImpl.update`)
- 요청 본문: `Warehouse` — `strSeq` 필수. 사용 중지는 이 API 가 아니라 `/cnf/str/stop.do`
- 검증 보조: `GET /cnf/str/{strSeq}/detail.do`

## 동작 순서

```
인증 확인
 → strSeq <= 0 이면 CHECK_DATA
 → selectBySeq : 없으면 NO_DATA
 → STATE='D' 면 WH_READONLY (중지된 창고는 볼 수만 있다)
 → validate : whName → mngId → typeCode(공통코드 조회) → 공백 정리 → 창고명 중복(자기 제외)
 → 병합 : 본문에 없는 항목은 저장된 값으로 채운다 (2026-10-16)
 → validate : whName → mngId → typeCode(공통코드 조회) → 공백 정리 → 창고명 중복(자기 제외)
 → UPDATE : 0행이면 WH_READONLY 예외
```

**보낸 항목만 고친다.** 구역 수정(`updateZone`)과 같은 규칙이다.

| 항목 | 「보냈다」 판정 | 비우려면 |
|---|---|---|
| `whName` · `typeCode` | 값이 있을 때 | 비울 수 없다 |
| `mngId` · `whLoc` | `null` 이 아닐 때 | 빈 문자열을 보낸다 |
| `deptSeq` | `null` 이 아닐 때 | `0` 이하를 보낸다 |

다섯 항목을 하나도 보내지 않으면 `필수입력값을 확인하세요` 로 거절한다.

## 테스트케이스

| TC | 구분 | 입력·조건 | 기대 결과 |
|---|---|---|---|
| TC-01 | 정상 | 모든 항목을 채워 수정 | `result=true`. 값이 모두 반영 |
| TC-02 | 정상·응답 | TC-01 응답 본문 | `data` 는 요청 echo. `typeCode` 는 정규화된 값 |
| TC-03 | 정상·이름유지 | 이름은 그대로, 위치만 변경 | `result=true` — 자기 이름은 중복이 아니다 |
| TC-04 | 정상·공백 | `whName="  1창고  "`, `whLoc="   "`, `mngId=" hong "` | `1창고`, `whLoc=null`, `hong` 으로 정리 |
| TC-05 | 정상·상태 | 수정 후 `detail.do` | `state='U'`, `udtDt`=오늘, `udtMemName`=수정자 |
| TC-06 | 필수값 | `whName` 누락 | `result=false`, `필수입력값을 확인하세요` |
| TC-07 | 정상 | `mngId=""` 로 관리자 비우기 | `result=true`, `MNG_ID` 가 `NULL` (2026-10-16 변경) |
| TC-08 | 형태 | `typeCode=99` | `result=false`, `창고 형태를 확인하세요.` |
| TC-09 | 중복 | 다른 사용 중인 창고와 같은 이름 | `result=false`, `같은 이름의 창고가 있습니다.` |
| TC-10 | 중복 | 사용 중지된 창고와 같은 이름 | `result=true` — 중지된 이름은 재사용 가능 |
| TC-11 | 상태 | 사용 중지된 창고 수정 | `result=false`, `사용 중지된 창고는 볼 수만 있습니다.` |
| TC-12 | 없음 | 없는 `strSeq` | `result=false`, `데이터가 없습니다` |
| TC-13 | 경계 | `strSeq=0` / 누락 | `result=false`, `필수입력값을 확인하세요` |
| TC-14 | 권한 | 비로그인 | `result=false`, `권한이 없습니다` |
| TC-15 | 권한 | 로그인했지만 창고 메뉴 쓰기권한 없음 | `result=false`, `권한이 없습니다` |
| TC-16 | 보안 | 본문에 `udtMemId=admin` | 저장된 값은 로그인 ID |
| TC-17 | 보안 | 본문에 `state='D'` | 무시된다. UPDATE 가 `STATE='U'` 로 고정 |
| TC-18 | 부분수정 | `deptSeq` 만 전송 | 부서만 바뀌고 이름·위치·형태·관리자는 유지 |
| TC-19 | 부분수정 | `whLoc` 을 빼고 전송 | 기존 위치가 유지된다 (2026-10-16 수정됨) |
| TC-20 | 부분수정 | `typeCode` 를 빼고 전송 (기존 02) | 02 가 유지된다 (2026-10-16 수정됨) |
| TC-20b | 부분수정 | `whLoc=""`, `memo=""` 전송 | 둘 다 비워진다 |
| TC-20c | 부분수정 | `strSeq` 만 전송 | `result=false`, `필수입력값을 확인하세요` |
| TC-21 | 동시성 | 수정 중 다른 세션이 중지 | `result=false`, `사용 중지된 창고는 볼 수만 있습니다.` (2026-10-16 수정됨) |
| TC-22 | 경계 | `whName` 101자 | 미검증 구간 (`TC_str_insert.md` 와 동일) |
| TC-23 | 참조 | 없는 `mngId` | 현재는 통과 (`TC_str_insert.md` 와 동일) |

## 남은 항목

### U4 — 길이 · 관리자 실재 검증 없음

`TC_str_insert.md` 의 ②·③ 과 같다. `validate()` 를 등록과 공유하므로 증상도 같다.
