package egovframework.raw.report;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.util.StringUtils;
import egovframework.raw.dto.PicDTO;
import egovframework.raw.dto.ReportDTO;
import egovframework.raw.dto.TelDTO;
import egovframework.raw.util.TelResultCodeUtil;

/** Freemarker 템플릿용 TEL 성적서 표시 헬퍼. */
public class TelReportTemplateHelper {

  private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
  private static final DateTimeFormatter KOR_DATE =
      DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");

  public String resultLabel(String code) {
    return TelResultCodeUtil.resultLabel(code);
  }

  /** 1.1·1.2 적용 여부 — 적합(1)/부적합(0) 만 true, 그 외(해당없음) false */
  public boolean isApplicable(String code) {
    return TelResultCodeUtil.isApplicable(code);
  }

  public String esc(String value) {
    if (value == null) {
      return "";
    }
    return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        .replace("\"", "&quot;");
  }

  public String radioChecked(int yn) {
    return yn == 1 ? " checked" : "";
  }

  public String radioChecked(boolean checked) {
    return checked ? " checked" : "";
  }

  public String radioDisabled(boolean disabled) {
    return disabled ? " disabled" : "";
  }

  public String resultRadioFit(String code) {
    return radioChecked("1".equals(code)) + radioDisabled(!"1".equals(code) && !"0".equals(code));
  }

  public String resultRadioUnfit(String code) {
    return radioChecked("0".equals(code)) + radioDisabled(!"1".equals(code) && !"0".equals(code));
  }

  /** 3.1 시험결과 — 1.1·1.2 통합 적합 라디오 */
  public String section3ResultRadioFit(Object telObj) {
    TelDTO tel = asTel(telObj);
    boolean fit = TelResultCodeUtil.isSection3Fit(tel);
    boolean unfit = TelResultCodeUtil.isSection3Unfit(tel);
    boolean na = !fit && !unfit;
    return radioChecked(fit) + radioDisabled(na || unfit);
  }

  /** 3.1 시험결과 — 1.1·1.2 통합 부적합 라디오 */
  public String section3ResultRadioUnfit(Object telObj) {
    TelDTO tel = asTel(telObj);
    boolean fit = TelResultCodeUtil.isSection3Fit(tel);
    boolean unfit = TelResultCodeUtil.isSection3Unfit(tel);
    boolean na = !fit && !unfit;
    return radioChecked(unfit) + radioDisabled(na || fit);
  }

  public String imgTag(String url) {
    if (StringUtils.isEmpty(url)) {
      return "";
    }
    return "<img src=\"" + escAttr(url) + "\" oncontextmenu=\"return false\" "
        + "ondragstart=\"return false\" onselectstart=\"return false\">";
  }

  public String signImgTag(String url) {
    if (StringUtils.isEmpty(url)) {
      return "";
    }
    return "<img class=\"a4_sign_img\" src=\"" + escAttr(url) + "\" oncontextmenu=\"return false\" "
        + "ondragstart=\"return false\" onselectstart=\"return false\">";
  }

  public String escAttr(String value) {
    return esc(value);
  }

  public String nullSafe(String value) {
    return value == null ? "" : value;
  }

  /**
   * 자동 제품 라벨 하단 — 인증(등록)·모델식별번호.
   * 프론트 report_tel.js: athntNmbr, mdlIdntf 중 있는 값만 콤마로 연결.
   */
  public String productLabelAuthText(Object reportObj) {
    if (!(reportObj instanceof ReportDTO)) {
      return "";
    }
    ReportDTO report = (ReportDTO) reportObj;
    StringBuilder sb = new StringBuilder();
    appendIfPresent(sb, report.getAthntNmbr());
    appendIfPresent(sb, report.getMdlIdntf());
    return sb.toString();
  }

