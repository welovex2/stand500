<#import "../tel_macros.ftl" as tel>
<#assign r = document.tel.report>
<section class="a4_box">
  <@tel.a4Header />
  <article class="a4_content">
    <#if (showLabelTitle!true)><h3 class="mg_st">7. 시험기자재 사진</h3></#if>
    <@tel.productLabelTable r document.assetBaseUrl />
  </article>
  <@tel.a4Footer pageNum />
</section>
