<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="utf-8"/>
<title>시험 적체 현황</title>
<style>
body { margin:0; background:#e9edf2; color:#111; font:16px/1.4 "맑은 고딕", Malgun Gothic, sans-serif; }
a { color:#1a3d73; }
#wrap { width:1200px; margin:0 auto; background:#fff; min-height:100vh; box-shadow:0 0 0 1px #c5ccd6; }
#top { background:#2b4c7e; color:#fff; padding:12px 18px; overflow:hidden; }
#top h1 { margin:0; font-size:22px; display:inline; }
#top .who { float:right; font-size:15px; padding-top:4px; }
#bar { background:#f3f5f8; border-bottom:1px solid #c5ccd6; padding:10px 18px; overflow:hidden; }
#bar .tabs { float:left; }
#bar .tabs a { display:inline-block; margin-right:6px; padding:6px 12px; text-decoration:none; color:#333; font-size:16px; border:1px solid transparent; }
#bar .tabs a.on { background:#fff; border:1px solid #c5ccd6; border-bottom-color:#fff; font-weight:bold; color:#111; }
#bar .today-btns { float:right; }
#bar button { font:16px "맑은 고딕", Malgun Gothic, sans-serif; padding:6px 12px; cursor:pointer; margin-left:4px; }
#today-box { display:none; background:#fff8e8; border-bottom:1px solid #e0d3a8; padding:12px 18px 16px; }
#today-box.open { display:block; }
#today-box h2 { margin:0 0 8px; font-size:18px; }
#today-tools { margin:0 0 8px; font-size:15px; }
#guide { margin:16px 18px 8px; padding:14px 16px; background:#1f4e79; color:#fff; }
#guide h3 { margin:0 0 10px; font-size:18px; }
#guide .row { display:table; width:100%; border-collapse:separate; border-spacing:8px 0; margin-left:-8px; }
#guide .card { display:table-cell; width:25%; background:#fff; color:#111; padding:10px 12px; vertical-align:top; }
#guide .card b { display:block; font-size:16px; color:#1f4e79; margin-bottom:4px; }
#guide .card span { display:block; font-size:15px; }
#guide .foot { margin:10px 0 0; font-size:15px; }
.block { margin:16px 18px 20px; }
.block h2 { margin:0 0 8px; font-size:18px; }
.score { display:table; width:100%; border:1px solid #b8c0cc; border-collapse:collapse; margin-bottom:10px; }
.score .cell { display:table-cell; width:33.33%; padding:10px 12px; border-right:1px solid #b8c0cc; vertical-align:top; }
.score .cell:last-child { border-right:0; }
.score .k { display:block; font-size:14px; color:#555; }
.score .v { display:block; font-size:32px; font-weight:bold; line-height:1.15; margin:2px 0 4px; }
.score .s { display:block; font-size:14px; color:#444; }
.score .plus .v { color:#c00; }
table.grid { border-collapse:collapse; width:100%; table-layout:fixed; margin:0; font-size:15px; }
table.grid th, table.grid td { border:1px solid #b8c0cc; padding:7px 8px; vertical-align:middle; overflow:hidden; }
table.grid th { background:#eef2f7; text-align:center; }
table.grid td.now { white-space:nowrap; }
table.grid td.num { text-align:right; font-weight:bold; white-space:nowrap; }
table.grid td.ellipsis { overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.dept-table th:nth-child(1), .dept-table td:nth-child(1) { width:68px; }
.dept-table th:nth-child(4), .dept-table td:nth-child(4) { width:52px; }
.dept-table th:nth-child(5), .dept-table td:nth-child(5) { width:48px; }
#today-table th:nth-child(1), #today-table td:nth-child(1) { width:52px; }
#today-table th:nth-child(2), #today-table td:nth-child(2) { width:72px; }
#today-table th:nth-child(3), #today-table td:nth-child(3) { width:150px; }
#today-table th:nth-child(5), #today-table td:nth-child(5) { width:52px; }
#today-table th:nth-child(6), #today-table td:nth-child(6) { width:48px; }
#today-table th:nth-child(7), #today-table td:nth-child(7) { width:96px; }
.tag { display:inline-block; padding:1px 6px; font-size:13px; font-weight:bold; white-space:nowrap; }
.tag.ext { background:#fde8e8; color:#9b1c1c; }
.tag.unk { background:#e8eef5; color:#1f4e79; }
.empty { color:#666; padding:10px; text-align:center; }
@media print {
  body { background:#fff; }
  #wrap { width:auto; box-shadow:none; }
  #top, #bar, #guide, .block, #today-tools { display:none !important; }
  #today-box { display:block !important; border:0; background:#fff; padding:0; }
}
</style>
</head>
<body>
<div id="wrap">
<div id="top">
  <h1>시험 적체 현황</h1>
  <span class="who">
    <a href="<c:url value='/page/list.do'/>" style="color:#fff;margin-right:12px;">화면 목록</a>
    <c:out value="${userName}"/> / <c:out value="${today}"/>
  </span>
</div>

<div id="bar">
  <div class="tabs">
    <a href="<c:url value='/page/stuck.do?testTypeCode=EM'/>" class="${testTypeCode == 'EM' ? 'on' : ''}">EMC</a>
    <a href="<c:url value='/page/stuck.do?testTypeCode=RS'/>" class="${testTypeCode == 'RS' ? 'on' : ''}">RF&amp;SAR</a>
    <a href="<c:url value='/page/stuck.do?testTypeCode=SF'/>" class="${testTypeCode == 'SF' ? 'on' : ''}">SAFETY&amp;효율신뢰</a>
    <a href="<c:url value='/page/stuck.do?testTypeCode=MD'/>" class="${testTypeCode == 'MD' ? 'on' : ''}">MEDICAL</a>
  </div>
  <div class="today-btns">
    <button type="button" id="btnToday" onclick="toggleToday()">오늘 처리할 명단 (<c:out value="${todayCount}"/>)</button>
    <button type="button" onclick="copyToday()" <c:if test="${todayCount == 0}">disabled="disabled"</c:if>>명단 복사</button>
    <button type="button" onclick="printToday()" <c:if test="${todayCount == 0}">disabled="disabled"</c:if>>명단 인쇄</button>
  </div>
</div>

<div id="today-box">
  <h2>오늘 처리할 명단 — 홀딩·RD지연 7일 이상, 경과일 긴 순</h2>
  <p id="today-tools">
    <label><input type="checkbox" id="hideCompany" onclick="filterToday()"/> 업체사정 홀딩 접기 (<c:out value="${companyHoldCount}"/>건)</label>
    · 사유는 시험 게시판 최신글입니다. 마우스를 올리면 작성자와 전체가 보입니다.
  </p>
  <c:choose>
    <c:when test="${empty todayList}"><div class="empty">해당 건이 없습니다.</div></c:when>
    <c:otherwise>
      <table class="grid" id="today-table">
        <thead>
          <tr>
            <th>상태</th>
            <th>구분</th>
            <th>시험번호</th>
            <th>고객</th>
            <th>담당</th>
            <th>경과일</th>
            <th>상태일</th>
            <th>사유</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="row" items="${todayList}">
            <tr data-cause="<c:out value='${row.causeType}'/>">
              <td class="now"><c:out value="${row.testState}"/></td>
              <td class="now">
                <c:choose>
                  <c:when test="${row.causeType == '업체사정'}"><span class="tag ext">업체사정</span></c:when>
                  <c:when test="${row.causeType == '확인필요'}"><span class="tag unk">확인필요</span></c:when>
                  <c:otherwise>-</c:otherwise>
                </c:choose>
              </td>
              <td class="now"><c:out value="${row.testId}"/></td>
              <td class="ellipsis" title="<c:out value='${row.cmpyName}'/>"><c:out value="${row.cmpyName}"/></td>
              <td class="ellipsis" title="<c:out value='${row.mngName}'/>"><c:out value="${row.mngName}"/></td>
              <td class="num"><c:out value="${row.stuckDays}"/></td>
              <td class="now"><c:out value="${row.stateDt}"/></td>
              <td class="ellipsis" title="<c:out value='${row.boardTitle}'/>"><c:out value="${row.causeMemo}"/></td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </c:otherwise>
  </c:choose>
</div>

<div id="guide">
  <div class="row">
    <div class="card">
      <b>막힌 일</b>
      <span>디버깅 + 홀딩 + RD지연만 셉니다.</span>
    </div>
    <div class="card">
      <b>14일 이상</b>
      <span>같은 상태로 2주를 넘긴 건입니다.</span>
    </div>
    <div class="card">
      <b>지난주 순증</b>
      <span>접수에서 완료를 뺀 값입니다. <b style="color:#c00">+ 면 일이 쌓인 주</b>, − 면 소화한 주입니다.</span>
    </div>
    <div class="card">
      <b>출장</b>
      <span>EMC·MEDICAL만 있습니다. 위 부서 숫자에 이미 들어 있으니 아래 출장 줄과 더하지 않습니다.</span>
    </div>
  </div>
  <div class="foot">홀딩 구분은 시험 게시판 <b>최신글</b>에 업체·취하·요청이 있으면 <b>업체사정</b>입니다.</div>
</div>

<c:forEach var="g" items="${groups}">
  <div class="block">
    <h2>
      <c:choose>
        <c:when test="${g.summary.testTypeCode == 'TT'}">합계</c:when>
        <c:when test="${g.summary.testTypeCode == 'EMC' or g.summary.testTypeCode == 'MDC'}">출장</c:when>
        <c:otherwise><c:out value="${g.summary.testType}"/></c:otherwise>
      </c:choose>
    </h2>

    <div class="score">
      <div class="cell">
        <span class="k">막힌 일</span>
        <span class="v"><c:out value="${g.summary.stuckCnt}"/></span>
        <span class="s">디버깅 <c:out value="${g.summary.debugCnt}"/> · 홀딩 <c:out value="${g.summary.holdingCnt}"/> · RD지연 <c:out value="${g.summary.rdDelayCnt}"/></span>
      </div>
      <div class="cell">
        <span class="k">14일 이상</span>
        <span class="v"><c:out value="${g.summary.stuck14dCnt}"/></span>
        <span class="s">같은 상태로 2주 초과</span>
      </div>
      <div class="cell ${g.summary.lastWeekNet gt 0 ? 'plus' : ''}">
        <span class="k">지난주 순증</span>
        <span class="v"><c:out value="${g.summary.lastWeekNet}"/></span>
        <span class="s">접수 <c:out value="${g.summary.lastWeekInCnt}"/> · 완료 <c:out value="${g.summary.lastWeekEndCnt}"/></span>
      </div>
    </div>

    <c:if test="${g.summary.testTypeCode != 'TT'}">
      <table class="grid dept-table">
        <thead>
          <tr>
            <th>구분</th>
            <th>시험번호</th>
            <th>고객</th>
            <th>담당</th>
            <th>경과일</th>
            <th>사유</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="row" items="${g.holdingList}">
            <tr>
              <td class="now"><c:choose>
                <c:when test="${row.causeType == '업체사정'}"><span class="tag ext">업체</span></c:when>
                <c:when test="${row.causeType == '확인필요'}"><span class="tag unk">확인</span></c:when>
                <c:otherwise>홀딩</c:otherwise>
              </c:choose></td>
              <td class="now"><c:out value="${row.testId}"/></td>
              <td class="ellipsis" title="<c:out value='${row.cmpyName}'/>"><c:out value="${row.cmpyName}"/></td>
              <td class="ellipsis" title="<c:out value='${row.mngName}'/>"><c:out value="${row.mngName}"/></td>
              <td class="num"><c:out value="${row.stuckDays}"/></td>
              <td class="ellipsis" title="<c:out value='${row.boardTitle}'/>"><c:out value="${row.causeMemo}"/></td>
            </tr>
          </c:forEach>
          <c:forEach var="row" items="${g.rdDelayList}">
            <tr>
              <td class="now">RD지연</td>
              <td class="now"><c:out value="${row.testId}"/></td>
              <td class="ellipsis" title="<c:out value='${row.cmpyName}'/>"><c:out value="${row.cmpyName}"/></td>
              <td class="ellipsis" title="<c:out value='${row.mngName}'/>"><c:out value="${row.mngName}"/></td>
              <td class="num"><c:out value="${row.stuckDays}"/></td>
              <td class="ellipsis" title="<c:out value='${row.boardTitle}'/>"><c:out value="${row.causeMemo}"/></td>
            </tr>
          </c:forEach>
          <c:if test="${empty g.holdingList and empty g.rdDelayList}">
            <tr><td colspan="6" class="empty">7일 이상 홀딩·RD지연 건 없음</td></tr>
          </c:if>
        </tbody>
      </table>
    </c:if>
  </div>
</c:forEach>
</div>

<script>
function toggleToday() {
  var box = document.getElementById('today-box');
  var open = box.className.indexOf('open') >= 0;
  box.className = open ? '' : 'open';
}
function filterToday() {
  var hide = document.getElementById('hideCompany').checked;
  var rows = document.querySelectorAll('#today-table tbody tr');
  for (var i = 0; i < rows.length; i++) {
    var cause = rows[i].getAttribute('data-cause') || '';
    rows[i].style.display = (hide && cause === '업체사정') ? 'none' : '';
  }
}
function printToday() {
  document.getElementById('today-box').className = 'open';
  window.print();
}
function copyToday() {
  var table = document.getElementById('today-table');
  if (!table) {
    alert('복사할 명단이 없습니다.');
    return;
  }
  var lines = [];
  var rows = table.rows;
  for (var i = 0; i < rows.length; i++) {
    if (rows[i].style && rows[i].style.display === 'none') {
      continue;
    }
    var cells = rows[i].cells;
    var cols = [];
    for (var j = 0; j < cells.length; j++) {
      cols.push((cells[j].innerText || '').replace(/\t/g, ' ').trim());
    }
    lines.push(cols.join('\t'));
  }
  var text = lines.join('\n');
  if (navigator.clipboard && navigator.clipboard.writeText) {
    navigator.clipboard.writeText(text).then(function() {
      alert('명단을 복사했습니다. 엑셀에 붙여 넣으면 됩니다.');
    }, function() {
      fallbackCopy(text);
    });
  } else {
    fallbackCopy(text);
  }
}
function fallbackCopy(text) {
  var ta = document.createElement('textarea');
  ta.value = text;
  document.body.appendChild(ta);
  ta.select();
  try {
    document.execCommand('copy');
    alert('명단을 복사했습니다. 엑셀에 붙여 넣으면 됩니다.');
  } catch (e) {
    alert('복사에 실패했습니다. 명단을 연 뒤 직접 선택하세요.');
  }
  document.body.removeChild(ta);
}
</script>
</body>
</html>
