# 모바일 웹푸시 알림 — 백엔드 구현 + 프론트 작업 메모

대상: `/mErp/` 모바일 SPA (홈 화면에 추가해서 쓰는 PWA)
방식: 표준 Web Push (VAPID). Firebase·외부 SDK 없음.

---

## 1. 전체 그림

```
[모바일 PWA /mErp/]                      [Tomcat api.war]              [푸시 서비스]
  서비스워커 등록 (/mErp/sw.js)
  Notification 권한 요청
  pushManager.subscribe(공개키)  ──────►  POST /api/push/subscribe.do
                                            PUSH_SUB_TB 저장
                                                    │
  업무 이벤트 발생 ───────────────────────────────►  PushService.sendToMemberAsync()
                                            RFC 8291 암호화 + VAPID 서명
                                                    └──────────────►  FCM / Apple / Mozilla
  sw.js 의 push 이벤트 ◄──────────────────────────────────────────────────┘
  showNotification()
```

푸시 서비스는 본문을 복호화하지 못한다. 암호화 키는 브라우저의 구독 키(p256dh/auth)에서
유도되고, 서버와 해당 기기만 알고 있다.

## 2. 넘기 전에 알아야 할 제약 — 여기서 대부분 막힌다

### 2-1. 개발서버의 자체서명 SSL 에서는 알림이 아예 안 된다

푸시는 서비스워커가 필수인데, 브라우저는 **인증서 오류가 난 페이지에서 서비스워커 등록을
차단**한다. 경고창에서 "계속 진행"을 눌러 페이지가 보이더라도
`navigator.serviceWorker.register()` 는 실패한다. 코드 문제가 아니다.

테스트하려면 넷 중 하나를 골라야 한다.

| 방법 | 대상 | 비고 |
|--|--|--|
| **Chrome 포트 포워딩** | 안드로이드 | 가장 빠름. USB 연결 → PC Chrome `chrome://inspect` → Port forwarding 에 `8080 → localhost:8080` 등록 → 폰에서 `http://localhost:8080` 접속. localhost 는 HTTP 라도 보안 컨텍스트라 서비스워커가 뜬다 |
| **사설 CA 를 폰에 신뢰 설치** | 둘 다 | 안드로이드: 설정 > 보안 > 인증서 설치. iOS: 프로파일 설치 후 **설정 > 일반 > 정보 > 인증서 신뢰 설정**에서 완전 신뢰를 켜야 함(이 단계를 빠뜨리면 그대로 실패) |
| **Cloudflare Tunnel / ngrok** | 둘 다 | 개발서버에 임시 공인 도메인+정상 인증서를 붙인다. 홈 화면 추가 테스트까지 그대로 됨 |
| **운영에서만 검증** | 둘 다 | 공인 SSL 이라 아무 설정 없이 동작 |

iOS 는 포트 포워딩에 해당하는 수단이 없다. **아이폰 테스트는 사실상 2번 또는 3번이 필요하다.**

### 2-2. iOS 는 홈 화면에 추가한 앱 안에서만 알림이 된다

- iOS/iPadOS **16.4 이상**.
- Safari 로 그냥 열어 놓은 상태에서는 `Notification` 자체가 없다. 반드시 **공유 > 홈 화면에 추가**
  후 그 아이콘으로 실행해야 한다.
- 홈 화면 아이콘이 "제대로 된 PWA"가 되려면 manifest 에 `display: standalone` 이 있어야 한다.
  그냥 북마크 바로가기로 잡히면 Safari 로 열리고 푸시가 안 붙는다.
- `Notification.requestPermission()` 은 **사용자 제스처(버튼 탭) 안에서 직접** 호출해야 한다.
  클릭 핸들러 안이라도 `await` 를 먼저 걸면 제스처가 끊겨 iOS 에서 거부된다.
  → **권한 요청을 핸들러의 첫 줄에 둔다.**
- 사용자가 한 번 거부하면 앱을 삭제하고 다시 홈 화면에 추가하기 전까지 다시 물어볼 수 없다.
  안내 문구를 먼저 보여주고 나서 요청하는 흐름을 권한다.

