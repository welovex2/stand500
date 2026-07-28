package egovframework.raw.report.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import egovframework.raw.dto.PicDTO;
import egovframework.raw.dto.ReportDTO;
import egovframework.raw.dto.TelDTO;
import egovframework.tst.service.Test;
import lombok.Getter;
import lombok.Setter;

/** Freemarker {@code report_tel_3078.html} 에 전달하는 TEL 성적서 데이터. */
@Getter
@Setter
public class ReportTel3078ViewModel {

  private ReportDTO report;
  private String reportNo;
  private String testId;
  private String assetBaseUrl;

  private String reportDtFormatted;
  private String resultText;
  private int page1Article;
  private String telEtc;

  private List<Test> historyRows = new ArrayList<Test>();
  private List<String> modFiles = new ArrayList<String>();
  private List<List<String>> modExtraChunks = new ArrayList<List<String>>();
  /**
   * 보완사진 추가 페이지가 2장으로 꽉 찬 경우 — 프론트와 동일하게
   * 안내 표(table_7)를 다음 빈 페이지에 둔다.
   */
  private boolean modNoticeSeparatePage;

  private Map<String, Integer> toc = new HashMap<String, Integer>();
  private int totalPages;
  /** 3.1 세부 시험내역 시작 페이지 (1.1·1.2, 콤마 구분) */
  private String section3DetailPages;

  private List<PicDTO> directScreenPics = new ArrayList<PicDTO>();
  private List<PicDTO> cloudScreenPics = new ArrayList<PicDTO>();
  private List<PicDTO> measurePics = new ArrayList<PicDTO>();
  private List<PicDTO> productPics = new ArrayList<PicDTO>();

  private List<List<PicDTO>> measurePhotoPages = new ArrayList<List<PicDTO>>();
  private List<List<PicDTO>> productPhotoPages = new ArrayList<List<PicDTO>>();
  private List<List<PicDTO>> directScreenPages = new ArrayList<List<PicDTO>>();
  private List<List<PicDTO>> cloudScreenPages = new ArrayList<List<PicDTO>>();
  /** 1.1 직접 접속 — 적합/부적합일 때만 상세·측정화면 페이지 출력 */
  private boolean directApplicable;
  /** 1.2 클라우드 접속 — 적합/부적합일 때만 상세·측정화면 페이지 출력 */
  private boolean cloudApplicable;
  private boolean showProductLabelPage;
  private boolean mergeLabelWithLastPhoto;

  public TelDTO tel() {
    return report == null ? null : report.getTel();
  }

  public PicDTO directPic(int index) {
    return picAt(directScreenPics, index);
  }

  public PicDTO cloudPic(int index) {
    return picAt(cloudScreenPics, index);
  }

  private static PicDTO picAt(List<PicDTO> pics, int index) {
    if (pics == null || index < 0 || index >= pics.size()) {
      return null;
    }
    return pics.get(index);
  }
}
