# 모바일 웹푸시 알림

대상: `/mErp/` 모바일 SPA (홈 화면에 추가해서 쓰는 PWA)
방식: 표준 Web Push (VAPID). Firebase·외부 SDK 없음. **의존성 추가 없음.**

**상태 (2026-09-15)** — 개발서버에서 전 구간 동작 확인 완료.
남은 일은 **4-1 원본 프로젝트 반영** 하나다. 프론트 담당자는 4장부터 보면 된다.

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

### 2-1. 반드시 도메인으로 접속해야 한다 (IP 접속 불가)

푸시는 서비스워커가 필수인데, 브라우저는 **인증서가 검증되지 않은 페이지에서 서비스워커
등록을 차단**한다. 경고창에서 "계속 진행"을 눌러 화면이 보이더라도
`navigator.serviceWorker.register()` 는 실패한다. 코드 문제가 아니다.

**개발서버는 2026-09-15 자로 정식 인증서로 전환했다.**

| 접속 주소 | 서비스워커 | 비고 |
|--|--|--|
| `https://dev.stand500.com` | ✅ | Let's Encrypt. **이걸 쓴다** |
| `https://172.22.0.40` | ❌ | 인증서 이름 불일치 → 경고 → 차단 |
| 운영 `https://stand500.com` | ✅ | 공인 SSL |

IP 로 접속하면 인증서가 `dev.stand500.com` 전용이라 이름이 맞지 않아 그대로 막힌다.
기존 북마크나 하드코딩된 IP 가 있으면 도메인으로 바꿔야 한다.

전환 절차와 90일 갱신 방법은 `docs/docker/dev/SSL-NGINX-DEV.md` 에 있다.

> 개발서버와 운영서버는 같은 ERP 화면을 서비스해서 눈으로 구분되지 않는다.
> 주소창 자물쇠 → 인증서 → 발급 대상이 `dev.stand500.com` 인지 확인한다.

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

## 4. 프론트 — 개발서버 적용 완료 (2026-09-15)

개발서버 `/mErp/` 에 아래 4개 파일을 적용해 **동작 확인까지 끝냈다.**
구독 등록 → 테스트 발송 → 공지 등록 알림까지 전 구간 검증했다.

| 파일 | 구분 | 내용 |
|--|--|--|
| `push-sw.js` | **신규** | 푸시 수신 핸들러 3개 |
| `sw.js` | 수정 2곳 | `importScripts` 1줄 + precache revision 2개 |
| `index.html` | 수정 2곳 | `lang="ko"` + 알림 설정 패널(임시) |
| `manifest.webmanifest` | 수정 1곳 | `lang: en` → `ko` |

> ### ⚠️ 지금 적용된 건 빌드 산출물이다
>
> `/mErp/` 는 `vite-plugin-pwa` 의 **generateSW** 모드 빌드 결과다.
> 원본 프로젝트에 반영하지 않으면 **다음 빌드에 `sw.js` 와 `index.html` 이 덮어써지고
> 전부 사라진다.** 4-1 을 먼저 처리할 것.

### 4-1. 원본 프로젝트 반영 — 이것만 하면 된다

**(1) `public/push-sw.js`** — 4-2 의 파일을 그대로 둔다.

**(2) `vite.config`** — VitePWA 설정에 한 줄.

```js
VitePWA({
  workbox: {
    importScripts: ['push-sw.js']      // ← 추가
  },
  manifest: {
    lang: 'ko',                        // ← en 이었다
    // manifestFilename: 'manifest.json'   ← 4-6 참고
  }
})
```

이러면 빌드 결과가 **지금 개발서버 구조와 똑같아진다.** precache revision 은 빌드가
알아서 계산하므로 손댈 필요가 없다.

**(3) 알림 UI** — 4-3 의 모듈을 두고, 설정 화면에서 호출한다.

**(4) `index.html` 의 임시 블록 제거** — 4-5 참고.

