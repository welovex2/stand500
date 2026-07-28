package egovframework.raw.report.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import egovframework.raw.dto.PicDTO;
import egovframework.raw.dto.ReportDTO;
import egovframework.raw.dto.TelDTO;
import egovframework.raw.report.ReportCommonPaths;
import egovframework.raw.util.TelResultCodeUtil;
import egovframework.raw.report.model.ReportMeta;
import egovframework.raw.report.model.ReportTel3078ViewModel;
import egovframework.tst.service.Test;

/** {@link ReportDTO} → {@link ReportTel3078ViewModel} (퍼블리셔 {@code report_tel.html} 용). */
public final class ReportTel3078DataBuilder {

  private static final int PHOTOS_PER_PAGE = 2;
  private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
  private static final DateTimeFormatter KOR_DATE =
      DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");

  private ReportTel3078DataBuilder() {}

  public static ReportTel3078ViewModel build(ReportDTO report, String publicBaseUrl) {
    ReportTel3078ViewModel vm = new ReportTel3078ViewModel();
    if (report == null) {
      return vm;
    }
    String appBase = trimSlash(publicBaseUrl);
    absolutizeSignUrls(report, appBase);

    vm.setReport(report);
    vm.setReportNo(ReportMeta.resolveReportNo(report));
    vm.setTestId(nullSafe(report.getTestId()));
    vm.setAssetBaseUrl(ReportCommonPaths.assetBase(appBase));

    vm.setReportDtFormatted(formatReportDt(report));
    vm.setResultText(report.isResult() ? "적합" : "부적합");
    vm.setPage1Article(resolvePage1Article(report));
    vm.setTelEtc(resolveTelEtc(report));

    if (!ObjectUtils.isEmpty(report.getReportList())) {
      vm.getHistoryRows().addAll(report.getReportList());
    }
    if (!ObjectUtils.isEmpty(report.getModFileList())) {
      for (String url : report.getModFileList()) {
        vm.getModFiles().add(ReportCommonPaths.absoluteAppUrl(appBase, url));
      }
      if (report.getModFileList().size() > 1) {
        List<List<String>> extraChunks = chunkList(
            vm.getModFiles().subList(1, vm.getModFiles().size()), PHOTOS_PER_PAGE);
        vm.setModExtraChunks(extraChunks);
        // 프론트: 마지막 추가 페이지가 사진 2장이면 안내 표는 다음 페이지
        if (!extraChunks.isEmpty()) {
          List<String> lastChunk = extraChunks.get(extraChunks.size() - 1);
          vm.setModNoticeSeparatePage(lastChunk.size() >= PHOTOS_PER_PAGE);
        }
      }
    }

    TelDTO tel = report.getTel();
    boolean directApplicable = TelResultCodeUtil.isApplicable(tel == null ? null : tel.getResultCode());
    boolean cloudApplicable =
        TelResultCodeUtil.isApplicable(tel == null ? null : tel.getResultCloudCode());
    vm.setDirectApplicable(directApplicable);
    vm.setCloudApplicable(cloudApplicable);

    vm.setDirectScreenPics(
        directApplicable ? filterPics(report.getImgList(), "20", appBase) : new ArrayList<PicDTO>());
    vm.setCloudScreenPics(
        cloudApplicable ? filterPics(report.getImgList(), "21", appBase) : new ArrayList<PicDTO>());
    vm.setMeasurePics(filterPics(report.getImgList(), "19", appBase));
    vm.setProductPics(filterPics(report.getImgList(), "14", appBase));

    vm.setMeasurePhotoPages(chunkPics(vm.getMeasurePics(), PHOTOS_PER_PAGE));
    vm.setDirectScreenPages(chunkPics(vm.getDirectScreenPics(), PHOTOS_PER_PAGE));
    vm.setCloudScreenPages(chunkPics(vm.getCloudScreenPics(), PHOTOS_PER_PAGE));

    buildProductPhotoPlan(vm);

    computePagePlan(vm);
    return vm;
  }

