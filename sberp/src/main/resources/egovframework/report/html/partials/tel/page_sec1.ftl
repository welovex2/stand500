<#import "../tel_macros.ftl" as tel>
<#assign r = document.tel.report>
<@tel.bindTel r />
<section class="a4_box">
  <@tel.a4Header />
  <article class="a4_content no_margin">
    <h3 class="mg_st">1. 시험 결과</h3>
    <h4 class="left_h mg_bottom h_12">1.1 종합의견</h4>
    <table class="a4_table table_2 table_2_tel">
      <tr><th rowspan="4">1. 시험기자재</th><th>기자재 명칭</th><td colspan="3">${h.nullSafe(r.eqpmn)}</td></tr>
      <tr><th>모 델 명</th><td colspan="3">${h.nullSafe(r.model)}</td></tr>
      <tr><th>종명등가번호</th><td colspan="3">-</td></tr>
      <tr><th>통신접속단자</th><td colspan="3">${h.nullSafe(r.testPower)}</td></tr>
      <tr><th>2. 용&nbsp;&nbsp;&nbsp;&nbsp;도</th><td colspan="4"><pre style="text-align: left;">${h.nullSafe(r.prdUses)}</pre></td></tr>
      <tr><th>3. 구&nbsp;성&nbsp;품</th><td colspan="4"><pre style="text-align: left;">${h.nullSafe(r.cmp)}</pre></td></tr>
      <tr><th>4. 시험기준</th><td colspan="4"><pre style="text-align: left;">국립전파연구원 고시 제2025-13호 (단말장치 기술기준)</pre></td></tr>
      <tr><th>5. 시험방법</th><td colspan="4"><pre style="text-align: left;">KS X 3078:2025 디지털 방송통신 및 종합정보통신설비에 접속되는
단말장치의 적합성평가 시험방법</pre></td></tr>
      <tr><th>6. 시험환경</th><td>온도</td><td>${h.telEnvTemp(t)}</td><td>습도</td><td>${h.telEnvHmdt(t)}</td></tr>
      <tr><th>7. 기타 사항</th><td colspan="4"><pre class="tel_etc" style="text-align: left;"><#if document.tel.telEtc?has_content>${document.tel.telEtc}</#if></pre></td></tr>
      <tr><th>시&nbsp;험&nbsp;원</th><td colspan="4" class="si_testBy">성명 <span class="a4_sign_name">${h.nullSafe(r.testBy)}${h.signImgTag(r.testSignUrl)}</span> (서명)</td></tr>
      <tr><th>기술책임자</th><td colspan="4" class="si_revBy">성명 <span class="a4_sign_name">${h.nullSafe(r.revBy)}${h.signImgTag(r.revSignUrl)}</span> (서명)</td></tr>
    </table>
  </article>
  <@tel.a4Footer pageNum />
</section>
