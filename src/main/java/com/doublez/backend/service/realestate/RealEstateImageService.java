package com.doublez.backend.service.realestate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.doublez.backend.service.image.ImageProcessingService;
import com.doublez.backend.service.s3.S3Service;
import com.doublez.backend.service.validation.FileValidationService;

import jakarta.transaction.Transactional;

@Service
public class RealEstateImageService {
    private static final Logger logger = LoggerFactory.getLogger(RealEstateImageService.class);
    
    private final S3Service s3Service;
    private final ImageProcessingService imageProcessingService;
    private final FileValidationService validationService;
    
    @Value("${app.s3.folder:real-estates}")
    private String s3Folder;

    public RealEstateImageService(S3Service s3Service, 
                                 ImageProcessingService imageProcessingService,
                                 FileValidationService validationService) {
        this.s3Service = s3Service;
        this.imageProcessingService = imageProcessingService;
        this.validationService = validationService;
    }

    // Sequential upload with processing - MEMORY SAFE
    public List<String> uploadRealEstateImages(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }

        validationService.validateFiles(files);
        List<String> imageUrls = new ArrayList<>();

        logger.info("🔄 Starting sequential processing of {} images", files.length);

        // Process images SEQUENTIALLY to avoid memory issues
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            
            if (file.isEmpty()) {
                logger.warn("⚠️ Skipping empty file at index {}", i);
                continue;
            }

            try {
                logger.info("📦 Processing image {}/{}: {} ({} MB)", 
                    i + 1, files.length, file.getOriginalFilename(),
                    String.format("%.1f", file.getSize() / (1024.0 * 1024.0)));

                // Process and upload image one at a time
                String imageUrl = uploadAndProcessImage(file);
                imageUrls.add(imageUrl);
                
                logger.info("✅ Successfully processed and uploaded image {}/{}", i + 1, files.length);
                
                // Force garbage collection between images to free memory
                System.gc();
                
            } catch (Exception e) {
                logger.error("❌ Failed to process image {}: {}", file.getOriginalFilename(), e.getMessage());
                // Continue with next image instead of failing entire batch
            }
        }

        logger.info("🎉 Completed processing all {} images", files.length);
        return imageUrls;
    }

    // Single file upload with processing
    public String uploadAndProcessImage(MultipartFile file) throws IOException {
        validationService.validateFile(file);
        
        logger.info("🔄 Processing image: {} ({} MB)", 
            file.getOriginalFilename(), 
            String.format("%.1f", file.getSize() / (1024.0 * 1024.0)));
        
        // Process image (resize + compress) - this happens one at a time
        byte[] processedImage = imageProcessingService.processImage(file);
        
        String uniqueFilename = generateUniqueFilename(file, null);
        String presignedUrl = s3Service.generatePresignedUrl(uniqueFilename);
        
        // Upload processed image
        s3Service.uploadFile(presignedUrl, processedImage, "image/jpeg");
        
        logger.info("✅ Image processed: {} MB -> {} KB ({}% reduction)",
            String.format("%.1f", file.getSize() / (1024.0 * 1024.0)),
            String.format("%.0f", processedImage.length / 1024.0),
            String.format("%.0f", (1 - (double) processedImage.length / file.getSize()) * 100));
        
        return extractPublicUrl(presignedUrl);
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
        return uploadAndProcessImage(file);
    }

    private String generateUniqueFilename(MultipartFile file, String customName) {
        String originalName = customName != null ? customName : file.getOriginalFilename();
        String extension = "jpg"; // Always use jpg after processing
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

        imageUrls.forEach(url -> {
            try {
                String key = extractS3Key(url);
                s3Service.deleteFile(key);
                logger.info("🗑️ Deleted image from S3: {}", key);
            } catch (Exception e) {
                logger.error("Failed to delete image from S3: {}", url, e);
                // Don't throw exception to allow other deletions to proceed
            }
        });
    }

    private String extractS3Key(String imageUrl) {
        try {
            java.net.URI uri = new java.net.URI(imageUrl);
            String path = uri.getPath();
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (java.net.URISyntaxException e) {
            logger.warn("Invalid image URL format: {}", imageUrl);
            // Fallback: extract key from URL
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("/([^/]+/[^/]+)$");
            java.util.regex.Matcher matcher = pattern.matcher(imageUrl);
            return matcher.find() ? matcher.group(1) : 
                imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
        }
    }

    // Custom exception
    public static class ImageUploadException extends RuntimeException {
        public ImageUploadException(String message) {
            super(message);
        }
        
        public ImageUploadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}