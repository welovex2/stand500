// 공통: API 호출, 로그인 확인, 머리글, 테마. 주소는 모두 상대 경로 (/chamber/ 아래에서 동작)
const TIMEOUT_MS = 10000;
// 모니터 전용 주소(…/monitor?key=)로 연 화면: 일반 로그인과 다른 모니터 쿠키를 쓴다 (서버가 X-Chamber-Kiosk 를 본다)
const KIOSK = new URLSearchParams(location.search).has('kiosk');

// opts.poll: 화면 자동 새로고침. 로그인 연장으로 치지 않는다 (서버가 X-Chamber-Poll 을 본다)
async function api(path, body, opts) {
  const ctrl = new AbortController();
  const timer = setTimeout(() => ctrl.abort(), TIMEOUT_MS);
  try {
    const res = await fetch(path, {
      method: body === undefined ? 'GET' : 'POST',
      headers: Object.assign(body === undefined ? {} : { 'Content-Type': 'application/json' },
        opts && opts.poll ? { 'X-Chamber-Poll': '1' } : {}, KIOSK ? { 'X-Chamber-Kiosk': '1' } : {}),
      body: body === undefined ? undefined : JSON.stringify(body),
      credentials: 'same-origin',
      signal: ctrl.signal,
    });
    if (res.status === 401 && KIOSK) throw new Error('모니터 주소로 다시 열어 주세요.');   // 로그인 화면으로 보내지 않는다
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
  return h ? `${h}시간 ${String(m).padStart(2, '0')}분` : `${m}분`;
}

// 구역이 하나뿐인 챔버는 구역 이름이 비어 있다
function zoneLabel(name) {
  return name || '전체';
}

function nowText() {
  return new Date().toLocaleTimeString('ko-KR', { hour12: false });
}

// 테마: 기본은 밝은 화면. 고른 값은 브라우저에 남긴다 (각 화면 <head> 의 한 줄이 같은 값을 먼저 읽는다)
function initTheme() {
  const btn = document.getElementById('thBtn');
  if (!btn) return;
  const draw = () => {
    const light = document.documentElement.classList.contains('light');
    btn.innerHTML = `<i class="ic ${light ? 'ic-moon' : 'ic-sun'}"></i>`;
    btn.dataset.tip = light ? '다크 모드' : '라이트 모드';
  };
  btn.addEventListener('click', () => {
    const light = document.documentElement.classList.toggle('light');
    try { localStorage.setItem('chamberTheme', light ? 'light' : 'dark'); } catch (e) { /* 저장 못 해도 화면은 바뀐다 */ }
    draw();
  });
  draw();
}

let toastTimer = null;
function toast(msg) {
  const el = document.getElementById('toast');
  if (!el) return;
  el.textContent = msg;
  el.classList.add('show');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => el.classList.remove('show'), 2600);
}

// 머리글에 사용자 이름·구분과 로그아웃을 넣고, 로그인 정보를 돌려준다
async function initPage() {
  initTheme();
  if (KIOSK) document.body.classList.add('kiosk');   // 메뉴 · 계정 · 로그아웃 · 모니터 버튼 숨김
  const me = await api('api/me');
  document.getElementById('who').textContent = me.memName;
  document.getElementById('accAv').textContent = (me.memName || '?')[0];
  const rb = document.getElementById('accRb');
  // 권한: 일반 사용자(EMC 부서) / 기술책임자(EMC + P04) / 관리자(P01·P02) / 조회 전용(모니터 계정)
  const ROLE = { user: '일반 사용자', rev: '기술책임자', admin: '관리자', mon: '조회 전용' };
  rb.textContent = ROLE[me.role] || ROLE.user;
  rb.className = 'rb ' + (me.role || 'user');
  const admin = document.getElementById('navAdmin');
  if (admin && me.adminMenu) admin.hidden = false;
  const tl = document.getElementById('navTimeline');
  if (tl && me.timelineMenu) tl.hidden = false;   // 시험원 현황은 기술책임자·관리자   // 관리 탭(챔버 관리)은 관리자(P01·P02)
  const menu = document.getElementById('menu');
  document.getElementById('acct').addEventListener('click', (e) => { e.stopPropagation(); menu.classList.toggle('show'); });
  document.addEventListener('click', () => menu.classList.remove('show'));
  document.getElementById('logout').addEventListener('click', async () => {
    await api('api/logout', {}).catch(() => {});
    location.href = 'login.html';
  });
  return me;
}