  private static void buildProductPhotoPlan(ReportTel3078ViewModel vm) {
    List<PicDTO> pics = vm.getProductPics();
    boolean hasUploadedLabel = hasUploadedLabelImage(pics);
    List<PicDTO> nonLabelPics = filterDisplayableNonLabelPics(pics);

    if (hasUploadedLabel) {
      vm.setShowProductLabelPage(false);
      vm.setMergeLabelWithLastPhoto(false);
      vm.setProductPhotoPages(chunkPics(filterDisplayablePics(pics), PHOTOS_PER_PAGE));
    } else if (nonLabelPics.isEmpty()) {
      vm.setShowProductLabelPage(true);
      vm.setMergeLabelWithLastPhoto(false);
      vm.setProductPhotoPages(new ArrayList<List<PicDTO>>());
    } else {
      vm.setShowProductLabelPage(false);
      vm.setMergeLabelWithLastPhoto(true);
      vm.setProductPhotoPages(chunkPics(nonLabelPics, PHOTOS_PER_PAGE));
    }
  }

  private static void computePagePlan(ReportTel3078ViewModel vm) {
    int p = 1;
    p++;
    p += vm.getModExtraChunks().size();
    if (vm.isModNoticeSeparatePage()) {
      p += 1;
    }
    int tocPage = ++p;
    int sec1 = ++p;
    int sec2 = ++p;
    int sec3 = ++p;
    int sec4Direct = ++p;

    if (vm.isDirectApplicable()) {
      if (vm.getDirectScreenPages().isEmpty()) {
        p += 1;
      } else {
        p += vm.getDirectScreenPages().size();
      }
    }

    int sec4Cloud = p + 1;
    p++;

    if (vm.isCloudApplicable()) {
      if (vm.getCloudScreenPages().isEmpty()) {
        p += 1;
      } else {
        p += vm.getCloudScreenPages().size();
      }
    }

    int sec5 = ++p;

    int measureStart = ++p;
    if (!vm.getMeasurePhotoPages().isEmpty() && vm.getMeasurePhotoPages().size() > 1) {
      p += vm.getMeasurePhotoPages().size() - 1;
    }

    int productStart = ++p;
    if (!vm.getProductPhotoPages().isEmpty()) {
      if (vm.getProductPhotoPages().size() > 1) {
        p += vm.getProductPhotoPages().size() - 1;
      }
    } else if (!vm.isShowProductLabelPage()) {
      // 빈 시험기자재 페이지 (이미 ++p 로 반영)
    }

    vm.setTotalPages(p);
    TelDTO tel = vm.getReport() == null ? null : vm.getReport().getTel();
    vm.setSection3DetailPages(
        TelResultCodeUtil.section3DetailPages(tel, sec4Direct, sec4Cloud));

    Map<String, Integer> toc = new LinkedHashMap<String, Integer>();
    toc.put("1. 시험 결과", sec1);
    toc.put("1.1 종합의견", sec1);
    toc.put("2. 시험기관", sec2);
    toc.put("2.1 일반현황", sec2);
    toc.put("2.2 시험장 소재지", sec2);
    toc.put("2.3 시험기관 지정사항", sec2);
    toc.put("3. 시험 항목 및 결과", sec3);
    toc.put("4. 세부 시험내역", sec4Direct);
    toc.put("5. 사용 장비 내역", sec5);
    toc.put("6. 측정 사진", measureStart);
    toc.put("7. 시험기자재 사진", productStart);
    vm.setToc(toc);
    vm.getToc().put("_tocPage", tocPage);
  }

  private static String resolveTelEtc(ReportDTO report) {
    StringBuilder sb = new StringBuilder();
    if (report.getStdYn() == 1) {
      sb.append("- 암호 변경 후 정상동작 됨");
    }
    if (report.getStdEtcYn() == 1 && !StringUtils.isEmpty(report.getStdMemo())) {
      if (sb.length() > 0) {
        sb.append("<br>");
      }
      sb.append("- ").append(report.getStdMemo());
    }
    return sb.toString();
  }

  private static int resolvePage1Article(ReportDTO report) {
    String dt = resolveCurrentReportDt(report);
    if (ObjectUtils.isEmpty(dt)) {
      return 13;
    }
    try {
      LocalDate d = LocalDate.parse(dt, ISO_DATE);
      LocalDate cutoff = LocalDate.of(2025, 4, 30);
      return d.isAfter(cutoff) ? 14 : 13;
    } catch (Exception e) {
      return 13;
    }
  }

  private static String resolveCurrentReportDt(ReportDTO report) {
    if (!ObjectUtils.isEmpty(report.getReportList())) {
      for (Test t : report.getReportList()) {
        if (t.getTestSeq() == report.getTestSeq()) {
          return t.getReportDt();
        }
      }
    }
    return report.getReportDt();
  }

