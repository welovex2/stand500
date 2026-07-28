<#import "../tel_macros.ftl" as tel>



<#assign r = document.tel.report>



<section class="a4_box">



  <@tel.a4Header />



  <#assign photoPageClass = (pics?size >= 2)?then('photo_page_pair', ((pics?size == 1)?then('photo_page_single', '')))>

  <#assign withLabelClass = (appendAutoLabel!false)?then(' photo_page_with_label', '')>

  <article class="a4_content product_photo_page ${photoPageClass}${withLabelClass}">



    <#if showTitle><h3 class="mg_st">7. 시험기자재 사진</h3></#if>



    <@tel.photoPairStack pics />

    <#if appendAutoLabel!false>

    <@tel.productLabelTable r document.assetBaseUrl />

    </#if>



  </article>



  <@tel.a4Footer pageNum />



</section>



