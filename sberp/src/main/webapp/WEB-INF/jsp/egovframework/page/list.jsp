<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="utf-8"/>
<title>화면 목록</title>
<style>
body { margin:0; background:#e9edf2; color:#111; font:16px/1.4 "맑은 고딕", Malgun Gothic, sans-serif; }
a { color:#1a3d73; }
#wrap { width:1200px; margin:0 auto; background:#fff; min-height:100vh; box-shadow:0 0 0 1px #c5ccd6; }
#top { background:#2b4c7e; color:#fff; padding:12px 18px; overflow:hidden; }
#top h1 { margin:0; font-size:22px; display:inline; }
#top .who { float:right; font-size:15px; padding-top:4px; }
.note { margin:16px 18px 8px; color:#444; }
table.grid { border-collapse:collapse; width:calc(100% - 36px); margin:8px 18px 24px; font-size:15px; }
table.grid th, table.grid td { border:1px solid #b8c0cc; padding:8px 10px; vertical-align:middle; }
table.grid th { background:#eef2f7; text-align:center; }
table.grid td.path { font-family:Consolas, "맑은 고딕", monospace; font-size:14px; }
button { font:15px "맑은 고딕", Malgun Gothic, sans-serif; padding:4px 10px; cursor:pointer; }
</style>
</head>
<body>
<div id="wrap">
<div id="top">
  <h1>화면 목록</h1>
  <span class="who"><c:out value="${userName}"/> / <c:out value="${today}"/></span>
</div>
<%-- <p class="note">로그인된 상태에서 바로가기를 누르면 됩니다. 즐겨찾기는 <c:url value="/page/list.do"/> 를 저장하면 됩니다. 화면을 추가할 때는 PageViewController.pageLinks() 에 한 줄 넣으면 됩니다.</p> --%>
<table class="grid">
  <thead>
    <tr>
      <th style="width:240px">화면</th>
      <th style="width:220px">설명</th>
      <th>주소</th>
      <th style="width:160px"></th>
    </tr>
  </thead>
  <tbody>
    <c:forEach var="p" items="${pages}">
      <tr>
        <td><c:out value="${p.name}"/></td>
        <td><c:out value="${p.note}"/></td>
        <td class="path"><c:url value="${p.path}"/></td>
        <td>
          <a href="<c:url value='${p.path}'/>">바로가기</a>
          ·
          <button type="button" onclick="copyPath(this)" data-path="<c:url value='${p.path}'/>">주소 복사</button>
        </td>
      </tr>
    </c:forEach>
  </tbody>
</table>
</div>
<script>
function copyPath(btn) {
  var path = btn.getAttribute('data-path') || '';
  var url = window.location.origin + path;
  if (navigator.clipboard && navigator.clipboard.writeText) {
    navigator.clipboard.writeText(url).then(function() {
      alert('주소를 복사했습니다.\n' + url);
    }, function() {
      fallbackCopy(url);
    });
  } else {
    fallbackCopy(url);
  }
}
function fallbackCopy(text) {
  var ta = document.createElement('textarea');
  ta.value = text;
  document.body.appendChild(ta);
  ta.select();
  try {
    document.execCommand('copy');
    alert('주소를 복사했습니다.\n' + text);
  } catch (e) {
    prompt('주소를 복사하세요', text);
  }
  document.body.removeChild(ta);
}
</script>
</body>
</html>
