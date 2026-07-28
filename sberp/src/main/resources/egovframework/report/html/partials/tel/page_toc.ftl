<#import "../tel_macros.ftl" as tel>

<#assign toc = document.tel.toc>

<section class="a4_box a4_count">

  <@tel.a4Header />

  <article class="a4_content">

    <h6 class="h_16"> &lt; 목 차 &gt; </h6>

    <@tel.tocLine title="1. 시험 결과" page=toc["1. 시험 결과"] />

    <@tel.tocLine title="1.1 종합의견" page=toc["1.1 종합의견"] indent=true />

    <@tel.tocLine title="2. 시험기관" page=toc["2. 시험기관"] />

    <@tel.tocLine title="2.1 일반현황" page=toc["2.1 일반현황"] indent=true />

    <@tel.tocLine title="2.2 시험장 소재지" page=toc["2.2 시험장 소재지"] indent=true />

    <@tel.tocLine title="2.3 시험기관 지정사항" page=toc["2.3 시험기관 지정사항"] indent=true />

    <@tel.tocLine title="3. 시험 항목 및 결과" page=toc["3. 시험 항목 및 결과"] />

    <@tel.tocLine title="4. 세부 시험내역" page=toc["4. 세부 시험내역"] />

    <@tel.tocLine title="5. 사용 장비 내역" page=toc["5. 사용 장비 내역"] />

    <@tel.tocLine title="6. 측정 사진" page=toc["6. 측정 사진"] />

    <@tel.tocLine title="7. 시험기자재 사진" page=toc["7. 시험기자재 사진"] />

  </article>

  <@tel.a4Footer pageNum />

</section>

