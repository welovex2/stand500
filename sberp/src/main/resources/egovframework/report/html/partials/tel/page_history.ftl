<#import "../tel_macros.ftl" as tel>
<#assign r = document.tel.report>
<section class="a4_box">
  <@tel.a4Header />
  <article class="a4_content">
    <h3>시험성적서 발급 내역</h3>
    <p>이 문서의 개정 내역이 표시됩니다.</p>
    <table class="a4_table table_1">
      <thead><tr><th>발급일</th><th>시험성적서 발급번호</th><th>발급사유</th></tr></thead>
      <tbody>
        <#list document.tel.historyRows as row>
        <tr><td>${h.formatHistoryDate(row.reportDt)}</td><td>${h.nullSafe(row.reportNo)}</td><td>${h.nullSafe(row.reportMemo)}</td></tr>
        </#list>
      </tbody>
    </table>
    <h3 class="modfile_in" style="margin-bottom: 8px;">시험기자재 보완내용</h3>
    <#if document.tel.modFiles?has_content>
    <pre class="box_pre last_pre">${h.imgTag(document.tel.modFiles[0])}</pre>
    <#else>
    <table class="a4_table product_img">
      <tr><td><p>해당 없음.</p></td></tr>
    </table>
    </#if>
    <#if !document.tel.modExtraChunks?has_content>
    <pre>${h.nullSafe(r.modMemo)}</pre>
    <table class="a4_table table_7">
      <tbody>
        <tr><td>(보완이 있는 경우) 시험기자재에 반드시 보완내용을 적용하여 유통하여야 하며, 이를 위반시 전파법 등 관계 법령에 따라 행정처분 대상이 될 수 있음을 안내하였음</td>
          <td><label style="width: auto;"><input type="radio"${h.radioChecked(r.modCheck1Yn)}><span></span>안내</label></td></tr>
        <tr><td>(보완이 없는 경우) 향후 기자재에 변경 사항이 발생할 경우, 반드시 변경신고를 완료한 후에 유통하여야 하며, 이를 위반 시 전파법 등 관계 법령에 따라 행정처분 대상이 될 수 있음을 안내하였음</td>
          <td><label style="width: auto;"><input type="radio"${h.radioChecked(r.modCheck2Yn)}><span></span>안내</label></td></tr>
      </tbody>
    </table>
    </#if>
  </article>
  <@tel.a4Footer pageNum />
</section>