### 2-3. 서비스워커 scope

SPA 가 `/mErp/` 에 있으므로 **`sw.js` 도 `/mErp/sw.js` 로 서빙**해야 한다.
서비스워커는 자기 경로보다 상위를 제어할 수 없다.

`sw.js` 는 캐시가 길면 배포해도 갱신이 안 된다. nginx/Tomcat 에서
`Cache-Control: no-cache` 를 주는 편이 안전하다.

---

## 3. 백엔드 — 구현 완료분

### 3-1. 추가된 파일

```
src/main/java/egovframework/psh/
  service/PushService.java              발송·구독 인터페이스
  service/PushServiceImpl.java (impl/)  발송 구현, 실패·만료 처리
  service/PushMapper.java               MyBatis 매퍼
  service/PushSub.java                  PUSH_SUB_TB VO
  service/PushDTO.java                  Subscribe / Unsubscribe / Config / Message
  util/WebPushCrypto.java               RFC 8291 + RFC 8188 페이로드 암호화
  util/VapidTokenFactory.java           RFC 8292 ES256 JWT
  util/VapidKeyGenerator.java           VAPID 키쌍 생성기 (main)
  web/PushController.java               /push/*.do

src/main/resources/
  db/PUSH_SUB_TB.sql                                   테이블 DDL
  egovframework/sqlmap/mappers/psh/pushMapper.xml      SQL
  egovframework/sqlmap/sql-mapper-config.xml           typeAlias pushSub 추가
  egovframework/spring/context-properties.xml          Globals.push.vapid.* 추가
```

**의존성 추가 없음.** 암호화·서명은 Java 8 표준 JCE(SunEC, AES-GCM, HmacSHA256)로 직접
구현했다. eGov 3.10 / Spring 4.3 스택에 BouncyCastle·Netty 를 새로 끌어오지 않기 위함이다.

### 3-2. API

모두 `.do` 라 AuthInterceptor 가 세션 로그인을 요구한다. 응답은 공통 `BasicResponse`.

#### `GET /api/push/config.do`

```json
{
  "result": true,
  "data": { "configured": true, "publicKey": "BBGUhFOM...", "subscribed": false }
}
```

- `configured` — 서버에 VAPID 키가 설정됐는지. `false` 면 프론트는 알림 UI 를 숨긴다.
- `publicKey` — `pushManager.subscribe()` 의 `applicationServerKey`. 공개해도 되는 값이다.
- `subscribed` — 이 계정에 살아 있는 구독이 하나라도 있는지.

#### `POST /api/push/subscribe.do`

요청 본문은 **브라우저의 `PushSubscription` 을 그대로** 보내면 된다. 가공 불필요.

```json
{
  "endpoint": "https://web.push.apple.com/QK...",
  "keys": { "p256dh": "BEl...", "auth": "tBH..." }
}
```

같은 기기가 다시 보내면 새 행을 만들지 않고 갱신한다(재구독·재로그인 안전).

#### `POST /api/push/unsubscribe.do`

```json
{ "endpoint": "https://web.push.apple.com/QK..." }
```

#### `POST /api/push/test.do`

로그인한 본인의 모든 기기로 테스트 알림을 보낸다. `data` 는 성공한 기기 수.
**프론트 연동 확인은 이 엔드포인트 하나로 끝난다.**

### 3-3. 업무 이벤트에 붙이는 법 (백엔드)

```java
@Resource(name = "PushService")
private PushService pushService;

// ...업무 처리 후
PushDTO.Message msg = new PushDTO.Message();
msg.setTitle("제품사진 업로드 요청");
msg.setBody(sbkId + " 신청서의 제품사진을 등록해 주세요.");
msg.setUrl("/mErp/#/sbk/" + sbkId + "/photo");   // 알림 클릭 시 열 딥링크
msg.setTag("sbk-" + sbkId);                       // 같은 tag 는 덮어써서 알림이 쌓이지 않는다
pushService.sendToMemberAsync(mngId, msg);        // 별도 스레드. 트랜잭션을 붙잡지 않는다
```

