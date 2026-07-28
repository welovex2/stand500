<#import "../tel_macros.ftl" as tel>

<section class="a4_box">

  <@tel.a4Header />

  <#assign photoPageClass = (pics?size >= 2)?then('photo_page_pair', ((pics?size == 1)?then('photo_page_single', '')))>
  <article class="a4_content measure_photo_page ${photoPageClass}">

    <#if showTitle><h3 class="mg_st">6. 측정 사진</h3></#if>

    <@tel.photoPairStack pics />

  </article>

  <@tel.a4Footer pageNum />

</section>

