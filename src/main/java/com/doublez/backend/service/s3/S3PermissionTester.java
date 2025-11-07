package com.doublez.backend.service.s3;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class S3PermissionTester {
    private static final Logger logger = LoggerFactory.getLogger(S3PermissionTester.class);
    
    @Autowired
    private S3Service s3Service;
    
//    @EventListener(ApplicationReadyEvent.class)
    public void testS3Permissions() {
//        logger.info("🔍 Starting S3 permissions test...");
//        
//        try {
//            // Test 1: Generate presigned URL
//            String testKey = "permission-test/" + UUID.randomUUID() + ".txt";
//            String presignedUrl = s3Service.generatePresignedUrl(testKey);
//            logger.info("✅ Presigned URL generation: SUCCESS");
//            
//            // Test 2: Upload a small test file
//            byte[] testData = "S3 Permission Test Content".getBytes();
//            s3Service.uploadFile(presignedUrl, testData, "text/plain");
//            logger.info("✅ File upload: SUCCESS");
//            
//            // Test 3: Delete the test file
//            s3Service.deleteFile(testKey);
//            logger.info("✅ File deletion: SUCCESS");
//            
//            logger.info("🎉 All S3 permission tests PASSED!");
//            
//        } catch (Exception e) {
//            logger.error("❌ S3 permissions test FAILED: {}", e.getMessage(), e);
//            throw new RuntimeException("S3 permissions test failed - check IAM policies", e);
//        }
    }
}
