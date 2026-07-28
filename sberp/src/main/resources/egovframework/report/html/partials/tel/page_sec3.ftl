<#import "../tel_macros.ftl" as tel>

<#assign r = document.tel.report>

<@tel.bindTel r />

<section class="a4_box">

  <@tel.a4Header />

  <article class="a4_content">

    <h3 class="mg_st pic_1">3. 시험 항목 및 결과</h3>

    <table class="a4_table table_3">

      <tr><th>번호</th><th>시&nbsp;험&nbsp;항&nbsp;목</th><th>적&nbsp;용<br>여&nbsp;부</th><th>시험결과</th><th>페이지</th></tr>

      <tbody>

        <tr class="sec3-item-row" style="height: 50px;">

          <td style="text-align: center; width: 70px;">3.1</td>

          <td>영상정보처리기기의 비밀번호 등(제29조)</td>

          <td class="sec3-apply-cell"><label data-check="0" class="method0 sec3-check"><input type="radio" checked><span></span></label></td>

          <td class="sec3-result-cell">

            <label><input type="radio"<#if hasTel>${h.section3ResultRadioFit(t)}</#if>><span></span>적합</label>

            <label><input type="radio"<#if hasTel>${h.section3ResultRadioUnfit(t)}</#if>><span></span>부적합</label>

          </td>

          <td class="sec3-page-cell">${document.tel.section3DetailPages!''}</td>

        </tr>

      </tbody>

    </table>

  </article>

  <@tel.a4Footer pageNum />

</section>

