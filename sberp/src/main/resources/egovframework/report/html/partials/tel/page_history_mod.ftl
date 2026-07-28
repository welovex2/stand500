<#import "../tel_macros.ftl" as tel>

<section class="a4_box">

  <@tel.a4Header />

  <article class="a4_content">

    <h3 style="margin-bottom: 8px;">시험기자재 보완내용</h3>

    <#list chunk as url>

    <pre style="margin-bottom: 0px;" class="box_pre last_pre mod_slot">${h.imgTag(url)}</pre>

    </#list>

    <#-- 프론트: 사진 2장으로 페이지가 꽉 차면 안내 표는 다음 페이지(showModNotice=false) -->

    <#if showModNotice!false>

    <pre style="margin-top: 16px;">${h.nullSafe(document.tel.report.modMemo)}</pre>

    <table class="a4_table table_7">

      <tbody>

        <tr><td>(보완이 있는 경우) 시험기자재에 반드시 보완내용을 적용하여 유통하여야 하며, 이를 위반시 전파법 등 관계 법령에 따라 행정처분 대상이 될 수 있음을 안내하였음</td>

          <td><label style="width: auto;"><input type="radio"${h.radioChecked(document.tel.report.modCheck1Yn)}><span></span>안내</label></td></tr>

        <tr><td>(보완이 없는 경우) 향후 기자재에 변경 사항이 발생할 경우, 반드시 변경신고를 완료한 후에 유통하여야 하며, 이를 위반 시 전파법 등 관계 법령에 따라 행정처분 대상이 될 수 있음을 안내하였음</td>

          <td><label style="width: auto;"><input type="radio"${h.radioChecked(document.tel.report.modCheck2Yn)}><span></span>안내</label></td></tr>

      </tbody>

    </table>

    </#if>

  </article>

  <@tel.a4Footer pageNum />

</section>

