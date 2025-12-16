package egovframework.cmm.service;

public interface MinioFileService {

  /**
   * objectKey 기반 Presigned GET URL 생성
   * @param objectKey MinIO object key (예: 2025/12/RAW/xxxx.jpg)
   * @param minutes  만료(분)
   */
  String getPresignedGetUrl(String objectKey, int minutes);
  
}
