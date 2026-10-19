<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="utf-8"/>
<title>담당자별 포트폴리오</title>
<style>
body { margin:0; background:#e9edf2; color:#111; font:16px/1.45 "맑은 고딕", Malgun Gothic, sans-serif; }
a { color:#1a3d73; }
#wrap { width:1180px; margin:0 auto; background:#fff; min-height:100vh; box-shadow:0 0 0 1px #c5ccd6; }
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
table.grid tfoot td { background:#f7f9fb; font-weight:bold; }
table.mix { border-collapse:collapse; width:100%; font-size:14px; }
table.mix th, table.mix td { border:1px solid #b8c0cc; padding:5px 6px; text-align:center; }
table.mix th { background:#eef2f7; }
table.mix td.nm { text-align:left; white-space:nowrap; font-weight:bold; }
table.mix td.z { color:#bbb; }
table.mix td.s1 { background:#f4f7fb; }
table.mix td.s2 { background:#e3ebf5; }
table.mix td.s3 { background:#cbdaec; font-weight:bold; }
table.mix td.s4 { background:#a9c2e0; font-weight:bold; }
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
  <h1>담당자별 포트폴리오</h1>
  <span class="who">
    <a href="<c:url value='/page/list.do'/>" style="color:#fff;margin-right:12px;">화면 목록</a>
    <c:out value="${userName}"/> / <c:out value="${mix.asOfDate}"/>
  </span>
</div>

<div id="bar">
  <div class="note">
    수주(견적 발행) 기준 · <c:out value="${mix.year}"/>년 1월 1일 ~ 오늘 vs
    <c:out value="${mix.prevYear}"/>년 같은 기간
  </div>
  <div class="btns">
    <button type="button" onclick="window.print()">인쇄</button>
  </div>
</div>

<c:choose>
<c:when test="${empty mix.rows}">
  <div class="sec"><div class="empty">집계할 담당자 실적이 없습니다.</div></div>
</c:when>
<c:otherwise>

<div class="sec">
  <h2>올해 실적</h2>
  <p class="def">
    <b>건당 단가</b> = 순매출 &divide; 시험 건수. 같은 건수를 처리해도 단가가 다르면 매출이 달라진다.
  </p>
  <div class="cards">
    <div class="card">
      <span class="k">순매출</span>
      <span class="v"><c:out value="${mix.totalCurNetEok}"/></span>
      <span class="s">담당 <c:out value="${mix.ownerCnt}"/>명 · <c:out value="${mix.totalCurCnt}"/>건</span>
    </div>
    <div class="card">
      <span class="k">회사 평균 건당 단가</span>
      <span class="v"><c:out value="${mix.totalCurAvgMan}"/>만</span>
      <span class="s">전체 기준</span>
    </div>
    <div class="card">
      <span class="k">건당 단가 가장 높은 담당</span>
      <span class="v"><c:out value="${mix.topAvgMan}"/>만</span>
      <span class="s"><c:out value="${mix.topAvgName}"/></span>
    </div>
    <div class="card">
      <span class="k">가장 낮은 담당</span>
      <span class="v"><c:out value="${mix.lowAvgMan}"/>만</span>
      <span class="s"><c:out value="${mix.lowAvgName}"/></span>
    </div>
    <div class="card warn">
      <span class="k">단가 차이</span>
      <span class="v"><c:out value="${mix.avgSpreadX}"/>배</span>
      <span class="s">20건 이상 담당자끼리 비교</span>
    </div>
    <c:if test="${mix.goneCnt > 0}">
      <div class="card bad">
        <span class="k">올해 실적이 끊긴 담당</span>
        <span class="v"><c:out value="${mix.goneCnt}"/>명</span>
        <span class="s">전년동기 <c:out value="${mix.goneNetEok}"/> 담당</span>
      </div>
    </c:if>
  </div>
</div>

<div class="sec">
  <h2>담당자별 올해 실적</h2>
  <p class="def">
    <b>직고객 비중</b> = 매출 중 직접고객에서 나온 몫. 낮으면 컨설팅사 물량을 대량으로 받는 자리라는 뜻이고,
    그런 자리는 원래 건당 단가가 낮게 나온다. 그래서 단가만 보고 잘잘못을 가리면 안 된다.<br/>
    <b>의료·SAR·RF 비중</b> = 건당 단가가 높은 세 시험부에서 나온 몫.<br/>
    <b>늘어난 까닭</b> = 매출이 늘었을 때 건수를 더 받아서인지 단가가 올라서인지.
    건수는 장비와 사람을 더 써야 늘지만, 단가는 더 쓰지 않아도 는다.
  </p>
  <table class="grid">
    <thead>
      <tr>
        <th rowspan="2">담당</th>
        <th colspan="5">올해</th>
        <th colspan="2">전년동기</th>
        <th colspan="2">증감</th>
        <th rowspan="2">늘어난<br/>까닭</th>
      </tr>
      <tr>
        <th>건수</th>
        <th>순매출</th>
        <th>건당</th>
        <th>직고객<br/>비중</th>
        <th>의료·SAR<br/>·RF 비중</th>
        <th>순매출</th>
        <th>건당</th>
        <th>순매출</th>
        <th>건당</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="r" items="${mix.rows}">
        <tr>
          <td class="now">
            <b><c:out value="${r.mngName}"/></b>
            <span style="font-size:13px;color:#666;">고객 <c:out value="${r.curCustCnt}"/></span>
          </td>
          <td class="num"><c:out value="${r.curCnt}"/></td>
          <td class="num"><c:out value="${r.curNetEok}"/></td>
          <td class="num"><c:out value="${r.curAvgMan}"/>만</td>
          <td class="sub"><c:out value="${r.directPct}"/>%</td>
          <td class="sub"><c:out value="${r.hiPct}"/>%</td>
          <td class="sub"><c:out value="${r.prevNetMan}"/>만</td>
          <td class="sub"><c:out value="${r.prevAvgMan}"/>만</td>
          <td class="num">
            <c:choose>
              <c:when test="${r.prevNetMan == 0}"><span class="flat">&mdash;</span></c:when>
              <c:when test="${r.netYoyPct > 0}"><span class="up">+<c:out value="${r.netYoyPct}"/>%</span></c:when>
              <c:when test="${r.netYoyPct < 0}"><span class="dn"><c:out value="${r.netYoyPct}"/>%</span></c:when>
              <c:otherwise><span class="flat">0%</span></c:otherwise>
            </c:choose>
          </td>
          <td class="num">
            <c:choose>
              <c:when test="${r.prevAvgMan == 0}"><span class="flat">&mdash;</span></c:when>
              <c:when test="${r.avgYoyPct > 0}"><span class="up">+<c:out value="${r.avgYoyPct}"/>%</span></c:when>
              <c:when test="${r.avgYoyPct < 0}"><span class="dn"><c:out value="${r.avgYoyPct}"/>%</span></c:when>
              <c:otherwise><span class="flat">0%</span></c:otherwise>
            </c:choose>
          </td>
          <td class="now">
            <c:choose>
              <c:when test="${r.growthSrc == '단가'}"><span class="tag good">단가</span></c:when>
              <c:when test="${r.growthSrc == '감소'}"><span class="tag old">감소</span></c:when>
              <c:otherwise><span class="tag info"><c:out value="${r.growthSrc}"/></span></c:otherwise>
            </c:choose>
          </td>
        </tr>
      </c:forEach>
    </tbody>
    <tfoot>
      <tr>
        <td class="now">합계</td>
        <td class="num"><c:out value="${mix.totalCurCnt}"/></td>
        <td class="num"><c:out value="${mix.totalCurNetEok}"/></td>
        <td class="num"><c:out value="${mix.totalCurAvgMan}"/>만</td>
        <td class="num">&mdash;</td>
        <td class="num">&mdash;</td>
        <td class="num">&mdash;</td>
        <td class="num">&mdash;</td>
        <td class="num">&mdash;</td>
        <td class="num">&mdash;</td>
        <td class="num">&mdash;</td>
      </tr>
    </tfoot>
  </table>
</div>

<div class="sec">
  <h2>담당자별 시험부 구성</h2>
  <p class="def">
    칸 안의 값은 그 담당자 올해 매출 중 해당 시험부가 차지한 비중(%)이다. 진한 칸이 그 사람의 주력이다.
    단가가 높은 담당과 낮은 담당의 차이가 어느 시험부에서 오는지 가로로 훑어보면 보인다.
  </p>
  <table class="mix">
    <thead>
      <tr>
        <th style="text-align:left;">담당</th>
        <c:forEach var="col" items="${mix.deptCols}">
          <th><c:out value="${col.name}"/></th>
        </c:forEach>
        <th>건당</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="r" items="${mix.rows}">
        <tr>
          <td class="nm"><c:out value="${r.mngName}"/></td>
          <c:forEach var="s" items="${r.shares}">
            <c:choose>
              <c:when test="${s.sharePct == 0}"><td class="z">&middot;</td></c:when>
              <c:when test="${s.sharePct >= 50}"><td class="s4"><c:out value="${s.sharePct}"/></td></c:when>
              <c:when test="${s.sharePct >= 25}"><td class="s3"><c:out value="${s.sharePct}"/></td></c:when>
              <c:when test="${s.sharePct >= 10}"><td class="s2"><c:out value="${s.sharePct}"/></td></c:when>
              <c:otherwise><td class="s1"><c:out value="${s.sharePct}"/></td></c:otherwise>
            </c:choose>
          </c:forEach>
          <td><b><c:out value="${r.curAvgMan}"/>만</b></td>
        </tr>
      </c:forEach>
    </tbody>
  </table>
</div>

<c:if test="${not empty mix.goneRows}">
<div class="sec">
  <h2>올해 실적이 끊긴 담당 &mdash; 인수인계 확인</h2>
  <p class="def">
    전년동기에는 실적이 있었는데 올해는 한 건도 없는 담당자다.
    담당이 넘어갔다면 문제가 없지만, 그 고객들이 같이 빠져나갔다면 이 숫자가 그대로 매출 손실이다.
    <a href="<c:url value='/page/custladder.do'/>">고객 성장 단계</a> 화면의 조용해진 고객 목록과 겹쳐 보면 확인된다.
  </p>
  <table class="grid">
    <thead>
      <tr>
        <th>담당</th>
        <th>전년동기 건수</th>
        <th>전년동기 순매출</th>
        <th>전년동기 건당</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="r" items="${mix.goneRows}">
        <tr>
          <td class="now"><b><c:out value="${r.mngName}"/></b></td>
          <td class="num"><c:out value="${r.prevCnt}"/></td>
          <td class="num">
            <c:choose>
              <c:when test="${r.prevNetMan >= 10000}"><span class="dn"><c:out value="${r.prevNetMan}"/>만</span></c:when>
              <c:otherwise><c:out value="${r.prevNetMan}"/>만</c:otherwise>
            </c:choose>
          </td>
          <td class="sub"><c:out value="${r.prevAvgMan}"/>만</td>
        </tr>
      </c:forEach>
    </tbody>
    <tfoot>
      <tr>
        <td class="now">합계</td>
        <td class="num">&mdash;</td>
        <td class="num"><c:out value="${mix.goneNetEok}"/></td>
        <td class="num">&mdash;</td>
      </tr>
    </tfoot>
  </table>
</div>
</c:if>

</c:otherwise>
</c:choose>

<div class="foot">
  <b>이 화면을 읽는 법</b> &mdash; 건당 단가가 낮은 담당을 나무라는 화면이 아니다.
  컨설팅사 물량을 대량으로 받는 자리와 직접고객 소량을 받는 자리는 애초에 단가가 다르므로,
  직고객 비중을 같이 보고 자리에서 오는 차이와 구성에서 오는 차이를 갈라야 한다.
  볼 것은 <b>직고객 비중이 비슷한데 단가가 다른 담당</b>이다. 그 차이는 시험부 구성에서 온다.<br/>
  <b>목표 대비는 넣지 않았다</b> &mdash; TARGET_TB 의 목표는 돈이 들어온 시점 기준인데 이 화면은 견적 발행 기준이라
  두 숫자를 나란히 두면 잘못 읽힌다. 목표 진척은 영업 주간 화면에서 본다.<br/>
  <b>매출 기준</b> &mdash; 견적 발행일 기준 순매출(청구액에서 대납분·특별할인·컨설팅비·외주비를 뺀 것). 삭제 자료는 제외.
  담당은 JOB_TB 의 업무담당자다.
</div>

</div>
</body>
</html>
