package com.doublez.backend.service.s3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetBucketVersioningResponse;

@Service
public class S3BucketConfigChecker {
    private static final Logger logger = LoggerFactory.getLogger(S3BucketConfigChecker.class);
    
    @Autowired(required = false)
    private S3Client s3Client; // Only available in non-dev profiles
    
    @Value("${aws.s3.bucket:}")
    private String bucketName;
    
    @Value("${spring.profiles.active:}")
    private String activeProfiles;
    
    @EventListener(ApplicationReadyEvent.class)
    public void checkBucketConfiguration() {
    	// Skip in dev profile or if no real S3 client
    	if (activeProfiles.contains("dev") || s3Client == null || bucketName.isEmpty()) {
            logger.info("🔄 Skipping S3 bucket check - running in dev mode or no real S3 configured");
            return;
        }
        
    	// Only run for production/non-dev environments
        try {
            logger.info("🔍 Checking S3 bucket configuration for: {}", bucketName);
            s3Client.headBucket(builder -> builder.bucket(bucketName));
            logger.info("✅ Bucket access: SUCCESS");
            checkVersioningStatus();
            checkBucketPolicy();
            logger.info("🎉 S3 bucket configuration check COMPLETED");
        } catch (Exception e) {
            logger.warn("⚠️ S3 bucket configuration check issues: {}", e.getMessage());
        }
    }
    
    private void checkVersioningStatus() {
        try {
            GetBucketVersioningResponse versioning = s3Client.getBucketVersioning(
                builder -> builder.bucket(bucketName));
            
            String status = versioning.statusAsString();
            if ("Enabled".equals(status)) {
                logger.warn("⚠️ S3 Versioning is ENABLED - deleted files will create versions");
                logger.info("💡 Recommendation: Add lifecycle policy or disable versioning");
            } else {
                logger.info("✅ Versioning status: {} (recommended for image cleanup)", status);
            }
        } catch (Exception e) {
            logger.warn("⚠️ Could not check versioning status: {}", e.getMessage());
        }
    }
    
    private void checkBucketPolicy() {
        try {
            s3Client.getBucketPolicy(builder -> builder.bucket(bucketName));
            logger.info("✅ Bucket policy: EXISTS");
        } catch (Exception e) {
            logger.info("ℹ️ No bucket policy configured (using IAM roles)");
        }
    }
}