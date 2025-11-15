package com.doublez.backend.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.doublez.backend.service.cloudfront.CloudFrontService;
import com.doublez.backend.service.realestate.RealEstateImageService;
import com.doublez.backend.service.s3.S3Service;

@RestController
@RequestMapping("/api/admin/cdn-test")
@PreAuthorize("hasRole('ADMIN')") // Only admins can test
public class CdnTestController {
    
    private final CloudFrontService cloudFrontService;
    private final RealEstateImageService realEstateImageService;
    private final S3Service s3Service;
    
    public CdnTestController(CloudFrontService cloudFrontService,
                           RealEstateImageService realEstateImageService,
                           S3Service s3Service) {
        this.cloudFrontService = cloudFrontService;
        this.realEstateImageService = realEstateImageService;
        this.s3Service = s3Service;
    }
    
    /**
     * Test basic CDN URL conversion
     */
    @GetMapping("/convert")
    public Map<String, Object> testCdnConversion(@RequestParam String s3Url) {
        String cdnUrl = cloudFrontService.convertToCdnUrl(s3Url);
        
        return Map.of(
            "originalS3Url", s3Url,
            "convertedCdnUrl", cdnUrl,
            "cdnEnabled", cloudFrontService.isCloudFrontEnabled(),
            "note", "CDN URLs should work, direct S3 URLs should return 403"
        );
    }
    
    /**
     * Test full image upload flow with CDN
     */
    @PostMapping(value = "/upload-test", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> testUploadWithCdn(@RequestParam("file") MultipartFile file) {
        try {
            // Test the full upload flow
            String imageUrl = realEstateImageService.uploadFile(file, "cdn-test-" + System.currentTimeMillis());
            
            return Map.of(
                "status", "SUCCESS",
                "uploadedImageUrl", imageUrl,
                "isCdnUrl", imageUrl.contains("cloudfront.net"),
                "fileSize", file.getSize(),
                "fileName", file.getOriginalFilename()
            );
            
        } catch (Exception e) {
            return Map.of(
                "status", "ERROR",
                "error", e.getMessage()
            );
        }
    }
    
    /**
     * Test CDN vs S3 access
     */
    @GetMapping("/access-test")
    public Map<String, Object> testAccess() {
        Map<String, Object> result = new HashMap<>();
        result.put("cdnStatus", "ENABLED: " + cloudFrontService.isCloudFrontEnabled());
        
        // You can add HTTP client tests here to verify:
        // - CDN URL returns 200
        // - Direct S3 URL returns 403
        
        result.put("nextSteps", Arrays.asList(
            "1. Upload a test image using /upload-test",
            "2. Try accessing the returned CDN URL in browser",
            "3. Try accessing the direct S3 URL (should get 403)",
            "4. Check CloudFront metrics in AWS Console"
        ));
        
        return result;
    }
    
    /**
     * Test URL conversion with various examples
     */
    @GetMapping("/conversion-examples")
    public Map<String, Object> testConversionExamples() {
        List<String> testUrls = Arrays.asList(
            "https://mdexoawsbucket.s3.eu-north-1.amazonaws.com/real-estates/abc123.jpg",
            "https://mdexoawsbucket.s3.eu-north-1.amazonaws.com/real-estates/test/image.png",
            "https://mdexoawsbucket.s3.eu-north-1.amazonaws.com/other-folder/file.jpg"
        );
        
        Map<String, String> conversions = new HashMap<>();
        for (String s3Url : testUrls) {
            conversions.put(s3Url, cloudFrontService.convertToCdnUrl(s3Url));
        }
        
        return Map.of(
            "conversions", conversions,
            "cdnEnabled", cloudFrontService.isCloudFrontEnabled(),
            "cdnDomain", "https://d1q6ktorx7xm8p.cloudfront.net" // Replace with your actual domain
        );
    }
    
    /**
     * Test both CDN and S3 URL access
     */
    @GetMapping("/verify-access")
    public Map<String, Object> verifyAccess(@RequestParam String cdnUrl) {
        // Convert CDN URL back to S3 URL for testing
        String s3Url = cdnUrl.replace(
            "https://d6oif2udj7x1l.cloudfront.net/",
            "https://mdexoawsbucket.s3.eu-north-1.amazonaws.com/"
        );
        
        return Map.of(
            "cdnUrl", cdnUrl,
            "s3Url", s3Url,
            "expectedBehavior", Map.of(
                "cdnUrl", "SHOULD LOAD IMAGE (200 OK)",
                "s3Url", "SHOULD RETURN 403 ACCESS DENIED"
            ),
            "testInstructions", "Manually open both URLs in browser to verify security"
        );
    }
}
