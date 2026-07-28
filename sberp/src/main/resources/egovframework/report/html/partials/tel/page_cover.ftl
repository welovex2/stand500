<#import "../tel_macros.ftl" as tel>
<#assign r = document.tel.report>
<section class="a4_box">
  <@tel.a4Header />
  <article class="a4_content a4_content_border" style="margin-bottom: 0px;">
    <h2 style="font-size: 18pt;">방송통신기자재등(유선-영상정보처리기기) 시험성적서</h2>
    <ul class="info_list">
      <li><h4>1.&nbsp;발&nbsp;급&nbsp;번&nbsp;호<span>:</span></h4><p>${document.tel.reportNo!''}</p></li>
      <li><h4>2.&nbsp;접&nbsp;수&nbsp;일<span>:</span></h4><p>${h.nullSafe(r.rcptDt)}</p></li>
      <li><h4>3.&nbsp;시&nbsp;험&nbsp;기&nbsp;간<span>:</span></h4>
        <p style="justify-content: flex-start;"><span style="font-size: 11pt;">${h.nullSafe(r.testSDt)}</span>&nbsp;&nbsp;&nbsp;~&nbsp;&nbsp;&nbsp;<span style="font-size: 11pt;">${h.nullSafe(r.testEDt)}</span></p></li>
      <li><h4>4.&nbsp;신청인(상호명)<span>:</span></h4><p>${h.nullSafe(r.aplcn)}</p>
        <ul class="info_sub_list">
          <li><h4>사업자등록번호<span>:</span></h4><p>${h.nullSafe(r.bsnsRgnmb)}</p></li>
          <li><h4>대표자 성명<span>:</span></h4><p>${h.nullSafe(r.rprsn)}</p></li>
          <li><h4>주&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;소<span>:</span></h4><p style="line-height: 14px;">${h.nullSafe(r.address)}</p></li>
        </ul>
      </li>
      <li><h4>5.&nbsp;기자재&nbsp;명칭<br>&nbsp;/&nbsp;모&nbsp;델&nbsp;명<span>:</span></h4>
        <p style="justify-content: flex-start;"><span style="font-size: 11pt; text-align: left;">${h.nullSafe(r.eqpmn)}<#if r.model?? && r.model?has_content> / ${h.nullSafe(r.model)}</#if></span></p></li>
      <li><h4>6.&nbsp;제&nbsp;조&nbsp;자<br>&nbsp;/&nbsp;제조국가<span>:</span></h4>
        <p style="justify-content: flex-start;"><span style="font-size: 11pt; text-align: left;">${h.nullSafe(r.mnfctCmpny)}<#if r.mnfctCntry?? && r.mnfctCntry?has_content> / ${h.nullSafe(r.mnfctCntry)}</#if></span></p></li>
      <li><h4>7.&nbsp;시&nbsp;험&nbsp;결&nbsp;과<span>:</span></h4><p>${document.tel.resultText!''}</p></li>
    </ul>
    <h4 class="cover_legal">방송통신기자재등 시험기관의 지정 및 관리에 관한 고시<br>제<span>${document.tel.page1Article}</span>조의 규정에 의하여 시험성적서를 발급합니다.</h4>
    <h6 class="rp_reportDt">${document.tel.reportDtFormatted!''}</h6>
    <h1 class="stamp_text">(주)스탠다드뱅크 대표이사 (인)<span class="stamp_in"></span></h1>
    <h6>주소 : 경기도 군포시 군포첨단산업2로 48(부곡동)<br><span>전화번호 : 031-393-9394</span><br><span>팩스번호 : 031-393-9392</span><br></h6>
    <h6 class="color_red">※ 인증 받은 방송통신기자재는 반드시 "적합성평가표시"를 부착하여 유통하여야 합니다.<br>위반 시 과태료 처분 및 인증이 취소될 수 있습니다.</h6>
    <h6><span>본 시험성적서의 시험결과는 신청인이 제출한 시료에 한합니다.</span></h6>
    <h6><strong class="color_blue">본 시험성적서는 전파법에 따른 적합성평가 시험성적서이므로 "KOLAS 인정"과 관련이 없음.</strong></h6>
  </article>
  <article class="a4_text"><p class="a4_text_in"></p></article>
  <@tel.a4Footer pageNum />
</section>