`sendToMemberAsync` 는 실패해도 예외를 던지지 않는다(로그만 남는다).
**알림 실패가 업무 처리를 되돌리면 안 되기 때문이다.** 발송 결과가 필요하면 동기
`sendToMember` 가 성공 기기 수를 돌려준다.

수신자를 특정하지 않는 알림(공지 등)은 `sendToAll(message, 제외할사용자)` 을 쓴다.
구독 중인 전 사용자에게 나가며, 두 번째 인자로 작성자 본인을 빼면 자기 글 알림을 받지 않는다.

### 3-4. 연결된 이벤트

| 이벤트 | 위치 | 수신자 | 알림 |
|--|--|--|--|
| 공지사항 신규 등록 | `BbsController.insertBoardArticle` → `notifyNewNotice` | 구독 중인 전 사용자 (작성자 제외) | 제목 "새 공지사항", 본문 = 게시글 제목(60자 초과 시 생략) |

공지는 **신규 등록일 때만** 보낸다(`nttId == 0`). 수정은 알리지 않는다.
저장이 성공한 뒤에 호출하므로 롤백된 글로 알림이 나가지 않는다.

알림 클릭 시 URL 은 현재 `/mErp/`(앱 첫 화면)이다. 모바일 SPA 에 공지 상세 라우트가
생기면 `notifyNewNotice` 의 `message.setUrl(...)` 을 딥링크로 바꾸면 된다.

### 3-5. 실패 처리

| 응답 | 처리 |
|--|--|
| 2xx | `LAST_SENT_DT` 갱신, `FAIL_CNT` 0 |
| 404 / 410 | 구독 소멸. 즉시 `STATE='D'`, 재시도 안 함 |
| 401 / 403 | VAPID 키·subject 설정 오류. 구독은 살려 두고 ERROR 로그 |
| 그 외 | `FAIL_CNT` +1. 10회 연속 실패하면 자동으로 접음 |

---

## 4. 프론트 작업 — 담당자 전달분

### 4-1. `/mErp/manifest.webmanifest`

`target/manifest.webmanifest` 에 초안이 있다. 두 가지만 손보면 된다.
`lang` 을 `ko` 로, 그리고 `logo_512.png` 가 실제로 있는지 확인.

```json
{
  "name": "스탠다드뱅크",
  "short_name": "스탠다드",
  "start_url": "/mErp/",
  "scope": "/mErp/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#000000",
  "lang": "ko",
  "icons": [
    { "src": "icons/logo_180.png", "sizes": "180x180", "type": "image/png", "purpose": "any" },
    { "src": "icons/logo_180.png", "sizes": "180x180", "type": "image/png", "purpose": "maskable" },
    { "src": "icons/logo_512.png", "sizes": "512x512", "type": "image/png", "purpose": "any" }
  ]
}
```

### 4-2. `/mErp/index.html` `<head>`

iOS 는 manifest 만으로는 부족해서 apple 메타태그가 같이 있어야 홈 화면 앱으로 잡힌다.

```html
<link rel="manifest" href="/mErp/manifest.webmanifest">
<meta name="theme-color" content="#000000">
<meta name="apple-mobile-web-app-capable" content="yes">
<meta name="apple-mobile-web-app-status-bar-style" content="default">
<meta name="apple-mobile-web-app-title" content="스탠다드">
<link rel="apple-touch-icon" href="/mErp/icons/logo_180.png">
```

### 4-3. `/mErp/sw.js`

