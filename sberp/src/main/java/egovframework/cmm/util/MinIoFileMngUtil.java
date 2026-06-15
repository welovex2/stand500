package egovframework.cmm.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import egovframework.cmm.service.FileVO;
import egovframework.ncc.dto.UploadPolicy;
import egovframework.ncc.service.NextcloudDavService;
import egovframework.ncc.service.NextcloudFolderService;
import egovframework.ncc.service.impl.NextcloudDavServiceImpl.DavAlreadyExistsException;
import egovframework.raw.dto.PicDTO;
import egovframework.rte.fdl.idgnr.EgovIdGnrService;
import egovframework.rte.fdl.property.EgovPropertyService;

@Component("MinIoFileMngUtil")
public class MinIoFileMngUtil {

  @Resource(name = "propertiesService")
  protected EgovPropertyService propertyService;

  @Resource(name = "egovFileIdGnrService")
  private EgovIdGnrService idgenService;

  @Resource(name = "NextcloudDavService")
  private NextcloudDavService nextcloudDavService;

  @Resource(name = "NextcloudFolderService")
  NextcloudFolderService nextcloudFolderService;

  // 차단할 확장자 리스트
  private static final List<String> BLOCKED_EXTENSIONS =
      Arrays.asList("exe", "dll", "js", "php", "jsp", "vbs", "bat", "sh", "jar", "com", "cmd",
          "sys", "scr", "msi", "iso", "img", "vhd", "dmg");

  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public List<FileVO> parseFile(List<MultipartFile> files, String KeyStr, int fileKeyParam,
      String atchFileId, String storePath) throws Exception {

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
      storePathString = KeyStr + "/" + formatedNow; // 예: RAW/2025/12
    } else {
      storePathString = storePath;
    }

    if ("".equals(atchFileId) || atchFileId == null) {
      atchFileIdString = idgenService.getNextStringId();
    } else {
      atchFileIdString = atchFileId;
    }

    // === (2) MinIO S3 Client 준비 ===
    // String endpoint = propertyService.getString("Globals.minio.endpoint");
    // String accessKey = propertyService.getString("Globals.minio.accessKey");
    // String secretKey = propertyService.getString("Globals.minio.secretKey");
    // String bucketName = propertyService.getString("Globals.minio.bucket");
    //
    // AmazonS3 s3 = MinioS3ClientFactory.create(endpoint, accessKey, secretKey);
    //
    // // 버킷 없으면 생성
    // if (!s3.doesBucketExistV2(bucketName)) {
    // s3.createBucket(bucketName);
    // }

    MultipartFile file;
    List<FileVO> result = new ArrayList<>();
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

      String finalFileName = null; // ✅ 실제 저장된 파일명

      // 원본 그대로 저장 (리사이즈는 report.do 노출 시점에 수행)
      long _size = file.getSize();

      // === (3) MinIO objectKey 생성 ===
      // storePathString = prefix
      // newName = 서버측 파일명
      // fileExt는 기존대로 FileVO에 따로 저장
      String safeOriginal = orginFileName.replaceAll("[\\\\/:*?\"<>|]", "_");

      // === (4) MinIO 업로드 ===
      /*
       * ObjectMetadata meta = new ObjectMetadata(); meta.setContentLength(_size);
       * meta.setContentType(file.getContentType()); try (InputStream is = file.getInputStream()) {
       * s3.putObject(bucketName, objectKey, is, meta); }
       */
      // === (4) 폴더 보장(MKCOL) ===
      nextcloudFolderService.ensureFolder(storePathString);

      // 동일 폴더 동일 파일명 처리: " (1)", " (2)"...
      String davPath = null;

      for (int j = 0; j <= 999; j++) {
        String candidateName =
            (j == 0) ? safeOriginal : ErpDavPathUtil.withWindowsCopySuffix(safeOriginal, j);
        String relativePath = storePathString + "/" + candidateName;

        try {
          davPath = nextcloudDavService.uploadIfNotExists(file, relativePath);
          finalFileName = candidateName;
          break; // 성공
        } catch (RuntimeException ex) {
          if (ex.getMessage() != null && ex.getMessage().contains("PreconditionFailed")) {
            continue;
          }
          throw ex;
        }

      }

      if (davPath == null) {
        throw new RuntimeException("동일 파일명이 너무 많아 업로드할 수 없습니다.");
      }

      // 업로드(Nextcloud가 MinIO에 저장 + 인덱스 등록)
      // String davPath = nextcloudDavService.upload(file, objectKey);

      // === (5) FileVO 생성 (DB 저장은 기존 insert/update 그대로) ===
      fvo = new FileVO();
      fvo.setFileExtsn(fileExt);
      fvo.setFileStreCours("NEXTCLOUD_DAV");
      fvo.setFileMg(Long.toString(_size));
      fvo.setOrignlFileNm(finalFileName);
      fvo.setStreFileNm(davPath);
      fvo.setAtchFileId(atchFileIdString);
      fvo.setFileSn(String.valueOf(fileKey));

