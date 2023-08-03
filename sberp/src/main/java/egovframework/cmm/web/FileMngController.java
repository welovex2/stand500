package egovframework.cmm.web;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import egovframework.cmm.service.EgovFileMngService;
import egovframework.cmm.service.FileVO;
import io.swagger.annotations.Api;

@Api(tags = {"파일"})
@RestController
@RequestMapping("/file")
public class FileMngController {

    @Resource(name = "EgovFileMngService")
    private EgovFileMngService fileService;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(FileMngController.class);
    
    /**
     * 이미지 첨부파일에 대한 목록을 조회한다.
     *
     * @param fileVO
     * @param atchFileId
     * @param sessionVO
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping("/cmm/fms/selectImageFileInfs.do")
    public List<FileVO> selectImageFileInfs(@RequestParam Map<String, Object> commandMap) throws Exception {
    
    	FileVO fileVO = new FileVO();
    	
		String atchFileId = (String)commandMap.get("atchFileId");
	
		fileVO.setAtchFileId(atchFileId);
		List<FileVO> result = fileService.selectImageFileList(fileVO);
	
		return result;
//		return "cmm/fms/EgovImgFileList";
    }
    
    /**
     * 첨부된 이미지에 대한 미리보기 기능을 제공한다.
     *
     * @param atchFileId
     * @param fileSn
     * @param sessionVO
     * @param model
     * @param response
     * @throws Exception
     */
    @SuppressWarnings("resource")
	@RequestMapping("/getImage.do")
    public void getImageInf(ModelMap model, @RequestParam Map<String, Object> commandMap, HttpServletResponse response) throws Exception {

		//@RequestParam("atchFileId") String atchFileId,
		//@RequestParam("fileSn") String fileSn,
		String atchFileId = (String)commandMap.get("atchFileId");
		String fileSn = (String)commandMap.get("fileSn");

		FileVO vo = new FileVO();

		vo.setAtchFileId(atchFileId);
		vo.setFileSn(fileSn);

		FileVO fvo = fileService.selectFileInf(vo);

		//String fileLoaction = fvo.getFileStreCours() + fvo.getStreFileNm();

		
		//File file = addImageWatermark(new File(fvo.getFileStreCours(), fvo.getStreFileNm()),new File(fvo.getFileStreCours(), fvo.getStreFileNm()), new File(fvo.getFileStreCours(), fvo.getStreFileNm()));
		File file = new File(fvo.getFileStreCours(), fvo.getStreFileNm());
		FileInputStream fis = null; new FileInputStream(file);

		BufferedInputStream in = null;
		ByteArrayOutputStream bStream = null;
		try{
			fis = new FileInputStream(file);
			in = new BufferedInputStream(fis);
			bStream = new ByteArrayOutputStream();
			int imgByte;
			while ((imgByte = in.read()) != -1) {
			    bStream.write(imgByte);
			}

			String type = "";

			if (fvo.getFileExtsn() != null && !"".equals(fvo.getFileExtsn())) {
			    if ("jpg".equals(fvo.getFileExtsn().toLowerCase())) {
				type = "image/jpeg";
			    } else {
				type = "image/" + fvo.getFileExtsn().toLowerCase();
			    }
			    type = "image/" + fvo.getFileExtsn().toLowerCase();

			} else {
				LOGGER.debug("Image fileType is null.");
			}

			response.setHeader("Content-Type", type);
			response.setContentLength(bStream.size());

			bStream.writeTo(response.getOutputStream());

			response.getOutputStream().flush();
			response.getOutputStream().close();


		}catch(Exception e){
			LOGGER.debug("{}", e);
		}finally{
			if (bStream != null) {
				try {
					bStream.close();
				} catch (Exception est) {
					LOGGER.debug("IGNORED: {}", est.getMessage());
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (Exception ei) {
					LOGGER.debug("IGNORED: {}", ei.getMessage());
				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception efis) {
					LOGGER.debug("IGNORED: {}", efis.getMessage());
				}
			}
		}
    }
    
    /**
     * Embeds an image watermark over a source image to produce a watermarked one.
     *
     * @param watermarkImageFile 삽입할 워터마크이미지
     * @param sourceImageFile    원본이미지
     * @param destImageFile      결과이미지
     */
    static File addImageWatermark(File watermarkImageFile, File sourceImageFile, File destImageFile) {
      try {
        BufferedImage sourceImage = ImageIO.read(sourceImageFile);
        BufferedImage watermarkImage = ImageIO.read(watermarkImageFile);

        Graphics2D g2d = (Graphics2D) sourceImage.getGraphics();
        AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f); //워터마크 투명도 조절
        g2d.setComposite(alphaChannel);

        double authHeight;
        authHeight = 0.5;

        int scaledWidth = (int) (sourceImage.getWidth() * 0.15);
        int scaledHeight = (int) (scaledWidth * authHeight);

        // 원본 이미지 크기에 맞게 워크마크 크기 조절
        BufferedImage resizedWatermarkImage = new BufferedImage(scaledWidth, scaledHeight, watermarkImage.getType());

        Graphics2D resizeG2d = resizedWatermarkImage.createGraphics();
        resizeG2d.drawImage(watermarkImage, 0, 0, scaledWidth, scaledHeight, null);
        resizeG2d.dispose();

        // 워터 마크가 삽입 될 좌표 계산
        int topLeftX = (sourceImage.getWidth() - resizedWatermarkImage.getWidth()) / 2;
        int topLeftY = (sourceImage.getHeight() - resizedWatermarkImage.getHeight()) / 2;

        // 워터마크 이미지 삽입
        g2d.drawImage(resizedWatermarkImage, topLeftX, topLeftY, null);

        ImageIO.write(sourceImage, "png", destImageFile);
        g2d.dispose();
        
      } catch (IOException ex) {
        System.err.println(ex);
      }
      
      return destImageFile;
    }
    
}