### 4-2. `public/push-sw.js`

```js
/** 서버(PushDTO.Message)가 보내는 JSON: { title, body, url, tag } */
self.addEventListener('push', function (event) {
  var data = {};
  try {
    data = event.data ? event.data.json() : {};
  } catch (e) {
    data = { title: 'ERP 알림', body: event.data ? event.data.text() : '' };
  }

  // 알림을 띄우지 않고 push 를 받으면 브라우저가 구독을 끊는다(userVisibleOnly 규칙).
  // 그래서 내용이 비어도 반드시 showNotification 을 호출한다.
  event.waitUntil(
    self.registration.showNotification(data.title || 'ERP 알림', {
      body: data.body || '',
      icon: '/mErp/icons/logo_192.png',
      badge: '/mErp/icons/logo_192.png',
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
        // 이미 앱이 열려 있으면 새 창을 띄우지 않고 그 창을 재활용한다.
        if (list[i].url.indexOf('/mErp/') !== -1) {
          return list[i].focus().then(function (client) {
            return client && client.navigate ? client.navigate(target) : null;
          });
        }
      }
      return clients.openWindow(target);
    })
  );
});

/**
 * 브라우저가 구독을 갱신하면(만료·키 교체) 이 이벤트가 온다.
 * 새 구독을 서버에 다시 등록하지 않으면 그 기기로는 알림이 영영 오지 않는다.
 */
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
      .catch(function () { /* 다음 진입 때 복구된다 */ })
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

### 4-3. 알림 켜기 / 끄기 모듈

개발서버에는 `index.html` 안에 임시로 넣어 뒀다. 원본에는 별도 모듈로 두는 것을 권한다.

```js
// src/lib/push.js
const API = '/api';
const SCOPE = '/mErp/';
const SW = '/mErp/sw.js';

function toUint8(base64) {
  const padding = '='.repeat((4 - (base64.length % 4)) % 4);
  const s = (base64 + padding).replace(/-/g, '+').replace(/_/g, '/');
  const raw = atob(s);
  const out = new Uint8Array(raw.length);
  for (let i = 0; i < raw.length; i++) out[i] = raw.charCodeAt(i);
  return out;
}

/** 이 환경에서 푸시가 가능한지. 불가능하면 사유 문자열, 가능하면 null */
export function pushBlockedReason() {
  if (!('serviceWorker' in navigator) || !('PushManager' in window)
      || !('Notification' in window)) {
    return '이 브라우저는 알림을 지원하지 않습니다';
  }
  if (!window.isSecureContext) {
    // IP 접속·인증서 오류. dev.stand500.com 으로 들어와야 한다(2-1 참고)
    return 'HTTPS 인증서 문제로 알림을 쓸 수 없습니다';
  }
  const isIOS = /iPad|iPhone|iPod/.test(navigator.userAgent);
  const standalone = window.navigator.standalone === true
    || window.matchMedia('(display-mode: standalone)').matches;
  if (isIOS && !standalone) {
    return '공유 > 홈 화면에 추가 후 그 아이콘으로 열어주세요';
  }
  if (Notification.permission === 'denied') {
    return '알림이 차단되어 있습니다. 주소창 자물쇠 > 알림 에서 초기화하세요';
  }
  return null;
}

/**
 * { loggedIn, configured, publicKey, subscribed }
 *
 * ★ subscribed 는 **계정 단위**다. 폰 하나만 등록돼 있어도 true 가 된다.
 *   토글의 on/off 를 이 값으로 정하면 안 된다 — 폰에서 켠 사용자가 PC 로
 *   들어왔을 때 "끄기" 버튼이 나와 PC 에서 켤 방법이 없어진다.
 *   기기 상태는 isDeviceSubscribed() 로 판단한다.
 */
export async function getPushConfig() {
  const res = await fetch(`${API}/push/config.do`, { credentials: 'include' });
  const body = await res.json();
  return { loggedIn: !!body.result, ...(body.data || {}) };
}

