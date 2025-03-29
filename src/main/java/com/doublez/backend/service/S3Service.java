package com.doublez.backend.service;

import java.net.URL;
import java.time.Duration;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
public class S3Service {
	
	private final S3Presigner s3Presigner;
	private final String bucketName;
	
	public S3Service(S3Presigner s3Presigner, @Value("${aws.s3.bucket}") String bucketName) {
		this.s3Presigner = s3Presigner;
		this.bucketName = bucketName;
	}
	
	public String generatePresignedUrl(String fileName) {
		// Set expiration time (15 minutes)
		Duration expiration = Duration.ofMinutes(15);
				
		// Create presigned URL request for uploading a file (PUT method)
		PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(expiration)
                .putObjectRequest(p -> p.bucket(bucketName).key(fileName))
                .build();
		
		try {
			PresignedPutObjectRequest presignedPutObjectRequest = s3Presigner.presignPutObject(presignRequest);
			return presignedPutObjectRequest.url().toString();
		} catch (SdkException  e) {
			e.printStackTrace();
			return null;
		}
		
		
	}

}