      result.add(fvo);

      fileKey++;
      i++;
    }

    return result;
  }


  /**
   * 로데이터 시험결과에 사용
   * 
   * @param files
   * @param KeyStr
   * @param fileKeyParam
   * @param atchFileId
   * @param storePath
   * @return
   * @throws Exception
   */
  public List<FileVO> parsePicFile(List<PicDTO> files, String picId, int fileKey, String atchFileId,
      String storePath) throws Exception {

    String atchFileIdString = "";

    if ("".equals(atchFileId) || atchFileId == null) {
      atchFileIdString = idgenService.getNextStringId();
    } else {
      atchFileIdString = atchFileId;
    }

    // === (2) MinIO S3 Client 준비 ===
    // String endpoint = propertyService.getString("Globals.minio.endpoint");
    // String accessKey = propertyService.getString("Globals.minio.accessKey");
    // String secretKey = propertyService.getString("Globals.minio.secretKey");
    // String bucketName = propertyService.getString("Globals.minio.bucket");
    //
    // AmazonS3 s3 = MinioS3ClientFactory.create(endpoint, accessKey, secretKey);
    //
    // if (!s3.doesBucketExistV2(bucketName)) {
    // s3.createBucket(bucketName);
    // }

    List<FileVO> result = new ArrayList<>();
    FileVO fvo;

    int i = 0;
    while (files.size() > i) {

      MultipartFile file = files.get(i).getImage();

      // fileSn에 따라 폴더 경로 동적 변경
      // 시험그래프 여부 판단
      boolean isGraph = !"14".equals(picId) && files.get(i).getTitle() != null
          && "3".equals(files.get(i).getTitle());
      String storePathString = storePath;
      if (isGraph) {
        // 시험그래프일 경우, storePath의 마지막 폴더를 02.데이터/측정데이터로 교체
        storePathString = storePath.replaceAll("/03\\.시험사진$", "/02.데이터/측정데이터");
      }

      String orginFileName = "";
      String fileExt = "";
      String newName = "";
      long _size = 0;
      String davPath = "";

      if (file != null) {

        orginFileName = file.getOriginalFilename();
        if ("".equals(orginFileName)) {
          i++;
          continue;
        }

        int index = orginFileName.lastIndexOf(".");
        fileExt = orginFileName.substring(index + 1);

        // 기존 naming 유지
        newName = (fileKey == 0) ? "" : String.valueOf(fileKey);

        // 원본 그대로 저장 (리사이즈는 report.do 노출 시점에 수행)
        _size = file.getSize();

        // === (5) MinIO objectKey 만들고 업로드 ===
        String safeOriginal = "";
        boolean isUploaded = false;
        int count = 0;

        // 파일명 구성
        String fileExtForName = fileExt.isEmpty() ? "" : "." + fileExt;

        if (isGraph) {
          // 시험그래프: 원본파일명 기준, AUTO_RENAME
          String originBase = orginFileName.substring(0, orginFileName.lastIndexOf("."))
              .replaceAll("[\\\\/:*?\"<>|]", "_");
          fileExtForName = fileExt.isEmpty() ? "" : "." + fileExt;

          while (!isUploaded) {
            String currentName = (count == 0) ? originBase + fileExtForName
                : originBase + "(" + count + ")" + fileExtForName;

            String objectKey = storePathString + "/" + currentName;
            try {
              // 폴더 보장(MKCOL)
              nextcloudFolderService.ensureFolder(storePathString);
              davPath = nextcloudDavService.uploadIfNotExists(file, objectKey);
              isUploaded = true;
            } catch (DavAlreadyExistsException e) {
              count++;
              if (count > 100)
                throw new Exception("파일 업로드 재시도 횟수가 너무 많습니다.");
            }
          }

        } else {
          // 기존 로직 유지: safeOriginal 결정
          PicType type = PicType.fromId(picId);

          if (type == null) {
            String baseName = orginFileName.substring(0, orginFileName.lastIndexOf("."));
            safeOriginal = baseName.replaceAll("[\\\\/:*?\"<>|]", "_");
          } else {
            safeOriginal = type.getDescription().replaceAll("[\\\\/:*?\"<>|]", "_");
          }

          // newName이 빈값이면 숫자 없이 "파일명.확장자", 있으면 "파일명 숫자.확장자"
          String finalFileName =
              safeOriginal + (newName.isEmpty() ? "" : " " + newName) + "." + fileExt;
          String objectKey = storePathString + "/" + finalFileName;

          /*
           * ObjectMetadata meta = new ObjectMetadata(); meta.setContentLength(_size);
           * meta.setContentType(file.getContentType());
           * 
           * try (InputStream is = new ByteArrayInputStream(uploadBytes)) { s3.putObject(bucketName,
           * objectKey, is, meta); }
           */
          // 폴더 보장(MKCOL)
          nextcloudFolderService.ensureFolder(storePathString);
          // 업로드(Nextcloud가 MinIO에 저장 + 인덱스 등록)
          davPath = nextcloudDavService.upload(file, objectKey);

        }
      }

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
      i++;
    }

    return result;

  }

  /**
   * 첨부파일에 대한 목록 정보를 취득한다. (한건) - MinIO/Nextcloud 방식
   *
   * - storePathString: Nextcloud 내부 폴더(prefix) 개념 (예: /COMPANY) - objectKey: prefix + "/" +
   * 원본파일명_타임스탬프키 - 업로드는 Nextcloud WebDAV로 수행 (Nextcloud가 ObjectStore(MinIO)에 저장)
   */
  public FileVO parseFileInternal(MultipartFile file, String resolvedName, int fileKeyParam,
      String atchFileId, String storePath, UploadPolicy policy) throws Exception {

    int fileKey = fileKeyParam;

    String storePathString = "";
    String atchFileIdString = "";

    // === (1) storePathString을 "MinIO prefix" 용도로 사용 ===
    // 기존 로컬 경로 대신, MinIO 내부 폴더(프리픽스) 개념으로 바꿈
    storePathString = storePath;

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
    if (!"".equals(resolvedName)) {
      orginFileName = resolvedName;
    }

    int index = orginFileName.lastIndexOf(".");
    String fileNameOnly;
    String fileExt;

    // 확장자 없이 들어왔을때
    if (index == -1) {
      fileNameOnly = orginFileName;
      fileExt = "";
    } else {
      fileNameOnly = orginFileName.substring(0, index);
      fileExt = orginFileName.substring(index + 1);
    }

    // 원본 파일명 (Nextcloud에서 허용 안 되는 문자 치환)
    fileNameOnly = fileNameOnly.replaceAll("[\\\\/:*?\"<>|]", "_");

    // 차단된 확장자 체크(기존 유지)
    if (!"".equals(fileExt) && BLOCKED_EXTENSIONS.contains(fileExt.toLowerCase())) {
      throw new Exception("차단된 확장자입니다: " + fileExt);
    }

    // newName 생성(기존 유지)
    long _size = file.getSize();

    // === (3) 폴더 보장 + 업로드(Nextcloud -> MinIO 저장 + 인덱스 등록) ===
    nextcloudFolderService.ensureFolder(storePathString);
    String davPath = "";
    int count = 0;
    boolean isUploaded = false;

    while (!isUploaded) {
      // 처음엔 원본명, 두 번째부터는명(1), 명(2)... 순으로 생성
      String currentName = (count == 0) ? fileNameOnly + (fileExt.isEmpty() ? "" : "." + fileExt)
          : fileNameOnly + "(" + count + ")" + (fileExt.isEmpty() ? "" : "." + fileExt);

      String objectKey = storePathString + "/" + currentName;

      try {
        if (policy == UploadPolicy.AUTO_RENAME)
          // 업로드 시도 (이미 있으면 412 에러 발생)
          davPath = nextcloudDavService.uploadIfNotExists(file, objectKey);
        else
          // 덮어쓰기 허용
          davPath = nextcloudDavService.upload(file, objectKey);

        isUploaded = true; // 업로드 성공 시 루프 탈출
      } catch (DavAlreadyExistsException e) {
        // 412 에러 발생 시 숫자를 올리고 다시 루프
        count++;
        if (count > 100) { // 무한 루프 방지용 안전장치
          throw new Exception("파일 업로드 재시도 횟수가 너무 많습니다.");
        }
      }
    }

    // upload 결과는 davPath로 받는다고 했으니 그대로 사용, 이건 덮어쓰기용도
    // String davPath = nextcloudDavService.upload(file, objectKey);

    // === (4) FileVO 구성 ===
    fvo.setFileExtsn(fileExt);
    fvo.setFileStreCours("NEXTCLOUD_DAV"); // 다건과 통일
    fvo.setFileMg(Long.toString(_size));
    fvo.setOrignlFileNm(orginFileName);
    fvo.setStreFileNm(davPath); // ★ 로컬 newName 대신 davPath 저장
    fvo.setAtchFileId(atchFileIdString);
    fvo.setFileSn(String.valueOf(fileKey));

    return fvo;
  }

  // 덮어쓰기용
  public FileVO parseFile(MultipartFile file, String resolvedName, int fileKeyParam,
      String atchFileId, String storePath, UploadPolicy policy) throws Exception {

    return parseFileInternal(file, resolvedName, fileKeyParam, atchFileId, storePath,
        UploadPolicy.OVERWRITE);
  }

  // 기존사용
  public FileVO parseFile(MultipartFile file, String resolvedName, int fileKeyParam,
      String atchFileId, String storePath) throws Exception {

    return parseFileInternal(file, resolvedName, fileKeyParam, atchFileId, storePath,
        UploadPolicy.AUTO_RENAME);
  }

}
