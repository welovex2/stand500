package egovframework.cmm.service.impl;

import java.util.Date;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import egovframework.cmm.service.MinioFileService;
import egovframework.cmm.util.MinioS3ClientFactory;
import egovframework.rte.fdl.property.EgovPropertyService;

@Service("minioFileService")
public class MinioFileServiceImpl implements MinioFileService {
  
  @Resource(name="propertiesService")
  private EgovPropertyService propertyService;

  private AmazonS3 s3;
  private String bucketName;

  @PostConstruct
  public void init() {
      String endpoint  = propertyService.getString("Globals.minio.endpoint");
      String accessKey = propertyService.getString("Globals.minio.accessKey");
      String secretKey = propertyService.getString("Globals.minio.secretKey");
      bucketName = propertyService.getString("Globals.minio.bucket");

      s3 = MinioS3ClientFactory.create(endpoint, accessKey, secretKey);

      if (!s3.doesBucketExistV2(bucketName)) {
          s3.createBucket(bucketName);
      }
  }

  public String getPresignedGetUrl(String objectKey, int minutes) {
      Date expiration = new Date(System.currentTimeMillis() + 1000L * 60L * minutes);
      return s3.generatePresignedUrl(bucketName, objectKey, expiration, HttpMethod.GET).toString();
  }
  
}
