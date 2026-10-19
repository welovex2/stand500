<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="utf-8"/>
<title>완료 후 매출 미확정</title>
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
#bar button, #bar a.btn { font:15px "맑은 고딕", Malgun Gothic, sans-serif; padding:6px 10px;
  cursor:pointer; text-decoration:none; border:1px solid #97a3b4; background:#fff; color:#1a3d73;
  display:inline-block; }
#bar a.btn.on { background:#2b4c7e; color:#fff; border-color:#2b4c7e; font-weight:bold; }
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
.card.good .v { color:#1d6f42; }
table.grid { border-collapse:collapse; width:100%; font-size:15px; }
table.grid th, table.grid td { border:1px solid #b8c0cc; padding:6px 8px; vertical-align:middle; }
table.grid th { background:#eef2f7; text-align:center; }
table.grid td.num { text-align:right; font-weight:bold; white-space:nowrap; }
table.grid td.sub { text-align:right; font-weight:normal; white-space:nowrap; }
table.grid td.now { white-space:nowrap; }
table.grid td.small { font-size:13px; color:#444; }
table.grid tfoot td { background:#f7f9fb; font-weight:bold; }
table.grid tr.hot td { background:#fdf3f3; }
table.grid td.warn { color:#9b1c1c; font-weight:bold; }
table.list { font-size:13px; }
table.list th, table.list td { padding:4px 6px; }
.two { display:table; width:100%; table-layout:fixed; border-spacing:14px 0; margin-left:-14px; }
.two > div { display:table-cell; vertical-align:top; }
.tag { display:inline-block; padding:1px 5px; font-size:12px; font-weight:bold; }
.tag.old { background:#fde8e8; color:#9b1c1c; }
.tag.good { background:#e6f4ea; color:#1d6f42; }
.tag.info { background:#eef2f7; color:#1a3d73; }
.dn { color:#c00; font-weight:bold; }
.flat { color:#555; }
.copy_bt { font:14px "맑은 고딕", Malgun Gothic, sans-serif; padding:4px 10px; margin-left:10px;
  cursor:pointer; border:1px solid #97a3b4; background:#fff; color:#1a3d73; vertical-align:middle; }
.copy_bt:hover { background:#eef2f7; }
.copy_msg { font-size:13px; color:#1d6f42; margin-left:8px; vertical-align:middle; }
.empty { color:#666; padding:10px; text-align:center; }
.foot { margin:16px 18px 24px; color:#555; font-size:13px; }
@media print {
  body { background:#fff; }
  #wrap { width:auto; box-shadow:none; }
  #bar { display:none !important; }
  #top a { color:#fff; }
  .sec { margin:10px 0 0; }
  table.list { font-size:11px; }
  table.list th, table.list td { padding:2px 4px; }
  tr { page-break-inside:avoid; }
  table.list a { color:#111; text-decoration:none; }
  .copy_bt, .copy_msg { display:none !important; }
}
</style>
</head>
<body>
<div id="wrap">
<div id="top">
  <h1>완료 후 매출 미확정</h1>
  <span class="who">
    <a href="<c:url value='/page/list.do'/>" style="color:#fff;margin-right:12px;">화면 목록</a>
    <c:out value="${userName}"/> / <c:out value="${ub.asOfDate}"/>
  </span>
</div>

<div id="bar">
  <div class="note">
    <c:out value="${ub.fromYear}"/>년 이후 완료분 ·
    명단 기준 &mdash; 완료 후 <b><c:out value="${ub.minDays}"/>일</b> 이상 경과분
  </div>
  <div class="btns">
    <a class="btn <c:if test='${ub.minDays == 0}'>on</c:if>" href="<c:url value='/page/unbilled.do'><c:param name='minDays' value='0'/></c:url>">전체</a>
    <a class="btn <c:if test='${ub.minDays == 30}'>on</c:if>" href="<c:url value='/page/unbilled.do'><c:param name='minDays' value='30'/></c:url>">30일+</a>
    <a class="btn <c:if test='${ub.minDays == 90}'>on</c:if>" href="<c:url value='/page/unbilled.do'><c:param name='minDays' value='90'/></c:url>">90일+</a>
    <a class="btn <c:if test='${ub.minDays == 365}'>on</c:if>" href="<c:url value='/page/unbilled.do'><c:param name='minDays' value='365'/></c:url>">365일+</a>
    <button type="button" onclick="window.print()">인쇄</button>
  </div>
</div>

<c:choose>
<c:when test="${ub.quoCnt == 0}">
  <div class="sec"><div class="empty">완료 후 매출 미확정 건이 없습니다.</div></div>
</c:when>
<c:otherwise>

<div class="sec">
  <h2>전체 규모</h2>
  <p class="def">
    시험이 <b>전부</b> 끝났는데 <b>매출확정조차 안 된</b> 견적.
    같은 견적에 진행 중인 시험이 남아 있으면 아직 확정할 수 없으므로 제외했고,
    매출확정까지 간 건은 매출 리스트에서 조회되므로 여기서 뺐다.
    금액은 <b>순매출</b>(대납분·할인·컨설팅비·외주비를 뺀 것)과
    <b>청구액</b>(부가세 포함 견적 총액) 둘 다 적었다.
    <c:if test="${ub.excludedCnt > 0 || ub.oldCnt > 0}">
      <br/>뺀 건 &mdash;
      <c:if test="${ub.excludedCnt > 0}">매출확정됨 <b><c:out value="${ub.excludedCnt}"/>건
        (<c:out value="${ub.excludedEok}"/>)</b></c:if><c:if test="${ub.excludedCnt > 0 && ub.oldCnt > 0}">,</c:if>
      <c:if test="${ub.oldCnt > 0}"><c:out value="${ub.fromYear - 1}"/>년 이전 완료
        <b><c:out value="${ub.oldCnt}"/>건 (<c:out value="${ub.oldEok}"/>)</b></c:if>.
    </c:if>
  </p>
  <div class="cards">
    <div class="card">
      <span class="k">매출 미확정 견적</span>
      <span class="v"><c:out value="${ub.quoCnt}"/>건</span>
      <span class="s">시험 <c:out value="${ub.testCnt}"/>건</span>
    </div>
    <div class="card">
      <span class="k">순매출</span>
      <span class="v"><c:out value="${ub.netEok}"/></span>
      <span class="s">청구액 <c:out value="${ub.vatEok}"/></span>
    </div>
    <div class="card bad">
      <span class="k">90일 이상 경과</span>
      <span class="v"><c:out value="${ub.d90Eok}"/></span>
      <span class="s"><c:out value="${ub.d90Cnt}"/>건</span>
    </div>
    <div class="card bad">
      <span class="k">365일 이상 경과</span>
      <span class="v"><c:out value="${ub.d365Eok}"/></span>
      <span class="s"><c:out value="${ub.d365Cnt}"/>건 · 원인 확인 필요</span>
    </div>
    <div class="card warn">
      <span class="k">시험상태 확인 필요</span>
      <span class="v"><c:out value="${ub.checkCnt}"/>건</span>
      <span class="s"><c:out value="${ub.checkEok}"/> · 취소 섞임·성적서 없음</span>
    </div>
  </div>
</div>

<div class="sec">
  <h2>1. 완료 후 경과일별</h2>
  <p class="def">
    마지막 시험이 끝난 날부터 오늘까지. 30일 미만은 정상 진행 중일 수 있고,
    그 위부터가 확인 대상이다.
  </p>
  <table class="grid">
    <thead>
      <tr>
        <th>경과일</th>
        <th>견적</th>
        <th>시험</th>
        <th>순매출</th>
        <th>청구액</th>
        <th>비중</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="b" items="${ub.buckets}">
        <tr<c:if test="${b.label == '365일 이상'}"> class="hot"</c:if>>
          <td class="now"><b><c:out value="${b.label}"/></b></td>
          <td class="num"><c:out value="${b.quoCnt}"/></td>
          <td class="sub"><c:out value="${b.testCnt}"/></td>
          <td class="num"><c:out value="${b.netEok}"/></td>
          <td class="sub"><c:out value="${b.vatMan}"/>만</td>
          <td class="sub"><c:out value="${b.sharePct}"/>%</td>
        </tr>
      </c:forEach>
    </tbody>
    <tfoot>
      <tr>
        <td class="now">합계</td>
        <td class="num"><c:out value="${ub.quoCnt}"/></td>
        <td class="num"><c:out value="${ub.testCnt}"/></td>
        <td class="num"><c:out value="${ub.netEok}"/></td>
        <td class="num"><c:out value="${ub.vatMan}"/>만</td>
        <td class="num">100%</td>
      </tr>
    </tfoot>
  </table>
</div>

<div class="sec">
  <h2>2. 완료 연도별</h2>
  <p class="def">
    영업 주간 화면은 올해 완료분만 세기 때문에 작년 이전 건이 안 보인다. 여기에는 전부 넣었다.
  </p>
  <table class="grid">
    <thead>
      <tr>
        <th>완료 연도</th>
        <th>견적</th>
        <th>시험</th>
        <th>순매출</th>
        <th>청구액</th>
        <th>비중</th>
      </tr>
    </thead>
    <tbody>
      <c:forEach var="b" items="${ub.years}">
        <tr>
          <td class="now"><b><c:out value="${b.label}"/></b></td>
          <td class="num"><c:out value="${b.quoCnt}"/></td>
          <td class="sub"><c:out value="${b.testCnt}"/></td>
          <td class="num"><c:out value="${b.netEok}"/></td>
          <td class="sub"><c:out value="${b.vatMan}"/>만</td>
          <td class="sub"><c:out value="${b.sharePct}"/>%</td>
        </tr>
      </c:forEach>
    </tbody>
  </table>
</div>

<div class="sec">
  <div class="two">
    <div>
      <h2>3. 시험부별</h2>
      <p class="def">한 견적이 여러 시험부에 걸치므로 금액은 시험 단위로 나눠 담았다.</p>
      <table class="grid">
        <thead>
          <tr><th>시험부</th><th>시험</th><th>순매출</th><th>비중</th></tr>
        </thead>
        <tbody>
          <c:forEach var="g" items="${ub.depts}">
            <tr>
              <td class="now"><b><c:out value="${g.name}"/></b></td>
              <td class="num"><c:out value="${g.testCnt}"/></td>
              <td class="num"><c:out value="${g.netEok}"/></td>
              <td class="sub"><c:out value="${g.sharePct}"/>%</td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
    <div>
      <h2>4. 담당자별</h2>
      <p class="def">
        가장 오래 밀린 건의 경과일을 같이 뒀다.
        <b>퇴사</b> 표시가 붙은 줄은 챙길 담당자가 없는 건이라 따로 배정해야 한다.
      </p>
      <table class="grid">
        <thead>
          <tr><th>담당</th><th>견적</th><th>순매출</th><th>비중</th><th>최장 경과</th></tr>
        </thead>
        <tbody>
          <c:forEach var="g" items="${ub.owners}">
            <tr<c:if test="${g.quit}"> class="hot"</c:if>>
              <td class="now"><b><c:out value="${g.name}"/></b><c:if test="${g.quit}"> <span class="tag old">퇴사</span></c:if></td>
              <td class="num"><c:out value="${g.quoCnt}"/></td>
              <td class="num"><c:out value="${g.netEok}"/></td>
              <td class="sub"><c:out value="${g.sharePct}"/>%</td>
              <td class="num">
                <c:choose>
                  <c:when test="${g.maxDays >= 365}"><span class="dn"><c:out value="${g.maxDays}"/>일</span></c:when>
                  <c:otherwise><c:out value="${g.maxDays}"/>일</c:otherwise>
                </c:choose>
              </td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
  </div>
</div>

<div class="sec">
  <h2>5. 고객별 상위 20</h2>
  <p class="def">금액 큰 순. 한 고객에 여러 건이 몰려 있으면 한 번에 정리할 수 있다.</p>
  <table class="grid">
    <thead>
      <tr><th>고객</th><th>견적</th><th>시험</th><th>순매출</th><th>비중</th><th>최장 경과</th></tr>
    </thead>
    <tbody>
      <c:forEach var="g" items="${ub.custs}">
        <tr>
          <td><c:out value="${g.name}"/></td>
          <td class="num"><c:out value="${g.quoCnt}"/></td>
          <td class="sub"><c:out value="${g.testCnt}"/></td>
          <td class="num"><c:out value="${g.netEok}"/></td>
          <td class="sub"><c:out value="${g.sharePct}"/>%</td>
          <td class="num">
            <c:choose>
              <c:when test="${g.maxDays >= 365}"><span class="dn"><c:out value="${g.maxDays}"/>일</span></c:when>
              <c:otherwise><c:out value="${g.maxDays}"/>일</c:otherwise>
            </c:choose>
          </td>
        </tr>
      </c:forEach>
    </tbody>
  </table>
</div>

<div class="sec">
  <h2>6. 명단 &mdash; 완료 후 <c:out value="${ub.minDays}"/>일 이상 매출 미확정
    <button type="button" id="copyList" class="copy_bt">노션용 표 복사</button>
    <span id="copyMsg" class="copy_msg"></span>
  </h2>
  <p class="def">
    <c:out value="${ub.listQuoCnt}"/>건 · 순매출 <c:out value="${ub.listNetEok}"/>. 경과일 긴 순.
    <b>취</b> 표시는 취합견적이라 묶어서 청구되는 건이다.
    <b>시험상태</b>는 그 견적에 딸린 시험을 상태별로 센 것이다.
    "완료 N"만 있으면 정상이고, <b>취소</b>가 섞였거나 <b>성적서 없음</b>이 붙은 줄은
    정말 끝난 건인지 눈으로 확인해야 한다.
    <b>견적번호를 누르면</b> 견적 리스트가 새 탭에서 그 견적 한 건만 걸러 열린다.
  </p>
  <c:choose>
    <c:when test="${empty ub.list}"><div class="empty">해당 건이 없습니다.</div></c:when>
    <c:otherwise>
      <table class="grid list" id="listTable">
        <thead>
          <tr>
            <th>#</th>
            <th>견적번호</th>
            <th>고객</th>
            <th>담당</th>
            <th>제품</th>
            <th>시험부</th>
            <th>시험상태</th>
            <th>순매출<br/>(만원)</th>
            <th>청구액<br/>(만원)</th>
            <th>견적일</th>
            <th>완료일</th>
            <th>경과</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="r" items="${ub.list}" varStatus="st">
            <tr<c:if test="${r.days >= 365}"> class="hot"</c:if>>
              <td class="sub"><c:out value="${st.count}"/></td>
              <td class="now">
                <c:choose>
                  <c:when test="${empty r.listUrl}"><c:out value="${r.quoId}"/></c:when>
                  <c:otherwise>
                    <a href="https://www.stand500.com${r.listUrl}" target="_blank" rel="noopener"
                       title="견적 리스트에서 이 견적 보기"><c:out value="${r.quoId}"/></a>
                  </c:otherwise>
                </c:choose>
                <c:if test="${r.chq}"> <span class="tag info">취</span></c:if>
              </td>
              <td><c:out value="${r.cmpyName}"/></td>
              <td class="now"><c:out value="${r.mngName}"/><c:if test="${r.mngQuit}"> <span class="tag old">퇴사</span></c:if></td>
              <td class="small"><c:out value="${r.prdctName}"/></td>
              <td class="small"><c:out value="${r.deptNames}"/></td>
              <td class="small<c:if test="${r.stateWarn}"> warn</c:if>">
                <c:out value="${r.testStateText}"/>
              </td>
              <td class="num"><c:out value="${r.netMan}"/></td>
              <td class="sub"><c:out value="${r.vatMan}"/></td>
              <td class="now"><c:out value="${r.issueDt}"/></td>
              <td class="now"><c:out value="${r.doneDt}"/></td>
              <td class="num">
                <c:choose>
                  <c:when test="${r.days >= 365}"><span class="dn"><c:out value="${r.days}"/></span></c:when>
                  <c:otherwise><c:out value="${r.days}"/></c:otherwise>
                </c:choose>
              </td>
            </tr>
          </c:forEach>
        </tbody>
        <tfoot>
          <tr>
            <td colspan="7">합계 <c:out value="${ub.listQuoCnt}"/>건</td>
            <td class="num"><c:out value="${ub.listNetMan}"/></td>
            <td colspan="4"><c:out value="${ub.listNetEok}"/></td>
          </tr>
        </tfoot>
      </table>
    </c:otherwise>
  </c:choose>
</div>

</c:otherwise>
</c:choose>

<div class="foot">
  <b>어떤 건을 담았나</b> &mdash; 견적 총액(부가세 포함)이 0원보다 크고,
  그 견적에 딸린 시험이 전부 끝났으며(완료·시험취소·시험미필요 외 남은 것이 없음),
  그 견적 또는 그 견적이 속한 취합견적에 <b>매출확정(SLS_TB)이 하나도 없는</b> 건.
  매출확정까지 간 건은 매출 리스트에서 조회되므로 뺐다.<br/>
  <b>영업 주간 화면과 숫자가 다른 이유</b> &mdash; 그 화면은 올해 완료분만 세고, 시험 한 건만 끝나도 미청구로 세며,
  매출확정된 건도 계산서만 없으면 함께 센다.
  이 표는 연도 제한을 두지 않고, 진행 중인 시험이 남은 견적과 매출확정된 견적을 뺐다.<br/>
  <b>시험상태 읽는 법</b> &mdash; 목록에 실리는 시험은 상태가 프로젝트완료(18)인 것뿐이라
  줄만 봐서는 그 견적이 정말 다 끝난 것인지, 형제 시험이 취소돼서 끝난 것처럼 보이는지 알 수 없다.
  그래서 그 견적의 시험을 상태별로 세어 함께 적었다.
  <b>성적서 없음</b>은 완료라고 돼 있는데 성적서 발급일도 발급번호도 없는 시험이다.<br/>
  <b><c:out value="${ub.fromYear - 1}"/>년 이전 제외</b> &mdash; ERP 를 막 쓰기 시작한 때라
  취소·무상 처리 뒤 상태만 안 바뀐 건이 섞여 있어 미확정으로 읽으면 잘못된 보고가 된다.<br/>
  <b>견적번호 링크</b> &mdash; 견적 리스트 화면(<code>/html/estimate_list.html</code>)에
  견적서번호 검색(code=5)과 작성일 구간(date=15)을 붙여 연다.
  작성일 구간을 넘기지 않으면 그 화면이 최근 90일을 강제로 걸어 오래된 견적이 안 보이기 때문에,
  시작일을 사실상 제한 없이 넘긴다.<br/>
  <b>확인이 필요한 것</b> &mdash; 경과가 아주 오래된 건에는 실제 미청구가 아니라
  취소·무상 처리된 뒤 상태가 정리되지 않은 건이 섞여 있을 수 있다. 명단에서 사유를 확인해야 한다.
</div>

</div>
<script>
/*
  명단 표만 노션으로 옮기는 복사 버튼.

  노션은 클립보드의 text/html 에 표가 들어 있으면 그대로 노션 표로 바꿔 준다.
  그래서 화면 표를 그대로 긁지 않고 노션이 알아듣는 모양으로 다시 만든다.
  화면 표에는 노션에서 깨지는 것이 둘 있다.
    - 합계행의 셀 병합(colspan). 노션 표는 병합을 모른다. 값만 채운 보통 줄로 바꾼다.
    - 견적번호 링크가 /html/... 상대경로다. 노션에서는 눌러도 안 열리므로 절대주소로 바꾼다.
      (a.href 속성을 읽으면 브라우저가 절대주소로 돌려준다.)
  붙여넣는 곳이 표를 못 받을 때를 대비해 text/plain 에 탭 구분 값도 같이 넣는다.
*/
(function () {
  var btn = document.getElementById('copyList');
  var msg = document.getElementById('copyMsg');
  var table = document.getElementById('listTable');
  if (!btn || !table) { return; }

  var NET_COL = 7;   // 순매출(만원)
  var VAT_COL = 8;   // 청구액(만원)

  function clean(v) { return (v || '').replace(/\s+/g, ' ').trim(); }

  function esc(v) {
    return clean(v).replace(/&/g, '&amp;').replace(/</g, '&lt;')
                   .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
  }

  function num(v) {
    var n = parseInt(clean(v).replace(/[^0-9-]/g, ''), 10);
    return isNaN(n) ? 0 : n;
  }

  function read() {
    var heads = [];
    var ths = table.querySelectorAll('thead th');
    for (var i = 0; i < ths.length; i++) { heads.push(clean(ths[i].textContent)); }

    var rows = [];
    var netSum = 0;
    var vatSum = 0;
    var trs = table.querySelectorAll('tbody tr');
    for (var r = 0; r < trs.length; r++) {
      var tds = trs[r].children;
      var row = [];
      for (var c = 0; c < tds.length; c++) {
        var a = tds[c].querySelector('a[href]');
        // 견적번호 칸에는 링크 뒤에 '취' 같은 표시가 더 붙는다. 링크는 번호만 감싸고 나머지는 옆에 둔다.
        var full = clean(tds[c].textContent);
        var link = a ? clean(a.textContent) : '';
        var rest = link && full.indexOf(link) === 0 ? clean(full.slice(link.length)) : '';
        row.push({ text: full, href: a ? a.href : '', link: link, rest: rest });
      }
      if (row.length > VAT_COL) {
        netSum += num(row[NET_COL].text);
        vatSum += num(row[VAT_COL].text);
      }
      rows.push(row);
    }

    // 합계는 셀 병합 없이 보통 줄로 덧붙인다.
    if (rows.length > 0) {
      var last = [];
      for (var k = 0; k < heads.length; k++) { last.push({ text: '', href: '', link: '', rest: '' }); }
      last[0] = { text: '합계 ' + rows.length + '건', href: '', link: '', rest: '' };
      if (heads.length > VAT_COL) {
        last[NET_COL] = { text: String(netSum), href: '', link: '', rest: '' };
        last[VAT_COL] = { text: String(vatSum), href: '', link: '', rest: '' };
      }
      rows.push(last);
    }
    return { heads: heads, rows: rows };
  }

  function toHtml(d) {
    var h = '<table><thead><tr>';
    for (var i = 0; i < d.heads.length; i++) { h += '<th>' + esc(d.heads[i]) + '</th>'; }
    h += '</tr></thead><tbody>';
    for (var r = 0; r < d.rows.length; r++) {
      h += '<tr>';
      for (var c = 0; c < d.rows[r].length; c++) {
        var cell = d.rows[r][c];
        if (cell.href) {
          h += '<td><a href="' + esc(cell.href) + '">' + esc(cell.link || cell.text) + '</a>'
             + (cell.rest ? ' ' + esc(cell.rest) : '') + '</td>';
        } else {
          h += '<td>' + esc(cell.text) + '</td>';
        }
      }
      h += '</tr>';
    }
    return h + '</tbody></table>';
  }

  function toText(d) {
    var lines = [d.heads.join('\t')];
    for (var r = 0; r < d.rows.length; r++) {
      var vals = [];
      for (var c = 0; c < d.rows[r].length; c++) { vals.push(d.rows[r][c].text); }
      lines.push(vals.join('\t'));
    }
    return lines.join('\n');
  }

  /* 클립보드 API 가 막힌 환경(구형 브라우저, http 로 접속)에서 쓰는 옛 방식. */
  function fallback(html) {
    var box = document.createElement('div');
    box.setAttribute('contenteditable', 'true');
    box.style.position = 'fixed';
    box.style.left = '-9999px';
    box.style.top = '0';
    box.innerHTML = html;
    document.body.appendChild(box);
    var range = document.createRange();
    range.selectNodeContents(box);
    var sel = window.getSelection();
    sel.removeAllRanges();
    sel.addRange(range);
    var ok = false;
    try { ok = document.execCommand('copy'); } catch (e) { ok = false; }
    sel.removeAllRanges();
    document.body.removeChild(box);
    return ok;
  }

  function done(rowCnt) {
    msg.style.color = '#1d6f42';
    msg.textContent = rowCnt + '줄 복사됨 · 노션에 붙여넣기';
    setTimeout(function () { msg.textContent = ''; }, 4000);
  }

  function fail() {
    msg.style.color = '#c00';
    msg.textContent = '복사 실패 — 표를 직접 끌어서 선택해 주세요';
    setTimeout(function () { msg.textContent = ''; }, 6000);
  }

  btn.addEventListener('click', function () {
    var d = read();
    if (d.rows.length === 0) { fail(); return; }
    var html = toHtml(d);
    var text = toText(d);
    var cnt = d.rows.length - 1;

    if (navigator.clipboard && window.ClipboardItem && window.Blob) {
      try {
        var item = new ClipboardItem({
          'text/html': new Blob([html], { type: 'text/html' }),
          'text/plain': new Blob([text], { type: 'text/plain' })
        });
        navigator.clipboard.write([item]).then(function () {
          done(cnt);
        }, function () {
          if (fallback(html)) { done(cnt); } else { fail(); }
        });
        return;
      } catch (e) { /* 아래 옛 방식으로 넘어간다 */ }
    }
    if (fallback(html)) { done(cnt); } else { fail(); }
  });
})();
</script>
</body>
</html>
