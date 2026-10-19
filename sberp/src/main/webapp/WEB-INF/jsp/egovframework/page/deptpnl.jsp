<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="utf-8"/>
<title>시험부 손익</title>
<style>
body { margin:0; background:#e9edf2; color:#111; font:16px/1.45 "맑은 고딕", Malgun Gothic, sans-serif; }
a { color:#1a3d73; }
#wrap { width:1100px; margin:0 auto; background:#fff; min-height:100vh; box-shadow:0 0 0 1px #c5ccd6; }
#top { background:#2b4c7e; color:#fff; padding:12px 18px; overflow:hidden; }
#top h1 { margin:0; font-size:22px; display:inline; }
#top .who { float:right; font-size:15px; padding-top:4px; }
#bar { background:#f3f5f8; border-bottom:1px solid #c5ccd6; padding:10px 18px; overflow:hidden; }
#bar .note { float:left; color:#333; font-size:15px; padding-top:6px; }
#bar .btns { float:right; }
#bar button { font:16px "맑은 고딕", Malgun Gothic, sans-serif; padding:6px 12px; cursor:pointer; }
.sec { margin:18px 18px 0; }
.sec h2 { margin:0 0 6px; font-size:18px; }
.sec .def { margin:0 0 10px; color:#444; font-size:14px; }
.cards { display:table; width:100%; border:1px solid #b8c0cc; border-collapse:collapse; table-layout:fixed; }
.card { display:table-cell; padding:12px 14px; border-right:1px solid #b8c0cc; vertical-align:top; }
.card:last-child { border-right:0; }
.card .k { display:block; font-size:13px; color:#555; }
.card .v { display:block; font-size:24px; font-weight:bold; line-height:1.2; margin:4px 0; }
.card .s { display:block; font-size:13px; color:#444; }
.card.bad .v { color:#c00; }
.card.warn .v { color:#b45309; }
table.grid { border-collapse:collapse; width:100%; font-size:15px; }
table.grid th, table.grid td { border:1px solid #b8c0cc; padding:7px 8px; vertical-align:middle; }
table.grid th { background:#eef2f7; text-align:center; }
table.grid td.num { text-align:right; font-weight:bold; white-space:nowrap; }
table.grid td.now { white-space:nowrap; }
table.grid td.sub { text-align:right; font-weight:normal; white-space:nowrap; }
table.grid tfoot td { background:#f7f9fb; font-weight:bold; }
table.grid tr.hi td { background:#fbfdff; }
.up { color:#1d6f42; font-weight:bold; }
.dn { color:#c00; font-weight:bold; }
.flat { color:#555; }
.tag { display:inline-block; padding:1px 6px; font-size:13px; font-weight:bold; }
.tag.old { background:#fde8e8; color:#9b1c1c; }
.tag.good { background:#e6f4ea; color:#1d6f42; }
.empty { color:#666; padding:10px; text-align:center; }
.foot { margin:16px 18px 24px; color:#555; font-size:13px; }
@media print {
  body { background:#fff; }
  #wrap { width:auto; box-shadow:none; }
  #bar { display:none !important; }
  #top a { color:#fff; }
}
</style>
</head>
<body>
<div id="wrap">
<div id="top">
  <h1>시험부 손익</h1>
  <span class="who">
    <a href="<c:url value='/page/list.do'/>" style="color:#fff;margin-right:12px;">화면 목록</a>
    <c:out value="${userName}"/> / <c:out value="${pnl.asOfDate}"/>
  </span>
</div>

<div id="bar">
  <div class="note">
    수주(견적 발행) 기준 · <c:out value="${pnl.year}"/>년 1월 1일 ~ 오늘 vs
    <c:out value="${pnl.prevYear}"/>년 같은 기간
  </div>
  <div class="btns">
    <button type="button" onclick="window.print()">인쇄</button>
  </div>
</div>

<c:choose>
<c:when test="${empty pnl.rows}">
  <div class="sec"><div class="empty">집계할 수주 자료가 없습니다.</div></div>
</c:when>
<c:otherwise>

<div class="sec">
  <h2>올해 수주</h2>
  <p class="def">
    <b>수주</b> = 견적 발행일이 올해인 시험의 순매출. 매출확정이 아니라 견적 시점으로 잡는다.
    견적에서 신청으로 넘어가는 비율이 90%대여서 견적이 곧 수주에 가깝고,
    매출확정으로 보면 리드타임만큼 늦게 보여 믹스 변화를 놓친다.
  </p>
  <div class="cards">
    <div class="card">
      <span class="k">순매출</span>
      <span class="v"><c:out value="${pnl.totalCurNetEok}"/></span>
      <span class="s">전년동기 <c:out value="${pnl.totalPrevNetEok}"/>
        <c:choose>
          <c:when test="${pnl.totalNetYoyPct > 0}"><span class="up">+<c:out value="${pnl.totalNetYoyPct}"/>%</span></c:when>
          <c:when test="${pnl.totalNetYoyPct < 0}"><span class="dn"><c:out value="${pnl.totalNetYoyPct}"/>%</span></c:when>
          <c:otherwise><span class="flat">&mdash;</span></c:otherwise>
        </c:choose>
      </span>
    </div>
    <div class="card">
      <span class="k">건수</span>
      <span class="v"><c:out value="${pnl.totalCurCnt}"/>건</span>
      <span class="s">전년동기 <c:out value="${pnl.totalPrevCnt}"/>건
        <c:choose>
          <c:when test="${pnl.totalCntYoyPct > 0}"><span class="up">+<c:out value="${pnl.totalCntYoyPct}"/>%</span></c:when>
          <c:when test="${pnl.totalCntYoyPct < 0}"><span class="dn"><c:out value="${pnl.totalCntYoyPct}"/>%</span></c:when>
          <c:otherwise><span class="flat">&mdash;</span></c:otherwise>
        </c:choose>
      </span>
    </div>
    <div class="card">
      <span class="k">건당 단가</span>
      <span class="v"><c:out value="${pnl.totalCurAvgMan}"/>만</span>
      <span class="s">전년동기 <c:out value="${pnl.totalPrevAvgMan}"/>만</span>
    </div>
    <div class="card warn">
      <span class="k">진행 중이라 묶인 돈</span>
      <span class="v"><c:out value="${pnl.totalWipEok}"/></span>
      <span class="s"><c:out value="${pnl.totalWipCnt}"/>건 진행 중</span>
    </div>
    <div class="card bad">
      <span class="k">그중 막힌 것</span>
      <span class="v"><c:out value="${pnl.totalStuckEok}"/></span>
      <span class="s"><c:out value="${pnl.totalStuckCnt}"/>건 · 디버깅·홀딩·RD지연</span>
    </div>
  </div>
</div>

<div class="sec">
  <h2>시험부별 수주 &mdash; 건수와 매출이 어긋나는 곳</h2>
  <p class="def">
    <b>매출 몫 &minus; 건수 몫</b> = 그 시험부가 차지한 매출 비율에서 차지한 건수 비율을 뺀 값.
    음수면 시험 대수를 쓴 만큼 매출을 못 낸다는 뜻이고, 양수면 적은 대수로 많이 번다는 뜻이다.
    장비를 더 사지 않고 매출을 올리는 길은 음수 쪽 물량을 양수 쪽으로 옮기는 것이다.<br/>
    <b>남는 비율</b> = 순매출 &divide; 청구액. 낮으면 외주비·대납분이 많이 끼어 있다.
  </p>
  <table class="grid">
    <thead>
      <tr>
        <th rowspan="2">시험부</th>
        <th colspan="3">올해</th>
        <th colspan="2">전년동기</th>
        <th colspan="2">비중</th>
        <th rowspan="2">매출 몫<br/>&minus; 건수 몫</th>
        <th rowspan="2">남는<br/>비율</th>
      </tr>
      <tr>
        <th>건수</th>
        <th>순매출</th>
        <th>건당</th>
        <th>순매출</th>
        <th>건당</th>
        <th>건수</th>
        <th>매출</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="r" items="${pnl.rows}">
        <tr<c:if test="${r.shareGapPct > 0}"> class="hi"</c:if>>
          <td class="now"><b><c:out value="${r.deptName}"/></b></td>
          <td class="num"><c:out value="${r.curCnt}"/></td>
          <td class="num"><c:out value="${r.curNetEok}"/></td>
          <td class="num"><c:out value="${r.curAvgMan}"/>만</td>
          <td class="sub"><c:out value="${r.prevNetMan}"/>만</td>
          <td class="sub"><c:out value="${r.prevAvgMan}"/>만</td>
          <td class="sub"><c:out value="${r.cntSharePct}"/>%</td>
          <td class="sub"><c:out value="${r.netSharePct}"/>%</td>
          <td class="num">
            <c:choose>
              <c:when test="${r.shareGapPct > 0}"><span class="tag good">+<c:out value="${r.shareGapPct}"/>%p</span></c:when>
              <c:when test="${r.shareGapPct < 0}"><span class="tag old"><c:out value="${r.shareGapPct}"/>%p</span></c:when>
              <c:otherwise><span class="flat">0</span></c:otherwise>
            </c:choose>
          </td>
          <td class="sub"><c:out value="${r.netRatePct}"/>%</td>
        </tr>
      </c:forEach>
    </tbody>
    <tfoot>
      <tr>
        <td class="now">합계</td>
        <td class="num"><c:out value="${pnl.totalCurCnt}"/></td>
        <td class="num"><c:out value="${pnl.totalCurNetEok}"/></td>
        <td class="num"><c:out value="${pnl.totalCurAvgMan}"/>만</td>
        <td class="num"><c:out value="${pnl.totalPrevNetMan}"/>만</td>
        <td class="num"><c:out value="${pnl.totalPrevAvgMan}"/>만</td>
        <td class="num">100%</td>
        <td class="num">100%</td>
        <td class="num">&mdash;</td>
        <td class="num">&mdash;</td>
      </tr>
    </tfoot>
  </table>
</div>

<div class="sec">
  <h2>전년동기 대비 &mdash; 어디가 늘었나</h2>
  <p class="def">
    건수가 늘어서 늘었는지, 단가가 올라서 늘었는지를 가른다.
    건수는 장비와 사람을 더 써야 늘지만, 단가는 더 쓰지 않아도 는다.
  </p>
  <table class="grid">
    <thead>
      <tr>
        <th>시험부</th>
        <th>건수 증감</th>
        <th>순매출 증감</th>
        <th>건당 단가 증감</th>
        <th>성장의 출처</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="r" items="${pnl.rows}">
        <tr>
          <td class="now"><b><c:out value="${r.deptName}"/></b></td>
          <td class="num">
            <c:choose>
              <c:when test="${r.prevCnt == 0}"><span class="flat">신규</span></c:when>
              <c:when test="${r.cntYoyPct > 0}"><span class="up">+<c:out value="${r.cntYoyPct}"/>%</span></c:when>
              <c:when test="${r.cntYoyPct < 0}"><span class="dn"><c:out value="${r.cntYoyPct}"/>%</span></c:when>
              <c:otherwise><span class="flat">0%</span></c:otherwise>
            </c:choose>
          </td>
          <td class="num">
            <c:choose>
              <c:when test="${r.prevNetMan == 0}"><span class="flat">신규</span></c:when>
              <c:when test="${r.netYoyPct > 0}"><span class="up">+<c:out value="${r.netYoyPct}"/>%</span></c:when>
              <c:when test="${r.netYoyPct < 0}"><span class="dn"><c:out value="${r.netYoyPct}"/>%</span></c:when>
              <c:otherwise><span class="flat">0%</span></c:otherwise>
            </c:choose>
          </td>
          <td class="num">
            <c:choose>
              <c:when test="${r.prevAvgMan == 0}"><span class="flat">신규</span></c:when>
              <c:when test="${r.avgYoyPct > 0}"><span class="up">+<c:out value="${r.avgYoyPct}"/>%</span></c:when>
              <c:when test="${r.avgYoyPct < 0}"><span class="dn"><c:out value="${r.avgYoyPct}"/>%</span></c:when>
              <c:otherwise><span class="flat">0%</span></c:otherwise>
            </c:choose>
          </td>
          <td class="now">
            <c:choose>
              <c:when test="${r.netYoyPct <= 0}">감소</c:when>
              <c:when test="${r.avgYoyPct >= r.cntYoyPct}">단가</c:when>
              <c:otherwise>건수</c:otherwise>
            </c:choose>
          </td>
        </tr>
      </c:forEach>
    </tbody>
  </table>
</div>

<div class="sec">
  <h2>처리량과 리드타임</h2>
  <p class="def">
    <b>리드타임</b> = 시험 등록일 &rarr; 프로젝트완료일 평균 일수.
    평균만 보면 꼬리가 안 보여서 <b>60일 초과</b> 완료 건수를 같이 뒀다.
    리드타임이 줄면 같은 설비로 더 많이 돌린다는 뜻이다.
  </p>
  <table class="grid">
    <thead>
      <tr>
        <th>시험부</th>
        <th>올해 완료</th>
        <th>전년동기 완료</th>
        <th>리드타임</th>
        <th>전년동기</th>
        <th>증감</th>
        <th>60일 초과</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="r" items="${pnl.rows}">
        <c:if test="${r.curDoneCnt > 0 || r.prevDoneCnt > 0}">
          <tr>
            <td class="now"><b><c:out value="${r.deptName}"/></b></td>
            <td class="num"><c:out value="${r.curDoneCnt}"/></td>
            <td class="sub"><c:out value="${r.prevDoneCnt}"/></td>
            <td class="num"><c:out value="${r.curLeadDays}"/>일</td>
            <td class="sub"><c:out value="${r.prevLeadDays}"/>일</td>
            <td class="num">
              <c:choose>
                <c:when test="${r.leadDeltaDays > 0}"><span class="dn">+<c:out value="${r.leadDeltaDays}"/>일</span></c:when>
                <c:when test="${r.leadDeltaDays < 0}"><span class="up"><c:out value="${r.leadDeltaDays}"/>일</span></c:when>
                <c:otherwise><span class="flat">0</span></c:otherwise>
              </c:choose>
            </td>
            <td class="sub"><c:out value="${r.curOver60Cnt}"/>건 (<c:out value="${r.over60Pct}"/>%)</td>
          </tr>
        </c:if>
      </c:forEach>
    </tbody>
  </table>
</div>

<div class="sec">
  <h2>지금 묶여 있는 물량</h2>
  <p class="def">
    완료·시험취소·시험미필요를 뺀 진행 중 시험.
    <b>막힌 것</b> = 디버깅·홀딩·RD지연 (영업 주간 화면의 정의와 같다).
    <b>90일 이상</b>은 등록 후 90일이 지났는데 아직 안 끝난 건수다.
  </p>
  <table class="grid">
    <thead>
      <tr>
        <th>시험부</th>
        <th>진행 건수</th>
        <th>묶인 금액</th>
        <th>평균 경과일</th>
        <th>막힌 건수</th>
        <th>막힌 금액</th>
        <th>90일 이상</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="r" items="${pnl.rows}">
        <c:if test="${r.wipCnt > 0}">
          <tr>
            <td class="now">
              <b><c:out value="${r.deptName}"/></b>
              <a href="<c:url value='/page/stuck.do'><c:param name='testTypeCode' value='${r.deptCode}'/></c:url>"
                 style="font-size:13px;margin-left:6px;">적체</a>
            </td>
            <td class="num"><c:out value="${r.wipCnt}"/></td>
            <td class="num"><c:out value="${r.wipEok}"/></td>
            <td class="num">
              <c:choose>
                <c:when test="${r.wipAvgAge >= 90}"><span class="dn"><c:out value="${r.wipAvgAge}"/>일</span></c:when>
                <c:otherwise><c:out value="${r.wipAvgAge}"/>일</c:otherwise>
              </c:choose>
            </td>
            <td class="sub"><c:out value="${r.stuckCnt}"/></td>
            <td class="num"><c:out value="${r.stuckEok}"/></td>
            <td class="sub"><c:out value="${r.aged90Cnt}"/></td>
          </tr>
        </c:if>
      </c:forEach>
    </tbody>
    <tfoot>
      <tr>
        <td class="now">합계</td>
        <td class="num"><c:out value="${pnl.totalWipCnt}"/></td>
        <td class="num"><c:out value="${pnl.totalWipEok}"/></td>
        <td class="num">&mdash;</td>
        <td class="num"><c:out value="${pnl.totalStuckCnt}"/></td>
        <td class="num"><c:out value="${pnl.totalStuckEok}"/></td>
        <td class="num">&mdash;</td>
      </tr>
    </tfoot>
  </table>
</div>

</c:otherwise>
</c:choose>

<div class="foot">
  수주는 견적 발행일 기준이라 영업 주간 화면의 실적(완납 기준)과 숫자가 다르다.
  같은 시험을 견적 시점에 잡는 것과 돈 들어온 시점에 잡는 것의 차이다.<br/>
  금액은 순매출(청구액에서 대납분·특별할인·컨설팅비·외주비를 뺀 것) 기준. 삭제 자료는 모두 제외.
</div>

</div>
</body>
</html>