/** 지금 이 기기(브라우저)가 구독 중인지. 토글 상태는 이 값으로 정한다. */
export async function isDeviceSubscribed() {
  if (!('serviceWorker' in navigator)) return false;
  const reg = await navigator.serviceWorker.getRegistration(SCOPE);
  return !!(reg && (await reg.pushManager.getSubscription()));
}

/**
 * @param publicKey getPushConfig() 로 미리 받아 둔 VAPID 공개키
 *
 * ★ 이 함수의 첫 줄이 반드시 requestPermission 이어야 한다.
 *   앞에 await 가 하나라도 있으면 iOS 에서 사용자 제스처가 끊겨 거부된다.
 *   그래서 config 는 버튼을 누르기 전에 받아 둔다.
 */
export async function enablePush(publicKey) {
  const permission = await Notification.requestPermission();
  if (permission !== 'granted') throw new Error('알림 권한이 거부되었습니다.');

  const reg = await navigator.serviceWorker.register(SW, { scope: SCOPE });
  await navigator.serviceWorker.ready;

  let sub = await reg.pushManager.getSubscription();
  if (!sub) {
    sub = await reg.pushManager.subscribe({
      userVisibleOnly: true,                      // 필수. false 는 거부된다
      applicationServerKey: toUint8(publicKey)
    });
  }

  const res = await fetch(`${API}/push/subscribe.do`, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(sub)                     // 가공 없이 그대로
  });
  const body = await res.json();
  if (!body.result) throw new Error(body.message || '구독 등록 실패');
}

export async function disablePush() {
  const reg = await navigator.serviceWorker.getRegistration(SCOPE);
  const sub = reg && (await reg.pushManager.getSubscription());
  if (!sub) return;

  // 서버에서 먼저 지운다. 브라우저에서 먼저 끊으면 endpoint 를 잃어 서버에 찌꺼기가 남는다.
  await fetch(`${API}/push/unsubscribe.do`, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ endpoint: sub.endpoint })
  });
  await sub.unsubscribe();
}
```

### 4-4. `sw.js` 는 직접 고치지 않는다

`generateSW` 모드라 빌드할 때마다 새로 생성된다. 개발서버에 적용한 두 가지는
**손으로 넣은 임시 조치**이며, 원본에서는 4-1 (2) 로 대체된다.

```js
importScripts("./push-sw.js");     // 맨 앞에 추가한 줄
```

```
precache revision 갱신
  index.html            c3a14f84 → b97b71a0
  manifest.webmanifest  3a3e6731 → 931e6d58
```

revision 을 갱신하지 않으면 **이미 앱을 설치한 기기에서 수정이 보이지 않는다.**
workbox 는 revision 이 같으면 캐시된 옛 파일을 계속 내보내고, `index.html` 은
NavigationRoute 가 캐시에서 꺼내 쓰기 때문이다. 빌드하면 자동 계산되므로
원본 반영 후에는 신경 쓸 필요가 없다.

### 4-5. `index.html` 임시 블록

개발서버 `index.html` 에는 진단용 패널이 들어 있다. 아래 주석 사이 전체가 대상이다.

```html
<!-- ===== 임시: 웹푸시 알림 설정 패널 ===== -->
   ... (style + markup + script, 약 290줄)
