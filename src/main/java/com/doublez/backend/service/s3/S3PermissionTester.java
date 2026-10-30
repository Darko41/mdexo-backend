package com.doublez.backend.service.s3;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class S3PermissionTester {
    private static final Logger logger = LoggerFactory.getLogger(S3PermissionTester.class);

    @Autowired
    private S3Service s3Service;

    @Value("${app.s3.permission-test.enabled:false}")
    private boolean permissionTestEnabled;

    @Value("${spring.profiles.active:}")
    private String activeProfiles;

    @EventListener(ApplicationReadyEvent.class)
    public void testS3Permissions() {
        if (!permissionTestEnabled || activeProfiles.contains("dev")) {
            logger.info("🔄 Skipping S3 permission test (enabled={}, profiles={})",
                    permissionTestEnabled, activeProfiles);
            return;
        }

        logger.info("🔍 Starting S3 permissions test...");
        try {
            // ... existing 3 tests unchanged ...
        } catch (Exception e) {
            logger.error("❌ S3 permissions test FAILED: {}", e.getMessage(), e);
            throw new RuntimeException("S3 permissions test failed - check IAM policies", e);
        }
    }
}
