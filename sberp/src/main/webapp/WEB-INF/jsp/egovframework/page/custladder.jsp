<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="utf-8"/>
<title>고객 성장 단계</title>
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
table.grid td.sub { text-align:right; font-weight:normal; white-space:nowrap; }
table.grid td.now { white-space:nowrap; }
table.grid td.small { font-size:13px; color:#444; }
table.grid tfoot td { background:#f7f9fb; font-weight:bold; }
.up { color:#1d6f42; font-weight:bold; }
.dn { color:#c00; font-weight:bold; }
.flat { color:#555; }
.tag { display:inline-block; padding:1px 6px; font-size:13px; font-weight:bold; }
.tag.old { background:#fde8e8; color:#9b1c1c; }
.tag.good { background:#e6f4ea; color:#1d6f42; }
.tag.info { background:#eef2f7; color:#1a3d73; }
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
  <h1>고객 성장 단계</h1>
  <span class="who">
    <a href="<c:url value='/page/list.do'/>" style="color:#fff;margin-right:12px;">화면 목록</a>
    <c:out value="${userName}"/> / <c:out value="${ladder.asOfDate}"/>
  </span>
</div>

<div id="bar">
  <div class="note">
    거래 이력이 있는 고객 <c:out value="${ladder.custCnt}"/>곳
    (업체 등록 <c:out value="${ladder.rawCnt}"/>줄 중
    <c:out value="${ladder.mergedCnt}"/>곳은 같은 회사가 여러 줄로 등록돼 있어 하나로 합쳤습니다)
  </div>
  <div class="btns">
    <button type="button" onclick="window.print()">인쇄</button>
  </div>
</div>

<c:choose>
<c:when test="${empty ladder.ladder}">
  <div class="sec"><div class="empty">집계할 고객 자료가 없습니다.</div></div>
</c:when>
<c:otherwise>

<div class="sec">
  <h2>올해 매출은 누가 냈나</h2>
  <p class="def">
    <b>기존 고객</b> = 작년 이전에 이미 거래가 있던 곳.
    <b>새 고객</b> = 올해 처음 거래한 곳.
  </p>
  <div class="cards">
    <div class="card">
      <span class="k">올해 매출</span>
      <span class="v"><c:out value="${ladder.netCurEok}"/></span>
      <span class="s">고객 <c:out value="${ladder.existCnt + ladder.newCnt}"/>곳</span>
    </div>
    <div class="card">
      <span class="k">기존 고객이 낸 몫</span>
      <span class="v"><c:out value="${ladder.existSharePct}"/>%</span>
      <span class="s"><c:out value="${ladder.existCnt}"/>곳 · <c:out value="${ladder.existNetEok}"/></span>
    </div>
    <div class="card">
      <span class="k">새 고객이 낸 몫</span>
      <span class="v"><c:out value="${ladder.newNetEok}"/></span>
      <span class="s"><c:out value="${ladder.newCnt}"/>곳 · 새로 데려온 결과</span>
    </div>
    <div class="card warn">
      <span class="k">한 번만 오고 안 온 고객</span>
      <span class="v"><c:out value="${ladder.onceTotalCnt}"/>곳</span>
      <span class="s">지금까지 <c:out value="${ladder.onceTotalEok}"/> 결제</span>
    </div>
    <div class="card warn">
      <span class="k">한 시험부만 쓰는 단골</span>
      <span class="v"><c:out value="${ladder.crossTotalCnt}"/>곳</span>
      <span class="s">지금까지 <c:out value="${ladder.crossTotalEok}"/> 결제</span>
    </div>
    <div class="card bad">
      <span class="k">평소보다 조용해진 고객</span>
      <span class="v"><c:out value="${ladder.quietTotalCnt}"/>곳</span>
      <span class="s">지금까지 <c:out value="${ladder.quietTotalEok}"/> 결제</span>
    </div>
  </div>
</div>

<div class="sec">
  <h2>몇 번 의뢰했는지에 따라</h2>
  <p class="def">
    <b>고객 1곳 평균</b> = 그 구간 고객들이 지금까지 낸 매출 ÷ 고객 수.
    <b>아래 칸 대비</b> = 한 단계 아래 구간과 비교해 몇 배인가.
    한 번 더 오게 만드는 것만으로 고객 1곳이 내는 돈이 얼마나 달라지는지 보는 표다.
  </p>
  <table class="grid">
    <thead>
      <tr>
        <th>의뢰 횟수</th>
        <th>고객 수</th>
        <th>지금까지 낸 매출</th>
        <th>고객 1곳 평균</th>
        <th>아래 칸 대비</th>
        <th>올해 매출</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="r" items="${ladder.ladder}">
        <tr>
          <td class="now"><b><c:out value="${r.label}"/></b></td>
          <td class="num"><c:out value="${r.custCnt}"/>곳</td>
          <td class="num"><c:out value="${r.netAllEok}"/></td>
          <td class="num"><c:out value="${r.perCustMan}"/>만</td>
          <td class="num">
            <c:choose>
              <c:when test="${r.stepUpX > 1}"><span class="tag good"><c:out value="${r.stepUpX}"/>배</span></c:when>
              <c:when test="${r.stepUpX > 0}"><span class="flat"><c:out value="${r.stepUpX}"/>배</span></c:when>
              <c:otherwise><span class="flat">&mdash;</span></c:otherwise>
            </c:choose>
          </td>
          <td class="sub"><c:out value="${r.netCurMan}"/>만</td>
        </tr>
      </c:forEach>
    </tbody>
  </table>
</div>

<div class="sec">
  <h2>몇 개 시험부를 쓰는지에 따라</h2>
  <p class="def">
    이미 거래하는 고객에게 다른 시험부 시험을 붙였을 때 고객 1곳이 내는 돈이 어떻게 달라지는지 보는 표다.
    새 고객을 찾는 것보다 여기가 확률이 높다.
  </p>
  <table class="grid">
    <thead>
      <tr>
        <th>쓰는 시험부</th>
        <th>고객 수</th>
        <th>지금까지 낸 매출</th>
        <th>고객 1곳 평균</th>
        <th>아래 칸 대비</th>
        <th>올해 매출</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="r" items="${ladder.breadth}">
        <tr>
          <td class="now"><b><c:out value="${r.label}"/></b></td>
          <td class="num"><c:out value="${r.custCnt}"/>곳</td>
          <td class="num"><c:out value="${r.netAllEok}"/></td>
          <td class="num"><c:out value="${r.perCustMan}"/>만</td>
          <td class="num">
            <c:choose>
              <c:when test="${r.stepUpX > 1}"><span class="tag good"><c:out value="${r.stepUpX}"/>배</span></c:when>
              <c:when test="${r.stepUpX > 0}"><span class="flat"><c:out value="${r.stepUpX}"/>배</span></c:when>
              <c:otherwise><span class="flat">&mdash;</span></c:otherwise>
            </c:choose>
          </td>
          <td class="sub"><c:out value="${r.netCurMan}"/>만</td>
        </tr>
      </c:forEach>
    </tbody>
  </table>
</div>

<div class="sec">
  <h2>연락 대상 1 &middot; 한 번만 오고 안 온 고객</h2>
  <p class="def">
    딱 한 번 의뢰하고 30일 넘게 안 온 곳. 금액 큰 순 <c:out value="${ladder.onceTotalCnt}"/>곳 중 상위 30곳.
    마지막 의뢰가 540일이 넘은 곳은 사실상 남이라 뺐다.
  </p>
  <c:choose>
    <c:when test="${empty ladder.onceList}"><div class="empty">해당 고객이 없습니다.</div></c:when>
    <c:otherwise>
      <table class="grid">
        <thead>
          <tr>
            <th>고객</th>
            <th>담당</th>
            <th>지금까지</th>
            <th>쓴 시험부</th>
            <th>마지막 의뢰</th>
            <th>지난 날</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="r" items="${ladder.onceList}">
            <tr>
              <td><c:out value="${r.cmpyName}"/></td>
              <td class="now"><c:out value="${r.mngName}"/></td>
              <td class="num"><c:out value="${r.netAllMan}"/>만</td>
              <td class="small"><c:out value="${r.usedDepts}"/></td>
              <td class="now"><c:out value="${r.lastDt}"/></td>
              <td class="num">
                <c:choose>
                  <c:when test="${r.days >= 365}"><span class="dn"><c:out value="${r.days}"/>일</span></c:when>
                  <c:otherwise><c:out value="${r.days}"/>일</c:otherwise>
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
  <h2>연락 대상 2 &middot; 한 시험부만 쓰는 단골</h2>
  <p class="def">
    두 번 이상 의뢰했는데 시험부는 하나만 쓰는 곳. 금액 큰 순 <c:out value="${ladder.crossTotalCnt}"/>곳 중 상위 30곳.
    거래는 이미 검증됐으니 남은 건 시험 종류를 넓히는 것이다.
  </p>
  <c:choose>
    <c:when test="${empty ladder.crossList}"><div class="empty">해당 고객이 없습니다.</div></c:when>
    <c:otherwise>
      <table class="grid">
        <thead>
          <tr>
            <th>고객</th>
            <th>담당</th>
            <th>의뢰</th>
            <th>지금까지</th>
            <th>쓰는 시험부</th>
            <th>아직 안 쓴 시험부</th>
            <th>지난 날</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="r" items="${ladder.crossList}">
            <tr>
              <td><c:out value="${r.cmpyName}"/></td>
              <td class="now"><c:out value="${r.mngName}"/></td>
              <td class="num"><c:out value="${r.orders}"/>회</td>
              <td class="num"><c:out value="${r.netAllMan}"/>만</td>
              <td class="now"><span class="tag info"><c:out value="${r.usedDepts}"/></span></td>
              <td class="small"><c:out value="${r.unusedDepts}"/></td>
              <td class="sub"><c:out value="${r.days}"/>일</td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </c:otherwise>
  </c:choose>
</div>

<div class="sec">
  <h2>연락 대상 3 &middot; 평소보다 조용해진 고객</h2>
  <p class="def">
    <b>평소 주기</b> = 첫 의뢰부터 마지막 의뢰까지 기간 &divide; 의뢰 간격 수. 그 고객이 보통 며칠에 한 번 오는지다.<br/>
    <b>몇 배 늦음</b> = 지난 날 &divide; 평소 주기. 2.0이면 평소의 두 배로 늦었다는 뜻.
    평소 20일에 한 번 오던 곳의 40일과, 평소 200일에 한 번 오던 곳의 40일은 완전히 다른 신호라서 배수로 본다.
    30일이 안 지난 곳과 의뢰가 3회 미만인 곳은 판단 근거가 없어 뺐다.
  </p>
  <c:choose>
    <c:when test="${empty ladder.quietList}"><div class="empty">해당 고객이 없습니다.</div></c:when>
    <c:otherwise>
      <table class="grid">
        <thead>
          <tr>
            <th>고객</th>
            <th>담당</th>
            <th>의뢰</th>
            <th>지금까지</th>
            <th>평소 주기</th>
            <th>지난 날</th>
            <th>몇 배 늦음</th>
            <th>마지막 의뢰</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="r" items="${ladder.quietList}">
            <tr>
              <td>
                <c:out value="${r.cmpyName}"/>
                <c:if test="${r.mergedSeqCnt > 1}">
                  <span class="tag info"><c:out value="${r.mergedSeqCnt}"/>줄 합침</span>
                </c:if>
              </td>
              <td class="now"><c:out value="${r.mngName}"/></td>
              <td class="num"><c:out value="${r.orders}"/>회</td>
              <td class="num"><c:out value="${r.netAllMan}"/>만</td>
              <td class="sub"><c:out value="${r.avgGap}"/>일</td>
              <td class="num"><c:out value="${r.days}"/>일</td>
              <td class="num">
                <c:choose>
                  <c:when test="${r.overdue >= 3}"><span class="tag old"><c:out value="${r.overdue}"/>배</span></c:when>
                  <c:otherwise><c:out value="${r.overdue}"/>배</c:otherwise>
                </c:choose>
              </td>
              <td class="now"><c:out value="${r.lastDt}"/></td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </c:otherwise>
  </c:choose>
</div>

</c:otherwise>
</c:choose>

<div class="foot">
  <b>같은 회사 합치기</b> &mdash; 업체가 여러 줄로 등록된 경우가 있어(예: 한 회사가 CMPY_SEQ 셋으로 나뉘어 있음)
  사업자등록번호가 같으면 하나로 봤고, 번호가 없는 줄은 상호를 정리해서 비교했다.
  같은 상호 안에 서로 다른 사업자번호가 섞여 있으면 다른 회사일 수 있어 합치지 않았다.
  합치지 않으면 의뢰 횟수가 낮게 잡혀 단계가 틀리고, 다른 줄로 거래 중인 고객이 조용해진 고객으로 올라온다.<br/>
  <b>매출 기준</b> &mdash; 견적 발행일 기준 순매출(청구액에서 대납분·특별할인·컨설팅비·외주비를 뺀 것). 삭제 자료는 제외.
  영업 주간 화면의 실적은 돈이 들어온 시점 기준이라 숫자가 다르다.<br/>
  <b>안 쓴 시험부</b> &mdash; 대행은 외주를 통과시키는 매출이라 권유 대상에서 뺐다.
</div>

</div>
</body>
</html>
