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

  // 진행 중 목록: 넘치면 천천히 위로 흐른다. 처음·끝에서 4초 멈추고, 끝에서는 흐려졌다가 처음부터 다시.
  // scrollTop 은 1px 단위로 끊겨 보여서, 목록(.lrs)을 transform 으로 소수점 단위로 부드럽게 옮긴다.
  let liveTop = 0, liveWait = 4000, liveFade = 0, liveLast = 0;
  function liveMove(l) {
    const rs = l.querySelector('.lrs');
    if (!rs) return;
    rs.style.transform = liveTop ? `translate3d(0,${-liveTop}px,0)` : '';
    rs.style.opacity = liveFade ? '0' : '';
  }
  function liveTick(now) {
    requestAnimationFrame(liveTick);
    const dt = Math.min(100, now - (liveLast || now));
    liveLast = now;
    const l = document.querySelector('#view .live');
    if (!l) return;
    const rs = l.querySelector('.lrs');
    if (!rs || !monitorOn()) { liveTop = 0; liveFade = 0; if (rs) liveMove(l); return; }
    // 목록 아래 끝이 패널 안쪽 아래 끝에 닿을 때까지
    const pad = parseFloat(getComputedStyle(l).paddingBottom) || 0;
    const max = Math.max(0, rs.getBoundingClientRect().bottom + liveTop - (l.getBoundingClientRect().bottom - pad));
    if (max <= 2) { liveTop = 0; liveWait = 4000; liveFade = 0; liveMove(l); return; }
    if (liveFade) {                        // 흐려진 동안 처음으로 돌아간다
      liveFade -= dt;
      if (liveFade <= 0) { liveFade = 0; liveTop = 0; liveWait = 4000; }
    } else if (liveWait > 0) {
      liveWait -= dt;
      if (liveWait <= 0 && liveTop >= max - 2) liveFade = 700;   // 높이는 소수점이 조금씩 흔들려서 2px 여유
    } else {
      liveTop = Math.min(max, liveTop + dt * innerHeight / 45000);   // 1080 화면에서 초당 약 24px
      if (liveTop >= max - 2) liveWait = 4000;
    }
    liveMove(l);
  }
  requestAnimationFrame(liveTick);

  // index.html 의 render 뒤에 붙인다 (30초마다·탭을 바꿀 때마다 다시 그려진다)
  const draw = render;
  render = function () {
    draw();
    cards();
    changes();
    stats();
    const l = document.querySelector('#view .live');
    if (l) liveMove(l);                    // 다시 그려도 흐르던 위치 그대로
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
