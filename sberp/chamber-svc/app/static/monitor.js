// 모니터 모드 스킨의 동작 (index.html 에서만 읽는다. index.html 의 render·me 를 쓴다)
//  - 머리글 시계, 사용 중 시험장 막대, 0건 숫자 흐리게
//  - 카드의 경과 시간 막대 (장시간 기준 = me.staleHours), 콜론 깜빡임, 상태가 바뀐 카드 강조
//  - 진행 중 목록이 넘치면 천천히 흐르게
//  - [모니터용 확대] 버튼으로 전체 화면, 마우스를 5초 움직이지 않으면 커서 숨김
(function () {
  const $ = (id) => document.getElementById(id), body = document.body;
  const pad = (n) => String(n).padStart(2, '0');
  const monitorOn = () => body.classList.contains('monitor');

  function clock() {
    const d = new Date();
    $('sigD').textContent = `${d.getMonth() + 1}월 ${d.getDate()}일 ${'일월화수목금토'[d.getDay()]}요일`;
    $('sigH').innerHTML = pad(d.getHours()) + '<span class="cl">:</span>' + pad(d.getMinutes());
    $('sigS').textContent = pad(d.getSeconds());
  }
  clock();
  setInterval(clock, 1000);

  // 사용 중 시험장 수를 막대로 (켜진 칸 = 사용 중). 0건인 숫자는 흐리게
  const pips = document.createElement('span');
  pips.className = 'pips';
  $('sTot').after(pips);
  function stats() {
    const on = Number($('sOn').textContent) || 0, tot = Number($('sTot').textContent.replace(/\D/g, '')) || 0;
    pips.innerHTML = Array.from({ length: tot }, (_, i) => `<i class="${i < on ? 'on' : ''}"></i>`).join('');
    document.querySelectorAll('.stat b').forEach(b => b.classList.toggle('zero', b.textContent.trim() === '0'));
  }

  // 사용 중 카드(구역 하나)의 경과 시간 막대와 콜론
  function cards() {
    const full = (me && me.staleHours ? me.staleHours : 12) * 60;
    document.querySelectorAll('#view .card:not(.multi):not(.idle) .big').forEach(b => {
      const m = /(\d+):(\d+)/.exec(b.textContent);
      if (!m) return;
      const p = Math.min(100, (Number(m[1]) * 60 + Number(m[2])) / full * 100);
      b.insertAdjacentHTML('afterend', `<div class="prog"><i style="width:${p.toFixed(1)}%"></i></div>`);
    });
    document.querySelectorAll('#view .big').forEach(b => { b.innerHTML = b.textContent.replace(':', '<span class="cl">:</span>'); });
  }

  // 직전 화면과 비교해 상태가 바뀐 카드에 강조 효과 (대기 ↔ 사용 중, 구역별 시작·종료, 장시간 전환)
  let prev = null;
  function changes() {
    const cur = {};
    document.querySelectorAll('#view .card').forEach(c => {
      const n = (c.querySelector('.name') || {}).textContent;
      if (!n) return;
      const st = ['warn', 'on', 'idle'].find(k => c.classList.contains(k)) || '';
      cur[n] = st + '|' + [...c.querySelectorAll('.slot')].map(s => s.className).join(',');
      if (prev && n in prev && prev[n] !== cur[n]) c.classList.add('chg');
    });
    prev = Object.assign(prev || {}, cur);
  }

  // 진행 중 목록: 넘치면 천천히 아래로 흐르고, 끝에서 3초 멈췄다가 처음으로
  let liveTop = 0, livePause = 0;
  setInterval(() => {
    const l = document.querySelector('#view .live');
    if (!l || !monitorOn()) return;
    const max = l.scrollHeight - l.clientHeight;
    if (max <= 2) { liveTop = 0; return; }
    if (livePause > 0) {
      livePause--;
      if (livePause === 0 && liveTop >= max) liveTop = 0;
      l.scrollTop = liveTop;
      return;
    }
    liveTop = Math.min(max, liveTop + 1);
    l.scrollTop = liveTop;
    if (liveTop >= max || liveTop === 1) livePause = 60;
  }, 50);

  // index.html 의 render 뒤에 붙인다 (30초마다·탭을 바꿀 때마다 다시 그려진다)
  const draw = render;
  render = function () {
    draw();
    cards();
    changes();
    stats();
    const l = document.querySelector('#view .live');
    if (l) l.scrollTop = liveTop;
  };

  // [모니터용 확대]를 누르면 전체 화면, 해제하면 전체 화면도 해제 (브라우저는 사용자가 누를 때만 전체 화면을 허용한다)
  $('monBtn').addEventListener('click', () => {
    try {
      if (monitorOn()) { if (document.documentElement.requestFullscreen) document.documentElement.requestFullscreen().catch(() => {}); }
      else if (document.fullscreenElement) document.exitFullscreen();
    } catch (e) { /* 전체 화면을 못 해도 화면은 그대로 쓴다 */ }
  });

  // "연결 끊김" 안내가 뜨거나 사라지면 평면도 높이를 다시 맞춘다 (모니터 모드는 스크롤이 없어 아래가 잘린다)
  new MutationObserver(() => fitView()).observe($('notice'), { attributes: true, attributeFilter: ['class'] });

  // 마우스를 5초 동안 움직이지 않으면 커서를 숨긴다
  let ct;
  document.addEventListener('mousemove', () => {
    body.classList.remove('nocur');
    clearTimeout(ct);
    ct = setTimeout(() => body.classList.add('nocur'), 5000);
  });

  stats();
})();
