<#-- TEL 성적서 공통 매크로 (퍼블리셔 report_tel.html 기준) -->

<#-- report.tel 미입력·미저장 시에도 렌더 오류 없음 — 호출 후 t, hasTel 사용 -->
<#macro bindTel report>
  <#if report.tel??>
    <#global t = report.tel>
    <#global hasTel = true>
  <#else>
    <#global t = ''>
    <#global hasTel = false>
  </#if>
</#macro>

<#macro a4Header>
  <article class="a4_header">
    <img src="${document.assetBaseUrl}/img/logo.svg" alt="">
    <h5>발급번호:&nbsp;<span>${document.tel.reportNo!''}</span>호</h5>
  </article>
</#macro>

<#macro a4Footer pageNum>
  <article class="a4_footer">
    <div class="a4_footer_top">
      <h6>TEL-KCC-001 (Rev.5)</h6>
      <h6 class="print_page">${pageNum} / ${document.tel.totalPages}</h6>
      <h6><strong>시험 접수번호: <span>${document.tel.testId!''}</span>호</strong></h6>
    </div>
    <div class="a4_footer_bottom">
      본 시험성적서는 (주)스탠다드뱅크의 서면 동의없이 무단 전재 및 복사를 할 수 없습니다.<br>
      본 시험성적서의 진위확인은 DocuQR 홈페이지에서 가능합니다. (단, G4B를 통해 발급한 경우, 하단 QR 코드로만 가능합니다.)
    </div>
  </article>
</#macro>

<#-- 목차 한 줄 (report-pdf.css .toc_line flex 레이아웃) -->
<#macro tocLine title page indent=false>
  <p class="toc_line<#if indent> toc_indent</#if>">
    <span class="list_title">${title}</span>
    <span class="toc_leader">....................................................................................................................................</span>
    <span class="list_number">${page!''}</span>
  </p>
</#macro>

<#-- 사진 1~2장 페이지 — 사진마다 별도 테이블, 세로 균등 배치 -->
<#macro photoPairStack pics>
  <#assign photoSlotClass = (pics?size >= 2)?then('photo_pair', ((pics?size == 1)?then('photo_single', '')))>
  <div class="photo_pair_stack">
    <#list pics as pic>
    <table class="a4_table product_img ${photoSlotClass}">
      <tr><td>${h.picCellHtml(pic)}</td></tr>
      ${h.picTitleRowHtml(pic)}
    </table>
    </#list>
    <#if !pics?has_content>
    <table class="a4_table product_img">
      <tr><td><p>해당 없음.</p></td></tr>
    </table>
    </#if>
  </div>
</#macro>

<#-- 자동 생성 제품 라벨 테이블 (시험기자재 사진 하단 병합·단독 페이지 공용) -->
<#macro productLabelTable report assetBaseUrl>
  <#assign labelAuth = h.productLabelAuthText(report)>
  <table class="a4_table product_img product_label_table">
    <tr><td>
      <div class="product_label"><div class="product_label_in">
        <div class="product_label_box">
          <img src="${assetBaseUrl}/img/label.jpg" alt="">
          <div class="label_text">
            <p class="lb_cmpyName">상호 : ${h.nullSafe(report.aplcn)}</p>
            <p class="lb_prdctName">기기명칭 : ${h.nullSafe(report.eqpmn)}</p>
            <p class="lb_modelName">모델명 : ${h.nullSafe(report.model)}</p>
            <p>제조년월일 : 별도표기</p>
            <p class="lb_mnfctCmpny">제조자 : ${h.nullSafe(report.mnfctCmpny)}</p>
            <p class="lb_mnfctCntry">제조국가 : ${h.nullSafe(report.mnfctCntry)}</p>
          </div>
        </div>
        <div id="lb_data" class="label_bottom">${labelAuth}</div>
      </div></div>
    </td></tr>
    <tr><th>제품 라벨</th></tr>
  </table>
</#macro>
