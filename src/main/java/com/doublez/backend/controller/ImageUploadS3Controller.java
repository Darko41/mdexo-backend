package com.doublez.backend.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.doublez.backend.entity.RealEstate;
import com.doublez.backend.exception.InvalidFileException;
import com.doublez.backend.response.ApiResponse;
import com.doublez.backend.service.realestate.RealEstateImageService;
import com.doublez.backend.service.realestate.RealEstateService;
import com.doublez.backend.service.validation.FileValidationService;

@RestController
@RequestMapping("api/s3")
public class ImageUploadS3Controller {
    private final RealEstateImageService imageService;
    private final FileValidationService validationService;
    private final RealEstateService realEstateService;

    public ImageUploadS3Controller(RealEstateImageService imageService, 
                                 FileValidationService validationService,
                                 RealEstateService realEstateService) {
        this.imageService = imageService;
        this.validationService = validationService;
        this.realEstateService = realEstateService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "fileName", required = false) String fileName) {
        
        try {
            validationService.validateFile(file);
            String url = imageService.uploadFile(file, fileName);
            return ResponseEntity.ok(ApiResponse.success(url, "File uploaded successfully"));
        } catch (InvalidFileException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Security violation: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Upload failed: " + e.getMessage()));
        }
    }

    @GetMapping("/generate-presigned-url")
    public ResponseEntity<Map<String, String>> generatePresignedUrl(
            @RequestParam String fileName) {
        String url = imageService.generatePresignedUrl(fileName);
        return ResponseEntity.ok(Map.of(
            "url", url,
            "type", url.startsWith("mock://") ? "MOCK" : "S3"
        ));
    }
    
    @PostMapping("/property/{propertyId}/upload")
    public ResponseEntity<ApiResponse<RealEstate>> uploadToProperty(
            @PathVariable Long propertyId,
            @RequestParam("files") MultipartFile[] files) {
        
        try {
            Arrays.stream(files).forEach(validationService::validateFile);
            RealEstate updated = realEstateService.addImagesToProperty(propertyId, files);
            return ResponseEntity.ok(ApiResponse.success(updated));
        } catch (InvalidFileException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Security violation: " + e.getMessage()));
        }
    } 
    
    
}
