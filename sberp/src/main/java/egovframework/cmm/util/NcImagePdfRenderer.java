package egovframework.cmm.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * A4 세로 · 페이지당 2장(위·아래) PDF 렌더러.
 * <p>용량 절감: 슬롯 표시 크기 기준 {@value #EMBED_DPI} DPI 로 다운스케일 후 JPEG 유손실 embed.
 */
public final class NcImagePdfRenderer {

  /** A4 좌우 여백 (pt) */
  private static final float MARGIN_HORIZONTAL_PT = mmToPt(10f);
  /** A4 위·아래 여백 (pt) — 라벨/메모 작성 공간 */
  private static final float MARGIN_VERTICAL_PT = mmToPt(20f);
  /** 위·아래 사진 사이 간격 — 라벨/메모 작성용 (pt) */
  private static final float GAP_PT = mmToPt(22f);
  /** 슬롯 내부 여백 — 사진 가장자리와 슬롯 경계 사이 (pt) */
  private static final float SLOT_PADDING_PT = mmToPt(5f);

  /** PDF embed 해상도 (표시 크기 대비). 슬롯 선명도 우선 (이전 150 DPI 대비 약 3배 픽셀). */
  static final float EMBED_DPI = 260f;

  /** JPEG 품질 후보 (높음→낮음). 목표 용량 초과 시 순차 적용. */
  private static final float[] JPEG_QUALITIES = {0.88f, 0.82f, 0.75f};

  /** 페이지당 목표 PDF 용량 (bytes). 4페이지 ≈ 1.8MB 기준. */
  private static final long TARGET_BYTES_PER_PAGE = 450L * 1024L;

  private NcImagePdfRenderer() {}

  public static byte[] renderTwoPerPage(List<BufferedImage> images) throws IOException {
    if (images == null || images.isEmpty()) {
      throw new IllegalArgumentException("images required");
    }

    int pageCount = (images.size() + 1) / 2;
    long targetBytes = pageCount * TARGET_BYTES_PER_PAGE;

    byte[] result = null;
    for (float quality : JPEG_QUALITIES) {
      result = renderTwoPerPage(images, quality);
      if (result.length <= targetBytes) {
        return result;
      }
    }
    return result;
  }

  static byte[] renderTwoPerPage(List<BufferedImage> images, float jpegQuality)
      throws IOException {
    if (images == null || images.isEmpty()) {
      throw new IllegalArgumentException("images required");
    }

    PDDocument document = new PDDocument();
    try {
      PDRectangle pageSize = PDRectangle.A4;
      float pageWidth = pageSize.getWidth();
      float pageHeight = pageSize.getHeight();

      float slotWidth = pageWidth - (2f * MARGIN_HORIZONTAL_PT);
      float usableHeight = pageHeight - (2f * MARGIN_VERTICAL_PT) - GAP_PT;
      float slotHeight = usableHeight / 2f;

      for (int i = 0; i < images.size(); i += 2) {
        PDPage page = new PDPage(pageSize);
        document.addPage(page);

        BufferedImage top = images.get(i);
        drawImageInSlot(document, page, top, MARGIN_HORIZONTAL_PT,
            pageHeight - MARGIN_VERTICAL_PT - slotHeight, slotWidth, slotHeight, jpegQuality);

        if (i + 1 < images.size()) {
          BufferedImage bottom = images.get(i + 1);
          drawImageInSlot(document, page, bottom, MARGIN_HORIZONTAL_PT, MARGIN_VERTICAL_PT,
              slotWidth, slotHeight, jpegQuality);
        }
      }

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      document.save(out);
      return out.toByteArray();
    } finally {
      document.close();
    }
  }

  private static void drawImageInSlot(PDDocument document, PDPage page, BufferedImage image,
      float slotX, float slotY, float slotWidth, float slotHeight, float jpegQuality)
      throws IOException {

    if (image == null) {
      return;
    }

    float imgWidth = image.getWidth();
    float imgHeight = image.getHeight();
    if (imgWidth <= 0f || imgHeight <= 0f) {
      return;
    }

    float innerWidth = Math.max(0f, slotWidth - (2f * SLOT_PADDING_PT));
    float innerHeight = Math.max(0f, slotHeight - (2f * SLOT_PADDING_PT));
    float drawWidth;
    float drawHeight;
    if (innerWidth > 0f && innerHeight > 0f) {
      float scale = Math.min(innerWidth / imgWidth, innerHeight / imgHeight);
      drawWidth = imgWidth * scale;
      drawHeight = imgHeight * scale;
    } else {
      float scale = Math.min(slotWidth / imgWidth, slotHeight / imgHeight);
      drawWidth = imgWidth * scale;
      drawHeight = imgHeight * scale;
    }

    float drawX = slotX + ((slotWidth - drawWidth) / 2f);
    float drawY = slotY + ((slotHeight - drawHeight) / 2f);

    int maxWidthPx = ptToPx(drawWidth, EMBED_DPI);
    int maxHeightPx = ptToPx(drawHeight, EMBED_DPI);
    BufferedImage embedImage = prepareJpegImage(image, maxWidthPx, maxHeightPx);

    PDImageXObject pdImage = JPEGFactory.createFromImage(document, embedImage, jpegQuality);
    PDPageContentStream contentStream = new PDPageContentStream(document, page,
        PDPageContentStream.AppendMode.APPEND, true, true);
    try {
      contentStream.drawImage(pdImage, drawX, drawY, drawWidth, drawHeight);
    } finally {
      contentStream.close();
    }
  }

  /**
   * JPEG embed용 RGB 이미지. 슬롯 표시 크기({@link #EMBED_DPI})를 넘는 픽셀은 축소한다.
   */
  static BufferedImage prepareJpegImage(BufferedImage source, int maxWidthPx, int maxHeightPx) {
    BufferedImage rgb = toRgb(source);
    return scaleDownToFit(rgb, maxWidthPx, maxHeightPx);
  }

  static BufferedImage toRgb(BufferedImage src) {
    if (src.getType() == BufferedImage.TYPE_INT_RGB) {
      return src;
    }
    BufferedImage rgb = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
    Graphics2D g = rgb.createGraphics();
    try {
      g.setColor(Color.WHITE);
      g.fillRect(0, 0, src.getWidth(), src.getHeight());
      g.drawImage(src, 0, 0, null);
    } finally {
      g.dispose();
    }
    return rgb;
  }

  static BufferedImage scaleDownToFit(BufferedImage src, int maxWidthPx, int maxHeightPx) {
    int w = src.getWidth();
    int h = src.getHeight();
    if (w <= maxWidthPx && h <= maxHeightPx) {
      return src;
    }
    float scale = Math.min((float) maxWidthPx / w, (float) maxHeightPx / h);
    int nw = Math.max(1, Math.round(w * scale));
    int nh = Math.max(1, Math.round(h * scale));
    BufferedImage out = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = out.createGraphics();
    try {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g.drawImage(src, 0, 0, nw, nh, null);
    } finally {
      g.dispose();
    }
    return out;
  }

  static int ptToPx(float pt, float dpi) {
    return Math.max(1, Math.round(pt / 72f * dpi));
  }

  private static float mmToPt(float mm) {
    return mm * 72f / 25.4f;
  }

}
