// 공통: API 호출, 로그인 확인, 머리글. 주소는 모두 상대 경로 (/chamber/ 아래에서 동작)
const TIMEOUT_MS = 10000;

async function api(path, body) {
  const ctrl = new AbortController();
  const timer = setTimeout(() => ctrl.abort(), TIMEOUT_MS);
  try {
    const res = await fetch(path, {
      method: body === undefined ? 'GET' : 'POST',
      headers: body === undefined ? {} : { 'Content-Type': 'application/json' },
      body: body === undefined ? undefined : JSON.stringify(body),
      credentials: 'same-origin',
      signal: ctrl.signal,
    });
    if (res.status === 401 && !path.startsWith('api/login')) {
      location.href = 'login.html';
      throw new Error('로그인이 필요합니다.');
    }
    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
      const err = new Error(typeof data.detail === 'string' ? data.detail : '요청을 처리하지 못했습니다.');
      err.status = res.status;
      err.data = data;
      throw err;
    }
    return data;
  } catch (e) {
    if (e.name === 'AbortError') throw new Error('응답이 늦어 요청을 취소했습니다.');
    throw e;
  } finally {
    clearTimeout(timer);
  }
}

function esc(v) {
  return String(v ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

function minText(min) {
  const h = Math.floor(min / 60), m = min % 60;
  return h ? `${h}시간 ${m}분` : `${m}분`;
}

// 구역이 하나뿐인 챔버는 구역 이름이 비어 있다
function zoneLabel(name) {
  return name || '전체';
}

function nowText() {
  return new Date().toLocaleTimeString('ko-KR', { hour12: false });
}

// 머리글에 사용자 이름과 로그아웃을 넣고, 로그인 정보를 돌려준다
async function initPage() {
  const me = await api('api/me');
  document.getElementById('who').textContent = me.memName + (me.monitorYn ? ' (조회 전용)' : '');
  const admin = document.getElementById('navAdmin');
  if (admin && me.revYn) admin.hidden = false;   // 챔버 관리는 기술책임자만
  document.getElementById('logout').addEventListener('click', async () => {
    await api('api/logout', {}).catch(() => {});
    location.href = 'login.html';
  });
  return me;
}
