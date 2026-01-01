package egovframework.cmm.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import com.amazonaws.services.s3.AmazonS3;
import egovframework.cmm.service.FileVO;
import egovframework.cmm.service.NextcloudDavService;
import egovframework.raw.dto.PicDTO;
import egovframework.rte.fdl.idgnr.EgovIdGnrService;
import egovframework.rte.fdl.property.EgovPropertyService;
import net.coobird.thumbnailator.Thumbnails;

@Component("MinIoFileMngUtil")
public class MinIoFileMngUtil {

  @Resource(name = "propertiesService")
  protected EgovPropertyService propertyService;
  
  @Resource(name = "egovFileIdGnrService")
  private EgovIdGnrService idgenService;
  
  @Resource(name = "NextcloudDavService")
  private NextcloudDavService nextcloudDavService;
  
  // 차단할 확장자 리스트
  private static final List<String> BLOCKED_EXTENSIONS = Arrays.asList(
      "exe", "dll", "js", "php", "jsp", "vbs", "bat", "sh", "jar",
      "com", "cmd", "sys", "scr", "msi", "iso", "img", "vhd", "dmg"
  );
  
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }
  
  public List<FileVO> parseFile(
      List<MultipartFile> files,
      String KeyStr,
      int fileKeyParam,
      String atchFileId,
      String storePath
) throws Exception {

  int fileKey = fileKeyParam;

  String storePathString = "";
  String atchFileIdString = "";

  // 현재 날짜 구하기
  LocalDate now = LocalDate.now();
  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM");
  String formatedNow = now.format(formatter);

  // === (1) storePathString을 "MinIO prefix" 용도로 사용 ===
  // 기존 로컬 경로 대신, MinIO 내부 폴더(프리픽스) 개념으로 바꿈
  if ("".equals(storePath) || storePath == null) {
      storePathString = KeyStr + "/" + formatedNow;   // 예: RAW/2025/12
  } else {
      storePathString = storePath;
  }

  if ("".equals(atchFileId) || atchFileId == null) {
      atchFileIdString = idgenService.getNextStringId();
  } else {
      atchFileIdString = atchFileId;
  }

  // === (2) MinIO S3 Client 준비 ===
  String endpoint   = propertyService.getString("Globals.minio.endpoint");
  String accessKey  = propertyService.getString("Globals.minio.accessKey");
  String secretKey  = propertyService.getString("Globals.minio.secretKey");
  String bucketName = propertyService.getString("Globals.minio.bucket");

  AmazonS3 s3 = MinioS3ClientFactory.create(endpoint, accessKey, secretKey);

  // 버킷 없으면 생성
  if (!s3.doesBucketExistV2(bucketName)) {
      s3.createBucket(bucketName);
  }

  MultipartFile file;
  List<FileVO> result  = new ArrayList<>();
  FileVO fvo;

  int i = 0;
  while (files.size() > i) {

      file = files.get(i);
      String orginFileName = file.getOriginalFilename();

      // 원 파일명이 없는 경우 skip
      if ("".equals(orginFileName)) {
          i++;
          continue;
      }

      int index = orginFileName.lastIndexOf(".");
      String fileExt = orginFileName.substring(index + 1);

      // 기존 newName 생성 로직 유지
      String newName = EgovStringUtil.getTimeStamp() + fileKey;

      long _size = file.getSize();

      // === (3) MinIO objectKey 생성 ===
      // storePathString = prefix
      // newName = 서버측 파일명
      // fileExt는 기존대로 FileVO에 따로 저장
      String safeOriginal = orginFileName.replaceAll("[\\\\/:*?\"<>|]", "_");
      String objectKey = storePathString + "/" + safeOriginal + "_" + newName;

      // === (4) MinIO 업로드 ===
      /*
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(_size);
        meta.setContentType(file.getContentType());
        try (InputStream is = file.getInputStream()) {
            s3.putObject(bucketName, objectKey, is, meta);
        }
      */
      // === (4) 폴더 보장(MKCOL) ===
      nextcloudDavService.ensureFolder(storePathString);
      
      // 업로드(Nextcloud가 MinIO에 저장 + 인덱스 등록)
      String davPath = nextcloudDavService.upload(file, objectKey);

      // === (5) FileVO 생성 (DB 저장은 기존 insert/update 그대로) ===
      fvo = new FileVO();
      fvo.setFileExtsn(fileExt);
      fvo.setFileStreCours("NEXTCLOUD_DAV");
      fvo.setFileMg(Long.toString(_size));
      fvo.setOrignlFileNm(orginFileName);
      fvo.setStreFileNm(davPath);
      fvo.setAtchFileId(atchFileIdString);
      fvo.setFileSn(String.valueOf(fileKey));

      result.add(fvo);

      fileKey++;
      i++;
  }

  return result;
}


  public List<FileVO> parsePicFile(
      List<PicDTO> files,
      String KeyStr,
      int fileKeyParam,
      String atchFileId,
      String storePath
) throws Exception {

  int fileKey = fileKeyParam;

  String storePathString = "";
  String atchFileIdString = "";

  // 현재 날짜 구하기
  LocalDate now = LocalDate.now();
  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM");
  String formatedNow = now.format(formatter);

  // === (1) storePathString을 "MinIO prefix"로 사용 ===
  if ("".equals(storePath) || storePath == null) {
      storePathString = formatedNow + "/" + KeyStr;   // 예: 2025/12/RAW
  } else {
      storePathString = storePath; // storePath도 prefix로 취급
  }

  if ("".equals(atchFileId) || atchFileId == null) {
      atchFileIdString = idgenService.getNextStringId();
  } else {
      atchFileIdString = atchFileId;
  }

  // === (2) MinIO S3 Client 준비 ===
  String endpoint   = propertyService.getString("Globals.minio.endpoint");
  String accessKey  = propertyService.getString("Globals.minio.accessKey");
  String secretKey  = propertyService.getString("Globals.minio.secretKey");
  String bucketName = propertyService.getString("Globals.minio.bucket");

  AmazonS3 s3 = MinioS3ClientFactory.create(endpoint, accessKey, secretKey);

  if (!s3.doesBucketExistV2(bucketName)) {
      s3.createBucket(bucketName);
  }

  List<FileVO> result  = new ArrayList<>();
  FileVO fvo;

  int i = 0;
  while (files.size() > i) {

      MultipartFile file = files.get(i).getImage();

      String orginFileName = "";
      String fileExt = "";
      String newName = "";
      long _size = 0;

      if (file != null) {

          orginFileName = file.getOriginalFilename();
          if ("".equals(orginFileName)) {
              i++;
              continue;
          }

          int index = orginFileName.lastIndexOf(".");
          fileExt = orginFileName.substring(index + 1);

          // 기존 naming 유지
          newName = EgovStringUtil.getTimeStamp() + fileKey + "." + fileExt;

          // === (3) 리사이즈/원본 결정 ===
          BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
          int newWidth = bufferedImage.getWidth();
          int newHeight = bufferedImage.getHeight();

          byte[] uploadBytes;

          // 350KB 이하는 원본 그대로
          if (file.getSize() <= 358400) {

              uploadBytes = file.getBytes();
              _size = uploadBytes.length;

          } else {

              if (file.getSize() < 512000) {
                  newWidth = (int) (bufferedImage.getWidth() * 0.5);
                  newHeight = (bufferedImage.getHeight() * newWidth) / bufferedImage.getWidth();
              } else if (file.getSize() < 2097152) {
                  newWidth = (int) (bufferedImage.getWidth() * 0.4);
                  newHeight = (bufferedImage.getHeight() * newWidth) / bufferedImage.getWidth();
              } else {
                  newWidth = (int) (bufferedImage.getWidth() * 0.3);
                  newHeight = (bufferedImage.getHeight() * newWidth) / bufferedImage.getWidth();
              }

              // === (4) Thumbnailator를 메모리에서 실행 ===
              ByteArrayOutputStream baos = new ByteArrayOutputStream();

               // Thumbnailator 올바른 사용법
               Thumbnails.of(bufferedImage)
                       .size(newWidth, newHeight)
                       .outputFormat(fileExt)      // jpg/png 등 원 확장자 유지
                       .toOutputStream(baos);
    
               uploadBytes = baos.toByteArray();
               _size = uploadBytes.length;

          }

          // === (5) MinIO objectKey 만들고 업로드 ===
          String safeOriginal = orginFileName.replaceAll("[\\\\/:*?\"<>|]", "_");
          String objectKey = storePathString + "/" + safeOriginal + "_" + newName;

          /*
          ObjectMetadata meta = new ObjectMetadata();
          meta.setContentLength(_size);
          meta.setContentType(file.getContentType());

          try (InputStream is = new ByteArrayInputStream(uploadBytes)) {
              s3.putObject(bucketName, objectKey, is, meta);
          }
          */
          // 폴더 보장(MKCOL)
          nextcloudDavService.ensureFolder(storePathString);
          // 업로드(Nextcloud가 MinIO에 저장 + 인덱스 등록)
          String davPath = nextcloudDavService.upload(file, objectKey);
          
          // === (6) FileVO 생성 ===
          fvo = new FileVO();
          fvo.setFileExtsn(fileExt);
          fvo.setFileStreCours("NEXTCLOUD_DAV");
          fvo.setFileMg(Long.toString(_size));
          fvo.setOrignlFileNm(orginFileName);
          fvo.setStreFileNm(davPath);
          fvo.setAtchFileId(atchFileIdString);
          fvo.setFileSn(String.valueOf(fileKey));
          fvo.setFileOrdr(files.get(i).getFileOrdr() == 0 ? fileKey : files.get(i).getFileOrdr());
          fvo.setFileCn(files.get(i).getTitle());
          fvo.setFileMemo(files.get(i).getMode());

          result.add(fvo);

          fileKey++;
      }

      i++;
  }

  return result;
}

  /**
   * 첨부파일에 대한 목록 정보를 취득한다. (한건) - MinIO/Nextcloud 방식
   *
   * - storePathString: Nextcloud 내부 폴더(prefix) 개념 (예: /COMPANY)
   * - objectKey: prefix + "/" + 원본파일명_타임스탬프키
   * - 업로드는 Nextcloud WebDAV로 수행 (Nextcloud가 ObjectStore(MinIO)에 저장)
   */
  public FileVO parseFile(
          MultipartFile file,
          String KeyStr,
          int fileKeyParam,
          String atchFileId,
          String storePath
  ) throws Exception {

      int fileKey = fileKeyParam;

      String storePathString = "";
      String atchFileIdString = "";

      // 현재 날짜 구하기
      LocalDate now = LocalDate.now();
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM");
      String formatedNow = now.format(formatter);

      // === (1) storePathString을 "MinIO prefix" 용도로 사용 ===
      // 기존 로컬 경로 대신, MinIO 내부 폴더(프리픽스) 개념으로 바꿈
      if ("".equals(storePath) || storePath == null) {
          storePathString = KeyStr + "/" + formatedNow;   // 예: RAW/2025/12
      } else {
          storePathString = storePath;
      }

      if ("".equals(atchFileId) || atchFileId == null) {
          atchFileIdString = idgenService.getNextStringId();
      } else {
          atchFileIdString = atchFileId;
      }

      FileVO fvo = new FileVO();

      // 파일이 없으면 빈 VO 반환(기존 동작 유지)
      if (ObjectUtils.isEmpty(file)) {
          return fvo;
      }

      String orginFileName = file.getOriginalFilename();

      // 원 파일명이 없는 경우 skip (단건이므로 빈 VO 반환)
      if ("".equals(orginFileName)) {
          return fvo;
      }
      
      int index = orginFileName.lastIndexOf(".");
      String fileExt = orginFileName.substring(index + 1);

      // 차단된 확장자 체크(기존 유지)
      if (!"".equals(fileExt) && BLOCKED_EXTENSIONS.contains(fileExt.toLowerCase())) {
          throw new Exception("차단된 확장자입니다: " + fileExt);
      }

      // newName 생성(기존 유지)
      String newName = EgovStringUtil.getTimeStamp() + fileKey;
      long _size = file.getSize();

      // === (2) objectKey 생성 ===
      // 다건과 동일한 규칙: "prefix/원본파일명_타임스탬프"
      String safeOriginal = orginFileName.replaceAll("[\\\\/:*?\"<>|]", "_");
      String objectKey = storePathString + "/" + safeOriginal + "_" + newName;

      // === (3) 폴더 보장 + 업로드(Nextcloud -> MinIO 저장 + 인덱스 등록) ===
      nextcloudDavService.ensureFolder(storePathString);

      // upload 결과는 davPath로 받는다고 했으니 그대로 사용
      String davPath = nextcloudDavService.upload(file, objectKey);

      // === (4) FileVO 구성 ===
      fvo.setFileExtsn(fileExt);
      fvo.setFileStreCours("NEXTCLOUD_DAV");     // 다건과 통일
      fvo.setFileMg(Long.toString(_size));
      fvo.setOrignlFileNm(orginFileName);
      fvo.setStreFileNm(davPath);               // ★ 로컬 newName 대신 davPath 저장
      fvo.setAtchFileId(atchFileIdString);
      fvo.setFileSn(String.valueOf(fileKey));

      return fvo;
  }

}
