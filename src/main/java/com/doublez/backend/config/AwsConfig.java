package com.doublez.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class AwsConfig {
	
	@Value("${aws.access.key.id}")
	private String awsAccessKeyId;
	
	@Value("${aws.secret.access.key}")
	private String awsSecretAccessKey;
	
	@Value("${aws.region}")
	private String awsRegion;
	
	@Bean
	S3Presigner s3Presigner() {
		
		AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(awsAccessKeyId, awsSecretAccessKey);
		
		return S3Presigner.builder()
				.region(Region.of(awsRegion))
				.credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
				.build();
		
	}

}
