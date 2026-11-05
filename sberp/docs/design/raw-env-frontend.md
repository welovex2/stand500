# 로데이터 측정일·환경조건 — 프론트 연동 (#26-8)

> 성적서 출력은 이 문서 범위 밖. 자세한 서버 규칙: `raw-env-msrmn-multi.md`

## 0. 공통

- **envList** = 시험항목 하나의 측정 목록. 화면 하나에 표 하나. **RE만 3개**(대역별 RE1·RE2·RE3).
- **envList.rows** = 측정 한 번 (시작 ~ 종료). 표 하나에 최대 10행.
- 기압 칸은 ESD·RS·EFT·SURGE·CS·MF·VDIP 화면에만 있다. 나머지 화면은 기압을 보내도 저장 안 됨.
- 기존 측정일·온습도·서명 필드(`temp`, `hmdt`, `brmt`, `msrmnYear~Min`, `sign`, `signUrl`, RE의 `hz1Temp` …)는 **더 이상 안 씀**. 보내도 무시된다.

---

## 1. 조회 (화면 그리기)

`detail.do` → `data.envList` (표 목록. RE는 3개, 나머지는 1개)

**envList**

| 값 | 쓰임 |
|---|---|
| `metKey` | 표 구분. RE 화면에서만 의미 있음 (RE1/RE2/RE3) |
| `canAdd` | [시작] 버튼 활성 |
| `rows` | 행 목록 |

`rows`가 0개면 빈 입력 행 1개 + [시작] 버튼만 그린다.

**envList.rows**

| 값 | 쓰임 |
|---|---|
| `envSeq` | 행 번호 (저장·중단·수정·삭제 때 보냄) |
| `testSDt` / `testEDt` | 시작·종료 일시. 화면에서 **직접 입력 불가** (읽기 전용) |
| `temp` `tempPlus` `hmdt` `hmdtPlus` `brmt` `brmtPlus` | 온습도·기압 |
| `memName` | 시험자명 |
| `atchFileId` | 서명 첨부파일 ID. 저장할 때 **이 값을 그대로** 보낸다 |
| `signUrl` | 서명 이미지 주소. **화면 표시용만** (저장 때 보내지 않음) |
| `canEdit` | 온습도·이름·서명 **입력칸 활성** (진행 중 행). false면 읽기 전용 |
| `canEnd` | [종료] 버튼 노출 |
| `canStop` | [중단] 버튼 노출 |
| `canRevEdit` | [수정] 버튼 노출 (기술책임자 + 종료된 행) |
| `canDelete` | [삭제] 버튼 노출 (기술책임자) |

---

## 2. [시작]

`POST /raw/{방법}/insert.do` — **화면 전체 저장 데이터** + 아래 값

```json
"envAction": "START",
"envRows": [
  { "envSeq": 0,
    "temp": "23.1", "tempPlus": "0.5", "hmdt": "45.2", "hmdtPlus": "0.5",
    "brmt": "99.8", "brmtPlus": "0.1", "memName": "김시험", "atchFileId": "" }
]
```

- 새 행은 `envSeq: 0`
- **RE 화면만** envAction 과 같은 단계에 `"envMetKey": "RE1"`(RE2/RE3)을 같이 보낸다. 다른 화면은 안 보내도 된다.
  RE를 대역별로 나눈 화면(예: `raw_update_REC_9814.html` = RE3)도 `re/insert.do`를 쓰므로 그 화면의 대역(`RE3`)을 보낸다.
  RE는 다른 대역 표에 진행 중 행이 있으면 그 행도 `envRows`에 넣는다 (값 저장).
- 성공하면 `data` = **rawSeq**. 신규 화면이었다면 **이 값으로 수정 모드로 전환** (안 하면 다음 저장 때 로데이터가 또 생김).

## 3. [저장] (화면 하단 저장 버튼)

같은 `insert.do`, `envAction` 빈 값

```json
"envAction": "",
"envRows": [ { "envSeq": 512, "temp": "24.0", "tempPlus": "0.5", "hmdt": "45.2", "hmdtPlus": "0.5",
               "brmt": "", "brmtPlus": "", "memName": "김시험", "atchFileId": "FILE_000000000000862" } ]
```

- `envRows`에는 **진행 중 행(`canEdit=true`)만** 넣는다. 종료된 행은 보내도 무시된다.
- 진행 중 행이 없으면 `envRows: []`.

## 4. [종료]

같은 `insert.do`

```json
"envAction": "END",
"envRows": [ { "envSeq": 512, "temp": "...", "hmdt": "...", "memName": "...", "atchFileId": "..." } ]
```

- RE 화면만 `"envMetKey": "RE1"`(RE2/RE3)을 `envAction`과 같은 단계에 같이 보낸다 (`envRows` 안이 아님).
- 종료 전 필수: **온도·습도·시험자명·서명(`atchFileId`)** (기압 칸 있는 화면은 기압도). 비어 있으면 서버가 거부 → 화면에서도 먼저 막기.
- 같은 시험원(서명)이 그 시간에 다른 측정을 하고 있었으면 서버가 거부한다. 응답 `message`를 그대로 띄운다.
  예: `같은 시험원의 다른 측정과 시간이 겹칩니다. (SB26-G3712-EM3320 ESD 2026-11-05 14:33 ~ 진행 중)`
  기술책임자 [수정]에서 일시를 바꿀 때도 같은 오류가 날 수 있다.
- 종료 후 그 행은 시험원이 못 고친다 (기술책임자 [수정]만).

## 5. [중단] (진행 중 행, `canStop`)

사유 입력 모달 (필수, 200자) → `POST /raw/env/{envSeq}/stop.do`

```json
{ "testSeq": 21697, "reason": "장비 이상으로 재시험" }
```

## 6. [수정] 기술책임자 (`canRevEdit`)

수정 모달 → `POST /raw/env/{envSeq}/update.do`

```json
{ "testSeq": 21697,
  "testSDt": "2026-11-04 09:10:00", "testEDt": "2026-11-04 11:40:00",
  "temp": "23.1", "tempPlus": "0.5", "hmdt": "45.2", "hmdtPlus": "0.5",
  "brmt": "99.8", "brmtPlus": "0.1", "memName": "김시험", "atchFileId": "FILE_000000000000862" }
```

- 이 모달에서만 일시 입력 가능. 일시는 조회 값 형식(`yyyy-MM-dd HH:mm:ss`) 그대로 보낸다.
- 값은 보낸 그대로 덮어쓴다 (빈 값 = 지움, `atchFileId` 빈 값 = 서명 취소).

## 7. [삭제] 기술책임자 (`canDelete`)

사유 입력 모달 (필수, 200자) → `POST /raw/env/{envSeq}/delete.do`

```json
{ "testSeq": 21697, "reason": "중복 입력" }
```

---

모든 버튼은 성공 후 `detail.do`를 다시 불러 표를 새로 그린다.
