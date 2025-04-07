package com.doublez.backend.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.doublez.backend.exception.ImageUploadException;

import jakarta.transaction.Transactional;

@Service
public class RealEstateImageService {
	
    private final S3Service s3Service;
    private final FileValidationService validationService;
    private static final Logger logger = LoggerFactory.getLogger(RealEstateImageService.class);
    
    public RealEstateImageService(S3Service s3Service, FileValidationService validationService) {
		this.s3Service = s3Service;
		this.validationService = validationService;
	}

	@Value("${app.s3.folder}")
    private String s3Folder;
	@Value("#{'${app.upload.allowed-types}'.split(',')}")
    private List<String> allowedMimeTypesist;
	@Value("${app.upload.max-size}")
	private long maxFileSize;
	
    public List<String> uploadRealEstateImages(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }
        
        validationService.validateFiles(files, allowedMimeTypesist, maxFileSize);
        
        return Arrays.stream(files)
            .map(this::uploadImageWithRetry)
            .collect(Collectors.toList());
    }
    
    private String uploadImageWithRetry(MultipartFile file) {
        int maxAttempts = 3;
        IOException lastException = null;
        
        for (int i = 0; i < maxAttempts; i++) {
            try {
                return uploadSingleImage(file);
            } catch (IOException e) {
                lastException = e;
                logger.warn("Upload attempt {} failed for {}", i + 1, file.getOriginalFilename(), e);
            }
        }
        
        throw new ImageUploadException("Failed to upload image after " + maxAttempts + " attempts", lastException);
    }
    
    private String uploadSingleImage(MultipartFile file) throws IOException {
        String uniqueFilename = generateUniqueFilename(file);
        String presignedUrl = s3Service.generatePresignedUrl(uniqueFilename);
        
        if (file.getSize() > 5_000_000) { // Stream if >5MB
            s3Service.uploadFileStreaming(
                presignedUrl,
                file.getInputStream(),
                file.getSize(),
                file.getContentType()
            );
        } else {
            s3Service.uploadFile(
                presignedUrl,
                file.getBytes(),
                file.getContentType()
            );
        }
        
        return extractPublicUrl(presignedUrl);
    }
    
    private String generateUniqueFilename(MultipartFile file) {
        String originalName = file.getOriginalFilename();
    	String extension = originalName != null ? FilenameUtils.getExtension(originalName) : "jpg";
        String baseName = UUID.randomUUID().toString();
        return String.format("%s/%s.%s", s3Folder, baseName, extension);
    }
    
    private String extractPublicUrl(String presignedUrl) {
        // Handle both real S3 and mock URLs
        return presignedUrl.split("\\?")[0];
    }
    
    @Transactional
    public void deleteImages(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        
        imageUrls.forEach(url -> {
            try {
                // Extract the S3 key from the URL
                String key = extractS3Key(url);
                s3Service.deleteFile(key);
            } catch (Exception e) {
                logger.error("Failed to delete image from S3: {}", url, e);
                // Consider adding a retry mechanism here if needed
            }
        });
    }
    
    private String extractS3Key(String imageUrl) {
        // Assuming your URLs are in format: https://bucket.s3.region.amazonaws.com/folder/filename.jpg
        try {
            URI uri = new URI(imageUrl);
            String path = uri.getPath();
            // Remove leading slash if present
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (URISyntaxException e) {
            logger.warn("Invalid image URL format: {}", imageUrl);
            // Fallback - extract everything after last slash
            return imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
        }
    }
}