<!-- ===== 임시 블록 끝 ===== -->
```

리액트 번들 밖에서 도는 독립 스크립트로, **배관 검증용이다.** 원본에 옮길 것은
4-3 의 두 함수뿐이고 나머지(체크리스트 UI)는 옮기지 않아도 된다.

개발 중에는 이 패널이 유용하다. 어느 단계에서 막혔는지 항목별로 보여준다.

```
✅ 보안 연결(HTTPS)          ← IP 로 접속하면 ❌
✅ 브라우저 지원
✅ 홈 화면 앱으로 실행        ← iOS 에서만 표시
✅ 로그인                    ← SPA 라 로그인 후 "다시 확인" 필요
✅ 서버 알림 설정(VAPID)
✅ 알림 권한 — 허용됨
✅ 이 기기 알림              ← 토글이 따르는 값 (4-7)
✅ 계정에 등록된 기기         ← 참고. 다른 기기 포함
```

`lang="en"` → `"ko"` 도 같이 고쳤다.

### 4-6. manifest — 확인 필요

`/mErp/manifest.webmanifest` 의 **Content-Type 이 비어 있다.** Tomcat 이
`.webmanifest` 확장자를 모르기 때문이다. 크롬이 manifest 를 무시할 수 있다.

가장 간단한 해결은 파일명을 바꾸는 것이다(Tomcat 은 `.json` 을 안다).

```js
VitePWA({ manifestFilename: 'manifest.json' })
```

현재 내용은 이미 올바르다. `display: standalone`, `scope: /mErp/`, `start_url: /mErp/`,
아이콘 3종(180·192·512) 모두 있다.

### 4-7. UI 흐름 권장안

```
설정 화면 진입
  ├ pushBlockedReason() !== null  → 사유를 그대로 안내하고 토글 숨김
  └ getPushConfig() + isDeviceSubscribed()
       ├ loggedIn === false            → "로그인 후 이용" 안내
       ├ configured === false          → 항목 숨김 (서버 VAPID 미설정)
       ├ isDeviceSubscribed() === true → 토글 ON,  끄면 disablePush()
       └ 아니면                         → 토글 OFF, 켜면 enablePush(publicKey)
```

### ⚠️ 토글 상태를 `subscribed` 로 정하지 말 것

`config.do` 의 `subscribed` 는 **계정 단위**다. 한 사람이 폰·PC 를 함께 쓰면
폰만 등록돼 있어도 `true` 가 된다. 이 값으로 토글을 정하면 이렇게 된다.

```
폰에서 알림 켬  →  PC 로 접속  →  subscribed: true  →  토글이 "끄기" 로 표시
                                → PC 에서 켤 방법이 없어진다
```

개발 중 실제로 겪은 문제다. **기기 상태는 `isDeviceSubscribed()`** (브라우저의
`pushManager.getSubscription()`) 로 판단하고, `subscribed` 는 "다른 기기에도
등록돼 있다" 는 안내에만 쓴다.

| 값 | 의미 | 용도 |
|--|--|--|
| `isDeviceSubscribed()` | **이 기기**가 구독 중 | 토글 on/off |
| `config.subscribed` | **이 계정**에 기기가 하나라도 있음 | 참고 표시, 테스트 발송 버튼 활성화 |

발송은 계정 단위라 어느 기기에서 `test.do` 를 눌러도 **등록된 모든 기기**로 나간다.
누른 기기로 오는 게 아니다.

### SPA 라 로그인 후 재조회가 필요하다

**로그인해도 페이지가 새로 뜨지 않는다.** 로드 시 한 번만 조회하면 "로그인 필요"
상태가 그대로 굳는다. 설정 화면에 들어올 때마다 `getPushConfig()` 를 다시 호출할 것.

권한을 한 번 거부하면 되돌리기 어려우므로, 토글을 켤 때 **왜 알림이 필요한지 한 줄
안내를 먼저 보여주고** 확인을 누르면 그 클릭에서 `enablePush()` 를 호출하는 편이 좋다.

### 4-8. 연동 확인

`POST /api/push/test.do` 를 호출하면 본인 기기로 테스트 알림이 온다.
응답 `data` 가 발송에 성공한 기기 수다. `0` 이면 구독이 등록되지 않은 것이다.

```
1. 알림 켜기 → 권한 허용
2. SELECT COUNT(*) FROM PUSH_SUB_TB WHERE STATE='I';   ← 1 이상
3. POST /api/push/test.do                              ← 알림이 떠야 한다
4. 공지 등록 → 서버 로그 "웹푸시 발송: 대상 N건, 성공 N건"
```

**3번이 분기점이다.** 여기까지 되면 웹푸시 배관은 정상이고, 4번이 안 되면
게시판 연결만 보면 된다.

> 알림이 안 보이면 OS 쪽을 본다. 윈도우는 **집중 지원(방해 금지)**, 안드로이드는
> 앱 알림 권한. 알림 센터에는 쌓이는데 화면에 안 뜨는 경우가 흔하다.

### 4-9. 개발 중 흰 화면이 뜰 때

서버를 내렸다 올리는 동안 홈 화면 앱을 실행하면, 서비스워커가 캐시로 화면을 띄우고
API 호출만 실패해 **흰 화면**이 될 수 있다. 서버 로그에 아무것도 안 남는 것이 특징이다
(요청이 캐시에서 처리돼 서버까지 가지 않는다).

홈 화면 앱은 `display: standalone` 이라 **주소창도 새로고침 버튼도 없다.** 순서대로 시도한다.

1. 화면 맨 위에서 아래로 당겨 새로고침 — 대부분 여기서 해결된다
2. 최근 앱에서 완전 종료 후 재실행
3. 크롬 일반 탭에서 `https://dev.stand500.com/mErp/` — 여기서 되면 앱 캐시만 문제
4. 자물쇠 > 사이트 설정 > 데이터 삭제 → 홈 화면 아이콘 삭제 후 재추가

