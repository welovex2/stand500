<#import "../tel_macros.ftl" as tel>
<#assign r = document.tel.report>
<section class="a4_box">
  <@tel.a4Header />
  <article class="a4_content">
    <h4 class="left_h mg_bottom h_12">1.2 클라우드 접속 방식</h4>
    <#if document.tel.cloudApplicable>
    <@tel.bindTel r />
    <h6 class="left_h">1) 단말장치 제어 및 관리</h6>
    <table class="a4_table table_3">
      <tr><th style="width: 80%;">시험기준</th><th style="width: 20%;">시험결과</th></tr>
      <tr><td>공장초기화 상태에서 클라우드 서버에 사용자 정보 등록 전 단말장치의 제어 및 관리 기능이 제공되지 않는 것을 확인</td><td style="text-align: center;">${h.resultLabel(t.resultCloudResetCode)}</td></tr>
    </table>
    <h6 class="left_h mg_top">2) 비밀번호 복잡도 조건</h6>
    <table class="a4_table table_3">
      <tr><th style="width: 45%;">시험항목</th><th style="width: 18%;">시험기준</th><th style="width: 37%;">시험결과</th></tr>
      <tr><td>숫자, 영문자 대문자, 영문자 소문자, 특수문자 중 임의의 3조합인 경우의 비밀번호 길이</td><td style="text-align: center;">≥ 8자리</td><td style="text-align: center;">${h.pwdWithMemo(t.resultCloudPwd3Code, t.resultCloudPwd3Memo)}</td></tr>
      <tr><td>숫자, 영문자 대문자, 영문자 소문자, 특수문자 중 임의의 2조합인 경우의 비밀번호 길이</td><td style="text-align: center;">≥ 10자리</td><td style="text-align: center;">${h.pwdWithMemo(t.resultCloudPwd2Code, t.resultCloudPwd2Memo)}</td></tr>
    </table>
    <h6 class="left_h mg_top">3) 클라우드 서버에 단말장치 등록</h6>
    <table class="a4_table table_3">
      <tr><th style="width: 80%;">시험기준</th><th style="width: 20%;">시험결과</th></tr>
      <tr><td>영상정보 처리 제어용 단말기를 통해 시험 대상 단말장치를 클라우드 서버에 등록할 때,<br><span class="criteria_fit">클라우드 서버와 시험 대상 단말장치 간에 보안성이 확보된 등록 과정이 존재하는지 확인</span></td><td style="text-align: center;">${h.resultLabel(t.resultCloudRegCode)}</td></tr>
      <tr><td>클라우드 서버에 시험 대상 단말장치를 등록한 후 영상정보의 조회 또는 제어 동작 확인</td><td style="text-align: center;">${h.resultLabel(t.resultCloudAccessCode)}</td></tr>
    </table>
    <h6 class="left_h mg_top">4) 클라우드 서버 비밀번호 인증 실패시 차단</h6>
    <table class="a4_table table_3">
      <tr><th style="width: 58%;">시험항목</th><th style="width: 22%;">시험기준</th><th style="width: 20%;">시험결과</th></tr>
      <tr><td>접속이 차단되는 비밀번호 연속 오입력 횟수</td><td class="criteria_oneline">≤ 5회</td><td style="text-align: center;">${h.resultLabel(t.resultCloudLockCntCode)}</td></tr>
      <tr><td>비밀번호 인증 실패 시 접속 차단 시간</td><td class="criteria_oneline">30초~60분 이내</td><td style="text-align: center;">${h.resultLabel(t.resultCloudLockTimeCode)}</td></tr>
    </table>
    <#else>
    <table class="a4_table product_img">
      <tr><td><p>해당 없음.</p></td></tr>
    </table>
    </#if>
  </article>
  <@tel.a4Footer pageNum />
</section>
