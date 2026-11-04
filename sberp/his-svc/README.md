# his-svc — 로데이터 변경 이력 조회

트리거가 쌓는 이력을 한 화면에서 본다. Tomcat·ERP 와 별개인 읽기 전용 FastAPI 서비스이고, nginx `/his/` 로만 접근한다.

화면 열마다 기준이 하나다.

| 열 | 기준 | 값 |
|---|---|---|
| 종류 | 무엇이 남았나 | `저장` (`RAW_DONE_EDIT_TB`) / `값 변경` (`RAW_HIS_TB` `TB` = `RAW_TB`·`RAW_MET_TB`) |
| 대상 | 어디를 | 저장: 기본정보, CE…TEL, 사진, 첨부 로데이터 (첫 저장/재저장)<br>값 변경: 시험시작일·완료일, 시험방법 체크·규격 |
| 시점 | 언제 | `완료 후` / `완료 전` — 프로젝트완료 처리 시각(`TEST_STATE_DT`) 기준 |

- 시점 계산: 저장 행은 트리거가 완료(18) 시험만 남기므로 항상 완료 후. 값 변경 행은 시험이 완료 상태이고 완료 시각 이후에 바뀌었으면 완료 후, 아니면 완료 전.
- 그래서 완료 전 행은 모두 값 변경이다(완료 전 저장은 기록하지 않는다).
- 화면은 시점=완료 후로 열린다. 저장 기록과 그때 바뀐 값이 함께 나온다. 전체·완료 전은 시점을 바꿔서 본다.
- `RAW_HIS_TB` 의 `TB=''` 행(기존 저장 시점 기록)은 보여주지 않는다. ERP 로데이터 이력 화면용이라 트리거는 계속 남긴다.
- `RAW_HIS_TB` 의 `TB='TEST_TB'` 행(성적서 발급일 변경)은 보여주지 않는다. 발급일은 원래 완료 후에 정하는 값이라서다. 트리거(`TEST_HIS_TG`)는 계속 남긴다.
- `RAW_DONE_EDIT_TB` 가 아직 없으면(트리거 미반영) 안내만 띄우고 값 변경은 그대로 보인다.
- 트리거: `src/main/resources/db/RAW_TEST_HIS_TG.sql`, 반영 절차: `docs/docker/pro/RAW-HIS-TRIGGER.md`

## 구성

```
his-svc/
  app/main.py          화면(/), API(/api/logs, /api/options), /health
  app/db.py            조회 SQL (MariaDB 10.1, 파생 테이블 UNION)
  app/labels.py        코드값 → 표시 이름, 종류·대상·시점 정의
  app/static/index.html
  sql/his_ro_user.sql  읽기 전용 DB 계정
  deploy/docker-compose.his-svc.yml  compose 서비스 블록
  deploy/nginx-his.conf              nginx location 블록
```

API (`/his/` 아래):

| 경로 | 파라미터 |
|---|---|
| `GET api/logs` | `from`, `to`(yyyy-mm-dd, 기본 최근 30일, 최대 1년), `type`(`save`/`change`), `target`(`api/options` 의 값, 예: `save:METHOD_CE_TB`, `change:period`), `timing`(`after`/`before`, 없으면 전체 — 화면은 `after` 로 시작), `mem`(아이디 또는 이름 일부), `testNo`(SB번호 일부), `page` (50건씩) |
| `GET api/options` | 대상 선택 목록 (저장·값 변경) |
| `GET health` | DB 연결 확인 |

## 운영 반영

순서: DB 계정 → 서비스 올리기 → 비밀번호 파일 → nginx. **his-svc 가 먼저 떠 있어야** nginx 가 설정을 읽는다.

### 1. 읽기 전용 DB 계정

`sql/his_ro_user.sql` 의 `<비밀번호>` 를 바꿔 실행한다. 이력 조회에 필요한 테이블만 SELECT, `MEMBER_TB` 는 아이디·이름 컬럼만 준다.
`RAW_DONE_EDIT_TB` GRANT 는 트리거 반영 후에만 성공한다(테이블이 없으면 그 줄만 오류). 트리거를 나중에 반영하면 그때 그 줄만 다시 실행한다.

