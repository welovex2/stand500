package egovframework.cmm.util;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class MinioS3ClientFactory {

  public static AmazonS3 create(String endpoint, String accessKey, String secretKey) {
      String region = "us-east-1";
  
      BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);
  
      return AmazonS3ClientBuilder.standard()
              .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
              .withCredentials(new AWSStaticCredentialsProvider(creds))
              .withPathStyleAccessEnabled(true) // MinIO는 이 옵션 필수
              .build();
  }
  
}
