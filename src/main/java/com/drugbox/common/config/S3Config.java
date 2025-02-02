package com.drugbox.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URISyntaxException;

@Configuration
public class S3Config {

    private String accessKey;
    private String secretKey;
    private String region;

    // @Value 어노테이션의 값들은 s3관련된 계정IAM의 accessKey, secretKey와 S3의 리전을 넣어주면된다.
    public S3Config(@Value("${spring.cloud.aws.credentials.accessKey}")String accessKey,
                    @Value("${spring.cloud.aws.credentials.secretKey}") String secretKey) {

        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = "ap-northeast-2";
    }

    @Bean
    public AwsCredentials basicAWSCredentials() {
        return AwsBasicCredentials.create(accessKey, secretKey);
    }

    @Bean
    public S3Client s3Client(AwsCredentials awsCredentials) throws URISyntaxException {

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider
                        .create(awsCredentials))
                .build();
    }
}
