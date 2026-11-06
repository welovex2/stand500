# 로데이터 측정일·환경조건 프론트 연동 (#26-8)

성적서는 제외. 서버 규칙은 raw-env-msrmn-multi.md

### 공통
1. envList : 시험항목별 측정 목록. 화면당 1개, RE만 3개(RE1·RE2·RE3)
2. envList.rows : 측정 1회(시작~종료). 최대 10행. TEL(3078)은 1행 (종료 후 canAdd false, [중단]·[삭제]로 0행이 되면 다시 [시작])
3. 기압은 ESD·RS·EFT·SURGE·CS·MF·VDIP만 저장
4. 기존 측정일·온습도·서명 필드(temp, hmdt, brmt, msrmnYear~Min, sign, signUrl, hz1Temp 등)는 사용 안 함

### 조회
detail.do 의 data.envList 사용. 데이터 없음(NO_DATA)이어도 envList는 내려옴

| envList | 설명 |
|---|---|
| metKey | 표 구분 (RE 화면만 사용) |
| canAdd | true면 [시작] 활성 |
| rows | 행 목록. 0개면 빈 행 1개 + [시작]만 표시 |

| envList.rows | 설명 |
|---|---|
| envSeq | 행 번호. 저장·중단·수정·삭제 때 사용 |
| testSDt / testEDt | 시작·종료 일시. 입력 불가 |
| temp, tempPlus, hmdt, hmdtPlus, brmt, brmtPlus | 온습도·기압 |
| memName | 시험자명 |
| atchFileId | 서명 파일 ID. 저장 때 그대로 보냄 |
| signUrl | 서명 이미지 주소. 표시용 |
| canEdit | true면 온습도·이름·서명 입력 가능 (진행 중 행) |
| canEnd | [종료] 표시 |
| canStop | [중단] 표시 |
| canRevEdit | [수정] 표시 (기술책임자, 종료된 행) |
| canDelete | [삭제] 표시 (기술책임자) |

### [시작]
POST /raw/{방법}/insert.do. 화면 저장 데이터에 아래 값 추가
```json
"envAction": "START",
"envRows": [
  { "envSeq": 0,
    "temp": "23.1", "tempPlus": "0.5", "hmdt": "45.2", "hmdtPlus": "0.5",
    "brmt": "99.8", "brmtPlus": "0.1", "memName": "김시험", "atchFileId": "" }
]
```
1. 새 행은 envSeq 0
2. RE 화면만 envMetKey 추가 (RE1 / RE2 / RE3). envAction과 같은 위치
3. RE 대역 분리 화면(raw_update_REC_9814 = RE3)도 re/insert.do 사용. envMetKey는 그 화면 대역
4. RE는 다른 대역에 진행 중 행이 있으면 envRows에 같이 넣음
5. 성공하면 data = rawSeq. 신규 화면이면 이 값으로 수정 모드 전환 (안 하면 로데이터 중복 생성)

### [저장]
같은 insert.do, envAction 빈 값
```json
"envAction": "",
"envRows": [ { "envSeq": 512, "temp": "24.0", "tempPlus": "0.5", "hmdt": "45.2", "hmdtPlus": "0.5",
               "brmt": "", "brmtPlus": "", "memName": "김시험", "atchFileId": "FILE_000000000000862" } ]
```
1. envRows에는 진행 중 행(canEdit=true)만. 종료된 행은 무시됨
2. 진행 중 행 없으면 envRows: []

### [종료]
같은 insert.do
```json
"envAction": "END",
"envRows": [ { "envSeq": 512, "temp": "...", "hmdt": "...", "memName": "...", "atchFileId": "..." } ]
```
1. RE 화면만 envMetKey 추가
2. 필수 : 온도·습도·시험자명·서명 (기압 화면은 기압까지). 없으면 서버에서 거부, 화면에서도 체크 필요
3. 같은 시험원(서명)이 같은 시간에 다른 측정 중이면 서버에서 거부. message 그대로 표시
4. 메시지 예 : 같은 시험원의 다른 측정과 시간이 겹칩니다. (SB26-G3712-EM3320 ESD 2026-11-05 14:33 ~ 진행 중)
5. 종료 후에는 기술책임자 [수정]만 가능

### [중단] (canStop)
사유 입력(필수, 200자) 후 POST /raw/env/{envSeq}/stop.do
```json
{ "testSeq": 21697, "reason": "장비 이상으로 재시험" }
```

### [수정] 기술책임자 (canRevEdit)
POST /raw/env/{envSeq}/update.do
```json
{ "testSeq": 21697,
  "testSDt": "2026-11-04 09:10:00", "testEDt": "2026-11-04 11:40:00",
  "temp": "23.1", "tempPlus": "0.5", "hmdt": "45.2", "hmdtPlus": "0.5",
  "brmt": "99.8", "brmtPlus": "0.1", "memName": "김시험", "atchFileId": "FILE_000000000000862" }
```
1. 일시는 여기서만 수정. 조회 형식(yyyy-MM-dd HH:mm:ss) 그대로 보냄
2. 보낸 값으로 덮어씀. 빈 값은 지움, atchFileId 빈 값은 서명 취소
3. 일시를 바꾸면 시간 겹침 오류 날 수 있음

### [삭제] 기술책임자 (canDelete)
사유 입력(필수, 200자) 후 POST /raw/env/{envSeq}/delete.do
```json
{ "testSeq": 21697, "reason": "중복 입력" }
```

모든 버튼은 성공 후 detail.do 다시 조회
