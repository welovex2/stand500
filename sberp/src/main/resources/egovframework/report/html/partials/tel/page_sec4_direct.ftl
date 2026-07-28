<#import "../tel_macros.ftl" as tel>
<#assign r = document.tel.report>
<section class="a4_box">
  <@tel.a4Header />
  <article class="a4_content">
    <h3 class="mg_st">4. 세부 시험내역</h3>
    <table class="a4_table table_3"><tr style="height: 50px;"><th style="font-weight: normal; width: 70px;">4-1</th><td>영상정보처리기기의 비밀번호 설정 및 접속(제29조)</td></tr></table>
    <h4 class="left_h mg_bottom h_12 mg_big_top">1.1 직접 접속 방식</h4>
    <#if document.tel.directApplicable>
    <@tel.bindTel r />
    <h6 class="left_h">1) 영상정보 조회</h6>
    <table class="a4_table table_3">
      <tr><th style="width: 80%;">시험기준</th><th style="width: 20%;">시험결과</th></tr>
      <tr><td>공장초기 상태에서 동작시 영상정보의 조회 또는 제어가 되지 않는 것을 확인</td><td style="text-align: center;">${h.resultLabel(t.resultResetCode)}</td></tr>
      <tr><td>공장초기 비밀번호가 설정되어있지 않는 경우 제어 프로그램을 이용하여<br>새로운 비밀번호 설정 후 영상정보의 조회 또는 제어 동작 확인</td><td style="text-align: center;">${h.resultLabel(t.resultNoPassCode)}</td></tr>
      <tr><td>공장초기 비밀번호가 설정되어 있는 경우, 공장초기 비밀번호를 변경한 후 영상정보의 조회 또는 제어 동작 확인<br>(이 때 공장초기 비밀번호와 동일한 비밀번호로는 변경되지 않거나 동일한 비밀번호로 변경할 경우 영상정보의 조회 또는 제어 동작등이 되지 않는 것을 확인)</td><td style="text-align: center;">${h.resultLabel(t.resultNewPassCode)}</td></tr>
    </table>
    <h6 class="left_h mg_top">2) 비밀번호 복잡도 조건</h6>
    <table class="a4_table table_3">
      <tr><th style="width: 45%;">시험항목</th><th style="width: 18%;">시험기준</th><th style="width: 37%;">시험결과</th></tr>
      <tr><td>숫자, 영문자 대문자, 영문자 소문자, 특수문자 중 임의의 3조합인 경우의 비밀번호 길이</td><td style="text-align: center;">≥ 8자리</td><td style="text-align: center;">${h.pwdWithMemo(t.resultPwd3Code, t.resultPwd3Memo)}</td></tr>
      <tr><td>숫자, 영문자 대문자, 영문자 소문자, 특수문자 중 임의의 2조합인 경우의 비밀번호 길이</td><td style="text-align: center;">≥ 10자리</td><td style="text-align: center;">${h.pwdWithMemo(t.resultPwd2Code, t.resultPwd2Memo)}</td></tr>
    </table>
    <h6 class="left_h mg_top">3) 비밀번호 인증 실패시 차단</h6>
    <table class="a4_table table_3">
      <tr><th style="width: 58%;">시험항목</th><th style="width: 22%;">시험기준</th><th style="width: 20%;">시험결과</th></tr>
      <tr><td>접속이 차단되는 비밀번호 연속 오입력 횟수</td><td class="criteria_oneline">≤ 5회</td><td style="text-align: center;">${h.resultLabel(t.resultLockCntCode)}</td></tr>
      <tr><td>비밀번호 인증 실패 시 접속 차단 시간</td><td class="criteria_oneline">30초~60분 이내</td><td style="text-align: center;">${h.resultLabel(t.resultLockTimeCode)}</td></tr>
    </table>
    <#else>
    <table class="a4_table product_img">
      <tr><td><p>해당 없음.</p></td></tr>
    </table>
    </#if>
  </article>
  <@tel.a4Footer pageNum />
</section>