  private static void appendIfPresent(StringBuilder sb, String value) {
    if (StringUtils.isEmpty(value)) {
      return;
    }
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      return;
    }
    if (sb.length() > 0) {
      sb.append(',');
    }
    sb.append(trimmed);
  }

  /** 시험환경 온도 — Freemarker {@code t.temp} 접근 회피 */
  public String telEnvTemp(Object tel) {
    TelDTO dto = asTel(tel);
    if (dto == null) {
      return "(-) ℃";
    }
    return "(" + nullSafe(dto.getTemp()) + " ± " + nullSafe(dto.getTempPlus()) + ") ℃";
  }

  /** 시험환경 습도 */
  public String telEnvHmdt(Object tel) {
    TelDTO dto = asTel(tel);
    if (dto == null) {
      return "(-) % R.H.";
    }
    return "(" + nullSafe(dto.getHmdt()) + " ± " + nullSafe(dto.getHmdtPlus()) + ") % R.H.";
  }

  public String withMemo(String code, String memo) {
    String label = resultLabel(code);
    if (!StringUtils.isEmpty(memo)) {
      return label + " (" + memo + ")";
    }
    return label;
  }

  /**
   * 비밀번호 길이(2·3조합) 표시.
   * <ul>
   *   <li>부적합(0): 라벨 없이 메모만 노출</li>
   *   <li>해당사항 없음(null·그 외): 빈칸</li>
   *   <li>적합(1): {@link #withMemo(String, String)} 와 동일</li>
   * </ul>
   */
  public String pwdWithMemo(String code, String memo) {
    if ("0".equals(code)) {
      return StringUtils.isEmpty(memo) ? "" : memo;
    }
    if (!"1".equals(code)) {
      return "";
    }
    return withMemo(code, memo);
  }

  /** 발급내역 날짜 — 프론트 report_tel.js 와 동일 (yyyy년 MM월 dd일) */
  public String formatHistoryDate(String reportDt) {
    if (StringUtils.isEmpty(reportDt)) {
      return "";
    }
    try {
      return LocalDate.parse(reportDt.trim(), ISO_DATE).format(KOR_DATE);
    } catch (Exception e) {
      return reportDt;
    }
  }

  /**
   * Freemarker {@code pic!} 는 null 을 빈 문자열로 바꿔 {@link PicDTO} 인자와 타입 불일치가 난다.
   * Object 로 받아 {@link PicDTO} 만 처리한다.
   */
  private static PicDTO asPic(Object value) {
    return value instanceof PicDTO ? (PicDTO) value : null;
  }

  private static TelDTO asTel(Object value) {
    return value instanceof TelDTO ? (TelDTO) value : null;
  }

  /** picYn==1 이고 imageUrl 이 있을 때만 이미지 표시 */
  public boolean showPicImage(Object pic) {
    PicDTO dto = asPic(pic);
    return dto != null && dto.getPicYn() == 1 && !StringUtils.isEmpty(dto.getImageUrl());
  }

  /** 사진 셀 — picYn==1 이면 img, 아니면 "해당 없음." */
  public String picCellHtml(Object pic) {
    PicDTO dto = asPic(pic);
    if (showPicImage(dto)) {
      return imgTag(dto.getImageUrl());
    }
    return "<p>해당 없음.</p>";
  }

  /** title 이 '-'·빈값이 아니고 사진이 있을 때만 제목 행 표시 */
  public boolean showPicTitle(Object pic) {
    PicDTO dto = asPic(pic);
    if (!showPicImage(dto)) {
      return false;
    }
    if (dto.getTitle() == null) {
      return false;
    }
    String title = dto.getTitle().trim();
    return !title.isEmpty() && !"-".equals(title);
  }

  /** 측정/제품/화면 사진 제목 — picId 20·21 옵션 코드 매핑 */
  public String picDisplayTitle(Object pic) {
    PicDTO dto = asPic(pic);
    if (dto == null || dto.getTitle() == null) {
      return "";
    }
    String title = dto.getTitle().trim();
    if (title.isEmpty() || "-".equals(title)) {
      return "";
    }
    String mapped = mapScreenPicTitle(title);
    if (mapped != null) {
      return mapped;
    }
    if ("ETC".equalsIgnoreCase(title)) {
      return "";
    }
    return title;
  }

  private static String mapScreenPicTitle(String code) {
    switch (code) {
      case "4":
        return "비밀번호 복잡도 조건 사진";
      case "5":
        return "사용자의 비밀번호 설정 후 영상정보 확인 가능 화면";
      case "6":
        return "비밀번호 인증 횟수 실패시 차단 메시지";
      case "7":
        return "시험배치도";
      case "8":
        return "비밀번호 복잡도 조건 사진";
      case "9":
        return "클라우드 서버에 단말장치 등록 후 영상정보의 조회 또는 제어 가능 화면";
      case "10":
        return "클라우드 서버와 시험 대상 단말장치 간 보안성이 확보된 등록 과정 확인";
      case "11":
        return "비밀번호 인증 횟수 실패시 차단 메세지 첨부 사진";
      case "12":
        return "시험배치도";
      case "1":
        return "사용자 등록 화면";
      case "2":
        return "사용자 등록 후 접속화면";
      case "3":
        return "로그인 후 동작화면";
      default:
        return null;
    }
  }

  /** 직접/클라우드 화면 고정 캡션 — 사진이 있을 때만 표시 */
  public boolean showPicCaption(String caption, Object pic) {
    return showPicImage(pic) && !StringUtils.isEmpty(caption);
  }

  /** Freemarker 매크로 null 인자 오류 회피 — 캡션 행 HTML (caption 은 HTML 허용) */
  public String picCaptionRowHtml(String captionHtml, Object pic) {
    if (!showPicCaption(captionHtml, pic)) {
      return "";
    }
    return "<tr><th>" + captionHtml + "</th></tr>";
  }

  /** Freemarker 매크로 null 인자 오류 회피 — 사진 제목 행 HTML */
  public String picTitleRowHtml(Object pic) {
    if (!showPicTitle(pic)) {
      return "";
    }
    return "<tr><th>" + esc(picDisplayTitle(pic)) + "</th></tr>";
  }
}
