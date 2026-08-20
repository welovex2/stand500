package egovframework.cmm.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * A4 세로 · 페이지당 2장(위·아래) PDF 렌더러.
 * <p>용량 절감: 슬롯 표시 크기 기준 {@value #EMBED_DPI} DPI 로 다운스케일 후 JPEG 유손실 embed.
 * <p>각 사진 아래에는 파일명 라벨을 가운데 정렬로 표기한다(폰트 지정 시).
 * <p>자동 제품라벨은 비트맵이 아니라 NanumGothic 텍스트(+KC 이미지)로 슬롯에 그린다.
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

  /** 사진 아래 파일명 라벨 영역 높이 (pt) */
  private static final float LABEL_AREA_PT = mmToPt(7f);
  /** 라벨 기본 글자 크기 (pt) */
  private static final float LABEL_FONT_SIZE = 9f;
  /** 라벨 최소 글자 크기 (pt) — 너비 초과 시 축소 하한 */
  private static final float LABEL_MIN_FONT_SIZE = 6f;

  /**
   * 성적서 CSS px → PDF pt (96dpi 기준: 12px = 9pt).
   * report_tel_base.css .product_label font-size:12px / line-height:18px 와 동일 비율.
   */
  private static final float CSS_PX_TO_PT = 72f / 96f;

  /** PDF embed 해상도 (표시 크기 대비). 슬롯 선명도 우선 (이전 150 DPI 대비 약 3배 픽셀). */
  static final float EMBED_DPI = 260f;

  /** JPEG 품질 후보 (높음→낮음). 목표 용량 초과 시 순차 적용. */
  private static final float[] JPEG_QUALITIES = {0.88f, 0.82f, 0.75f};

  /** 페이지당 목표 PDF 용량 (bytes). 4페이지 ≈ 1.8MB 기준. */
  private static final long TARGET_BYTES_PER_PAGE = 450L * 1024L;

  private NcImagePdfRenderer() {}

  public static byte[] renderTwoPerPage(List<BufferedImage> images) throws IOException {
    return renderTwoPerPage(images, null, null, null, null, null);
  }

  /**
   * @param images 페이지에 배치할 이미지(순서대로). 자동 제품라벨 슬롯은 null.
   * @param labels 각 슬롯 아래 표기할 라벨
   * @param fontFile 파일명 라벨용 TrueType 폰트
   */
  public static byte[] renderTwoPerPage(List<BufferedImage> images, List<String> labels,
      File fontFile) throws IOException {
    return renderTwoPerPage(images, null, labels, fontFile, null, null);
  }

  /**
   * @param productLabels 자동 제품라벨 슬롯 데이터. 인덱스별 null 이면 사진 슬롯.
   * @param regularFont NanumGothic-Regular (성적서 nanum)
   * @param boldFont NanumGothic-Bold (하단 인증문구)
   * @param kcMarkFile label.jpg
   */
  public static byte[] renderTwoPerPage(List<BufferedImage> images,
      List<ProductLabelContent> productLabels, List<String> labels, File regularFont,
      File boldFont, File kcMarkFile) throws IOException {

    int slotCount = slotCount(images, productLabels);
    if (slotCount <= 0) {
      throw new IllegalArgumentException("images required");
    }

    int pageCount = (slotCount + 1) / 2;
    long targetBytes = pageCount * TARGET_BYTES_PER_PAGE;

    byte[] result = null;
    for (float quality : JPEG_QUALITIES) {
      result = render(images, productLabels, labels, regularFont, boldFont, kcMarkFile, quality);
      if (result.length <= targetBytes) {
        return result;
      }
    }
    return result;
  }

  private static int slotCount(List<BufferedImage> images, List<ProductLabelContent> productLabels) {
    int n = 0;
    if (images != null) {
      n = Math.max(n, images.size());
    }
    if (productLabels != null) {
      n = Math.max(n, productLabels.size());
    }
    return n;
  }

  static byte[] render(List<BufferedImage> images, List<ProductLabelContent> productLabels,
      List<String> labels, File regularFontFile, File boldFontFile, File kcMarkFile,
      float jpegQuality) throws IOException {

    int slotCount = slotCount(images, productLabels);
    if (slotCount <= 0) {
      throw new IllegalArgumentException("images required");
    }

    PDDocument document = new PDDocument();
    try {
      PDFont regularFont = loadFont(document, regularFontFile);
      PDFont boldFont = loadFont(document, boldFontFile);
      if (boldFont == null) {
        boldFont = regularFont;
      }
      PDFont captionFont = regularFont;
      PDImageXObject kcImage = loadKcImage(document, kcMarkFile);

      PDRectangle pageSize = PDRectangle.A4;
      float pageWidth = pageSize.getWidth();
      float pageHeight = pageSize.getHeight();

      float slotWidth = pageWidth - (2f * MARGIN_HORIZONTAL_PT);
      float usableHeight = pageHeight - (2f * MARGIN_VERTICAL_PT) - GAP_PT;
      float slotHeight = usableHeight / 2f;

      for (int i = 0; i < slotCount; i += 2) {
        PDPage page = new PDPage(pageSize);
        document.addPage(page);

        drawSlot(document, page, images, productLabels, labels, i, MARGIN_HORIZONTAL_PT,
            pageHeight - MARGIN_VERTICAL_PT - slotHeight, slotWidth, slotHeight, jpegQuality,
            captionFont, regularFont, boldFont, kcImage);

        if (i + 1 < slotCount) {
          drawSlot(document, page, images, productLabels, labels, i + 1, MARGIN_HORIZONTAL_PT,
              MARGIN_VERTICAL_PT, slotWidth, slotHeight, jpegQuality, captionFont, regularFont,
              boldFont, kcImage);
        }
      }

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      document.save(out);
      return out.toByteArray();
    } finally {
      document.close();
    }
  }

  private static void drawSlot(PDDocument document, PDPage page, List<BufferedImage> images,
      List<ProductLabelContent> productLabels, List<String> labels, int index, float slotX,
      float slotY, float slotWidth, float slotHeight, float jpegQuality, PDFont captionFont,
      PDFont regularFont, PDFont boldFont, PDImageXObject kcImage) throws IOException {

    ProductLabelContent pl = productLabelAt(productLabels, index);
    String caption = labelAt(labels, index);
    if (pl != null) {
      drawProductLabelInSlot(document, page, pl, slotX, slotY, slotWidth, slotHeight, caption,
          captionFont, regularFont, boldFont, kcImage);
      return;
    }
    BufferedImage image = imageAt(images, index);
    drawImageInSlot(document, page, image, slotX, slotY, slotWidth, slotHeight, jpegQuality,
        caption, captionFont);
  }

  private static String labelAt(List<String> labels, int index) {
    if (labels == null || index < 0 || index >= labels.size()) {
      return null;
    }
    return labels.get(index);
  }

  private static BufferedImage imageAt(List<BufferedImage> images, int index) {
    if (images == null || index < 0 || index >= images.size()) {
      return null;
    }
    return images.get(index);
  }

  private static ProductLabelContent productLabelAt(List<ProductLabelContent> list, int index) {
    if (list == null || index < 0 || index >= list.size()) {
      return null;
    }
    return list.get(index);
  }

  private static PDFont loadFont(PDDocument document, File fontFile) {
    if (fontFile == null || !fontFile.isFile()) {
      return null;
    }
    InputStream in = null;
    try {
      in = new BufferedInputStream(new FileInputStream(fontFile));
      return PDType0Font.load(document, in, true);
    } catch (Exception e) {
      return null;
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException ignore) {
          // ignore
        }
      }
    }
  }

  private static PDImageXObject loadKcImage(PDDocument document, File kcMarkFile) {
    if (kcMarkFile == null || !kcMarkFile.isFile()) {
      return null;
    }
    try {
      BufferedImage kc = javax.imageio.ImageIO.read(kcMarkFile);
      if (kc == null) {
        return null;
      }
      return JPEGFactory.createFromImage(document, toRgb(kc), 0.92f);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * 성적서 .product_label 과 동일한 CSS 비율의 텍스트 라벨을 사진 슬롯에 contain+중앙 배치.
   * 글자는 PDF 텍스트(NanumGothic) — 비트맵이 아님.
   */
  private static void drawProductLabelInSlot(PDDocument document, PDPage page,
      ProductLabelContent content, float slotX, float slotY, float slotWidth, float slotHeight,
      String caption, PDFont captionFont, PDFont regularFont, PDFont boldFont,
      PDImageXObject kcImage) throws IOException {

    if (content == null || regularFont == null) {
      return;
    }

    boolean hasCaption = captionFont != null && caption != null && !caption.trim().isEmpty();
    float captionAreaH = hasCaption ? LABEL_AREA_PT : 0f;
    float imageAreaY = slotY + captionAreaH;
    float imageAreaH = slotHeight - captionAreaH;
    float innerW = Math.max(0f, slotWidth - (2f * SLOT_PADDING_PT));
    float innerH = Math.max(0f, imageAreaH - (2f * SLOT_PADDING_PT));

    // report_tel_base.css 수치 (px → pt)
    float fontSize = 12f * CSS_PX_TO_PT;
    float lineH = 18f * CSS_PX_TO_PT;
    float pad = 25f * CSS_PX_TO_PT;
    float kcW = 70f * CSS_PX_TO_PT;
    float kcH = 109f * CSS_PX_TO_PT;
    float textMargin = 25f * CSS_PX_TO_PT;
    float bottomMt = 10f * CSS_PX_TO_PT;

    String[] lines = content.bodyLines();
    String auth = content.getAuthBottom();
    // 성적서: .product_img tr td p { font-weight:bold } + label_bottom bold
    PDFont textFont = boldFont != null ? boldFont : regularFont;

    float textBlockW = 0f;
    for (String line : lines) {
      textBlockW = Math.max(textBlockW, stringWidth(textFont, line, fontSize));
    }
    if (!auth.isEmpty()) {
      textBlockW = Math.max(textBlockW, stringWidth(textFont, auth, fontSize));
    }

    boolean hasKc = kcImage != null;
    float boxInnerW = (hasKc ? kcW + textMargin : 0f) + textBlockW;
    float boxInnerH = Math.max(hasKc ? kcH : 0f, lines.length * lineH);
    float contentW = pad + boxInnerW + pad;
    float contentH = pad + boxInnerH + (auth.isEmpty() ? 0f : bottomMt + lineH) + pad;

    // 슬롯을 꽉 채우지 않음 — 성적서 CSS(12px→9pt) 실측 크기 이하로만 표시.
    // 슬롯이 더 커도 확대하지 않고(max 1), 넘칠 때만 축소.
    float scale = 1f;
    if (contentW > 0f && contentH > 0f && innerW > 0f && innerH > 0f) {
      float fit = Math.min(innerW / contentW, innerH / contentH);
      if (fit < 1f) {
        scale = fit;
      }
    }
    float drawW = contentW * scale;
    float drawH = contentH * scale;
    float originX = slotX + ((slotWidth - drawW) / 2f);
    float originY = imageAreaY + ((imageAreaH - drawH) / 2f);

    float sFont = fontSize * scale;
    float sLine = lineH * scale;
    float sPad = pad * scale;
    float sKcW = kcW * scale;
    float sKcH = kcH * scale;
    float sTextMargin = textMargin * scale;
    float sBottomMt = bottomMt * scale;

    float boxX = originX + sPad;
    // 로컬 y(상단=0) → PDF y
    float contentTop = originY + drawH;
    float boxTop = contentTop - sPad;

    PDPageContentStream cs = new PDPageContentStream(document, page,
        PDPageContentStream.AppendMode.APPEND, true, true);
    try {
      if (hasKc) {
        float kcPdfY = boxTop - Math.max(sKcH, lines.length * sLine) / 2f - sKcH / 2f;
        // KC를 박스 행 수직 중앙
        float rowH = Math.max(sKcH, lines.length * sLine);
        kcPdfY = boxTop - ((rowH - sKcH) / 2f) - sKcH;
        cs.drawImage(kcImage, boxX, kcPdfY, sKcW, sKcH);
      }

      float textX = boxX + (hasKc ? sKcW + sTextMargin : 0f);
      float rowH = Math.max(hasKc ? sKcH : 0f, lines.length * sLine);
      float textTop = boxTop - ((rowH - lines.length * sLine) / 2f);

      cs.setNonStrokingColor(0f, 0f, 0f);
      for (int i = 0; i < lines.length; i++) {
        float baseline = textTop - (i * sLine) - sFont * 0.8f;
        showText(cs, textFont, sFont, textX, baseline, lines[i]);
      }

      if (!auth.isEmpty()) {
        float authTop = boxTop - rowH - sBottomMt;
        float authBaseline = authTop - sFont * 0.8f;
        showText(cs, textFont, sFont, boxX, authBaseline, auth);
      }

      if (hasCaption) {
        drawCenteredLabel(cs, captionFont, caption.trim(), slotX, slotWidth, slotY, captionAreaH);
      }
    } finally {
      cs.close();
    }
  }

  private static float stringWidth(PDFont font, String text, float fontSize) throws IOException {
    if (font == null || text == null || text.isEmpty()) {
      return 0f;
    }
    try {
      return font.getStringWidth(text) / 1000f * fontSize;
    } catch (IllegalArgumentException e) {
      return 0f;
    }
  }

  private static void showText(PDPageContentStream cs, PDFont font, float fontSize, float x,
      float y, String text) throws IOException {
    if (font == null || text == null || text.isEmpty()) {
      return;
    }
    try {
      cs.beginText();
      cs.setFont(font, fontSize);
      cs.newLineAtOffset(x, y);
      cs.showText(text);
      cs.endText();
    } catch (IllegalArgumentException e) {
      // 폰트 미지원 글자 — 해당 줄 생략
      try {
        cs.endText();
      } catch (Exception ignore) {
        // ignore
      }
    }
  }

  private static void drawImageInSlot(PDDocument document, PDPage page, BufferedImage image,
      float slotX, float slotY, float slotWidth, float slotHeight, float jpegQuality,
      String label, PDFont labelFont)
      throws IOException {

    if (image == null) {
      return;
    }

    float imgWidth = image.getWidth();
    float imgHeight = image.getHeight();
    if (imgWidth <= 0f || imgHeight <= 0f) {
      return;
    }

    boolean hasLabel = labelFont != null && label != null && !label.trim().isEmpty();
    // 라벨을 쓰는 경우 슬롯 하단에 라벨 영역을 확보하고, 이미지는 그 위 영역에 배치.
    float labelAreaHeight = hasLabel ? LABEL_AREA_PT : 0f;
    float imageAreaY = slotY + labelAreaHeight;
    float imageAreaHeight = slotHeight - labelAreaHeight;

    float innerWidth = Math.max(0f, slotWidth - (2f * SLOT_PADDING_PT));
    float innerHeight = Math.max(0f, imageAreaHeight - (2f * SLOT_PADDING_PT));
    float drawWidth;
    float drawHeight;
    if (innerWidth > 0f && innerHeight > 0f) {
      float scale = Math.min(innerWidth / imgWidth, innerHeight / imgHeight);
      drawWidth = imgWidth * scale;
      drawHeight = imgHeight * scale;
    } else {
      float scale = Math.min(slotWidth / imgWidth, imageAreaHeight / imgHeight);
      drawWidth = imgWidth * scale;
      drawHeight = imgHeight * scale;
    }

    float drawX = slotX + ((slotWidth - drawWidth) / 2f);
    float drawY = imageAreaY + ((imageAreaHeight - drawHeight) / 2f);

    int maxWidthPx = ptToPx(drawWidth, EMBED_DPI);
    int maxHeightPx = ptToPx(drawHeight, EMBED_DPI);
    BufferedImage embedImage = prepareJpegImage(image, maxWidthPx, maxHeightPx);

    PDImageXObject pdImage = JPEGFactory.createFromImage(document, embedImage, jpegQuality);
    PDPageContentStream contentStream = new PDPageContentStream(document, page,
        PDPageContentStream.AppendMode.APPEND, true, true);
    try {
      contentStream.drawImage(pdImage, drawX, drawY, drawWidth, drawHeight);
      if (hasLabel) {
        drawCenteredLabel(contentStream, labelFont, label.trim(), slotX, slotWidth, slotY,
            labelAreaHeight);
      }
    } finally {
      contentStream.close();
    }
  }

  /** 라벨 영역(폭 slotWidth) 안에서 가운데 정렬로 텍스트를 그린다. 너비 초과 시 글자 크기 축소. */
  private static void drawCenteredLabel(PDPageContentStream cs, PDFont font, String rawText,
      float slotX, float slotWidth, float slotY, float labelAreaHeight) throws IOException {

    String text = sanitizeLabel(rawText);
    if (text.isEmpty()) {
      return;
    }

    float maxTextWidth = Math.max(1f, slotWidth - mmToPt(2f));
    float fontSize;
    float textWidth;
    try {
      float widthPerPt = font.getStringWidth(text) / 1000f;
      if (widthPerPt <= 0f) {
        return;
      }
      fontSize = LABEL_FONT_SIZE;
      if (widthPerPt * fontSize > maxTextWidth) {
        fontSize = Math.max(LABEL_MIN_FONT_SIZE, maxTextWidth / widthPerPt);
      }
      textWidth = widthPerPt * fontSize;
      // 최소 크기로도 넘치면 말줄임 처리
      if (textWidth > maxTextWidth) {
        text = ellipsize(font, text, fontSize, maxTextWidth);
        textWidth = font.getStringWidth(text) / 1000f * fontSize;
      }
    } catch (IllegalArgumentException e) {
      // 폰트가 표현 못하는 글자 포함 — 라벨 생략
      return;
    }

    float textX = slotX + ((slotWidth - textWidth) / 2f);
    float baselineY = slotY + ((labelAreaHeight - fontSize) / 2f) + (fontSize * 0.2f);

    cs.beginText();
    cs.setNonStrokingColor(0f, 0f, 0f);
    cs.setFont(font, fontSize);
    cs.newLineAtOffset(textX, baselineY);
    cs.showText(text);
    cs.endText();
  }

  private static String ellipsize(PDFont font, String text, float fontSize, float maxWidth)
      throws IOException {
    String ellipsis = "...";
    String body = text;
    while (body.length() > 1) {
      String candidate = body + ellipsis;
      if (font.getStringWidth(candidate) / 1000f * fontSize <= maxWidth) {
        return candidate;
      }
      body = body.substring(0, body.length() - 1);
    }
    return body;
  }

  private static String sanitizeLabel(String s) {
    if (s == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder(s.length());
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '\r' || c == '\n' || c == '\t') {
        sb.append(' ');
      } else if (c >= 0x20 || c >= 0xAC00) {
        sb.append(c);
      }
    }
    return sb.toString().trim();
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
