/*
 * #26-8 측정일·환경조건 — RE 화면 수동 테스트 (브라우저 개발자도구 콘솔용)
 *
 * 사용법
 *   1. 로그인한 상태로 http://localhost:8080/html/raw_update_RE.html?test=21697&&raw=10803&&type=RE 열기
 *   2. F12 → Console 에 이 파일 내용을 붙여넣고 Enter
 *   3. 아래 순서대로 한 줄씩 실행 (await 포함)
 *
 *      await envs()                                   // 현재 표 상태 (envSeq 확인)
 *      await re('START', 'RE1', [newRow('RE1')])      // [시작] RE1 — 새 행 + 온습도·이름
 *      await re('START', 'RE1', [newRow('RE1')])      // 다시 [시작] → CHECK_ENV_NOT_ENDED 기대
 *      await re('', null, [row(envSeq, {temp: '24.0'})])          // [저장] 진행 중 행 온도만 변경
 *      await re('END', 'RE1', [row(envSeq, {memName: ''})])       // 시험자명 비움 → CHECK_ENV_REQUIRED 기대
 *      await re('END', 'RE1', [row(envSeq)])                      // [종료]
 *      await re('', null, [row(envSeq, {temp: '99'})])            // 종료된 행 → 값 안 바뀌어야 함 (무시)
 *      await envs()
 *
 *   envSeq 는 envs() 출력에서 확인해 숫자로 넣는다.
 */
var TEST_SEQ = 21697;
var RAW_SEQ = 10803;

// 로그의 측정설비 그대로 (RA: 1~2.6GHz 대역, RB: 그 위 대역)
var MACS = [
  ['RA', 488], ['RA', 38], ['RA', 166], ['RA', 40], ['RA', 200], ['RA', 173], ['RA', 126],
  ['RA', 127], ['RA', 12], ['RA', 496], ['RA', 132], ['RA', 17], ['RA', 16], ['RA', 46],
  ['RB', 87], ['RB', 32], ['RB', 186], ['RB', 128], ['RB', 125], ['RB', 496], ['RB', 12],
  ['RB', 200], ['RB', 488], ['RB', 46]
].map(function (m) {
  return { rawMacSeq: 0, machineType: m[0], machineSeq: m[1], useYn: 0 };
});

/** RE 방법 저장 + 측정 행. action: 'START' | 'END' | '' */
async function re(action, metKey, rows) {
  var dto = {
    rawSeq: RAW_SEQ,
    testSeq: TEST_SEQ,
    picYn: 1,
    // 둘 다 있으면 RA/RB 2대역 규격으로 처리 (로그와 동일)
    hz1ResultCode: '1',
    hz2ResultCode: '1',
    macResetYn: 0,
    macList: MACS,
    envAction: action || '',
    envMetKey: metKey || '',
    envRows: rows || []
  };
  var fd = new FormData();
  fd.append('reDTO', new Blob([JSON.stringify(dto)], { type: 'application/json' }));
  var res = await fetch('/api/raw/re/insert.do', { method: 'POST', body: fd, credentials: 'include' });
  var json = await res.json();
  console.log(action || 'SAVE', metKey || '', json.result, json.message || '', json.data);
  return json;
}

/** [시작]할 새 행 값 (envSeq 0) */
function newRow(metKey) {
  return { envSeq: 0, metKey: metKey, temp: '23.1', tempPlus: '0.5', hmdt: '45.2', hmdtPlus: '0.5',
           memName: '김시험', sign: '', signUrl: '' };
}

/** 진행 중 행 값. override 로 일부만 바꿔 보낼 수 있다 */
function row(envSeq, override) {
  var r = { envSeq: envSeq, temp: '23.1', tempPlus: '0.5', hmdt: '45.2', hmdtPlus: '0.5',
            memName: '김시험', sign: '', signUrl: '' };
  return Object.assign(r, override || {});
}

/** 현재 RE 표 (detail.do 의 envList) */
async function envs() {
  var res = await fetch('/api/raw/' + TEST_SEQ + '/re/detail.do', { credentials: 'include' });
  var json = await res.json();
  var list = (json.data && json.data.envList) || [];
  list.forEach(function (t) {
    console.log(t.metKey, 'canAdd=' + t.canAdd);
    console.table((t.rows || []).map(function (r) {
      return { envSeq: r.envSeq, rowNo: r.rowNo, start: r.testSDt, end: r.testEDt, min: r.elapsedMin,
               temp: r.temp, hmdt: r.hmdt, name: r.memName, canEdit: r.canEdit, canEnd: r.canEnd,
               canRevEdit: r.canRevEdit, canDelete: r.canDelete };
    }));
  });
  return list;
}

/** 기술책임자 수정 (종료된 행) */
async function revUpdate(envSeq, body) {
  var res = await fetch('/api/raw/env/' + envSeq + '/update.do', {
    method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(Object.assign({ testSeq: TEST_SEQ }, body))
  });
  var json = await res.json();
  console.log('update', envSeq, json.result, json.message || '');
  return json;
}

/** [중단] / [삭제] */
async function stopRow(envSeq, reason) {
  return reasonCall('stop', envSeq, reason);
}
async function deleteRow(envSeq, reason) {
  return reasonCall('delete', envSeq, reason);
}
async function reasonCall(kind, envSeq, reason) {
  var res = await fetch('/api/raw/env/' + envSeq + '/' + kind + '.do', {
    method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ testSeq: TEST_SEQ, reason: reason })
  });
  var json = await res.json();
  console.log(kind, envSeq, json.result, json.message || '');
  return json;
}

console.log('준비 완료 — await envs() 부터 실행');