원인을 정확히 보려면 USB 연결 후 PC 크롬 `chrome://inspect/#devices` 에서
해당 페이지를 inspect 한다. Console 에 에러가 그대로 찍힌다.

---

## 5. 배포 체크리스트

### 개발 — 완료 (2026-09-15)

개발서버(`dev.stand500.com`)는 아래가 모두 끝나 동작 확인까지 마쳤다.

1. ✅ `PUSH_SUB_TB.sql` 적용
2. ✅ VAPID 키 설정 (`erp-local.properties`. `.gitignore` 대상)
3. ✅ 정식 인증서 전환 — `docs/docker/dev/SSL-NGINX-DEV.md`
4. ✅ 프론트 4개 파일 배포 (4장)
5. ✅ 구독 등록 → 테스트 발송 → 공지 등록 알림 전 구간 검증

**남은 일은 4-1 (원본 프로젝트 반영) 하나다.** 지금 개발서버에 올라간 프론트 코드는
빌드 산출물이라, 다음 빌드 때 사라진다.

로컬 Eclipse 로 띄울 때는 `GET /api/push/config.do` 가 `configured: true` 인지만 확인하면 된다.

### 운영

1. `PUSH_SUB_TB.sql` 운영 DB 적용
2. **운영용 키를 새로 생성**한다 — Eclipse 에서 `egovframework.psh.util.VapidKeyGenerator` 실행
3. 출력 2줄을 `sberp/.env` 의 `GLOBALS_PUSH_VAPID_PUBLIC` / `GLOBALS_PUSH_VAPID_PRIVATE` 에 기입
   (자리는 만들어 뒀다. 이 파일도 `.gitignore` 대상)
4. `docker compose up -d --force-recreate erp` 로 환경변수 반영 + `api.war` 재배포
5. 프론트 빌드에 4-1 반영 후 `/mErp/` 배포 (push-sw.js 포함)
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
- **프론트 원본 반영** — 개발서버에는 적용했지만 원본 프로젝트에는 아직이다. 4-1 참고.
- **manifest Content-Type** — Tomcat 이 `.webmanifest` 를 몰라 비어 있다. 4-6 참고.
- **PC 브라우저** — 서비스워커 scope 가 `/mErp/` 라 PC 화면(`/html/*`)에서는 구독할 수 없다.
  PC 에서도 받으려면 ROOT 에 별도 `sw.js` 와 알림 UI 가 필요하다.
