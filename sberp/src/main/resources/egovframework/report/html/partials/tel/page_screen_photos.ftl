<#import "../tel_macros.ftl" as tel>

<section class="a4_box">

  <@tel.a4Header />

  <#assign photoPageClass = (pics?size >= 2)?then('photo_page_pair', ((pics?size == 1)?then('photo_page_single', '')))>
  <article class="a4_content ${photoPageClass}">

    <#if showScreenTitle && screenTitle?has_content>

    <h6 class="left_h photo_section_title">${screenTitle}</h6>

    </#if>

    <@tel.photoPairStack pics />

  </article>

  <@tel.a4Footer pageNum />

</section>

