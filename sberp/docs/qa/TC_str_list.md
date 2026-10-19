# 창고 목록 `/cnf/str/list.do` 테스트케이스

- 대상: `GET /cnf/str/list.do` (`StrController.list` → `StrServiceImpl.selectList`)
- 파라미터: `searchWord`(선택), `includeStopped`(선택)
- 응답: `Warehouse[]`

## 동작 순서

```
인증 확인
 → includeStopped 가 "Y"(대소문자 무시) 가 아니면 STATE != 'D' 만
 → searchWord 가 있으면 창고명 · 관리자명 · 위치를 LIKE 로 OR 검색
 → STR_SEQ 순으로 정렬
```

행마다 집계 세 개가 함께 온다.

| 필드 | 뜻 |
|---|---|
| `zoneCnt` | 사용 중인 구역 수 |
| `stopZoneCnt` | 사용 중지된 구역 수 |
| `itemCnt` | 사용 중인 구역에 놓인 미반출 시료 수 |

## 테스트케이스

| TC | 구분 | 입력·조건 | 기대 결과 |
|---|---|---|---|
| TC-01 | 정상 | 파라미터 없이 | `result=true`, 사용 중인 창고만 |
| TC-02 | 정상·정렬 | 창고 여러 건 | `strSeq` 오름차순 |
| TC-03 | 정상·필드 | 응답 한 건 | `whName`·`whLoc`·`typeCode`·`typeName`·`mngId`·`mngName`·`mngDeptName`·`deptSeq`·`deptName`·`state`·`insDt`·`udtDt`·`udtMemName` |
| TC-04 | 정상·집계 | 구역 3개(1개 중지)·시료 2건인 창고 | `zoneCnt=2`, `stopZoneCnt=1`, `itemCnt=2` |
| TC-05 | 정상·집계 | 구역 0개인 창고 | `zoneCnt=0`, `stopZoneCnt=0`, `itemCnt=0` |
| TC-06 | 중지 | 사용 중지된 창고 | 기본 목록에서 빠진다 |
| TC-07 | 중지 | `includeStopped=Y` | 중지된 창고도 나오고 `state='D'` |
| TC-08 | 중지 | `includeStopped=y` (소문자) | `Y` 와 같게 동작 |
| TC-09 | 중지 | `includeStopped=N` · 빈 값 · 다른 값 | 중지된 창고는 안 나온다 |
| TC-10 | 검색 | `searchWord=2창고` | 창고명 부분일치 |
| TC-11 | 검색 | `searchWord=별관` | 위치 부분일치 |
| TC-12 | 검색 | `searchWord=<관리자 이름>` | 관리자명 부분일치 |
| TC-13 | 검색 | `searchWord=<담당 부서명>` | **안 잡힌다** — 아래 L1 |
| TC-14 | 검색 | `searchWord=` (빈 문자열) | 조건 없음과 같다 |
| TC-15 | 검색 | 결과가 없는 검색어 | `result=true`, `data=[]` |
| TC-16 | 검색 | `searchWord` + `includeStopped=Y` | 두 조건이 함께 걸린다 |
| TC-17 | 검색 | `searchWord=%` | **전건이 나온다** — 아래 L2 |
| TC-18 | 보안 | `searchWord=' OR 1=1 --` | 문자 그대로 검색. 결과 없음 |
| TC-19 | 권한 | 비로그인 | `result=false`, `권한이 없습니다` |
| TC-20 | 권한 | 읽기권한 없는 계정 | `result=false`, `권한이 없습니다` |

TC-02·04~20 은 DB·HTTP 계층이라 Swagger 수동 확인이 필요하다.

## 남은 항목

### L1 — 담당 부서로는 검색되지 않는다

검색은 창고명 · 관리자명 · 위치 세 가지만 본다.

```sql
AND ( STR_NAME LIKE ... OR IFNULL(FN_MEM_NAME(MNG_ID),'') LIKE ... OR IFNULL(STR_LOC,'') LIKE ... )
```

2026-10-16 에 담당 부서(`DEPT_SEQ`)를 더했지만 검색 대상에는 넣지 않았다. 관리자 없이 부서만 지정한 창고는 부서명으로 찾을 수 없다. `DEPT_TB` 를 조인해 `NAME` 을 조건에 더하면 된다.

### L2 — 검색어의 LIKE 와일드카드를 막지 않는다

`LIKE CONCAT('%', #{searchWord}, '%')` 로 파라미터 바인딩을 쓰므로 SQL 주입은 막힌다. 다만 값 안의 `%` 와 `_` 는 LIKE 메타문자로 그대로 동작한다. `%` 한 글자를 보내면 전건이 나오고, `_` 는 아무 한 글자와 맞는다.

창고 목록 규모에서는 실질 피해가 없다. 정확히 하려면 `ESCAPE` 절을 쓰거나 입력에서 두 글자를 치환해야 한다.

### L3 — 페이징이 없다

전체를 한 번에 내려준다. 다른 목록 API 는 `ComParam` 으로 페이징하는데 여기는 없다. 창고가 수십 건 수준이면 문제가 없지만, 창고 수에 상한이 없으므로(`zone/insert.do` Z3 · `insertRange.do` R4 와 같은 뿌리) 언젠가 걸린다.

행마다 집계 서브쿼리가 세 개 돌아서 비용도 창고 수에 비례한다.
