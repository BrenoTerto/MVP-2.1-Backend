package com.redepatas.api.config;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

@Configuration
public class S3Config {
    @Value("${aws.access-key-id}")
    private String accessKeyId;

    @Value("${aws.secret-access-key}")
    private String secretAccessKey; 

    @Bean
    public S3Client s3Client(){
        Region region = Region.SA_EAST_1;
        AwsCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        return S3Client.builder()
            .region(region)
            .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
            .build();    
    }

    @Bean
    public S3Presigner s3Presigner() {
        AwsCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        return S3Presigner.builder()
                .region(Region.SA_EAST_1) // Substitua pela região correta
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
}
