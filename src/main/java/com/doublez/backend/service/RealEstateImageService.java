package com.doublez.backend.service;

import java.io.IOException;
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
}
