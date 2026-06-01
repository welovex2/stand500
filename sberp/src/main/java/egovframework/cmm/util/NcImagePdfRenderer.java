package egovframework.cmm.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * A4 세로 · 페이지당 2장(위·아래) PDF 렌더러.
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

  private NcImagePdfRenderer() {}

  public static byte[] renderTwoPerPage(List<BufferedImage> images) throws IOException {
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
            pageHeight - MARGIN_VERTICAL_PT - slotHeight, slotWidth, slotHeight);

        if (i + 1 < images.size()) {
          BufferedImage bottom = images.get(i + 1);
          drawImageInSlot(document, page, bottom, MARGIN_HORIZONTAL_PT, MARGIN_VERTICAL_PT,
              slotWidth, slotHeight);
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
      float slotX, float slotY, float slotWidth, float slotHeight) throws IOException {

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

    PDImageXObject pdImage = LosslessFactory.createFromImage(document, image);
    PDPageContentStream contentStream = new PDPageContentStream(document, page,
        PDPageContentStream.AppendMode.APPEND, true, true);
    try {
      contentStream.drawImage(pdImage, drawX, drawY, drawWidth, drawHeight);
    } finally {
      contentStream.close();
    }
  }

  private static float mmToPt(float mm) {
    return mm * 72f / 25.4f;
  }

}
