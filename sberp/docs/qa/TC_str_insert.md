# 창고 등록 `/cnf/str/insert.do` 테스트케이스

- 대상: `POST /cnf/str/insert.do` (`StrController.insert` → `StrServiceImpl.insert` → `StrMapper.insert`)
- 사전조건: 로그인 세션, 공통코드 `TOP_CD='WH'` 에 01/02/03 등록, `STR_TB` 생성
- 검증 보조: `GET /cnf/str/list.do`, `GET /cnf/str/{strSeq}/detail.do`, `SELECT * FROM STR_TB`

## 요청 본문

| 필드 | 필수 | 비고 |
|---|---|---|
| `whName` | O | 창고명. 사용 중인 창고끼리 중복 불가 |
| `mngId` | - | 창고 관리자 `MEMBER_TB.ID`. 선택 |
| `deptSeq` | - | 담당 부서 `DEPT_TB.DEPT_SEQ`. 선택. 목록은 `/cnf/mem/dept/list.do` |
| `typeCode` | - | 생략 시 `01`(방) |
| `whLoc` | - | 위치 자유텍스트 |
| `memo` | - | |

`insMemId`·`udtMemId` 는 본문 값을 무시하고 로그인 사용자로 덮어쓴다.

## 검증 순서

인증 → `whName` → `typeCode`(공통코드 조회) → 공백 정리 → 창고명 중복 → INSERT

앞 단계에서 걸리면 뒤 단계는 보지 않는다. 기대 메시지가 순서에 묶여 있으므로 케이스를 섞지 말 것.

## 테스트케이스

| TC | 구분 | 입력 | 기대 결과 |
|---|---|---|---|
| TC-01 | 정상 | `whName=1창고`, `mngId=hong`, `typeCode=01`, `whLoc=본관 1층 시료실` | `result=true`, `data.strSeq>0`, `STR_TB.STATE='I'` |
| TC-02 | 정상·기본값 | `typeCode` 미전송 | `result=true`, 저장된 `TYPE_CODE='01'` |
| TC-03 | 정상·기본값 | `typeCode=""` (빈 문자열) | `result=true`, `TYPE_CODE='01'` |
| TC-04 | 정상·선택필드 | `whLoc` 미전송 | `result=true`, `STR_LOC` 이 `NULL` |
| TC-04b | 정상·담당주체 | `mngId`·`deptSeq` 둘 다 미전송 | `result=true` — 둘 다 선택이다 |
| TC-04c | 정상·담당주체 | `deptSeq` 만 전송 | `result=true`, `deptName` 이 응답에 채워짐 |
| TC-05 | 정상·공백정리 | `whName="  2창고  "`, `whLoc="   "` | `STR_NAME='2창고'`, `STR_LOC` 이 `NULL` |
| TC-06 | 정상·채번 | 연속 2건 등록 | `strSeq` 가 서로 다르고 증가 |
| TC-07 | 필수값 | `whName` 미전송 | `result=false`, `필수입력값을 확인하세요` |
| TC-08 | 필수값 | `whName="   "` | `result=false`, `필수입력값을 확인하세요` |
| TC-09 | 정상 | `mngId` 미전송 | `result=true` — 관리자는 필수가 아니다 (2026-10-16 변경) |
| TC-10 | 필수값 | `whName` 누락 | `result=false`, `필수입력값을 확인하세요` |
| TC-11 | 본문 | 본문 `{}` | `result=false`, `필수입력값을 확인하세요` |
| TC-12 | 형태 | `typeCode=99` | `result=false`, `창고 형태를 확인하세요.` |
| TC-13 | 형태 | `typeCode=02`, `03` 각각 | `result=true`, 해당 코드로 저장 |
| TC-14 | 형태 | 공통코드 `WH/03` 을 `USE_YN=0` 으로 바꾸고 `typeCode=03` | `result=false`, `창고 형태를 확인하세요.` |
| TC-15 | 중복 | TC-01 과 같은 `whName` 으로 재등록 | `result=false`, `같은 이름의 창고가 있습니다.` |
| TC-16 | 중복 | 사용 중지(`/stop.do`)한 창고와 같은 이름으로 등록 | `result=true` — 중지된 이름은 재사용 가능 |
| TC-17 | 중복 | `whName="1창고 "` (뒤 공백) 로 TC-01 중복 시도 | `result=false` — trim 후 비교 |
| TC-18 | 권한 | 비로그인(세션 없음) | `result=false`, `권한이 없습니다`, `STR_TB` 무변화 |
| TC-19 | 보안 | `insMemId=admin` 을 본문에 실어 전송 | 저장된 `INS_MEM_ID` 는 로그인 ID. 본문 값 무시 |
| TC-20 | 보안 | `whName="1'창고"`, `whLoc="'; DROP TABLE STR_TB; --"` | `result=true`, 문자 그대로 저장. 테이블 정상 |
| TC-21 | 응답 | TC-01 응답 본문 확인 | `data.typeName`·`mngName`·`insDt` 는 **비어 있음**. 화면은 `detail.do` 재조회 필요 |
| TC-22 | 경계 | `whName` 100자 | `result=true` |
| TC-23 | 경계 | `whName` 101자 | **미검증 구간** — 아래 「확인 필요」 참고 |
| TC-24 | 참조 | `mngId=존재하지않는ID` | 현재는 `result=true`. 목록의 `mngName` 이 빈칸 |
| TC-25 | 회귀 | 본문에 `strSeq=<TC-01 의 strSeq>` + `whName=1창고` | `result=false`, `같은 이름의 창고가 있습니다.` (2026-10-15 수정됨) |

## 남은 항목

### TC-23 — 창고명·위치·메모에 길이 검사가 없다

구역명은 `WhCodeSupport.isValidZoneName()` 이 100자를 막는데, 창고명에는 대응이 없다. `STR_NAME varchar(100)`, `STR_LOC varchar(200)`, `MEMO varchar(500)` 를 넘기면 MySQL strict 모드에서 1406 으로 떨어지고, 아니면 조용히 잘린다. 둘 다 사용자에게 보이는 메시지가 없다.

DB 모드를 확인하고 기대값을 확정해야 한다.

```bash
mysql -h 172.22.0.40 -u erp_user -p erp -e "SELECT @@sql_mode;"
```

### TC-24 — `mngId` 가 실재하는지 보지 않는다

`IDX_STR_MNG` 는 인덱스일 뿐 FK 가 아니고, 서비스도 `MEMBER_TB` 를 조회하지 않는다. 관리자 필수(`ERROR_WH_MNG`)는 "빈 값 아님"까지만 보장한다. 오타가 들어가면 목록에서 관리자명이 빈칸으로 나온다.

프런트 드롭다운이 `MEMBER_TB` 에서 고르게 되어 있으면 실사용에서는 안 터지지만, API 단독으로는 막히지 않는다.

2026-10-16 로컬 서버에서 확인했다. `mngId` 에 이름 `"이준우"` 를 넣어도 등록되고, 목록 · 상세의 `mngName` 이 빈 문자열로 나온다. 운영 DB 에도 이미 `mngId="02"` · `mngName=""` 인 창고가 한 건 있다.