```bash
"/c/Program Files/MariaDB 10.6/bin/mysql.exe" -h 172.22.0.44 -u erp_user -p --default-character-set=utf8 erp < D:/work/STD/workspace/sberp/his-svc/sql/his_ro_user.sql
```

`erp_user` 에 계정 생성 권한(CREATE USER, GRANT OPTION)이 없으면 root 로 실행한다.

### 2. 파일 올리기 (내 PC)

```bash
scp -P <포트> -r D:/work/STD/workspace/sberp/his-svc <계정>@<운영서버>:/mnt/tc/project/
```

### 3. 운영 compose 에 서비스 추가 (운영 서버)

- `deploy/docker-compose.his-svc.yml` 의 `his-svc:` 블록을 운영 `docker-compose.yml` 의 `services:` 아래에 붙인다.
- 같은 폴더 `.env` 에 추가:

```
HIS_DB_HOST=mariadb
HIS_DB_PASSWORD=<1번의 비밀번호>
```

`HIS_DB_HOST` 는 ERP(Tomcat) 가 접속하는 DB 와 같은 곳으로 둔다(같은 compose 의 `mariadb` 면 그대로).

```bash
docker compose up -d --build his-svc
```
```bash
docker exec his-svc python -c "import urllib.request;print(urllib.request.urlopen('http://127.0.0.1:8000/health').read().decode())"
```

`"ok":true` 가 나와야 한다. `false` 면 `dbError` 에 원인(계정·비밀번호·호스트)이 나온다.

### 4. 접속 비밀번호 파일 (운영 서버)

nginx 컨테이너에 마운트된 `conf.d` 폴더(`/mnt/tc/nginx/conf.d`)에 만든다. `.conf` 가 아니라 nginx 가 설정으로 읽지 않는다.

```bash
printf "his:$(openssl passwd -apr1 '<접속 비밀번호>')\n" > /mnt/tc/nginx/conf.d/his.htpasswd
```

사람을 더 추가하려면 같은 형식의 줄을 `>>` 로 덧붙인다. (bcrypt `-B` 는 alpine nginx 가 못 읽으니 `-apr1` 을 쓴다)

### 5. nginx 설정 (운영 서버)

`deploy/nginx-his.conf` 의 두 `location` 을 `/mnt/tc/nginx/conf.d/stand500.conf` 의 `server_name www.stand500.com;` (443) 블록 안 `location / {` 앞에 붙인다.

```bash
docker exec nginx-ssl nginx -t
```
```bash
docker exec nginx-ssl nginx -s reload
```

`nginx -t` 가 실패하면 reload 하지 않는다. `host not found in upstream "his-svc"` 면 3번(컨테이너 기동·같은 네트워크)을 먼저 확인한다.

### 6. 확인

`https://www.stand500.com/his/` → 비밀번호 창 → 이력 화면. 비밀번호 없이 `api/logs` 를 열면 401 이어야 한다.

### 되돌리기

1. `stand500.conf` 에서 `/his` location 두 개를 지우고 `nginx -t` → `nginx -s reload`
2. `docker compose stop his-svc && docker compose rm -f his-svc`, compose·`.env` 에서 블록 삭제
3. DB 계정: `DROP USER 'his_ro'@'%';`

## 로컬 실행

```bash
DB_HOST=127.0.0.1 DB_PORT=3306 DB_USER=his_ro DB_PASSWORD=<비밀번호> python -m uvicorn app.main:app --port 8099
```

`http://127.0.0.1:8099/` 로 연다(인증 없음).

## 확인한 범위 (2026-11-04)

- 임시 로컬 MariaDB 10.6 에 트리거 스크립트·`his_ro` 계정을 만들고, 트리거로 쌓인 이력을 API·화면으로 조회했다(종류·대상·시점 필터와 조합, 잘못된 조건·기간 오류).
- `RAW_DONE_EDIT_TB` 권한이 없을 때 안내만 뜨고 나머지가 조회되는 것을 확인했다.
- Docker 이미지 빌드와 nginx 설정(`nginx -t`·Basic 인증)은 로컬에서 실행해 보지 못했다. 운영 반영 3·5번에서 확인한다.