  private static String formatReportDt(ReportDTO report) {
    String dt = resolveCurrentReportDt(report);
    if (ObjectUtils.isEmpty(dt)) {
      return "";
    }
    try {
      return LocalDate.parse(dt, ISO_DATE).format(KOR_DATE);
    } catch (Exception e) {
      return dt;
    }
  }

  private static void absolutizeSignUrls(ReportDTO report, String appBase) {
    report.setTestSignUrl(ReportCommonPaths.absoluteAppUrl(appBase, report.getTestSignUrl()));
    report.setRevSignUrl(ReportCommonPaths.absoluteAppUrl(appBase, report.getRevSignUrl()));
  }

  private static List<PicDTO> filterPics(List<PicDTO> imgList, String picId, String appBase) {
    List<PicDTO> out = new ArrayList<PicDTO>();
    if (ObjectUtils.isEmpty(imgList)) {
      return out;
    }
    for (PicDTO p : imgList) {
      if (p == null || !picId.equals(p.getPicId())) {
        continue;
      }
      if (p.getPicYn() == 1 && !StringUtils.isEmpty(p.getImageUrl())) {
        p.setImageUrl(ReportCommonPaths.absoluteAppUrl(appBase, p.getImageUrl()));
      }
      out.add(p);
    }
    return out;
  }

  private static List<List<PicDTO>> chunkPics(List<PicDTO> pics, int size) {
    List<List<PicDTO>> pages = new ArrayList<List<PicDTO>>();
    if (ObjectUtils.isEmpty(pics)) {
      return pages;
    }
    for (int i = 0; i < pics.size(); i += size) {
      pages.add(new ArrayList<PicDTO>(pics.subList(i, Math.min(i + size, pics.size()))));
    }
    return pages;
  }

  private static <T> List<List<T>> chunkList(List<T> list, int size) {
    List<List<T>> chunks = new ArrayList<List<T>>();
    for (int i = 0; i < list.size(); i += size) {
      chunks.add(new ArrayList<T>(list.subList(i, Math.min(i + size, list.size()))));
    }
    return chunks;
  }

  /** 사용자가 직접 올린 '제품 라벨' 이미지 존재 여부 (picYn==1 + 이미지 URL). */
  private static boolean hasUploadedLabelImage(List<PicDTO> pics) {
    for (PicDTO p : pics) {
      if (p == null || p.getPicYn() != 1 || StringUtils.isEmpty(p.getImageUrl())) {
        continue;
      }
      String title = p.getTitle() == null ? "" : p.getTitle().trim();
      if ("제품 라벨".equals(title)) {
        return true;
      }
    }
    return false;
  }

  /** 실제 표시 가능한(picYn==1 + 이미지 URL) 시험기자재 사진. */
  private static List<PicDTO> filterDisplayablePics(List<PicDTO> pics) {
    List<PicDTO> out = new ArrayList<PicDTO>();
    if (ObjectUtils.isEmpty(pics)) {
      return out;
    }
    for (PicDTO p : pics) {
      if (p != null && p.getPicYn() == 1 && !StringUtils.isEmpty(p.getImageUrl())) {
        out.add(p);
      }
    }
    return out;
  }

  /** 표시 가능한 사진 중 '제품 라벨' 슬롯 제외 (자동 라벨 페이지와 중복 방지). */
  private static List<PicDTO> filterDisplayableNonLabelPics(List<PicDTO> pics) {
    List<PicDTO> out = new ArrayList<PicDTO>();
    if (ObjectUtils.isEmpty(pics)) {
      return out;
    }
    for (PicDTO p : pics) {
      if (p == null || p.getPicYn() != 1 || StringUtils.isEmpty(p.getImageUrl())) {
        continue;
      }
      String title = p.getTitle() == null ? "" : p.getTitle().trim();
      if ("제품 라벨".equals(title)) {
        continue;
      }
      out.add(p);
    }
    return out;
  }

  private static String nullSafe(String value) {
    return value == null ? "" : value;
  }

  private static String trimSlash(String base) {
    if (base == null) {
      return "";
    }
    String s = base.trim();
    while (s.endsWith("/")) {
      s = s.substring(0, s.length() - 1);
    }
    return s;
  }
}
