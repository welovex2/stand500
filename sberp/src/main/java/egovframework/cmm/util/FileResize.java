package egovframework.cmm.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class FileResize {

  // <!-- 이미지변환 라이브러리 // 용량이 200메가정도 차지해서 속도 문제 아니면 보류 -->
  // marvin, MarvinPlugins
//  public static MultipartFile resizeImage(String fileName, String fileFormatName,
//      MultipartFile originalImage) {
//
//    // 사진이 2MB(2,097,152 Byte) 이상일 경우에만 진행
//    if (originalImage.getSize() < 2097152) {
//      return originalImage;
//    }
//
//    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//    try {
//      // MultipartFile -> BufferedImage Convert
//      BufferedImage image = ImageIO.read(originalImage.getInputStream());
//      // newWidth : newHeight = originWidth : originHeight
//      int originWidth = image.getWidth();
//      int originHeight = image.getHeight();
//
//      int targetWidth = (int) (image.getWidth() * 0.8);
//
//      // origin 이미지가 resizing될 사이즈보다 작을 경우 resizing 작업 안 함
//      if (originWidth < targetWidth)
//        return originalImage;
//
//      MarvinImage imageMarvin = new MarvinImage(image);
//
//      Scale scale = new Scale();
//      scale.load();
//      scale.setAttribute("newWidth", targetWidth);
//      scale.setAttribute("newHeight", targetWidth * originHeight / originWidth);
//      scale.process(imageMarvin.clone(), imageMarvin, null, null, false);
//
//      BufferedImage imageNoAlpha = imageMarvin.getBufferedImageNoAlpha();
//
//      ImageIO.write(imageNoAlpha, fileFormatName, baos);
//      baos.flush();
//
//
//    } catch (IOException e) {
//      // throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 리사이즈에 실패했습니다.");
//    }
//    return new MockMultipartFile(fileName, baos.toByteArray());
//  }


  // 이미지 크기 줄이기
  public static MultipartFile resizeImageFile(String fileName, String fileFormatName,
      MultipartFile originalImage)

      throws Exception {

    // 사진이 2MB(2,097,152 Byte) 이상일 경우에만 진행
    if (originalImage.getSize() < 2097152) {
      return originalImage;
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    // 이미지 읽어 오기
    BufferedImage inputImage = ImageIO.read(originalImage.getInputStream());
    
    // 이미지 세로 가로 측정
    int originWidth = inputImage.getWidth();
    int originHeight = inputImage.getHeight();
    // 변경할 가로 길이
    int newWidth = (int) (originWidth * 0.8);

    if (originWidth > newWidth) {
      // 기존 이미지 비율을 유지하여 세로 길이 설정
      int newHeight = (originHeight * newWidth) / originWidth;
      // 이미지 품질 설정
      // Image.SCALE_DEFAULT : 기본 이미지 스케일링 알고리즘 사용
      // Image.SCALE_FAST : 이미지 부드러움보다 속도 우선
      // Image.SCALE_REPLICATE : ReplicateScaleFilter 클래스로 구체화 된 이미지 크기 조절 알고리즘
      // Image.SCALE_SMOOTH : 속도보다 이미지 부드러움을 우선
      // Image.SCALE_AREA_AVERAGING : 평균 알고리즘 사용
      Image resizeImage = inputImage.getScaledInstance(newWidth, newHeight, Image.SCALE_FAST);
      BufferedImage newImage;
      if ("png".equals(fileFormatName.toLowerCase())) {
        newImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
      } else {
        newImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
      }
      Graphics2D graphics = newImage.createGraphics();

      if ("png".equals(fileFormatName.toLowerCase())) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
            RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      }
      graphics.drawImage(resizeImage, 0, 0, null);
      graphics.dispose();
      // 이미지 저장
      ImageIO.write(newImage, fileFormatName, baos);
      baos.flush();

    } else {

    }
    return new MockMultipartFile(fileName, baos.toByteArray());
  }


}
