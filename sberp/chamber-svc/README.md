# chamber-svc

챔버 현황판. 현황·이력 조회, 로데이터 외 사용(점검·디버깅·입회·예비시험) 기록, 온습도 센서 수신을 맡는다.
화면(html·js)을 이 서비스가 직접 내려 주므로 프론트엔드 프로젝트가 따로 없다.

- 설계: `docs/design/chamber-svc.md` (ERP 쪽은 `chamber-erp.md`)
- 구조는 his-svc 와 같다 (FastAPI + pymysql, 정적 화면, nginx 뒤에서만 접근)

## 원칙

| 항목 | 내용 |
|---|---|
| DB | `erp` 스키마는 읽기만, `ops` 스키마에만 쓴다. 전용 계정 `chamber_svc` |
| 측정 행 | 보여 주기만 한다. 기록·종료는 ERP 가 한다 |
| 사용 중 확인 | [사용] 때 구역 행(`erp.CHAMBER_ZONE_TB`)을 잠근 뒤 확인한다. ERP 의 측정 [시작]도 같은 행을 잠근다 |
| 로그인 | ERP 계정(`MEMBER_TB`). 세션은 서명된 쿠키 (1시간, 모니터 계정 7일·조회 전용) |
| 갱신 | 화면이 30초마다 다시 조회한다 |

## 폴더

```
app/
  main.py      라우팅, 응답 모양
  config.py    환경변수 (비밀번호·서명 키가 비면 기동 실패), 용도 구분값
  auth.py      로그인, 세션 쿠키
  db.py        조회·기록 쿼리
  static/      index.html(현황) history.html(이력) login.html app.js app.css
deploy/        compose 블록, nginx location
sql/           ops_schema.sql, chamber_svc_user.sql, dev_seed.sql(개발용 시험 데이터)
```

## 환경변수

| 이름 | 기본값 | 설명 |
|---|---|---|
| `DB_HOST`, `DB_PORT`, `DB_USER` | `127.0.0.1`, `3306`, `chamber_svc` | DB 접속 |
| `DB_PASSWORD` | (필수) | 비어 있으면 기동 실패 |
| `SESSION_SECRET` | (필수) | 쿠키 서명 키. 32자 이상. `openssl rand -hex 32` |
| `MONITOR_MEM_ID` | 없음 | 모니터 전용 계정 아이디 |
| `STALE_HOURS` | `12` | 이 시간을 넘긴 사용 건을 강조 |
| `COOKIE_PATH` | `/chamber/` | 로컬에서 직접 띄울 때는 `/` |
| `COOKIE_SECURE` | `true` | 로컬(http)에서는 `false` |

## DB 준비 (순서대로)

1. ERP 의 챔버 테이블과 `RAW_ENV_TB.ZONE_SEQ` 가 적용돼 있어야 한다 (`chamber-erp.md` §3)
2. `sql/ops_schema.sql` — ERP 가 도는 **모든 DB**에 실행한다
3. `sql/chamber_svc_user.sql` — `<비밀번호>` 를 바꿔서 실행한다

로컬·개발 DB 에서 화면을 바로 보려면 2번 뒤에 `sql/dev_seed.sql` 을 실행한다. 시험용 챔버·구역(번호 9001~)과 사용 기록을 넣고, ERP 챔버 테이블이 아직 없으면 같은 정의로 만든다. **운영 DB 에서는 실행하지 않는다.**

## 로컬 실행

`chamber-svc/.env` (커밋하지 않는다):

```
DB_HOST=<DB 주소>
DB_PASSWORD=<비밀번호>
SESSION_SECRET=<32자 이상>
COOKIE_PATH=/
COOKIE_SECURE=false
```

```bash
cd chamber-svc
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8010
```

http://localhost:8010/ 로 연다.

## 서버 배포

1. 이 폴더를 서버의 `/mnt/tc/project/chamber-svc` (개발서버는 `/docker-project/chamber-svc`) 에 둔다
2. `deploy/docker-compose.chamber-svc.yml` 블록을 서버 compose 의 `services:` 아래에 붙인다 (`context` 경로 확인)
3. compose `.env` 에 `CHAMBER_DB_PASSWORD`, `CHAMBER_SESSION_SECRET`, `CHAMBER_MONITOR_MEM_ID` 를 넣는다
4. `docker compose up -d --build chamber-svc`
5. `docker compose exec chamber-svc python -c "import urllib.request;print(urllib.request.urlopen('http://localhost:8000/health').read().decode())"` — `"ok":true` 확인
6. `deploy/nginx-chamber.conf` 를 nginx 설정에 붙이고 `docker compose exec nginx nginx -t` 후 `docker compose exec nginx nginx -s reload`

## API

| 메서드·URL | 설명 | 권한 |
|---|---|---|
| `POST /api/login`, `POST /api/logout` | 로그인·로그아웃 | - |
| `GET /api/me` | 로그인 사용자, 용도 구분값 | 로그인 |
| `GET /api/status` | 챔버 > 구역 > 사용 건 (`src`: `RAW` 측정 행 / `ETC` 로데이터 외 사용) | 로그인 |
| `GET /api/history?from=&to=&chamberSeq=&src=` | 종료된 기록 (종료일 기준), 사용 시간 합계 | 로그인 |
| `GET /api/chambers` | 이력 화면의 챔버 선택 목록 | 로그인 |
| `GET /api/zones` | [사용] 때 고를 구역 목록 | 모니터 계정 제외 |
| `POST /api/etc-use/start` | 로데이터 외 [사용]. 사용 중이면 409 | 모니터 계정 제외 |
| `POST /api/etc-use/{seq}/end` | [종료] | 기록한 사람 또는 기술책임자 |
| `POST /api/etc-use/{seq}/delete` | 삭제 (사유 필수, `DEL_YN=1`) | 기록한 사람 또는 기술책임자 |
| `GET /api/dht/latest` | 최근 온습도 | 로그인 |
| `POST /api/dht` | ESP32 수신 `{temperature, humidity}` | 로그인 없음 (nginx 에서 IP 제한) |
| `GET /health` | DB 연결 확인 | - |
