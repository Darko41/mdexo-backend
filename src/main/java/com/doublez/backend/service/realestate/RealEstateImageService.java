package com.doublez.backend.service.realestate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.doublez.backend.exception.ImageUploadException;
import com.doublez.backend.service.s3.S3Service;
import com.doublez.backend.service.validation.FileValidationService;

import jakarta.transaction.Transactional;

@Service
public class RealEstateImageService {
    private final S3Service s3Service;
    private final FileValidationService validationService;
    private static final Logger logger = LoggerFactory.getLogger(RealEstateImageService.class);
    
    @Value("${app.s3.folder}")
    private String s3Folder;

    // Constructor
    public RealEstateImageService(S3Service s3Service, FileValidationService validationService) {
        this.s3Service = s3Service;
        this.validationService = validationService;
    }

    // Bulk upload
    public List<String> uploadRealEstateImages(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }

        validationService.validateFiles(files);

        return Arrays.stream(files)
            .parallel()
            .map(this::processImage)
            .collect(Collectors.toList());
    }

    // Single file upload with custom filename support
    public String uploadFile(MultipartFile file, String customFilename) throws IOException {
        validationService.validateFile(file);
        return uploadImageWithRetry(file, customFilename);
    }

    // Presigned URL generation
    public String generatePresignedUrl(String fileName) {
        return s3Service.generatePresignedUrl(fileName);
    }

    // Private helper methods
    private String processImage(MultipartFile file) {
        try {
            return uploadImageWithRetry(file, null);
        } catch (Exception e) {
            logger.error("Failed to process image: {}", file.getOriginalFilename(), e);
            throw new ImageUploadException("Failed to process image: " + file.getOriginalFilename(), e);
        }
    }

    private String uploadImageWithRetry(MultipartFile file, String customFilename) throws IOException {
        final int maxAttempts = 3;
        IOException lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                String uniqueFilename = generateUniqueFilename(file, customFilename);
                return uploadSingleImage(file, uniqueFilename);
            } catch (IOException e) {
                lastException = e;
                logger.warn("Upload attempt {}/{} failed for {}: {}", 
                    attempt, maxAttempts, file.getOriginalFilename(), e.getMessage());
                
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(1000 * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new ImageUploadException("Upload interrupted", ie);
                    }
                }
            }
        }
        throw new ImageUploadException(
            String.format("Failed to upload %s after %d attempts", 
                file.getOriginalFilename(), maxAttempts), 
            lastException);
    }

    private String uploadSingleImage(MultipartFile file, String uniqueFilename) throws IOException {
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

    private String generateUniqueFilename(MultipartFile file, String customName) {
        String originalName = customName != null ? customName : file.getOriginalFilename();
        String extension = originalName != null ? 
            FilenameUtils.getExtension(originalName) : "jpg";
        String baseName = UUID.randomUUID().toString();
        return String.format("%s/%s.%s", s3Folder, baseName, extension.toLowerCase());
    }

    private String extractPublicUrl(String presignedUrl) {
        return presignedUrl.split("\\?")[0];
    }

    @Transactional
    public void deleteImages(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        imageUrls.parallelStream().forEach(url -> {
            try {
                String key = extractS3Key(url);
                s3Service.deleteFile(key);
            } catch (Exception e) {
                logger.error("Failed to delete image from S3: {}", url, e);
            }
        });
    }

    private String extractS3Key(String imageUrl) {
        try {
            URI uri = new URI(imageUrl);
            String path = uri.getPath();
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (URISyntaxException e) {
            logger.warn("Invalid image URL format: {}", imageUrl);
            Pattern pattern = Pattern.compile("/([^/]+/[^/]+)$");
            Matcher matcher = pattern.matcher(imageUrl);
            return matcher.find() ? matcher.group(1) : 
                imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
        }
    }
}