```js
// 서버가 보낸 JSON: { title, body, url, tag }
self.addEventListener('push', function (event) {
  var data = {};
  try {
    data = event.data ? event.data.json() : {};
  } catch (e) {
    data = { title: 'ERP 알림', body: event.data ? event.data.text() : '' };
  }

  event.waitUntil(
    self.registration.showNotification(data.title || 'ERP 알림', {
      body: data.body || '',
      icon: '/mErp/icons/logo_180.png',
      badge: '/mErp/icons/badge.png',
      tag: data.tag || undefined,
      renotify: !!data.tag,
      data: { url: data.url || '/mErp/' }
    })
  );
});

self.addEventListener('notificationclick', function (event) {
  event.notification.close();
  var target = (event.notification.data && event.notification.data.url) || '/mErp/';

  event.waitUntil(
    clients.matchAll({ type: 'window', includeUncontrolled: true }).then(function (list) {
      for (var i = 0; i < list.length; i++) {
        if (list[i].url.indexOf('/mErp/') !== -1) {
          // 이미 열려 있으면 새 창 대신 그 창을 딥링크로 이동시킨다
          return list[i].focus().then(function (c) {
            return c && c.navigate ? c.navigate(target) : null;
          });
        }
      }
      return clients.openWindow(target);
    })
  );
});

// 브라우저가 구독을 갱신하면(만료·키 교체) 새 구독을 서버에 다시 등록한다.
// 세션 쿠키가 살아 있어야 성공한다. 실패해도 다음 로그인 때 enablePush 가 복구한다.
self.addEventListener('pushsubscriptionchange', function (event) {
  event.waitUntil(
    fetch('/api/push/config.do', { credentials: 'include' })
      .then(function (r) { return r.json(); })
      .then(function (res) {
        return self.registration.pushManager.subscribe({
          userVisibleOnly: true,
          applicationServerKey: urlBase64ToUint8Array(res.data.publicKey)
        });
      })
      .then(function (sub) {
        return fetch('/api/push/subscribe.do', {
          method: 'POST',
          credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(sub)
        });
      })
      .catch(function () { /* 다음 진입 때 복구 */ })
  );
});

function urlBase64ToUint8Array(base64String) {
  var padding = '='.repeat((4 - (base64String.length % 4)) % 4);
  var base64 = (base64String + padding).replace(/-/g, '+').replace(/_/g, '/');
  var raw = atob(base64);
  var out = new Uint8Array(raw.length);
  for (var i = 0; i < raw.length; i++) { out[i] = raw.charCodeAt(i); }
  return out;
}
```

### 4-4. 알림 켜기 / 끄기

```js
var API = '/api';

function urlBase64ToUint8Array(base64String) {
  var padding = '='.repeat((4 - (base64String.length % 4)) % 4);
  var base64 = (base64String + padding).replace(/-/g, '+').replace(/_/g, '/');
  var raw = atob(base64);
  var out = new Uint8Array(raw.length);
  for (var i = 0; i < raw.length; i++) { out[i] = raw.charCodeAt(i); }
  return out;
}

/** 이 환경에서 푸시가 가능한지. UI 를 그릴지 말지 판단용. */
function pushAvailable() {
  if (!('serviceWorker' in navigator) || !('PushManager' in window)) return false;
  // iOS 는 홈 화면에 추가한 상태에서만 가능하다
  var isIOS = /iPad|iPhone|iPod/.test(navigator.userAgent);
  var standalone = window.navigator.standalone === true ||
                   window.matchMedia('(display-mode: standalone)').matches;
  if (isIOS && !standalone) return false;
  return true;
}

async function enablePush() {
  // ★ iOS: 권한 요청이 반드시 클릭 핸들러의 첫 동작이어야 한다.
  //    앞에 await 를 하나라도 걸면 사용자 제스처가 끊겨 거부된다.
  var perm = await Notification.requestPermission();
  if (perm !== 'granted') {
    throw new Error('알림 권한이 거부되었습니다. 기기 설정에서 허용해 주세요.');
  }

  var cfgRes = await fetch(API + '/push/config.do', { credentials: 'include' });
  var cfg = await cfgRes.json();
  if (!cfg.result || !cfg.data.configured) {
    throw new Error('서버에 알림이 설정되어 있지 않습니다.');
  }

  var reg = await navigator.serviceWorker.register('/mErp/sw.js', { scope: '/mErp/' });
  await navigator.serviceWorker.ready;

  var sub = await reg.pushManager.getSubscription();
  if (!sub) {
    sub = await reg.pushManager.subscribe({
      userVisibleOnly: true,                                  // 필수. false 는 거부된다
      applicationServerKey: urlBase64ToUint8Array(cfg.data.publicKey)
    });
  }

  var res = await fetch(API + '/push/subscribe.do', {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(sub)                                 // 그대로 보낸다
  });
  var body = await res.json();
  if (!body.result) throw new Error(body.message);
}

async function disablePush() {
  var reg = await navigator.serviceWorker.getRegistration('/mErp/');
  if (!reg) return;
  var sub = await reg.pushManager.getSubscription();
  if (!sub) return;

  await fetch(API + '/push/unsubscribe.do', {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ endpoint: sub.endpoint })
  });
  await sub.unsubscribe();
}
```

