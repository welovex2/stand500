<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="utf-8"/>
<title>영업 주간</title>
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
.age { display:table; width:100%; border:1px solid #b8c0cc; border-collapse:collapse; table-layout:fixed; }
.age .b { display:table-cell; padding:10px 12px; border-right:1px solid #b8c0cc; text-align:center; }
.age .b:last-child { border-right:0; }
.age .k { display:block; font-size:13px; color:#555; }
.age .v { display:block; font-size:20px; font-weight:bold; margin:4px 0 2px; }
.age .s { display:block; font-size:13px; color:#444; }
.age .hot .v { color:#c00; }
table.grid { border-collapse:collapse; width:100%; font-size:15px; }
table.grid th, table.grid td { border:1px solid #b8c0cc; padding:7px 8px; vertical-align:middle; }
table.grid th { background:#eef2f7; text-align:center; }
table.grid td.num { text-align:right; font-weight:bold; white-space:nowrap; }
table.grid td.now { white-space:nowrap; }
.tag { display:inline-block; padding:1px 6px; font-size:13px; font-weight:bold; }
.tag.old { background:#fde8e8; color:#9b1c1c; }
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
  <h1>영업 주간</h1>
  <span class="who">
    <a href="<c:url value='/page/list.do'/>" style="color:#fff;margin-right:12px;">화면 목록</a>
    <c:out value="${userName}"/> / <c:out value="${brief.asOfDate}"/>
  </span>
</div>

<div id="bar">
  <div class="btns">
    <button type="button" onclick="window.print()">인쇄</button>
  </div>
</div>

<div class="sec">
  <h2><c:out value="${brief.month}"/>월 · 연 실적</h2>
  <p class="def">
    <b>착지</b> = 지금 속도가 이어질 때 월말·연말에 찍힐 예상 실적.
  </p>
  <div class="cards">
    <div class="card">
      <span class="k"><c:out value="${brief.month}"/>월 목표</span>
      <span class="v"><c:out value="${brief.monthTargetEok}"/></span>
      <span class="s">이번 달 목표</span>
    </div>
    <div class="card">
      <span class="k"><c:out value="${brief.month}"/>월 현재</span>
      <span class="v"><c:out value="${brief.monthActualEok}"/></span>
      <span class="s">오늘까지 확정</span>
    </div>
    <div class="card">
      <span class="k"><c:out value="${brief.month}"/>월 착지</span>
      <span class="v"><c:out value="${brief.monthForecastEok}"/></span>
      <span class="s">이 속도면 월말</span>
    </div>
    <div class="card">
      <span class="k">연 목표 / 누적 / 연말 착지</span>
      <span class="v"><c:out value="${brief.yearForecastEok}"/></span>
      <span class="s">목표 <c:out value="${brief.yearTargetEok}"/> · 누적 <c:out value="${brief.yearActualEok}"/></span>
    </div>
  </div>
</div>

<div class="sec">
  <h2>이번 주 현금 · 막힌 일</h2>
  <p class="def">
    <b>완료 후 미청구</b> = 시험은 끝났는데 계산서를 아직 안 낸 건.<br/>
    <b>적체 시험 순매출</b> = 디버깅·홀딩·RD지연으로 멈춰 있는 시험의 순매출 합.
  </p>
  <div class="cards">
    <div class="card">
      <span class="k">이번 주 입금 (<c:out value="${brief.weekStartDate}"/>~)</span>
      <span class="v"><c:out value="${brief.weekPayEok}"/></span>
      <span class="s">회수된 금액</span>
    </div>
    <div class="card warn">
      <span class="k">완료 후 미청구</span>
      <span class="v"><c:out value="${brief.unbilledCnt}"/>건</span>
      <span class="s"><c:out value="${brief.unbilledEok}"/> · 청구하면 매출/미수로 전환</span>
    </div>
    <div class="card">
      <span class="k">적체 시험 순매출</span>
      <span class="v"><c:out value="${brief.stuckEok}"/></span>
      <span class="s"><c:out value="${brief.stuckCnt}"/>건 · 막혀 있는 파이프라인</span>
    </div>
  </div>
</div>

<div class="sec">
  <h2>미수 고령화</h2>
  <p class="def">
    <b>미수</b> = 계산서 냈는데 아직 안 들어온 돈.<br/>
    <b>고령화</b> = 그 미수가 며칠째 안 들어왔는지 구간별로 나눈 것 (계산서일 기준, 없으면 확정일).
    오래될수록 회수가 어렵고 위험하다.
  </p>
  <div class="age">
    <div class="b">
      <span class="k">전체 미수</span>
      <span class="v"><c:out value="${brief.arrearsEok}"/></span>
      <span class="s"><c:out value="${brief.arrearsCnt}"/>건</span>
    </div>
    <div class="b">
      <span class="k">30일 미만</span>
      <span class="v"><c:out value="${brief.arrearsD30Man}"/>만</span>
      <span class="s"><c:out value="${brief.arrearsD30Cnt}"/>건</span>
    </div>
    <div class="b">
      <span class="k">30–59일</span>
      <span class="v"><c:out value="${brief.arrearsD60Man}"/>만</span>
      <span class="s"><c:out value="${brief.arrearsD60Cnt}"/>건</span>
    </div>
    <div class="b">
      <span class="k">60–89일</span>
      <span class="v"><c:out value="${brief.arrearsD90Man}"/>만</span>
      <span class="s"><c:out value="${brief.arrearsD90Cnt}"/>건</span>
    </div>
    <div class="b hot">
      <span class="k">90일+</span>
      <span class="v"><c:out value="${brief.arrearsD90pEok}"/></span>
      <span class="s"><c:out value="${brief.arrearsD90pCnt}"/>건 · 우선 회수</span>
    </div>
  </div>
</div>

<div class="sec">
  <h2>미수 긴 순 15건 — 이번 주 회수 후보</h2>
  <p class="def">경과일 긴 순.</p>
  <c:choose>
    <c:when test="${empty brief.arList}"><div class="empty">미수가 없습니다.</div></c:when>
    <c:otherwise>
      <table class="grid">
        <thead>
          <tr>
            <th>매출번호</th>
            <th>고객</th>
            <th>담당</th>
            <th>미수(만원)</th>
            <th>경과일</th>
            <th>구간</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="row" items="${brief.arList}">
            <tr>
              <td class="now"><c:out value="${row.slsId}"/></td>
              <td><c:out value="${row.cmpyName}"/></td>
              <td class="now"><c:out value="${row.mngName}"/></td>
              <td class="num"><c:out value="${row.arrearsMan}"/></td>
              <td class="num"><c:out value="${row.days}"/></td>
              <td class="now">
                <c:choose>
                  <c:when test="${row.days >= 90}"><span class="tag old"><c:out value="${row.bucket}"/></span></c:when>
                  <c:otherwise><c:out value="${row.bucket}"/></c:otherwise>
                </c:choose>
              </td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </c:otherwise>
  </c:choose>
</div>

<div class="sec">
  <h2>완료 후 미청구</h2>
  <c:choose>
    <c:when test="${empty brief.unbilledList}"><div class="empty">해당 건이 없습니다.</div></c:when>
    <c:otherwise>
      <table class="grid">
        <thead>
          <tr>
            <th>시험번호</th>
            <th>견적</th>
            <th>고객</th>
            <th>담당</th>
            <th>순매출(만원)</th>
            <th>완료일</th>
            <th>경과일</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="row" items="${brief.unbilledList}">
            <tr>
              <td class="now"><c:out value="${row.testId}"/></td>
              <td class="now"><c:out value="${row.quoId}"/></td>
              <td><c:out value="${row.cmpyName}"/></td>
              <td class="now"><c:out value="${row.mngName}"/></td>
              <td class="num"><c:out value="${row.netMan}"/></td>
              <td class="now"><c:out value="${row.endDt}"/></td>
              <td class="num"><c:out value="${row.days}"/></td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </c:otherwise>
  </c:choose>
</div>

<div class="sec">
  <h2>목표 대비 실적 (하위 3)</h2>
  <c:choose>
    <c:when test="${empty brief.redList}"><div class="empty">목표 대비 부족한 담당이 없습니다.</div></c:when>
    <c:otherwise>
      <table class="grid">
        <thead>
          <tr>
            <th>담당</th>
            <th>일할 목표</th>
            <th>완납</th>
            <th>갭</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="row" items="${brief.redList}">
            <tr>
              <td><c:out value="${row.memName}"/></td>
              <td class="num"><c:out value="${row.targetEok}"/></td>
              <td class="num"><c:out value="${row.actualEok}"/></td>
              <td class="num"><c:out value="${row.gapEok}"/></td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </c:otherwise>
  </c:choose>
</div>

</div>
</body>
</html>
