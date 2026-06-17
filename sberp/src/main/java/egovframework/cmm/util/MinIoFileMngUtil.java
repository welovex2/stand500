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

      PicDTO pic = files.get(i);
      MultipartFile file = pic.getImage();
      String title = pic.getTitle();

      boolean isGraph = isGraph(picId, title);     // 시험그래프
      boolean isEquipment = "14".equals(picId);    // 시험기자재
      boolean isMeasure = "19".equals(picId);      // TEL 측정사진

      // 저장 폴더 결정
      String storePathString = resolvePicStorePath(storePath, isGraph, isEquipment);

      String orginFileName = "";
      String fileExt = "";
      long _size = 0;
      String davPath = "";

      if (file != null) {

        orginFileName = file.getOriginalFilename();
        if ("".equals(orginFileName)) {
          i++;
          continue;
        }

        fileExt = orginFileName.substring(orginFileName.lastIndexOf(".") + 1);
        _size = file.getSize();

        if (isGraph || isMeasure) {
          // 시험그래프/측정사진: 원본 파일명 사용 / 자동 넘버링 없이 중복 시에만 번호
          // (측정사진은 title 이 화면용 코드값이라 파일명으로 쓰지 않고 원본 파일명 사용)
          davPath = uploadAutoRename(file, storePathString, sanitize(stripExt(orginFileName)),
              fileExt);
        } else if (isEquipment) {
          // 시험기자재: Title 사용(없으면 원본 파일명) / 자동 넘버링 없이 중복 시에만 번호
          String baseName = !ObjectUtils.isEmpty(title) ? sanitize(title)
              : sanitize(stripExt(orginFileName));
          davPath = uploadAutoRename(file, storePathString, baseName, fileExt);
        } else {
          // 기본: PicType 설명(없으면 원본 파일명) + 자동 넘버링
          PicType type = PicType.fromId(picId);
          String baseName =
              type != null ? sanitize(type.getDescription()) : sanitize(stripExt(orginFileName));
          String numberSuffix = (fileKey == 0) ? "" : String.valueOf(fileKey);
          davPath = uploadStandard(file, storePathString, baseName, numberSuffix, fileExt);
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
      fvo.setFileOrdr(pic.getFileOrdr() == 0 ? fileKey : pic.getFileOrdr());
      fvo.setFileCn(title);
      fvo.setFileMemo(pic.getMode());
      result.add(fvo);

      fileKey++;
      i++;
    }

    return result;

  }

  /** 파일명/경로에 사용할 수 없는 문자를 _ 로 치환 */
  private String sanitize(String name) {
    return name == null ? "" : name.replaceAll("[\\\\/:*?\"<>|]", "_");
  }

  /** 확장자를 제외한 파일명 반환 */
  private String stripExt(String fileName) {
    int idx = fileName.lastIndexOf(".");
    return idx < 0 ? fileName : fileName.substring(0, idx);
  }

  /** 시험그래프 여부: picId 14/19 가 아니면서 title 이 "3" 인 경우 */
  private boolean isGraph(String picId, String title) {
    return !"14".equals(picId) && !"19".equals(picId) && "3".equals(title);
  }

  /** picId 별 저장 폴더 결정 */
  private String resolvePicStorePath(String storePath, boolean isGraph, boolean isEquipment) {
    if (isGraph) {
      // 시험그래프: 마지막 폴더(03.시험사진)를 02.데이터/측정데이터로 교체
      return storePath.replaceAll("/03\\.시험사진$", "/02.데이터/측정데이터");
    }
    if (isEquipment) {
      // 시험기자재: testNo/03.시험사진을 걷어내고 folderName 하위의 00.신청서 및 공통/01.제품사진으로 교체
      return storePath.replaceAll("/[^/]+/03\\.시험사진$", "/00.신청서 및 공통/01.제품사진");
    }
    return storePath;
  }

  /** 자동 넘버링 없이, 파일명 중복 시에만 뒤에 (n) 을 붙여 업로드 */
  private String uploadAutoRename(MultipartFile file, String storePathString, String baseName,
      String fileExt) throws Exception {
    String extForName = fileExt.isEmpty() ? "" : "." + fileExt;
    int count = 0;
    while (true) {
      String currentName =
          (count == 0) ? baseName + extForName : baseName + " (" + count + ")" + extForName;
      String objectKey = storePathString + "/" + currentName;
      try {
        nextcloudFolderService.ensureFolder(storePathString);
        return nextcloudDavService.uploadIfNotExists(file, objectKey);
      } catch (DavAlreadyExistsException e) {
        count++;
        if (count > 100)
          throw new Exception("파일 업로드 재시도 횟수가 너무 많습니다.");
      }
    }
  }

  /** 파일명 + 자동 넘버링(numberSuffix) 으로 업로드 */
  private String uploadStandard(MultipartFile file, String storePathString, String baseName,
      String numberSuffix, String fileExt) throws Exception {
    String finalFileName =
        baseName + (numberSuffix.isEmpty() ? "" : " " + numberSuffix) + "." + fileExt;
    String objectKey = storePathString + "/" + finalFileName;
    nextcloudFolderService.ensureFolder(storePathString);
    return nextcloudDavService.upload(file, objectKey);
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