### 4-5. UI 흐름 권장안

```
설정 화면 진입
  ├ pushAvailable() === false
  │    ├ iOS + 홈화면 아님  → "공유 > 홈 화면에 추가 후 그 아이콘으로 열어 주세요" 안내
  │    └ 그 외              → 알림 항목 자체를 숨김
  └ pushAvailable() === true
       GET /push/config.do
        ├ configured === false      → 항목 숨김 (서버 미설정)
        ├ subscribed === true       → 토글 ON,  끄면 disablePush()
        └ subscribed === false      → 토글 OFF, 켜면 enablePush()
```

권한을 한 번 거부하면 되돌리기 어려우므로, 토글을 켤 때 **왜 알림이 필요한지 한 줄 안내를
먼저 보여주고** 확인을 누르면 그 클릭에서 `enablePush()` 를 호출하는 편이 좋다.

### 4-6. 연동 확인

`POST /api/push/test.do` 를 호출하면 본인 기기로 테스트 알림이 온다.
응답 `data` 가 발송에 성공한 기기 수다. `0` 이면 구독이 등록되지 않은 것이다.

---

## 5. 배포 체크리스트

### 개발(로컬 Eclipse)

1. `src/main/resources/db/PUSH_SUB_TB.sql` 을 개발 DB 에 적용
2. `erp-local.properties` 에 VAPID 키 3줄 — **이미 넣어 뒀다**
   (`GLOBALS_PUSH_VAPID_PUBLIC` / `PRIVATE` / `SUBJECT`. 이 파일은 `.gitignore` 대상)
3. Tomcat 재기동 → `GET /api/push/config.do` 가 `configured: true` 인지 확인

### 운영

1. `PUSH_SUB_TB.sql` 운영 DB 적용
2. **운영용 키를 새로 생성**한다 — Eclipse 에서 `egovframework.psh.util.VapidKeyGenerator` 실행
3. 출력 2줄을 `sberp/.env` 의 `GLOBALS_PUSH_VAPID_PUBLIC` / `GLOBALS_PUSH_VAPID_PRIVATE` 에 기입
   (자리는 만들어 뒀다. 이 파일도 `.gitignore` 대상)
4. `docker compose up -d --force-recreate erp` 로 환경변수 반영 + `api.war` 재배포
5. 프론트 `/mErp/` 에 manifest·sw.js 배포
6. 실기기에서 홈 화면에 추가 → 알림 켜기 → `POST /api/push/test.do`

### 키를 바꿀 때

VAPID 키를 교체하면 **기존 구독이 전부 무효**가 된다(푸시 서비스가 401 을 준다).
`PUSH_SUB_TB` 를 비우고 사용자에게 재구독을 받아야 한다.

---

## 6. 아직 안 한 것

- **알림함(인앱 히스토리)** — 지금은 발송만 하고 내용을 남기지 않는다. "지난 알림 보기"가
  필요하면 `PUSH_LOG_TB` 같은 테이블과 목록 API 가 따로 있어야 한다.
- **알림 종류별 수신 설정** — 지금은 전부 받거나 전부 안 받거나다. 항목별 on/off 가
  필요하면 구독 테이블에 종류 컬럼을 붙이는 방향.
- **공지 외 업무 이벤트** — 지금 연결된 건 공지사항 신규 등록 하나뿐이다(3-4). 제품사진
  업로드 요청 등 나머지는 3-3 방식으로 붙이면 되지만, 어떤 이벤트에서 누구에게 보낼지는
  아직 정하지 않았다.
- **공지 상세 딥링크** — 모바일에 공지 상세 라우트가 없어 알림 클릭 시 앱 첫 화면으로 간다.
