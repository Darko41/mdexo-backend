package com.doublez.backend.service.cloudfront;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CloudFrontService {
    
    private final String cloudFrontDomain;
    private final boolean cloudFrontEnabled;
    private final Logger logger = LoggerFactory.getLogger(CloudFrontService.class);
    
    public CloudFrontService(
            @Value("${aws.cloudfront.domain:}") String cloudFrontDomain,
            @Value("${aws.cloudfront.enabled:false}") boolean cloudFrontEnabled) {
        this.cloudFrontDomain = cloudFrontDomain;
        this.cloudFrontEnabled = cloudFrontEnabled;
        
        logger.info("🌐 CloudFront Service initialized - Enabled: {}, Domain: {}", 
                   cloudFrontEnabled, cloudFrontDomain);
    }
    
    /**
     * Convert S3 URL to CloudFront CDN URL
     */
    public String convertToCdnUrl(String s3Url) {
        if (!cloudFrontEnabled || cloudFrontDomain == null || cloudFrontDomain.isEmpty()) {
            logger.debug("CDN disabled, using original S3 URL: {}", s3Url);
            return s3Url; // Fallback to direct S3 URL
        }
        
        try {
            // Extract the object key from S3 URL
            // From: https://mdexoawsbucket.s3.eu-north-1.amazonaws.com/real-estates/uuid.jpg
            // To: https://d1q6ktorx7xm8p.cloudfront.net/real-estates/uuid.jpg
            
            URI uri = new URI(s3Url);
            String path = uri.getPath();
            
            // Remove leading slash if present
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            
            String cdnUrl = cloudFrontDomain + "/" + path;
            logger.debug("Converted URL: {} -> {}", s3Url, cdnUrl);
            
            return cdnUrl;
            
        } catch (Exception e) {
            logger.warn("Failed to convert S3 URL to CDN URL: {}, using original", s3Url);
            return s3Url; // Fallback on error
        }
    }
    
    public boolean isCloudFrontEnabled() {
        return cloudFrontEnabled;
    }
}
